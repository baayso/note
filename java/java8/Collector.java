/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.util.stream;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 一个<a href="package-summary.html#Reduction">可变的汇聚操作(mutable reduction operation)</a>，
 * 它将输入元素累积到一个可变的结果容器中；在处理完所有输入元素后，将累积的结果转换为一个最终表示（这是一个可选操作）。
 * 汇聚操作（Reduction operations）可以串行执行，也可以并行执行。
 * 
 * <p>可变的汇聚操作的示例包括：
 * 将元素累积到一个{@code Collection}中；使用{@code StringBuilder}连接字符串；
 * 计算关于元素的sum、min、max或average等汇总信息；
 * 计算“数据透视表(pivot table)”汇总信息，如“卖方最大交易金额”等。
 * {@link Collectors}类提供了许多常见的可变汇聚的实现，{@link Collectors}类是一个工厂。
 *
 * 一个{@code Collector}由四个函数指定，这四个函数一起协同工作将条目累积到可变的结果容器中，并可选地对结果执行最终转换。
 * 它们是：<ul>
 *     <li>创建一个新的结果容器({@link #supplier()})</li>
 *     <li>将新的数据元素合并到结果结果容器中({@link #accumulator()})</li>
 *     <li>将两个结果容器合并成一个({@link #combiner()})</li>
 *     <li>对容器执行可选的最终转换({@link #finisher()})</li>
 * </ul>
 *
 * <p>Collectors还具有一组特性，例如{@link Characteristics#CONCURRENT}，
 * 它提供了一些提示，可以由汇聚实现使用这些提示来提供更好的性能。
 *
 * <p>使用Collector进行汇聚的串行实现将使用supplier函数创建单个结果容器，并为每个输入元素调用accumulator函数一次。
 * 并行实现将对输入进行分区，并为每个分区创建一个结果容器，
 * 将每个分区的内容累积到该分区的子结果中，然后使用combiner函数将子结果合并到一个组合结果中。
 *
 * <p>为了确保串行执行和并行执行产生相同的结果，collector函数必须满足<em>同一性(identity)</em>和
 * <a href="package-summary.html#Associativity">结合性(associativity)</a>两个约束。
 *
 * <p>同一性(identity)约束表示对于任何部分累积的结果，将其与空的结果容器合并必须产生等价结果。
 * 也就是说，对于任何系列accumulator和combiner调用所产生的部分累积结果{@code a}，
 * {@code a}必须等价于{@code combiner.apply(a, supplier.get())}。
 *
 * <p>结合性(associativity)约束表示拆分(splitting)计算必须产生等价结果。
 * 也就是说，对于任意输入元素{@code t1}和{@code t2}，下面的计算结果{@code r1}和{@code r2}必须是等价的：
 * <pre>{@code
 *     A a1 = supplier.get();
 *     accumulator.accept(a1, t1);
 *     accumulator.accept(a1, t2);
 *     R r1 = finisher.apply(a1);  // result without splitting
 *
 *     A a2 = supplier.get();
 *     accumulator.accept(a2, t1);
 *     A a3 = supplier.get();
 *     accumulator.accept(a3, t2);
 *     R r2 = finisher.apply(combiner.apply(a2, a3));  // result with splitting
 * } </pre>
 *
 * <p>对于没有{@code UNORDERED}特性的收集器(collectors)，
 * 如果{@code finisher.apply(a1).equals(finisher.apply(a2))}，
 * 则两个累积结果{@code a1}和{@code a2}是等效的。 
 * 对于无序收集器(unordered collectors)，可以放宽等价性，以允许与顺序差异的不相等。
 * （例如，一个无序收集器将元素累积到一个列表中，如果两个列表包含相同的元素，那么它将认为它们是等价的，而忽略了顺序。）
 *
 * <p>基于{@code Collector}实现的reduction的库，比如{@link Stream#collect(Collector)}，必须遵守以下约束:
 * <ul>
 *     <li><传递给accumulator函数的第一个参数、传递给combiner函数的两个参数以及传递给finisher函数的参数
 *     必须是上一次调用result supplier、accumulator或combiner函数的结果。/li>
 *     <li>实现不应该对任何result supplie、accumulator或combiner函数的结果执行任何操作，
 *     除非再次将它们传递给accumulator、combiner或finisher函数，
 *     或者将它们返回给汇聚操作(reduction operation)的调用者。</li>
 *     <li>如果将结果传递给combiner或finisher函数，而该函数却没有返回相同的对象，
 *     则永远不会再次使用该对象。</li>
 *     <li>一旦将一个结果传递给combiner或finisher函数，它就再也不会被传递给accumulator函数。</li>
 *     <li>对于非并发收集器(non-concurrent collectors)，
 *     从result supplier、accumulator或combiner函数返回的任何结果都必须是串行线程受限的。
 *     这使得收集可以并行进行，而{@code Collector}不需要实现任何额外的同步。
 *     reduction实现必须确保输入被正确分区，分区是独立处理的，并且只有在累积(accumulation)完成之后才进行合并。</li>
 *     <li>对于并发收集器(concurrent collectors)，实现可以自由地(但不是必须)并发地实现reduction。
 *     并发reduction是指使用相同的可并发修改的结果容器从多个线程并发地调用accumulator函数，而不是在累积期间将结果进行隔离。
 *     只有当collector具有{@link Characteristics#UNORDERED}特性或者原始数据是无序的时，才应该应用并发reduction。</li>
 * </ul>
 *
 * <p>除了在{@link Collectors}中预定义的实现外，
 * 还可以使用静态工厂方法{@link #of(Supplier, BiConsumer, BinaryOperator, Characteristics...)}构造collectors。
 * 例如，你可以创建一个collector，使用以下命令将widgets累积到{@code TreeSet}中：
 *
 * <pre>{@code
 *     Collector<Widget, ?, TreeSet<Widget>> intoSet =
 *         Collector.of(TreeSet::new, TreeSet::add,
 *                      (left, right) -> { left.addAll(right); return left; });
 * }</pre>
 *
 * （此行为也由预定义的收集器{@link Collectors#toCollection(Supplier)}实现。
 *
 * @apiNote
 * 使用{@code Collector}执行汇聚操作(reduction operation)的结果等价于：
 * <pre>{@code
 *     R container = collector.supplier().get();
 *     for (T t : data)
 *         collector.accumulator().accept(container, t);
 *     return collector.finisher().apply(container);
 * }</pre>
 *
 * <p>然而，库可以自由地对输入进行分区，对每个分区执行reduction，
 * 然后使用combiner函数将部分结果合并起来，实现并行reduction。
 * （这取决于具体的reduction operation，取决于accumulator和combiner功能的相对成本，
 * 它的性能可能更好，也可能更差。）
 *
 * <p>收集器被设计成是可以<em>组合(composed)</em>的；
 * {@link Collectors}中的许多方法都是接受收集器并生成新收集器的函数。
 * 例如，给定如下的收集器，它计算一个员工流的工资总和：
 *
 * <pre>{@code
 *     Collector<Employee, ?, Integer> summingSalaries
 *         = Collectors.summingInt(Employee::getSalary))
 * }</pre>
 *
 * 如果我们想要创建一个收集器来按部门列出工资总额，
 * 我们可以使用{@link Collectors#groupingBy(Function, Collector)}重用上面“工资总和”的逻辑：
 *
 * <pre>{@code
 *     Collector<Employee, ?, Map<Department, Integer>> summingSalariesByDept
 *         = Collectors.groupingBy(Employee::getDepartment, summingSalaries);
 * }</pre>
 *
 * @see Stream#collect(Collector)
 * @see Collectors
 *
 * @param <T> 汇聚操作(reduction operation)的输入元素的类型（流中的每一个元素的类型）
 * @param <A> 汇聚操作(reduction operation)的可变累积类型(通常作为实现细节被隐藏)
 *            （每一次操作的结果容器类型）
 * @param <R> 汇聚操作(reduction operation)的结果类型
 * @since 1.8
 */
public interface Collector<T, A, R> {
    /**
     * 一个函数，它会创建并返回一个新的可变结果容器。
     *
     * @return 返回一个新的可变结果容器的“函数”
     */
    Supplier<A> supplier();

    /**
     * 一个函数，将一个值折叠到可变结果容器数。
     *
     * @return 将值折叠成可变结果容器的函数
     */
    BiConsumer<A, T> accumulator();

    /**
     * 一个函数，接受两个部分结果并将其合并。
     * 组合器函数可以将状态从一个参数折叠到另一个参数并返回该参数，
     * 或者返回一个新的结果容器。
     *
     * @return 将两个部分结果组合成一个组合结果的函数
     */
    BinaryOperator<A> combiner();

    /**
     * 执行从中间累积类型{@code A}转换成最终结果类型{@code R}。
     *
     * <p>如果设置了特征{@code IDENTITY_TRANSFORM}，
     * 则可以假定该函数是一个identity transform，
     * 从{@code A}到{@code R}的转换是未检查的。
     *
     * @return 将中间结果转换为最终结果的函数
     */
    Function<A, R> finisher();

    /**
     * Returns a {@code Set} of {@code Collector.Characteristics} indicating
     * the characteristics of this Collector.  This set should be immutable.
     *
     * @return an immutable set of collector characteristics
     */
    Set<Characteristics> characteristics();

    /**
     * Returns a new {@code Collector} described by the given {@code supplier},
     * {@code accumulator}, and {@code combiner} functions.  The resulting
     * {@code Collector} has the {@code Collector.Characteristics.IDENTITY_FINISH}
     * characteristic.
     *
     * @param supplier The supplier function for the new collector
     * @param accumulator The accumulator function for the new collector
     * @param combiner The combiner function for the new collector
     * @param characteristics The collector characteristics for the new
     *                        collector
     * @param <T> The type of input elements for the new collector
     * @param <R> The type of intermediate accumulation result, and final result,
     *           for the new collector
     * @throws NullPointerException if any argument is null
     * @return the new {@code Collector}
     */
    public static<T, R> Collector<T, R, R> of(Supplier<R> supplier,
                                              BiConsumer<R, T> accumulator,
                                              BinaryOperator<R> combiner,
                                              Characteristics... characteristics) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        Objects.requireNonNull(characteristics);
        Set<Characteristics> cs = (characteristics.length == 0)
                                  ? Collectors.CH_ID
                                  : Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH,
                                                                           characteristics));
        return new Collectors.CollectorImpl<>(supplier, accumulator, combiner, cs);
    }

    /**
     * Returns a new {@code Collector} described by the given {@code supplier},
     * {@code accumulator}, {@code combiner}, and {@code finisher} functions.
     *
     * @param supplier The supplier function for the new collector
     * @param accumulator The accumulator function for the new collector
     * @param combiner The combiner function for the new collector
     * @param finisher The finisher function for the new collector
     * @param characteristics The collector characteristics for the new
     *                        collector
     * @param <T> The type of input elements for the new collector
     * @param <A> The intermediate accumulation type of the new collector
     * @param <R> The final result type of the new collector
     * @throws NullPointerException if any argument is null
     * @return the new {@code Collector}
     */
    public static<T, A, R> Collector<T, A, R> of(Supplier<A> supplier,
                                                 BiConsumer<A, T> accumulator,
                                                 BinaryOperator<A> combiner,
                                                 Function<A, R> finisher,
                                                 Characteristics... characteristics) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        Objects.requireNonNull(finisher);
        Objects.requireNonNull(characteristics);
        Set<Characteristics> cs = Collectors.CH_NOID;
        if (characteristics.length > 0) {
            cs = EnumSet.noneOf(Characteristics.class);
            Collections.addAll(cs, characteristics);
            cs = Collections.unmodifiableSet(cs);
        }
        return new Collectors.CollectorImpl<>(supplier, accumulator, combiner, finisher, cs);
    }

    /**
     * 表示{@code Collector}属性的特征，可用于优化汇聚实现。
     */
    enum Characteristics {
        /**
         * 标识此收集器是<em>并发的</em>，
         * 这意味着结果容器可以支持从多个线程同时使用相同的结果容器调用累加器函数(accumulator function)。
         * (即：多个线程会同时的操作同一个结果容器。)
         *
         * <p>如果 {@code CONCURRENT} 收集器不是 {@code UNORDERED}，
         * 那么只有应用于无序数据源时才应该并发地计算它。
         */
        CONCURRENT,

        /**
         * 标识收集操作不承诺保存输入元素的相遇顺序。
         * (如果结果容器没有内部顺序，例如{@link Set}，则可能是这样。)
         */
        UNORDERED,

        /**
         * 标识完成器函数(finisher function)是identity function并且可以省略。
         * 如果设置了，则从A到R的未检查强制转换必须成功。
         */
        IDENTITY_FINISH
    }
}
