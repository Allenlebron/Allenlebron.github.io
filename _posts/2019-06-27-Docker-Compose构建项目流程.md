---
layout:     post
title:      Docker-Compose构建项目
subtitle:   docker-compose进行服务编排
date:       2019-06-27
author:     zwilpan
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:
    - Docker-Compose
---

## docker环境安装
### docker安装
1. 安装yum-utils：
yum install -y yum-utils \
device-mapper-persistent-data \
lvm2

2. 为yum源添加docker仓库位置：
yum-config-manager \
--add-repo \
https://download.docker.com/linux/centos/docker-ce.repo

3. 安装docker:
yum install docker-ce

4. 启动docker:
systemctl start docker

5. 安装上传下载插件：
yum -y install lrzsz

### docker compose安装
1. 下载地址：https://github.com/docker/compose/releases
2. 安装地址：/usr/local/bin/docker-compose
3. 设置为可执行：sudo chmod +x /usr/local/bin/docker-compose
4. 测试是否安装成功：docker-compose --version

## mysql安装
### 下载镜像文件
docker pull mysql:5.7 
### 创建实例并启动
docker run -p 3306:3306 --name mysql \
-v /mydata/mysql/log:/var/log/mysql \
-v /mydata/mysql/data:/var/lib/mysql \
-v /mydata/mysql/conf:/etc/mysql \
-e MYSQL_ROOT_PASSWORD=root  \
-d mysql:5.7
> 参数说明
- -p 3306:3306：将容器的3306端口映射到主机的3306端口
- -v /mydata/mysql/conf:/etc/mysql：将配置文件夹挂在到主机
- -v /mydata/mysql/log:/var/log/mysql：将日志文件夹挂载到主机
- -v /mydata/mysql/data:/var/lib/mysql/：将配置文件夹挂载到主机
- -e MYSQL_ROOT_PASSWORD=root：初始化root用户的密码
### 通过容器的mysql命令行工具连接
docker exec -it mysql mysql -uroot -proot
### 设置远程访问
grant all privileges on *.* to 'root' @'%' identified by 'root';
flush privileges;
### 进入容器文件系统
docker exec -it mysql /bin/bash

## redis安装
### 下载镜像文件
docker pull redis:3.2
### 创建实例并启动
docker run -p 6379:6379 --name redis -v /mydata/redis/data:/data -d redis:3.2 redis-server --appendonly yes
### 使用redis镜像执行redis-cli命令连接
docker exec -it redis redis-cli

## nginx安装
### 下载镜像文件
docker pull nginx:1.10

### 创建实例并启动
docker run -p 80:80 --name nginx \
-v /mydata/nginx/html:/usr/share/nginx/html \
-v /mydata/nginx/logs:/var/log/nginx  \
-d nginx:1.10

### 修改nginx配置
1. 将容器内的配置文件拷贝到当前目录：docker container cp nginx:/etc/nginx .
2. 修改文件名称：mv nginx conf
3. 终止容器：docker stop nginx
4. 执行命令删除原容器：docker rm $ContainerId
5. 执行以下命令：

docker run -p 80:80 --name nginx \
-v /mydata/nginx/html:/usr/share/nginx/html \
-v /mydata/nginx/logs:/var/log/nginx  \
-v /mydata/nginx/conf:/etc/nginx \
-d nginx:1.10

## rabbitmq安装
### 下载镜像文件
docker pull rabbitmq:management
### 创建实例并启动
docker run -d --name rabbitmq --publish 5671:5671 \
 --publish 5672:5672 --publish 4369:4369 --publish 25672:25672 --publish 15671:15671 --publish 15672:15672 \
rabbitmq:management

## elasticsearch安装
### 下载镜像文件
docker pull elasticsearch:6.4.0
### 创建实例并运行
docker run -p 9200:9200 -p 9300:9300 --name elasticsearch \
-v /mydata/elasticsearch/plugins:/usr/share/elasticsearch/plugins \
-v /mydata/elasticsearch/data:/usr/share/elasticsearch/data \
-d elasticsearch:6.4.0
### 测试
访问会返回版本信息：http://192.168.1.66:9200/
### 安装目录位置
/usr/share/elasticsearch
### 安装head插件(可以不安装，仅用于测试)
1. 进入docker内部bash:docker exec -it elasticsearch /bin/bash
2. 安装插件，具体参考：https://github.com/mobz/elasticsearch-head
3. 测试：http://192.168.1.66:9200/_plugin/head/
### 安装中文分词器IKAnalyzer
1. 进入docker内部bash:docker exec -it elasticsearch /bin/bash
2. 安装中文分词插件，执行以下命令：elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v6.2.2/elasticsearch-analysis-ik-6.2.2.zip
3. 测试：
    - 访问header插件：打开地址http://192.168.1.66:9200/_plugin/head/ 
    - 选择复合查询，输入地址：POST:http://192.168.1.66:9200/_analyze 
    - 输入参数：JSON:{"analyzer":"ik","text":"我们是大数据开发人员"}

