# Linux

### 性能评估相关的命令
* **整机**
  * `top`（切换内存的显示单位：top运行时，顶部内存信息按`E`切换，下方进程信息按`e`切换）
    ```
    $ top
    top - 10:14:03 up 34 min,  1 user,  load average: 2.27, 2.57, 2.18
    Tasks: 180 total,   2 running, 127 sleeping,   0 stopped,   0 zombie
    %Cpu(s): 18.3 us, 28.5 sy,  0.0 ni, 53.0 id,  0.1 wa,  0.0 hi,  0.1 si,  0.0 st
    MiB Mem : 3944.523 total,  111.035 free,  933.750 used, 2899.738 buff/cache
    MiB Swap: 4095.996 total, 4095.996 free,    0.000 used. 2729.422 avail Mem 

     PID  USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND
     5240 baayso    20   0 3277.9m  53.6m  15.8m S  67.8  1.4  12:24.83 java             // 注意
     4634 baayso    20   0  593.8m  41.5m  29.4m R  58.8  1.1  13:33.44 deepin-terminal
     5611 root      20   0    0.0m   0.0m   0.0m I  22.9  0.0   0:18.48 kworker/u8:2
     5306 root      20   0    0.0m   0.0m   0.0m I  13.3  0.0   1:32.48 kworker/u8:5
     4033 baayso    20   0 1945.2m 254.8m  80.1m S  10.3  6.5   2:53.89 QQ.exe
     5124 root      20   0    0.0m   0.0m   0.0m I   9.3  0.0   1:33.37 kworker/u8:1
     3823 baayso    20   0    9.4m   6.9m   1.9m S   6.0  0.2   2:14.22 wineserver.real
     2970 root      20   0  420.5m 106.8m  60.3m S   4.3  2.7   1:03.29 Xorg
     4866 baayso    20   0  590.9m  38.1m  27.9m S   1.7  1.0   0:12.04 deepin-terminal
     3343 baayso    20   0 3082.2m  64.7m  53.2m S   0.7  1.6   0:08.19 kwin_x11
     3262 baayso    20   0  385.4m  58.0m  20.5m S   0.3  1.5   0:02.65 fcitx
     3277 baayso    20   0   44.3m   3.7m   3.1m S   0.3  0.1   0:02.48 dbus-daemon
     3673 baayso    20   0 3198.5m  97.2m  51.0m S   0.3  2.5   0:02.60 sogou-qimpanel
     5202 baayso    20   0  839.5m  72.0m  60.9m S   0.3  1.8   0:00.76 deepin-menu
        1 root      20   0  135.9m   6.9m   5.2m S   0.0  0.2   0:01.63 systemd
        2 root      20   0    0.0m   0.0m   0.0m S   0.0  0.0   0:00.00 kthreadd
        4 root       0 -20    0.0m   0.0m   0.0m I   0.0  0.0   0:00.00 kworker/0:0H
        6 root       0 -20    0.0m   0.0m   0.0m I   0.0  0.0   0:00.00 mm_percpu_wq
        7 root      20   0    0.0m   0.0m   0.0m S   0.0  0.0   0:00.28 ksoftirqd/0
        8 root      20   0    0.0m   0.0m   0.0m I   0.0  0.0   0:01.69 rcu_sched
        9 root      20   0    0.0m   0.0m   0.0m I   0.0  0.0   0:00.00 rcu_bh
       10 root      rt   0    0.0m   0.0m   0.0m S   0.0  0.0   0:00.00 migration/0
    ```
  * `uptime`：精简版系统性能命令
    ```
    $ uptime
    10:25:43 up 46 min,  1 user,  load average: 2.29, 2.64, 2.49
    ```
