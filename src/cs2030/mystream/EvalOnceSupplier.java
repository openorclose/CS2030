package cs2030.mystream;

import java.util.function.Supplier;

public class EvalOnceSupplier<T> implements Supplier<T> {
    private T value = null;
    private boolean hasBeenEvaluated = false;
    private Supplier<T> supplier;

    EvalOnceSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }
    @Override
    public T get() {
        if (hasBeenEvaluated) {
            return value;
        }
        hasBeenEvaluated = true;
        return value = supplier.get();
    }
}
