# JVM（Java Virtual Machine，Java虚拟机）

### JVM内存结构
* 请阅读[精美图文带你掌握 JVM 内存布局](https://juejin.im/post/5e0708baf265da33c34e495b)
* Java8之后，永久代已经被移除，使用元空间取代。元空间的本质与永久代类似。
* 元空间与永久代之间最大的区别：
  * 永久代使用JVM的堆内存
  * 元空间使用本机物理内存，不在JVM堆内存中。
  * 默认情况下，元空间的大小受本机物理内存限制。类的元数据放入 native memory，字符串池和类的静态变量放入JVM堆中，这样可以加载多少类的元数据就不再由`MaxPermSize`控制，而是由系统的实际可用空间来控制。

### GC作用区域
* 堆区
* 方法区

### 判断对象是否可回收
* 引用计数法
  * 引用和对象是有关联的，如果要操作对象则必须使用引用进行。
  * 因此，一个简单的办法是通过引用计数来判断一个对象是否可以回收。简单的说，给对象中添加一个引用计数器。
    * 每当有一个地方引用它，计数器值加1；
    * 每当有一个引用失效时，计数器值减1。
  * 任何时刻计数器值为零的对象就是不可以再被使用的，那么这个对象就是可回收对象。
  * 缺点：
    * 每次对对象赋值时均要维护引用计数器，且计数器本身也有一定的消耗；
    * **引用计数法很难解决对象之间相互循环引用的问题，所以主流的JVM都没有选用这种算法。**
      * 比如说A对象的一个属性引用B，B对象的一个属性同时引用A，`A.b = B(); B.a = A();`此时A和B对象的计数器都是1，但是，如果没有其他任何地方引用A和B对象的时候，A、B对象其实在系统中是无法发挥任何作用的，既然无法发挥作用那就应该被视为内存垃圾予以清理掉，可是因为此时A和B的计数器的值都是1，虚拟机就无法回收A、B对象。这样就会造成内存浪费。
      * 解决办法：在语言层面处理, 例如Objective-C就使用强弱引用类型来解决这个问题。强引用计数器加1，弱引用不增加。
* 可达性分析算法（枚举根节点做可达性分析，根搜索路径）
  * 为了解决引用计数法的循环引用问题，Java使用了可达性分析的方法。
  * 基本思路就是**通过一系列成为`GC Roots`的对象作为起始点**，从这个被称为`GC Roots`的对象开始向下搜索，如果一个对象到`GC Roots`没有任何引用链相连时，则说明此对象不可用。也即给定一个集合的引用作为根出发，通过引用关系遍历对象图，能被遍历到的（可到达的）对象就被判定为存活；没有被遍历到的就被判定为死亡。
  * 所谓`GC Roots`或者说`tracing GC`的“根节点”**就是一组必须活跃的引用**。
  * Java中可以作为`GC Roots`的对象
    * 虚拟机栈（栈帧中的局部变量表）
    * 方法区中的类静态属性引用的对象
    * 方法区中常量引用的对象
    * 本地方法栈中JNI（Native方法）引用的对象

### JVM调优和参数配置，查看JVM参数默认值
* JVM的参数类型
  * 标配参数
    * `-version`
    * `-help`
    * `-showversion`：输出产品版本并继续
  * X参数（了解）
    * `-Xint`：解释执行
      ```
      $ java -Xint -version
      java version "1.8.0_202"
      Java(TM) SE Runtime Environment (build 1.8.0_202-b08)
      Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, interpreted mode)
      ```
    * `-Xcomp`：第一次使用就编译成本地代码
      ```
      $ java -Xcomp -version
      java version "1.8.0_202"
      Java(TM) SE Runtime Environment (build 1.8.0_202-b08)
      Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, compiled mode)
      ```
    * `-Xmixed`：混合模式（默认）
      ```
      $ java -version
      java version "1.8.0_202"
      Java(TM) SE Runtime Environment (build 1.8.0_202-b08)
      Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)
      ```
  * XX参数（重点）
    * Boolean类型
      > `-XX:+[属性]`表示开启某个属性，`-XX:-<属性>`表示关闭某个属性
      * 是否打印GC收集细节
        * `-XX:+PrintGCDetails`
        * `-XX:-PrintGCDetails`
      * 是否使用串行垃圾回收器
        * `-XX:+UseSerialGC`
        * `-XX:-UseSerialGC`
    * KV键值类型
      > `-XX:[属性键]=[属性值]`
      * `-XX:MetaspaceSize=128m`
      * `-XX:MaxTenuringThreshold=15`
    * 使用`jinfo`查看当前运行程序的配置
      * jinfo -flags <进程ID>：输出JVM全部参数
      * jinfo -flag name <进程ID>：输出对应名称的参数
        ```
        $ jinfo -flag InitialHeapSize 25152
        -XX:InitialHeapSize=268435456

        $ jinfo -flag PrintFlagsFinal 25152
        -XX:-PrintFlagsFinal
        ```
    * 其他
      * **`-Xms`：等价于`-XX:InitialHeapSize`**
      * **`-Xmx`：等价于`-XX:MaxHeapSize`**
* 查看JVM参数默认值
  * 查看初始默认值
    * `java -XX:+PrintFlagsInitial`
  * 查看修改更新
    * `java -XX:+PrintFlagsFinal -version`
  * `=`与`:=`
    > `=`为默认值，`:=`为修改过的值
    ```
      uintx MaxMetaspaceSize          = 4294901760          {product}
      uintx MaxNewSize               := 1424490496          {product}
    ```
  * 打印命令行参数
    ```
    $ java -XX:+PrintCommandLineFlags -version
    -XX:InitialHeapSize=267109312 -XX:MaxHeapSize=4273748992 -XX:+PrintCommandLineFlags -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:-UseLargePagesIndividualAllocation -XX:+UseParallelGC
    java version "1.8.0_202"
    Java(TM) SE Runtime Environment (build 1.8.0_202-b08)
    Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)
    ```

### 常用的基本配置参数
* `-Xms`：等价于`-XX:InitialHeapSize`
  * 初始内存大小，默认为物理内存的1/64
* `-Xmx`：等价于`-XX:MaxHeapSize`
  * 最大分配内存，默认为物理内存的1/4
* `-Xss`：等价于`-XX:ThreadStackSize`
  * 单个线程的内存大小，一般默认为512K~1024K
  ```
  -Xss size
  Sets the thread stack size (in bytes).
  Append the letter k or K to indicate KB, m or M to indicate MB, g or G to indicate GB.
  The default value depends on the platform:

  Linux/ARM (32-bit): 320 KB
  Linux/i386 (32-bit): 320 KB
  Linux/x64 (64-bit): 1024 KB
  OS X (64-bit): 1024 KB
  Oracle Solaris/i386 (32-bit): 320 KB
  Oracle Solaris/x64 (64-bit): 1024 KB
  Windows: The default value depends on virtual memory

  The following examples set the thread stack size to 1024 KB in different units:

  -Xss1m
  -Xss1024k
  -Xss1048576

  This option is equivalent to -XX:ThreadStackSize.
  ```
* `-Xmn`
  * 设置年轻代大小
* `-XX:MetaspaceSize`
  * 设置元空间的大小
  * 元空间的本质和永久代类似，都是对JVM规范中的方法区的实现。不过元空间与永久代之间最大的区别在于：元空间并不使用虚拟机堆空间，而是使用本地内存。因此，默认情况下，元空间大小受本地内存限制。
  * `-Xms10m -Xmx10m -XX:MetaspaceSize=1024m -XX:+PrintFlagsFinal`
* `-XX:PrintGCDetails`
  * 输出详细GC收集日志信息
    ```
    jinfo -flag PrintGCDetails <进程编号>

    VM options:
    -Xms10m -Xmx10m -XX:+PrintCommandLineFlags -XX:+PrintGCDetails -XX:+UseParallelGC
    ```
    ```
    [GC (Allocation Failure) [PSYoungGen: 1966K->503K(2560K)] 1966K->997K(9728K), 0.0008437 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
    [GC (Allocation Failure) [PSYoungGen: 2551K->488K(2560K)] 3045K->1379K(9728K), 0.0008770 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
    [GC (Allocation Failure) [PSYoungGen: 2536K->504K(2560K)] 3427K->1677K(9728K), 0.0006665 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
    [GC (Allocation Failure) [PSYoungGen: 1012K->504K(2560K)] 2185K->1758K(9728K), 0.0011944 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
    [GC (Allocation Failure) [PSYoungGen: 504K->504K(2560K)] 1758K->1774K(9728K), 0.0005198 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
    [Full GC (Allocation Failure) [PSYoungGen: 504K->0K(2560K)] [ParOldGen: 1270K->1471K(7168K)] 1774K->1471K(9728K), [Metaspace: 3390K->3390K(1056768K)], 0.0073477 secs] [Times: user=0.06 sys=0.00, real=0.01 secs] 
    [GC (Allocation Failure) [PSYoungGen: 0K->0K(1536K)] 1471K->1471K(8704K), 0.0002993 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
    [Full GC (Allocation Failure) [PSYoungGen: 0K->0K(1536K)] [ParOldGen: 1471K->1455K(7168K)] 1471K->1455K(8704K), [Metaspace: 3390K->3390K(1056768K)], 0.0082870 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
    Disconnected from the target VM, address: '127.0.0.1:59205', transport: 'socket'
    Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
    Heap
     at GCDetailsDemo.main(GCDetailsDemo.java:10)
     PSYoungGen      total 1536K, used 82K [0x00000000ffd00000, 0x0000000100000000, 0x0000000100000000)
      eden space 1024K, 8% used [0x00000000ffd00000,0x00000000ffd14890,0x00000000ffe00000)
      from space 512K, 0% used [0x00000000fff80000,0x00000000fff80000,0x0000000100000000)
      to   space 1024K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x00000000fff00000)
     ParOldGen       total 7168K, used 1455K [0x00000000ff600000, 0x00000000ffd00000, 0x00000000ffd00000)
      object space 7168K, 20% used [0x00000000ff600000,0x00000000ff76bca8,0x00000000ffd00000)
     Metaspace       used 3428K, capacity 4568K, committed 4864K, reserved 1056768K
      class space    used 369K, capacity 392K, committed 512K, reserved 1048576K
    ```
  * 解读GC日志
    ```
    [GC (Allocation Failure) [PSYoungGen: 1966K->509K(2560K)] 1966K->1015K(9728K), 0.0123801 secs] [Times: user=0.03 sys=0.02, real=0.01 secs]
    ```
    * 规律：[`名称:` `GC前内存占用`->`GC后内存占用`(`该区域内存总大小`)]
    * `GC (Allocation Failure)`：GC类型
    * `PSYoungGen:`：新生代（Young区）
    * `1966K`：GC前新生代内存占用
    * `509K`：GC后新生代内存占用
    * `(2560K)`：新生代总大小
    * `1966K`：GC前JVM堆内存占用
    * `1015K`：GC后JVM堆内存占用
    * `(9728K)`：JVM堆总大小
    * `0.0123801 secs`：GC耗时
    * `Times: user=0.03`：GC用户耗时
    * `sys=0.02`：GC系统耗时
    * `real=0.01 secs`：GC实际耗时
  * 解读FullGC日志
    ```
    [Full GC (Allocation Failure) [PSYoungGen: 510K->0K(2560K)] [ParOldGen: 1229K->1230K(7168K)] 1739K->1230K(9728K), [Metaspace: 3393K->3393K(1056768K)], 0.0084324 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
    ```
    * 规律：[`名称:` `GC前内存占用`->`GC后内存占用`(`该区域内存总大小`)]
    * `Full GC (Allocation Failure)`：GC类型
    * `PSYoungGen:`：新生代（Young区）
    * `510K`：GC前新生代内存占用
    * `0K`：GC后新生代内存占用
    * `(2560K)`：新生代总大小
    * `ParOldGen:`：老年代（Old区）
    * `1229K`：GC前老年代内存占用
    * `1230K`：GC后老年代内存占用
    * `(7168K)`：老年代总大小
    * `1739K`：GC前JVM堆内存占用
    * `1230K`：GC后JVM堆内存占用
    * `(9728K)`：JVM堆总大小
    * `Metaspace:`：元空间
    * `3393K`：GC前元空间内存占用
    * `3393K`：GC后元空间内存占用
    * `(1056768K)`：元空间总大小
    * `0.0084324 secs`：GC耗时
    * `Times: user=0.00`：GC用户耗时
    * `sys=0.00`：GC系统耗时
    * `real=0.01 secs`：GC实际耗时
* `-XX:SurvivorRatio`
  * 设置新生代中`Eden`和`S0/S1`空间的比例
  * 默认：`-XX:SurvivorRatio=8`，Eden:S0:S1=8:1:1
  * 例如：`-XX:SurvivorRatio=4`，Eden:S0:S1=4:1:1
  * `-XX:SurvivorRatio`值就是设置`Eden`区的比例点多少，`S0/S1`比例相同
* `-XX:NewRatio`
  * 设置新生代（年轻代）与老年代在堆内存的占比
  * 默认：`-XX:NewRatio=2`，新生代占1，老年代占2，新生代占整个堆内存的1/3
  * 例如：`-XX:NewRatio=4`，新生代占1，老年代占4，新生代占整个堆内存的1/5
  * `-XX:NewRatio`值就是设置老年代的占比，剩下的1给新生代（年轻代）
* `-XX:MaxTenuringThreshold`
  * 该参数主要是控制新生代需要经历多少次GC晋升到老年代中的最大阈值。
  * 在JVM中用4个bit存储（放在对象头中），所以其最大值是15。
  * 但并非意味着对象必须要经历15次YGC才会晋升到老年代中。例如，当Survivor区空间不够时，便会提前进入到老年代中，但这个次数一定不大于设置的最大阈值。
  * 如果设置为0，则新生代对象不经过Survivor区，直接进入老年代。对于老年代空间大的应用，可以提高效率。如果将此值设置为一个较大值，则新生代对象会在Survivor区进行多次复制，这样可以增加对象在新生代的存活时间，增加在新生代被回收的概率。
  * `jinfo -flag MaxTenuringThreshold <进程编号>`
    ```
    $ jinfo -flag MaxTenuringThreshold 22192
    -XX:MaxTenuringThreshold=15
    ```
* 案例

### 强引用、软引用、弱引用、虚引用
* **强引用**
  * 当内存不足，JVM开始垃圾回收，对于强引用的对象，就算是出现了OOM也不会对该对象进行回收，死都不收。
  * 强引用是我们最常见的普通对象引用，只要还有强引用指问一个对象， 就能表明对象还“活着”，垃圾收集器不会回收这种对象。在Java中最常见的就是强引用，把一个对象赋给一个引用变量，这个引用变量就是一个强引用。当一个对象被强引用变量引用时，它处于可达状态，它是不可能被垃圾回收机制回收的，即使该对象以后水远都不会被用到，JVM也不会回收。因此强引用是造成Java内存泄漏的主要原因之一。
  * 对于一个普通的对象，如果没有其他的引用关系，只要超过了引用的作用域或者显式地将相应(强)引用赋值为null，一般认为就是可以被垃圾回收（当然具体回收时机还是要看垃圾收集策略）。
  ```java
  public class StrongReferenceDemo {

      public static void main(String[] args) {
          Object obj1 = new Object(); // 强引用
          Object obj2 = obj1;
          obj1 = null;

          System.gc();

          System.out.println(obj2);
      }

  }
  ```
  ```
  输出结果：
  java.lang.Object@1b701da1
  ```
* **软引用**
  * 软引用是一种相对强引用弱化了一些的引用，需要使用`java.lang.ref.SoftReference<T>`类来实现，可以让对象豁免一些垃圾回收。
  * 对于只有软引用的对象来说：
    * 当系统**内存充足**时它**不会**被回收；
    * 当系统**内存不足**时它**会**被回收。
  * 软引用通常用在对内存敏感的程序中，比如高速缓存就有用到软引用，内存够用时就保留，不够用时就回收！
  * 内存足够时：
    ```java
    public class SoftReferenceDemo {

        public static void main(String[] args) {
            Object o1 = new Object();
            SoftReference<Object> sr = new SoftReference<>(o1);

            System.out.println(o1);
            System.out.println(sr.get());

            o1 = null;
            System.gc();

            System.out.println(o1);
            System.out.println(sr.get());
        }

    }
    ```
    ```
    输出结果：
    java.lang.Object@15db9742
    java.lang.Object@15db9742
    null
    java.lang.Object@15db9742    // 当系统内存充足时不会被回收
    ```
  * 内存不够时：
    ```
    VM options: 
    -Xms10m -Xmx10m
    ```
    ```java
    import java.lang.ref.SoftReference;

    public class SoftReferenceDemo {

        public static void main(String[] args) {
            Object o1 = new Object();
            SoftReference<Object> sr = new SoftReference<>(o1);

            System.out.println(o1);
            System.out.println(sr.get());

            o1 = null;

            try {
                byte[] bytes = new byte[50 * 1024 * 1024];
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
            finally {
                System.out.println(o1);
                System.out.println(sr.get());
            }

        }

    }
    ```
    ```
    输出结果：
    java.lang.Object@15db9742
    java.lang.Object@15db9742
    java.lang.OutOfMemoryError: Java heap space
     at SoftReferenceDemo.main(SoftReferenceDemo.java:15)
    null
    null    // 当系统内存不足时会被回收
    ```
* **弱引用**
  * 弱引用需要使用`java.lang.ref.WeakReference<T>`类来实现，它比软引用的生存期更短；
  * 对于只有弱引用的对象来说，只要垃圾回收一运行，不管JVM的内存空间是否足够，都会回收该对象占用的内存。
  ```java
  public class WeakReferenceDemo {

      public static void main(String[] args) {
          Object o1 = new Object();
          WeakReference<Object> ref = new WeakReference<>(o1);

          System.out.println(o1);
          System.out.println(ref.get());

          o1 = null;
          System.gc();

          System.out.println("==============================");

          System.out.println(o1);
          System.out.println(ref.get());
      }

  }
  ```
  ```
  输出结果：
  java.lang.Object@15db9742
  java.lang.Object@15db9742
  ==============================
  null
  null
  ```
* **软引用和弱引用的适用场景**
  * 假如有一个应用需要读取大量的本地图片：
    * 如果每次读取图片都从硬盘读取则会严重影响性能；
    * 如果一次性全部加载到内存中又可能造成内存溢出。
  * 使用软引用可以解决上面的问题：
    * 设计思路：用一个`HashMap`来保存图片的路径和相应图片对象关联的软引用之间的映射关系，在内存不足时，JVM会自动回收这些缓存图片对象所占用的空间，从而有效的避免了OOM的风险。
      ```java
      Map<String, SoftReference<Image>> imageCache = new HashMap<>();
      ```
* `java.util.WeakHashMap<K, V>`
  ```java
  public class WeakHashMapDemo {

      public static void main(String[] args) {
          Map<Integer, String> map = new WeakHashMap<>();

          Integer key = new Integer(3);
          String value = "WeakHashMap";

          map.put(key, value);
          System.out.println(map);

          System.out.println("=========================");

          key = null;
          System.out.println(map);

          System.out.println("=========================");

          System.gc();

          System.out.println(map);
          System.out.println("map size = " + map.size());
      }

  }
  ```
  ```
  输出结果：
  {3=WeakHashMap}
  =========================
  {3=WeakHashMap}
  =========================
  {}
  map size = 0
  ```

### OOM

### GC（Garbage Collection，垃圾回收）算法
* 引用计数
* 复制
* 标记-清除
* 标记-清除-整理

### 垃圾回收器

### 生产环境上配置垃圾回收器

### G1垃圾回收器

### 生产环境诊断和评估性能