* **CPU**
  * `vmstat`：对操作系统的虚拟内存、进程、CPU活动进行监控。可以对系统的整体情况进行统计，不足之处是无法对某个进程进行深入分析。
    * `vmstat [-n] [-S unit] [delay [count]]`
    * `-n`：只在开始时显示一次各字段名称。
    * `-S`：使用指定单位显示。参数有 `k` 、`K` 、`m` 、`M`，分别代表`1000 bytes`、`1024 bytes`、`1000000 bytes`、`1048576 bytes`。默认单位为`K（1024 bytes）`
    * `delay`：刷新时间间隔。如果不指定，只显示一条结果。
    * `count`：刷新次数。如果不指定刷新次数，但指定了刷新时间间隔，这时刷新次数为无穷。
  * 使用示例：
    ```
    $ vmstat -n -S m 2 3
    procs -----------memory---------- ---swap-- -----io---- -system-- ------cpu-----
     r  b   swpd   free   buff  cache   si   so    bi    bo   in   cs us sy id wa st
     4  0      0    109     48   3011    0    0   107   285 1950 2236 14 25 57  4  0
     1  0      0    106     48   3014    0    0     0     8 28079 289201 18 28 54  0  0
     2  0      0    119     48   3000    0    0     0     0 33559 141320 20 24 56  0  0
    ```
    * `procs`
      * r：运行和等待CPU时间片的进程数，原则上单核CPU的运行队列不要超过2，整个系统的运行队列不能超过总核数的2倍，否则表示系统压力过大。
      * b：等待资源的进程数，比如正在等待磁盘I/O、网络I/O等。
    * `cpu`
      * `us`：用户进程消耗CPU时间百分比，us值高表示用户进程消耗CPU时间多，如果长期大于50%需要优化程序。
      * `sy`：内核进程消耗的CPU时间百分比。
      * **`us + sy`参考值为80%，如果`us + sy`大于80%，说明可能存在CPU不足。**
      * `id`：处于空闲的CPU百分比。
      * `wa`：系统等待I/O的CPU时间百分比。
      * `st`：来自于一个虚拟机偷取CPU时间的百分比。
  * 安装`sysstat`工具
    * CentOS：`sudo yum install sysstat`
    * Ubuntu：`sudo apt-get install sysstat`
  * `mpstat -P ALL 2`：查看所有CPU核心信息（需先安装`sysstat`）
    ```
    $ mpstat -P ALL 2
    Linux 4.15.0-30deepin-generic (baayso-linux) 	03/03/2020 	_x86_64_	(4 CPU)

    11:06:28 AM  CPU    %usr   %nice    %sys %iowait    %irq   %soft  %steal  %guest  %gnice   %idle
    11:06:30 AM  all   18.82    0.00   25.00    0.14    0.00    0.00    0.00    0.00    0.00   56.03
    11:06:30 AM    0   48.78    0.00   14.02    0.00    0.00    0.00    0.00    0.00    0.00   37.20
    11:06:30 AM    1   23.23    0.00   46.46    0.00    0.00    0.00    0.00    0.00    0.00   30.30
    11:06:30 AM    2    2.35    0.00   17.06    0.00    0.00    0.00    0.00    0.00    0.00   80.59
    11:06:30 AM    3    0.00    0.00   18.79    0.00    0.00    0.00    0.00    0.00    0.00   81.21

    11:06:30 AM  CPU    %usr   %nice    %sys %iowait    %irq   %soft  %steal  %guest  %gnice   %idle
    11:06:32 AM  all   17.81    0.00   24.26    0.00    0.00    0.00    0.00    0.00    0.00   57.92
    11:06:32 AM    0   32.75    0.00   11.70    0.00    0.00    0.58    0.00    0.00    0.00   54.97
    11:06:32 AM    1   15.56    0.00   40.00    0.00    0.00    0.00    0.00    0.00    0.00   44.44
    11:06:32 AM    2   13.19    0.00   10.99    0.00    0.00    0.00    0.00    0.00    0.00   75.82
    11:06:32 AM    3   11.05    0.00   33.70    0.00    0.00    0.00    0.00    0.00    0.00   55.25
    ^C

    平均时间:  CPU    %usr   %nice    %sys %iowait    %irq   %soft  %steal  %guest  %gnice   %idle
    平均时间:  all   18.32    0.00   24.98    0.05    0.00    0.00    0.00    0.00    0.00   56.66
    平均时间:    0   28.34    0.00   16.97    0.00    0.00    0.20    0.00    0.00    0.00   54.49
    平均时间:    1   21.44    0.00   41.65    0.00    0.00    0.00    0.00    0.00    0.00   36.91
    平均时间:    2    6.24    0.00   12.85    0.00    0.00    0.00    0.00    0.00    0.00   80.91
    平均时间:    3   17.27    0.00   26.87    0.00    0.00    0.00    0.00    0.00    0.00   55.85
    ```
  * `pidstat -u [采样间隔秒数] -p [进程编号]`：显示指定进程的CPU使用统计（需先安装`sysstat`）
     ```
     $ pidstat -u 1 -p 5240
     Linux 4.15.0-30deepin-generic (baayso-linux) 	03/03/2020 	_x86_64_	(4 CPU)

     11:06:08 AM   UID       PID    %usr %system  %guest    %CPU   CPU  Command
     11:06:09 AM  1000      5240   21.00   44.00    0.00   65.00     0  java
     11:06:10 AM  1000      5240   27.00   45.00    0.00   72.00     0  java
     11:06:11 AM  1000      5240   26.00   45.00    0.00   71.00     0  java
     11:06:12 AM  1000      5240   23.00   41.00    0.00   64.00     0  java
     11:06:13 AM  1000      5240   22.00   44.00    0.00   66.00     0  java
     11:06:14 AM  1000      5240   26.00   38.00    0.00   64.00     0  java
     11:06:15 AM  1000      5240    0.00    0.00    0.00    0.00     0  java
     11:06:16 AM  1000      5240   16.83   31.68    0.00   48.51     0  java
     11:06:17 AM  1000      5240   27.00   40.00    0.00   67.00     0  java
     ^C
     平均时间:  1000      5240   20.98   36.51    0.00   57.49     -  java
     ```
