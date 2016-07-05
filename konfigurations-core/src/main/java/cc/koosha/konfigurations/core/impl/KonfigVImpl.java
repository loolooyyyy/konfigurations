package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.KonfigV;
import lombok.NonNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.String.format;


final class KonfigVImpl<T> implements KonfigV<T> {

    private final BaseKonfiguration source;
    private final KonfigKeyObservers konfigKeyObservers;
    private final String key;
    private final Supplier<T> supplier;

    private T value;

    KonfigVImpl(final BaseKonfiguration source,
                final KonfigKeyObservers konfigKeyObservers,
                final String key,
                final Supplier<T> supplier) {

        this.source = source;
        this.konfigKeyObservers = konfigKeyObservers;
        this.key = key;
        this.supplier = supplier;

        this.update();

        konfigKeyObservers.register(key, k -> this.update());
    }

    void update() {

        this.value = this.supplier.get();
    }

    @Override
    public KonfigV<T> callAndRegister(@NonNull final Consumer<String> observer) {

        this.register(observer);
        observer.accept(this.key);

        return this;
    }

    @Override
    public KonfigV<T> register(@NonNull final Consumer<String> observer) {

        konfigKeyObservers.register(this.key, observer);

        return this;
    }

    @Override
    public String key() {

        return this.key;
    }

    @Override
    public T v() {

        return this.value;
    }


    @Override
    public String toString() {

        return format("Config(%s=%s)", this.key, this.value);
    }

    public boolean equals(final Object o) {

        if (o == this)
            return true;

        if (!(o instanceof KonfigVImpl))
            return false;

        final KonfigVImpl other = (KonfigVImpl) o;

        return this.source.equals(other.source) && this.key.equals(other.key);
    }

    public int hashCode() {

        final int PRIME = 59;
        int result = 1;

        result = result * PRIME + this.source.hashCode();
        result = result * PRIME + this.key.hashCode();

        return result;
    }

}
