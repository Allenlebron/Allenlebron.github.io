---
layout:     post
title:      Exception的正确处理方式
subtitle:   
date:       2018-11-16
author:     zwilpan
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:
    - JAVA
---
Exception和Error都继承了Throwable类，在Java中只有Throwable类型的实例才可以被抛出（Throw）或者捕获（Catch）,它是异常处理机制的基本组成类型。

Exception和Error体现了Java平台设计者对不同异常情况的分类。Exception是程序正常运行中，可以预料的意外情况，可能并且应该被捕获，进行相应处理。

Error是指正常情况下，不大可能出现的情况，绝大部分的Error都会导致程序（比如JVM自身）处于非正常的、不可恢复状态。既然是非正常情况，所以不便于也不需要捕获，常见的OutOfMemoryError之类的，都是Error的子类。

Exception又分为checked和unchecked异常，checked在源代码中必须显式的进行捕获处理，这是编译期检查的一部分，前面介绍的不可查Error，是Throwable而不是Exception，unchecked就是运行时异常，
类似NPE,ArrayIndexOutOfBoundsException之类的，通常是可以编码避免的逻辑错误，具体根据需要来判断是否需要捕获，并不会在编译期强制要求
 
第一，理解Throwable,Exception,Error的设计和分类

第二，理解Java语言中操作Throwable的元素和实践，如try..catch..finally块，throw，throws关键字
## 知识扩展：
    try{
	    //业务代码
	    Thread.sleep(1000l)
    }catch(Exception e){
	    //Ignore it
    }   

这段代码虽然很短，但是已经违反了异常处理的两个通用原则：
第一，尽量不要捕获类似Exception这样的通用异常，而是应该捕获特定的异常，在这里是Thread.sleep()抛出的InterruptionedException
第二，不要生吞swallow异常，这是异常处理中要特别注意的事情，很可能导致非常难以诊断的诡异情况 
如果我们不把异常抛出来，或者没有输出日志logger之类的，程序可能在后续代码以不可控的方式结束，没有人能够轻易判断究竟是哪里抛出了异常，以及是什么原因产生了异常
我们从性能角度来审视一下Java的异常处理机制：
1.try-catch代码段会带来额外的开销，它往往会影响到JVM对代码的优化，所以建议仅捕获有必要的代码段，尽量不要一个大的try包住整个代码，与此同时利用异常控制代码流程，也不是一个好主意，远比if-else/switch要低效
2.Java每实例化一个Exception,都会对当时的栈进行快照，这是一个比较重的操作，如果发生频繁，这个开销就不能忽略了

a. 不要推诿或延迟处理异常，就地解决最好，并且需要实实在在的进行处理，而不是只捕捉，不动作。

b. 一个函数尽管抛出了多个异常，但是只有一个异常可被传播到调用端。最后被抛出的异常时唯一被调用端接收的异常，其他异常都会被吞没掩盖。如果调用端要知道造成失败的最初原因，程序之中就绝不能掩盖任何异常。

c.不要在finally代码块中处理返回值。

d.按照我们程序员的惯性认知：当遇到return语句的时候，执行函数会立刻返回。但是，在Java语言中，如果存在finally就会有例外。除了return语句，try代码块中的break或continue语句也可能使控制权进入finally代码块。

e.请勿在try代码块中调用return、break或continue语句。万一无法避免，一定要确保finally的存在不会改变函数的返回值。

f. 函数返回值有两种类型：值类型与对象引用。对于对象引用，要特别小心，如果在finally代码块中对函数返回的对象成员属性进行了修改，即使不在finally块中显式调用return语句，这个修改也会作用于返回值上。

g. 勿将异常用于控制流。