* **内存**
  * `free`：显示系统内存的使用情况，包括物理内存、交换内存（swap）和内核缓冲区内存
    * `-m`：以MB为单位显示内存使用情况
    * `-s <间隔秒数>`：持续观察内存使用状况
    * `free -m`
      ```
      $ free -m
                    total        used        free      shared  buff/cache   available
      Mem:           3944        1186         334          26        2422        2473
      Swap:          4095           0        4095
      ```
    * 经验值
      * 可用内存/系统物理内存 > 70%，内存充足
      * 可用内存/系统物理内存 < 20%，内存不足，需增加内存
      * 20% < 可用内存/系统物理内存 < 70%，内存基本够用
  * `pidstat -r [采样间隔秒数] -p [进程编号]`：显示指定进程的内存使用统计（需先安装`sysstat`）
    ```
    $ pidstat -r 1 -p 5479
    Linux 4.15.0-30deepin-generic (baayso-linux) 	03/03/2020 	_x86_64_	(4 CPU)

    12:35:00 PM   UID       PID  minflt/s  majflt/s     VSZ     RSS   %MEM  Command
    12:35:01 PM  1000      5479      0.00      0.00 3356616  161316   3.99  java
    12:35:02 PM  1000      5479      1.00      0.00 3356616  161316   3.99  java
    12:35:03 PM  1000      5479      0.00      0.00 3356616  161316   3.99  java
    12:35:04 PM  1000      5479      0.00      0.00 3356616  161316   3.99  java
    12:35:05 PM  1000      5479      0.00      0.00 3356616  161316   3.99  java
    12:35:06 PM  1000      5479      0.00      0.00 3356616  161316   3.99  java
    12:35:07 PM  1000      5479      1.00      0.00 3356616  161316   3.99  java
    ^C
    平均时间:  1000      5479      0.29      0.00 3356616  161316   3.99  java
    ```
* **硬盘**
  * `df`：查看磁盘使用情况
    * `-h`：`--human-readable`，使用人类可读的格式
    * `df -h`
      ```
      $ df -h
      FileSystem      Size  Used  Avail  Use%  Mounted on
      /dev/sda1       98G   14G   80G    15%   /
      ```
