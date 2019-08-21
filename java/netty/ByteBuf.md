# io.netty.buffer.ByteBuf

```java
package io.netty.buffer;

import ...

/**
 * 零个或多个字节（八位字节）的随机和顺序可访问序列。 
 * 此接口为一个或多个原始字节数组（{@code byte[]}）和{@linkplain ByteBuffer NIO buffers}提供抽象视图。
 *
 * <h3>创建缓冲区</h3>
 *
 * 建议使用{@link Unpooled}中的辅助方法创建一个新缓冲区，而不是调用每一个具体实现的构造函数。
 *
 * <h3>随机访问索引</h3>
 *
 * 就像一个普通的原生字节数组一样，ByteBuf使用
 * <a href="http://en.wikipedia.org/wiki/Zero-based_numbering">从零开始的索引（zero-based indexing）</a>。 
 * 这意味着第一个字节的索引始终为{@code 0}，最后一个字节的索引始终为{@link #capacity() capacity - 1}。
 * 例如，要迭代buffer的所有字节，无论其内部实现如何，都可以执行以下操作：
 *
 * <pre>
 * {@link ByteBuf} buffer = ...;
 * for (int i = 0; i &lt; buffer.capacity(); i ++) {
 *     byte b = buffer.getByte(i);
 *     System.out.println((char) b);
 * }
 * </pre>
 *
 * <h3>顺序访问索引</h3>
 *
 * {@link ByteBuf}提供了两个指针变量来支持顺序读取和写入操作：
 * 读取操作的{@link #readerIndex() readerIndex}和写入操作的{@link #writerIndex() writerIndex}。 
 * 下图展示了如何通过两个指针将buffer分为三个区域：
 *
 * <pre>
 *      +-------------------+------------------+------------------+
 *      | discardable bytes |  readable bytes  |  writable bytes  |
 *      |    (已读取字节)    |     (CONTENT)    |                  |
 *      +-------------------+------------------+------------------+
 *      |                   |                  |                  |
 *      0      <=      readerIndex   <=   writerIndex    <=    capacity
 * </pre>
 *
 * <h4>可读字节（实际内容）</h4>
 *
 * 这个区域是存储实际数据的位置。名称以{@code read}或{@code skip}开头的任何操作都将获取或跳过当前{@link #readerIndex() readerIndex}的数据，
 * 然后增加已经读取的字节数量。
 * 如果读取操作的参数还是{@link ByteBuf}并且未指定目标索引，则指定缓冲区的{@link #writerIndex() writerIndex}也会一起增加。
 * <p>
 * 当没有足够的内容时继续进行读取操作的话，则引发{@link IndexOutOfBoundsException}。
 * 新创建的、包装的或是拷贝的buffer的{@link #readerIndex() readerIndex}的默认值为{@code 0}。
 *
 * <pre>
 * // 迭代buffer的可读字节
 * {@link ByteBuf} buffer = ...;
 * while (buffer.isReadable()) {
 *     System.out.println(buffer.readByte());
 * }
 * </pre>
 *
 * <h4>可写字节</h4>
 *
 * 这个区域是未定义的空间，需要被填充。名称以{@code write}开头的任何操作都会将数据写入当前{@link #writerIndex() writerIndex}的位置，然后增加写入的字节数。
 * 如果写操作的参数还是{@link ByteBuf}，并且未指定源索引，则指定缓冲区的{@link #readerIndex() readerIndex}也会一起增加。
 * <p>
 * 如果没有足够的字节可以写入的话，则引发{@link IndexOutOfBoundsException}。
 * 新分配的buffer的{@link #writerIndex() writerIndex}的默认值为{@code 0}。
 * 而包装的或是拷贝的buffer的{@link #writerIndex() writerIndex}的默认值则是buffer的{@link #capacity() capacity}。
 *
 * <pre>
 * // 使用随机整数填充buffer的可写字节
 * {@link ByteBuf} buffer = ...;
 * while (buffer.maxWritableBytes() >= 4) {
 *     buffer.writeInt(random.nextInt());
 * }
 * </pre>
 *
 * <h4>可丢弃字节（已读取字节）</h4>
 *
 * 这个区域包含了已经被读操作读取过的字节。最开始时，这个区域的size为{@code 0}，但在执行读操作时，其size会增加到{@link #writerIndex() writerIndex}。
 * 已读取过的字节可以通过调用{@link #discardReadBytes()}方法丢弃掉，来回收未使用的空间，如下图所示：
 *
 * <pre>
 *  BEFORE discardReadBytes()
 *
 *      +-------------------+------------------+------------------+
 *      | discardable bytes |  readable bytes  |  writable bytes  |
 *      +-------------------+------------------+------------------+
 *      |                   |                  |                  |
 *      0      <=      readerIndex   <=   writerIndex    <=    capacity
 *
 *
 *  AFTER discardReadBytes()
 *
 *      +------------------+--------------------------------------+
 *      |  readable bytes  |    writable bytes (got more space)   |
 *      +------------------+--------------------------------------+
 *      |                  |                                      |
 * readerIndex (0) <= writerIndex (decreased)        <=        capacity
 * </pre>
 *
 * 请注意，调用{@link #discardReadBytes()}方法之后无法保证可写字节的内容。
 * 在大多数情况下，可写字节不会被移动，甚至可以根据底层缓冲区实现填充完全不同的数据。
 *
 * <h4>清除缓冲区索引</h4>
 *
 * You can set both {@link #readerIndex() readerIndex} and
 * {@link #writerIndex() writerIndex} to {@code 0} by calling {@link #clear()}.
 * It does not clear the buffer content (e.g. filling with {@code 0}) but just
 * clears the two pointers.  Please also note that the semantic of this
 * operation is different from {@link ByteBuffer#clear()}.
 *
 * <pre>
 *  BEFORE clear()
 *
 *      +-------------------+------------------+------------------+
 *      | discardable bytes |  readable bytes  |  writable bytes  |
 *      +-------------------+------------------+------------------+
 *      |                   |                  |                  |
 *      0      <=      readerIndex   <=   writerIndex    <=    capacity
 *
 *
 *  AFTER clear()
 *
 *      +---------------------------------------------------------+
 *      |             writable bytes (got more space)             |
 *      +---------------------------------------------------------+
 *      |                                                         |
 *      0 = readerIndex = writerIndex            <=            capacity
 * </pre>
 *
 * <h3>Search operations</h3>
 *
 * For simple single-byte searches, use {@link #indexOf(int, int, byte)} and {@link #bytesBefore(int, int, byte)}.
 * {@link #bytesBefore(byte)} is especially useful when you deal with a {@code NUL}-terminated string.
 * For complicated searches, use {@link #forEachByte(int, int, ByteProcessor)} with a {@link ByteProcessor}
 * implementation.
 *
 * <h3>Mark and reset</h3>
 *
 * There are two marker indexes in every buffer. One is for storing
 * {@link #readerIndex() readerIndex} and the other is for storing
 * {@link #writerIndex() writerIndex}.  You can always reposition one of the
 * two indexes by calling a reset method.  It works in a similar fashion to
 * the mark and reset methods in {@link InputStream} except that there's no
 * {@code readlimit}.
 *
 * <h3>Derived buffers</h3>
 *
 * You can create a view of an existing buffer by calling one of the following methods:
 * <ul>
 *   <li>{@link #duplicate()}</li>
 *   <li>{@link #slice()}</li>
 *   <li>{@link #slice(int, int)}</li>
 *   <li>{@link #readSlice(int)}</li>
 *   <li>{@link #retainedDuplicate()}</li>
 *   <li>{@link #retainedSlice()}</li>
 *   <li>{@link #retainedSlice(int, int)}</li>
 *   <li>{@link #readRetainedSlice(int)}</li>
 * </ul>
 * A derived buffer will have an independent {@link #readerIndex() readerIndex},
 * {@link #writerIndex() writerIndex} and marker indexes, while it shares
 * other internal data representation, just like a NIO buffer does.
 * <p>
 * In case a completely fresh copy of an existing buffer is required, please
 * call {@link #copy()} method instead.
 *
 * <h4>Non-retained and retained derived buffers</h4>
 *
 * Note that the {@link #duplicate()}, {@link #slice()}, {@link #slice(int, int)} and {@link #readSlice(int)} does NOT
 * call {@link #retain()} on the returned derived buffer, and thus its reference count will NOT be increased. If you
 * need to create a derived buffer with increased reference count, consider using {@link #retainedDuplicate()},
 * {@link #retainedSlice()}, {@link #retainedSlice(int, int)} and {@link #readRetainedSlice(int)} which may return
 * a buffer implementation that produces less garbage.
 *
 * <h3>Conversion to existing JDK types</h3>
 *
 * <h4>Byte array</h4>
 *
 * If a {@link ByteBuf} is backed by a byte array (i.e. {@code byte[]}),
 * you can access it directly via the {@link #array()} method.  To determine
 * if a buffer is backed by a byte array, {@link #hasArray()} should be used.
 *
 * <h4>NIO Buffers</h4>
 *
 * If a {@link ByteBuf} can be converted into an NIO {@link ByteBuffer} which shares its
 * content (i.e. view buffer), you can get it via the {@link #nioBuffer()} method.  To determine
 * if a buffer can be converted into an NIO buffer, use {@link #nioBufferCount()}.
 *
 * <h4>Strings</h4>
 *
 * Various {@link #toString(Charset)} methods convert a {@link ByteBuf}
 * into a {@link String}.  Please note that {@link #toString()} is not a
 * conversion method.
 *
 * <h4>I/O Streams</h4>
 *
 * Please refer to {@link ByteBufInputStream} and
 * {@link ByteBufOutputStream}.
 */
@SuppressWarnings("ClassMayBeInterface")
public abstract class ByteBuf implements ReferenceCounted, Comparable<ByteBuf> {

    ...

}
```
