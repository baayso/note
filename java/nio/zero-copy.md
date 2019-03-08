# 零拷贝(zero copy)
> 以操作系统的视角来解析

## 1. 传统IO底层操作机制
![传统IO底层操作机制](https://github.com/baayso/note/blob/master/java/nio/zero-copy_1.png)

## 2. 零拷贝底层操作机制
![零拷贝底层操作机制](https://github.com/baayso/note/blob/master/java/nio/zero-copy_2.png)

## 3. 改进的零拷贝底层操作机制
![改进的零拷贝底层操作机制](https://github.com/baayso/note/blob/master/java/nio/zero-copy_3.png)

## 4. 使用内存映射文件来解决需要修改数据的问题
![使用内存映射文件来解决需要修改数据的问题](https://github.com/baayso/note/blob/master/java/nio/zero-copy_4.png)

## 5. 解析操作系统底层如何实现零拷贝
![操作系统底层如何实现零拷贝](https://github.com/baayso/note/blob/master/java/nio/zero-copy_5.png)

## 6. Java中实现零拷贝的代码：
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
  
