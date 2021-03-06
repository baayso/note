# Java Other 笔记

## 1. Runtime.getRuntime().availableProcessors()
Runtime.getRuntime().availableProcessors()并非都能返回你所期望的数值。
比如说，在我的双核1-2-1机器上，它返回的是2，这是对的。不过在我的1-4-2机器 上，也就是一个CPU插槽，4核，每个核2个超线程，这样的话会返回8。
不过我其实只有4个核，如果代码的瓶颈是在CPU这块的话，我会有7个线程在同时 竞争CPU周期，而不是更合理的4个线程。如果我的瓶颈是在内存这的话，那这个测试我可以获得7倍的性能提升。

不过这还没完！Java Champions上的一个哥们发现了一种情况，
他有一台16-4-2的机器 （也就是16个CPU插槽，每个CPU4个核，每核两个超线程，返回的值居然是16！
从我的i7 Macbook pro上的结果来看，我觉得应该返回的是16*4*2=168。在这台机器上运行Java 8的话，它只会将通用的FJ池的并发数设置成15。
正如 Brian Goetz所指出的，“虚拟机其实不清楚什么是处理器，它只是去请求操作系统返回一个值。同样的，操作系统也不知道怎么回事，它是去问的硬件设备。硬件会告诉它一个值，通常来说是硬件线程数。操作系统相信硬件说的，而虚拟机又相信操作系统说的。”

## 2. Integer
* Integer 类有一个缓存，它会缓存介于-128～127 之间的整数。

## 3. new 关键字
1) 申请内存空间用来存储对象。
2) 调用类的构造方法。
3) 返回生成的对象的地址。

## 4. Java中不可以将`基本数据类型`做为泛型参数类型的原因是：
* Java的泛型是类型擦除的，擦除后，会转换为`Object`；而`Object`必须『指向』对象，不能『指向』`基本数据类型`的值。
