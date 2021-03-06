## 1. Netty初始化过程
当服务端创建一个channel的时候，会立刻为这个channel创建对应的ChannelPipeline对象，
Channel和ChannelPipeline创建好之后就会建立二者的关联关系，并且这种关联关系在Channel的
整个生命周期当中是不会发生改变的。也就是说，Channel一旦绑定到某个ChannelPipeline上就不会
在去绑定到其他的ChannelPipeline上，他们是一种一对一的关系。
对于ChannelPipeline来说，他里面维护的是一个又一个的ChannelHandler，实际是由一个双向链表来去构造的，
在创建每个ChannelHandle的同时又会为他创建对应的ChannelHandlerContext对象，
ChannelHandlerContext对象是连接ChannelPipeline和ChannelHandler的桥梁和纽带。
从根本上来说，ChannelPipeline当中维护的实际上是一个又一个的ChannelHandlerContext对象，
而ChannelHandlerContext对象里面是可以引用到对应的ChannelHandler对象的。这样就建立好这些组件之间的关联关系。
而ChannelHandler本向又是分为两个方向的，inbound和outbound，即入站和出站。
当数据从外界进来时，netty会检查ChannelPipeline中每一个ChannelHandler，
然后判断ChannelHandler是inbound还是outbound（源码中直接使用的是instanceof关键字进行判断的），
和Servelt的Filter以及Spring MVC中的Interceptor不同，Filter和Interceptor是即拦截进来的，也拦截出去的。
对于Netty的ChannelHandler来说，入站和出站是分开的，入站的数据不会流经出站的Handler的，反之亦然。
自定义ChannelHandler时可以同时实现inbound接口和outbound接口，如此即可以拦截入站的数据，也可以拦截出站的数据。
但是不建立这样做。无论什么时候都建议分别定义Inbound Handler和Outbound Handler。

## 2. Netty架构设计原则
  1. 一个EventLoopGroup当中会包含一个或多个EventLoop。
  2. 一个EventLoop在它的整个生命周期当中都只会与唯一一个Thread进行绑定。
  3. 所有由EventLoop所处理的各种I/O事件都将在它所关联的那个Thread上进行处理。
  4. 一个Channel在它的整个生命周期中只会注册在一个EventLoop上。
  5. 一个EventLoop在运行过程当中，会被分配给一个或者多个Channel。

* 基于上面的5点，得到如下结论：
  1. 在Netty中，Channel的实现是线程安全的；基于此，我们可以存储一个Channel的引用，
  并且在需要向远程端发送数据时，通过这个引用来调用Channel的相应方法；
  即便当前有很多线程都在使用它也不会出现多线程问题；而且消息一定会按照顺序发送出去。
  2. 我们在业务开发时，不要将长时间执行的耗时任务放到EventLoop的执行队列中（如：ChannelHandler的channelRead0()方法），
  因为它将会一直阻塞该线程所对应的所有Channel上的其他执行任务，如果我们需要进行阻塞调用
  或是耗时的操作，那么我们使用一个专门的EventExecutor（业务线程池）。

* 两种业务线程池实现方式：
  1. 在ChannelHandle的回调方法中，使用自己定义的业务线程池，这样就可以实现异步调用。
  2. 借助于Netty提供的向ChannelPipeline添加ChannelHandler时调用的addLast方法来传递EventExecutor。

## 3. 
默认情况下（调用addLast(name, handler)），ChannelHandler中的回调方法都是由I/O线程所执行，
如果调用了```ChannelPipeline addLast(EventExecutorGroup group, String name, ChannelHandler handler);```方法，
那么ChannelHandler中的回调方法就是由参数中的group线程组来执行。
```java
public interface ChannelPipeline
        extends ChannelInboundInvoker, ChannelOutboundInvoker, Iterable<Entry<String, ChannelHandler>> {

    ...

    /**
     * Appends a {@link ChannelHandler} at the last position of this pipeline.
     *
     * @param group    这个 {@link EventExecutorGroup} 会用于执行 {@link ChannelHandler} 中的方法
     * @param name     the name of the handler to append
     * @param handler  the handler to append
     *
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     * @throws NullPointerException
     *         if the specified handler is {@code null}
     */
    ChannelPipeline addLast(EventExecutorGroup group, String name, ChannelHandler handler);

    ...

}
```

