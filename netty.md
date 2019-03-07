# Netty

## 1. EventLoopGroup
* `EventLoopGroup`是一个接口，继承自`EventExecutorGroup`。
* 一种特殊的`EventExecutorGroup`，在事件循环中（如：等待连接建立，等待输入的到来等等）进行select操作时注册一个个的`Channel`。可以将一个`Channel`理解为一个客户端的连接。
* `@Override EventLoop next();`
  * 返回下一个要使用的`EventLoop`
* `ChannelFuture register(Channel channel);`
  * 将一个`Channel`注册到当前`EventLoop`当中。注册完成后，返回的`ChannelFuture`将收到通知。
* `ChannelFuture register(ChannelPromise promise);`
  * 使用`ChannelFuture`（参数`ChannelPromise`继承`ChannelFuture`，并且`ChannelPromise`类中包含一个`Channel`的引用）在当前`EventLoop`中注册一个`Channel`。注册完成后，传递进来的`ChannelFuture`和返回的`ChannelFuture`都将收到通知。

## 2. EventExecutorGroup
