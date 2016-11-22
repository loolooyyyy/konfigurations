package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.KeyObserver;
import cc.koosha.konfigurations.core.KonfigV;
import cc.koosha.konfigurations.core.KonfigurationMissingKeyException;
import lombok.NonNull;

import java.util.*;


/**
 * Dummy konfig value, holding a constant konfig value with no source.
 *
 * Good for use in cases where a konfiguration source is not available but a
 * konfiguration value is needed.
 *
 * @param <T> type of konfig value this object holds.
 */
@SuppressWarnings("unused")
public final class DummyV<T> implements KonfigV<T> {

    private final String key;
    private final T v;
    private final boolean hasValue;


    private DummyV(final String key, final T v, final boolean hasValue) {

        this.key = key;
        this.v = v;
        this.hasValue = hasValue;
    }

    /**
     * For konfiguration value holding no actual value ({@link #v()} always fails).
     *
     * @param key key representing this konfiguration value.
     */
    public DummyV(@NonNull final String key) {

        this(key, null, false);
    }

    /**
     * For konfiguration value holding v as value and key as its key.
     *
     * @param key key representing this konfiguration value.
     * @param v value this konfiguration holds (can be null).
     */
    public DummyV(@NonNull final String key, final T v) {

        this(key, v, true);
    }


    /**
     * No-op. does nothing.
     */
    @Override
    public KonfigV<T> deregister(final KeyObserver observer) {

        // this.v is constant and never changes.

        return this;
    }

    /**
     * No-op. does nothing.
     */
    @Override
    public KonfigV<T> register(final KeyObserver observer) {

        // this.v is constant and never changes.

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String key() {

        return this.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T v() {

        if(this.hasValue)
            return this.v;

        throw new KonfigurationMissingKeyException(this.key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T v(final T defaultValue) {

        return this.hasValue ? this.v : defaultValue;
    }


    // ________________________________________________ PREDEFINED CONST VALUES

    public static KonfigV<Boolean> false_() {

        return new DummyV<>("", false);
    }

    public static KonfigV<Boolean> true_() {

        return new DummyV<>("", true);
    }

    public static KonfigV<Integer> int_(final Integer i) {

        return new DummyV<>("", i);
    }

    public static KonfigV<Long> long_(final Long l) {

        return new DummyV<>("", l);
    }

    public static KonfigV<Double> double_(final Double d) {

        return new DummyV<>("", d);
    }

    public static KonfigV<String> string(final String s) {

        return new DummyV<>("", s);
    }

    public static <T> DummyV<Collection<T>> emptyCollection() {

        return new DummyV<Collection<T>>("", Collections.<T>emptyList());
    }

    public static <T> DummyV<List<T>> emptyList() {

        return new DummyV<>("", Collections.<T>emptyList());
    }

    public static <K, V> DummyV<Map<K, V>> emptyMap() {

        return new DummyV<>("", Collections.<K, V>emptyMap());
    }

    public static <T> DummyV<Set<T>> emptySet() {

        return new DummyV<>("", Collections.<T>emptySet());
    }

    public static <T> DummyV<List<T>> list(final List<T> l) {

        return new DummyV<>("", l);
    }

    public static <K, V> DummyV<Map<K, V>> map(final Map<K, V> m) {

        return new DummyV<>("", m);
    }

    public static <T> DummyV<Set<T>> set(final Set<T>  s) {

        return new DummyV<>("", s);
    }

    @SuppressWarnings("unchecked")
    public static KonfigV<?> null_() {

        return new DummyV("", null);
    }

}
