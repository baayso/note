# 零拷贝(zero copy)
> 以操作系统的视角来解析

## 0. 用户空间和内核空间
* 对于32位操作系统而言，它的寻址空间（虚拟存储空间）为4G（2的32次方）。操心系统的核心是内核，独立于普通的应用程序，可以访问受保护的内存空间，也有访问底层硬件设备的所有权限。
* 为了保证用户进程不能直接操作内核，保证内核的安全，操心系统将虚拟空间划分为两部分，一部分为内核空间，一部分为用户空间。
* 针对linux操作系统而言，将最高的1G字节（从虚拟地址0xC0000000到0xFFFFFFFF），供内核使用，称为内核空间，而将较低的3G字节（从虚拟地址0x00000000到0xBFFFFFFF），供各个进程使用，称为用户空间。
* 内核态与用户态
  * 当一个任务（进程）执行系统调用而陷入内核代码中执行时，称进程处于内核运行态（内核态）。
  * 当进程在执行用户自己的代码时，则称其处于用户运行态（用户态）。
* 以上内容摘自：https://www.cnblogs.com/Anker/p/3269106.html

## 1. 传统IO底层操作机制
* 第一步：JVM向操作系统发起`read()`系统调用，***由用户空间切换到内核空间***。
* 第二步：操作系统向硬件发出读取数据的请求。
* 第三步：通过`DMA`(直接内存访问)机制将数据读取到内核空间的缓冲区当中。**第一次数据拷贝**。
* 第四步：将内核空间缓冲区的数据原样拷贝到用户空间的缓冲区当中。**第二次数据拷贝**。***由内核空间切换到用户空间***。
* 第五步：进行业务逻辑处理...
* 第六步：JVM向操作系统发起`write()`系统调用，将用户空间缓冲区的数据拷贝到内核空间的`socket`缓冲区当中。**第三次数据拷贝**。***由用户空间切换到内核空间***。
* 第七步：操作系统将内核空间中`socket`缓冲区的数据写入硬件。**第四次数据拷贝**。
* 第八步：硬件通知操作系统完成数据写入。
* 第九步：`write()`系统调用完成并返回结果，***由内核空间切换到用户空间***。
![传统IO底层操作机制](zero-copy_1.png)

## 2. 零拷贝底层操作机制
> 数据不会在从内核空间拷贝到用户空间中，**所有的操作都在内核空间中完成**。
* 第一步：JVM向操作系统发起`sendfile()`系统调用，***由用户空间切换到内核空间***。
* 第二步：操作系统向硬件发出读取数据的请求。
* 第三步：通过`DMA`(直接内存访问)机制将数据读取到内核空间的缓冲区当中。**第一次数据拷贝**。
* 第四步：将内核空间缓冲区的数据写入将要发送数据的`socket`缓冲区当中。**第二次数据拷贝**
* 第五步：操作系统将内核空间中`socket`缓冲区的数据写入硬件。**第三次数据拷贝**
* 第六步：硬件通知操作系统完成数据写入。
* 第七步：`sendfile()`系统调用完成并返回结果，***由内核空间切换到用户空间***。
![零拷贝底层操作机制-1](zero-copy_2.png)
![零拷贝底层操作机制-2](zero-copy_5.png)

## 3. 改进的(底层使用了Gather)零拷贝底层操作机制
* 第一步：JVM向操作系统发起`sendfile()`系统调用，***由用户空间切换到内核空间***。
* 第二步：操作系统向硬件发出读取数据的请求。
* 第三步：通过`DMA`(直接内存访问)机制将数据读取到内核空间的缓冲区当中。**第一次数据拷贝**。
* 第四步：将内核空间缓冲区数据的*描述符*拷贝到`socket`缓冲区之中。
  * *描述符*包含以下两个信息：
    * 1) 内核缓冲区数据的内存地址
    * 2) 内核缓冲区数据的长度(即需要读多少数据)
* 第五步：协议引擎根据`socket`缓冲区存储的*描述符*信息从内核空间缓冲区中读取数据(Gather)，然后写入对端。
* 第六步：`sendfile()`系统调用完成并返回结果，***由内核空间切换到用户空间***。
![改进的零拷贝底层操作机制-1](zero-copy_6.jpg)
![改进的零拷贝底层操作机制-2](zero-copy_3.png)

## 4. 使用内存映射文件来解决需要修改数据的问题
![使用内存映射文件来解决需要修改数据的问题](zero-copy_4.png)

