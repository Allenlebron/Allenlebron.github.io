---
layout:     post
title:      int、Integer有什么区别
subtitle:   
date:       2018-11-13
author:     zwilpan
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:
    - JAVA
---

# int和Integer有什么区别？谈谈Integer的值缓存范围。

+ int是我们常说的整形数字，是Java的8个原始数据类型（Primitive Types，boolean、byte 、short、char、int、float、double、long）之一。Java语言虽然号称一切都是对象，但原始数据类型是例外。  

+ Integer是int对应的包装类，它有一个int类型的字段存储数据，并且提供了基本操作，比如数学运算、int和字符串之间转换等。在Java 5中，引入了自动装箱和自动拆箱功能（boxing/unboxing），Java可以根据上下文，自动进行转换，极大地简化了相关编程。

## Integer的值缓存
JDK1.5引入了自动装箱与自动拆箱功能，Java可根据上下文，实现int/Integer,double/Double,boolean/Boolean等基本类型与相应对象之间的自动转换，为开发过程带来极大便利。

最常用的是通过new方法构建Integer对象。但是，基于大部分数据操作都是集中在有限的、较小的数值范围，在JDK1.5 中新增了静态工厂方法 valueOf，其背后实现是将int值为-128 到 127 之间的Integer对象进行缓存，在调用时候直接从缓存中获取，进而提升构建对象的性能，也就是说使用该方法后，如果两个对象的int值相同且落在缓存值范围内，那么这个两个对象就是同一个对象；当值较小且频繁使用时，推荐优先使用整型池方法（时间与空间性能俱佳）。

## 注意事项

[1] 基本类型均具有取值范围，在大数*大数的时候，有可能会出现越界的情况。  
[2] 基本类型转换时，使用声明的方式。例：long result= 1234567890 * 24 * 365；结果值一定不会是你所期望的那个值，因为1234567890 * 24已经超过了int的范围，如果修改为：long result= 1234567890L * 24 * 365；就正常了。  
[3] 慎用基本类型处理货币存储。如采用double常会带来差距，常采用BigDecimal、整型（如果要精确表示分，可将值扩大100倍转化为整型）解决该问题。  
[4] 优先使用基本类型。原则上，建议避免无意中的装箱、拆箱行为，尤其是在性能敏感的场合  
[5] 如果有线程安全的计算需要，建议考虑使用类型AtomicInteger、AtomicLong 这样的线程安全类。部分比较宽的基本数据类型，比如 float、double，甚至不能保证更新操作的原子性，可能出现程序读取到只更新了一半数据位的数值。