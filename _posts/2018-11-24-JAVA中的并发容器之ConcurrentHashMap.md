---
layout:     post
title:      JAVA中的并发容器之ConcurrentHashMap
subtitle:   java.util.concurrent
date:       2018-11-24
author:     zwilpan
header-img: img/post-bg-debug.png
catalog: true
tags:
    - JAVA并发
---

## 如何保证容器是线程安全？ConcurrentHashMap如何实现高效的线程安全

Java提供了不同层面的线程安全支持.在传统集合框架内部,除了内部HashTable等同步容器，还提供了所谓的同步包装器（synchronized Wrapper）,我们可以调用Collections工具类提供的包装方法,来获取一个同步的包装容器(如Collections.synchronizedMap),但是它们都是非常粗粒度的同步方式,在高并发的情况下效率很低.更加普遍的选择是利用并发包中提供的线程安全容器类,它提供了:  

各种并发容器类：比如ConcurrentHashMap、CopyOnWriteArrayList  

各种线程安全队列Queue/Deque:比如ArrayBlockingQueue、SynchronousQueue  

各种有序容器的线程安全版本  

具体保证线程安全的方式,包括从简单的synchronized的方式,到基于更加精细化的，比如基于分离锁实现的ConcurrentHashMap等并发实现等.具体选择看开发的实际场景,总体来说,并发包内提供的容器通用实现远优于早期的简单同步实现   

早期ConcurrentHashMap,其实现是基于:  
+ 分离锁,也就是将内部进行分段（Segment）,里面则是HashEntry的数组,和HashMap类似,哈希相同的条目以链表形式存放  

+ HashEntry内部使用volatile的value字段保证可见性,也利用了不可变对象的机制以改进利用Unsafe提供的底层能力，比如volatile access,去直接完成部分操作,以最优化性能,毕竟Unsafe中的很多操作都是JVM intrinsic优化过的  

早期ConcurrentHashMap内部结构示意图：  
![avatar](/img/segment.png)  
在构造的时候，Segment的数量是由所谓的concurrentcyLevel决定，默认16,也可以在相应构造函数直接指定,get操作需要保证的是可见性,所以并没有什么同步逻辑：

    public V get(Object key) {
        Segment<K,V> s; // manually integrate access methods to reduce overhead
        HashEntry<K,V>[] tab;
        int h = hash(key.hashCode());
       // 利用位操作替换普通数学运算
       long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
        // 以 Segment 为单位，进行定位
        // 利用 Unsafe 直接进行 volatile access
        if ((s = (Segment<K,V>)UNSAFE.getObjectVolatile(segments, u)) != null &&
            (tab = s.table) != null) {
           // 省略
          }
        return null;
    }

而对于put操作，首先是通过二次哈希冲突避免哈希冲突，然后以Unsafe调用方式，直接获取相应的Segment,然后进行线程安全的put操作  

     public V put(K key, V value) {
        Segment<K,V> s;
        if (value == null)
            throw new NullPointerException();
        // 二次哈希，以保证数据的分散性，避免哈希冲突
        int hash = hash(key.hashCode());
        int j = (hash >>> segmentShift) & segmentMask;
        if ((s = (Segment<K,V>)UNSAFE.getObject          // nonvolatile; recheck
             (segments, (j << SSHIFT) + SBASE)) == null) //  in ensureSegment
            s = ensureSegment(j);
        return s.put(key, hash, value, false);
    }

核心逻辑：

    final V put(K key, int hash, V value, boolean onlyIfAbsent) {
            // scanAndLockForPut 会去查找是否有 key 相同 Node
            // 无论如何，确保获取锁
            HashEntry<K,V> node = tryLock() ? null :
                scanAndLockForPut(key, hash, value);
            V oldValue;
            try {
                HashEntry<K,V>[] tab = table;
                int index = (tab.length - 1) & hash;
                HashEntry<K,V> first = entryAt(tab, index);
                for (HashEntry<K,V> e = first;;) {
                    if (e != null) {
                        K k;
                        // 更新已有 value...
                    }
                    else {
                        // 放置 HashEntry 到特定位置，如果超过阈值，进行 rehash
                        // ...
                    }
                }
            } finally {
                unlock();
            }
            return oldValue;
        }

