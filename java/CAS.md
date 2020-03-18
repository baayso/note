# CAS（Compare And Swap/Set，比较并交换）

### 什么是CAS：
* Compare And Swap，或者 Compare And Set，比较并交换
* 比较当前线程工作内存中的值和主内存中的值，如果相同则执行规定操作，否则放弃执行规定操作。

### CAS的底层原理：
* 是一条CPU并发原语，其功能是判断内存中某个位置的值是否为预期值，如果是则修改为新值，否则放弃修改。这个过程是原子的。
* Java中```Unsafe```类中封装了实现CAS的方法，调用这些方法，JVM会帮我们实现出CAS汇编指令。
* CAS是一种完全依赖硬件的功能，通过它实现了原子操作。由于CAS是一种系统原语，原语属于操作系统范畴，是由若干条指令组成，用于完成某个功能的一个过程。且原语的执行必须是连续的，在执行过程中不允许被中断。换句话说CAS是一条CPU原子指令，不会造成数据不一致问题。
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
### CAS的缺点：
* 循环时间长开销很大
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
         // 如果CAS失败，会一直进行尝试。如果CAS长时间一直未能成功，可能会给CPU带来很大的开销。
         do {
             v = getIntVolatile(o, offset); // 获取主内存中的值，获取后可能当前线程被挂起，其他线程会修改主内存中的值。
         } while (!compareAndSwapInt(o, offset, v, v + delta)); // 为了检查上一步获取并保存在变量v中的值依然是最新的，所以compareAndSwapInt()方法会先比较工作内存中变量v的值是否与主内存中的值相同。如果相同表示变量v的值依然是最新的，则更新值（v + delta）并返回true，循环结束。如果不相同，继续循环从主内存中取值然后再比较，直至相同并更新完成。
         return v;
     }
     ```
* 只能保证一个共享变量的原子操作
  * 当对一个共享变量执行操作时，我们可以使用循环CAS的方式来保证原子性，但是对于多个共享变量操作时，循环CAS就无法保证操作的原子性，这个时候需要使用锁来保证原子性。
* ABA问题
  * CAS算法实现一个重要前提需要取出内存中某时刻的数据并在当下时刻比较并替换，那么在这个时间差类会导致数据的变化。
  * 比如说一个线程T1从内存位置V中取出A，这时候另一个线程T2也从内存中取出A，并且线程T2进行了一些操作将值修改成了B，然后线程T2又将V位置的数据修改成A，这时候线T1进行CAS操作发现内存中的值仍然是A，然后线程T1操作成功。**尽管线程T1的CAS操作成功，但是不代表这个过程就是没有问题的**。
    ```java
    public class ABADemo {

        private static AtomicReference<String> ar = new AtomicReference<>("A");

        public static void main(String[] args) {
            new Thread(() -> {
                ar.compareAndSet("A", "B");
                ar.compareAndSet("B", "A");
            }, "T1").start();

            new Thread(() -> {
                // 暂停100毫秒T2线程，保证上面的T1线程完成了一次ABA操作
                try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }

                System.out.println(ar.compareAndSet("A", "ABA"));;

                System.out.println(ar.get());
            }, "T2").start();
        }

    }
    ```
    ```
    运行结果：
    true
    ABA
    ```

### 解决CAS的ABA问题：
* 使用[```java.util.concurrent.atomic.AtomicStampedReference<V>```](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/classes/java/util/concurrent/atomic/AtomicStampedReference.java)：带修改版本号的原子引用类
  ```java
  public class ABADemo {

      private static AtomicStampedReference<String> asr = new AtomicStampedReference<>("A", 1);

      public static void main(String[] args) {
          new Thread(() -> {
              int stamp = asr.getStamp();
              System.out.println(Thread.currentThread().getName() + "\t第1次版本号：" + stamp);

              // 暂停100毫秒T1线程
              try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }

              asr.compareAndSet("A", "B", asr.getStamp(), asr.getStamp() + 1);
              System.out.println(Thread.currentThread().getName() + "\t第2次版本号：" + asr.getStamp());

              asr.compareAndSet("B", "A", asr.getStamp(), asr.getStamp() + 1);
              System.out.println(Thread.currentThread().getName() + "\t第3次版本号：" + asr.getStamp());

          }, "T1").start();

          new Thread(() -> {
              int stamp = asr.getStamp();
              System.out.println(Thread.currentThread().getName() + "\t第1次版本号：" + stamp);

              // 暂停1秒T2线程，保证上面的T1线程完成了一次ABA操作
              try { TimeUnit.MILLISECONDS.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

              boolean ret = asr.compareAndSet("A", "ABA", stamp, stamp + 1);

              System.out.println(Thread.currentThread().getName() + "\t修改是否成功：" + ret + "\t当前最新版本号：" + asr.getStamp());
              System.out.println(Thread.currentThread().getName() + "\t当前最新值：" + asr.getReference());

          }, "T2").start();
      }

  }
  ```
  ```
  运行结果：
  T1	第1次版本号：1
  T2	第1次版本号：1
  T1	第2次版本号：2
  T1	第3次版本号：3
  T2	修改是否成功：false	当前最新版本号：3
  T2	当前最新值：A
  ```
  
