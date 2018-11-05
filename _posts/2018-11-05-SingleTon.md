
---
layout:     post
title:      单例模式了解一下
subtitle:   设计模式系列
date:       2018-11-05
author:     zwilpan
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:
    - 设计模式
---
# 单例模式

聊单例模式之前首先要搞清两个问题

1.什么是单例多例
2.怎么产生单例多例

所谓单例模式就是所有的请求都用一个对象来处理，比如我们常用的service和dao层的对象通常都是单例的，而多例则是每个请求都是用新的对象处理，比如我们的action

单例多例都属于对象模式
单例模式整个系统只有一份，而多例可以有多个实例
它们对外都不提供构造方法，即构造方法为私有

通用SSH，单例在spring中是默认的，对于struts2，action必须用多例，因为action本身有请求的值，即可变的状态

之所以用单例，是因为没有必要每个请求都新创建一个对象，浪费内存
之所以用多例，是因为防止并发问题，一个请求改变了对象的状态，此时对象又处理一个另一个请求，就导致了错误的处理

单例模式确保某个类只有一个实例，而且自行实例化并向整个系统提供这个实例。在计算机系统中，线程池、缓存、日志对象、对话框、打印机、显卡的驱动程序对象常被设计成单例。这些应用都或多或少具有资源管理器的功能。每台计算机可以有若干个打印机，但只能有一个Printer Spooler，以避免两个打印作业同时输出到打印机中。每台计算机可以有若干通信端口，系统应当集中管理这些通信端口，以避免一个通信端口同时被两个请求同时调用。总之，选择单例模式就是为了避免不一致状态，避免政出多头。

ok，了解了单例模式的含义之后就可以来实现一个单例了

##一.懒汉式(在需要的时候才实例化自己，线程不安全)
    
# 1.在getInstance方法上加同步
    public class Singleton(){
        private Singleton(){}
        private static Singleton singleton = null;
        private static synchronized Singleton getInstance(){
            if(singleton == null){
                singleton = new SingleTon();
            }
            return singleton;
        }
    }
# 2.双重检查锁定
    public class Singleton(){
        private Singleton(){}
        private static Singleton singleton = null;
        private static  Singleton getInstance(){
            if(singleton == null){
                synchronized(Singleton.class){
                    if(singleton == null){
                        singleton = new SingleTon();
                    }
                }
            }
            return singleton;
        }
    }
# 3.静态内部类  (这种比上面1、2都好一些，既实现了线程安全，又避免了同步带来的性能影响。)
     public class Singleton(){
        private static class LazyHolder(){
            private static final Singleton INSTANCE= new Singleton();
        }

        private Singleton(){}
        private static synchronized Singleton getInstance(){
            return LazyHolder.INSTANCE;
        }
    }
    

#二.饿汉式 （在类创建的同时就已经创建好一个静态的对象供系统使用，以后不再改变，所以天生是线程安全的。）

    public class Singleton2(){
        private Singleton2(){}
        private static final Singleton2 singleton2 = new Singleton2();
        //静态工厂方法
        private static Singleton2 getInstance(){
            return singleton2;
        }
    }

 #三.使用枚举

    public enum Singleton(){
        ONE;
    }

