package io.koosha.konfiguration;


import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    private final KonfigurationKombinerHelper kh;

    KonfigurationKombiner(final KonfigSource k0) {
        this(Collections.singletonList(requireNonNull(k0)));
    }

    KonfigurationKombiner(final KonfigSource k0,
                          final KonfigSource k1) {
        this(Arrays.asList(
                requireNonNull(k0),
                requireNonNull(k1)
        ));
    }

    KonfigurationKombiner(final KonfigSource k0,
                          final KonfigSource k1,
                          final KonfigSource k2) {
        this(Arrays.asList(
                requireNonNull(k0),
                requireNonNull(k1),
                requireNonNull(k2)
        ));
    }


    KonfigurationKombiner(final KonfigSource k0,
                          final KonfigSource k1,
                          final KonfigSource k2,
                          final KonfigSource... sources) {
        this(helper(k0, k1, k2, sources));
    }

    KonfigurationKombiner(final Collection<KonfigSource> sources) {
        final List<KonfigSource> sources_ = new ArrayList<>(requireNonNull(sources));

        if (sources_.isEmpty())
            throw new IllegalArgumentException("no source given");

        for (final KonfigSource source : sources)
            if (source == null)
                throw new IllegalArgumentException("null value in sources");

        this.kh = new KonfigurationKombinerHelper(sources_);
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
    public final <T> K<List<T>> list(final String key, final Class<T> type) {
        return kh.getWrappedValue(key, List.class, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> K<Map<String, T>> map(final String key, final Class<T> type) {
        return kh.getWrappedValue(key, Map.class, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> K<Set<T>> set(final String key, final Class<T> type) {
        return kh.getWrappedValue(key, Set.class, type);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <T> K<T> custom(final String key, final Class<T> type) {
        requireNonNull(type);

        if (Boolean.class.equals(type))
            return (K<T>) this.bool(key);

        if (Integer.class.equals(type))
            return (K<T>) this.int_(key);

        if (Long.class.equals(type))
            return (K<T>) this.long_(key);

        if (Double.class.equals(type))
            return (K<T>) this.double_(key);

        if (String.class.equals(type))
            return (K<T>) this.string(key);

        if (type.isAssignableFrom(Map.class) || type.isAssignableFrom(Set.class) || type.isAssignableFrom(List.class))
            throw new KonfigurationTypeException("for collection types, use corresponding methods");

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
    public boolean isReadonly() {
        return false;
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
    public boolean update() {
        return kh.update();
    }


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

        KonfigurationSubsetView(final Konfiguration wrapped, final String baseKey) {
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
        public <T> K<List<T>> list(final String key, final Class<T> type) {
            return wrapped.list(baseKey + key, type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> K<Map<String, T>> map(final String key, final Class<T> type) {
            return wrapped.map(baseKey + key, type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> K<Set<T>> set(final String key, final Class<T> type) {
            return wrapped.set(baseKey + key, type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> K<T> custom(final String key, final Class<T> type) {
            return wrapped.custom(baseKey + key, type);
        }

        @Override
        public boolean contains(final String key) {
            return wrapped.contains(baseKey + key);
        }


        /**
         * Does not return false or true, as clients might wait for this method
         * it in a while loop and wait for it to return true, indicating a
         * change being applied. Simply fails fast.
         *
         * @throws KonfigurationException
         *         always.
         */
        @Override
        public boolean update() {
            throw new UnsupportedOperationException("update is not available from read-only view");
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
        public Konfiguration readonly() {
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isReadonly() {
            return true;
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

        private final Collection<KonfigSource> sources;

        private final Object UPDATE_LOCK = new Object();
        private final ReadWriteLock VALUES_LOCK = new ReentrantReadWriteLock();
        private final ReadWriteLock OBSERVERS_LOCK = new ReentrantReadWriteLock();

        private final Map<String, Object> valueCache = new HashMap<>();
        private final Map<String, KonfigVImpl> kvalCache = new HashMap<>();

        private final WeakHashMap<KeyObserver, Collection<String>> keyObservers = new WeakHashMap<>();

        KonfigurationKombinerHelper(Collection<KonfigSource> sources) {
            this.sources = sources;
        }

        // --------------------

        private Object putValue(final KonfigSource source,
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

        private <T> T getValue(String name, T def, boolean mustExist) {
            Lock lock = null;
            try {
                lock = VALUES_LOCK.readLock();
                lock.lock();

                if (valueCache.containsKey(name)) {
                    @SuppressWarnings("unchecked") final T get = (T) valueCache.get(name);
                    return get;
                }

                if (mustExist)
                    throw new KonfigurationMissingKeyException(name);

                return def;
            }
            finally {
                if (lock != null)
                    lock.unlock();
            }
        }

        // --------------------

        <T> K<T> getWrappedValue(String name, Class<?> dataType, Class<?> elementType) {

            // We can not do this, in order to support default values.
            //    if(does not exist) throw new KonfigurationMissingKeyException(key);

            Lock readLock = null;
            try {
                readLock = VALUES_LOCK.readLock();
                readLock.lock();
                if (this.kvalCache.containsKey(name))
                    return this.getWrappedValue0(name, dataType, elementType);
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
                if (this.kvalCache.containsKey(name))
                    return this.getWrappedValue0(name, dataType, elementType);

                for (final KonfigSource source : sources)
                    if (source.contains(name)) {
                        this.putValue(source, name, dataType, elementType);
                        break;
                    }

                final KonfigVImpl<T> rr = new KonfigVImpl<>(this, name, dataType, elementType);
                this.kvalCache.put(name, rr);
                return rr;
            }
            finally {
                if (writeLock != null)
                    writeLock.unlock();
            }
        }

        private <T> K<T> getWrappedValue0(String name, Class<?> dataType, Class<?> elementType) {

            final KonfigVImpl<?> r = this.kvalCache.get(name);

            if (r.isSameAs(dataType, elementType)) {
                @SuppressWarnings("unchecked") final K<T> cast = (K<T>) r;
                return cast;
            } else {
                throw new KonfigurationTypeException(TypeName.typeName(dataType, elementType),
                                                     TypeName.typeName(r.getDataType(), r.getElementType()),
                                                     name);
            }
        }

        // --------------------


        boolean update() {
            synchronized (UPDATE_LOCK) {
                boolean isUpdatable = false;
                for (final KonfigSource source : this.sources)
                    if (source.isUpdatable()) {
                        isUpdatable = true;
                        break;
                    }
                if (!isUpdatable)
                    return false;

                final List<KonfigSource> updatedSources = new ArrayList<>(this.sources.size());
                for (final KonfigSource source : this.sources)
                    updatedSources.add(source.isUpdatable()
                                       ? source.copyAndUpdate()
                                       : source);

                final Map<String, Object> newCache = new HashMap<>();
                final Set<String> updatedKeys = new HashSet<>();

                final Map<KeyObserver, Collection<String>> observers = update0(updatedSources, newCache, updatedKeys);
                if (observers == null)
                    return false;

                for (final Map.Entry<KeyObserver, Collection<String>> o : observers.entrySet()) {
                    // Never empty or null.
                    final Collection<String> interestedKeys = o.getValue();
                    final KeyObserver observer = o.getKey();
                    for (final String updatedKey : updatedKeys)
                        if (interestedKeys.contains(updatedKey))
                            observer.accept(updatedKey);
                    if (interestedKeys.contains(""))
                        observer.accept("");
                }

                return true;
            }
        }

        /**
         * Thread safe (lock guarded) part of {@link #update()}.
         */
        private Map<KeyObserver, Collection<String>> update0(List<KonfigSource> updatedSources,
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
                        for (final KonfigSource source : updatedSources) {
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

        // --------------------

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
                } else {
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


        // --------------------


        private static final class KonfigVImpl<T> implements K<T> {

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
                return Objects.equals(this.getDataType(), dataType) && Objects.equals(this.getElementType(), elementType);
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
            public T v() {
                return this.origin.getValue(key, null, true);
            }

            @Override
            public T v(final T defaultValue) {
                return this.origin.getValue(key, defaultValue, false);
            }

            @Override
            public K<T> deregister(final KeyObserver observer) {
                this.origin.deregister(observer, this.key);
                return this;
            }

            @Override
            public K<T> register(final KeyObserver observer) {
                requireNonNull(observer, "observer");
                this.origin.register(observer, this.key);
                return this;
            }

            @Override
            public String toString() {
                try {
                    return String.format("KonfigV(%s=%s)", this.key, this.v().toString());
                }
                catch (final Exception e) {
                    return String.format("KonfigV(%s=?)", this.key);
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

    }

    private static Collection<KonfigSource> helper(
            final KonfigSource k0,
            final KonfigSource k1,
            final KonfigSource k2,
            final KonfigSource... sources) {
        final List<KonfigSource> all = new ArrayList<>();
        Collections.addAll(all, k0, k1, k2);
        Collections.addAll(all, sources);
        return all;
    }

}
