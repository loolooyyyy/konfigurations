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
 * @param <U> type of konfig value this object holds.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class DummyV<U> implements K<U> {

    private final String key;
    private final U v;
    private final boolean hasValue;

    private DummyV(final String key, final U v, final boolean hasValue) {
        requireNonNull(key, "key");
        this.key = key;
        this.v = v;
        this.hasValue = hasValue;
    }

    /**
     * For konfiguration value holding no actual value ({@link #v()} always
     * fails).
     *
     * @param key key representing this konfiguration value.
     * @throws NullPointerException if the key is null
     */
    private DummyV(final String key) {
        this(key, null, false);
    }

    /**
     * For konfiguration value holding v as value and key as its key.
     *
     * @param key key representing this konfiguration value.
     * @param v   value this konfiguration holds (can be null).
     * @throws NullPointerException if the key is null
     */
    private DummyV(final String key, final U v) {
        this(key, v, true);
    }


    /**
     * No-op. does nothing.
     */
    @Override
    public K<U> deregister(final KeyObserver observer) {
        // Note that this.v is constant and never changes, but in combination
        // to other sources, it might!
        return this;
    }

    /**
     * No-op. does nothing.
     */
    @Override
    public K<U> register(final KeyObserver observer) {
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
    public U v() {
        if (this.hasValue)
            return this.v;

        throw new KfgMissingKeyException(this.key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public U v(final U defaultValue) {
        return this.hasValue ? this.v : defaultValue;
    }


    // ________________________________________________ PREDEFINED CONST VALUES

    private static final DummyV<?> NULL = new DummyV<>("", null);

    @SuppressWarnings("unchecked")
    public static <U> K<U> null_() {
        return (K<U>) NULL;
    }


    public static <U> K<U> of(final U u) {
        return u == null ? null_() : of(u);
    }

    public static <U> K<U> of(final String key, final U u) {
        requireNonNull(key, "key");
        return new DummyV<>(key, u);
    }


    public static K<Boolean> false_() {
        return of(false);
    }

    public static K<Boolean> true_() {
        return of(true);
    }


    public static K<Integer> mOne() {
        return of(-1);
    }

    public static K<Integer> zero() {
        return of(0);
    }

    public static K<Integer> one() {
        return of(1);
    }


    public static <U> K<Collection<U>> emptyCollection() {
        return of(Collections.emptyList());
    }

    public static <U> K<List<U>> emptyList() {
        return of(Collections.emptyList());
    }

    public static <U, V> K<Map<U, V>> emptyMap() {
        return of(Collections.emptyMap());
    }

    public static <U> K<Set<U>> emptySet() {
        return of(Collections.emptySet());
    }


    public static K<Boolean> bool(final Boolean v) {
        if (v == null)
            return null_();
        return v ? true_() : false_();
    }

    public static K<Character> char_(final Character v) {
        return of(v);
    }

    public static K<String> string(final String v) {
        return of(v);
    }


    public static K<Byte> byte_(final Byte v) {
        return of(v);
    }

    public static K<Short> short_(final Short v) {
        return of(v);
    }

    public static K<Integer> int_(final Integer v) {
        return of(v);
    }

    public static K<Long> long_(final Long v) {
        return of(v);
    }


    public static K<Float> float_(final Float v) {
        return of(v);
    }

    public static K<Double> double_(final Double v) {
        return of(v);
    }


    public static <U> K<List<U>> list(final List<U> v) {
        return of(v);
    }

    public static <U> K<Set<U>> set(final Set<U> s) {
        return of(s);
    }

    public static <U, V> K<Map<U, V>> map(final Map<U, V> v) {
        return of(v);
    }

    public static <U, V> K<Map<U, V>> map(final U k, final V v) {
        final Map<U, V> m = new HashMap<>();
        m.put(k, v);
        return of(Collections.unmodifiableMap(m));
    }

    public static <U, V> K<Map<U, V>> map(final U k0, final V v0,
                                          final U k1, final V v1) {
        final Map<U, V> m = new HashMap<>();
        m.put(k0, v0);
        m.put(k1, v1);
        return of(Collections.unmodifiableMap(m));
    }

    public static <U, V> K<Map<U, V>> map(final U k0, final V v0,
                                          final U k1, final V v1,
                                          final U k2, final V v2) {
        final Map<U, V> m = new HashMap<>();
        m.put(k0, v0);
        m.put(k1, v1);
        m.put(k2, v2);
        return of(Collections.unmodifiableMap(m));
    }

    public static <U, V> K<Map<U, V>> map(final U k0, final V v0,
                                          final U k1, final V v1,
                                          final U k2, final V v2,
                                          final Object... values) {
        requireNonNull(values);

        if (values.length == 0)
            return map(k0, v0, k1, v1, k2, v2);

        if (values.length % 2 != 0)
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

        return of(Collections.unmodifiableMap(m));
    }

}
