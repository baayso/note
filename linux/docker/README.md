# Docker

### 镜像命令
* `docker images`：列出本地所有镜像
  * `-a`：列出本地所有镜像（含中间镜像层）
  * `-q`：只显示镜像ID
  * `--digests`：显示镜像的摘要信息
  * `--no-trunc`：显示完整的镜像信息
* `docker search [OPTIONS] 镜像名称`：从`https://hub.docker.com`中查找指定名称的镜像（命令行展示）
  * `--no-trunc`：显示完整的镜像信息
  * `-s`：列出收藏数不小于指定值的镜像
  * `--automated`：只列出 automated build 类型的镜像
* `docker pull 镜像名称`：下载镜像
  * `docker pull 镜像名称[:TAG]`：TAG为版本号，默认为`latest`
    * `docker pull mysql:latest`
* `docker rmi 镜像名称或者镜像ID`：删除本地镜像
  * `docker rmi -f 镜像ID`：删除本地单个镜像
  * `docker rmi -f 镜像名1:TAG 镜像名2:TAG`：删除本地多个镜像
  * `docker rmi -f $(docker images -qa)`：删除本地全部镜像

### 容器命令
> 创建容器的前提是本地有镜像
* `docker run [OPTIONS] IMAGE [COMMAND] [ARG...]`：新建并启动容器
  > `docker run -it centos /bin/bash`
  * `--name="容器新名称"`：为容器指定一个名称
  * `-d `：后台运行容器，并返回容器ID，也即启动守护式容器
  * **`-i`：以交互模式运行容器，通常与`-t`同时使用**
  * **`-t`：为容器重新分配一个伪输入终端，通常与`-i`同时使用**
  * `-P`：随机端口映射
  * `-p`：指定端口映射，有以下四种方式：
    * ip:host_port:container_port
    * ip::container_port
    * **host_port:container_port**
    * container_port
* `docker ps [OPTIONS]`：列出当前所有正在运行的容器
  * `-a `：列出当前所有正在运行的容器+历史上运行过的
  * `-l `：显示最近创建的容器
  * `-n `：显示最近n个创建的容器
  * **`-q `：静默模式，只显示容器编号**
  * `--no-trunc `：不截断输出
* 退出容器
  * `exit`：停止容器并退出
  * `CTRL+P+Q`：不停止容器退出
* `docker start 容器ID或者容器名`：启动容器
* `docker restart 容器ID或者容器名`：重启容器
* `docker stop 容器ID或者容器名`：停止容器
* `docker kill 容器ID或者容器名`：强制停止容器
* `docker rm 容器ID`：删除已停止容器
* 一次删除多个容器：
  * `docker rm -f $(docker ps -a -q)`
  * `docker ps -a -q | xargs docker rm`