* **硬盘I/O**
  * `iostat`：磁盘I/O性能评估
    * * `iostat [-xdk] [delay [count]]`
    * `-x`：输出更详细的I/O设备统计信息。
    * `-d`：单独输出Device结果，不包括CPU结果。
    * `-k/-m`：输出结果以kB/mB为单位，而不是以扇区数为单位。
    * `delay`：刷新时间间隔。如果不指定，只显示一条结果。
    * `count`：刷新次数。如果不指定刷新次数，但指定了刷新时间间隔，这时刷新次数为无穷。
    ```
    $ iostat -xdk 2 3
    Linux 4.15.0-30deepin-generic (baayso-linux) 	03/03/2020 	_x86_64_	(4 CPU)

    Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
    scd0              0.00     0.00    0.01    0.00     0.05     0.00     6.74     0.00    0.46    0.46    0.00   0.46   0.00
    sda               4.44     2.77   18.87   63.64   626.93  1044.73    40.52     6.48   78.41   36.02   90.97   1.46  12.05

    Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
    scd0              0.00     0.00    0.00    0.00     0.00     0.00     0.00     0.00    0.00    0.00    0.00   0.00   0.00
    sda               0.00     1.50    0.50    1.00     4.00    10.00    18.67     0.03   17.33   32.00   10.00  17.33   2.60

    Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
    scd0              0.00     0.00    0.00    0.00     0.00     0.00     0.00     0.00    0.00    0.00    0.00   0.00   0.00
    sda               0.00     0.50    0.00    0.50     0.00     4.00    16.00     0.00    4.00    0.00    4.00   4.00   0.20
    ```
    * `rkB/s`：每秒读取数据量kB。
    * `wkB/s`：每秒写入数据量kB。
    * `svctm：I/O请求的平均服务时间，单位毫秒。
    * `await：I/O请求的平均等待时间，单位毫秒；值越小，性能越好。
    * **`util`：每秒有百分之几的时间用于I/O操作。接近100%时，表示磁盘带宽跑满，需要优化程序或者增加磁盘。**
    * `rkB/s`、`wkB/s`根据系统应用的不同会有不同的值，但有规律遵循：长期、超大数据读写，肯定不正常，需要优化程序。
    * `svctm`的值与`await`的值很接近，表示几乎没有I/O等待，磁盘性能好。如果`await`值远高于`svctm`的值，则表示I/O队列等待时间太长，需要优化程序或者更换性能更好的磁盘。
  * `pidstat -d [采样间隔秒数] -p [进程编号]`：显示指定进程的IO使用情况（需先安装`sysstat`）
    ```
    pidstat -d 2 -p 2833
    Linux 4.15.0-30deepin-generic (baayso-linux) 	03/03/2020 	_x86_64_	(4 CPU)

    01:52:07 PM   UID       PID   kB_rd/s   kB_wr/s kB_ccwr/s iodelay  Command
    01:52:09 PM  1000      2833      0.00      3.98      0.00       0  java
    01:52:11 PM  1000      2833      0.00      0.00      0.00       0  java
    01:52:13 PM  1000      2833      0.00      0.00      0.00       0  java
    01:52:15 PM  1000      2833      0.00      0.00      0.00       0  java
    01:52:17 PM  1000      2833      0.00      0.00      0.00       0  java
    01:52:19 PM  1000      2833      0.00      0.00      0.00       0  java
    01:52:21 PM  1000      2833      0.00      0.00      0.00       0  java
    01:52:23 PM  1000      2833      0.00      0.00      0.00       0  java
    ^C
    平均时间:  1000      2833      0.00      0.47      0.00       0  java
    ```
* **网络I/O**
  * `ifstat`：网络流量实时监控
    * 安装
      ```
      wget http://distfiles.macports.org/ifstat/ifstat-1.1.tar.gz
      tar xzvf ifstat-1.1.tar.gz
      cd ifstat-1.1
      ./configure
      make
      make install
      ```
    * `ifstat -tT [delay [count]]`
      * `-t`：在每一行的开头加一个时间戳。
      * `-T`：报告所有监测接口的全部带宽（最后一列有个total，显示所有的接口的in流量和所有接口的out流量，简单的把所有接口的in流量相加，out流量相加）。
      * `delay`：刷新时间间隔。如果不指定，只显示一条结果。
      * `count`：刷新次数。如果不指定刷新次数，但指定了刷新时间间隔，这时刷新次数为无穷。
      ```
      $ ifstat -t 1
        Time          enp0s3      
      HH:MM:SS   KB/s in  KB/s out
      14:36:37      0.00      0.00
      14:36:38     13.97      0.74
      14:36:39     53.03      0.70
      14:36:40      3.22      0.22
      14:36:41      0.00      0.00
      14:36:42      0.00      0.00
      14:36:43      0.00      0.00
      ^C
      ```

### 生产环境分析及定位CPU占用过高的思路
> 结合Linux和JDK相关工具
* 示例代码：
  ```java
  // 编译后运行下面这段代码
  // javac EndlessLoopDemo.java
  // java EndlessLoopDemo

  1 public class EndlessLoopDemo {
  2     public static void main(String[] args) {
  3         while (true) {
  4             System.out.println(new java.util.Random().nextInt(99999999));
  5         }
  6     }
  7 }
  ```
