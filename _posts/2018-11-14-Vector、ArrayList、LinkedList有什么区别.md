---
layout:     post
title:      Vector、ArrayList、LinkedList有什么区别
subtitle:   
date:       2018-11-14
author:     zwilpan
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:
    - JAVA
---


# 对比Vector、ArrayList、LinkedList有何区别？

+ Vector是Java早期提供的线程安全的动态数组，如果不需要线程安全，并不建议选择，毕竟同步是有额外开销的。Vector内部是使用对象数组来保存数据，可以根据需要自动的增加容量，当数组已满时，会创建新的数组，并拷贝原有数组数据。

+ ArrayList是应用更加广泛的动态数组实现，它本身不是线程安全的，所以性能要好很多。与Vector近似，ArrayList也是可以根据需要调整容量，不过两者的调整逻辑有所区别，Vector在扩容时会提高1倍，而ArrayList则是增加50%。

+ LinkedList顾名思义是Java提供的双向链表，所以它不需要像上面两种那样调整容量，它也不是线程安全的。

## 应用场景
+ Vector和ArrayList作为动态数组，其内部元素以数组形式顺序存储的，所以非常适合随机访问的场合。除了尾部插入和删除元素，往往性能会相对较差，比如我们在中间位置插入一个元素，需要移动后续所有元素。

+ LinkedList进行节点插入、删除却要高效得多，但是随机访问性能则要比动态数组慢。

## 总结
Vector、ArrayList、LinkedList均为线型的数据结构，但是从实现方式与应用场景中又存在差别。

1 底层实现方式  
ArrayList内部用数组来实现；LinkedList内部采用双向链表实现；Vector内部用数组实现。

2 读写机制  
ArrayList在执行插入元素是超过当前数组预定义的最大值时，数组需要扩容，扩容过程需要调用底层System.arraycopy()方法进行大量的数组复制操作；在删除元素时并不会减少数组的容量（如果需要缩小数组容量，可以调用trimToSize()方法）；在查找元素时要遍历数组，对于非null的元素采取equals的方式寻找。

LinkedList在插入元素时，须创建一个新的Entry对象，并更新相应元素的前后元素的引用；在查找元素时，需遍历链表；在删除元素时，要遍历链表，找到要删除的元素，然后从链表上将此元素删除即可。
Vector与ArrayList仅在插入元素时容量扩充机制不一致。对于Vector，默认创建一个大小为10的Object数组，并将capacityIncrement设置为0；当插入元素数组大小不够时，如果capacityIncrement大于0，则将Object数组的大小扩大为现有size+capacityIncrement；如果capacityIncrement<=0,则将Object数组的大小扩大为现有大小的2倍。

3 读写效率  

ArrayList对元素的增加和删除都会引起数组的内存分配空间动态发生变化。因此，对其进行插入和删除速度较慢，但检索速度很快。

LinkedList由于基于链表方式存放数据，增加和删除元素的速度较快，但是检索速度较慢。

4 线程安全性  

ArrayList、LinkedList为非线程安全；Vector是基于synchronized实现的线程安全的ArrayList。

需要注意的是：单线程应尽量使用ArrayList，Vector因为同步会有性能损耗；即使在多线程环境下，我们可以利用Collections这个类中为我们提供的synchronizedList(List list)方法返回一个线程安全的同步列表对象。