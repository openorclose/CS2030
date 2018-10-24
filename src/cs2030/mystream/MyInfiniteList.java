package cs2030.mystream;

import java.util.Optional;
import java.util.function.*;

import static java.util.Optional.of;

public class MyInfiniteList<T> implements InfiniteList<T>{
    private T head;
    private Supplier<InfiniteList<T>> tail;

    public MyInfiniteList(T head, Supplier<InfiniteList<T>> tail) {
        this.head = head;
        this.tail = tail;
    }

    @Override
    public T getHead() {
        return head;
    }
    @Override
    public long count() {
        if (tail == null) {
            return 1;
        }
        return 1 + tail.get().count();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        action.accept(head);
        if (tail != null) {
            tail.get().forEach(action);
        }
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        if (tail == null) {
            return Optional.empty();
        }
        T acc = head;
        InfiniteList<T> curr = tail.get();
        if (curr == null) {
            return Optional.empty();
        }
        while (curr != null) {
            acc = accumulator.apply(acc, curr.getHead());
            curr = tail.get();
        }
        return Optional.of(acc);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public InfiniteList<T> limit(long maxSize) {
        return null;
    }

    @Override
    public InfiniteList<T> filter(Predicate<? super T> predicate) {
        return null;
    }

    @Override
    public <R> InfiniteList<R> map(Function<? super T, ? extends R> mapper) {
        return null;
    }

    @Override
    public InfiniteList<T> takeWhile(Predicate<? super T> predicate) {
        return null;
    }
}
