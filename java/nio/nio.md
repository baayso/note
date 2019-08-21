# NIO

## 1. `java.io` 与 `java.nio`
* `java.io`中最为核心的一个概念是流（`Stream`），面向流的编程。Java中，一个流要么是输入流，要么是输出流，不会同时即是输入流又是输出流。
* `java.nio`中拥有3个核心概念：`Selector`、`Channel`与`Buffer`。在`java.nio`中，是面向块（block）或是缓冲区（buffer）编程。`Buffer`本身就是一块内存，底层实现上，它实际上是一个数组。数据的读、写都是通过`Buffer`来实现的。除了数组之外，`Buffer`还提供了对于数据的结构化访问方式，并且可以追踪系统的读写过程。
* Java中的**8种**原生数据类型有**7种**都有各自对应的Bufffer类型，如：`IntBuffer`、`LongBuffer`、`ByteBuffer`、`CharBuffer`等等，但是**没有**`BooleanBuffer`类型。
* `Channel`指的是可以向其写入数据或是从中读取数据的对象，它类似于`java.io`中的`Stream`。
* 所有数据的读写都是通过`Buffer`来进行的，永远不会出现直接向`Channel`写入数据的情况，或是直接从`Channel`读取数据的情况。
* 与`Stream`不同的是，`Channel`是双向的，一个流只可能是`InputStream`或是`OutputStream`，但是`Channel`打开后则可以进行读取、写入或是读写。
* 由于`Channel`是双向的，因此它可以更好地反映出底层操作系统的真实情况；在Linux操作系统中，底层的通道就是双向的。

## 2. NIO Buffer
* `java.nio.Buffer`抽象类很多子类都是由机器生成的，比如`ByteBuffer`、`IntBuffer`、`DirectByteBuffer`等等。  
  `// -- This file was mechanically generated: Do not edit! -- //`
* 3个重要的状态属性含义：position、limit与capacity。
  * `0 <= mark <= position <= limit <= capacity`
  * capacity 是缓冲区所包含的元素的数量。缓冲区的capacity不能为负并且不能更改。
  * limit 是第一个不应该读取或写入的元素的索引。缓冲区的limit不能为负，并且不能大于capacity。
  * position 是下一个要读取或写入的元素的索引。缓冲区的position不能为负，并且不能大于limit。
