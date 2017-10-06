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
    private final _KonfigObserversManager konfigObserversManager =
            new _KonfigObserversManager();

    @Getter(AccessLevel.PACKAGE)
    private final _KonfigurationCache cache;

    public KonfigurationKombiner(@NonNull final KonfigSource... sources) {

        this(Arrays.asList(sources));
    }

    public KonfigurationKombiner(@NonNull final Collection<KonfigSource> sources) {

        val sources_ = new ArrayList<KonfigSource>(sources);

        if (sources_.isEmpty())
            throw new IllegalArgumentException("no source given");

        this.cache = new _KonfigurationCache(sources_, konfigObserversManager);
    }


    private <T> KonfigV<T> get(@NonNull String key, Class<?> dt, Class<?> el) {

        return cache.create(this, key, dt, el);
    }

    @Override
    public final KonfigV<Boolean> bool(final String key) {

        return get(key, Boolean.class, null);
    }

    @Override
    public final KonfigV<Integer> int_(final String key) {

        return get(key, Integer.class, null);
    }

    @Override
    public final KonfigV<Long> long_(final String key) {

        return get(key, Long.class, null);
    }

    @Override
    public final KonfigV<Double> double_(final String key) {

        return get(key, Double.class, null);
    }

    @Override
    public final KonfigV<String> string(final String key) {

        return get(key, String.class, null);
    }

    @Override
    public final <T> KonfigV<List<T>> list(final String key, final Class<T> type) {

        return get(key, List.class, type);
    }

    @Override
    public final <T> KonfigV<Map<String, T>> map(final String key, final Class<T> type) {

        return get(key, Map.class, type);
    }

    @Override
    public final <T> KonfigV<Set<T>> set(final String key, final Class<T> type) {

        return get(key, Set.class, type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> KonfigV<T> custom(final String key, @NonNull final Class<T> type) {

        if (Boolean.class.equals(type) || boolean.class.equals(type))
            return (KonfigV<T>) this.bool(key);

        if (Integer.class.equals(type) || int.class.equals(type))
            return (KonfigV<T>) this.int_(key);

        if (Long.class.equals(type) || long.class.equals(type))
            return (KonfigV<T>) this.long_(key);

        if (Double.class.equals(type) || double.class.equals(type))
            return (KonfigV<T>) this.double_(key);

        if (String.class.equals(type))
            return (KonfigV<T>) this.string(key);

        if (type.isAssignableFrom(Map.class)
                || type.isAssignableFrom(Set.class)
                || type.isAssignableFrom(List.class))
            throw new KonfigurationException("for collection types, use corresponding methods");

        return get(key, null, type);
    }


    // ________________________________________________________________________

    @Override
    public final Konfiguration subset(final String key) {

        if (key.startsWith("."))
            throw new IllegalArgumentException("key must not start with a dot: " + key);

        return new _KonfigurationSubsetView(this, key);
    }

    @Override
    public final Konfiguration register(final EverythingObserver observer) {

        konfigObserversManager.register(observer);
        return this;
    }

    @Override
    public final Konfiguration deregister(final EverythingObserver observer) {

        konfigObserversManager.deregister(observer);
        return this;
    }

    @Override
    public boolean update() {

        return cache.update();
    }

}
