# Java8新特性
> 在Java8中，接口中可以声明抽象(```abstract```)方法，可以定义默认( ```default```)方法 以及 静态(```static```)方法。

## 0. 接口中的```default```方法和```static```方法
> 接口中的变量都会被```public static final```修饰，成为常量  
> 接口中的方法都会被```public```修饰，如果方法没有方法体则会被```public abstract```修饰，即抽象方法  
* 接口中的```static```方法
  * 不能被子接口继承
  * 不能被实现该接口的类继承
  * 调用形式：```接口名.静态方法名()```
* 接口中的```default```方法
  * 可以被子接口继承
  * 可以被实现该接口的类继承
  * 子接口中如有同名默认方法，父接口中的默认方法会被覆盖
  * 不能通过接口名调用，需要通过接口实现类的实例进行调用
  * 调用形式：```对象名.默认方法名()```
  * 在接口中增加default方法的语法机制是为了兼容已有的接口实现类的代码（不用修改实现类的代码）。
  * 对于在已有的接口中增加一个新的抽象方法，那么势必需要对所有实现了该接口的类进行修改（实现新增加的抽象方法）。
  * 比如```java.util.List<E>```接口中新增加的```default void sort(Comparator<? super E> c) {...}```方法，如果这个方法不是一个default方法，而是一个抽象方法，那么就需要对List接口的所有实现类进行修改以增加对sort()方法的实现。但是如果List接口中增加的sort()方法是一个default方法，它拥有默认的实现，那么List接口的所有实现类将自动继承sort()方法，如此则不需要大规模修改List接口的所有实现类。
* 实现多个接口时，接口中都具有同名的```default```方法
  ```java
  // 编译错误：DefaultMethodDemo inherits unrelated defaults for method() from types InterfaceOne and InterfaceTwo
  // 编译错误：DefaultMethodDemo 从 InterfaceOne 和 InterfaceTwo 类型继承了 method() 的不相关默认值
  public class DefaultMethodDemo implements InterfaceOne, InterfaceTwo {
      public static void main(String[] args) {
          DefaultMethodDemo demo = new DefaultMethodDemo();
          String str = demo.method();
          System.out.println(str);
      }
  }

  interface InterfaceOne {
      default String method() {
          return "hello";
      }
  }

  interface InterfaceTwo {
      default String method() {
          return "world";
      }
  }
  ```
  ```java
  public class DefaultMethodDemo implements InterfaceOne, InterfaceTwo {
      // 必须重写InterfaceOne和InterfaceTwo同名的default方法
      @Override
      public String method() {
          return InterfaceTwo.super.method();
      }

      public static void main(String[] args) {
          DefaultMethodDemo demo = new DefaultMethodDemo();
          String str = demo.method();
          System.out.println(str);
      }
  }

  interface InterfaceOne {
      default String method() {
          return "hello";
      }
  }

  interface InterfaceTwo {
      default String method() {
          return "world";
      }
  }
  ```
