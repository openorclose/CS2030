package cs2030.mystream;

import java.util.Optional;
import java.util.function.*;

public interface InfiniteList<T> {
    static <T> InfiniteList<T> generate(Supplier<? extends T> supplier) {
        return new MyInfiniteList<>(supplier.get(), () -> InfiniteList.generate(supplier));
    }

    static <T> InfiniteList<T> iterate(T seed, UnaryOperator<T> next) {
        return new MyInfiniteList<>(seed, () -> InfiniteList.iterate(next.apply(seed), next));
    }

    boolean isEmpty();

    T getHead();

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
