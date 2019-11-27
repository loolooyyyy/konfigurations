package io.koosha.konfiguration;


import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;


/**
 * Almost Thread-safe, <b>NOT</b> immutable.
 *
 * <p>Clients may safely use any method, BUT the {@link #update()} must be
 * called from a single thread only. Although calling it still does NOT
 * invalidate thread safety of clients.
 *
 * <p>In order to stop clients from calling {@link #update()}, call
 * {@link #readonly()} and obtain a readonly view of the konfiguration.
 *
 * <p>Pitfall: The first call for a key, defines the expected type of that key.
 * For instance calling string("even_though_is_int"), will make
 * KonfigurationKombiner to always return an string typed key, while it actually
 * is an int.
 */
final class KonfigurationKombiner implements Konfiguration {

    private String name;
    private final KonfigurationKombinerHelper kh;

    KonfigurationKombiner(final String name,
                          final Konfiguration k0) {
        this(name, Collections.singletonList(requireNonNull(k0)));
    }

    KonfigurationKombiner(final String name,
                          final Konfiguration k0,
                          final Konfiguration k1) {
        this(name, Arrays.asList(
                requireNonNull(k0),
                requireNonNull(k1)
        ));
    }

    KonfigurationKombiner(final String name,
                          final Konfiguration k0,
                          final Konfiguration k1,
                          final Konfiguration k2) {
        this(name, Arrays.asList(
                requireNonNull(k0),
                requireNonNull(k1),
                requireNonNull(k2)
        ));
    }


    KonfigurationKombiner(final String name,
                          final Konfiguration k0,
                          final Konfiguration k1,
                          final Konfiguration k2,
                          final Konfiguration... sources) {
        this(name, helper(k0, k1, k2, sources));
    }

