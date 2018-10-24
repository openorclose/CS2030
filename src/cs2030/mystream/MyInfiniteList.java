package cs2030.mystream;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.*;

public class MyInfiniteList<T> implements InfiniteList<T>{
    private Supplier<T> head = null;
    private Supplier<InfiniteList<T>> tail = null;
    private Predicate<? super T> predicate = null;
    private T forcedHead = null;
    private boolean hasBeenForced = false;
    private Consumer<MyInfiniteList<T>> listProducer = null;
    private MyInfiniteList(Consumer<MyInfiniteList<T>> listProducer) {
        this.listProducer = listProducer;
    }

    private MyInfiniteList(Supplier<T> head, Supplier<InfiniteList<T>> tail, Predicate<? super T> predicate) {
        this(head, tail);
        this.predicate = predicate;
    }

    MyInfiniteList(Supplier<T> head, Supplier<InfiniteList<T>> tail) {
        this.head = head;
        this.tail = tail;
    }

    private MyInfiniteList() {
    }

    private static <T> MyInfiniteList<T> empty() {
        return new MyInfiniteList<>();
    }

    @Override
    public boolean isEmpty() {
        if (listProducer != null) {
            listProducer.accept(this);
        }
        return head == null;
        /*
        if (head == null) {
            return true;
        }
        if (predicate == null) {
            return false;
        }
        return !predicate.test(getHead());*/
    }

    @Override
    public T getHead() {
        if (hasBeenForced) {
            return forcedHead;
        }
        hasBeenForced = true;
        return forcedHead = head.get();
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
        return new MyInfiniteList<>(
                this::getHead,
                maxSize == 1 ?
                        MyInfiniteList::empty :
                        () -> getTail().limit(maxSize - 1)
        );
    }

    public void setHead(Supplier<T> head) {
        this.head = head;
    }

    public void setTail(Supplier<InfiniteList<T>> tail) {
        this.tail = tail;
    }

    @Override
    public InfiniteList<T> filter(Predicate<? super T> predicate) {
        if (isEmpty()) {
            return MyInfiniteList.empty();
        }

        if (predicate.test(getHead())) {
            return new MyInfiniteList<>(this::getHead, () -> getTail().filter(predicate));
        }
        return getTail().filter(predicate);
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
        if (isEmpty()) {
            return MyInfiniteList.empty();
        }
        return new MyInfiniteList<>(this::getHead, () -> getTail().takeWhile(predicate), predicate);
    }
}
