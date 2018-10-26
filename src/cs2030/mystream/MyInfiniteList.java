package cs2030.mystream;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.*;

public class MyInfiniteList<T> implements InfiniteList<T> {
    private Supplier<T> head = null;
    private Supplier<MyInfiniteList<T>> tail = null;
    private Predicate<? super T> predicate = null;
    private T forcedHead = null;
    private boolean hasHeadBeenForced = false;
    private boolean continueIfHeadFailsPredicate = false;

    static <T> MyInfiniteList<T> generate(Supplier<? extends T> supplier) {
        return new MyInfiniteList<>(supplier::get, () -> MyInfiniteList.generate(supplier));
    }

    static <T> MyInfiniteList<T> iterate(T seed, UnaryOperator<T> next) {
        return new MyInfiniteList<>(() -> seed, () -> MyInfiniteList.iterate(next.apply(seed), next));
    }

    private MyInfiniteList(
        Supplier<T> head,
        Supplier<MyInfiniteList<T>> tail,
        Predicate<? super T> predicate,
        boolean filter
    ) {
        this(head, tail, predicate);
        this.continueIfHeadFailsPredicate = filter;
    }

    private MyInfiniteList(
        Supplier<T> head,
        Supplier<MyInfiniteList<T>> tail,
        Predicate<? super T> predicate
    ) {
        this(head, tail);
        this.predicate = predicate;
    }

    private MyInfiniteList(Supplier<T> head, Supplier<MyInfiniteList<T>> tail) {
        this.head = head;
        this.tail = tail;
    }

    /**
     * Constructor for empty list.
     */
    private MyInfiniteList() {
    }

    /**
     * Generates an empty list.
     * @param <T> T
     * @return An empty list.
     */
    private static <T> MyInfiniteList<T> empty() {
        return new MyInfiniteList<>();
    }

    /**
     * Checks if list is empty.
     * @return True if list is empty, false if not.
     */
    private boolean isEmpty() {
        return getHead() == null;
    }

    /**
     * Get the supplier of the head of the list.
     * @return The supplier of the head of the list.
     */
    private Supplier<T> getHeadSupplier() {
        return head;
    }

    /**
     * Get the supplier of the tail of the list.
     * @return The supplier of the tail of the list.
     */
    private Supplier<MyInfiniteList<T>> getTailSupplier() {
        return tail;
    }

    /**
     * Current list head fails the predicate,
     * but this is a filtered list.
     * So we replace this list with its tail and continue.
     */
    private void replaceCurrentListWithTailList() {
        MyInfiniteList<T> nextTail = getTail();
        head = nextTail.getHeadSupplier();
        tail = nextTail.getTailSupplier();
    }

    /**
     * Gets the head of the list.
     * @return The head of the list
     */
    private T getHead() {
        if (head == null) {
            return null;
        }
        if (hasHeadBeenForced) {
            return forcedHead;
        }
        hasHeadBeenForced = true;
        forcedHead = head.get();

        //only test the predicate if it exists when we need to ask for the head
        if (predicate == null || predicate.test(forcedHead)) {
            return forcedHead;
        }

        //filter function, we want to continue on and test the rest of list
        if (continueIfHeadFailsPredicate) {
            replaceCurrentListWithTailList();
            hasHeadBeenForced = false;
            return getHead();
        }

        //takeWhile function, we want to stop at the first failure
        return forcedHead = null;
    }

    /**
     * Gets the rest of the list.
     * @return The rest of the list.
     */
    private MyInfiniteList<T> getTail() {
        return tail.get();
    }

    /**
     * Gets the length of the list.
     * Forces evaluation.
     * @return The length of the list.
     */
    @Override
    public long count() {
        return isEmpty() ? 0 : 1 + tail.get().count();
    }

    /**
     * Does an action on each item of the list.
     * Forces evaluation.
     * @param action The action to perform.
     */
    @Override
    public void forEach(Consumer<? super T> action) {
        if (isEmpty()) {
            return;
        }
        action.accept(getHead());
        tail.get().forEach(action);
    }

    /**
     * Reduces the list from start to end according to a specified operator.
     * Forces evaluation.
     * Like so:
     * ...accumulator(thirdElement, accumulator(firstElement, secondElement))
     * @param accumulator The operator to reduce with.
     * @return The reduced value.
     */
    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        if (isEmpty()) {
            return Optional.empty();
        }
        T acc = getHead();
        MyInfiniteList<T> curr = getTail();
        if (curr.isEmpty()) {
            return Optional.empty();
        }
        while (!curr.isEmpty()) {
            acc = accumulator.apply(acc, curr.getHead());
            curr = curr.getTail();
        }
        return Optional.of(acc);
    }

    /**
     * Reduces the list with the specified function.
     * Forces evaluation.
     * Like so:
     * ...accumulator(secondElement, accumulator(identity, firstElement))
     * and so on.
     * @param identity The starting value.
     * @param accumulator The function to reduce with.
     * @return The reduced value.
     */
    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        T acc = identity;
        MyInfiniteList<T> curr = this;
        while (!curr.isEmpty()) {
            acc = accumulator.apply(acc, curr.getHead());
            curr = curr.getTail();
        }
        return acc;
    }

    /**
     * Converts the list to an array.
     * Forces evaluation of list.
     * @return The list as an array.
     */
    @Override
    public Object[] toArray() {
        ArrayList<T> list = new ArrayList<>();
        forEach(list::add);
        return list.toArray();
    }

    /**
     * Limits the list to a certain length.
     * Lazy.
     * @param maxSize The length to limit the list with.
     * @return The limited list.
     */
    @Override
    public MyInfiniteList<T> limit(long maxSize) {
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

    /**
     * Filters a list according to a certain predicate.
     * Lazy.
     * @param predicate Predicate to limit list with.
     * @return The filtered list.
     */
    @Override
    public MyInfiniteList<T> filter(Predicate<? super T> predicate) {
        if (head == null) {
            return MyInfiniteList.empty();
        }
        return new MyInfiniteList<>(
            this::getHead,
            () -> getTail().filter(predicate),
            predicate,
            true
        );
    }

    /**
     * Maps each list element, creating a new list.
     * Lazy.
     * @param mapper The mapper to map each element with.
     * @param <R> The return type of the mapper.
     * @return The mapped list.
     */
    @Override
    public <R> MyInfiniteList<R> map(Function<? super T, ? extends R> mapper) {
        if (head == null) {
            return MyInfiniteList.empty();
        }
        return new MyInfiniteList<>(
            () -> isEmpty() ? null : mapper.apply(getHead()),
            () -> getTail().map(mapper)
        );
    }

    /**
     * Creates a new list containing successive elements of the old list
     * until the next element fails the given predicate.
     * @param predicate The predicate to test with.
     * @return The new list.
     */
    @Override
    public MyInfiniteList<T> takeWhile(Predicate<? super T> predicate) {
        if (head == null) {
            return MyInfiniteList.empty();
        }
        return new MyInfiniteList<>(this::getHead, () -> getTail().takeWhile(predicate), predicate);
    }
}