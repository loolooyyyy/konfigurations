package cc.koosha.konfigurations.core;


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

}
