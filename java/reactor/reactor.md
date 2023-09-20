# Reactor

## 1. 传统Socket编程模式所面临的问题：
> 编程模式简单清晰，适用并发量小的场景。
* 一个Socket一个线程，服务器需要大量的线程用来和客户端通信，但是服务器线程数量会受到服务器资源的限制并不能无限制的创建。
* 线程上下文切换的开销成本会随着线程数量的增长越来越高。
* 连接建立好后，可能并不会一直存在着数据传输，但是服务器线程需要一直保持，造成了资源浪费。

## 2. Reactor模式的角色构成（Reactor模式一共有5种角色构成）：
![Reactor模式类图](reactor-pattern%20class%20diagram.png)
1) `Handle（句柄或者叫描述符）`：表示一种资源，是由操作系统提供的；该资源用于表示一个个的事件，比如说文件描述符，或是针对网络编程中的Socket描述符。事件即可以来自于外部，也可以来自于内部；外部事件比如说客户端的连接请求，客户端发送过来的数据等；内部事件比如说操作系统产生的定时器事件等。它本质上是一个文件描述符。Handle是事件产生的发源地。
2) `Synchronous Event Demultiplexer（同步事件分离器）`：它本身是一个系统调用，用于等待事件的发生（事件可能是一个，也可能是多个）。调用方在调用它的时候会被阻塞，一直阻塞到同步事件分离器上有事件产生为止。对于Linux来说，同步事件分离器指的就是常用的`I/O多路复用机制`，如`select`、`poll`、`epoll`等。在Java NIO领域中，同步事件分离器对应的组件就是`Selector`；对应的阻塞方法就是`select()`方法。
3) `Event Handler（事件处理器）`：由多个回调方法构成，这些回调方法构成了与应用相关的对于某个事件的反馈机制。Netty相比于Java NIO来说，在事件处理器这个角色上进行了升级，为开发者提供了大量的回调方法，供我们在特定事件产生时实现相应的回调方法进行业务逻辑的处理。
4) `Concrete Event Handler（具体事件处理器）`：`事件处理器`的实现。它本身实现了`事件处理器`所提供的各个回调方法，从而实现了特定于业务的逻辑。它本质上就是我们所编写的一个个的处理器实现。
5) `Initiation Dispatcher（初始分发器）`：实际上就是`Reactor角色`。其本身定义了一些规范，这些规范用于控制事件的调度方式，同时又提供了应用进行事件处理器的注册、删除等设施。是整个事件处理器的核心，`Initiation Dispatcher`会通过`同步事件分离器`来等待事件的发生。一旦事件发生，`Initiation Dispatcher`首先会**分离出每一个事件**（分离事件：遍历SelectionKey集合，取出集合中的每一个SelectionKey。一个SelectionKey就是一个事件。），然后调用`事件处理器`，最后调用相关的回调方法来处理这些事件。

## 3. Reactor模式的流程：
![Reactor模式类图](reactor-pattern%20class%20diagram.png)
1) 当应用向`Initiation Dispatcher`注册具体的`事件处理器`时，应用会标识出该事件处理器希望`Initiation Dispatcher`在某个事件发生时向其通知的该事件，该事件与`Handle`关联。
2) `Initiation Dispatcher`要求每个`事件处理器`向其传递内部的`Handle`。该`Handle`向操作系统标识了事件处理器。
3) 当所有的`事件处理器`注册完成后，应用会调用`handle_events()`方法来启动`Initiation Dispatcher`的事件循环。这时，`Initiation Dispatcher`会将每个注册的`事件处理器`的`Handle`放入集合之中，并使用`同步事件分离器`等待这些事件的发生。比如说，TCP协议层会使用`select`同步事件分离器来等待客户端发送的数据到达连接的`socket handle`上。
4) 当与某个事件源对应的`Handle`变为`ready状态`时（比如TCP socket变为等待读状态时），`同步事件分离器`就会通知`Initiation Dispatcher`。
5) `Initiation Dispatcher`会触发`事件处理器`的回调方法，从而响应这个处于ready状态的`Handle`。当事件发生时，`Initiation Dispatcher`会将被事件源激活的`Handle`作为**key**来寻找并分发恰当的`事件处理器回调方法`。
6) `Initiation Dispatcher`会回调`事件处理器`的`handle_event(type)`回调方法来执行特定于应用的业务逻辑（开发者所编写的业务逻辑），从而响应这个事件。所发生的事件类型可以作为该方法参数并被该方法内部使用来执行额外的特定于服务的分离与分发。

## 4. Netty中`Event Handler（事件处理器）`的回调方法
```java
package io.netty.channel;

/**
 * {@link ChannelHandler} which adds callbacks for state changes. This allows the user
 * to hook in to state changes easily.
 */
public interface ChannelInboundHandler extends ChannelHandler {

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was registered with its {@link EventLoop}
     * 通道注册
     */
    void channelRegistered(ChannelHandlerContext ctx) throws Exception;

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was unregistered from its {@link EventLoop}
     * 通道取消注册
     */
    void channelUnregistered(ChannelHandlerContext ctx) throws Exception;

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} is now active
     * 通道激活
     */
    void channelActive(ChannelHandlerContext ctx) throws Exception;

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was registered is now inactive and reached its
     * end of lifetime.
     * 通道取消激活
     */
    void channelInactive(ChannelHandlerContext ctx) throws Exception;

    /**
     * Invoked when the current {@link Channel} has read a message from the peer.
     * 通道读数据
     */
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;

    /**
     * Invoked when the last message read by the current read operation has been consumed by
     * {@link #channelRead(ChannelHandlerContext, Object)}.  If {@link ChannelOption#AUTO_READ} is off,
     * no further attempt to read an inbound data from the current {@link Channel} will be made until
     * {@link ChannelHandlerContext#read()} is called.
     * 通道读数据完成
     */
    void channelReadComplete(ChannelHandlerContext ctx) throws Exception;

    /**
     * Gets called if an user event was triggered.
     * 用户事件触发
     */
    void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception;

    /**
     * Gets called once the writable state of a {@link Channel} changed. You can check the state with
     * {@link Channel#isWritable()}.
     * 通道可写能力改变
     */
    void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception;

    /**
     * Gets called if a {@link Throwable} was thrown.
     * 异常捕获
     */
    @Override
    @SuppressWarnings("deprecation")
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}
```
