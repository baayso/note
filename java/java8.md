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
* ```BiFunction<T, U, R>```接口的```default```方法：
  ```java
  // BiFunction接口中没有compose默认方法的原因是：
  // 1. BiFunction接口中compose默认方法的参数应该是BiFunction类型（before函数）；
  // 2. 首先应用于before函数，得到一个返回值；
  // 3. 然后将before函数的返回值做为当前函数的参数，但是当前函数是需要两个参数的（BiFunction接口表示接收两个参数的的函数），
  //    而before函数的返回值只会是一个，不可能是两个，因为这是Java语法所规定的。

  /**
   * 返回一个组合函数，该函数首先将当前函数应用于其输入，然后将当前函数的结果做为after函数的参数。
   * 如果对任一函数的求值抛出异常，则将其转发给组合函数的调用者。
   *
   * @param <V> after函数的输出类型和组合函数的输出类型
   * @param after 在应用当前函数之后所要应用的函数
   * @return 一个组合函数，首先应用当前函数，然后应用after函数
   * @throws NullPointerException if after is null
   */
   default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
       Objects.requireNonNull(after);
       return (T t, U u) -> after.apply(apply(t, u));
   }
  ```

### 1.4 ```java.util.function.Predicate<T>``` 接口
* 这个函数式接口表示一个参数的predicate（boolean-valued function）。
* ```boolean test(T t);```
  > 根据给定的参数计算this predicate。
* ```Predicate<T>```接口的```default```方法：
  ```java
  // 与 &&
  default Predicate<T> and(Predicate<? super T> other) {
      Objects.requireNonNull(other);
      return (t) -> test(t) && other.test(t);
  }

  // 非（取反） !
  default Predicate<T> negate() {
      return (t) -> !test(t);
  }

  // 或 ||
  default Predicate<T> or(Predicate<? super T> other) {
      Objects.requireNonNull(other);
      return (t) -> test(t) || other.test(t);
  }
  ```
 
### 1.5 ```java.util.function.Supplier<T>``` 接口
* Supplier使用场景：没有输入参数的工厂。
* ```Supplier<T>``` 接口源码：
  ```java
  package java.util.function;

  /**
   * 代表结果的供应者。
   *
   * <p>每次调用Supplier时都不要求返回新的或不同的结果。
   *
   * <p>这是一个函数式接口。其函数式方法是 {@link #get()}.
   *
   * @param <T> the type of results supplied by this supplier
   *
   * @since 1.8
   */
  @FunctionalInterface
  public interface Supplier<T> {

      /**
       * Gets a result.
       *
       * @return a result
       */
      T get();
  }
  ```

### 1.6 ```java.util.function.Consumer<T>``` 接口
* 这个函数式接口表示接受单个输入参数但不返回结果的操作。与大多数其他函数式接口不同，Consumer需要通过```side-effects（函数“附”作用）```进行操作。
* 这是一个函数式接口。其函数式方法是：```void accept(T t);```
* ```java.lang.Iterable<T>```接口中的```forEach()```方法源码：
  ```java
  default void forEach(Consumer<? super T> action) {
      Objects.requireNonNull(action);
      for (T t : this) {
          action.accept(t);
      }
    }
  ```

## 2. ```java.util.Optional<T>``` final类
* 是一个容器对象，可能包含也可能不包含```非null值```。如果值存在则```isPresent()```方法会返回```true```，调用```get()```方法会返回该对象。
* 可以保存类型```T```的值，或者仅仅保存```null```。```Optional```类提供很多有用的方法，这样我们就不用显式进行空值检测。
* 可以很好的解决```NPE(NullPointerException, 空指针异常)```。
* ```Optional```类通常只做为方法的返回值，不要定义```接收Optional类型参数的方法```和```Optional类型的成员变量```。因为```Optional```类并没有实现```Serializable```接口。
* 推荐用法：
  ```java
  Optional<String> optional = Optional.ofNullable(...);
  optional.isPresent(str -> System.out.println(str));
  ```
  ```java
  class User {
      private String name;
      private List<Role> roles;

      // 省略 getter 和 setter
  }

  User user = ...;

  Optional<User> optional = Optional.ofNullable(user);
  Lsit<Roles> roles = optional.map(u -> u.getRoles()).orElse(Collections.emptyList());
  ```
## 3. 方法引用
* 共分为4类：
  1) 类名::静态方法名
  2) 实例名::实例方法名
  3) 类名::实例方法名，第一个Lambda参数做为调用**实例方法**的对象，第二个及后面的Lambda参数做为**实例方法**的参数。
  4) 类名::new（构造方法引用）
