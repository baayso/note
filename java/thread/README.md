# Thread（多线程）

### 线程池
> 线程池的工作主要是控制运行的线程的数量，**处理过程中将任务放入队列**，然后在线程创建后启动这些任务，如果线程数量超过了最大数量，超出数量的线程将会排队等候，等到其他线程执行完毕，再从队列中取出任务来执行。
* 主要特点：
  * 线程复用
  * 控制最大并发数
  * 管理线程
* 使用线程池的优势
  * 降低资源消耗。通过重复利用已创建的线程降低线程创建和销毁造成的消耗。
  * 提高响应速度。当任务到达时，任务可以不需要等到线程创建就可以立即执行。
  * 提高线程的可管理性。线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行统一的分配、调优和监控。
* 如何使用线程池
  * Java中的线程池是通过`Executor`框架实现的，该框架中用到了`Executor`、`Executors`、`ExecutorService`、`ThreadPoolExecutor`这几个类。  
  ![Executor框架类图](https://github.com/baayso/note/blob/master/java/thread/Executor%E6%A1%86%E6%9E%B6%E7%B1%BB%E5%9B%BE.png)
  * [`Executors.newFixedThreadPool(int nThreads)`](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/concurrent/Executors.java#L88)：适用于执行长期任务，性能较好。
    * 创建一个**定长线程池**，可控制线程最大并发数，超出的线程会在队列中等待。
    * `corePoolSize`和`maximumPoolSize`是相等的，阻塞队列使用的是`new LinkedBlockingQueue<Runnable>()`。
      ```java
      /**
       * Creates a thread pool that reuses a fixed number of threads
       * operating off a shared unbounded queue.  At any point, at most
       * {@code nThreads} threads will be active processing tasks.
       * If additional tasks are submitted when all threads are active,
       * they will wait in the queue until a thread is available.
       * If any thread terminates due to a failure during execution
       * prior to shutdown, a new one will take its place if needed to
       * execute subsequent tasks.  The threads in the pool will exist
       * until it is explicitly {@link ExecutorService#shutdown shutdown}.
       *
       * @param nThreads the number of threads in the pool
       * @return the newly created thread pool
       * @throws IllegalArgumentException if {@code nThreads <= 0}
       */
      public static ExecutorService newFixedThreadPool(int nThreads) {
          return new ThreadPoolExecutor(nThreads, nThreads,
                                        0L, TimeUnit.MILLISECONDS,
                                        new LinkedBlockingQueue<Runnable>());
      }
      ```
  * [`Executors.newSingleThreadExecutor()`](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/concurrent/Executors.java#L170)：适用于任务需要依次执行。
    * 创建一个**只有一个线程**的线程池，它只会用唯一的工作线程来执行任务，保证所有的任务按照指定的顺序执行。
    * `corePoolSize`和`maximumPoolSize`都为`1`，阻塞队列使用的是`new LinkedBlockingQueue<Runnable>()`。
      ```java
      /**
       * Creates an Executor that uses a single worker thread operating
       * off an unbounded queue. (Note however that if this single
       * thread terminates due to a failure during execution prior to
       * shutdown, a new one will take its place if needed to execute
       * subsequent tasks.)  Tasks are guaranteed to execute
       * sequentially, and no more than one task will be active at any
       * given time. Unlike the otherwise equivalent
       * {@code newFixedThreadPool(1)} the returned executor is
       * guaranteed not to be reconfigurable to use additional threads.
       *
       * @return the newly created single-threaded Executor
       */
      public static ExecutorService newSingleThreadExecutor() {
          return new FinalizableDelegatedExecutorService
              (new ThreadPoolExecutor(1, 1,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>()));
      }
      ```
  * [`Executors.newCachedThreadPool()`](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/concurrent/Executors.java#L215)：适用于执行很多短期异步的小程序或者负载较轻的服务器。
    * 创建一个**可缓存线程池**，如果线程池长度超过处理需要，可灵活回收空间线程，若无可回收，则新建线程。
    * 任务来了就创建线程运行，当线程空闲超过`60秒`就会被销毁。
    * `corePoolSize`初始为`0`，`maximumPoolSize`为`Integer.MAX_VALUE`，阻塞队列使用的是`new SynchronousQueue<Runnable>()`。
      ```java
      /**
       * Creates a thread pool that creates new threads as needed, but
       * will reuse previously constructed threads when they are
       * available.  These pools will typically improve the performance
       * of programs that execute many short-lived asynchronous tasks.
       * Calls to {@code execute} will reuse previously constructed
       * threads if available. If no existing thread is available, a new
       * thread will be created and added to the pool. Threads that have
       * not been used for sixty seconds are terminated and removed from
       * the cache. Thus, a pool that remains idle for long enough will
       * not consume any resources. Note that pools with similar
       * properties but different details (for example, timeout parameters)
       * may be created using {@link ThreadPoolExecutor} constructors.
       *
       * @return the newly created thread pool
       */
      public static ExecutorService newCachedThreadPool() {
          return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                        60L, TimeUnit.SECONDS,
                                        new SynchronousQueue<Runnable>());
      }
      ```
  * [`Executors.newScheduledThreadPool(int corePoolSize)`](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/concurrent/Executors.java#L284)
    ```java
    /**
     * Creates a thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     * @param corePoolSize the number of threads to keep in the pool,
     * even if they are idle
     * @return a newly created scheduled thread pool
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }

    /**
     * Creates a new {@code ScheduledThreadPoolExecutor} with the
     * given core pool size.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public ScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
              new DelayedWorkQueue());
    }
    ```
  * [`Executors.newWorkStealingPool()`](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/concurrent/Executors.java#L124)：Java8新增，使用目前机器上可用的处理器作为它的并行级别
    ```java
    /**
     * Creates a work-stealing thread pool using all
     * {@link Runtime#availableProcessors available processors}
     * as its target parallelism level.
     * @return the newly created thread pool
     * @see #newWorkStealingPool(int)
     * @since 1.8
     */
    public static ExecutorService newWorkStealingPool() {
        return new ForkJoinPool
            (Runtime.getRuntime().availableProcessors(),
             ForkJoinPool.defaultForkJoinWorkerThreadFactory,
             null, true);
    }
    ```
  * 示例代码
    ```java
    public class ThreadPoolDemo {

        private static ExecutorService threadPool = Executors.newFixedThreadPool(6); // 一池6个线程
        // private static ExecutorService threadPool = Executors.newSingleThreadExecutor(); // 一池单个线程
        // private static ExecutorService threadPool = Executors.newCachedThreadPool(); // 一池N个线程

        public static void main(String[] args) {
            try {
                for (int i = 1; i <= 10; i++) {
                    threadPool.execute(() -> {
                        System.out.println(Thread.currentThread().getName() + "\t 办理业务");
                    });
                }
            }
            finally {
                threadPool.shutdown();
            }
        }
    }
    ```
    ```
    输出结果
    pool-1-thread-3	 办理业务
    pool-1-thread-2	 办理业务
    pool-1-thread-4	 办理业务
    pool-1-thread-3	 办理业务
    pool-1-thread-6	 办理业务
    pool-1-thread-5	 办理业务
    pool-1-thread-4	 办理业务
    pool-1-thread-2	 办理业务
    pool-1-thread-3	 办理业务
    pool-1-thread-1	 办理业务
    ```

### `ThreadPoolExecutor`构造函数参数说明
* **`int corePoolSize`**：线程池中的常驻核心线程数
  * 在创建了线程池后，当有请求任务来了之后，就会安排池中的线程去执行请求任务，近似理解为今日当值线程。
  * 当线程池中的线程数达到`corePoolSize`后，就会把到达的任务放到缓存队列当中。
* **`int maximumPoolSize`**：线程池能够容纳同时执行的最大线程数，此值必须大于等于`1`。
* **`long keepAliveTime`**：多余的空闲线程的存活时间。当前线程池数量超过`corePoolSize`时，当空闲时间达到`keepAliveTime`值时，多余空闲线程会被销毁直到只剩下`corePoolSize`个线程为止。
  * 默认情况下，只有当线程池中的线程数**大于**`corePoolSize`时`keepAliveTime`才会起作用，直到线程池中的线程数**不大于**`corePoolSize`。
* **`TimeUnit unit`**：`keepAliveTime`参数的单位。
* **`BlockingQueue<Runnable> workQueue`**：任务队列，被提交但尚未被执行的任务。
* **`ThreadFactory threadFactory`**：生成线程池中工作线程的线程工厂，用于创建线程**一般使用默认线程工厂即可**。
* **`RejectedExecutionHandler handler`**：拒绝策略，表示当队列已满且工作线程大于等于线程池的最大线程数（`maximumPoolSize`）时如何来拒绝请求执行的`Runnable`的策略。
```java
    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @param threadFactory the factory to use when the executor
     *        creates a new thread
     * @param handler the handler to use when execution is blocked
     *        because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code threadFactory} or {@code handler} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        ...
    }
```

### 线程程底层工作原理
* **当向线程池提交一个任务之后，线程池的主要处理流程：**  
  ![线程池的主要处理流程](https://github.com/baayso/note/blob/master/java/thread/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E7%9A%84%E4%B8%BB%E8%A6%81%E5%A4%84%E7%90%86%E6%B5%81%E7%A8%8B.png)
  * 1）线程池判断核心线程池里的线程是否都在执行任务。如果不是，则创建一个新的工作线程来执行任务。如果核心线程池里的线程都在执行任务，则进入下个流程。
  * 2）线程池判断工作队列是否已经满。如果工作队列没有满，则将新提交的任务存储在这个工作队列里。如果工作队列满了，则进入下个流程。
  * 3）线程池判断线程池的线程是否都处于工作状态。如果没有，则创建一个新的工作线程来执行任务。如果已经满了，则交给饱和策略来处理这个任务。
* **`ThreadPoolExecutor`执行`execute()`方法的示意图：**  
  ![ThreadPoolExecutor执行示意图](https://github.com/baayso/note/blob/master/java/thread/ThreadPoolExecutor%E6%89%A7%E8%A1%8C%E7%A4%BA%E6%84%8F%E5%9B%BE.png)
  * `ThreadPoolExecutor`执行`execute()`方法分下面4种情况：
    * 1）如果当前运行的线程少于`corePoolSize`，则创建新线程来执行任务（注意，执行这一步骤需要获取全局锁）。
    * 2）如果运行的线程等于或多于`corePoolSize`，则将任务加入`BlockingQueue`。
    * 3）如果无法将任务加入`BlockingQueue`（队列已满），则创建新的线程来处理任务（注意，执行这一步骤需要获取全局锁）。
    * 4）如果创建新线程将使当前运行的线程超出`maximumPoolSize`，任务将被拒绝，并调用`RejectedExecutionHandler.rejectedExecution()`方法。
  * `ThreadPoolExecutor`采取上述步骤的总体设计思路，是为了在执行`execute()`方法时，尽可能地避免获取全局锁（那将会是一个严重的可伸缩瓶颈）。在`ThreadPoolExecutor`完成预热之后（当前运行的线程数大于等于`corePoolSize`），几乎所有的`execute()`方法调用都是执行步骤2，而步骤2不需要获取全局锁。
* **线程程底层工作原理：**
  * 1）在创建了线程地后，等待提交过来的任务请求。
  * 2）当调用`execute()`方法添加一个请求任务时，线程池会做如下判断：
    * 2.1 如果正在运行的线程数量小于`corePoolSize`，那么马上创建线程运行这个任务；
    * 2.2 如果正在运行的线程数量大于或等于`corePoolSize`，那么将这个任务**放入队列**；
    * 2.3 如果这时队列满了且正在运行的线程数量还小于`maximumPoolSize`，那么还是要创建非核心线程立刻运行这个任务；
    * 2.4 如果队列满了且正在运行的线程数量大于或等于`maximumPoolSize`，那么线程池**会启动饱和拒绝策略来执行**。
  * 3）当一个线程完成任务时，它会从队列中取下一个任务来执行。
  * 4）当一个线程无事可做并超过一定的时间（`keepAliveTime`）时，线程池会判断：
    * 如果当前运行的线程数大`corePoolSize`，那么这个线程就会被停掉。
    * 所以线程池的所有任务完成后它**最终会收缩到`corePoolSize`的大小**。

### 线程池的拒绝策略

### 使用`Executors`创建线程池的问题
> 以下内容摘自【阿里巴巴Java开发手册->编程规约->并发处理】：
* **线程池不允许使用`Executors`创建，而是通过`ThreadPoolExecutor`的方式**，这样的处理方式让写的同学更加明确线程池的运行规则，规避资源耗尽的风险。
* 说明：`Executors`创建的线程池对象的弊端如下：
  * `Executors.newFixedThreadPool(int nThreads)`和`Executors.newSingleThreadExecutor()`：
    * 允许的**请求队列长度**为`Integer.MAX_VALUE`，可能会**堆积大量的请求**，从而导致`OOM`。
  * `Executors.newCachedThreadPool()`：
    * 允许的**创建线程数量**为`Integer.MAX_VALUE`，可能会**创建大量的线程**，从而导致`OOM`。

### 自定义线程池

### [`Callable<V>`](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/concurrent/Callable.java#L58) 与 [`FutureTask<V>`](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/concurrent/FutureTask.java#L132)

### 并发工具类
#### `CountDownLatch`
* `CountDownLatch`主要有两个方法，当一个或多个线程调用`await()`方法时，调用线程会被阻塞。其他线程调用`countDown()`方法会将计数器`减1`（调用`countDown()`方法的线程不会被阻塞）。当计数器的值变为`零`时，因调用`await()`方法被阻塞的线程会被唤醒，继续执行。
* 示例代码：
  ```java
  public class CountDownLatchDemo {

      public static void main(String[] args) throws InterruptedException {

          CountDownLatch countDownLatch = new CountDownLatch(7);

          for (int i = 1; i <= 7; i++) {

              new Thread(() -> {

                  System.out.println(Thread.currentThread().getName() + "\t工作完成，离开公司。");

                  countDownLatch.countDown();

              }, "T" + i).start();

          }

          countDownLatch.await();

          System.out.println(Thread.currentThread().getName() + "\t### 领导最后锁上公司大门走人。");
      }

  }
  ```
  ```
  输出结果：
  T1	工作完成，离开公司。
  T2	工作完成，离开公司。
  T3	工作完成，离开公司。
  T4	工作完成，离开公司。
  T5	工作完成，离开公司。
  T6	工作完成，离开公司。
  T7	工作完成，离开公司。
  main	### 领导最后锁上公司大门走人。
  ```

#### `CyclicBarrier`
* `CyclicBarrier`的字面意思是可循环（Cyclic）使用的屏障（Barrier）。它要做的事情是：让一组线程到达一个屏障（也可以叫做同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会解除，所有被屏障拦截的线程才会继续执行，使用`CyclicBarrier#await()`方法让线程进入屏障。
* 示例代码：
  ```java
  public class CyclicBarrierDemo {

      public static void main(String[] args) {

          // CyclicBarrier(int parties, Runnable barrierAction)
          CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> System.out.println("####### 收集到所有卡片"));

          for (int i = 1; i <= 7; i++) {

              final String strI = String.valueOf(i);

              new Thread(() -> {

                  System.out.println(Thread.currentThread().getName() + "\t收集到第 " + strI + " 张卡片");

                  try {
                      cyclicBarrier.await();
                  }
                  catch (InterruptedException e) {
                      e.printStackTrace();
                  }
                  catch (BrokenBarrierException e) {
                      e.printStackTrace();
                  }

              }, "T" + strI).start();
          }
      }

  }
  ```
  ```
  输出结果：
  T1	收集到第 1 张卡片
  T3	收集到第 3 张卡片
  T4	收集到第 4 张卡片
  T2	收集到第 2 张卡片
  T5	收集到第 5 张卡片
  T7	收集到第 7 张卡片
  T6	收集到第 6 张卡片
  ####### 收集到所有卡片
  ```

#### `Semaphore`
* `Semaphore`（信号量）主要用于两个目的：一个是用于多个共享资源的互斥使用，另一个是用于并发线程数的控制。
* 示例代码：
  ```java
  /** 抢车位 */
  public class SemaphoreDemo {

      public static void main(String[] args) {

          Semaphore semaphore = new Semaphore(3); //模拟3个停车位

          for (int i = 1; i <= 7; i++) { // 模拟7辆汽车需要停车

              new Thread(() -> {

                  try {
                      semaphore.acquire();

                      System.out.println(Thread.currentThread().getName() + "\t抢到车位");

                      // 模拟停车5秒
                      try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }

                      System.out.println(Thread.currentThread().getName() + "\t停车5秒后离开车位");
                  }
                  catch (InterruptedException e) {
                      e.printStackTrace();
                  }
                  finally {
                      semaphore.release();
                  }

              }, "T" + i).start();
          }
      }

  }
  ```
  ```
  输出结果：
  T1	抢到车位
  T6	抢到车位
  T3	抢到车位
  T1	停车5秒后离开车位
  T6	停车5秒后离开车位
  T7	抢到车位
  T5	抢到车位
  T3	停车5秒后离开车位
  T2	抢到车位
  T5	停车5秒后离开车位
  T2	停车5秒后离开车位
  T4	抢到车位
  T7	停车5秒后离开车位
  T4	停车5秒后离开车位
  ```
  
