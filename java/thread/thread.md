# Thread

### `CountDownLatch`
* `CountDownLatch`主要有两个方法，当一个或多个线程调用`await()`方法时，调用线程会被阻塞。其他线程调用`countDown()`方法会将计数器`减1`（调用`countDown()`方法的线程不会被阻塞）。当计数器的值变为`零`时，因调用`await()`方法被阻塞的线程会被唤醒，继续执行。

### `CyclicBarrier`
* `CyclicBarrier`的字面意思是可循环（Cyclic）使用的屏障（Barrier）。它要做的事情是：让一组线程到达一个屏障（也可以叫做同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会解除，所有被屏障拦截的线程才会继续执行，使用`CyclicBarrier#await()`方法让线程进入屏障。

### `Semaphore`
* `Semaphore`（信号量）主要用于两个目的：一个是用于多个共享资源的互斥使用，另一个是用于并发线程数的控制。
