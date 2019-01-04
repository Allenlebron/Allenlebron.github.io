---
layout:     post
title:      Jenkins + Docker
subtitle:   自动打包部署
date:       2018-11-22
author:     zwilpan
header-img: img/post-bg-debug.png
catalog: true
tags:
    - Docker
---
# Jenkins + Docker

![avatar](/img/jenkins.png)

+ 1.开发人员在gitLab上打了一个tag

+ 2.gitLab把tag事件推送到Jenkins  

+ 3.获取tag源码，编译，打包，构建镜像

+ 4.Jenkins push 镜像到阿里云仓库  
+ 5.Jenkins 执行远程脚本

  5-1. 远程服务器 pull 指定镜像

  5-2. 停止老版本容器，启动新版本容器

+ 6.通知测试人员部署结果


## 拉取jenkins镜像  
    docker pull jenkins  

    docker images | grep jenkins  

    docker run -d --name myjenkins -p 8080:8080 -v /var/jenkins_home jenkins 

    mkdir /home/jenkins_home               

    docker run -d --name myjenkins -p 8080:8080 -v /home/jenkins_home:/var/jenkins_home jenkins                               
                                            
    docker ps | grep jenkins               

## 轻量级微服务的自动化发布平台  
主要实现思路：  
Jenkins从GitLab中获取源码，构建后生成docker镜像，
以Docker容器的方式进行发布，将生成的Docker镜像推送到本地的Docker Registry，以供生产环境使用。
如此，我们交付的不再是源码，而是Docker镜像，这种方式更加简单高效。


免安装模式
wget http://mirrors.jenkins.io/war-stable/latest/jenkins.war

使用tomcat运行  

获取密码
vi /root/.jenkins/secrets/initialAdminPassword