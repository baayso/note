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
  * capacity 是缓冲区所包含的元素的数量。缓冲区的capacity不能为负并且不能更改
  * limit 是第一个不应该读取或写入的元素的索引。缓冲区的limit不能为负，并且不能大于capacity
  * position 是下一个要读取或写入的元素的索引。缓冲区的position不能为负，并且不能大于limit
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
* `Buffer#flip()`：**Buffer有两种模式，写模式和读模式。在写模式下调用flip()之后，Buffer从写模式变成读模式。**
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
* `只读Buffer`，我们可以随机在一个`普通Buffer`上调用`asReadOnlyBuffer()`方法返回一个`只读Buffer`，但是不能将一个`只读Buffer`转换为`读写Buffer`。

## 3. <span id="DirectByteBuffer">NIO堆外内存与零拷贝</span>
* `HeapByteBuffer`
  * 当使用`HeapByteBuffer`，其底层的字节数组使用的是Java堆来进行存储的。然而对于操作系统来说，在进行IO操作的时候，操作系统并不会直接处理`HeapByteBuffer`底层所封装的存储在Java堆上的字节数组，而是会将Java堆上的字节数组的内容原样拷贝一份到Java堆外的某一块内存当中，然后使用拷贝到堆外内存的数据跟IO设备进行交互。如此就会多一个数据拷贝的过程。
* `DirectByteBuffer`
  * `DirectByteBuffer`底层所封装的字节数组为null（`final byte[] hb`为`null`），也就是Java堆上没有存储数据，而是直接将数据存储在堆外内存之中。在进行IO操作时，操作系统直接使用堆外内存中的数据直接跟IO设备进行交互。对比`HeapByteBuffer`则少了一个数据拷贝的过程，标准术语我们称之为零拷贝（Zero Copy）。
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
            // allocateMemory() 用于申请堆外内存，底层是使用C语言的 malloc() 函数向计算机申请的内存，不在JVM的GC管控之内，
            // 用完需要使用 free() 函数手动释放内存。
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
```

## 4. 通过NIO读取文件涉及到3个步骤：
1. 从`FileInputStream`对象中获取`FileChannel`对象
2. 创建`Buffer`
3. 将数据从`Channel`读取到`Buffer`中
```java
    try (FileInputStream inputStream = new FileInputStream("text.txt")) {
        FileChannel channel = inputStream.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        channel.read(buffer);

        buffer.flip();

        int i = 0;
        while (buffer.remaining() > 0) {
            byte b = buffer.get();
            System.out.println("Character " + i + ": " + ((char) b));
            i++;
        }
    }
    catch (FileNotFoundException e) {
        e.printStackTrace();
    }
    catch (IOException e) {
        e.printStackTrace();
    }
```

## 5. 

