# volatile

### volatile是JVM提供的*轻量级*的*同步机制*
* 保证可见性
* **不保证原子性**
* 禁止指令重排序

### JMM（Java Memory Model，Java内存模型）
> 是一种抽象的概念并不真实存在，描述的是一组规则或者规范，通过这组规范定义了程序中各个变量（包括实例字段，静态字段和构成数组对象的元素）的访问方式。
* JMM关于同步的规定：
  * 线程解锁前，必须把共享变量的值刷新回主内存
  * 线程加锁前，必须读取主内存的最新值到自己的工作内存
  * 加锁解锁是同一把锁
* 三个特性：[可见性](#visibility)、[原子性](#atomicity)、[有序性](#ordering)
1. <span id="visibility">**可见性**</span>
   > 由于JVM运行程序的实体是线程，而每个线程创建时JVM都会为其创建一个工作内存（也称栈空间），工作内存是每个线程的私有数据区域，而Java内存模型中规定所有变量都存储在主内存，主内存是共享内存区域，所有线程都可以访问，**但线程对变量的操作（读取赋值等）必须在工作内存中进行，首先要将变量从主内存拷贝到自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存**，不能直接操作主内存中的变量，各个线程中的工作内存中存储着主内存中的**变量副本拷贝**，因此不同线程之间无法访问对方的工作内存，线程间的通信（传值）必须通过主内存来完成。
2. <span id="atomicity">**原子性**</span>
   > 一个操作是不可中断和分割的，要么全部执行成功要么全部执行失败。

   volatile不保证原子性，请使用`java.util.concurrent.atomic`包下的`AtomicInteger`
   ```java
   public class ClassOne {

       volatile int num = 0;

       public void addNum() {
           this.num++;
       }

   }

   public class ClassOne {
     volatile int num;

     public ClassOne();
       Code:
          0: aload_0
          1: invokespecial #1                  // Method java/lang/Object."<init>":()V
          4: aload_0
          5: iconst_0
          6: putfield      #2                  // Field num:I
          9: return

     public void addNum();
       Code:
          0: aload_0
          1: dup
          2: getfield      #2                  // Field num:I
          5: iconst_1
          6: iadd
          7: putfield      #2                  // Field num:I
         10: return
   }
   ```
   ```java
   public class ClassTwo {

       AtomicInteger num = new AtomicInteger();

       public void addNum() {
           num.getAndDecrement();
       }

   }

   public class ClassTwo {
     java.util.concurrent.atomic.AtomicInteger num;

     public ClassTwo();
       Code:
          0: aload_0
          1: invokespecial #1                  // Method java/lang/Object."<init>":()V
          4: aload_0
          5: new           #2                  // class java/util/concurrent/atomic/AtomicInteger
          8: dup
          9: invokespecial #3                  // Method java/util/concurrent/atomic/AtomicInteger."<init>":()V
         12: putfield      #4                  // Field num:Ljava/util/concurrent/atomic/AtomicInteger;
         15: return

     public void addNum();
       Code:
          0: aload_0
          1: getfield      #4                  // Field num:Ljava/util/concurrent/atomic/AtomicInteger;
          4: invokevirtual #5                  // Method java/util/concurrent/atomic/AtomicInteger.getAndDecrement:()I
          7: pop
          8: return
   }
   ```
3. <span id="ordering">**有序性**</span>
   * 为了提高性能，编译器和处理器会对指令做重排序：
     > 源代码 -> 编译器优化重排序 -> 指令集并行重排序 -> 内存系统重排序 -> 最终执行的指令序列
     * 单线程环境里面确保程序最终执行结果和代码顺序执行的结果一致。
     * 处理器在进行重排序时必须要考虑指令之间的数据依赖性。
     * 多线程环境中线程交替执行，由于编译器优化重排，多个线程中使用的变量能否保证一致性是无法确定的，结果无法预测。
       ```java
       public class ReSortSeq {

           int     num  = 0;
           boolean flag = false;

           public void m1() {
               this.num = 1;
               this.flag = true;

               // 由于指令重排序的原因，上面两行语句可能会被重排序为：
               // this.flag = true;
               // this.num = 1;
               // 如果线程一执行了this.flag = true;后进入阻塞状态，然后线程二执行了m2()方法，
               // 会造成代码编写顺序与执行顺序不一致
           }

           public void m2() {
               if (this.flag) {
                   this.num += 10;
               }
           }

       }
       ```
   * **volatile可以禁止指令重排序优化，从而避免多线程环境下程序出现乱序执行的现象。**

   * 内存屏障（Memory Barrier，又称内存栅栏），是一个CPU指令，其作用有两个：
     * 保证特定操作的执行顺序
     * 保证某些变量的内存可见性（利用该特性实现`volatile`的内存可见性）
   * 由于编译器和处理器都能执行指令重排序优化，如果在指令间插入一条`Memory Barrier`则会告诉编译器和处理器，不管什么指令都不能和这条`Memory Barrier`指令重排序，也就是说**通过插入内存屏障指令禁止在内存屏障前后的指令执行重排序优化**。内存屏障指令的另外一个作用是强制刷新各种CPU缓存数据到内存中，因此任何线程都能读取到数据的最新版本。
     * 对`volatile`变量进行写操作时，会在写操作后加入一条`store`屏障指令，将工作内存中的共享变量的值刷新回主内存
     * 对`volatile`变量进行读操作时，会在读操作前加入一条`load`屏障指令，从主内存中读取共享变量的值

### 总结
* 工作内存与主内存同步延迟现象导致的可见性问题：
  > 使用`synchronized`或者`volatile`关键字解决，他们都可以使某个线程修改变量后立即对其他线程可见。
* 对于指令重排序导致的可见性问题和有序性问题：
  > 使用`volatile`关键字解决，`volatile`关键字的另外一个作用就是**禁止指令重排序**

### volatile的使用
1. **懒汉单例**
   * DCL（Double Check Lock，双端检锁）机制：因为指令重排序的原因会存在线程安全问题，可以使用volatile禁止指令重排序以保证线程安全。
   * 某一个线程执行到第一次检测，读取到的instance不为null时，instance的引用对象**可能没有完成初始化**。
     ```java
     instance = new Singleton(); // 可以分为以下3步骤完成（伪代码）

     1: memory = allocate(); // 给对象分配内存空间
     2: init(memory); // 初始化对象
     3: instance = memory; // 设置instance指向刚分配的内存，此时instance != null
     ```
   * 上面伪代码中的第2行和第3行**不存在数据依赖关系**，且无论指令重排序前还是指令重排序后程序的执行结果在单线程中没有改变，因此这种指令重排序优化是允许的。
     ```java
     1: memory = allocate(); // 给对象分配内存空间
     2: instance = memory; // 设置instance指向刚分配的内存，此时instance != null，但是对象还没有初始化完成
     3: init(memory); // 初始化对象
     ```
   * 指令重排序只会保证串行语义执行的一致性（单线程），并不关心多线程间的语义一致性。所以当某一线程访问instance不为null时，由于instance实例未必已初始化完成，也就造成了线程安全问题。
     ```java
     public class Singleton {

         // private static Singleton instance;
         private static volatile Singleton instance;

         private Singleton() {
         }

         public static Singleton getInstance() {
             if (instance == null) {
                 synchronized (Singleton.class) {
                     if (instance == null) {
                         instance = new Singleton();
                     }
                 }
             }

             return instance;
         }
     }
     ```
2. **自旋锁**
   * ```sun.misc.Unsafe```（jdk9开始包名修改为```jdk.internal.misc.Unsafe```）：Java无法直接访问操作系统底层，需要通过本地（native）方法来访问。```Unsafe```类封装了访问操作系统底层的各种方法，相当于一个后门，基于该类可以直接操作特定内存的数据。其内部方法操作可以像C语言指针一样直接操作内存。
     * [```allocateMemory(long bytes)```](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/sun/misc/Unsafe.java#L481)方法用于申请堆外内存，底层是使用C语言的```malloc()```函数向操作系统申请的内存，不在JVM的GC管控之内。且用完需要使用```freeMemory(long address)```方法手动释放内存。
     * [```freeMemory(long address)```](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/sun/misc/Unsafe.java#L570)方法用于释放使用```allocateMemory(long bytes)```或者```reallocateMemory(long address, long bytes)```申请的堆外内存，底层是使用C语言的```free()```函数。
   * [```java.util.concurrent.atomic.AtomicInteger```](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/concurrent/atomic/AtomicInteger.java#L54)类的静态变量和成员变量：
     ```java
     // setup to use Unsafe.compareAndSwapInt for updates
     private static final Unsafe unsafe = Unsafe.getUnsafe();
     private static final long valueOffset; // 表示value变量值在内存中的偏移地址，用于Unsafe类中的方法根据偏移地址获取数据

     static {
         try {
             valueOffset = unsafe.objectFieldOffset
                 (AtomicInteger.class.getDeclaredField("value"));
         } catch (Exception ex) { throw new Error(ex); }
     }

     private volatile int value; // 使用volatile修饰，保证多线程之间的内存可能性
     ```
     * [```java.util.concurrent.atomic.AtomicInteger#getAndIncrement()```](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/concurrent/atomic/AtomicInteger.java#L157)方法是使用```Unsafe#getAndAddInt(...)```方法实现：
       ```java
       /**
        * Atomically increments by one the current value.
        *
        * @return the previous value
        */
       public final int getAndIncrement() {
           // this: AtomicInteger对象本身
           // valueOffset: 该对象值在堆外内存中的偏移地址
           // 1: 需要变动的数据
           return unsafe.getAndAddInt(this, valueOffset, 1);
       }
       ```
   * CAS（Compare And Swap）：是一条CPU并发原语，其功能是判断内存中某个位置的值是否为预期值，如果是则修改为新值，否则放弃修改。这个过程是原子的。
     * Java中```Unsafe```类中封装了实现CAS的方法，调用这些方法，JVM会帮我们实现出CAS汇编指令。
     * CAS是一种完全依赖硬件的功能，通过它实现了原子操作。由于CAS是一种系统原语，原语属于操作系统范畴，是由若干条指令组成，用于完成某个功能的一个过程。且原语的执行必须是连续的，在执行过程中不允许被中断。换句话说CAS是一条CPU原子指令，不会造成数据不一致问题。
     * [```Unsafe#getAndAddInt(...)```](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/sun/misc/Unsafe.java#L1031)方法里使用```CAS + 自旋锁```实现：
       ```java
       // The following contain CAS-based Java implementations used on
       // platforms not supporting native instructions

       /**
        * Atomically adds the given value to the current value of a field
        * or array element within the given object <code>o</code>
        * at the given <code>offset</code>.
        *
        * @param o object/array to update the field/element in
        * @param offset field/element offset
        * @param delta the value to add
        * @return the previous value
        * @since 1.8
        */
       public final int getAndAddInt(Object o, long offset, int delta) {
           int v;
           do {
               v = getIntVolatile(o, offset); // 获取主内存中的值，获取后可能当前线程被挂起，其他线程会修改主内存中的值。
           } while (!compareAndSwapInt(o, offset, v, v + delta)); // 为了检查上一步获取并保存在变量v中的值依然是最新的，所以compareAndSwapInt()方法会先比较工作内存中变量v的值是否与主内存中的值相同。如果相同表示变量v的值依然是最新的，则更新值（v + delta）并返回true，循环结束。如果不相同，继续循环从主内存中取值然后再比较，直至相同并更新完成。
           return v;
       }
       ```
       ```
       假设线程A和线程B两个线程同时执行getAndAddInt(...)方法（两个线程分别跑在不同的CPU上）：

       1: AtomicInteger对象的value原始值为3，即主内存中Atomicinteger对象的value为3。
          根据JMM模型，线程A和线程B在各自的工作内存中各自持有一份值为3的value变量的副本。
          
       2: 线程A通过getIntVolatile(o, offset);获取value变量的值为3，然后线程A被挂起。
       
       3: 线程B也通过getIntVolatile(o, offset);获取到value变量的值为3，
          此时刚好线程B没有被挂起并执行compareAndSwapInt(o, offset, v, v + delta)比较主内存中的值也为3，
          成功修改主内存中的值为4，循环结束，线程B执行结束。
          
       4: 这时线程A恢复，执行compareAndSwapInt(o, offset, v, v + delta)进行比较，
          发现工作内存中的value变量的值为3和主内存中的value变量的值4不一致，说明该值已经被其它线程抢先一步修改过了，
          那A线程本次修改失败，继续循环，重新获取主内存中的value变量的值。
          
       5: 线程A重新获取主内存中的value变量的值，因为value变量修饰了volatile关键字，
          所以其它线程对它的修改，线程A总是能够看到，
          线程A继续执行compareAndSwapInt(o, offset, v, v + delta)进行比较并交换，直至成功。
       ```
     * [```Unsafe#compareAndSwapInt(...)```](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/sun/misc/Unsafe.java#L885)是一个native方法，底层使用C++语言编写：
       ```java
       /**
        * Atomically update Java variable to <tt>x</tt> if it is currently
        * holding <tt>expected</tt>.
        * @return <tt>true</tt> if successful
        */
       public final native boolean compareAndSwapInt(Object o, long offset,
                                                     int expected,
                                                     int x);
       ```
     * ```Unsafe#compareAndSwapInt(...)```native方法的[底层源码](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/share/vm/prims/unsafe.cpp#L1213)：
       ```cpp
       UNSAFE_ENTRY(jboolean, Unsafe_CompareAndSwapInt(JNIEnv *env, jobject unsafe, jobject obj, jlong offset, jint e, jint x))
         UnsafeWrapper("Unsafe_CompareAndSwapInt");
         oop p = JNIHandles::resolve(obj);
         jint* addr = (jint *) index_oop_from_field_offset_long(p, offset);
         // x 是即将更新的值，e是原内存的值
         return (jint)(Atomic::cmpxchg(x, addr, e)) == e;
       UNSAFE_END
       ```
