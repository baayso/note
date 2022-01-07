# 03_JSP笔记之session对象

    session, 会话对象
        session对象代表服务器与客户端所建立的会话，当需要在不同的JSP页面中保留客户信息的情况下使用，
        比如在线购物、客户轨迹跟踪等。
        概要：
          HTTP是无状态（stateless）协议。
          Web Server 对每个客户端请求都没有历史记忆。
          session用来保存客户端状态信息。
        常用方法： setAttribute(),  getAttribute()


### session的概念：
session用于跟踪客户的状态。session指的是在一段时间内，单个客户与Web服务器的一连串相关的交互过程。在一个session中，客户可能会多次请求访问同一个网页，也有可能请求访问不同的服务器资源。


### session的执行过程：

当一个session开始时，Servlet容器将创建一个HttpSession对象，在HttpSession对象中可以存放客户状态的信息（例如购物车）。

Servlet容器为HttpSession分配一个惟一标志符，称为Session ID。Servlet容器把Session ID作为Cookie保存在客户的浏览器中。

每次客户发生HTTP请求时，Servlet容器可以从HTTPServletRequest对象中读取Session ID，然后根据Session ID找到相应的HttpSession对象，从面获取客户的状态信息。


### 总结：

当某个客户端向服务器第一次发送请求时（或称为发起一个新的会话），那么此请求中肯定不包括session id。服务器将创建一个HttpSession对象并分配一个Session ID。当正常操作完成后，服务器会将此Session ID以Cookie的形式返回给客户端。
当客户端再次发送新的请求时，会同时将上次服务器返回的Session ID也发送给服务器。然后服务器可以通过HttpServletRequest对象获取到客户端发送过来的Session ID。服务器获取到SessionID后，会将此SessionID与自己保存的SessionID（应该是通过HashMap来保存的）进行比较，以找到相应的数据。
假如又有另外一个客户端向服务器发送第一次请求，将重复上述过程。


session有一个 不活动状态的最大时间间隔，不活动状态的时间间隔：指的是客户端在发送一次请求后到下一次发送一个新的请求的时间间隔。默认是30分钟。如果上次请求到下次请求的时间间隔超过这个时间，session自动失效。
可以通过setMaxInactiveInterval()方法来设置这个时间，参数是以秒为单位。如果设置成负数，表示此session对象永远不销毁也就是不限制session处于不活动状态的时间。

有时候我们在论坛发帖子，中午吃饭去了，回来后需要重新登录，这就是session的 不活动状态的最大时间间隔到了。

以前我们公司用的邮箱也是用Java平台写的，其中有个设置叫做“邮箱自动断开时间”，就是在一定的时间内如果不操作邮箱的话，将自动断开。我想这个设置其实就是设置session的 不活动状态的最大时间间隔。


将浏览器关闭，session对象并不会被销毁。浏览器关不关闭，服务器也不知道，虽然可以用第三手段来实现，但是不能保证100%的情况下可以通知到服务器。
严格来讲，做不到这一点，可以做一点努力的办法：在所有的客户端页面里使用javascript代码window.onclose来监视浏览器的关闭动作，然后向服务器发送一个请求来删除session。但是对于浏览器崩溃或者强行干掉进程这些非常规手段仍然无能为力。


服务器判断一次会话是不是新会话，主要就是通过SessionID。如果客户端发送的请求中不包括SessionID，那么服务器将认为这是一次新的会话，当浏览器关闭后，浏览器所保存的SessionID会被删除掉，而服务器的session对象并没有销毁，直到他的 不活动状态的最大时间间隔 超过指定时间后才会销毁。
所以每次重新打开一个浏览器，发送的任何请求都是不包括SessionID的，那么服务器判定这是一次新会话，将重新创建HttpSession对象并分配SessionID（注：客户端请求的JSP页面需要支持session，才会重新开始一个新的会话。默认都支持。通过 <%@ page session="true" %> 可以设置JSP页面支持session。）。
