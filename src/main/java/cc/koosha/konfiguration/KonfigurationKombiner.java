package cc.koosha.konfiguration;

import lombok.*;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Thread-safe, <b>NOT</b> immutable.
 */
public final class KonfigurationKombiner implements Konfiguration {

    public KonfigurationKombiner(@NonNull final KonfigSource... sources) {

        this(Arrays.asList(sources));
    }

    public KonfigurationKombiner(@NonNull final Collection<KonfigSource> sources) {

        val sources_ = new ArrayList<KonfigSource>(sources);

        if (sources_.isEmpty())
            throw new IllegalArgumentException("no source given");

        this.sources = sources_;
    }

    @Override
    public final K<Boolean> bool(final String key) {

        return create(key, Boolean.class, null);
    }

    @Override
    public final K<Integer> int_(final String key) {

        return create(key, Integer.class, null);
    }

    @Override
    public final K<Long> long_(final String key) {

        return create(key, Long.class, null);
    }

    @Override
    public final K<Double> double_(final String key) {

        return create(key, Double.class, null);
    }

    @Override
    public final K<String> string(final String key) {

        return create(key, String.class, null);
    }

    @Override
    public final <T> K<List<T>> list(final String key, final Class<T> type) {

        return create(key, List.class, type);
    }

    @Override
    public final <T> K<Map<String, T>> map(final String key, final Class<T> type) {

        return create(key, Map.class, type);
    }

    @Override
    public final <T> K<Set<T>> set(final String key, final Class<T> type) {

        return create(key, Set.class, type);
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

        return create(key, null, type);
    }


    @Override
    public final Konfiguration subset(final String key) {

        if (key.startsWith("."))
            throw new IllegalArgumentException("key must not start with a dot: " + key);

        return new _KonfigurationSubsetView(this, key);
    }


    @Override
    public final Konfiguration register(final EverythingObserver observer) {

        this.konfigObserversHolder.register(observer);
        return this;
    }

    @Override
    public final Konfiguration deregister(final EverythingObserver observer) {

        this.konfigObserversHolder.deregister(observer);
        return this;
    }

    @Override
    public boolean update() {

        boolean isUpdatable = false;
        for (val source : this.sources)
            if (source.isUpdatable()) {
                isUpdatable = true;
                break;
            }
        if (!isUpdatable)
            return false;

        val updatedSources = new ArrayList<KonfigSource>(this.sources.size());
        for (val source : this.sources)
            updatedSources.add(source.copyAndUpdate());

        final Map<String, Object> newCache = new HashMap<>();
        final Set<String> updatedKeys = new HashSet<>();

        val holder = _update(updatedSources, newCache, updatedKeys);
        if (holder == null)
            return false;

        for (val listener : holder.getEverythingObservers().entrySet())
            listener.getKey().accept();
        for (val observer : holder.getKeyObservers().entrySet())
            for (val updatedKey : updatedKeys)
                if (observer.getValue().contains(updatedKey))
                    observer.getKey().accept(updatedKey);
        holder.getKeyObservers().clear();
        holder.getEverythingObservers().clear();

        return true;
    }

