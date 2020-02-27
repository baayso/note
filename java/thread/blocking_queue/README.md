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
方法类型 | 抛出异常 | 特殊值 | 阻塞 | 超时
-|-|-|-|-
插入 | `add(e)` | `offer(e)` | `put(e)` | `offer(e, time, unit)` |
移除 | `remove()` | `poll()` | `take()` | `poll(time, unit)` |
检查 | `element()` | `peek()` | 不可用 | 不可用 |

类型 | 说明
-|-
抛出异常 | 当阻塞队列已满时，再往队列里`add`元素会抛出`IllegalStateException: Queue full`异常。<br> 当阻塞队列为空时，再从队列里`remove`元素会抛出`NoSuchElementException`异常。 |
特殊值 | 插入方法，成功时返回`true`，失败时返回`false`。<br> 移除方法，成功时返回被移除队列的元素，若队列已经为空则返回`null`。 |
一直阻塞 | 当阻塞队列已满时，生产者线程继续往队列里`put`元素，队列会一直阻塞生产线程直到可以`put`数据或者响应中断退出。<br> 当阻塞队列为空时，消费者线程试图从队列里`take`元素，队列会一直阻塞消费者线程直到队列中有元素。 |
超时退出 | 当阻塞队列已满时，队列会阻塞生产者线程一定时间，超过限时后生产者线程则会退出。 |

### 阻塞队列的种类及架构
![阻塞队列类图](https://github.com/baayso/note/blob/master/java/thread/blocking_queue/BlockingQueue.png)
* **`ArrayBlockingQueue<E>`：由数组结构组成的有界阻塞队列。**
* **`LinkedBlockingQueue<E>`：由链表结构组成的有界（但默认值是：`Integer.MAX_VALUE`）阻塞队列。**
* `PriorityBlockingQueue<E>`：支持优先级排序的无界阻塞队列。
* `DelayQueue<E extends Delayed>`：使用优先级队列实现的延迟无界阻塞队列。
* **`SynchronousQueue<E>`：不存储元素的阻塞队列，即单个元素的阻塞队列。**
  * `SynchronousQueue<E>`没有容量，与其他阻塞队列不同，`SynchronousQueue<E>`是一个不存储元素的阻塞队列。每一个`put`操作必须要等待一个`take`操作，否则不能继续添加元素，反之依然。
  * 示例代码：
    ```java
    public class SynchronousQueueDemo {

        public static void main(String[] args) {

            BlockingQueue<Integer> queue = new SynchronousQueue<>();

            new Thread(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + "\tput: 1");
                    queue.put(1);

                    System.out.println(Thread.currentThread().getName() + "\tput: 2");
                    queue.put(2);

                    System.out.println(Thread.currentThread().getName() + "\tput: 3");
                    queue.put(3);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "T1").start();

            new Thread(() -> {
                try {
                    try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
                    System.out.println(Thread.currentThread().getName() + "\ttake: " + queue.take());

                    try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
                    System.out.println(Thread.currentThread().getName() + "\ttake: " + queue.take());

                    try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
                    System.out.println(Thread.currentThread().getName() + "\ttake: " + queue.take());
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "T2").start();

        }

    }
    ```
    ```
    输出结果
    T1	put: 1
    T2	take: 1
    T1	put: 2
    T2	take: 2
    T1	put: 3
    T2	take: 3
    ```
* `LinkedTransferQueue<E>`：由链表结构组成的无界阻塞队列。
* `LinkedBlockingDeque<E>`：由链表结构组成的双向阻塞队列。

### 阻塞队列的使用场景
* 生产者消费者模式
  * 传统版实现生产者消费者模式
    ```java
    class Resource {
        private int       number    = 0;
        private Lock      lock      = new ReentrantLock();
        private Condition condition = this.lock.newCondition();

        public void increment() {
            this.lock.lock();
            try {
                while (this.number != 0) {
                    this.condition.await(); // 等待，暂不能进行生产
                }

                this.number++; // 生产
                System.out.println(Thread.currentThread().getName() + "\tnumber: " + this.number);

                this.condition.signalAll(); // 通知唤醒
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                this.lock.unlock();
            }
        }

        public void decrement() {
            this.lock.lock();
            try {
                while (this.number == 0) {
                    this.condition.await(); // 等待，暂不能进行消费
                }

                this.number--; // 消费
                System.out.println(Thread.currentThread().getName() + "\tnumber: " + this.number);

                this.condition.signalAll(); // 通知唤醒
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                this.lock.unlock();
            }
        }
    }

    public class ProducerAndConsumerTraditionDemo {

        public static void main(String[] args) {

            Resource resource = new Resource();

            new Thread(() -> {
                for (int i = 1; i <= 5; i++) {
                    resource.increment();
                }
            }, "T1").start();

            new Thread(() -> {
                for (int i = 1; i <= 5; i++) {
                    resource.decrement();
                }
            }, "T2").start();
        }

    }
    ```
    ```
    输出结果：
    T1	number: 1
    T2	number: 0
    T1	number: 1
    T2	number: 0
    T1	number: 1
    T2	number: 0
    T1	number: 1
    T2	number: 0
    T1	number: 1
    T2	number: 0
    ```
  * **阻塞队列版实现生产者消费者模式**
    ```java

    ```
    ```
    输出结果：

    ```
* 线程池

* 消息中间件

