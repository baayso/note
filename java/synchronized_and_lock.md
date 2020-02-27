# `synchronized`和`Lock`有什么区别？`Lock`有什么优点？

### 原始构成
* `Lock`是一个接口，属于API层面实现的锁，位于`java.util.concurrent.locks`包下。
* `synchronized`是关键字，属于JVM层面实现的锁，底层是通过`monitor`对象来实现的。
  * `monitorenter`
  * `monitorexit`
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