    KonfigurationKombiner(final String name,
                          final Collection<Konfiguration> sources) {
        this.name = requireNonNull(name);
        final List<Konfiguration> sources_ = new ArrayList<>(requireNonNull(sources));

        if (sources_.isEmpty())
            throw new IllegalArgumentException("no source given");

        for (final Konfiguration source : sources)
            if (source == null)
                throw new IllegalArgumentException("null value in sources");

        this.kh = new KonfigurationKombinerHelper(this, sources_);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Boolean> bool(final String key) {
        return kh.getWrappedValue(key, Boolean.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K<Byte> byte_(String key) {
        return kh.getWrappedValue(key, Byte.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K<Character> char_(String key) {
        return kh.getWrappedValue(key, Character.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K<Short> short_(String key) {
        return kh.getWrappedValue(key, Short.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Integer> int_(final String key) {
        return kh.getWrappedValue(key, Integer.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Long> long_(final String key) {
        return kh.getWrappedValue(key, Long.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K<Float> float_(String key) {
        return kh.getWrappedValue(key, Float.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Double> double_(final String key) {
        return kh.getWrappedValue(key, Double.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<String> string(final String key) {
        return kh.getWrappedValue(key, String.class, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <U> K<List<U>> list(final String key, final Class<U> type) {
        return kh.getWrappedValue(key, List.class, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <U> K<Map<String, T>> map(final String key, final Class<U> type) {
        return kh.getWrappedValue(key, Map.class, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <U> K<Set<U>> set(final String key, final Class<U> type) {
        return kh.getWrappedValue(key, Set.class, type);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <U> K<U> custom(final String key, final Class<U> type) {
        requireNonNull(type);

        if (Boolean.class.equals(type))
            return (K<U>) this.bool(key);

        if (Integer.class.equals(type))
            return (K<U>) this.int_(key);

        if (Long.class.equals(type))
            return (K<U>) this.long_(key);

        if (Double.class.equals(type))
            return (K<U>) this.double_(key);

        if (String.class.equals(type))
            return (K<U>) this.string(key);

        if (type.isAssignableFrom(Map.class) || type.isAssignableFrom(Set.class) || type.isAssignableFrom(List.class))
            throw new KfgException("for collection types, use corresponding methods");

        return kh.getWrappedValue(key, null, type);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final String key) {
        return kh.contains(key);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Konfiguration subset(final String key) {
        requireNonNull(key, "key");
        if (key.startsWith("."))
            throw new IllegalArgumentException("key must not start with a dot: " + key);
        return new KonfigurationSubsetView(this, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Konfiguration readonly() {
        return new KonfigurationSubsetView(this);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final Konfiguration register(final KeyObserver observer) {
        kh.register(observer, "");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Konfiguration deregister(final KeyObserver observer) {
        kh.deregister(observer, "");
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Konfiguration update() {
        kh.update();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasUpdate() {
        return kh.hasUpdate();
    }

    // =========================================================================

    /**
     * Read only subset view of a konfiguration. Prepends a pre-defined key
     * to all konfig values
     *
     * <p>Immutable and thread safe by itself, although the underlying wrapped
     * konfiguration's thread safety is not guarantied.
     */
    private static final class KonfigurationSubsetView implements Konfiguration {

        private final Konfiguration wrapped;
        private final String baseKey;

        KonfigurationSubsetView(final Konfiguration wrapped,
                                final String baseKey) {
            requireNonNull(baseKey, "baseKey");
            this.wrapped = requireNonNull(wrapped, "wrapped");
            this.baseKey = baseKey.endsWith(".") ? baseKey : baseKey + ".";
        }

        KonfigurationSubsetView(final Konfiguration wrapped) {
            this.wrapped = requireNonNull(wrapped, "wrapped");
            this.baseKey = "";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<Boolean> bool(final String key) {
            return wrapped.bool(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<Byte> byte_(String key) {
            return wrapped.byte_(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<Character> char_(String key) {
            return wrapped.char_(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<Short> short_(String key) {
            return wrapped.short_(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<Integer> int_(final String key) {
            return wrapped.int_(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<Long> long_(final String key) {
            return wrapped.long_(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<Float> float_(String key) {
            return wrapped.float_(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<Double> double_(final String key) {
            return wrapped.double_(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K<String> string(final String key) {
            return wrapped.string(baseKey + key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <U> K<List<U>> list(final String key, final Class<U> type) {
            return wrapped.list(baseKey + key, type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <U> K<Map<String, T>> map(final String key, final Class<U> type) {
            return wrapped.map(baseKey + key, type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <U> K<Set<U>> set(final String key, final Class<U> type) {
            return wrapped.set(baseKey + key, type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <U> K<U> custom(final String key, final Class<U> type) {
            return wrapped.custom(baseKey + key, type);
        }

        @Override
        public boolean contains(final String key) {
            return wrapped.contains(baseKey + key);
        }

        @Override
        public String getName() {
            return this.wrapped.getName();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration subset(final String key) {
            requireNonNull(key, "key");
            final String newKey = this.baseKey + "." + key;
            return new KonfigurationSubsetView(this.wrapped, newKey);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration register(final KeyObserver observer) {
            return this.wrapped.register(observer);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration deregister(final KeyObserver observer) {
            return this.wrapped.deregister(observer);
        }

    }

    private static final class KonfigurationKombinerHelper {

        private final Konfiguration origin;
        private final Collection<Konfiguration> sources;

        private final Object UPDATE_LOCK = new Object();
        private final ReadWriteLock VALUES_LOCK = new ReentrantReadWriteLock();
        private final ReadWriteLock OBSERVERS_LOCK = new ReentrantReadWriteLock();

        private final Map<String, Object> valueCache = new HashMap<>();
        private final Map<String, KonfigVImpl> kvalCache = new HashMap<>();

        private final WeakHashMap<KeyObserver, Collection<String>> keyObservers = new WeakHashMap<>();

        KonfigurationKombinerHelper(final Konfiguration origin,
                                    final Collection<Konfiguration> sources) {
            this.origin = requireNonNull(origin, "origin");
            this.sources = requireNonNull(sources, "sources");
        }

        // --------------------

        private Object putValue(final Konfiguration source,
                                final String name,
                                final Class<?> dataType,
                                final Class<?> elementType) {
            final Object result;

            if (dataType == Boolean.class)
                result = source.bool(name);
            else if (dataType == Integer.class)
                result = source.int_(name);
            else if (dataType == Long.class)
                result = source.long_(name);
            else if (dataType == Double.class)
                result = source.double_(name);
            else if (dataType == String.class)
                result = source.string(name);
            else if (dataType == List.class)
                result = source.list(name, elementType);
            else if (dataType == Map.class)
                result = source.map(name, elementType);
            else if (dataType == Set.class)
                result = source.set(name, elementType);
            else
                result = source.custom(name, elementType);

            valueCache.put(name, result);
            return result;
        }

        private <U> U getValue(String name, U def, boolean mustExist) {
            Lock lock = null;
            try {
                lock = VALUES_LOCK.readLock();
                lock.lock();

                if (valueCache.containsKey(name)) {
                    @SuppressWarnings("unchecked")
                    final U get = (U) valueCache.get(name);
                    return get;
                }

                if (mustExist)
                    throw new KfgMissingKeyException(name);

                return def;
            }
            finally {
                if (lock != null)
                    lock.unlock();
            }
        }

        // --------------------

        <U> K<U> getWrappedValue(final String key0,
                                 final Class<?> dataType,
                                 final Class<?> elementType) {
            // We can not do this, in order to support default values.
            //    if(does not exist) throw new KfgMissingKeyException(key);

            Lock readLock = null;
            try {
                readLock = VALUES_LOCK.readLock();
                readLock.lock();
                if (this.kvalCache.containsKey(key0))
                    return this.getWrappedValue0(key0, dataType, elementType);
            }
            finally {
                if (readLock != null)
                    readLock.unlock();
            }

            Lock writeLock = null;
            try {
                writeLock = VALUES_LOCK.writeLock();
                writeLock.lock();

                // Cache was already populated between two locks.
                if (this.kvalCache.containsKey(key0))
                    return this.getWrappedValue0(key0, dataType, elementType);

                for (final Konfiguration source : sources)
                    if (source.contains(key0)) {
                        this.putValue(source, key0, dataType, elementType);
                        break;
                    }

                final KonfigVImpl<U> rr = new KonfigVImpl<>(this, key0, dataType, elementType);
                this.kvalCache.put(key0, rr);
                return rr;
            }
            finally {
                if (writeLock != null)
                    writeLock.unlock();
            }
        }

        private <U> K<U> getWrappedValue0(final String key0,
                                          final Class<?> dataType,
                                          final Class<?> elementType) {
            final KonfigVImpl<?> r = this.kvalCache.get(key0);

            if (r.isSameAs(dataType, elementType)) {
                @SuppressWarnings("unchecked")
                final K<U> cast = (K<U>) r;
                return cast;
            }
            else {
                throw new KfgTypeException(this.origin, key0, null, dataType, null, r);
            }
        }

        // --------------------

        boolean update() {
            synchronized (UPDATE_LOCK) {
                if (!hasUpdate())
                    return false;

                final List<Konfiguration> updatedSources = this.sources
                        .stream()
                        .filter(x -> x != this.origin)
                        .map(x -> x.hasUpdate() ? x.update() : x)
                        .collect(Collectors.toList());

                final Map<String, Object> newCache = new HashMap<>();
                final Set<String> updatedKeys = new HashSet<>();

                final Map<KeyObserver, Collection<String>> observers =
                        update0(updatedSources, newCache, updatedKeys);
                if (observers == null)
                    return false;

                observers.forEach((observer, interestedKeys) -> {
                    Stream<String> s = updatedKeys.stream();
                    if (!interestedKeys.contains(KeyObserver.LISTEN_TO_ALL))
                        s = s.filter(interestedKeys::contains);
                    s.forEach(observer);
                });

                return true;
            }
        }

        boolean hasUpdate() {
            synchronized (UPDATE_LOCK) {
                return this.sources
                        .stream()
                        .filter(x -> x != this.origin)
                        .anyMatch(Konfiguration::hasUpdate);
            }
        }

        /**
         * Thread safe (lock guarded) part of {@link #update()}.
         */
        private Map<KeyObserver, Collection<String>> update0(List<Konfiguration> updatedSources,
                                                             Map<String, Object> newCache,
                                                             Set<String> updatedKeys) {
            Lock vLock = null;
            try {
                vLock = VALUES_LOCK.writeLock();
                vLock.lock();

                // Bad idea, I know. Dead locks?
                Lock oLock = null;
                try {
                    oLock = this.OBSERVERS_LOCK.writeLock();
                    oLock.lock();

                    for (final Map.Entry<String, Object> each : this.valueCache.entrySet()) {
                        final String name = each.getKey();
                        final Object value = each.getValue();
                        boolean found = false;
                        boolean changed = false;
                        for (final Konfiguration source : updatedSources) {
                            if (source.contains(name)) {
                                found = true;
                                final KonfigVImpl kVal = kvalCache.get(name);
                                final Object newVal = this.putValue(source,
                                        name,
                                        kVal.getDataType(),
                                        kVal.getElementType());
                                newCache.put(name, newVal);
                                changed = !newVal.equals(value);
                                break;
                            }
                        }

                        if (!found || changed)
                            updatedKeys.add(name);
                    }

                    if (updatedKeys.isEmpty())
                        return null;

                    valueCache.clear();
                    valueCache.putAll(newCache);

                    this.sources.clear();
                    this.sources.addAll(updatedSources);

                    return new HashMap<>(this.keyObservers);
                }
                finally {
                    if (oLock != null)
                        oLock.unlock();
                }
            }
            finally {
                if (vLock != null)
                    vLock.unlock();
            }
        }

        void deregister(final KeyObserver observer, final String key) {
            Lock lock = null;
            try {
                lock = this.OBSERVERS_LOCK.writeLock();
                lock.lock();

                final Collection<String> keys = this.keyObservers.get(observer);
                if (keys == null)
                    return;
                keys.remove(key);
                if (keys.isEmpty())
                    this.keyObservers.remove(observer);
            }
            finally {
                if (lock != null)
                    lock.unlock();
            }
        }

        void register(final KeyObserver observer, final String key) {
            requireNonNull(observer, "observer");
            requireNonNull(key, "key");

            Lock lock = null;
            try {
                lock = this.OBSERVERS_LOCK.writeLock();
                lock.lock();

                if (!this.keyObservers.containsKey(observer)) {
                    final HashSet<String> put = new HashSet<>(1);
                    put.add(key);
                    this.keyObservers.put(observer, put);
                }
                else {
                    this.keyObservers.get(observer).add(key);
                }
            }
            finally {
                if (lock != null)
                    lock.unlock();
            }
        }

        boolean contains(final String key) {
            Lock readLock = null;
            try {
                readLock = VALUES_LOCK.readLock();
                readLock.lock();
                if (this.kvalCache.containsKey(key))
                    return true;
            }
            finally {
                if (readLock != null)
                    readLock.unlock();
            }
            return false;
        }

    }

    private static final class KonfigVImpl<U> implements K<U> {

        private final KonfigurationKombinerHelper origin;
        private final String key;
        private final Class<?> dataType;
        private final Class<?> elementType;

        private KonfigVImpl(KonfigurationKombinerHelper origin, String key, Class<?> dataType, Class<?> elementType) {
            this.origin = origin;
            this.key = key;
            this.dataType = dataType;
            this.elementType = elementType;
        }


        boolean isSameAs(Class<?> dataType, Class<?> elementType) {
            return Objects.equals(this.getDataType(), dataType)
                    && Objects.equals(this.getElementType(), elementType);
        }

        Class<?> getDataType() {
            return this.dataType;
        }

        Class<?> getElementType() {
            return this.elementType;
        }


        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public U v() {
            return this.origin.getValue(key, null, true);
        }

        @Override
        public U v(final U defaultValue) {
            return this.origin.getValue(key, defaultValue, false);
        }

        @Override
        public K<U> deregister(final KeyObserver observer) {
            this.origin.deregister(observer, this.key);
            return this;
        }

        @Override
        public K<U> register(final KeyObserver observer) {
            requireNonNull(observer, "observer");
            this.origin.register(observer, this.key);
            return this;
        }

        @Override
        public String toString() {
            try {
                return format("KonfigV(%s=%s)", this.key, this.v().toString());
            }
            catch (final Exception e) {
                return format("KonfigV(%s=?)", this.key);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            KonfigVImpl<?> konfigV = (KonfigVImpl<?>) o;
            return Objects.equals(origin, konfigV.origin) && Objects.equals(key, konfigV.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(origin, key);
        }

    }


    private static Collection<Konfiguration> helper(
            final Konfiguration k0,
            final Konfiguration k1,
            final Konfiguration k2,
            final Konfiguration... sources) {
        final List<Konfiguration> all = new ArrayList<>();
        Collections.addAll(all, k0, k1, k2);
        Collections.addAll(all, sources);
        return all;
    }

}
