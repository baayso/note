# Lock（锁）

### 公平锁/非公平锁
* **公平锁**
  * 是指多个线程按照申请锁的顺序来获取锁，类似队列，先来后倒。
  * 在并发环境下，每个线程在获取锁时会先查看此锁维护的等待队列，如果为空，或者当前线程是等待队列的第一个，就占有锁，否则就会加入到等待队列中，以后会按照FIFO的规则从队列中取到自己。
* **非公平锁**
  * 是指多个线程获取锁的顺序并不是按照申请锁的顺序，有可能后申请锁的线程比先申请锁的线程先获得锁。在高并发的情况下，有可能会造成优先级反转或者饥饿现象。
  * 非公平锁比较霸道，一上来就直接尝试占有锁，如果尝试失败，就再采用类似公平锁那种方式来获取锁。
  * 非公平锁的优点在于吞吐量比公平锁大。
  * `synchronized`关键字是一种非公平锁。
* **Java中创建公平锁和非公平锁：**
  ```java
  // ReentrantLock通过构造函数指定该锁是否为公平锁，默认是非公平锁。
  
  // 公平锁
  Lock nonFairLock = new ReentrantLock();
  Lock nonFairLock = new ReentrantLock(false);

  // 非公平锁
  Lock fairLock = new ReentrantLock(true);

  // ReentrantLock构造函数源码：
  public class ReentrantLock implements Lock, java.io.Serializable {

      ...

      /**
       * Creates an instance of {@code ReentrantLock}.
       * This is equivalent to using {@code ReentrantLock(false)}.
       */
      public ReentrantLock() {
          sync = new NonfairSync();
      }

      /**
       * Creates an instance of {@code ReentrantLock} with the
       * given fairness policy.
       *
       * @param fair {@code true} if this lock should use a fair ordering policy
       */
      public ReentrantLock(boolean fair) {
          sync = fair ? new FairSync() : new NonfairSync();
      }

      ...

  }
  ```

### 可重入锁/递归锁

### 自旋锁
