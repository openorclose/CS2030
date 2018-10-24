package cs2030.mystream;

import java.util.Optional;
import java.util.function.*;

public class MyInfiniteList<T> implements InfiniteList<T>{
    private T head;
    private Supplier<InfiniteList<T>> tail;
    private boolean isEmpty = false;

    MyInfiniteList(T head, Supplier<InfiniteList<T>> tail) {
        this.head = head;
        this.tail = tail;
    }

    private MyInfiniteList() {
        isEmpty = true;
    }

    @Override
    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public T getHead() {
        return head;
    }
    @Override
    public long count() {
        return isEmpty() ? 0 : 1 + tail.get().count();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        if (isEmpty()) {
            return;
        }
        action.accept(head);
        tail.get().forEach(action);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        if (isEmpty()) {
            return Optional.empty();
        }
        T acc = head;
        InfiniteList<T> curr = tail.get();
        if (curr.isEmpty()) {
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
        if (maxSize <= 0 || isEmpty) {
            return new MyInfiniteList<>();
        }
        return new MyInfiniteList<>(head, () -> tail.get().limit(maxSize - 1));
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
        if (isEmpty() || !predicate.test(head)) {
            return new MyInfiniteList<>();
        }
        return new MyInfiniteList<>(head, () -> tail.get().takeWhile(predicate));
    }
}