* 1）先用`top`命令找出CPU占用比最高的进程
* 2）`ps -ef | grep java`或者`jps`进一步定位有异常的进程
* **3）定位到具体线程或者代码**
  * `ps -mp <进程ID> -o THREAD,tid,time`
    * `-m`：显示所有的线程
    * `-p pid`：进程使用CPU的时间
    * `-o`：该参数后面为用户自定义格式
    ```
    $ ps -mp 6091 -o THREAD,tid,time
    USER     %CPU PRI SCNT WCHAN  USER SYSTEM   TID     TIME
    baayso   77.9   -    - -         -      -     - 00:15:28
    baayso    0.0  19    - futex_    -      -  6091 00:00:00
    baayso   76.8  19    - -         -      -  6092 00:15:23    // 此线程占用CPU过高，线程ID为6092
    baayso    0.2  19    - futex_    -      -  6093 00:00:01
    baayso    0.1  19    - futex_    -      -  6094 00:00:01
    baayso    0.1  19    - futex_    -      -  6095 00:00:00
    baayso    0.3  19    - futex_    -      -  6096 00:00:01
    baayso    0.0  19    - futex_    -      -  6097 00:00:00
    baayso    0.0  19    - futex_    -      -  6098 00:00:00
    baayso    0.0  19    - futex_    -      -  6099 00:00:00
    baayso    0.0  19    - futex_    -      -  6100 00:00:00
    baayso    0.0  19    - futex_    -      -  6101 00:00:00
    baayso    0.0  19    - futex_    -      -  6102 00:00:00
    baayso    0.0  19    - futex_    -      -  6103 00:00:00
    baayso    0.0  19    - futex_    -      -  6104 00:00:00
    baayso    0.0  19    - futex_    -      -  6105 00:00:00
    baayso    0.0  19    - skb_wa    -      -  6387 00:00:00
    ```
* 4）将需要的`线程ID`转换为十六进制格式（小写英文格式）
  * `printf "%x\n" <线程ID>`
    ```
    $ printf "%x\n" 6092
    17cc
    ```
* 5）`jstack <进程ID> | grep <十六进制小写英文格式的线程ID> -A60`
  ```
  $ jstack 6091 | grep 17cc -A60
  "main" #1 prio=5 os_prio=0 tid=0x00007f92b400a800 nid=0x17cc runnable [0x00007f92bca48000]
     java.lang.Thread.State: RUNNABLE
   at java.io.FileOutputStream.writeBytes(Native Method)
   at java.io.FileOutputStream.write(FileOutputStream.java:326)
   at java.io.BufferedOutputStream.flushBuffer(BufferedOutputStream.java:82)
   at java.io.BufferedOutputStream.flush(BufferedOutputStream.java:140)
   - locked <0x00000000c2416e10> (a java.io.BufferedOutputStream)
   at java.io.PrintStream.write(PrintStream.java:482)
   - locked <0x00000000c24053a0> (a java.io.PrintStream)
   at sun.nio.cs.StreamEncoder.writeBytes(StreamEncoder.java:221)
   at sun.nio.cs.StreamEncoder.implFlushBuffer(StreamEncoder.java:291)
   at sun.nio.cs.StreamEncoder.flushBuffer(StreamEncoder.java:104)
   - locked <0x00000000c2405358> (a java.io.OutputStreamWriter)
   at java.io.OutputStreamWriter.flushBuffer(OutputStreamWriter.java:185)
   at java.io.PrintStream.write(PrintStream.java:527)
   - eliminated <0x00000000c24053a0> (a java.io.PrintStream)
   at java.io.PrintStream.print(PrintStream.java:597)
   at java.io.PrintStream.println(PrintStream.java:736)
   - locked <0x00000000c24053a0> (a java.io.PrintStream)
   at EndlessLoopDemo.main(EndlessLoopDemo.java:4)                  // 有问题的代码的具体位置

  "VM Thread" os_prio=0 tid=0x00007f92b4078800 nid=0x17d1 runnable 

  "GC task thread#0 (ParallelGC)" os_prio=0 tid=0x00007f92b401f800 nid=0x17cd runnable 

  "GC task thread#1 (ParallelGC)" os_prio=0 tid=0x00007f92b4021800 nid=0x17ce runnable 

  "GC task thread#2 (ParallelGC)" os_prio=0 tid=0x00007f92b4023000 nid=0x17cf runnable 

  "GC task thread#3 (ParallelGC)" os_prio=0 tid=0x00007f92b4025000 nid=0x17d0 runnable 

  "VM Periodic Task Thread" os_prio=0 tid=0x00007f92b40cb800 nid=0x17d9 waiting on condition 

  JNI global references: 5
  ```

