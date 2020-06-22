# `synchronized`和`Lock`有什么区别？`Lock`有什么优点？

### `synchronized`
* 当使用`synchronized`关键字来`修饰代码块`时，字节码层面上是通过`monitorenter`与`monitorexit`指令来实现的锁的获取与释放动作。当线程进入到`monitorenter`指令后，线程将会持有`Monitor对象`，退出`monitorenter`指令后，线程将会释放`Monitor对象`。
* 对于`synchronized`关键字`修饰方法`来说，并没有出现`monitorenter`与`monitorexit`指令，而是出现了一个`ACC_SYNCHRONIZED`标志。
* JVM使用了`ACC_SYNCHRONIZED`访问标志来区分一个方法是否为同步方法；当方法被调用时，调用指令会检查该方法是否拥有`ACC_SYNCHRONIZED`标志，如果有，那么执行线程将会先持有方法所在对象的`Monitor对象`，然后再去执行方法体；在该方法执行期间，其他任何线程均无法再获取到这个`Monitor对象`，当线程执行完该方法后，它会释放掉这个`Monitor对象`。
* JVM中的同步是基于进入与退出监视器对象（管程对象）（Monitor）来实现的，每个对象实例都会有一个Monitor对象，Monitor对象会和Java对象一同创建并销毁。Monitor对象是由C++来实现的。
* 当多个线程同时访问一段同步代码时，这些线程会被放到一个`EntryList`集合中，处于阻塞状态的线程都会被放到该列表当中。接下来，当线程获取到对象的Monitor时，Monitor是依赖于底层操作系统的`mutex lock`来实现互斥的，线程获取mutex成功，则会持有该mutex，这时其他线程就无法再获取到该mutex。
* 如果线程调用了`wait()`方法，那么该线程就会释放掉所持有的mutex，并且该线程会进入到`WaitSet`集合（等待集合）中，等待下一次被其他线程调用`notify()/notifyAll()`唤醒。如果当前线程顺利执行完毕方法，那么它也会释放掉所持有的mutex。
* 总结：同步锁在这种实现方式当中，因为Monitor是依赖于底层的操作系统实现，这样就存在用户态与内核态之间的切换，所以会增加性能开销。
* 通过对象互斥锁的概念来保证共享数据操作的完整性。每个对象都对应于一个可称为『互斥锁』的标记，这个标记用于保证在任何时刻，只能有一个线程访问该对象。
* 那些处于`[EntryList](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/share/vm/runtime/objectMonitor.hpp#L150)`与`[WaitSet](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/share/vm/runtime/objectMonitor.hpp#L144)`中的线程均处于阻塞状态，阻塞操作是由操作系统来完成的，在linux下是通过`pthread_mutex_lock`函数实现的。线程被阻塞后便会进入到内核调度状态，这会导致系统在用户态与内核态之间来回切换，严重影响锁的性能。
* 解决上述问题的办法便是自旋（Spin）。其原理是：当发生对Monitor的争用时，若Owner能够在很短的时间内释放掉锁，则那些正在争用的线程就可以稍微等待一下（即所谓的自旋），在Owner线程释放锁之后，争用线程可能会立刻获取到锁，从而避免了系统阻塞。不过，当Owner运行的时间超过了临界值后，争用线程自旋一段时间后依然无法获取到锁，这时争用线程则会停止自旋而进入到阻塞状态。所以总体的思想是：先自旋，不成功再进行阻塞，尽量降低阻塞的可能性，这对那些执行时间很短的代码块来说有极大的性能提升。显然，自旋在多处理器（多核心）上才有意义。
* 互斥锁的属性：
  1. `PTHREAD_MUTEX_TIMED_NP`：这是缺省值，也就是普通锁。当一个线程加锁以后，其余请求锁的线程将会形成一个等待队列，并且在解锁后按照优先级获取到锁。这种策略可以确保资源分配的公平性。
  2. `PTHREAD_MUTEX_RECURSIVE_NP`：嵌套锁。允许一个线程对同一个锁成功获取多次，并通过unlock解锁。如果是不同线程请求，则在加锁线程解锁时重新进行竞争。
  3. `PTHREAD_MUTEX_ERRORCHECK_NP`：检错锁。如果一个线程请求同一个锁，则返回`EDEADLK`，否则与`PTHREAD_MUTEX_TIMED_NP`类型动作相同，这样就保证了当不允许多次加锁时不会出现最简单情况下的死锁。
  4. `PTHREAD_MUTEX_ADAPTIVE_NP`：适应锁，动作最简单的锁类型，仅仅等待解锁后重新竞争。
