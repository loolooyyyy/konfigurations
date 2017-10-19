package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.K;
import cc.koosha.konfiguration.KeyObserver;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;


/**
 * Thread-safe. Immutable by itself, but it's value is...? should it be?
 * It's better if it was, take care!
 *
 * @param <T> type of the configuration value.
 */
@RequiredArgsConstructor
final class _KonfigVImpl<T> implements K<T> {

    private final KonfigurationKombiner origin;

    private final String name;

    @Getter(AccessLevel.PACKAGE)
    private final Class<?> dt;

    @Getter(AccessLevel.PACKAGE)
    private final Class<?> el;


    @Override
    public T v() {

        return origin.getCache().get(name, null, true);
    }

    @Override
    public T v(final T defaultValue) {

        return origin.getCache().get(name, defaultValue, false);
    }

    @Override
    public K<T> deregister(final KeyObserver observer) {

        origin.getKonfigObserversHolder().deregister(observer, this.name);
        return this;
    }

    @Override
    public K<T> register(@NonNull final KeyObserver observer) {

        origin.getKonfigObserversHolder().register(observer, this.name);
        return this;
    }

    @Override
    public String key() {

        return this.name;
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

        return format("KonfigV(%s=%s)", this.name, vs);
    }

    @Override
    public boolean equals(final Object o) {

        if (o == this)
            return true;

        if (!(o instanceof _KonfigVImpl))
            return false;

        final _KonfigVImpl other = (_KonfigVImpl) o;

        return this.name.equals(other.name) && this.origin.equals(other.origin);
    }

    @Override
    public int hashCode() {

        final int PRIME = 59;
        int result = 1;

        result = result * PRIME + this.origin.hashCode();
        result = result * PRIME + this.name.hashCode();

        return result;
    }

}
