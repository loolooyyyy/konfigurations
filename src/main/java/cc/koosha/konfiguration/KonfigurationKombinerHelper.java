package cc.koosha.konfiguration;

import lombok.*;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class KonfigurationKombinerHelper {

    private final Collection<KonfigSource> sources;

    private final ReadWriteLock VALUES_LOCK = new ReentrantReadWriteLock();
    private final ReadWriteLock OBSERVERS_LOCK = new ReentrantReadWriteLock();

    private final Map<String, Object> valueCache = new HashMap<>();
    private final Map<String, KonfigVImpl> kvalCache = new HashMap<>();

    private final WeakHashMap<KeyObserver, Collection<String>> keyObservers =
            new WeakHashMap<>();

    public KonfigurationKombinerHelper(Collection<KonfigSource> sources) {
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

    private <T> T getValue(String name,
                           T def,
                           boolean mustExist) {
        @Cleanup("unlock")
        val lock = VALUES_LOCK.readLock();
        lock.lock();

        if (valueCache.containsKey(name)) {
            @SuppressWarnings("unchecked")
            val get = (T) valueCache.get(name);
            return get;
        }

        if (mustExist)
            throw new KonfigurationMissingKeyException(name);

        return def;
    }

    // --------------------

    <T> K<T> getWrappedValue(String name,
                             Class<?> dataType,
                             Class<?> elementType) {

        // We can not do this, in order to support default values.
        //    if(does not exist) throw new KonfigurationMissingKeyException(key);

        val readLock = VALUES_LOCK.readLock();
        try {
            readLock.lock();
            if (this.kvalCache.containsKey(name))
                return this.getWrappedValue0(name, dataType, elementType);
        }
        finally {
            readLock.unlock();
        }

        @Cleanup("unlock")
        val writeLock = VALUES_LOCK.writeLock();
        writeLock.lock();

        // Cache was already populated between two locks.
        if (this.kvalCache.containsKey(name))
            return this.getWrappedValue0(name, dataType, elementType);

        for (val source : sources)
            if (source.contains(name)) {
                this.putValue(source, name, dataType, elementType);
                break;
            }

        val rr = new KonfigVImpl<T>(this, name, dataType, elementType);
        this.kvalCache.put(name, rr);
        return rr;
    }

    private <T> K<T> getWrappedValue0(String name,
                                      Class<?> dataType,
                                      Class<?> elementType) {

        final KonfigVImpl<?> r = this.kvalCache.get(name);

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

    // --------------------

    @Synchronized
    boolean update() {
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

        val observers = update0(updatedSources, newCache, updatedKeys);
        if (observers == null)
            return false;

        for (val o : observers.entrySet()) {
            // Never empty or null.
            val interestedKeys = o.getValue();
            val observer = o.getKey();
            for (val updatedKey : updatedKeys)
                if (interestedKeys.contains(updatedKey))
                    observer.accept(updatedKey);
            if (interestedKeys.contains(""))
                observer.accept("");
        }

        return true;
    }

    /**
     * Thread safe (lock guarded) part of {@link #update()}.
     */
    private Map<KeyObserver, Collection<String>> update0(List<KonfigSource> updatedSources,
                                                         Map<String, Object> newCache,
                                                         Set<String> updatedKeys) {
        @Cleanup("unlock")
        val vLock = VALUES_LOCK.writeLock();
        vLock.lock();

        // Bad idea, I know. Dead locks?
        @Cleanup("unlock")
        val oLock = this.OBSERVERS_LOCK.writeLock();
        oLock.lock();

        for (val each : this.valueCache.entrySet()) {
            val name = each.getKey();
            val value = each.getValue();
            boolean found = false;
            boolean changed = false;
            for (val source : updatedSources) {
                if (source.contains(name)) {
                    found = true;
                    val kVal = kvalCache.get(name);
                    val newVal = this.putValue(source, name, kVal.getDataType(), kVal.getElementType());
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

    // --------------------

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
            val put = new HashSet<String>(1);
            put.add(key);
            this.keyObservers.put(observer, put);
        }
        else {
            this.keyObservers.get(observer).add(key);
        }
    }


    // --------------------

    @RequiredArgsConstructor
    @EqualsAndHashCode(of = {"key", "origin"})
    private static final class KonfigVImpl<T> implements K<T> {

        private final KonfigurationKombinerHelper origin;

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
        public K<T> register(@NonNull final KeyObserver observer) {
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

    }

}