* 在JDK 1.5之前，我们若想实现线程同步，只能通过`synchronized`关键字这一种方式来达成；底层，Java也是通过`synchronized`关键字来做到数据的原子性维护的；`synchronized`关键字是JVM实现的一种内置锁，从底层角度来说，这种锁的获取与释放都是由JVM帮助我们隐式实现的。
* 从JDK 1.5开始，并发包引入了Lock锁，Lock同步锁是基于Java来实现的，因此锁的获取与释放都是通过Java代码来实现与控制的；然而，synchronized是基于底层操作系统的Mutex Lock来实现的，每次对锁的获取与释放动作都会带来用户态与内核态之间的切换，这种切换会极大地增加系统的负担；在并发量较高时，也就是说锁的竞争比较激烈时，synchronized锁在性能上的表现就非常差。
* 从JDK 1.6开始，`synchronized`锁的实现发生了很大的变化；JVM引入了相应的优化手段来提升`synchronized`锁的性能，这种提升涉及到`偏向锁`、`轻量级锁`及`重量级锁`等，从而减少锁的竞争所带来的用户态与内核态之间的切换；这种锁的优化实际上是通过`Java对象头`中的一些`标志位`来去实现的；对于锁的访问与改变，实际上都与Java对象头息息相关。
* 从JDK 1.6开始，对象实例在堆当中会被划分为三个组成部分：对象头、实例数据与对齐填充。
  * 对象头主要由3块内容来构成：
    * `Mark Word`: 记录了对象、锁及垃圾回收相关的信息，在64位的JVM中，其长度也是64bit
      1. 无锁标记
      2. 偏向锁标记
      3. 轻量级锁标记
      4. 重量级锁标记
      5. GC标记
    * `指向类的指针`
    * `数组长度`
* 对于`synchronized`锁来说，锁的升级主要都是通过`Mark Word`中的锁标志位与是否是偏向锁标志位来达成的；`synchronized`关键字所对应的锁都是先从偏向锁开始，随着锁竞争的不断升级，逐步演化至轻量级锁，最后则变成了重量级锁。
* 对于锁的演化来说，它会经历如下阶段：
  * 无锁 -> 偏向锁 -> 轻量级锁 -> 重量级锁
  * 偏向锁：
    * 针对于一个线程来说的，它的主要作用就是优化同一个线程多次获取一个锁的情况；如果一个`synchronized方法`或`synchronized块`被一个线程访问，那么这个方法所在的对象就会在其`Mark Word`中将偏向锁进行标记，同时还会有一个字段来存储该线程的ID；当这个线程再次访问同一个`synchronized方法`或`synchronized块`时，它会检查这个对象的`Mark Word`的偏向锁标记以及是否指向了其`线程ID`，如果是的话，那么该线程就无需再去进入管程（Monitor）了，而是直接进入到该方法体中。
    * 如果是另外一个线程访问这个`synchronized方法`或`synchronized块`，则偏向锁会被取消掉。
  * 轻量级锁：
    * 若第一个线程已经获取到了当前对象的锁，这时第二个线程又开始尝试争抢该对象的锁，由于该对象的锁已经被第一个线程获取到，因此它是偏向锁，而第二个线程在争抢时，会发现该对象头中的Mark Word已经是偏向锁，但里面存储的线程ID并不是自己（是第一个线程），那么它会进行CAS（Compare and Swap），从而获取到锁，这里面存在两种情况：
      1. 获取锁成功：那么它会直接将Mark Word中的线程ID由第一个线程变成自己（偏向锁标记位保持不变），这样该对象依然会保持偏向锁的状态。
      2. 获取锁失败：则表示这时可能会有多个线程同时在尝试争抢该对象的锁，那么这时偏向锁就会进行升级，升级为轻量级锁。
  * 自旋锁（轻量级锁的一种实现方式）：
    * 若自旋失败（依然无法获取到锁），那么锁就会转化为重量级锁，在这种情况下，无法获取到锁的线程都会进入到Monitor（即内核态）。
    * 自旋最大的一个特点就是避免了线程从用户态进入到内核态。
  * 重量级锁：
    * 线程最终从用户态进入到了内核态。

