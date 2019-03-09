# Collection

## 1. ArrayList
* 当向 ArrayList 添加一个对象时，实际上就是将该对象放置到了 ArrayList 底层所维护的数组当中。
* ArrayList 底层采用数组实现，当使用不带参数的构造方法生成 ArrayList 对象时，实际上会在底层生成一个长度为 10 的 Object 类型数组。
* 如果增加的元素个数超过了 10 个，那么 ArrayList 底层会新生成一个数组，长度为原数组的 1.5 倍+1，然后将原数组的内容复制到新数组当中，并且后续增加的内容都会放到新数组当中。当新数组无法容纳增加的元素时，重复该过程。
* 对于 ArrayList 元素的删除操作，需要将被删除元素的后续元素向前移动，代价比较高。
* 当执行搜索操作时，采用 ArrayList 比较好。

## 2. LinkedList
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

## 3. HashSet
* HashSet 底层是使用 HashMap 实现的。当使用 add 方法将对象添加到 Set 当中时，实际上是将该对象作为底层所维护的 Map 对象的 key，而 value 则都是同一个 Object对象（该对象我们用不上）。

## 4. HashMap
* HashMap 底层维护一个数组，我们向 HashMap 中所放置的对象实际上是存储在该数组当中。
* 当向 HashMap 中 put 一对键值时，它会根据 key 的 hashCode 值计算出一个位置，该位置就是此对象 准备往数组中存放的位置。
* 如果该位置没有对象存在，就将此对象直接放进数组当中；如果该位置已经有对象存在了，则顺着此存在的对象的链开始寻找（Entry 类有一个 Entry 类型的 next 成员变量，指向了该对象的下一个对象），如果此链上有对象的话，再去使用 equals 方法进行比较，如果对此链上的某个对象的 equals 方法比较为 false，则将该对象放到数组当中，然后将数组中该位置以前存在的那个对象链接到此对象的后面。
