package cc.koosha.konfigurations.core;


public final class DummyV<T> implements KonfigV<T> {

    private final T v;

    public DummyV(final T v) {
        this.v = v;
    }

    @SuppressWarnings("unchecked")
    public static <T> DummyV<T> dummy(final Object v) {

        return new DummyV<>((T) v);
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
    public KonfigV<T> deRegister(final SimpleObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KonfigV<T> register(final SimpleObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KonfigV<T> registerAndCall(final KeyObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KonfigV<T> registerAndCall(final SimpleObserver observer) {
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
