# String

## 1. toString()
* 当打印引用时，实际上会打印出引用所指对象的 `toString()`方法的返回值，因为每个类都直接或间接地继承自`Object`，而`Object`类中定义了`toString()`，因此每个类都有`toString()`这个方法。

## 2. String#equals()
* `equals()`方法，该方法定义在`Object`类当中，因此Java中的每个类都具有该方法，对于`Object`类的`equals()`方法来说，它是判断调用`equals()`方法的引用与传进来的引用是否一致，即这两个引用是否指向的是同一个对象。于 对于`Object`类的`equals()`方法来说，它等价于`==`。
* 对于`String`类的`equals()`方法来说，它是判断当前字符串与传进来的字符串的内容是否一致。
* 对于`String`对象的相等性判断来说，请使用`equals()`方法，而不要使用`==`。

## 3. String 的不变性
* `String`是常量，其对象一旦创建完毕就无法改变。当使用`+`拼接字符串时，会生成新的`String`对象，而不是向原有的`String`对象追加内容。

## 4. `String s = "abc";`（采用字面值方式赋值）
1) 查找 String Pool（字符串池）中是否存在`"abc"`这个对象，如果不存在，则在 String Pool 中创建一个`"abc"`对象，然后将 String Pool 中的这个`"abc"`对象的地址返回来，赋给引用变量`s`，这样`s`会指向 String Pool 中的这个`"abc"`字符串对象。
2) 如果存在，则不创建任何对象，直接将 String Pool 中的这个`"aaa"`对象地址返回来，赋给`s`引用。

## 5. `String s = new String("abc");`
1) 首先在 String Pool（字符串池）中查找有没有`"abc"`这个字符串对象，如果有，则不在 String Pool中再去创建`"abc"`这个对象了，直接在堆中（heap）中创建一个`"abc"`字符串对象，然后将堆中的这个`"abc"`对象的地址返回来，赋给`s`引用，导致`s`指向了堆中创建的这个`"abc"`字符串对象。
2)  如果没有，则首先在 String Pool 中创建一个`"abc"`对象，然后再在堆中（heap）创建一个`"abc"`对象，然后将堆中的这个`"abc"`对象的地址返回来，赋给`s`引用，导致`s`指向了堆中所创建的这个`"abc"`对象。
