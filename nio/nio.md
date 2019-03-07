# NIO

## 1. java.io 与 java.nio
* java.io中最为核心的一个概念是流（Stream），面向流的编程。Java中，一个流要么是输入流，要么是输出流，不会同时即是输入流又是输出流。
* java.nio中拥有3个核心概念：Selector、Channel与Buffer。在java.nio中，是面向块（block）或是缓冲区（buffer）编程。Buffer本身就是一块内存，底层实现上，它实际上是一个数组。数据的读、写都是通过Buffer来实现的。除了数组之外，Buffer还提供了对于数据的结构化访问方式，并且可以追踪系统的读写过程。
* Java中的**8种**原生数据类型有**7种**都有各自对应的Bufffer类型，如：IntBuffer、LongBuffer、ByteBuffer、CharBuffer等等，但是**没有**BooleanBuffer类型。
* Channel指的是可以向其写入数据或是从中读取数据的对象，它类似于java.io中的Stream。
* 所有数据的读写都是通过Buffer来进行的，永远不会出现直接向Channel写入数据的情况，或是直接从Channel读取数据的情况。
* 与Stream不同的是，Channel是双向的，一个流只可能是InputStream或是OutputStream，但是Channel打开后则可以进行读取、写入或是读写。
* 由于Channel是双向的，因此它可以更好地反映出底层操作系统的真实情况；在Linux操作系统中，底层的通道就是双向的。

## 2. NIO Buffer
* java.nio.Buffer抽象类很多子类都是由机器生成的，比如ByteBuffer、IntBuffer、DirectByteBuffer等等。  
  `// -- This file was mechanically generated: Do not edit! -- //`
* 3个重要的状态属性含义：position、limit与capacity。  
  * `0 <= mark <= position <= limit <= capacity`
  * capacity 是缓冲区所包含的元素的数量。缓冲区的capacity不能为负并且不能更改
  * limit 是第一个不应该读取或写入的元素的索引。缓冲区的limit不能为负，并且不能大于capacity
  * position 是下一个要读取或写入的元素的索引。缓冲区的position不能为负，并且不能大于limit
* clear()
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
     * <p> 这个方法实际上并没有擦除缓冲区中的数据，但是它的名称就好像擦除了一样，因为它通常会在这种情况下使用。 </p>
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
* flip()：**Buffer有两种模式，写模式和读模式。在写模式下调用flip()之后，Buffer从写模式变成读模式。**
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
* rewind()
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

## 3. 通过NIO读取文件涉及到3个步骤：
1. 从FileInputStream对象中获取FileChannel对象
2. 创建Buffer
3. 将数据从Channel读取到Buffer中
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

## 4. 

