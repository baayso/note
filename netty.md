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
     * 将一个 {@link Channel} 注册到当前的 {@link EventLoop} 当中。注册完成后，返回的 {@link ChannelFuture} 将收到通知。
     */
    ChannelFuture register(Channel channel);

    /**
     * 使用 {@link ChannelFuture} 在当前的 {@link EventLoop} 中注册一个 {@link Channel}。
     * （注：参数 {@link ChannelPromise} 继承 {@link ChannelFuture}，并且 {@link ChannelPromise} 类中包含一个对 {@link Channel} 的引用）
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
    * 使用默认的线程数以及默认的 {@link ThreadFactory} 和
    * {@link SelectorProvider#provider()} 方法所返回的 {@link SelectorProvider} 来创建一个新的实例。
     */
    public NioEventLoopGroup() {
        this(0);
    }

    public NioEventLoopGroup(int nThreads) {
        this(nThreads, (Executor) null);
    }

    public NioEventLoopGroup(int nThreads, Executor executor) {
        this(nThreads, executor, SelectorProvider.provider());
    }

    ...
}
```

## 4. MultithreadEventLoopGroup
```java
package io.netty.channel;

import io.netty.util.concurrent.MultithreadEventExecutorGroup;

/**
 * Abstract base class for {@link EventLoopGroup} implementations that handles their tasks with multiple threads at
 * the same time.
 * 实现了 {@link EventLoopGroup} 的抽象基类，该实现使用多个线程同时处理任务。
 */
public abstract class MultithreadEventLoopGroup extends MultithreadEventExecutorGroup implements EventLoopGroup {

    private static final int DEFAULT_EVENT_LOOP_THREADS;

    // 静态成员变量和静态代码块在class对象被加载到JVM中就会被初始化和执行
    static {
        // NettyRuntime.availableProcessors() 是对 Runtime.getRuntime().availableProcessors() 的一个封装
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));

        if (logger.isDebugEnabled()) {
            logger.debug("-Dio.netty.eventLoopThreads: {}", DEFAULT_EVENT_LOOP_THREADS);
        }
    }

    ...

    /**
     * @see MultithreadEventExecutorGroup#MultithreadEventExecutorGroup(int, Executor, Object...)
     */
    protected MultithreadEventLoopGroup(int nThreads, Executor executor, Object... args) {
        super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, args);
    }

   ...
}
```

## 5. MultithreadEventExecutorGroup
```java
package io.netty.util.concurrent;

/**
 * Abstract base class for {@link EventExecutorGroup} implementations that handles their tasks with multiple threads at
 * the same time.
 * 实现了 {@link EventExecutorGroup} 的抽象基类，该实现同时使用多个线程处理其任务。
 */
public abstract class MultithreadEventExecutorGroup extends io.netty.util.concurrent.AbstractEventExecutorGroup {

    ...

    /**
     * Create a new instance.
     *
     * @param nThreads          the number of threads that will be used by this instance.
     * @param executor          the Executor to use, or {@code null} if the default should be used.
     * @param args              arguments which will passed to each {@link #newChild(Executor, Object...)} call
     */
    protected MultithreadEventExecutorGroup(int nThreads, Executor executor, Object... args) {
        this(nThreads, executor, DefaultEventExecutorChooserFactory.INSTANCE, args);
    }

    /**
     * Create a new instance.
     *
     * @param nThreads          the number of threads that will be used by this instance.
     * @param executor          the Executor to use, or {@code null} if the default should be used.
     * @param chooserFactory    the {@link EventExecutorChooserFactory} to use.
     * @param args              arguments which will passed to each {@link #newChild(Executor, Object...)} call
     */
    protected MultithreadEventExecutorGroup(int nThreads, Executor executor,
                                            EventExecutorChooserFactory chooserFactory, Object... args) {
        if (nThreads <= 0) {
            throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", nThreads));
        }

        if (executor == null) {
            // newDefaultThreadFactory()
            executor = new ThreadPerTaskExecutor(newDefaultThreadFactory());
        }

        children = new EventExecutor[nThreads];

        for (int i = 0; i < nThreads; i ++) {
            boolean success = false;
            try {
                children[i] = newChild(executor, args);
                success = true;
            } catch (Exception e) {
                // TODO: Think about if this is a good exception type
                throw new IllegalStateException("failed to create a child event loop", e);
            } finally {
                if (!success) {
                    for (int j = 0; j < i; j ++) {
                        children[j].shutdownGracefully();
                    }

                    for (int j = 0; j < i; j ++) {
                        EventExecutor e = children[j];
                        try {
                            while (!e.isTerminated()) {
                                e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                            }
                        } catch (InterruptedException interrupted) {
                            // Let the caller handle the interruption.
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }

        chooser = chooserFactory.newChooser(children);

        final FutureListener<Object> terminationListener = new FutureListener<Object>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                if (terminatedChildren.incrementAndGet() == children.length) {
                    terminationFuture.setSuccess(null);
                }
            }
        };

        for (EventExecutor e: children) {
            e.terminationFuture().addListener(terminationListener);
        }

        Set<EventExecutor> childrenSet = new LinkedHashSet<EventExecutor>(children.length);
        Collections.addAll(childrenSet, children);
        readonlyChildren = Collections.unmodifiableSet(childrenSet);
    }

    ...
}
```

## 6. EventExecutor
```java
package io.netty.util.concurrent;

/**
 * {@link EventExecutor} 是一种特殊的 {@link EventExecutorGroup}，
 * 它提供了一些方便的方法来查看线程是否在事件循环中被执行了。除此之外，它还继承（扩展）了 {@link EventExecutorGroup}，
 * 允许以一种通用的方式来访问它里面的方法。
 */
public interface EventExecutor extends EventExecutorGroup {

    /**
     * 返回对自身的引用。
     */
    @Override
    EventExecutor next();

    /**
     * Return the {@link EventExecutorGroup} which is the parent of this {@link EventExecutor},
     * 返回当前 {@link EventExecutor} 的父级引用 {@link EventExecutorGroup}。
     */
    EventExecutorGroup parent();

    /**
     * Calls {@link #inEventLoop(Thread)} with {@link Thread#currentThread()} as argument
     */
    boolean inEventLoop();

    /**
     * Return {@code true} if the given {@link Thread} is executed in the event loop,
     * {@code false} otherwise.
     */
    boolean inEventLoop(Thread thread);

    ...
}
```


