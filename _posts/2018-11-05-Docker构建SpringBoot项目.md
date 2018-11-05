---
layout:     post
title:      Docker构建SpringBoot项目
subtitle:   docker入门系列
date:       2018-11-05
author:     zwilpan
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:
    - Docker
---

#### 构建打包环境
我们需要有一个 Docker 环境来打包构建 Spring Boot 项目

安装 Docker 环境
yum install docker
安装完成后，使用下面的命令来启动 docker 服务，并将其设置为开机启动：

service docker start
chkconfig docker on

如采用CentOS 7中支持的新式 systemd 语法，如下：
systemctl  start docker.service
systemctl  enable docker.service

使用Docker 中国加速器
vi  /etc/docker/daemon.json

#### 添加后：
{
    "registry-mirrors": ["https://registry.docker-cn.com"],
    "live-restore": true
}

重新启动docker
systemctl restart docker
输入docker version 返回版本信息则安装正常。

安装JDK
yum -y install java-1.8.0-openjdk*

配置环境变量
打开 vim /etc/profile

添加一下内容
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64
export PATH=$PATH:$JAVA_HOME/bin
修改完成之后，使其生效

source /etc/profile
输入java -version 返回版本信息则安装正常。

安装MAVEN
下载：http://mirrors.shu.edu.cn/apache/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.tar.gz

解压
tar vxf apache-maven-3.5.2-bin.tar.gz
移动
mv apache-maven-3.5.2 /usr/local/maven3
修改环境变量， 在/etc/profile中添加以下几行

MAVEN_HOME=/usr/local/maven3
export MAVEN_HOME
export PATH=${PATH}:${MAVEN_HOME}/bin
记得执行source /etc/profile使环境变量生效。

输入mvn -version 返回版本信息则安装正常。


使用 Docker 部署 Spring Boot 项目
本地环境变量需要配置
DOCKER_HOST=tcp://192.168.3.130:2375

在pom.xml配置docker打包插件
在项目cipas-web项目中添加DockerFile文件，内容如下：
FROM openjdk:8-jdk-alpine
EXPOSE 8082
VOLUME /tmp
ADD cipas-web.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

#打包
mvn package
#启动
java -jar target/cipas-web.jar
看到 Spring Boot 的启动日志后表明环境配置没有问题，接下来我们使用 DockerFile 构建镜像。

#### mvn package docker:build
第一次构建可能有点慢，当看到以下内容的时候表明构建成功：
...
Step 1 : FROM openjdk:8-jdk-alpine
---> 224765a6bdbe
Step 2 : VOLUME /tmp
---> Using cache
---> b4e86cc8654e
Step 3 : ADD spring-boot-docker-1.0.jar app.jar
---> a20fe75963ab
Removing intermediate container 593ee5e1ea51
Step 4 : ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -jar /app.jar
---> Running in 85d558a10cd4
---> 7102f08b5e95
Removing intermediate container 85d558a10cd4
Successfully built 7102f08b5e95
INFO] Built springboot/spring-boot-docker
INFO] ------------------------------------------------------------------------
INFO] BUILD SUCCESS
INFO] ------------------------------------------------------------------------
INFO] Total time: 54.346 s
INFO] Finished at: 2018-03-13T16:20:15+08:00
INFO] Final Memory: 42M/182M
INFO] ------------------------------------------------------------------------

#### 使用docker images命令查看构建好的镜像：
docker images
REPOSITORY                      TAG                 IMAGE ID            CREATED             SIZE
zwilpan/cipas                    latest              aa97d699dbe9        3 days ago          154 MBspringboot/spring-boot-docker


#### 下一步就是运行该镜像，运行成功之前，需要确定已经关闭了防火墙，指定宿主机的ip端口与docker ip端口建立映射关系
docker run -p 8082:8082 -t cipas-web/cipas    
启动完成之后我们使用docker ps 查看正在运行的镜像：
docker ps
CONTAINER ID        IMAGE                           COMMAND                  CREATED             STATUS              PORTS                    NAMES
fa9f3b2a0ed2        cipas-web/cipas                 "java -Djava.secur..."   3 days ago          Up 3 days           0.0.0.0:8082->8082/tcp   dazzling_yonath

可以看到我们构建的容器正在在运行，可以访问：http://192.168.3.130:8082/

说明使用 Docker 部署 Spring Boot 项目成功


docker部署springboot项目并连接mysql容器

下载mysql镜像
docker pull mysql

运行mysql
docker exec -it mysql bash
sudo docker run --name=mysql -it -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -d mysql


修改user表中的密码 用root可以远程连接
ALTER user 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
刷新
FLUSH PRIVILEGES;

用刚生成的项目镜像启动容器
docker run --rm -d -p 8082:8082 --name cipas-web/cipas  --link mysql:cipas cipas-web/cipas

 容器终止运行后自动删除容器文件

 --rm

 后台启动

 -d

 主机端口映射到容器端口

 -p 8082:8082

 为容器起别名

 --name cipas-web/cipas
 连接提供mysql服务的容器，冒号后面是别名，别名应该和代码中的数据库地址一致(这点真的很重要)

 --link mysql:cipas
 由哪个镜像生成的

 cipas-web/cipas
----------------
