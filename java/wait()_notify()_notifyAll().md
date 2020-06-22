#  `wait()`、`notify()`和`notifyAll()`

### `Object#wait()`与`Thread#sleep()`的区别：
* 在调用`wait()`方法时，线程必须要持有被调用对象的锁，当调用`wait()`方法后，线程就会释放掉该对象的`锁（monitor）`。
* 在调用`Thread`类的`sleep()`方法时，线程是不会释放掉对象的锁的。

### 关于[`wait()`](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/share/vm/runtime/objectMonitor.cpp#L1471)、[`notify()`](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/share/vm/runtime/objectMonitor.cpp#L1706)和[`notifyAll()`](https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/share/vm/runtime/objectMonitor.cpp#L1825)方法的总结：
1. 当调用`wait()`时，首先需要确保调用了`wait()`方法的线程已经持有了对象的锁。
2. 当调用`wait()`后，该线程就会释放掉这个对象的锁，然后进入到`等待状态（wait set）`。
3. 当线程调用了`wait()`后进入到等待状态时，它就可以等待其他线程调用相同对象的`notify()`或`notifyAll()`方法来使得自己被唤醒。
4. 一旦这个线程被其他线程唤醒后，该线程就会与其他线程一同开始竞争这个对象的锁（公平竞争）；只有当该线程获取到了这个对象的锁后，线程才会继续往下执行。
5. 调用`wait()`方法的代码片段需要放在一个`synchronized块`或是`synchronized方法`中，这样才可以确保线程在调用`wait()`方法前已经获取到了对象的锁。
6. 当调用对象的`notify()`方法时，它会随机唤醒该对象`等待集合（wait set）`中的任意一个线程，当某个线程被唤醒后，它就会与其他线程一同竞争对象的锁。
7. 当调用对象的`notifyAll()`方法时，它会唤醒该对象`等待集合（wait set）`中的所有线程，这些线程被唤醒后，又会开始竞争对象的锁。
8. 在某一时刻，只有唯一一个线程可以拥有对象的锁。
