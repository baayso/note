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
   > 由于JVM运行程序的实体是线程，而每个线程创建时JVM都会为其创建一个工作内存（也称栈空间），工作内存是每个线程的私有数据区域，而Java内存模型中规定所有变量都存储在主内存，主内存是共享内存区域，所有线程都可以访问，**但线程对变量的操作（读取赋值等）必须在工作内存中进行，首先要将变量从主内存拷贝到自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存**，不能直接操作主内存中的变量，各个线程中的工作内存中存储首主内存中的**变量副本拷贝**，因此不同线程之间无法访问对方的工作内存，线程间的通信（传值）必须通过主内存来完成。
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
   * volatile可以禁止指令重排序优化，从而避免多线程环境下程序出现乱序执行的现象。

### 总结
* 工作内存与主内存同步延迟现象导致的可见性问题：
  > 使用`synchronized`或者`volatile`关键字解决，他们都可以使某个线程修改变量后立即对其他线程可见。
* 对于指令重排序导致的可见性问题和有序性问题：
  > 使用`volatile`关键字解决，`volatile`关键字的另外一个作用就是**禁止指令重排序**
