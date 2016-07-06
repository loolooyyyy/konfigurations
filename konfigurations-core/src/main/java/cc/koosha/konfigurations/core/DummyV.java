package cc.koosha.konfigurations.core;


public final class DummyV<T> implements KonfigV<T> {

    private final T v;

    public DummyV(final T v) {
        this.v = v;
    }

    public static <T> DummyV<T> dummy(final T v) {
        return new DummyV<>(v);
    }

    @Override
    public KonfigV<T> deRegister(final KeyObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KonfigV<T> register(final KeyObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String key() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T v() {
        return this.v;
    }

    @Override
    public T v(final T defaultValue) {
        throw new UnsupportedOperationException();
    }

}
