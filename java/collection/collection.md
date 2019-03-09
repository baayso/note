# Collection
  ![Java集合框架结构图](https://github.com/baayso/note/blob/master/java/collection/Java%E9%9B%86%E5%90%88%E6%A1%86%E6%9E%B6%E7%BB%93%E6%9E%84%E5%9B%BE.gif)
* `Set`与`List`的最大区别就是`Set`中的元素不可重复（这个重复指的是`equals()`方法的返回值是否相等）。

## 1. ArrayList
* 当向 ArrayList 添加一个对象时，实际上就是将该对象放置到了 ArrayList 底层所维护的数组当中。
* ArrayList 底层采用数组实现，当使用不带参数的构造方法生成 ArrayList 对象时，实际上会在底层生成一个长度为 10 的 Object 类型数组。
* 如果增加的元素个数超过了 10 个，那么 ArrayList 底层会新生成一个数组，长度为原数组的 1.5 倍+1，然后将原数组的内容复制到新数组当中，并且后续增加的内容都会放到新数组当中。当新数组无法容纳增加的元素时，重复该过程。
* 对于 ArrayList 元素的删除操作，需要将被删除元素的后续元素向前移动，代价比较高。
* 当执行搜索操作时，采用 ArrayList 比较好。

## 2. Vector
* `Vector`是多线程版本的`ArrayList`。
* `Vector`的每个方法前都加上了`synchronized`关键字，同时只会允许一个线程进入方法，以此来保护集合中的数据不被`脏读`和`脏写`。

## 3. LinkedList
* 当向 LinkedList 中添加一个对象时，实际上 LinkedList 内部会生成一个Entry 对象，该 Entry 对象的结构为：
  ```java
  Entry {
      Entry previous;
      Object element;
      Entry next;
  }
  ```
* 其中的 Object 类型的元素 element 就是我们向 LinkedList 中所添加的元素，然后 Entry又构造好了向前与向后的引用 previous、next，最后将生成的这个 Entry 对象加入到了链表当中。换句话说，**LinkedList 中所维护的是一个个的 Entry 对象**。
* 当执行插入或者删除操作时，采用 LinkedList 比较好。

## 4. HashSet
* HashSet 底层是使用 HashMap 实现的。当使用 add 方法将对象添加到 Set 当中时，实际上是将该对象作为底层所维护的 Map 对象的 key，而 value 则都是同一个 Object对象（该对象我们用不上）。

## 5. TreeSet
* 可以自动排序的`Set`，默认排序为升序。它实现了`SortedSet`接口。
* `SortedSet`接口（`TreeSet`实现了该接口）只是定义了在给集合加入元素时将其进行排序，并不能保证元素修改后的排序结果，因此`TreeSet`适用于`不变量`的集合数据排序，比如`String`、`Integer`等类型。
* 解决上述`TreeSet`需要重排序的问题：
  * 重新生成一个`Set`对象，也就是对原有的`Set`对象重排序。
    ```java
    // 不可以使用`TreeSet(SortedSet<E> s)`构造函数，因为该构造函数只是对原`Set`的浅拷贝，如果集合里有相同的元素则不会重新排序。
    SortedSet newSet = new TreeSet<T>(new ArrayList<T>(oldSet));
    ```
  * 使用`List`，也就是使用`Collections.sort()`方法对`List`进行排序。
  * 对于`不变量`的排序，例如直接量（也就是8个基本类型）、`String`类型等，推荐使用`TreeSet`。而对于`可变量`，例如我们自己写的类，可能会在逻辑处理中改变其排序关键值的，则建议使用`List`自行排序。

## 6. EnumSet
* 枚举类型的专用`Set`，所有元素都是枚举类型。

## 7. HashMap
* HashMap 底层维护一个数组，我们向 HashMap 中所放置的对象实际上是存储在该数组当中。
* 当向 HashMap 中 put 一对键值时，它会根据 key 的 hashCode 值计算出一个位置，该位置就是此对象 准备往数组中存放的位置。
* 如果该位置没有对象存在，就将此对象直接放进数组当中；如果该位置已经有对象存在了，则顺着此存在的对象的链开始寻找（Entry 类有一个 Entry 类型的 next 成员变量，指向了该对象的下一个对象），如果此链上有对象的话，再去使用 equals 方法进行比较，如果对此链上的某个对象的 equals 方法比较为 false，则将该对象放到数组当中，然后将数组中该位置以前存在的那个对象链接到此对象的后面。
* HashMap存储结构图：
  ![HashMap存储结构图](https://github.com/baayso/note/blob/master/java/collection/HashMap%E5%AD%98%E5%82%A8%E7%BB%93%E6%9E%84%E5%9B%BE.png)
* 元素查找：使用`hashCode`定位元素，若有哈希冲突，则遍历对比。

## 8. HashTable
* `HashTable`是多线程版本的`HashMap`。
* `HashTable`的每个方法前都加上了`synchronized`关键字，同时只会允许一个线程进入方法，以此来保护集合中的数据不被`脏读`和`脏写`。

## 9. ConcurrentHashMap
* JDK1.5新增，位于`java.util.concurrent`包下。
* 

## 10. 线程安全 和 同步修改异常
* 

## 11. 使用优雅的方式进行集合运算
* 并集，也叫合集
  ```java
  listOne.addAll(listTwo);
  ```
* 交集，计算两个集合的共有元素
  ```java
  listOne.retainAll(listTwo);
  ```
* 差集，由所有属于A但不属于B的元素组成的集合叫做A与B的差集
  ```java
  listOne.removeAll(listTwo);
  ```
* 无重复的并集
  ```java
  // 删除在listOne中出现的元素
  listTwo.removeAll(listOne);
  // 把剩余的listTwo元素加到listOne中
  listOne.addAll(listTwo);
  ```
* 打乱一个列表的顺序
  ```java
  Collections.shuffle(list);
  ```

## 12. 列表遍历方式
* 随机存储 和 有序存储
  * `RandomAccess`接口
* 