* 当多个接口中都具有同名的```default```方法，自定义类继承了接口一实现类并实现了接口二，此时自定义类会继承接口一实现类的方法。因为实现类的优先级高于接口，类中的方法是具体的实现，更贴近需求，接口中的方法是一种契约，一种约定。
  ```java
  public class DefaultMethodDemo extends InterfaceOneImpl implements InterfaceTwo {
      public static void main(String[] args) {
          DefaultMethodDemo demo = new DefaultMethodDemo();
          String str = demo.method();
          System.out.println(str);
      }
  }

  interface InterfaceOne {
      default String method() {
          return "hello";
      }
  }

  interface InterfaceTwo {
      default String method() {
          return "world";
      }
  }

  class InterfaceOneImpl implements InterfaceOne {
      // 如果这里不重写method()方法会导致编译错误
      @Override
      public String method() {
          return "InterfaceOneImpl";
      }
  }
  ```

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
* ```@FunctionalInterface public interface BinaryOperator<T> extends BiFunction<T,T,T>```使用示例代码
  > 表示对两个相同类型的操作数的操作，产生与操作数相同类型的结果。这是```BiFunction<T, U, R>```在操作数和结果都是相同类型的情况下的特殊化。 
  ```java
  public class BinaryOperatorDemo {
      public static void main(String[] args) {
          // world
          System.out.println(BinaryOperatorDemo.getShortString("hello 123", "world", (a, b) -> a.length() - b.length()));
          // hello 123
          System.out.println(BinaryOperatorDemo.getShortString("hello 123", "world", (a, b) -> a.charAt(0) - b.charAt(0)));

          // hello 123
          System.out.println(BinaryOperatorDemo.getLongString("hello 123", "world", (a, b) -> a.length() - b.length()));
          // world
          System.out.println(BinaryOperatorDemo.getLongString("hello 123", "world", (a, b) -> a.charAt(0) - b.charAt(0)));
      }

      public static String getShortString(String a, String b, Comparator<String> comparator) {
          return BinaryOperator.minBy(comparator).apply(a, b);
      }

      public static String getLongString(String a, String b, Comparator<String> comparator) {
          return BinaryOperator.maxBy(comparator).apply(a, b);
      }
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
* 示例代码
  ```java
  public class PredicateTest {
      public static void main(String[] args) {
          List<Integer> numbers = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());
          // 2 4 6 8 10
          PredicateTest.conditionFilter(numbers, num -> num % 2 == 0).forEach(n -> System.out.print(n + " "));
          System.out.println();
          // 6 7 8 9 10
          PredicateTest.conditionFilter(numbers, num -> num > 5).forEach(n -> System.out.print(n + " "));
          System.out.println();
          // 1 2 3 4 5 6 7 8 9 10
          PredicateTest.conditionFilter(numbers, num -> true).forEach(n -> System.out.print(n + " "));
          System.out.println();
          // 2 4
          PredicateTest.conditionFilter(numbers, num -> num % 2 == 0, num -> num <= 5).forEach(n -> System.out.print(n + " "));
          System.out.println();
          // 1 3 5 6 7 8 9 10
          PredicateTest.conditionFilterNegate(numbers, num -> num % 2 == 0, num -> num <= 5).forEach(n -> System.out.print(n + " "));
          System.out.println();
      }

      public static List<Integer> conditionFilter(List<Integer> numbers, Predicate<Integer> predicate) {
          List<Integer> result = new ArrayList<>();
          for (Integer number : numbers) {
              if (predicate.test(number)) {
                  result.add(number);
              }
          }
          return result;
      }

      public static List<Integer> conditionFilter(List<Integer> numbers, Predicate<Integer> predicate, Predicate<Integer> predicate2) {
          List<Integer> result = new ArrayList<>();
          for (Integer number : numbers) {
              if (predicate.and(predicate2).test(number)) {
                  result.add(number);
              }
          }
          return result;
      }

      public static List<Integer> conditionFilterNegate(List<Integer> numbers, Predicate<Integer> predicate, Predicate<Integer> predicate2) {
          List<Integer> result = new ArrayList<>();
          for (Integer number : numbers) {
              if (predicate.and(predicate2).negate().test(number)) {
                  result.add(number);
              }
          }
          return result;
      }
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
* 示例代码
  ```java
  public class SupplierDemo {
      public static void main(String[] args) {
          Supplier<User> supplier = () -> new User("李四", 20);
          System.out.println(supplier.get().getName());

          Supplier<User> supplier2 = User::new;
          System.out.println(supplier2.get().getName());
      }
  }

  class User {
      private String name = "张三";
      private int    age  = 18;

      public User() {
      }

      public User(String name, int age) {
          this.name = name;
          this.age = age;
      }
      
      // getter and setter ...
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
* **```Optional```类通常只做为方法的返回值，不要定义```接收Optional类型参数的方法```和```Optional类型的成员变量```。因为```Optional```类并没有实现```Serializable```接口**。
* 推荐用法：
  ```java
  // String str = null;
  String str = "hello";
  Optional<String> optional = Optional.ofNullable(str);

  optional.ifPresent(v -> System.out.println(v));

  System.out.println(optional.orElse("world"));
  System.out.println(optional.orElseGet(() -> "hello world"));
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
## 3. 方法引用（method reference）
* 当Lambda表达式只有一行代码且这一行代码使用了已经存在的方法，并且这个方法的签名符合函数式方法的签名。则可以使用方法引用替换Lambda表达式。
* 方法引用是Lambda表达式的一种语法糖。
* 可以将方法引用看作是一个【函数指针，function pointer】
* 共分为4类：
  1) 类名::静态方法名
     ```java
     public class MethodReferenceDemo {
         public static void main(String[] args) {
             Student student_1 = new Student("zhangsan", 55);
             Student student_2 = new Student("lisi", 90);
             Student student_3 = new Student("wangwu", 85);
             Student student_4 = new Student("zhaoliu", 70);

             List<Student> studentList = Arrays.asList(student_1, student_2, student_3, student_4);

             // studentList.sort((studentOne, studentTwo) -> Student.compareStudentByScore(studentOne, studentTwo));
             studentList.sort(Student::compareStudentByScore);

             studentList.forEach(System.out::println);
         }
     }

     class Student {
         private String name;
         private int    score;

         public Student(String name, int score) {
             this.name = name;
             this.score = score;
         }

         // getter and setter...

         public static int compareStudentByScore(Student studentOne, Student studentTwo) {
             return studentOne.getScore() - studentTwo.getScore();
         }

         public static int compareStudentByName(Student studentOne, Student studentTwo) {
             return studentOne.getName().compareToIgnoreCase(studentTwo.getName());
         }

        @Override
        public String toString() {
            return "Student{" +
                    "name='" + name + '\'' +
                    ", score=" + score +
                    '}';
        }
     }
     ```
  2) 实例名::实例方法名
     ```java
     StudentComparator studentComparator = new StudentComparator();
     // studentList.sort((studentOne, studentTwo) -> studentComparator.compareStudentByName(studentOne, studentTwo));
     studentList.sort(studentComparator::compareStudentByName);

     class StudentComparator {
         public int compareStudentByScore(Student studentOne, Student studentTwo) {
             return studentOne.getScore() - studentTwo.getScore();
         }

         public int compareStudentByName(Student studentOne, Student studentTwo) {
             return studentOne.getName().compareToIgnoreCase(studentTwo.getName());
         }
     }
     ```
  3) 类名::实例方法名，第一个Lambda参数做为调用**实例方法**的对象，第二个及后面的Lambda参数做为**实例方法**的参数。
     ```java
     List<String> cityList = Arrays.asList("beijing", "nanjing", "chengdu", "guangzhou", "tianjin");

     // Collections.sort(cityList, (cityOne, cityTwo) -> cityOne.compareToIgnoreCase(cityTwo));
     // Collections.sort(cityList, String::compareToIgnoreCase);

     // cityList.sort((cityOne, cityTwo) -> cityOne.compareToIgnoreCase(cityTwo));
     cityList.sort(String::compareToIgnoreCase);

     cityList.forEach(System.out::println);
     ```
     ```java
     // studentList.sort((studentOne, studentTwo) -> studentOne.compareByScore(studentTwo));
     studentList.sort(Student::compareByScore);

     class Student {
         private String name;
         private int    score;

         public Student(String name, int score) {
             this.name = name;
             this.score = score;
         }

        // getter and setter...

         public int compareByScore(Student student) {
             return this.getScore() - student.getScore();
         }

         public int compareByName(Student student) {
             return this.getName().compareToIgnoreCase(student.getName());
         }

         @Override
         public String toString() {
             return "Student{" +
                     "name='" + name + '\'' +
                     ", score=" + score +
                     '}';
         }
     }
     ```
  4) 类名::new（构造方法引用）
     ```java
     public class MethodReferenceDemo {
         public static void main(String[] args) {
             // System.out.println(MethodReferenceDemo.getString(() -> ""));
             // System.out.println(MethodReferenceDemo.getString(() -> new String()));
             System.out.println(MethodReferenceDemo.getString(String::new));

             // System.out.println(MethodReferenceDemo.getString("hello", str -> str));
             // System.out.println(MethodReferenceDemo.getString("hello", str -> new String(str)));
             System.out.println(MethodReferenceDemo.getString("hello", String::new));
         }

         public static String getString(Supplier<String> supplier) {
             return supplier.get() + ";";
         }

         public static String getString(String str, Function<String, String> function) {
             return function.apply(str) + ";";
         }
     }
     ```

## 4. Stream（流）
> 流不存储值，通过管道的方式获取值，对流的操作会生成一个结果，不过并不会修改底层的数据源。
* Stream与集合的区别：
  * 集合关注的是数据与数据的存储
  * 流关注的是对数据的计算
  * 流与迭代器（```java.util.Iterator<E>```）类似的一点是：流不可以重复使用或消费
* Stream操作的分类：
  * 惰性求值
  * 及早求值
* Stream由3部分构成：
  1) 数据源：数组、集合、generator function（Stream.of(...)、IntStream.iterate(...)、Stream.generate(...)）
  2) 零个或多个中间操作（惰性求值、会返回**新的**Stream对象）：map()、filter()、distinct()、skip()、limit()，当没有终止操作时，所有中间操作不会执行
  3) 终止操作（及早求值、不返回Stream对象）：reduce()、forEach()、sum()、min()、max()、summaryStatistics()、findFirst()、collect()，只有当有终止操作时，所有的中间操作才会一并的执行
* [`java.util.stream.Collector<T, A, R>`接口](https://github.com/baayso/note/blob/master/java/java8/Collector.java)