## 5. Java中使用零拷贝的代码示例：
* Java中通过`java.nio.channels.FileChannel`中的`transferTo()`方法来支持零拷贝，内部实现就是使用操作系统底层的`sendfile()`系统调用。
  ```java
  FileChannel fileChannel = new FileInputStream(fileName).getChannel();
  // transferTo() 方法直接将字节从它被调用的通道上传输到另外一个可写字节通道上，数据无需流经应用程序。
  long transferCount = fileChannel.transferTo(0, fileChannel.size(), socketChannel);
  ```
* `java.nio.channels.FileChannel#transferTo()`方法签名：
  ```java
  public abstract long transferTo(long position, long count, WritableByteChannel target) throws IOException;
  ```

## 6. Java底层实现零拷贝的代码：
* https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/sun/nio/ch/FileChannelImpl.java#L1211
  ```java
  package sun.nio.ch;

  public class FileChannelImpl extends FileChannel {

  ...

      // -- Native methods --

      // Creates a new mapping
      private native long map0(int prot, long position, long length)
          throws IOException;

      // Removes an existing mapping
      private static native int unmap0(long address, long length);

      // Transfers from src to dst, or returns -2 if kernel can't do that
      private native long transferTo0(FileDescriptor src, long position,
                                      long count, FileDescriptor dst);

  ...
  }
  ```

* https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/jdk8u202-b08/jdk/src/solaris/native/sun/nio/ch/FileChannelImpl.c#L139
  ```c
  JNIEXPORT jlong JNICALL
  Java_sun_nio_ch_FileChannelImpl_transferTo0(JNIEnv *env, jobject this,
                                              jobject srcFDO,
                                              jlong position, jlong count,
                                              jobject dstFDO)
  {
      jint srcFD = fdval(env, srcFDO);
      jint dstFD = fdval(env, dstFDO);

  #if defined(__linux__)
      off64_t offset = (off64_t)position;
      jlong n = sendfile64(dstFD, srcFD, &offset, (size_t)count);
      if (n < 0) {
          if (errno == EAGAIN)
              return IOS_UNAVAILABLE;
          if ((errno == EINVAL) && ((ssize_t)count >= 0))
              return IOS_UNSUPPORTED_CASE;
          if (errno == EINTR) {
              return IOS_INTERRUPTED;
          }
          JNU_ThrowIOExceptionWithLastError(env, "Transfer failed");
          return IOS_THROWN;
      }
      return n;
  #elif defined (__solaris__)
      sendfilevec64_t sfv;
      size_t numBytes = 0;
      jlong result;

      sfv.sfv_fd = srcFD;
      sfv.sfv_flag = 0;
      sfv.sfv_off = (off64_t)position;
      sfv.sfv_len = count;

      result = sendfilev64(dstFD, &sfv, 1, &numBytes);

      /* Solaris sendfilev() will return -1 even if some bytes have been
       * transferred, so we check numBytes first.
       */
      if (numBytes > 0)
          return numBytes;
      if (result < 0) {
          if (errno == EAGAIN)
              return IOS_UNAVAILABLE;
          if (errno == EOPNOTSUPP)
              return IOS_UNSUPPORTED_CASE;
          if ((errno == EINVAL) && ((ssize_t)count >= 0))
              return IOS_UNSUPPORTED_CASE;
          if (errno == EINTR)
              return IOS_INTERRUPTED;
          JNU_ThrowIOExceptionWithLastError(env, "Transfer failed");
          return IOS_THROWN;
      }
      return result;
  #elif defined(__APPLE__)
      off_t numBytes;
      int result;

      numBytes = count;

      result = sendfile(srcFD, dstFD, position, &numBytes, NULL, 0);

      if (numBytes > 0)
          return numBytes;

      if (result == -1) {
          if (errno == EAGAIN)
              return IOS_UNAVAILABLE;
          if (errno == EOPNOTSUPP || errno == ENOTSOCK || errno == ENOTCONN)
              return IOS_UNSUPPORTED_CASE;
          if ((errno == EINVAL) && ((ssize_t)count >= 0))
              return IOS_UNSUPPORTED_CASE;
          if (errno == EINTR)
              return IOS_INTERRUPTED;
          JNU_ThrowIOExceptionWithLastError(env, "Transfer failed");
          return IOS_THROWN;
      }

      return result;

  ...

  #else
      return IOS_UNSUPPORTED_CASE;
  #endif
  }
  ```
  
  * https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/windows/native/sun/nio/ch/FileChannelImpl.c#L156
  
