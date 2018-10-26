package cs2030.mystream;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface InfiniteList<T> {

    static <T> InfiniteList<T> generate(Supplier<? extends T> supplier) {
        return MyInfiniteList.generate(supplier);
    }

    static <T> InfiniteList<T> iterate(T seed, UnaryOperator<T> next) {
        return MyInfiniteList.iterate(seed, next);
    }

    long count();

    void forEach(Consumer<? super T> action);

    Optional reduce(BinaryOperator<T> accumulator);

    T reduce(T identity, BinaryOperator<T> accumulator);

    Object[] toArray();

    InfiniteList<T> limit(long maxSize);

    InfiniteList<T> filter(Predicate<? super T> predicate);

    <R> InfiniteList<R> map(Function<? super T, ? extends R> mapper);

    InfiniteList<T> takeWhile(Predicate<? super T> predicate);
}
