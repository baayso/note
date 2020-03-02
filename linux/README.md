# Linux

### 性能评估相关的命令
* 整机
  * `top`
  * `uptime`
* CPU
  * `vmstat`
* 内存
  * `free`
* 硬盘
  * `df`
* 硬盘IO
  * `iostat`
* 网络IO
  * `ifstat`

### 生产环境分析及定位CPU占用过高的思路
> 结合Linux和JDK相关工具
* 1）先用`top`命令找出CPU占用比最高的进程
* 2）`ps -ef`或者`jps`进一步定位有异常的进程
* **3）定位到具体线程或者代码**
  * `ps -mp 进程 -0 THREAD,tid,time`
    * `-m`：显示所有的线程
    * `-p pid`：进程使用CPU的时间
    * `-o`：该参数后面为用户自定义格式
* 4）将需要的`线程ID`转换为16进制格式（小写英文格式）
  * `printf "%x\n" 线程ID`
* 5）`jstack 进程ID | grep tid(16进制小写英文格式的线程ID) -A60`

### [JDK自带的JVM监控和性能分析工具](https://docs.oracle.com/javase/8/docs/technotes/tools/)
* `jps`：虚拟机进程状况工具
* `jinfo`：Java配置信息工具
* `jmap`：内存映像工具
  * `jmap -heap 进程ID`：映射堆快照
  * 抓取堆内存
    * 生成hprof文件并下载到本地
    * [MAT分析插件工具](https://www.eclipse.org/mat/downloads.php)
* `jstat`：统计信息监视工具
  * `jstat`命令可以查看堆内存各部分的使用量，以及加载类的信息
  * `jstat [-命令选项][vm进程ID] 间隔时间(单位毫秒) 查询次数`
  * 类加载统计
  * 编译统计
  * 垃圾回收统计
* `jstack`：堆栈异常跟踪工具
* `jconsole`
* `jvisualvm`