从源码可以发现，在进行并发操作时：  
+ ConcurrentHashMap会获取再入锁，以保证数据一致性，Segment本身就是基于ReentrantLock扩展实现，所以在并发修改期间，相应的Segment是被锁定的  

+ 在最初的阶段，进行重复性的扫描，以确定相应key值是否已经在数组里面，进而决定是更新还是放置操作，你可以在代码里面看到相应的注释，重复扫描、检测冲突是ConcurrentHashMap的常见技巧  

+ 在concurrentHashMap中扩容不是整体的，而是单独对Segment进行扩容

## java8之后的版本中，ConcurrentHashMap发生了哪些变化?
+ 总体结构上，同样是大的桶（buket）数组，然后内部也是一个个的链表结构，同步的粒度更加细致一些  
+ 内部仍然有Segment定义，但仅仅是为了序列化的兼容性而已，不再有任何结构上的用处  
+ 因为不再使用Segment,初始化操作大大简化，修改为lazy_load形式，这样可以避免开销  
+ 数据存储利用volatile来保证可见性  
+ 使用CAS等操作，在特定场景进行无锁并发操作  
+ 使用Unsafe、LongAdder之类的底层手段，进行极端情况的优化

先看看现在的数据存储内部实现，我们可以发现 Key是final的，因为在生命周期中，一个条目的key变化是不可能的，与此同时val，则声明为volatile，以保证可见性  

     static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        volatile V val;
        volatile Node<K,V> next;
        // … 
    }

put的实现方法：

    final V putVal(K key, V value, boolean onlyIfAbsent) { if (key == null || value == null) throw new NullPointerException();
    int hash = spread(key.hashCode());
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh; K fk; V fv;
        if (tab == null || (n = tab.length) == 0)
            tab = initTable();
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            // 利用 CAS 去进行无锁线程安全操作，如果 bin 是空的
            if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value)))
                break; 
        }
        else if ((fh = f.hash) == MOVED)
            tab = helpTransfer(tab, f);
        else if (onlyIfAbsent // 不加锁，进行检查
                 && fh == hash
                 && ((fk = f.key) == key || (fk != null && key.equals(fk)))
                 && (fv = f.val) != null)
            return fv;
        else {
            V oldVal = null;
            synchronized (f) {
                   // 细粒度的同步修改操作... 
                }
            }
            // Bin 超过阈值，进行树化
            if (binCount != 0) {
                if (binCount >= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    addCount(1L, binCount);
    return null;
    }

初始化操作在initTable中，这是一个典型的CAS场景，利用volatile的sizeCtl最为互斥手段，如果发现竞争性的初始化，就spin在那里，等待条件恢复，否则利用CAS设置排他标志、如果成功则进行初始化，否则重试  

    private final Node<K,V>[] initTable() {
    Node<K,V>[] tab; int sc;
    while ((tab = table) == null || tab.length == 0) {
        // 如果发现冲突，进行 spin 等待
        if ((sc = sizeCtl) < 0)
            Thread.yield(); 
        // CAS 成功返回 true，则进入真正的初始化逻辑
        else if (U.compareAndSetInt(this, SIZECTL, sc, -1)) {
            try {
                if ((tab = table) == null || tab.length == 0) {
                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                    @SuppressWarnings("unchecked")
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    table = tab = nt;
                    sc = n - (n >>> 2);
                }
            } finally {
                sizeCtl = sc;
            }
            break;
        }
    }
    return tab;
    }

在同步逻辑上，它使用的是 synchronized，而不是通常建议的 ReentrantLock 之类，这是为什么呢？现代JDK中，synchronized 已经被不断优化，可以不再担心性能差异，相比于ReentrantLock,它可以减少内存消耗





