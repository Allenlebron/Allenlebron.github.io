---
layout:     post
title:     synchronized和ReentrantLock有什么区别
subtitle:   
date:       2018-11-18
author:     zwilpan
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:
    - JAVA并发
---
## synchronized
synchronized是Java内建的同步机制，所以也有人称其为Intrinsic Locking，它提供了互斥的语义和可见性，当一个线程已经获取当前锁时，其他试图获取的线程只能等待或者阻塞在那里。

在Java 5以前，synchronized是仅有的同步手段，在代码中， synchronized可以用来修饰方法，也可以使用在特定的代码块儿上，本质上synchronized方法等同于把方法全部语句用synchronized块包起来。

ReentrantLock，通常翻译为再入锁，是Java 5提供的锁实现，它的语义和synchronized基本相同。再入锁通过代码直接调用lock()方法获取，代码书写也更加灵活。与此同时，ReentrantLock提供了很多实用的方法，能够实现很多synchronized无法做到的细节控制，比如可以控制fairness，也就是公平性，或者利用定义条件等。但是，编码中也需要注意，必须要明确调用unlock()方法释放，不然就会一直持有该锁。

synchronized和ReentrantLock的性能不能一概而论，早期版本synchronized在很多场景下性能相差较大，在后续版本进行了较多改进，在低竞争场景中表现可能优于ReentrantLock。

锁作为并发的基础工具之一，你至少需要掌握：

1.理解什么是线程安全。

2.synchronized、ReentrantLock等机制的基本使用与案例。

3.掌握synchronized、ReentrantLock底层实现；理解锁膨胀、降级；理解偏斜锁、自旋锁、轻量级锁、重量级锁等概念。

4.掌握并发包中java.util.concurrent.lock各种不同实现和案例分析。

线程安全是一个多线程环境下正确性的概念，也就是保证多线程环境下共享的、可修改的状态的正确性，这里的状态反映在程序中其实可以看作是数据。

换个角度来看，如果状态不是共享的，或者不是可修改的，也就不存在线程安全问题，进而可以推理出保证线程安全的两个办法：

a.封装：通过封装，我们可以将对象内部状态隐藏、保护起来。

b.不可变：final和immutable，就是这个道理，Java语言目前还没有真正意义上的原生不可变，但是未来也许会引入。

线程安全需要保证几个基本特性：

原子性，简单说就是相关操作不会中途被其他线程干扰，一般通过同步机制实现。

可见性，是一个线程修改了某个共享变量，其状态能够立即被其他线程知晓，通常被解释为将线程本地状态反映到主内存上，volatile就是负责保证可见性的。

有序性，是保证线程内串行语义，避免指令重排等

## ReentrantLock
再来看看ReentrantLock。你可能好奇什么是再入？它是表示当一个线程试图获取一个它已经获取的锁时，这个获取动作就自动成功，这是对锁获取粒度的一个概念，也就是锁的持有是以线程为单位而不是基于调用次数。Java锁实现强调再入性是为了和pthread的行为进行区分。

再入锁可以设置公平性（fairness），我们可在创建再入锁时选择是否是公平的。 

ReentrantLock fairLock = new ReentrantLock(true);

这里所谓的公平性是指在竞争场景中，当公平性为真时，会倾向于将锁赋予等待时间最久的线程。公平性是减少线程“饥饿”（个别线程长期等待锁，但始终无法获取）情况发生的一个办法

如果使用synchronized，我们根本无法进行公平性的选择，其永远是不公平的，这也是主流操作系统线程调度的选择。通用场景中，公平性未必有想象中的那么重要，Java默认的调度策略很少会导致 “饥饿”发生。与此同时，若要保证公平性则会引入额外开销，自然会导致一定的吞吐量下降。所以，我建议只有当你的程序确实有公平性需要的时候，才有必要指定它

我们再从日常编码的角度学习下再入锁。为保证锁释放，每一个lock()动作，我建议都立即对应一个try-catch-finally，典型的代码结构如下，这是个良好的习惯。

ReentrantLock fairLock = new ReentrantLock(true);

// 这里是演示创建公平锁，一般情况不需要。

fairLock.lock();  
try {  
    // do something  
  } finally  {  
fairLock.unlock();  
}

ReentrantLock相比synchronized，因为可以像普通对象一样使用，所以可以利用其提供的各种便利方法，进行精细的同步操作，甚至是实现synchronized难以表达的用例，如：

带超时的获取锁尝试。

可以判断是否有线程，或者某个特定线程，在排队等待获取锁。

可以响应中断请求。


从性能角度，synchronized早期的实现比较低效，对比ReentrantLock，大多数场景性能都相差较大。但是在Java 6中对其进行了非常多的改进，可以参考性能对比，在高竞争情况下，ReentrantLock仍然有一定优势。我在下一讲进行详细分析，会更有助于理解性能差异产生的内在原因。在大多数情况下，无需纠结于性能，还是考虑代码书写结构的便利性、可维护性等。

在一些内置锁无法满足需求的情况下，ReentrantLock可以作为一种高级工具，当需要一些高级功能时才应该使用ReentrantLock，这些功能包括：可定时的，可轮询的与可中断的锁获取操作，公平队列，以及非块结构的锁，否则，还是应该优先使用synchronized。
