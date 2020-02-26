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
* 可重入锁，又名递归锁，指的是同一线程外层函数获得锁后，内层递归函数仍然能获取该锁的代码。在同一个线程中外层方法获取锁后，在进入内层方法时会自动获取同一把锁。
* 也就是说，线程可以进入任何一个它已经拥有的锁所同步着的代码块。
* 示例代码一：
  ```java
  class Phone {

      public synchronized void sendSMS() {
          System.out.println(Thread.currentThread().getName() + "\tinvoked sendSMS()");

          this.sendEmail();
      }

      public synchronized void sendEmail() {
          System.out.println(Thread.currentThread().getName() + "\t### invoked sendEmail()");
      }

  }

  public class LockDemo {

      public static void main(String[] args) {

          Phone phone = new Phone();

          new Thread(() -> phone.sendSMS(), "T1").start();

          new Thread(() -> phone.sendSMS(), "T2").start();

      }

  }
  ```
  ```
  输出结果：
  T1	invoked sendSMS()
  T1	### invoked sendEmail()
  T2	invoked sendSMS()
  T2	### invoked sendEmail()
  ```
* 示例代码二：
  ```java
  class Resource implements Runnable {

      private Lock lock = new ReentrantLock();

      @Override
      public void run() {
          this.get();
      }

      public void get() {

          this.lock.lock();
          // this.lock.lock(); // 可多次获取锁，但必须与unlock()方法配对

          try {
              System.out.println(Thread.currentThread().getName() + "\tinvoked get()");

              this.set();
          }
          finally {
              this.lock.unlock();
              // this.lock.unlock();
          }
      }

      public void set() {

          this.lock.lock();
          // this.lock.lock(); // 可多次获取锁，但必须与unlock()方法配对

          try {
              System.out.println(Thread.currentThread().getName() + "\t### invoked set()");
          }
          finally {
              this.lock.unlock();
              // this.lock.unlock();
          }
      }

  }

  public class LockDemo {

      public static void main(String[] args) {

          Resource resource = new Resource();

          new Thread(resource, "T1").start();
          new Thread(resource, "T2").start();

      }

  }
  ```
  ```
  输出结果：
  T1	invoked get()
  T1	### invoked set()
  T2	invoked get()
  T2	### invoked set()
  ```

### 自旋锁
