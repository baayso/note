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
* ``：
* ``：
