# 02_JPS笔记之JSP内置对象

JSP有以下九种内置对象：

	1. request, 请求对象
		request对象代表的是来自客户端的请求，例如我们在form表单中赶写的信息。
		常用方法：getParameter(), getParameterNames()和getParameterValues()。
			  这几个方法用来获取请求对象中所包含的参数的值。

	2. response, 响应对象
		response对象代表的是对客户端的响应，也就是说可以通过 response对象 来组织发送到客户的数据。
		但是由于组织方式比较底层，所以不建议普通读者使用，需要向客户端发送文字时直接使用 out对象 即可。

	3. pageContext, 页面上下文对象
		pageContext对象代表的是当前页面运行的一些属性。一般情况下此对象不是很常用，只有在项目所面临
		的情况比较复杂的情况下，地和会利用到页面属性来辅助处理。一般Servlet容器会使用该对象。
		常用方法：findAttribute(), getAttribute(), getAttributesScope(), getAttributeNamesinScope

	4. session, 会话对象
		session对象代表服务器与客户端所建立的会话，当需要在不同的JSP页面中保留客户信息的情况下使用，
		比如在线购物、客户轨迹跟踪等。
		概要：
			HTTP是无状态（stateless）协议。
			Web Server 对每个客户端请求都没有历史记忆。
			session用来保存客户端状态信息。
		常用方法： setAttribute(),  getAttribute()

	5. application, 应用程序对象
		application对象负责提供应用程序在服务器中运行时的一些全局信息。
		常用方法：getMimeType(), getRealPath()

	6. out, 输出对象
		out对象代表了向客户端发送数据的对象，与response对象不同，通过 out对象 发送的内容将是浏览器需要显示的内容，
		是文本一级的，可以通过 out对象 直接向客户端写一个由程序动态生成HTML文件。常用的方法除了
		print()和println()之外，还包括clear(), clearBuffer(), flush(), getBufferSize()和getRemaining(), 
		这里因为 out对象 内部包含了一个缓冲区，所以需要一些对缓冲区进行操作的方法。
		out.println(); 这个ln是对源代码的换行，如果要在页面上换行需要使用<br/>标签。
		out.print(); 这个不对源代码换行，源代码指的是 服务器运行JSP页面后 生成的HTML代码。

	7. config, 配置对象
		config对象提供一些配置信息，常用的方法有getInitParamter()和getInitParameterNames(), 
		以获得Servlet初始化时的参数。经常在Servlet中使用,JSP中很少使用。

	8. page, 页面对象
		page对象代表了正在运行的由JSP文件产生的类对象，不建设一般读者使用。
		JSP会转换成Servlet，而Servlet是一个类，page代表这个类本身的一个实例。一般是服务器使用。

	9. exception, 异常对象
		exception对象则代表了JSP文件运行时所产生的异常对象，此对象不能在一般JSP文件中直接使用，
		而只能在使用了 <%@ page isErrorPage="true" %> 页指令(编译器指令)的JSP文件中使用。
		<%@ page isErrorPage="true" %> 页指令(编译器指令) 表示当前页面是一个异常页面。
