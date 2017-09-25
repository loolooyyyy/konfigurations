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
    private final KonfigObserversManager konfigObserversManager;

    @Getter(AccessLevel.PACKAGE)
    private final KonfigurationCache cache;

    public KonfigurationKombiner(@NonNull final KonfigSource... sources) {

        this(Arrays.asList(sources));
    }

    public KonfigurationKombiner(@NonNull final Collection<KonfigSource> sources) {

        if (sources.isEmpty())
            throw new IllegalArgumentException("no source given");

        this.konfigObserversManager = new KonfigObserversManager();

        this.cache = new KonfigurationCacheSingleImpl(this, sources);
    }


    protected static Object getInSource(final KonfigSource source, final KonfigKey key) {

        final String   name = key.name();
        final Class<?> dt   = key.dt();
        final Class<?> el   = key.el();

        if (dt == Boolean.class)
            return source.bool(name);
        else if (dt == Integer.class)
            return source.int_(name);
        else if (dt == Long.class)
            return source.long_(name);
        else if (dt == Double.class)
            return source.double_(name);
        else if (dt == String.class)
            return source.string(name);
        else if (dt == List.class)
            return source.list(name, el);
        else if (dt == Map.class)
            return source.map(name, el);
        else if (dt == Set.class)
            return source.set(name, el);
        else
            return source.custom(name, el);
    }


    private <T> KonfigV<T> get(@NonNull String key, Class<?> dt, Class<?> el) {

        val                  k   = new KonfigKey(key, dt, el);
        final KonfigVImpl<T> ret = new KonfigVImpl<>(this, k);
        cache.create(k);

        // We can not do this, in order to support default values.
        //        if(!cache.create(k))
        //            throw new KonfigurationMissingKeyException(key);

        return ret;
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

        return new KonfigurationSubsetView(this, key);
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