    private _KonfigObserversHolder _update(ArrayList<KonfigSource> updatedSources,
                                           Map<String, Object> newCache,
                                           Set<String> updatedKeys) {

        @Cleanup("unlock")
        val lock = LOCK.writeLock();
        lock.lock();

        // Bad idea, I know.
        @Cleanup("unlock")
        val oLock = this.konfigObserversHolder.getOBSERVERS_LOCK().writeLock();
        oLock.lock();

        for (val each : this.valuCache.entrySet()) {
            val name = each.getKey();
            val value = each.getValue();
            boolean found = false;
            boolean changed = false;
            for (val source : updatedSources) {
                if (source.contains(name)) {
                    found = true;
                    val kVal = kvalCache.get(name);
                    val newVal = this._putV_calledByCreate(source, name, kVal.getDataType(), kVal.getElementType());
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

        valuCache.clear();
        valuCache.putAll(newCache);
        return this.konfigObserversHolder.copy();
    }

    public Konfiguration readonly() {

        return new _KonfigurationSubsetView(this);
    }


    // ------------------------------------------------------------------------

    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();
    private final Map<String, Object> valuCache = new HashMap<>();
    private final Map<String, _KonfigVImpl<?>> kvalCache = new HashMap<>();
    private final _KonfigObserversHolder konfigObserversHolder = new _KonfigObserversHolder();
    private final Collection<KonfigSource> sources;

    private Object _putV_calledByCreate(KonfigSource source, String name, Class<?> dataType, Class<?> elementType) {

        Object result;

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

        valuCache.put(name, result);
        return result;
    }

    private <T> T _get_calledByKonfigVImpl(String name, T def, boolean mustExist) {

        val lock = LOCK.readLock();
        try {
            lock.lock();
            if (valuCache.containsKey(name)) {
                @SuppressWarnings("unchecked")
                val get = (T) valuCache.get(name);
                return get;
            }
        }
        finally {
            lock.unlock();
        }

        if (mustExist)
            throw new KonfigurationMissingKeyException(name);

        return def;
    }

    private <T> K<T> _getK_calledByCreate(String name, Class<?> dataType, Class<?> elementType) {

        final _KonfigVImpl<?> r = this.kvalCache.get(name);

        if (r.isSameAs(dataType, elementType)) {
            @SuppressWarnings("unchecked")
            final K<T> cast = (K<T>) r;
            return cast;
        }
        else {
            throw new KonfigurationBadTypeException(
                    TypeName.typeName(dataType, elementType),
                    TypeName.typeName(r.getDataType(), r.getElementType()),
                    name
            );
        }
    }

    private <T> K<T> create(String name, Class<?> dataType, Class<?> elementType) {

        // We can not do this, in order to support default values.
        //    if(does not exist) throw new KonfigurationMissingKeyException(key);

        val readLock = LOCK.readLock();
        try {
            readLock.lock();
            if (this.kvalCache.containsKey(name))
                return this._getK_calledByCreate(name, dataType, elementType);
        }
        finally {
            readLock.unlock();
        }

        val writeLock = LOCK.writeLock();
        try {
            writeLock.lock();

            // Cache was already populated between two locks.
            if (this.kvalCache.containsKey(name))
                return this._getK_calledByCreate(name, dataType, elementType);

            for (val source : sources)
                if (source.contains(name)) {
                    this._putV_calledByCreate(source, name, dataType, elementType);
                    break;
                }

            val rr = new _KonfigVImpl<T>(this, name, dataType, elementType);
            this.kvalCache.put(name, rr);
            return rr;
        }
        finally {
            writeLock.unlock();
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode(of = {"key", "origin"})
    private static final class _KonfigVImpl<T> implements K<T> {

        private final KonfigurationKombiner origin;

        @Getter
        private final String key;

        @Getter(AccessLevel.PACKAGE)
        private final Class<?> dataType;

        @Getter(AccessLevel.PACKAGE)
        private final Class<?> elementType;


        boolean isSameAs(Class<?> dataType, Class<?> elementType) {

            return Objects.equals(this.getDataType(), dataType) &&
                    Objects.equals(this.getElementType(), elementType);
        }


        @Override
        public T v() {

            return this.origin._get_calledByKonfigVImpl(key, null, true);
        }

        @Override
        public T v(final T defaultValue) {

            return this.origin._get_calledByKonfigVImpl(key, defaultValue, false);
        }

        @Override
        public K<T> deregister(final KeyObserver observer) {

            this.origin.konfigObserversHolder.deregister(observer, this.key);
            return this;
        }

        @Override
        public K<T> register(@NonNull final KeyObserver observer) {

            this.origin.konfigObserversHolder.register(observer, this.key);
            return this;
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

            return String.format("KonfigV(%s=%s)", this.key, vs);
        }

    }

    private static final class _KonfigObserversHolder {

        /**
         * Observers mapped to the keys they are observing.
         * Reference to KeyObserver must be weak.
         */
        @Getter(AccessLevel.PACKAGE)
        private final Map<KeyObserver, Collection<String>> keyObservers;

        /**
         * Observers who observe everything, aka all the keys.
         * Reference to EverythingObserver must be weak, and that's why a map.
         */
        @Getter(AccessLevel.PACKAGE)
        private final Map<EverythingObserver, Void> everythingObservers;

        @Getter(AccessLevel.PACKAGE)
        private final ReadWriteLock OBSERVERS_LOCK = new ReentrantReadWriteLock();

        _KonfigObserversHolder() {

            this.everythingObservers = new WeakHashMap<>();
            this.keyObservers = new WeakHashMap<>();
        }

        /**
         * This constructor is used to create temporary snapshots of another
         * instance (and will be entirely GCed shortly after) so it's ok not to
         * use weak hash map.
         */
        private _KonfigObserversHolder(final _KonfigObserversHolder from) {

            this.keyObservers = new HashMap<>(from.keyObservers);
            this.everythingObservers = new HashMap<>(from.everythingObservers);
        }

        void register(final EverythingObserver observer) {

            @Cleanup("unlock")
            val lock = this.OBSERVERS_LOCK.writeLock();
            lock.lock();

            this.everythingObservers.put(observer, null);
        }

        void deregister(final EverythingObserver observer) {

            @Cleanup("unlock")
            val lock = this.OBSERVERS_LOCK.writeLock();
            lock.lock();

            this.everythingObservers.remove(observer);
        }

        void deregister(final KeyObserver observer, final String key) {

            @Cleanup("unlock")
            val lock = this.OBSERVERS_LOCK.writeLock();
            lock.lock();

            val keys = this.keyObservers.get(observer);
            if (keys == null)
                return;
            keys.remove(key);
            if (keys.isEmpty())
                this.keyObservers.remove(observer);
        }

        void register(@NonNull final KeyObserver observer, final String key) {

            @Cleanup("unlock")
            val lock = this.OBSERVERS_LOCK.writeLock();
            lock.lock();

            if (!this.keyObservers.containsKey(observer)) {
                val put = new HashSet<String>();
                put.add(key);
                this.keyObservers.put(observer, put);
            }
            else {
                this.keyObservers.get(observer).add(key);
            }
        }

        _KonfigObserversHolder copy() {

            @Cleanup("unlock")
            val lock = this.OBSERVERS_LOCK.readLock();
            lock.lock();

            return new _KonfigObserversHolder(this);
        }

    }

    private static final class _KonfigurationSubsetView implements Konfiguration {

        private final Konfiguration wrapped;
        private final String baseKey;

        _KonfigurationSubsetView(@NonNull final Konfiguration wrapped,
                                 @NonNull final String baseKey) {

            this.wrapped = wrapped;
            this.baseKey = baseKey.endsWith(".") ? baseKey : baseKey + ".";
        }

        _KonfigurationSubsetView(@NonNull final Konfiguration wrapped) {

            this.wrapped = wrapped;
            this.baseKey = "";
        }

        @Override
        public K<Boolean> bool(final String key) {

            return wrapped.bool(baseKey + key);
        }

        @Override
        public K<Integer> int_(final String key) {

            return wrapped.int_(baseKey + key);
        }

        @Override
        public K<Long> long_(final String key) {

            return wrapped.long_(baseKey + key);
        }

        @Override
        public K<Double> double_(final String key) {

            return wrapped.double_(baseKey + key);
        }

        @Override
        public K<String> string(final String key) {

            return wrapped.string(baseKey + key);
        }

        @Override
        public <T> K<List<T>> list(final String key, final Class<T> type) {

            return wrapped.list(baseKey + key, type);
        }

        @Override
        public <T> K<Map<String, T>> map(final String key, final Class<T> type) {

            return wrapped.map(baseKey + key, type);
        }

        @Override
        public <T> K<Set<T>> set(final String key, final Class<T> type) {

            return wrapped.set(baseKey + key, type);
        }

        @Override
        public <T> K<T> custom(final String key, final Class<T> type) {

            return wrapped.custom(baseKey + key, type);
        }


        @Override
        public boolean update() {

            throw new KonfigurationException("update is not available from subset view");
        }


        @Override
        public Konfiguration subset(@NonNull final String key) {

            val newKey = this.baseKey + "." + key;
            return new _KonfigurationSubsetView(this.wrapped, newKey);
        }

        @Override
        public Konfiguration register(final EverythingObserver observer) {

            return this.wrapped.register(observer);
        }

        @Override
        public Konfiguration deregister(final EverythingObserver observer) {

            return this.wrapped.deregister(observer);
        }

    }

}
