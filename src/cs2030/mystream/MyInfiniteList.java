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
    private boolean continueAfterFirstPredicateFailure = false;

    private MyInfiniteList(Supplier<T> head, Supplier<InfiniteList<T>> tail, Predicate<? super T> predicate, boolean filter) {
        this(head, tail, predicate);
        this.continueAfterFirstPredicateFailure = filter;
    }
    private MyInfiniteList(Supplier<T> head, Supplier<InfiniteList<T>> tail, Predicate<? super T> predicate) {
        this(head, tail);
        this.predicate = predicate;
    }

    MyInfiniteList(Supplier<T> head, Supplier<InfiniteList<T>> tail) {
        this.head = new EvalOnceSupplier<>(head);
        this.tail = tail;
    }

    private MyInfiniteList() {
    }

    private static <T> MyInfiniteList<T> empty() {
        return new MyInfiniteList<>();
    }

    @Override
    public boolean isEmpty() {
        return getHead() == null;
        /*
        if (predicate == null) {
            return false;
        }
        return !predicate.test(getHead());
        if (continueAfterFirstPredicateFailure && !passed) {
            return getTail().isEmpty();
        }
        return !passed;*/
    }

    public Supplier<T> getHeadSupplier() {
        return head;
    }

    public Supplier<InfiniteList<T>> getTailSupplier() {
        return tail;
    }

    @Override
    public T getHead() {
        if (head == null) {
            return null;
        }
        if (hasBeenForced) {
            return forcedHead;
        }
        hasBeenForced = true;
        forcedHead = head.get();
        System.out.println(predicate == null);
        if (predicate == null || predicate.test(forcedHead)) {
            return forcedHead;
        }
        if (continueAfterFirstPredicateFailure) {
            head = getTail().getHeadSupplier();
            tail = getTail().getTailSupplier();
            hasBeenForced = false;
            return getHead();
        }
        return null;
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
        if (maxSize <= 0 || head == null) {
            return MyInfiniteList.empty();
        }
        return new MyInfiniteList<>(
                this::getHead,
                maxSize == 1 ?
                        MyInfiniteList::empty :
                        () -> getTail().limit(maxSize - 1)
        );
    }

    @Override
    public InfiniteList<T> filter(Predicate<? super T> predicate) {
        if (head == null) {
            return MyInfiniteList.empty();
        }
        return new MyInfiniteList<>(this::getHead, () -> getTail().filter(predicate), predicate, true);
    }

    @Override
    public <R> InfiniteList<R> map(Function<? super T, ? extends R> mapper) {
        if (head == null) {
            return MyInfiniteList.empty();
        }

        return new MyInfiniteList<>(() -> getHead() == null ? null : mapper.apply(getHead()), () -> getTail().map(mapper));
    }

    @Override
    public InfiniteList<T> takeWhile(Predicate<? super T> predicate) {
        if (head == null) {
            return MyInfiniteList.empty();
        }
        return new MyInfiniteList<>(this::getHead, () -> getTail().takeWhile(predicate), predicate);
    }
}