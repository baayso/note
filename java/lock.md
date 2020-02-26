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
  // java.util.concurrent.locks.ReentrantLock 通过构造函数指定该锁是否为公平锁，默认是非公平锁。
  
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

### 可重入锁（递归锁）
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

### 自旋锁（SpinLock）
* 是指尝试获取锁的线程不会立即阻塞，而是**采用循环的方式去尝试获取锁**，这样做的好处是减少线程上下文切换的消耗，缺点是循环对CPU的消耗会很大。
* [```Unsafe#getAndAddInt(...)```](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/sun/misc/Unsafe.java#L1031)方法里使用```CAS + 自旋锁```实现，请[参见](https://github.com/baayso/note/blob/master/java/CAS.md#cas%E7%9A%84%E5%BA%95%E5%B1%82%E5%8E%9F%E7%90%86)
* **自己实现一个自旋锁：**
  > 通过CAS操作完成自旋锁，线程A先进来调用lock()方法自己持有锁5秒钟，线程B随后进来后发现当前已经有其他线程获取锁，所以只能通过自旋等待，直到线程A释放锁后线程B才能获取锁。
  ```java
  class SpinLock {

      private AtomicReference<Thread> ar = new AtomicReference<>();

      public void lock() {
          Thread thread = Thread.currentThread();
          System.out.println(thread.getName() + "\tinvoked lock()...");

          while (!this.ar.compareAndSet(null, thread)) {
              // log
          }
      }

      public void unlock() {
          Thread thread = Thread.currentThread();
          this.ar.compareAndSet(thread, null);
          System.out.println(thread.getName() + "\tinvoked unlock()");
      }

  }

  public class SpinLockDemo {

      public static void main(String[] args) {

          SpinLock spinLock = new SpinLock();

          new Thread(() -> {
              spinLock.lock();

              try { TimeUnit.MILLISECONDS.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }

              spinLock.unlock();
          }, "A").start();

          try { TimeUnit.MILLISECONDS.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

          new Thread(() -> {
              spinLock.lock();

              try { TimeUnit.MILLISECONDS.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

              spinLock.unlock();
          }, "B").start();

      }

  }
  ```
  ```
  输出结果：
  A	invoked lock()...
  B	invoked lock()...
  A	invoked unlock()
  B	invoked unlock()
  ```

### 共享锁（读锁）/独占锁（写锁）/互斥锁
* 共享锁（读锁）
  * 该锁可以被多个线程所持有。
* 独占锁（写锁）
  * 该锁一次只能被一个线程所持有。`ReentrantLock`和`synchronized`都是独占锁。
* `java.util.concurrent.locks.ReentrantReadWriteLock`
  * 其读锁是共享锁，其写锁是独占锁。
* 读锁的共享锁可以保证并发读是非常高效的，读写，写读，写写的过程是互斥的。
* 示例代码：
  ```java
  /**
   * 多个线程同时读一份资源没有任何问题，为了满足并发量，多个读取共享资源的操作可以同时进行。
   * <br>
   * 但是如果有一个线程需要对共享资源进行写操作，那么就不应该再有其他线程可以对该资源进行读或者写。
   * <br>
   * 读-读可以同时进行<br>
   * 读-写不可以同时进行<br>
   * 写-写不可以同时进行<br>
   * <br>
   * 写操作：原子 + 独占，整个过程必须是一个整体，不允许被分割。
   */
  class Cache<K, V> {

      private volatile Map<K, V>     map  = new HashMap<>();
      private          ReadWriteLock lock = new ReentrantReadWriteLock();

      public void put(K key, V value) {
          Lock lock = this.lock.writeLock();

          lock.lock();

          try {
              System.out.println(Thread.currentThread().getName() + "\t正在写入：" + key);

              try { TimeUnit.MILLISECONDS.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }

              this.map.put(key, value);

              System.out.println(Thread.currentThread().getName() + "\t正在完成。");
          }
          finally {
              lock.unlock();
          }
      }

      public V get(K key) {
          Lock lock = this.lock.readLock();

          lock.lock();

          try {
              System.out.println(Thread.currentThread().getName() + "\t正在读取...");

              try { TimeUnit.MILLISECONDS.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }

              V value = this.map.get(key);

              System.out.println(Thread.currentThread().getName() + "\t读取完成：" + value);

              return value;
          }
          finally {
              lock.unlock();
          }
      }

      public void clear() {
          // ...
      }
  }

  public class ReadWriteLockDemo {

      public static void main(String[] args) {

          Cache<String, String> cache = new Cache<>();

          for (int i = 1; i <= 5; i++) {
              final String strI = String.valueOf(i);

              new Thread(() -> cache.put(strI, strI), "T" + strI).start();
          }

          for (int i = 1; i <= 5; i++) {
              final String strI = String.valueOf(i);

              new Thread(() -> cache.get(strI), "T" + strI).start();
          }
      }
  }
  ```
  ```
  输出结果：
  T1	正在写入：1
  T1	正在完成。
  T3	正在写入：3
  T3	正在完成。
  T2	正在写入：2
  T2	正在完成。
  T4	正在写入：4
  T4	正在完成。
  T5	正在写入：5
  T5	正在完成。
  T1	正在读取...
  T2	正在读取...
  T4	正在读取...
  T5	正在读取...
  T3	正在读取...
  T3	读取完成：3
  T5	读取完成：5
  T1	读取完成：1
  T2	读取完成：2
  T4	读取完成：4
  ```
