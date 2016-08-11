package cc.koosha.konfigurations.core;


import java.util.*;


public final class DummyV<T> implements KonfigV<T> {

    private final String key;
    private final T v;

    public DummyV(final String key, final T v) {
        this.key = key;
        this.v = v;
    }

    @SuppressWarnings("unchecked")
    public static <T> DummyV<T> dummy(final String key, final Object v) {

        return new DummyV<>(key, (T) v);
    }

    public DummyV(final T v) {

        this("", v);
    }

    public static <T> DummyV<T> dummy(final Object v) {

        return dummy("", v);
    }


    @Override
    public KonfigV<T> deRegister(final KeyObserver observer) {

        return this;
    }

    @Override
    public KonfigV<T> register(final KeyObserver observer) {

        return this;
    }

    @Override
    public KonfigV<T> deRegister(final SimpleObserver observer) {

        return this;
    }

    @Override
    public KonfigV<T> register(final SimpleObserver observer) {

        return this;
    }

    @Override
    public KonfigV<T> registerAndCall(final KeyObserver observer) {

        observer.accept(this.key());
        return this;
    }

    @Override
    public KonfigV<T> registerAndCall(final SimpleObserver observer) {

        observer.accept();
        return this;
    }

    @Override
    public String key() {

        return this.key;
    }

    @Override
    public T v() {

        return this.v;
    }

    @Override
    public T v(final T defaultValue) {

        return this.v;
    }


    // ________________________________________________ PREDEFINED CONST VALUES

    public static final KonfigV<Boolean> FALSE = new DummyV<>(false);

    public static final KonfigV<Boolean> TRUE = new DummyV<>(true);


    public static final KonfigV<Integer> INT_0 = new DummyV<>(0);

    public static final KonfigV<Integer> INT_MAX = new DummyV<>(Integer.MAX_VALUE);

    public static final KonfigV<Integer> INT_MIN = new DummyV<>(Integer.MIN_VALUE);


    public static final KonfigV<Long> LONG_0 = new DummyV<>(0L);

    public static final KonfigV<Long> LONG_MAX = new DummyV<>(Long.MAX_VALUE);

    public static final KonfigV<Long> LONG_MIN = new DummyV<>(Long.MIN_VALUE);


    public static final KonfigV<Double> DOUBLE_0 = new DummyV<>(0.);

    public static final KonfigV<Double> DOUBLE_MAX = new DummyV<>(Double.MAX_VALUE);

    public static final KonfigV<Double> DOUBLE_MIN = new DummyV<>(Double.MIN_VALUE);

    public static final KonfigV<String> EMPTY_STR = new DummyV<>("");

    public static final KonfigV<?> NULL = new DummyV<>(null);


    public static <T> DummyV<Collection<T>> emptyCollection() {

        return new DummyV<>(Collections.emptyList());
    }

    public static <T> DummyV<List<T>> emptyList() {

        return new DummyV<>(Collections.emptyList());
    }

    public static <K, V> DummyV<Map<K, V>> emptyMap() {

        return new DummyV<>(Collections.emptyMap());
    }

    public static <T> DummyV<Set<T>> emptySet() {

        return new DummyV<>(Collections.emptySet());
    }

}