### [JDK自带的JVM监控和性能分析工具](https://docs.oracle.com/javase/8/docs/technotes/tools/)
* `jps`：虚拟机进程状况工具
  * `jps -l`：输出应用程序main class的完整package名或者应用程序的JAR文件完整路径名
* `jinfo`：Java配置信息工具
  * `jinfo -flags <进程ID>`：输出JVM全部参数
  * `jinfo -flag name <进程ID>`：输出对应名称的参数
     ```
     $ jinfo -flag InitialHeapSize 25152
     -XX:InitialHeapSize=268435456

    $ jinfo -flag PrintFlagsFinal 25152
    -XX:-PrintFlagsFinal
     ```
  * `jinfo -flag [+|-]name <进程ID>`：开启或者关闭对应名称的参数
     ```
     $ jinfo -flag PrintGCDetails 25152
     -XX:-PrintGCDetails

     $ jinfo -flag +PrintGCDetails 25152
     （无任务输出表示设置成功）

     $ jinfo -flag PrintGCDetails 25152
     -XX:+PrintGCDetails
     ```
  * `jinfo -flag name=value <进程ID>`：设定对应名称的参数
* `jmap`：内存映像工具
  * `jmap -heap 进程ID`：映射堆快照
  * `jmap -dump:format=b,file=[dumpFileName] <进程ID>`：抓取堆内存，生成指定进程的hprof文件并保存到指定位置
    ```
    $ jmap -dump:format=b,file=/tmp/heap_dump.hprof 25152
    Dumping heap to /tmp/heap_dump.hprof ...
    Heap dump file created
    ```
* 堆内存溢出时，保存内存快照
  > 在[`JAVA_OPTIONS`变量](https://github.com/baayso/ieasy-server/blob/master/bin/start.sh)中增加以下参数
  * `-XX:+HeapDumpOnOutOfMemoryError`：配置该参数表示当JVM发生OOM时，自动生成DUMP文件。也可以通过`-XX:HeapDumpPath=${保存路径}`来显示指定保存路径
    ```
    -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heap_dump.hprof
    ```
  * `-XX:OnOutOfMemoryError`参数允许用户指定当出现OOM时，指定某个脚本来完成一些动作。
    ```
    -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heap_dump.hprof -XX:OnOutOfMemoryError="sh ~/oom_test.sh"
    ```
* 使用`jconsole`选项通过`HotSpotDiagnosticMXBean`从运行时获得堆转储（生成hprof文件）。
* JVM堆内存分析
  * `jhat`：Java Heap Analysis Tool，是一个用来分析JVM堆情况的命令。
    ```
    $ jhat -port 8082 /tmp/heap_dump.hprof
    jhat -port 8082 /tmp/heap_dump.hprof
    Reading from /tmp/heap_dump.hprof...
    Dump file created Thu Mar 05 12:56:48 CST 2020
    Snapshot read, resolving...
    Resolving 440396 objects...
    Chasing references, expect 88 dots.................................................................
    Eliminating duplicate references.................................................................
    Snapshot resolved.
    Started HTTP server on port 8082
    Server is ready.
    ```
  * [MAT分析插件工具](https://www.eclipse.org/mat/downloads.php)：MAT是Memory Analyzer的简称，是一款功能强大的JVM堆内存分析器。
* `jstat`：统计信息监视工具
  * `jstat`命令可以查看堆内存各部分的使用量，以及加载类的信息
  * `jstat [-命令选项][vm进程ID] 间隔时间(单位毫秒) 查询次数`
  * 类加载统计
  * 编译统计
  * 垃圾回收统计
* `jstack`：堆栈异常跟踪工具
* `jconsole`
* `jvisualvm`
