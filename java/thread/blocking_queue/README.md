# `interface java.util.concurrent.BlockingQueue<E>`（阻塞队列）

### 什么是阻塞队列
* 阻塞队列，顾名思义，首先它是一个队列，而一个阻塞队列在数据结构中所起的作用大致如下图所示：  
  ![阻塞队列](https://github.com/baayso/note/blob/master/java/thread/blocking_queue/blocking_queue.png)
* 当阻塞队列为空时，从队列中**获取**元素的操作将会被阻塞。
* 当阻塞队列已满时，往队列里**添加**元素的操作将会被阻塞。
* 试图从空的阻塞队列中获取元素的线程将会被阻塞，直到其他的线程往空的队列插入新的元素。
* 同样的，试图往已满的阻塞队列中添加新元素的线程同样也会被阻塞，直到其他线程从队列中移除一个或多个元素或者完全清空队列后使队列重新变得空闲。

### 阻塞队列的优点
* 在多线程领域，所谓阻塞，在某些情况下会**挂起**线程（即阻塞），一亘条件满足，被挂起的线程又会自动被**唤醒**。
* 使用阻塞队列可以让我们无需关心什么时候需要阻塞线程，什么时候需要唤醒线程，因为这一切`BlockingQueue`都给你一手包办了。
* 在JDK 1.5的`java.util.concurrent`包发布以前，多线程环境下，程序员都必须自己控制阻塞和唤醒线程这些细节，还需要兼顾效率和线程安全，这带来了不小的复杂度。

### `BlockingQueue`的核心方法

### 阻塞队列的种类及架构
![阻塞队列类图](https://github.com/baayso/note/blob/master/java/thread/blocking_queue/BlockingQueue.png)
* **`ArrayBlockingQueue<E>`：由数组结构组成的有界阻塞队列。**
* **`LinkedBlockingQueue<E>`：由链表结构组成的有界（但默认值是：`Integer.MAX_VALUE`）阻塞队列。**
* `PriorityBlockingQueue<E>`：支持优先级排序的无界阻塞队列。
* `DelayQueue<E extends Delayed>`：使用优先级队列实现的延迟无界阻塞队列。
* **`SynchronousQueue<E>`：不存储元素的阻塞队列，即单个元素的阻塞队列。**
  * `SynchronousQueue<E>`没有容量，与其他`BlockingQueue`不同，`SynchronousQueue<E>`是一个不存储元素的`BlockingQueue`。每一个`put`操作必须要等待一个`take`操作，否则不能继续添加元素，反之依然。
* `LinkedTransferQueue<E>`：由链表结构组成的无界阻塞队列。
* `LinkedBlockingDeque<E>`：由链表结构组成的双向阻塞队列。

### 阻塞队列的用途