### 编译器对于`synchronized`锁的优化措施：
* 锁消除技术
  * JIT编译器（Just In Time编译器）可以在动态编译同步代码时，使用一种叫做逃逸分析的技术，来通过该项技术判别程序中所使用的锁对象是否只被一个线程所使用，而没有散布到其他线程当中；如果情况就是这样的话，那么JIT编译器在编译这个同步代码时就不会生成synchronized关键字所标识的锁的申请与释放机器码，从而消除了锁的使用流程。
* 锁粗化
  * JIT编译器在执行动态编译时，若发现前后相邻的synchronized块使用的是同一个锁对象，那么它就会把这几个synchronized块给合并为一个较大的同步块，这样做的好处在于线程在执行这些代码时，就无需频繁申请与释放锁了，从而达到申请与释放锁一次，就可以执行完全部的同步代码块，从而提升性能。

### 原始构成
* `Lock`是一个接口，属于API层面实现的锁，位于`java.util.concurrent.locks`包下。
* `synchronized`是关键字，属于JVM层面实现的锁，底层是通过`monitor对象`来实现的。
  * `synchronized`编译成字节码：
    ```java
    public class SynchronizedAndLockDemo {

        public static void main(String[] args) {

            synchronized (SynchronizedAndLockDemo.class) {

            }

            new ReentrantLock();
        }

    }
    ```
    ```java
    public class SynchronizedAndLockDemo {
      public SynchronizedAndLockDemo();
        Code:
           0: aload_0
           1: invokespecial #1                  // Method java/lang/Object."<init>":()V
           4: return

      public static void main(java.lang.String[]);
        Code:
           0: ldc           #2                  // class SynchronizedAndLockDemo
           2: dup
           3: astore_1
           4: monitorenter
           5: aload_1
           6: monitorexit   // 正常退出时释放锁
           7: goto          15
          10: astore_2
          11: aload_1
          12: monitorexit   // 保证异常退出时也能释放锁
          13: aload_2
          14: athrow
          15: new           #3                  // class java/util/concurrent/locks/ReentrantLock
          18: dup
          19: invokespecial #4                  // Method java/util/concurrent/locks/ReentrantLock."<init>":()V
          22: pop
          23: return
        Exception table:
           from    to  target type
               5     7    10   any
              10    13    10   any
    }
    ```

### 使用方式
* `synchronized`不需要用户手动释放锁，当`synchronized`代码执行完后系统会自动让线程释放对锁的占用。
* `ReentrantLock`则需要用户手动释放锁，若是没有主动释放锁，就很可能导致出现死锁。`lock()`方法和`unlock()`方法需要配对使用且需要配合`try/finally`语句块使用。

### 等待是否可中断
* `synchronized`不可中断，除非抛出异常或者正常运行完成。
* `ReentrantLock`可中断
  * 调用超时方法`tryLock(long timeout, TimeUnit unit)`
  * 使用`lockInterruptibly()`方法获取锁，然后调用线程的`interrupt()`方法可进行中断

### 加锁是否公平
* `synchronized`是非公平锁。
* `ReentrantLock`两者都可以，默认为非公平锁。
  ```java
  // 非公平锁
  Lock unfairLock = new ReentrantLock();
  Lock unfairLock = new ReentrantLock(false);

  // 公平锁
  Lock fairLock = new ReentrantLock(true);
  ```

