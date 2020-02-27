# NOTE
> 学习笔记

## [Java](https://github.com/baayso/note/tree/master/java)
* [Java8新特性](https://github.com/baayso/note/blob/master/java/java8)
  * [Lambda](https://github.com/baayso/note/tree/master/java/java8#1-lambda)
    * [函数式接口](https://github.com/baayso/note/tree/master/java/java8#11-%E5%87%BD%E6%95%B0%E5%BC%8F%E6%8E%A5%E5%8F%A3)
    * [```java.util.function.Function<T, R>``` 接口](https://github.com/baayso/note/tree/master/java/java8#12-javautilfunctionfunctiont-r-%E6%8E%A5%E5%8F%A3)
    * [```java.util.function.BiFunction<T, U, R>``` 接口](https://github.com/baayso/note/tree/master/java/java8#13-javautilfunctionbifunctiont-u-r-%E6%8E%A5%E5%8F%A3)
    * [```java.util.function.Predicate<T>``` 接口](https://github.com/baayso/note/tree/master/java/java8#14-javautilfunctionpredicatet-%E6%8E%A5%E5%8F%A3)
    * [```java.util.function.Supplier<T>``` 接口](https://github.com/baayso/note/tree/master/java/java8#15-javautilfunctionsuppliert-%E6%8E%A5%E5%8F%A3)
    * [```java.util.function.Consumer<T>``` 接口](https://github.com/baayso/note/tree/master/java/java8#16-javautilfunctionconsumert-%E6%8E%A5%E5%8F%A3)
  * [Optional](https://github.com/baayso/note/tree/master/java/java8#2-javautiloptionalt-final%E7%B1%BB)
  * [方法引用](https://github.com/baayso/note/tree/master/java/java8#3-%E6%96%B9%E6%B3%95%E5%BC%95%E7%94%A8)
  * [Stream](https://github.com/baayso/note/tree/master/java/java8#4-stream%E6%B5%81)
* [NIO](https://github.com/baayso/note/blob/master/java/nio)
  * [java.io 与 java.nio](https://github.com/baayso/note/blob/master/java/nio/nio.md#1-javaio-%E4%B8%8E-javanio)
  * [NIO Buffer](https://github.com/baayso/note/blob/master/java/nio/nio.md#2-nio-buffer)
  * [NIO Buffer 的 Scattering 和 Gathering](https://github.com/baayso/note/blob/master/java/nio/nio.md#4-nio-buffer-%E7%9A%84-scattering-%E5%92%8C-gathering)
  * [NIO堆外内存与零拷贝](https://github.com/baayso/note/blob/master/java/nio/nio.md#3-nio%E5%A0%86%E5%A4%96%E5%86%85%E5%AD%98%E4%B8%8E%E9%9B%B6%E6%8B%B7%E8%B4%9D)
  * [零拷贝（zero copy）](https://github.com/baayso/note/blob/master/java/nio/zero-copy.md)
  * [Channel](https://github.com/baayso/note/blob/master/java/nio/nio.md#5-channel)
  * [Selector](https://github.com/baayso/note/blob/master/java/nio/nio.md#6-selector)
* [Netty](https://github.com/baayso/note/tree/master/java/netty)
  * [Reactor模型](https://github.com/baayso/note/blob/master/java/reactor/reactor.md)
  * [Netty源码解析](https://github.com/baayso/note/blob/master/java/netty/netty.md)
  * [为什么Netty只基于NIO而抛弃AIO](https://github.com/baayso/note/blob/master/java/netty/netty-nio-aio.md)
  * [ByteBuf](https://github.com/baayso/note/blob/master/java/netty/README.md#6-netty-bytebuf%E6%89%80%E6%8F%90%E4%BE%9B%E7%9A%843%E7%A7%8D%E7%BC%93%E5%86%B2%E5%8C%BA%E7%B1%BB%E5%9E%8B)
  * [Handler](https://github.com/baayso/note/blob/master/java/netty/README.md#8-netty-%E5%A4%84%E7%90%86%E5%99%A8handler)
* [String](https://github.com/baayso/note/blob/master/java/String.md)
* [equals And hashCode](https://github.com/baayso/note/blob/master/java/equals_and_hashCode.md)
* [Collection](https://github.com/baayso/note/blob/master/java/collection/collection.md)
  * [线程安全 和 同步修改异常](https://github.com/baayso/note/blob/master/java/collection/collection.md#9-%E7%BA%BF%E7%A8%8B%E5%AE%89%E5%85%A8-%E5%92%8C-%E5%90%8C%E6%AD%A5%E4%BF%AE%E6%94%B9%E5%BC%82%E5%B8%B8)
  * [解决同步修改异常](https://github.com/baayso/note/blob/master/java/collection/collection.md#10-%E8%A7%A3%E5%86%B3%E5%90%8C%E6%AD%A5%E4%BF%AE%E6%94%B9%E5%BC%82%E5%B8%B8)
  * [CopyOnWriteArrayList](https://github.com/baayso/note/blob/master/java/collection/collection.md#11-copyonwritearraylist)
  * [CopyOnWriteArraySet](https://github.com/baayso/note/blob/master/java/collection/collection.md#12-copyonwritearrayset)
  * [ConcurrentHashMap](https://github.com/baayso/note/blob/master/java/collection/collection.md#13-concurrenthashmap)
  * [使用优雅的方式进行集合运算](https://github.com/baayso/note/blob/master/java/collection/collection.md#14-%E4%BD%BF%E7%94%A8%E4%BC%98%E9%9B%85%E7%9A%84%E6%96%B9%E5%BC%8F%E8%BF%9B%E8%A1%8C%E9%9B%86%E5%90%88%E8%BF%90%E7%AE%97)
  * [列表遍历方式](https://github.com/baayso/note/blob/master/java/collection/collection.md#15-%E5%88%97%E8%A1%A8%E9%81%8D%E5%8E%86%E6%96%B9%E5%BC%8F)
* [volatile 与 JMM](https://github.com/baayso/note/blob/master/java/volatile.md)
* [CAS](https://github.com/baayso/note/blob/master/java/CAS.md)
* [锁](https://github.com/baayso/note/blob/master/java/lock.md)
  * [synchronized和Lock的区别](https://github.com/baayso/note/blob/master/java/synchronized_and_lock.md)
* [多线程](https://github.com/baayso/note/blob/master/java/thread/)
  * [Thread#start()](https://github.com/baayso/note/blob/master/java/thread/Thread#start().md)
  * [CountDownLatch](https://github.com/baayso/note/blob/master/java/thread/thread.md#countdownlatch)
  * [CyclicBarrier](https://github.com/baayso/note/blob/master/java/thread/thread.md#cyclicbarrier)
  * [Semaphore](https://github.com/baayso/note/blob/master/java/thread/thread.md#semaphore)
* [阻塞队列](https://github.com/baayso/note/tree/master/java/thread/blocking_queue)
  * [什么是阻塞队列](https://github.com/baayso/note/tree/master/java/thread/blocking_queue#%E4%BB%80%E4%B9%88%E6%98%AF%E9%98%BB%E5%A1%9E%E9%98%9F%E5%88%97)
  * [阻塞队列的优点](https://github.com/baayso/note/tree/master/java/thread/blocking_queue#%E9%98%BB%E5%A1%9E%E9%98%9F%E5%88%97%E7%9A%84%E4%BC%98%E7%82%B9)
  * [`BlockingQueue`的核心方法](https://github.com/baayso/note/tree/master/java/thread/blocking_queue#blockingqueue%E7%9A%84%E6%A0%B8%E5%BF%83%E6%96%B9%E6%B3%95)
  * [阻塞队列的种类及架构](https://github.com/baayso/note/tree/master/java/thread/blocking_queue#%E9%98%BB%E5%A1%9E%E9%98%9F%E5%88%97%E7%9A%84%E7%A7%8D%E7%B1%BB%E5%8F%8A%E6%9E%B6%E6%9E%84)
  * [阻塞队列的使用场景](https://github.com/baayso/note/tree/master/java/thread/blocking_queue#%E9%98%BB%E5%A1%9E%E9%98%9F%E5%88%97%E7%9A%84%E4%BD%BF%E7%94%A8%E5%9C%BA%E6%99%AF)
* 杂项
  * [`Runtime.getRuntime().availableProcessors();`](https://github.com/baayso/note/blob/master/java/java_other.md#1-runtimegetruntimeavailableprocessors)

## Git
* [Git操作命令](https://github.com/baayso/note/blob/master/git.md)
  * [Git分支原理及操作命令](https://github.com/baayso/note/blob/master/git.md#8-%E5%88%86%E6%94%AF)
  * [Git版本回退](https://github.com/baayso/note/blob/master/git.md#9-%E7%89%88%E6%9C%AC%E5%9B%9E%E9%80%80)
  * [Git远程操作](https://github.com/baayso/note/blob/master/git.md#13-%E8%BF%9C%E7%A8%8B%E6%93%8D%E4%BD%9C)
  * [...](https://github.com/baayso/note/blob/master/git.md)

## 推荐阅读：
* https://github.com/Snailclimb/JavaGuide
* https://github.com/crossoverJie/JCSprout
* https://github.com/doocs/advanced-java
* https://github.com/dylanaraps/pure-bash-bible
* https://github.com/justjavac/free-programming-books-zh_CN
* https://github.com/codefollower/OpenJDK-Research
* https://github.com/LingCoder/OnJava8
* https://github.com/sjsdfg/effective-java-3rd-chinese
* https://github.com/ddean2009/Spring-Framework-Documentation
* [精美图文带你掌握 JVM 内存布局](https://juejin.im/post/5e0708baf265da33c34e495b)
* [点赞模块设计](https://juejin.im/post/5bdc257e6fb9a049ba410098)
* [分布式事务解决方案汇总](https://github.com/baayso/distributed-transaction-solution)
