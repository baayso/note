# Reactor

## 1. 传统Socket编程模式所面临的问题：
> 编程模式简单清晰，适用并发量小的场景。
* 一个Socket一个线程，服务器需要大量的线程用来和客户端通信，但是服务器线程数量会受到服务器资源的限制并不能无限制的创建。
* 线程上下文切换的开销成本会随着线程数量的增长越来越高。
* 连接建立好后，可能并不会一直存在着数据传输，但是服务器线程需要一直保持，造成了资源浪费。

## 2. Reactor编程模式：
> 用来解决传统Socket编程模式所面临的问题

## 3. Reactor模式的角色构成（Reactor模式一共有5种角色构成）：
![Reactor模式类图](https://github.com/baayso/note/blob/master/java/reactor/reactor-pattern%20class%20diagram.png)
1) Handle（句柄或者叫描述符）：表示一种资源，是由操作系统提供的；该资源用于表示一个个的事件，比如说文件描述符，或是针对网络编程中的Socket描述符。事件即可以来自于外部，也可以来自于内部；外部事件比如说客户端的连接请求，客户端发送过来的数据等；内部事件比如说操作系统产生的定时器事件等。它本质上是一个文件描述符。Handle是事件产生的发源地。
2) Synchronous Event Demultiplexer（同步事件分离器）：它本身是一个系统调用，用于等待事件的发生（事件可能是一个，也可能是多个）。调用方在调用它的时候会被阻塞，一直阻塞到同步事件分离器上有事件产生为止。对于Linux来说，同步事件分离器指的就是常用的I/O多路复用机制，如select、poll、epoll等。在Java NIO领域中，同步事件分离器对应的组件就是Selector；对应的阻塞方法就是select()方法。
3) Event Handler（事件处理器）：
4) Concrete Event Handler（具体事件处理器）：
5) Initiation Dispatcher（初始分发器）：