## 4. 
JDK8之前所提供的Future只能通过手工方式检查执行结果，而这个操作是阻塞的；
Netty则对ChannelFuture进行了增强，通过ChannelFutureListener以回调的方式来获取执行结果，去除了手工检查执行结果导致阻塞的操作；
值得注意的是：ChannelFutureListener的operationComplete()方法是由I/O线程执行的，因此要注意的是不要在这里执行耗时操作，
应该通过另外的线程或线程池来执行耗时操作。

## 5. Netty中有两种发送消息的方式
* ```ctx.channel().writeAndFlush(obj);``` 直接写到Channel中，消息会从ChannelPipeline的末尾开始流动；
* ```ctx.writeAndFlush(obj);``` 写到与ChannelHandler所关联的那个ChannelHandleContext中，消息将从ChannelPipeline中的下一个ChannelHandler开始流动。
* 结论：
  1. ChannelHandlerContext与ChannelHandler之间的关联绑定关系是永远都不会发生改变的，因此对其进行缓存是没有任何问题的。
  2. 对于Channel的同名方法来说，ChannelHandlerContext的方法将会产生更短的事件流，所以我们应该在可能的情况下利用这个特性来提升应用性能。

## 6. [Netty ByteBuf](https://github.com/baayso/note/blob/master/java/netty/ByteBuf.md)所提供的3种缓冲区类型：
> 最佳实践：对于后端业务消息的编解码推荐使用HeapByteBuf；对于I/O通信线程在读写缓冲区时，推荐使用DirectByteBuf。
* Heap Buffer (堆缓冲区)
  * 最常用的缓冲区，ByteBuf将数据存储到JVM的堆空间中，并将实际的数据存放到byte array中来实现。
  * 优点：由于数据是存储在JVM堆中，因此可以快速的创建与快速的释放，并且它提供了直接访问内部字节数组的方法。
  * 缺点：每次读写数据时，都需要先将数据复制到直接缓冲区中再时行网络传输。

* Direct Buffer (直接缓冲区)
  * 在JVM堆之外直接分配的内存空间，直接缓冲区并不会占用JVM堆的内存空间，因为它是由操作在本地内存进行分配的。
  * 优点：在使用Socket进行数据传输时，性能非常好，因为数据直接位于操作系统的本地内存中，所以不需要从JVM将数据复制到直接缓冲区中。
  * 缺点：因为Direct Buffer是直接在操作系统内存中的，所以内存空间的分配与释放要比JVM堆空间更加复杂且速度会慢一些。但是Netty通过提供内存池来解决这个问题。直接缓冲区并不支持通过字节数组的方式来访问数据。

* Composite Buffer (复合缓冲区)
  * 一个虚拟缓冲区，它将多个缓冲区展示为一个合并的缓冲区。建议使用```ByteBufAllocator.compositeBuffer()```或``` Unpooled.wrappedBuffer(ByteBuf...)```，而不是显式地调用构造函数。

## 7. Netty的[ByteBuf](https://github.com/baayso/note/blob/master/java/netty/ByteBuf.md)相较于JDK的[ByteBuffer](https://github.com/baayso/note/blob/master/java/nio/nio.md#2-nio-buffer)的不同之处
* Netty的ByteBuf采用了读写索引分离的策略(readerIndex 和 writerIndex)，一个初始化(里面尚未有任何数据)的ByteBuf的readerIndex与writerIndex值都为0。
* 当readerIndex和writerIndex处于同一个位置时，如果继续读取，将会抛出IndexOutOfBoundsException。
* 对于ByteBuf的任何读写操作都会分别单独维护读索引和写索引。其maxCapacity字段的最大空间默认的限制是Integer.MAX_VALUE。
* JDK的ByteBuffer的缺点：
  * ```final byte[] hb;```这行代码是JDK的ByteBuffer对象中用于存储数据的声明，其字节数组被声明为final的，也就是长度是固定不变的。一旦分配好后不能动态扩容与收缩。当待存储的数据字节很大时就很有可能抛出IndexOutOfBoundsException。如果要预防这个异常，那就需要在存储之前完全确定好待存储的字节大小 。如果ByteBuffer的空间不足，只有一种解决方案：创建一个全新的ByteBuffer对象，然后再将之前的ByteBuffer中的数据复制过去，但这一切都需要由开发者自己手动编码完成。
  * ByteBuffer只使用一个position指针来标识位置信息，在进行读写切换时需要调用flip()方法或者是rewind()方法使用起来很不方便。
* Netty的ByteBuf的优点：
  * 存储字节的数组是动态的，其长度最大值默认是Integer.MAX_VALUE。这里的动态性是体现在write()方法中，write()方法在执行时会判断buffer的容量，如果不足则自动扩容。
  * ByteBuf的读写索引是完全分开的，使用起来很方便。

