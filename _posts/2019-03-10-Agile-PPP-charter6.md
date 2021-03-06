---
layout:     post
title:      Agile-PPP-charter6
subtitle:   第6章
date:       2019-03-10
author:     zwilpan
header-img: img/post-bg-debug.png
catalog: true
tags:
    - 读书笔记
---


## 敏捷软件开发读书笔记（第 6 章）

这章主要讲的是关于XP编程的一次实践，其中包括了结对编程，测试驱动开发以及大量的重构。如果说前五章比较偏理论的话，那么这章就是一次实战练习， 测试驱动开发（Test-Driven Development，简称TDD）  

场景是RSK和RSM两个程序员在一家旅馆通过结对编程的方式来完成一个保龄球的计分小程序。在之前没有接触过保龄球这项运动前，最好先要了解一下保龄球的计分规则，不然会看不懂，这就是在开发前一定要熟悉业务。不熟悉业务情况下就开始写程序跟耍流氓没有两样。这个应用程序主要是用来记录保龄球比赛，他们两个人共同控制键盘来完成这个程序的开发，其中一个人作为客户来提出需求，另一个则充当开发。在知道用户素材之后，是需要一个输入投掷输出得分的函数。大概的起草了UML图，是由Game,Frame以及Throw组成，一次Game中有10个Frame,一个Frame中有一到三种Throw，发现Throw没什么行为就去掉了。这种开发模式跟我们实际开发的思路有些不一样，他们是在一边构思程序的设计，一边coding,然后不断地否定原来不合理的地方。我们更多的开始的时候把程序的需求，框架，思路先设定好，然后开始coding，这样的话不至于一直在修改代码，当然作者更多的是表达在设计就是不断地在推翻之前的过程。更多的是通过测试用例来倒逼程序的业务逻辑，以致最后能写出正确的代码。

### 心得体会：

1.最好的设计是在首先编写测试，一小步一小步前进时形成的  
2.在复杂程序时，要尽量学好抽离，降低耦合性，提高代码的可读性  
3.程序开发是一个不断重构的过程，隔段时间就要去清洁一下自己的旧代码