* `ByteBuffer`
  * `ByteBuffer`是一个抽象类，我们不能`new`一个`ByteBuffer`的实例，只能通过`ByteBuffer`的静态方法 `allocate(int capacity)` 和 `allocateDirect(int capacity)` 来创建。
  * `allocate(int capacity)`实际创建的是`HeapByteBuffer`实例。
    ```java
    public static ByteBuffer allocate(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return new HeapByteBuffer(capacity, capacity);
    }
    ```
  * `allocateDirect(int capacity)`实际创建的是 [`DirectByteBuffer`](#DirectByteBuffer) 实例。
    ```java
    public static ByteBuffer allocateDirect(int capacity) {
        return new DirectByteBuffer(capacity);
    }
    ```
* `Buffer#clear()`
  ```java
  /**
   * 清除缓冲区。position设置为0，limit设置为其capacity，mark则被丢弃。
   *
   * <p> 在使用一系列Channel的读取或put操作来填充此缓冲区之前调用此方法。例如:
   *
   * <blockquote><pre>
   * buf.clear();     // Prepare buffer for reading
   * in.read(buf);    // Read data
   * </pre></blockquote>
   *
   * <p> 这个方法实际上并没有擦除缓冲区中的数据，但是它的名称就好像可以擦除一样，因为它通常会在这种情况下使用。 </p>
   *
   * @return  This buffer
   */
  public final Buffer clear() {
      position = 0;
      limit = capacity;
      mark = -1;
      return this;
  }
  ```
* `Buffer#flip()`：**Buffer有两种模式，写模式和读模式。在写模式下调用flip()方法之后，Buffer从写模式变成读模式。**
  ```java
  /**
   * 翻转这个缓冲区。将limit设置为当前position，然后将position设置为零。如果定义了标记，则将其丢弃。
   *
   * <p> 在一系列Channel的读操作或put操作之后，调用此方法为一系列通道写操作或相关get操作做准备。例如:
   *
   * <blockquote><pre>
   * buf.put(magic);    // Prepend header
   * in.read(buf);      // Read data into rest of buffer
   * buf.flip();        // Flip buffer
   * out.write(buf);    // Write header + data to channel
   * </pre></blockquote>
   *
   * <p> 当将数据从一个地方传输到另一个地方时，经常将此方法与 {@link java.nio.ByteBuffer#compact()} 方法一起使用。 </p>
   *
   * @return  This buffer
   */
  public final Buffer flip() {
      limit = position;
      position = 0;
      mark = -1;
      return this;
  }
  ```
* `Buffer#rewind()`
  ```java
  /**
   * 倒带这个缓冲区。将position设置为0并丢弃mark。
   *
   * <p> 在一系列Channel的写操作或get操作之前调用此方法，假设已经适当设置了limit。例如:
   *
   * <blockquote><pre>
   * out.write(buf);    // Write remaining data
   * buf.rewind();      // Rewind buffer
   * buf.get(array);    // Copy data into array
   * </pre></blockquote>
   *
   * @return  This buffer
   */
  public final Buffer rewind() {
      position = 0;
      mark = -1;
      return this;
  }
  ```
* `ByteBuffer`类型化的`put()`方法与`get()`方法：
  ```java
  public static void main(String args[]) {

      ByteBuffer buffer = ByteBuffer.allocate(64);

      buffer.put((byte) 1);
      buffer.putLong(123456789L);
      buffer.putInt(61);
      buffer.putChar('南');
      buffer.putDouble(3.1415926);
      buffer.putChar('京');
      buffer.putShort((short) 2);

      buffer.flip();

      System.out.println(buffer.get());
      System.out.println(buffer.getLong());
      System.out.println(buffer.getInt());
      System.out.println(buffer.getChar());
      System.out.println(buffer.getDouble());
      System.out.println(buffer.getChar());
      System.out.println(buffer.getShort());
  }
  ```
* `只读Buffer`，我们可以随机在一个`普通Buffer`上调用`asReadOnlyBuffer()`方法返回一个`只读Buffer`，但是不能将一个`只读Buffer`转换为`读写Buffer`。

## 3. <span id="DirectByteBuffer">NIO堆外内存与零拷贝</span>
> 推荐阅读：Java NIO中，关于DirectBuffer，HeapBuffer的疑问？ - RednaxelaFX的回答 - 知乎
https://www.zhihu.com/question/57374068/answer/152691891
* `HeapByteBuffer`
  * `HeapByteBuffer`底层的字节数组是使用Java堆来进行存储的。然而对于操作系统来说，在进行IO操作的时候，操作系统并不会直接处理`HeapByteBuffer`底层所封装的存储在Java堆上的字节数组（注：可以通过JNI的方式来让操作系统访问Java堆上的内存，但是由于GC的存在导致了一些不确定性），而是会将Java堆上的字节数组的内容原样拷贝一份到Java堆外的某一块内存当中，然后使用拷贝到堆外内存的数据跟IO设备进行交互。如此就会多一个数据拷贝的过程。
  * 对于没有超高性能要求的情况下，使用`HeapByteBuffer`的性价比是比较高的。因为数据拷贝的过程很快，而IO操作相对来说比较慢。
* `DirectByteBuffer`
  * `DirectByteBuffer`底层所封装的字节数组为null（`final byte[] hb`为`null`），也就是Java堆上没有存储数据，而是直接将数据存储在堆外内存之中（`Buffer`类中有一个`long`类型的成员变量`address`，用于保存堆外内存的地址，如此就可以通过`address`这个成员变量来访问堆外内存。之所以将这个成员变量放在`Buffer`类中而不是放在实际使用的类中是为了加快JNI方法`GetDirectBufferAddress`的调用速度）。在进行IO操作时，操作系统直接使用堆外内存中的数据直接跟IO设备进行交互。对比`HeapByteBuffer`则少了一个数据拷贝的过程，标准术语我们称之为零拷贝（Zero Copy）。
  * `DirectByteBuffer`类的实例依然存储在Java堆中。
  * `DirectByteBuffer`构造函数中使用了`sun.misc.Unsafe#allocateMemory()`native方法申请堆外内存：
    ```java
    // -- This file was mechanically generated: Do not edit! -- //

    package java.nio;

    import java.io.FileDescriptor;
    import sun.misc.Cleaner;
    import sun.misc.Unsafe;
    import sun.misc.VM;
    import sun.nio.ch.DirectBuffer;

    class DirectByteBuffer extends MappedByteBuffer implements DirectBuffer {

        ...

        DirectByteBuffer(int cap) {

            super(-1, 0, cap, cap);
            boolean pa = VM.isDirectMemoryPageAligned();
            int ps = Bits.pageSize();
            long size = Math.max(1L, (long)cap + (pa ? ps : 0));
            Bits.reserveMemory(size, cap);

            long base = 0;
            try {
                // allocateMemory() 是一个 native 方法，查看sun.misc.Unsafe类源码可以看到如下注释及方法签名：
                // /// wrappers for malloc, realloc, free:
                // public native long allocateMemory(long bytes);
                // allocateMemory() 用于申请堆外内存，底层是使用C语言的 malloc() 函数向操作系统申请的内存，不在JVM的GC管控之内，
                // 且用完需要使用 free() 函数手动释放内存。
                // 注：从Java9以后，sun.misc.Unsafe类的包路径改成了jdk.internal.misc.Unsafe
                base = unsafe.allocateMemory(size);
            } catch (OutOfMemoryError x) {
                Bits.unreserveMemory(size, cap);
                throw x;
            }
            unsafe.setMemory(base, size, (byte) 0);
            if (pa && (base % ps != 0)) {
                // address 是 Buffer类中的一个 long类型的成员变量，用于保存堆外内存的地址，注释及定义如下：
                // Used only by direct buffers
                // NOTE: hoisted here for speed in JNI GetDirectBufferAddress
                // long address;
                // Round up to page boundary
                address = base + ps - (base & (ps - 1));
            } else {
                // address 是 Buffer类中的一个 long类型的成员变量，用于保存堆外内存的地址，注释及定义如下：
                // Used only by direct buffers
                // NOTE: hoisted here for speed in JNI GetDirectBufferAddress
                // long address;
                address = base;
            }
            cleaner = Cleaner.create(this, new Deallocator(base, size, cap));
            att = null;
        }

        ...
    }
    ```
* `MappedByteBuffer`
  * 抽象类，继承自`ByteBuffer`类，是`DirectByteBuffer`的父类。
  * 是一个直接字节缓冲区，其内容是一个文件的内存映射区域。
  * `MappedByteBuffer`通过`java.nio.channels.FileChannel#map()`方法创建。
  * 映射的字节缓冲区及它所表示的文件映射在缓冲区本身被垃圾回收之前一直保持有效。
  * `MappedByteBuffer`是一种允许Java程序直接从内存访问的一种特殊的文件，可以将整个文件或者文件的一部分映射到内存当中，接下来由操作系统负责相关的页面请求并且将内存的修改写入到文件当中。应用程序只需要处理内存中的数据，这样可以实现非常迅速的IO操作。用于内存映射文件的这个内存是堆外内存。
  * `MappedByteBuffer`使用示例：
    ```java
    File file = new File("text.txt");
    try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
        FileChannel fileChannel = randomAccessFile.getChannel();

        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, file.length());

        mappedByteBuffer.put(0, (byte) 'a');
        mappedByteBuffer.put(3, (byte) 'b');
    }
    ```

## 4. NIO Buffer 的 Scattering 和 Gathering
### 4.1 Scattering（分散，一个分散成多个）
* 从`Channel`中读取数据时不仅可以写入到一个`Buffer`中，还可以写入到一个`Buffer数组`。
* 例如：需要从`Channel`中读取数据并写入`Buffer`中，假设`Channel`中有20个字节，此时我们可以传递一个`ByteBuffer数组`，`ByteBuffer数组`第一个元素的`capacity`属性为`5`，第二个元素的`capacity`属性也为`5`，第三个元素的`capacity`属性也为`10`。当从`Channel`中读取数据并写入到`ByteBuffer数组`中时，会先将`ByteBuffer数组`的第一个元素写满，然后在往`ByteBuffer数组`的第二个元素中写入数据，如果第二个元素也被写满，在向`ByteBuffer数组`的第三个元素中写入数据，直到`Channel`中所有字节全部读完。也就是把来自于一个`Channel`中的数据给写入到了多个`Buffer`当中，总是会按照顺序并只有当`Buffer数组`的前一个元素被写满才会去向下一个元素中写入数据，如果`Buffer数组`的前一个元素没有被写满则不会向下一个元素中写入数据。

### 4.2 Gathering（聚集，多个聚集成一个）
* 往`Channel`中写入数据时可以传递一个`Buffer数组`。也就是将一个`Buffer数组`中的所有数据按数组元素次序写入到一个`Channel`中，只有当数组的前一个元素被读完才会去读下一个元素中的数据。

### 4.3 应用场景
* 比如在进行网络数据传输的时候使用自定义协议，自定义协议的报文格式的第一个Head是5个字节，第二个Head是10个字节，第三个是Body它长度是可变的。此时就可以使用Buffer的Scattering，在读取数据时将第一个Head的5个字节读到Buffer数组的第一个元素中，第二个Head的10个字节读到Buffer的第二个元素中，第三个是Body把它读到Buffer数组的第三个元素当中。如此就实现了数据的分类，而不用将所有的数据都读到一个Buffer中，然后在去解析这个Buffer。

### 4.4 示例代码（以下为服务端代码，客户端可以使用telnet、nc）：
```java
public static void main(String args[]) throws Exception {

    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    InetSocketAddress address = new InetSocketAddress(8899);
    serverSocketChannel.socket().bind(address);

    int firstMsgLen = 3;
    int secondMsgLen = 4;
    int thirdMsgLen = 3;

    int totalMsgLen = firstMsgLen + secondMsgLen + thirdMsgLen;

    ByteBuffer buffers[] = new ByteBuffer[3];
    buffers[0] = ByteBuffer.allocate(firstMsgLen);
    buffers[1] = ByteBuffer.allocate(secondMsgLen);
    buffers[2] = ByteBuffer.allocate(thirdMsgLen);

    SocketChannel socketChannel = serverSocketChannel.accept();

    while (true) {
        // Scatter-read into buffers
        int readBytes = 0;
        while (readBytes < totalMsgLen) {
            long r = socketChannel.read(buffers);
            readBytes += r;

            System.out.println("bytesRead: " + readBytes);

            System.out.println("r " + r);

            Arrays.stream(buffers)
                    .map(buffer -> "position: " + buffer.position() + ", limit: " + buffer.limit())
                    .forEach(System.out::println);
        }

        // Process message here

        // Flip buffers
        Arrays.asList(buffers).forEach(Buffer::flip);

        // Scatter-write back out
        long writeBytes = 0;
        while (writeBytes < totalMsgLen) {
            long r = socketChannel.write(buffers);
            writeBytes += r;
        }

        // Clear buffers
        Arrays.asList(buffers).forEach(Buffer::clear);

        System.out.println(readBytes + " " + writeBytes + " " + totalMsgLen);
    }
}
```

## 5. `Channel`
* `Channel`可以直接将指定文件的部分或全部直接映射成`Buffer`。
* 程序不能直接访问`Channel`中的数据，包括读取、写入都不行，`Channel`只能与`Buffer`进行交互。也就是说，如果要从`Channel`中取得数据，必须先用`Buffer`从`Channel`中取出一些数据，然后让程序从`Buffer`中取出这些数据；如果要将程序中的数据写入`Channel`，一样先让程序将数据放入`Buffer`中，程序再将`Buffer`里的数据写入`Channel`中。
* `Channel`接口的实现类：
  * 用于读取、写入、映射和操作文件的Channel：
    * `FileChannel`
  * 用于支持线程之间通信的管道Channel：
    * `Pipe.SinkChannel`
    * `Pipe.SourceChannel`
  * 用于支持TCP网络通信的Channel：
    * `ServerSocketChannel`
    * `SocketChannel`
  * 用于支持UDP网络通信的Channel：
    * `DatagramChannel`
* `Channel`中最常用的**三类**方法：
  * `map()`: 用于将`Channel`对应的部分或全部数据数据映射成`MappedByteBuffer`。
  * `read()`: 有一系列重载形式，用于从`Channel`中读取数据写入到`Buffer`中。
  * `write()`: 有一系列重载形式，用于将`Buffer`中的数据写入`Channel`。
* 示例代码：使用`FileChannel`从一个文件中读取数据并写入到另一个文件中。
  ```java  
  try (FileInputStream inputStream = new FileInputStream("input.txt");
       FileOutputStream outputStream = new FileOutputStream("output.txt")) {

      // 1. 从FileInputStream对象获取到FileChannel对象
      FileChannel inChannel = inputStream.getChannel();
      FileChannel outChannel = outputStream.getChannel();

      // 2. 创建Buffer对象
      ByteBuffer buffer = ByteBuffer.allocate(16);

      while (true) {
          buffer.clear();

          // 3. 读取Channel中的数据写入到Buffer对象中
          int r = inChannel.read(buffer);

          System.out.println("read " + r + " bytes.");

          if (r <= 0) {
              break;
          }

          // 在写模式下调用flip()方法之后，Buffer从写模式变成读模式
          buffer.flip();

          outChannel.write(buffer);
      }
  }
  catch (FileNotFoundException e) {
      e.printStackTrace();
  }
  catch (IOException e) {
      e.printStackTrace();
  }
  ```

## 6. `Selector`
* `Selector`是`SelectableChannel`对象的多路复用器，采用非阻塞方式进行通信的`Channel`都应该注册到`Selector`对象。可以通过调用此类的`open()`静态方法来创建`Selector`实例，该方法将使用系统默认的`Selector`来返回新的`Selector`。
  ```java
  public static Selector open() throws IOException {
      return SelectorProvider.provider().openSelector();
  }
  ```
* `Selector`可以同时监控多个`SelectableChannel`的IO状况，是非阻塞IO的核心。
* 一个`Selector`实例有三个`SelectionKey`集合：
  * 所有的`SelectionKey`集合：代表了注册在该`Selector`上的`Channel`，这个集合可以通过`keys()`方法返回。
  * 被选择的`SelectionKey`集合：代表了所有可通过`select()`方法获取的、需要进行IO处理的`Channel`，这个集合可以通过`selectedKeys()`方法返回。
  * 被取消的`SelectionKey`集合：代表了所有被取消注册关系的`Channel`，在下一次执行`select()`方法时，这些`Channel`对应的`SelectionKey`会被彻底删除，程序通常无须直接访问该集合。
* `SelectionKey`中用静态常量定义了4种IO操作：
  * `public static final int OP_READ = 1 << 0;` // 1
  * `public static final int OP_WRITE = 1 << 2;` // 4
  * `public static final int OP_CONNECT = 1 << 3;` // 8
  * `public static final int OP_ACCEPT = 1 << 4;` // 16
* 示例代码：
  ```java
  int ports[] = new int[3];
  ports[0] = 6001;
  ports[1] = 6002;
  ports[2] = 6003;

  // Create a new selector
  Selector selector = Selector.open();

  // Open a listener on each port, and register each one
  // with the selector
  for (int i = 0; i < ports.length; ++i) {
      ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.configureBlocking(false);
      ServerSocket serverSocket = serverSocketChannel.socket();
      InetSocketAddress address = new InetSocketAddress(ports[i]);
      serverSocket.bind(address);

      SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

      System.out.println("Going to listen on " + ports[i]);
  }

  while (true) {
      int num = selector.select();

      Set<SelectionKey> selectedKeys = selector.selectedKeys();

      System.out.println("selectedKeys: " + selectedKeys);

      Iterator<SelectionKey> it = selectedKeys.iterator();

      while (it.hasNext()) {
          SelectionKey selectionKey = it.next();

          if (selectionKey.isAcceptable()) {
              // Accept the new connection
              ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
              SocketChannel channel = serverSocketChannel.accept();
              channel.configureBlocking(false);

              // Add the new connection to the selector
              SelectionKey newKey = channel.register(selector, SelectionKey.OP_READ);
              it.remove();

              System.out.println("Got connection from " + channel.toString());
          }
          else if (selectionKey.isReadable()) {
              // Read the data
              SocketChannel channel = (SocketChannel) selectionKey.channel();

              int writeBytes = 0;

              ByteBuffer buffer = ByteBuffer.allocate(10);

              while (true) {
                  buffer.clear();

                  int r = channel.read(buffer);

                  if (r <= 0) {
                      break;
                  }

                  buffer.flip();

                  channel.write(buffer);

                  writeBytes += r;
              }

              System.out.println("Echoed " + writeBytes + " from " + channel);

              it.remove();
          }
      }
  }
  ```