## 8. Netty 处理器(Handler)
* Netty Handler分为两类：Inbound Handler(入站处理器) 与 Outbound Handler(出站处理器)。
* 入站处理器的顶层类是```ChannelInboundHandler```，出站处理器的顶层类是```ChannelOutboundHandler```。
* 编解码器：
  * 数据处理时常用的各种编解码器本质上都是处理器(Handler)。
  * 无论我们向网络中写入的数据是什么类型(int、char、String、二进制等)，数据在网络中传递时，其都是以字节流的形式呈现的。
  * 编码器通常以XxxEncoder命名；解码器通常以XxxDecoder命名，但并不是所有的编解码器都遵循这个命名规则。
  * 编解码统一称为codec。
  * 编码：本质上是一种出站处理器；因此，编码是一种```ChannelOutboundHandler```。
  * 解码：本质上是一种入站处理器；因此，解码是一种```ChannelInboundHandler```。
    * 在解码器进行数据解码时，一定要记得判断```ByteBuf```中的数据是否足够，否则将会产生一些问题。
  * 无论是编码器还是解码器，其所接收的消息类型必须要和待处理的参数类型一致，否则该编码器或解码器并不会被执行。
    ```java
    /**
     * Returns {@code true} if the given message should be handled. If {@code false} it will be passed to the next
     * {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     */
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return matcher.match(msg);
    }
    ```
  * 编解码器的一些抽象类，具体编解码器都继承这些抽象类，包名：```io.netty.handler.codec```
    * ```ByteToMessageDecoder```抽象类
    * ```ByteToMessageCodec<I>```抽象类
    * [```ReplayingDecoder<S> extends ByteToMessageDecoder```](https://github.com/baayso/note/blob/master/java/netty/ReplayingDecoder.java)抽象类
    * ```MessageToByteEncoder<I>```抽象类
    * ```MessageToMessageEncoder<I>```抽象类
    * ```MessageToMessageDecoder<I>```抽象类
    * ```MessageToMessageCodec<INBOUND_IN, OUTBOUND_IN> extends ChannelDuplexHandler```抽象类
  * 常用的编解码器，包名：```io.netty.handler.codec```
    * ```LineBasedFrameDecoder```
      ```java
      /**
       * A decoder that splits the received {@link ByteBuf}s on line endings.
       * <p>
       * Both {@code "\n"} and {@code "\r\n"} are handled.
       * For a more general delimiter-based decoder, see {@link DelimiterBasedFrameDecoder}.
       */
      public class LineBasedFrameDecoder extends ByteToMessageDecoder {
      }
      ```
    * ```FixedLengthFrameDecoder```
      ```java
      /**
       * A decoder that splits the received {@link ByteBuf}s by the fixed number
       * of bytes. For example, if you received the following four fragmented packets:
       * <pre>
       * +---+----+------+----+
       * | A | BC | DEFG | HI |
       * +---+----+------+----+
       * </pre>
       * A {@link FixedLengthFrameDecoder}{@code (3)} will decode them into the
       * following three packets with the fixed length:
       * <pre>
       * +-----+-----+-----+
       * | ABC | DEF | GHI |
       * +-----+-----+-----+
       * </pre>
       */
      public class FixedLengthFrameDecoder extends ByteToMessageDecoder {
      }
      ```
    * ```DelimiterBasedFrameDecoder```
      ```java
      /**
       * A decoder that splits the received {@link ByteBuf}s by one or more
       * delimiters.  It is particularly useful for decoding the frames which ends
       * with a delimiter such as {@link Delimiters#nulDelimiter() NUL} or
       * {@linkplain Delimiters#lineDelimiter() newline characters}.
       *
       * <h3>Predefined delimiters</h3>
       * <p>
       * {@link Delimiters} defines frequently used delimiters for convenience' sake.
       *
       * <h3>Specifying more than one delimiter</h3>
       * <p>
       * {@link DelimiterBasedFrameDecoder} allows you to specify more than one
       * delimiter.  If more than one delimiter is found in the buffer, it chooses
       * the delimiter which produces the shortest frame.  For example, if you have
       * the following data in the buffer:
       * <pre>
       * +--------------+
       * | ABC\nDEF\r\n |
       * +--------------+
       * </pre>
       * a {@link DelimiterBasedFrameDecoder}({@link Delimiters#lineDelimiter() Delimiters.lineDelimiter()})
       * will choose {@code '\n'} as the first delimiter and produce two frames:
       * <pre>
       * +-----+-----+
       * | ABC | DEF |
       * +-----+-----+
       * </pre>
       * rather than incorrectly choosing {@code '\r\n'} as the first delimiter:
       * <pre>
       * +----------+
       * | ABC\nDEF |
       * +----------+
       * </pre>
       */
      public class DelimiterBasedFrameDecoder extends ByteToMessageDecoder {
      }
      ```
    * ```LengthFieldBasedFrameDecoder```
      ```java
      /**
       * A decoder that splits the received {@link ByteBuf}s dynamically by the
       * value of the length field in the message.  It is particularly useful when you
       * decode a binary message which has an integer header field that represents the
       * length of the message body or the whole message.
       * <p>
       * {@link LengthFieldBasedFrameDecoder} has many configuration parameters so
       * that it can decode any message with a length field, which is often seen in
       * proprietary client-server protocols. Here are some example that will give
       * you the basic idea on which option does what.
       *
       * <h3>2 bytes length field at offset 0, do not strip header</h3>
       *
       * The value of the length field in this example is <tt>12 (0x0C)</tt> which
       * represents the length of "HELLO, WORLD".  By default, the decoder assumes
       * that the length field represents the number of the bytes that follows the
       * length field.  Therefore, it can be decoded with the simplistic parameter
       * combination.
       * <pre>
       * <b>lengthFieldOffset</b>   = <b>0</b>
       * <b>lengthFieldLength</b>   = <b>2</b>
       * lengthAdjustment    = 0
       * initialBytesToStrip = 0 (= do not strip header)
       *
       * BEFORE DECODE (14 bytes)         AFTER DECODE (14 bytes)
       * +--------+----------------+      +--------+----------------+
       * | Length | Actual Content |----->| Length | Actual Content |
       * | 0x000C | "HELLO, WORLD" |      | 0x000C | "HELLO, WORLD" |
       * +--------+----------------+      +--------+----------------+
       * </pre>
       *
       * <h3>2 bytes length field at offset 0, strip header</h3>
       *
       * Because we can get the length of the content by calling
       * {@link ByteBuf#readableBytes()}, you might want to strip the length
       * field by specifying <tt>initialBytesToStrip</tt>.  In this example, we
       * specified <tt>2</tt>, that is same with the length of the length field, to
       * strip the first two bytes.
       * <pre>
       * lengthFieldOffset   = 0
       * lengthFieldLength   = 2
       * lengthAdjustment    = 0
       * <b>initialBytesToStrip</b> = <b>2</b> (= the length of the Length field)
       *
       * BEFORE DECODE (14 bytes)         AFTER DECODE (12 bytes)
       * +--------+----------------+      +----------------+
       * | Length | Actual Content |----->| Actual Content |
       * | 0x000C | "HELLO, WORLD" |      | "HELLO, WORLD" |
       * +--------+----------------+      +----------------+
       * </pre>
       *
       * <h3>2 bytes length field at offset 0, do not strip header, the length field
       *     represents the length of the whole message</h3>
       *
       * In most cases, the length field represents the length of the message body
       * only, as shown in the previous examples.  However, in some protocols, the
       * length field represents the length of the whole message, including the
       * message header.  In such a case, we specify a non-zero
       * <tt>lengthAdjustment</tt>.  Because the length value in this example message
       * is always greater than the body length by <tt>2</tt>, we specify <tt>-2</tt>
       * as <tt>lengthAdjustment</tt> for compensation.
       * <pre>
       * lengthFieldOffset   =  0
       * lengthFieldLength   =  2
       * <b>lengthAdjustment</b>    = <b>-2</b> (= the length of the Length field)
       * initialBytesToStrip =  0
       *
       * BEFORE DECODE (14 bytes)         AFTER DECODE (14 bytes)
       * +--------+----------------+      +--------+----------------+
       * | Length | Actual Content |----->| Length | Actual Content |
       * | 0x000E | "HELLO, WORLD" |      | 0x000E | "HELLO, WORLD" |
       * +--------+----------------+      +--------+----------------+
       * </pre>
       *
       * <h3>3 bytes length field at the end of 5 bytes header, do not strip header</h3>
       *
       * The following message is a simple variation of the first example.  An extra
       * header value is prepended to the message.  <tt>lengthAdjustment</tt> is zero
       * again because the decoder always takes the length of the prepended data into
       * account during frame length calculation.
       * <pre>
       * <b>lengthFieldOffset</b>   = <b>2</b> (= the length of Header 1)
       * <b>lengthFieldLength</b>   = <b>3</b>
       * lengthAdjustment    = 0
       * initialBytesToStrip = 0
       *
       * BEFORE DECODE (17 bytes)                      AFTER DECODE (17 bytes)
       * +----------+----------+----------------+      +----------+----------+----------------+
       * | Header 1 |  Length  | Actual Content |----->| Header 1 |  Length  | Actual Content |
       * |  0xCAFE  | 0x00000C | "HELLO, WORLD" |      |  0xCAFE  | 0x00000C | "HELLO, WORLD" |
       * +----------+----------+----------------+      +----------+----------+----------------+
       * </pre>
       *
       * <h3>3 bytes length field at the beginning of 5 bytes header, do not strip header</h3>
       *
       * This is an advanced example that shows the case where there is an extra
       * header between the length field and the message body.  You have to specify a
       * positive <tt>lengthAdjustment</tt> so that the decoder counts the extra
       * header into the frame length calculation.
       * <pre>
       * lengthFieldOffset   = 0
       * lengthFieldLength   = 3
       * <b>lengthAdjustment</b>    = <b>2</b> (= the length of Header 1)
       * initialBytesToStrip = 0
       *
       * BEFORE DECODE (17 bytes)                      AFTER DECODE (17 bytes)
       * +----------+----------+----------------+      +----------+----------+----------------+
       * |  Length  | Header 1 | Actual Content |----->|  Length  | Header 1 | Actual Content |
       * | 0x00000C |  0xCAFE  | "HELLO, WORLD" |      | 0x00000C |  0xCAFE  | "HELLO, WORLD" |
       * +----------+----------+----------------+      +----------+----------+----------------+
       * </pre>
       *
       * <h3>2 bytes length field at offset 1 in the middle of 4 bytes header,
       *     strip the first header field and the length field</h3>
       *
       * This is a combination of all the examples above.  There are the prepended
       * header before the length field and the extra header after the length field.
       * The prepended header affects the <tt>lengthFieldOffset</tt> and the extra
       * header affects the <tt>lengthAdjustment</tt>.  We also specified a non-zero
       * <tt>initialBytesToStrip</tt> to strip the length field and the prepended
       * header from the frame.  If you don't want to strip the prepended header, you
       * could specify <tt>0</tt> for <tt>initialBytesToSkip</tt>.
       * <pre>
       * lengthFieldOffset   = 1 (= the length of HDR1)
       * lengthFieldLength   = 2
       * <b>lengthAdjustment</b>    = <b>1</b> (= the length of HDR2)
       * <b>initialBytesToStrip</b> = <b>3</b> (= the length of HDR1 + LEN)
       *
       * BEFORE DECODE (16 bytes)                       AFTER DECODE (13 bytes)
       * +------+--------+------+----------------+      +------+----------------+
       * | HDR1 | Length | HDR2 | Actual Content |----->| HDR2 | Actual Content |
       * | 0xCA | 0x000C | 0xFE | "HELLO, WORLD" |      | 0xFE | "HELLO, WORLD" |
       * +------+--------+------+----------------+      +------+----------------+
       * </pre>
       *
       * <h3>2 bytes length field at offset 1 in the middle of 4 bytes header,
       *     strip the first header field and the length field, the length field
       *     represents the length of the whole message</h3>
       *
       * Let's give another twist to the previous example.  The only difference from
       * the previous example is that the length field represents the length of the
       * whole message instead of the message body, just like the third example.
       * We have to count the length of HDR1 and Length into <tt>lengthAdjustment</tt>.
       * Please note that we don't need to take the length of HDR2 into account
       * because the length field already includes the whole header length.
       * <pre>
       * lengthFieldOffset   =  1
       * lengthFieldLength   =  2
       * <b>lengthAdjustment</b>    = <b>-3</b> (= the length of HDR1 + LEN, negative)
       * <b>initialBytesToStrip</b> = <b> 3</b>
       *
       * BEFORE DECODE (16 bytes)                       AFTER DECODE (13 bytes)
       * +------+--------+------+----------------+      +------+----------------+
       * | HDR1 | Length | HDR2 | Actual Content |----->| HDR2 | Actual Content |
       * | 0xCA | 0x0010 | 0xFE | "HELLO, WORLD" |      | 0xFE | "HELLO, WORLD" |
       * +------+--------+------+----------------+      +------+----------------+
       * </pre>
       * @see LengthFieldPrepender
       */
      public class LengthFieldBasedFrameDecoder extends ByteToMessageDecoder {
      }
      ```
