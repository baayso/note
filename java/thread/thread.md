# Thread

### `CountDownLatch`
* `CountDownLatch`主要有两个方法，当一个或多个线程调用`await()`方法时，调用线程会被阻塞。其他线程调用`countDown()`方法会将计数器`减1`（调用`countDown()`方法的线程不会被阻塞）。当计数器的值变为`零`时，因调用`await()`方法被阻塞的线程会被唤醒，继续执行。
* 示例代码：
  ```java
  public class CountDownLatchDemo {

      public static void main(String[] args) throws InterruptedException {

          CountDownLatch countDownLatch = new CountDownLatch(7);

          for (int i = 1; i <= 7; i++) {

              new Thread(() -> {

                  System.out.println(Thread.currentThread().getName() + "\t工作完成，离开公司。");

                  countDownLatch.countDown();

              }, "T" + i).start();

          }

          countDownLatch.await();

          System.out.println(Thread.currentThread().getName() + "\t### 领导最后锁上公司大门走人。");
      }

  }
  ```
  ```
  输出结果：
  T1	工作完成，离开公司。
  T2	工作完成，离开公司。
  T3	工作完成，离开公司。
  T4	工作完成，离开公司。
  T5	工作完成，离开公司。
  T6	工作完成，离开公司。
  T7	工作完成，离开公司。
  main	### 领导最后锁上公司大门走人。
  ```

### `CyclicBarrier`
* `CyclicBarrier`的字面意思是可循环（Cyclic）使用的屏障（Barrier）。它要做的事情是：让一组线程到达一个屏障（也可以叫做同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会解除，所有被屏障拦截的线程才会继续执行，使用`CyclicBarrier#await()`方法让线程进入屏障。
* 示例代码：
  ```java
  public class CyclicBarrierDemo {

      public static void main(String[] args) {

          // CyclicBarrier(int parties, Runnable barrierAction)
          CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> System.out.println("####### 收集到所有卡片"));

          for (int i = 1; i <= 7; i++) {

              final String strI = String.valueOf(i);

              new Thread(() -> {

                  System.out.println(Thread.currentThread().getName() + "\t收集到第 " + strI + " 张卡片");

                  try {
                      cyclicBarrier.await();
                  }
                  catch (InterruptedException e) {
                      e.printStackTrace();
                  }
                  catch (BrokenBarrierException e) {
                      e.printStackTrace();
                  }

              }, "T" + strI).start();
          }
      }

  }
  ```
  ```
  输出结果：
  T1	收集到第 1 张卡片
  T3	收集到第 3 张卡片
  T4	收集到第 4 张卡片
  T2	收集到第 2 张卡片
  T5	收集到第 5 张卡片
  T7	收集到第 7 张卡片
  T6	收集到第 6 张卡片
  ####### 收集到所有卡片
  ```

### `Semaphore`
* `Semaphore`（信号量）主要用于两个目的：一个是用于多个共享资源的互斥使用，另一个是用于并发线程数的控制。
* 示例代码：
  ```java

  ```
  ```
  输出结果：

  ```
  
