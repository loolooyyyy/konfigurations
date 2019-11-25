package io.koosha.konfiguration;


import java.util.*;

import static java.util.Objects.requireNonNull;


/**
 * Dummy konfig value, holding a constant konfig value with no source.
 *
 * <p>Good for use in cases where a konfiguration source is not available but a
 * konfiguration value is needed.
 *
 * <p>Thread-safe and immutable.
 *
 * <p>{@link #deregister(KeyObserver)} and {@link #register(KeyObserver)} do NOT
 * work.
 *
 * <p>Regarding equals and hashcode: Each instance of DummyV is considered to be
 * from a different origin (in contrast to _KonfigVImpl, so only each
 * object is equal to itself only, even with same key and values.
 *
 * @param <T>
 *         type of konfig value this object holds.
 */
@SuppressWarnings({"unused",
                   "WeakerAccess"
                  })
public final class DummyV<T> implements K<T> {

    private final String key;
    private final T v;
    private final boolean hasValue;

    private DummyV(final String key, final T v, final boolean hasValue) {
        requireNonNull(key, "key");
        this.key = key;
        this.v = v;
        this.hasValue = hasValue;
    }

    /**
     * For konfiguration value holding no actual value ({@link #v()} always
     * fails).
     *
     * @param key
     *         key representing this konfiguration value.
     *
     * @throws NullPointerException
     *         if the key is null
     */
    public DummyV(final String key) {
        this(key, null, false);
    }

    /**
     * For konfiguration value holding v as value and key as its key.
     *
     * @param key
     *         key representing this konfiguration value.
     * @param v
     *         value this konfiguration holds (can be null).
     *
     * @throws NullPointerException
     *         if the key is null
     */
    public DummyV(final String key, final T v) {
        this(key, v, true);
    }


    /**
     * No-op. does nothing.
     */
    @Override
    public K<T> deregister(final KeyObserver observer) {
        // Note that this.v is constant and never changes, but in combination
        // to other sources, it might!
        return this;
    }

    /**
     * No-op. does nothing.
     */
    @Override
    public K<T> register(final KeyObserver observer) {
        // Note that this.v is constant and never changes, but in combination
        // to other sources, it might!
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return this.key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T v() {
        if (this.hasValue)
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

    public static <T> K<T> t(final T t) {
        return new DummyV<>("", t);
    }


    public static K<Boolean> false_() {
        return t(false);
    }

    public static K<Boolean> true_() {
        return t(true);
    }


    public static <T> K<Collection<T>> emptyCollection() {
        return t(Collections.emptyList());
    }

    public static <T> K<List<T>> emptyList() {
        return t(Collections.emptyList());
    }

    public static <U, V> K<Map<U, V>> emptyMap() {
        return t(Collections.emptyMap());
    }

    public static <T> K<Set<T>> emptySet() {
        return t(Collections.emptySet());
    }

    public static <T> K<T> null_() {
        return t(null);
    }


    public static K<Boolean> bool(final Boolean b) {
        if (b == null)
            return null_();
        return b ? true_() : false_();
    }

    public static K<Integer> int_(final Integer i) {
        return t(i);
    }

    public static K<Long> long_(final Long l) {
        return t(l);
    }

    public static K<Double> double_(final Double d) {
        return t(d);
    }

    public static K<String> string(final String s) {
        return t(s);
    }

    public static <T> K<List<T>> list(final List<T> l) {
        return t(l);
    }

    public static <U, V> K<Map<U, V>> map(final Map<U, V> m) {
        return t(m);
    }

    public static <U, V> K<Map<U, V>> map(final U k, final V v) {
        final Map<U, V> m = new HashMap<>();
        m.put(k, v);
        return t(Collections.unmodifiableMap(m));
    }

    public static <U, V> K<Map<U, V>> map(final U k0, final V v0,
                                          final U k1, final V v1) {
        final Map<U, V> m = new HashMap<>();
        m.put(k0, v0);
        m.put(k1, v1);
        return t(Collections.unmodifiableMap(m));
    }

    public static <U, V> K<Map<U, V>> map(final U k0, final V v0,
                                          final U k1, final V v1,
                                          final U k2, final V v2) {
        final Map<U, V> m = new HashMap<>();
        m.put(k0, v0);
        m.put(k1, v1);
        m.put(k2, v2);
        return t(Collections.unmodifiableMap(m));
    }

    public static <U, V> K<Map<U, V>> map(final U k0, final V v0,
                                          final U k1, final V v1,
                                          final U k2, final V v2,
                                          final Object... values) {
        requireNonNull(values);

        if (values.length == 0)
            return map(k0, v0, k1, v1, k2, v2);

        if(values.length % 2 != 0)
            throw new IllegalArgumentException("mismatched number of keys and values: " + values.length);

        final Map<U, V> m = new HashMap<>();
        m.put(k0, v0);
        m.put(k1, v1);
        m.put(k2, v2);
        for (int i = 0; i < values.length; i += 2) {
            @SuppressWarnings("unchecked")
            U k = (U) values[i];
            @SuppressWarnings("unchecked")
            V v = (V) values[i + 1];
            m.put(k, v);
        }

        return t(Collections.unmodifiableMap(m));
    }

    public static <T> K<Set<T>> set(final Set<T> s) {
        return t(s);
    }

}
