# 

## 1. 关于 Object 类的 equals 方法的特点：
1) 自反性：x.equals(x)应该返回 true
2) 对称性：x.equals(y)为 true，那么 y.equals(x)也为 true。
3) 传递性：x.equals(y)为 true 并且 y.equals(z)为 true，那么 x.equals(z)也应该为 true。
4) 一致性：x.equals(y)的第一次调用为 true，那么 x.equals(y)的第二次、第三次、第 n 次调用也应该为 true，前提条件是在比较之间没有修改 x 也没有修改 y。
5) 对于任何非空引用值 x，x.equals(null) 都应返回 false。 

## 2. 关于 Object 类的 hashCode()方法的特点：
1) 在 Java 应用的一次执行过程当中，对于同一个对象的 hashCode 方法的多次调用，他们应该返回同样的值（前提是该对象的信息没有发生变化）。
2) 对于两个对象来说，如果使用equals方法比较返回true，那么这两个对象的hashCode值一定是相同的。
3) 对于两个对象来说，如果使用equals方法比较返回false，那么这两个对象的hashCode值不要求一定不同（可以相同，可以不同），但是如果不同则可以提高应用的性能。
4) 对于Object类来说，不同的Object对象的hashCode值是不同的（Object类的hashCode值表示的是对象的地址）。

## 3. hashCode在HashSet中的应用
* 当使用 HashSet 时，hashCode()方法就会得到调用，判断已经存储在集合中的对象的hash code 值是否与增加的对象的 hash code 值一致；如果不一致，直接加进去；如果一致，再进行 equals 方法的比较，equals 方法如果返回 true，表示对象已经加进去了，就不会再增加新的对象，否则加进去。

## 4. 如果我们重写 equals 方法，那么也要重写 hashCode 方法，反之亦然
