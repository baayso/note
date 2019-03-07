# Netty

## 1. EventLoopGroup
```java
package io.netty.channel;

import io.netty.util.concurrent.EventExecutorGroup;

/**
 * 一种特殊的 {@link EventExecutorGroup}，
 * 在事件循环中（如：等待连接建立，等待输入的到来等等）进行select操作时注册一个个的 {@link Channel}。
 * 可以将一个 {@link Channel} 理解为一个客户端的连接。
 */
public interface EventLoopGroup extends EventExecutorGroup {

    /** 
     * 返回下一个要使用的 {@link EventLoop}。
     */
    @Override
    EventLoop next();

    /**
     * 将一个 {@link Channel} 注册到当前 {@link EventLoop} 当中。注册完成后，返回的 {@link ChannelFuture} 将收到通知。
     */
    ChannelFuture register(Channel channel);

    /**
     * 使用 {@link ChannelFuture} 在当前 {@link EventLoop} 中注册一个 {@link Channel}。
     * （参数 {@link ChannelPromise} 继承 {@link ChannelFuture}，并且 {@link ChannelPromise} 类中包含一个对 {@link Channel} 的引用）
     * 注册完成后，传递进来的 {@link ChannelFuture} 和返回的 {@link ChannelFuture} 都将收到通知。
     */
    ChannelFuture register(ChannelPromise promise);

    /**
     * Register a {@link Channel} with this {@link EventLoop}. The passed {@link ChannelFuture}
     * will get notified once the registration was complete and also will get returned.
     *
     * @deprecated Use {@link #register(ChannelPromise)} instead.
     */
    @Deprecated
    ChannelFuture register(Channel channel, ChannelPromise promise);
}
```

## 2. EventExecutorGroup
```java
package io.netty.util.concurrent;

import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;

/**
 * {@link EventExecutorGroup} 负责通过 {@link #next()} 方法提供 {@link EventExecutor}。
 * 除此之外，它还负责处理它们的生命周期并且可以以全局方式关闭它们。
 */
public interface EventExecutorGroup extends ScheduledExecutorService, Iterable<EventExecutor> {
    
    ...

    /**
     * 返回由这个 {@link EventExecutorGroup} 所管理的一个 {@link EventExecutor}。
     */
    EventExecutor next();

    ...
}
```

## 3. NioEventLoopGroup
``` java
package io.netty.channel.nio;

import io.netty.channel.MultithreadEventLoopGroup;

/**
 * {@link MultithreadEventLoopGroup} 的一种实现，用于基于NIO {@link Selector} 的 {@link Channel}。
 */
public class NioEventLoopGroup extends MultithreadEventLoopGroup {

    /**
     * Create a new instance using the default number of threads, the default {@link ThreadFactory} and
     * the {@link SelectorProvider} which is returned by {@link SelectorProvider#provider()}.
     */
    public NioEventLoopGroup() {
        this(0);
    }

    ...
}
```
