package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.KeyObserver;
import cc.koosha.konfiguration.KonfigV;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;


@RequiredArgsConstructor
final class KonfigVImpl<T> implements KonfigV<T> {

    private final KonfigurationKombiner origin;
    private final KonfigKey key;

    @Override
    public T v() {

        return origin.cache().v(this.key, null, true);
    }

    @Override
    public T v(final T defaultValue) {

        return origin.cache().v(this.key, defaultValue, false);
    }

    @Override
    public KonfigV<T> deregister(final KeyObserver observer) {

        origin.konfigObserversManager().deregister(observer, this.key.name());
        return this;
    }

    @Override
    public KonfigV<T> register(@NonNull final KeyObserver observer) {

        origin.konfigObserversManager().register(observer, this.key.name());
        return this;
    }

    @Override
    public String key() {

        return this.key.name();
    }

    @Override
    public String toString() {

        String vs;

        try {
            vs = "<" + this.v().toString() + ">";
        }
        catch (final Exception e) {
            vs = "?";
        }

        return format("KonfigV(%s=%s)", this.key, vs);
    }

    @Override
    public boolean equals(final Object o) {

        if (o == this)
            return true;

        if (!(o instanceof KonfigVImpl))
            return false;

        final KonfigVImpl other = (KonfigVImpl) o;

        return this.key.equals(other.key) && this.origin.equals(other.origin);
    }

    @Override
    public int hashCode() {

        final int PRIME = 59;
        int result = 1;

        result = result * PRIME + this.origin.hashCode();
        result = result * PRIME + this.key.hashCode();

        return result;
    }

}
