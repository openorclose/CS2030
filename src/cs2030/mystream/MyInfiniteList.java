package cs2030.mystream;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.*;

public class MyInfiniteList<T> implements InfiniteList<T>{
    private Supplier<T> head;
    private Supplier<InfiniteList<T>> tail;
    private boolean isEmpty = false;

    MyInfiniteList(Supplier<T> head, Supplier<InfiniteList<T>> tail) {
        this.head = head;
        this.tail = tail;
    }

    private MyInfiniteList() {
        isEmpty = true;
    }

    private static <T> MyInfiniteList<T> empty() {
        return new MyInfiniteList<>();
    }
    
    @Override
    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public T getHead() {
        return head.get();
    }

    @Override
    public InfiniteList<T> getTail() {
        return tail.get();
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
        action.accept(getHead());
        tail.get().forEach(action);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        if (isEmpty()) {
            return Optional.empty();
        }
        T acc = getHead();
        InfiniteList<T> curr = getTail();
        if (curr.isEmpty()) {
            return Optional.empty();
        }
        while (!curr.isEmpty()) {
            acc = accumulator.apply(acc, curr.getHead());
            curr = curr.getTail();
        }
        return Optional.of(acc);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        T acc = identity;
        InfiniteList<T> curr = this;
        while (!curr.isEmpty()) {
            acc = accumulator.apply(acc, curr.getHead());
            curr = curr.getTail();
        }
        return acc;
    }

    @Override
    public Object[] toArray() {
        ArrayList<T> list = new ArrayList<>();
        forEach(list::add);
        return list.toArray();
    }

    @Override
    public InfiniteList<T> limit(long maxSize) {
        if (maxSize <= 0 || isEmpty()) {
            return MyInfiniteList.empty();
        }
        return new MyInfiniteList<>(this::getHead, () -> tail.get().limit(maxSize - 1));
    }

    @Override
    public InfiniteList<T> filter(Predicate<? super T> predicate) {
        if (isEmpty()) {
            return MyInfiniteList.empty();
        }
        if (predicate.test(getHead())) {
            return new MyInfiniteList<>(this::getHead, () -> getTail().filter(predicate));
        }
        InfiniteList<T> next = getTail();
        if (next.isEmpty()) {
            return MyInfiniteList.empty();
        }
        return new MyInfiniteList<>(next::getHead, () -> next.getTail().filter(predicate));
    }

    @Override
    public <R> InfiniteList<R> map(Function<? super T, ? extends R> mapper) {
        if (isEmpty()) {
            return MyInfiniteList.empty();
        }
        return new MyInfiniteList<>(() -> mapper.apply(getHead()), () -> getTail().map(mapper));
    }

    @Override
    public InfiniteList<T> takeWhile(Predicate<? super T> predicate) {
        if (isEmpty() || !predicate.test(getHead())) {
            return MyInfiniteList.empty();
        }
        return new MyInfiniteList<>(this::getHead, () -> getTail().takeWhile(predicate));
    }
}
