package cc.koosha.konfigurations.core;

import cc.koosha.konfigurations.core.impl.Converter;
import lombok.NonNull;


public final class ValueConverter<T, F> implements KonfigV<T> {

    private T t;
    private final KonfigV<F> wrappedValue;
    private final Converter<F, T> converter;

    public ValueConverter(@NonNull final KonfigV<F> wrappedValue,
                          @NonNull final Converter<F, T> converter) {

        this.wrappedValue = wrappedValue;
        this.converter = converter;
        this.wrappedValue.registerAndCall(new SimpleObserver() {
            @Override
            public void accept() {
                callByWrapped();
            }
        });
    }

    private void callByWrapped() {

        this.t = this.converter.apply(this.wrappedValue.v());
    }

    @Override
    public KonfigV<T> deRegister(final KeyObserver observer) {

        this.wrappedValue.deRegister(observer);
        return this;
    }

    @Override
    public KonfigV<T> register(final KeyObserver observer) {

        this.wrappedValue.register(observer);
        return this;
    }

    @Override
    public KonfigV<T> deRegister(final SimpleObserver observer) {

        this.wrappedValue.deRegister(observer);
        return this;
    }

    @Override
    public KonfigV<T> register(final SimpleObserver observer) {

        this.wrappedValue.register(observer);
        return this;
    }

    @Override
    public KonfigV<T> registerAndCall(final KeyObserver observer) {

        this.wrappedValue.registerAndCall(observer);
        return this;
    }

    @Override
    public KonfigV<T> registerAndCall(final SimpleObserver observer) {

        this.wrappedValue.registerAndCall(observer);
        return this;
    }

    @Override
    public String key() {

        return this.wrappedValue.key();
    }

    @Override
    public T v() {

        // Make sure wrapped has value
        this.wrappedValue.v();
        return this.t;
    }

    @Override
    public T v(final T defaultValue) {

        try {
            // Make sure wrapped has value too.
            this.wrappedValue.v();
            return this.t;
        }
        catch (final KonfigurationMissingKeyException e) {
            return defaultValue;
        }
    }

}
