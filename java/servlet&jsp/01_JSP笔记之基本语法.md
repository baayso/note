# JSP笔记之基本语法

## 1. JSP原始代码中包含：JSP元素、Template(模板) Data 两类。
* Template Data指的是JSP引擎不处理的部分，即`<%......%>`以外的部分。例如：HTML内容。
* JSP元素则是指将由JSP引擎直接处理的部分，这部分必须符合Java语法，否则导致编译错误。


> JPS会在修改或创建后的第一次访问（针对所有用户的访问）将JSP页面（JSP原始代码）转换成Servlet。（使用jsp parser jsp解析器，由服务器程序提供，如Tomcat）也就是转换成.java文件。在由JDSK（servlet分析器）编译成.class文件，在执行。<br/>
> 例：JSP表达式(例：`<%=变量名%>`)，其本质是使用`out.print(变量名);`这样的Java代码。


## 2. JSP语法分为三种不同的类型：

### 编译器指令（DIRECTIVE）例如：`<%@ page import = "java.io.*, java.util.*" %>`

	1.编译器指令包括“包含指令”，“页指令”和“taglib指令”

		包含指令（include指令）：向当前页中插入一个静态文件的内容。
			语法：<%@ include file="relativeURL" %> 或 <%@ include="相对位置" %>

		页指令（page指令）：作用于整个JSP页面，同样包括静态的包含文件。但是不能作用于动态的包含文件，比如“<jsp:include>”。
				    可以在一个页面中用上多个 <%@ page %> 指令，但是其中的属性只能使用一次，除import属性外。
				    无论把 <%@ page %> 指令放在JSP的文件的哪个地主，它的作用范围都是整个JSP页面。一般我们放在JSP文件的顶部。
			默认导入的包：java.lang.*, java.servlet.*, javax.servlet.jsp.*, javax.servlet.http.*

		taglib指令：用于引入定制标签库
			语法：<%@ taglib uri="URIToTagLibrary" prefix="tagPrefix" %>

	2.它们包含在 <%@ %> 卷标里
	3.两个主要的指令是 page 与 include


### 脚本语法（SCRIPTING）例如：<% String msg = "This is JSP test."; %>

	1.HTML注释：发送到客户端，但不直接显示，在源代码中可以查看到。语法：<!-- comments -->
		<!-- <%= new java.util.Date().toLocaleString() %> --> 转换成.java文件后的代码（Tomcat）是：
		
		out.write("	<!--");
		out.print( new java.util.Date().toLocaleString() );
		out.print("-->\r\n");
				

	2.隐藏注释（JSP注释）：不发送到客户端。语法：<%-- comments --%>

	3.声明：很少使用，因为写在声明中的代码转换成.java文件后会放在成员变量的位置，而转换后的.java文件又是一个单例模式，
		也就是说在声明中定义的变量在内存中只有存在一个。会造成其它用户访问时出现的是另一个用户修改后的值。
		这样就很容易发生错误，除非我们在声明中定义的是只读变量。
		语法：<%! declaration; [ declaration; ] ... %> 或 <%! 声明; [ 声明; ] ... %>
		
		将下面的例子运行一下，看一下结果会怎样：
		<%! int a = 3; %>   <%-- 成员变量 --%>
		<% int b = 3; %>    <%-- 局部变量 --%>
		
		<%= a-- %><br/>
		<%= b-- %>
		
		结果：刷新页面a的值会一直递减，而b的值一直都是3。

		原因：可以查看转换成servlet后的.java文件，其中 int a = 3; 这条代码的位置处于成员变量的位置，
		      而 int b = 3; 这条代码的位置处于_jspService方法（Tomcat）中（可以理解成doGet()方法），
		      也就是说变量b是一个局部变量。
		      先说明一下，jsp转换成servlet后使用了单例模式。当用户（针对所有用户的访问）第一次访问此页面时，
		      会将此jsp文件转换成servlet，也就是转换成.java文件并编译成.class文件。在将此.class文件加载到内存中。
		      此时，若有第二个用户在来访问此jsp页面将不会在进行转换并编译了，而是使用第一次访问后转换并编译的.class文件，
		      而且此.class文件已经加载到内存中了。
		      所以，变量a在内存中只存在一个（单例）。而变量b是在方法中，是局部变量，每次访问都会从新定义。
		      当我们刷新页面时，就会从新声明变量b并赋值为3，所以 b-- 永远是3。
		      而变量a在内存中只存在一个（单例），在方法中递减此变量，就会看到变量a的值会一直递减。
		      具体请查看转换成servlet后的.java源代码文件。（圣思园Java Web第13集第26分钟）

	4.表达式：<%= msg %>
		注：%>前不能加分号(;)，原因是会让<%= %>中的内容放入到out.print();这条java语句的括号中去执行。

	5.脚本段：<% 代码; %>
		注：脚本段中的java代码每一行必须以分号(;)结束，我们可以在转换成的.java文件（servlet）中看出，
		    脚本段中的代码会直接放入.java文件中；因为脚本段中的代码原本就是合法的java代码。

### 动作指令（ACTION）例如：<jsp:forward>, <jsp:getProperty>, <jsp:include>
	
	<jsp:forward>指令：用于转向页面
		语法：<jsp:forward page={"relativeURL" | <%= expression %>} >
	      	      	<jsp:param name="parameterName" value="{parameterValue | <%= expression %>}" />  <%-- 参数 -->
	      	      </jsp:forward>
		
		注：<jsp:forward>标签从一个JSP文件向另一个文件传递一个包含用户请求的request对象。
	    	<jsp:forward>标签以后的代码，将不会执行；因为页面的流程已经转向了另外一个页面了。
	    	查看转换成servlet后的.java文件，我们可以看到在<jsp:forward>标签后面一条代码是 return; 其后的代码肯定不会在执行了。
        
	<jsp:include>指令：用于包含一个静态或动态文件，可以向被包含的页面传送参数。
		语法：<jsp:include page="{relativeURL | <%= expression %>}" flush="true" >
		      	<jsp:param name="parameterName" value="{parameterValue | <%= expression %>}" />  <%-- 参数 -->
		      </jsp:include>