## mongodb安装
### 下载镜像文件
docker pull mongo:3.2
### 创建实例并运行
docker run -p 27017:27017 --name mongo -v /mydata/mongo/db:/data/db -d mongo:3.2
### 使用mongo命令进入容器
docker exec -it mongo mongo

## SpringBoot应用命令部署
**docker容器间进行连接才能互相访问**

### 部署mall-admin
docker run -p 8080:8080 --name mall-admin \
--link mysql:db \
-v /etc/timezone:/etc/timezone \
-v /etc/localtime:/etc/localtime \
-v /mydata/app/admin/logs:/var/logs \
-d mall/mall-admin:1.0-SNAPSHOT

### 部署mall-search
docker run -p 8081:8081 --name mall-search \
--link elasticsearch:es \
--link mysql:db \
-v /etc/timezone:/etc/timezone \
-v /etc/localtime:/etc/localtime \
-v /mydata/app/search/logs:/var/logs \
-d mall/mall-search:1.0-SNAPSHOT

### 部署mall-port
docker run -p 8085:8085 --name mall-portal \
--link mysql:db \
--link redis:redis \
--link mongo:mongo \
-v /etc/timezone:/etc/timezone \
-v /etc/localtime:/etc/localtime \
-v /mydata/app/portal/logs:/var/logs \
-d mall/mall-portal:1.0-SNAPSHOT

## SpringBoot应用自动化部署
### 部署文件
document/docker/docker-compose.yml
### 部署命令
docker-compose up -d
----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------

version: '3'
services:
  mysql:
    image: mysql:5.7
    container_name: mysql
    command: mysqld --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root #设置root帐号密码
    ports:
      - 3306:3306
    volumes:
      - /mydata/mysql/data/db:/var/lib/mysql #数据文件挂载
      - /mydata/mysql/data/conf:/etc/mysql/conf.d #配置文件挂载
      - /mydata/mysql/log:/var/log/mysql #日志文件挂载
  redis:
    image: redis:3.2
    container_name: redis
    command: redis-server --appendonly yes
    volumes:
      - /mydata/redis/data:/data #数据文件挂载
    ports:
      - 6379:6379
  nginx:
    image: nginx:1.10
    container_name: nginx
    volumes:
      - /mydata/nginx/nginx.conf:/etc/nginx/nginx.conf #配置文件挂载
      - /mydata/nginx/html:/usr/share/nginx/html #静态资源根目录挂载
      - /mydata/nginx/log:/var/log/nginx #日志文件挂载
    ports:
      - 80:80
  rabbitmq:
    image: rabbitmq:3.7.15-management
    container_name: rabbitmq
    volumes:
      - /mydata/rabbitmq/data:/var/lib/rabbitmq #数据文件挂载
      - /mydata/rabbitmq/log:/var/log/rabbitmq #日志文件挂载
    ports:
      - 5672:5672
      - 15672:15672
  elasticsearch:
    image: elasticsearch:6.4.0
    container_name: elasticsearch
    environment:
      - "cluster.name=elasticsearch" #设置集群名称为elasticsearch
      - "discovery.type=single-node" #以单一节点模式启动
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m" #设置使用jvm内存大小
    volumes:
      - /mydata/elasticsearch/plugins:/usr/share/elasticsearch/plugins #插件文件挂载
      - /mydata/elasticsearch/data:/usr/share/elasticsearch/data #数据文件挂载
    ports:
      - 9200:9200
  kibana:
    image: kibana:6.4.0
    container_name: kibana
    links:
      - elasticsearch:es #可以用es这个域名访问elasticsearch服务
    depends_on:
      - elasticsearch #kibana在elasticsearch启动之后再启动
    environment:
      - "elasticsearch.hosts=http://es:9200" #设置访问elasticsearch的地址
    ports:
      - 5601:5601
  mongo:
    image: mongo:3.2
    container_name: mongo
    volumes:
      - /mydata/mongo/db:/data/db #数据文件挂载
    ports:
      - 27017:27017