### 锁绑定多个条件`Condition`
* `synchronized`没有。
* `ReentrantLock`用来实现分组唤醒需要唤醒的线程们，**可以精确唤醒**，而不是像`synchronized`那样随机唤醒或者唤醒全部线程。
  * 实现精确唤醒的示例代码：
    ```java
    /** 多线程之间按顺序执行，实现 T1 -> T2 -> T3 三个线程依次执行 */

    class ShareResource {

        private int       num  = 1; // T1:1  T2:2  T3:3
        private Lock      lock = new ReentrantLock();
        private Condition c1   = this.lock.newCondition();
        private Condition c2   = this.lock.newCondition();
        private Condition c3   = this.lock.newCondition();

        public void printT1() {
            this.lock.lock();
            try {
                while (this.num != 1) {
                    this.c1.await();
                }

                for (int i = 1; i <= 2; i++) {
                    System.out.println(Thread.currentThread().getName() + "\t" + i);
                }

                // 只通知唤醒T2线程
                this.num = 2;
                this.c2.signal();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                this.lock.unlock();
            }
        }

        public void printT2() {
            this.lock.lock();
            try {
                while (this.num != 2) {
                    this.c2.await();
                }

                for (int i = 1; i <= 3; i++) {
                    System.out.println(Thread.currentThread().getName() + "\t" + i);
                }

                // 只通知唤醒T3线程
                this.num = 3;
                this.c3.signal();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                this.lock.unlock();
            }
        }

        public void printT3() {
            this.lock.lock();
            try {
                while (this.num != 3) {
                    this.c3.await();
                }

                for (int i = 1; i <= 4; i++) {
                    System.out.println(Thread.currentThread().getName() + "\t" + i);
                }

                // 只通知唤醒T1线程
                this.num = 1;
                this.c1.signal();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                this.lock.unlock();
            }
        }
    }

    public class SynchronizedAndLockDemo {

        public static void main(String[] args) {

            ShareResource resource = new ShareResource();

            new Thread(() -> {
                for (int i = 1; i <= 3; i++) {
                    resource.printT1();
                }
            }, "T1").start();

            new Thread(() -> {
                for (int i = 1; i <= 3; i++) {
                    resource.printT2();
                }
            }, "T2").start();

            new Thread(() -> {
                for (int i = 1; i <= 3; i++) {
                    resource.printT3();
                }
            }, "T3").start();
        }

    }
    ```
    ```
    输出结果：
    T1	1
    T1	2
    T2	1
    T2	2
    T2	3
    T3	1
    T3	2
    T3	3
    T3	4
    T1	1
    T1	2
    T2	1
    T2	2
    T2	3
    T3	1
    T3	2
    T3	3
    T3	4
    T1	1
    T1	2
    T2	1
    T2	2
    T2	3
    T3	1
    T3	2
    T3	3
    T3	4
    ```

### 共同点
* 都是用来协调多线程对共享对象、变量的访问。
* 都是可重入锁，同一线程可以多次获得同一个锁。
* 都保证了JMM的三个特性，即：内存可见性、原子性、有序性。

### 不同点
* ReentrantLock 显示的获得、释放锁，synchronized 隐式获得释放锁。
* ReentrantLock 可响应中断、可轮回，synchronized 是不可以响应中断的，为处理锁的不可用性提供了更高的灵活性。
* ReentrantLock 是 API 级别的，synchronized 是 JVM 级别的。
* ReentrantLock 可以实现公平锁和非公平锁，synchronized 是非公平锁。
* ReentrantLock 通过 Condition 可以绑定多个条件。
* 底层实现不一样， **synchronized 是同步阻塞，使用的是悲观并发策略，Lock 是同步非阻塞，采用的是乐观并发策略**。
* Lock 是一个接口，而 synchronized 是 Java 中的关键字，synchronized 是内置的语言实现。
* **synchronized 在发生异常时，会自动释放线程占有的锁，因此不会导致死锁现象发生**；而 Lock 在发生异常时，如果没有主动通过 unLock()去释放锁，则很可能造成死锁现象，因此使用 Lock 时需要在 finally 块中释放锁。
* **Lock 可以让等待锁的线程响应中断**，而 synchronized 却不行，使用 synchronized 时，等待的线程会一直等待下去，不能够响应中断。
* 通过 Lock 可以知道有没有成功获取锁，而 synchronized 却无法办到。
* Lock 可以提高多个线程进行读操作的效率，既就是实现读写锁等。
