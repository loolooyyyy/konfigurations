package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.util.*;


/**
 * Thread-safe, <b>NOT</b> immutable.
 */
public final class KonfigurationKombiner implements Konfiguration {

    @Getter(AccessLevel.PACKAGE)
    private final _KonfigObserversHolder konfigObserversHolder =
            new _KonfigObserversHolder();

    @Getter(AccessLevel.PACKAGE)
    private final _KonfigurationCache cache;

    public KonfigurationKombiner(@NonNull final KonfigSource... sources) {

        this(Arrays.asList(sources));
    }

    public KonfigurationKombiner(@NonNull final Collection<KonfigSource> sources) {

        val sources_ = new ArrayList<KonfigSource>(sources);

        if (sources_.isEmpty())
            throw new IllegalArgumentException("no source given");

        this.cache = new _KonfigurationCache(sources_, konfigObserversHolder);
    }


    private <T> K<T> get(@NonNull String key, Class<?> dt, Class<?> el) {

        return cache.create(this, key, dt, el);
    }

    @Override
    public final K<Boolean> bool(final String key) {

        return get(key, Boolean.class, null);
    }

    @Override
    public final K<Integer> int_(final String key) {

        return get(key, Integer.class, null);
    }

    @Override
    public final K<Long> long_(final String key) {

        return get(key, Long.class, null);
    }

    @Override
    public final K<Double> double_(final String key) {

        return get(key, Double.class, null);
    }

    @Override
    public final K<String> string(final String key) {

        return get(key, String.class, null);
    }

    @Override
    public final <T> K<List<T>> list(final String key, final Class<T> type) {

        return get(key, List.class, type);
    }

    @Override
    public final <T> K<Map<String, T>> map(final String key, final Class<T> type) {

        return get(key, Map.class, type);
    }

    @Override
    public final <T> K<Set<T>> set(final String key, final Class<T> type) {

        return get(key, Set.class, type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> K<T> custom(final String key, @NonNull final Class<T> type) {

        if (Boolean.class.equals(type) || boolean.class.equals(type))
            return (K<T>) this.bool(key);

        if (Integer.class.equals(type) || int.class.equals(type))
            return (K<T>) this.int_(key);

        if (Long.class.equals(type) || long.class.equals(type))
            return (K<T>) this.long_(key);

        if (Double.class.equals(type) || double.class.equals(type))
            return (K<T>) this.double_(key);

        if (String.class.equals(type))
            return (K<T>) this.string(key);

        if (type.isAssignableFrom(Map.class)
                || type.isAssignableFrom(Set.class)
                || type.isAssignableFrom(List.class))
            throw new KonfigurationException("for collection types, use corresponding methods");

        return get(key, null, type);
    }


    @Override
    public final Konfiguration subset(final String key) {

        if (key.startsWith("."))
            throw new IllegalArgumentException("key must not start with a dot: " + key);

        return new _KonfigurationSubsetView(this, key);
    }


    @Override
    public final Konfiguration register(final EverythingObserver observer) {

        konfigObserversHolder.register(observer);
        return this;
    }

    @Override
    public final Konfiguration deregister(final EverythingObserver observer) {

        konfigObserversHolder.deregister(observer);
        return this;
    }


    @Override
    public boolean update() {

        return cache.update();
    }

}
