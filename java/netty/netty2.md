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
