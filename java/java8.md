# Java8新特性
> 在Java8中，接口中可以声明抽象方法，可以定义 ```default```方法 以及 ```static```方法。

## 1. Lambda
> 高阶函数：如果一个函数接收一个函数作为参数，或者返回一个函数作为返回值，那么该函数就叫做高阶函数。
* Java Lambda表达式是一种匿名函数；它是没有声明的方法，即没有访问修饰符、返回值声明和名字。
* 传递行为，而不仅仅是值。
  * 提升抽象层次
  * API重用性更好
  * 更加灵活
### 1.1 函数式接口
* 如果一个接口只有一个抽象方法，那么该接口就是一个函数式接口。
* 如果我们在某个接口上声明了```FunctionalInterface```注解，那么编译器就会按照函数式接口的定义来要求该接口。
* 如果某个接口只有一个抽象方法，但我们并没有给该接口声明```FunctionalInterface```注解，那么编译器依旧会将该接口看作是函数式接口。
* 如果一个接口声明了一个抽象方法，但是这个抽象方法重写了```java.lang.Object```类的某一个```public```方法，那么这也同样不会增加这个接口的抽象方法数量，因为接口的任意一个实现都直接或者间接的继承```java.lang.Object```类。
* 在将函数作为一等公民的语言中，Lambda表达式的类型是函数。**但在Java中，Lambda表达式是对象**，他们必须依附于一类特别的对象类型——函数式接口(Functional Interface)。
* 函数式接口的实例可以通过Lambda表达式、方法引用、或者构造函数引用来去创建。

### 1.2 ```java.util.function.Function<T, R>``` 接口
* 这个接口表示一个函数，这个函数接收一个参数并且生成一个结果。
* 是一个函数式接口，函数式方法是：```R apply(T t);```
* 泛型参数：
  * T: 函数的输入类型，
  * R: 函数的结果类型。
* ```R apply(T t);```
  > 将这个函数应用到给定的参数上。
* ```Function<T, R>```接口的```default```方法：
  ```java
  /**
   * 返回一个组合函数，该函数首先将before函数应用于其输入，然后将before函数的结果做为当前函数的参数。
   * 如果对任一函数的求值抛出异常，则将其转发给组合函数的调用者。
   *
   * @param <V> before函数和组合函数的输入类型
   * @param before 在应用当前函数之前所要应用的函数
   * @return 一个组合函数，首先应用before函数，然后应用当前函数
   * @throws NullPointerException if before is null
   *
   * @see #andThen(Function)
   */
  default <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
      Objects.requireNonNull(before);
      return (V v) -> apply(before.apply(v));
  }

  /**
   * 返回一个组合函数，该函数首先将当前函数应用于其输入，然后将当前函数的结果做为after函数的参数。
   * 如果对任一函数的求值抛出异常，则将其转发给组合函数的调用者。
   *
   * @param <V> after函数的输出类型和组合函数的输出类型
   * @param after 在应用当前函数之后所要应用的函数
   * @return 一个组合函数，首先应用当前函数，然后应用after函数
   * @throws NullPointerException if after is null
   *
   * @see #compose(Function)
   */
  default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
      Objects.requireNonNull(after);
      return (T t) -> after.apply(apply(t));
  }
  ```

### 1.3 ```java.util.function.BiFunction<T, U, R>``` 接口
* Bi 是 Bidirectional 的缩写。
* 这个接口表示一个函数，这个函数接收两个参数并且生成一个结果。这是```Function<T, R>```接口的两个参数的一种特化形式。
* 是一个函数式接口，函数式方法是：```R apply(T t, U u);```
* 泛型参数：
  * T: 函数的每一个参数类型，
  * U: 函数的每二个参数类型，
  * R: 函数的结果类型。
* ```R apply(T t, U u);```
  > 将这个函数应用到给定的参数上。

### 1.4 ```java.util.function.Consumer<T>``` 接口
* ```void accept(T t);```

### 1.5 ```java.util.function.Predicate<T>``` 接口
* ```boolean test(T t);```
 
### 1.6 ```java.util.function.Supplier<T>``` 接口
* ```T get();```
 
