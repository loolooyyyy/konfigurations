package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.*;
import lombok.*;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Almost Thread-safe, <b>NOT</b> immutable.
 */
@ThreadSafe
final class Kombiner implements Konfiguration {

    static final long LOCK_WAIT_MILLIS__DEFAULT = 300;


    final boolean readonly;

    @NonNull
    @NotNull
    @Getter
    @Accessors(fluent = true)
    private final String name;

    private final long lockWaitTime;

    private final List<Konfiguration> sources;
    private final Map<String, K<?>> storage = new HashMap<>();
    private final WeakHashMap<KeyObserver, Collection<String>> keyObservers = new WeakHashMap<>();

    private final ReadWriteLock KONFIG_LOCK = new ReentrantReadWriteLock(true);
    private final ReadWriteLock AUX_LOCK = new ReentrantReadWriteLock(true);

    private final Object UPDATE_LOCK = new Object();


    private void acquire(@NonNull @NotNull final Lock lock) {
        this.init();
        try {
            if (!lock.tryLock(this.lockWaitTime, TimeUnit.MILLISECONDS))
                throw new KfgException(this, null, null, null, "could not acquire lock");
        }
        catch (InterruptedException e) {
            throw new KfgException(this, null, null, null, e);
        }
    }

    private void release(@Nullable final Lock lock) {
        this.init();
        if (lock != null)
            lock.unlock();
    }

    private <T> T doReadLocked(@NonNull @NotNull final Supplier<T> func) {
        this.init();
        Lock lock = null;
        try {
            lock = KONFIG_LOCK.readLock();
            acquire(lock);
            return func.get();
        }
        finally {
            release(lock);
        }
    }

    private <T> T doWriteLocked(@NonNull @NotNull final Supplier<T> func) {
        this.init();
        Lock lock = null;
        try {
            lock = KONFIG_LOCK.readLock();
            acquire(lock);
            return func.get();
        }
        finally {
            release(lock);
        }
    }


    Kombiner(@NotNull @NonNull final String name,
             final boolean readonly,
             @NotNull @NonNull final Collection<Konfiguration> sources,
             final long lockWaitTime) {
        this.name = name;
        this.readonly = readonly;
        if (lockWaitTime < 0)
            throw new KfgIllegalStateException(this, null, Q.LONG, lockWaitTime, "wait time must be gte 0");

        final List<Konfiguration> copy = sources
                .stream()
                .peek(k -> {
                    if (k == null)
                        throw new KfgIllegalArgumentException(this, "null in config sources");
                })
                .flatMap(k -> {
                    // Unwrap.
                    return k instanceof Kombiner
                           ? ((Kombiner) k).sources.stream()
                           : Stream.of(k);
                })
                .collect(toList());

        if (copy.isEmpty())
            throw new KfgIllegalArgumentException(this, "no source given");

        this.sources = new ArrayList<>(copy);
        this.lockWaitTime = lockWaitTime;
    }

    @NotNull
    @NonNull
    private static String key(@NotNull @NonNull final String key,
                              @Nullable final Q<?> type) {
        return key + (type == null ? null : "::" + type.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull @NonNull final String key,
                       @Nullable final Q<?> type) {
        return this.init().doReadLocked(() ->
                this.storage.containsKey(key(key, type))
                        || this.sources
                        .stream()
                        .filter(x -> x != this)
                        .anyMatch(k -> k.has(key, type))
        );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @NonNull
    public final Konfiguration registerSoft(@NonNull @NotNull final KeyObserver observer) {
        this.init().registerSoft(observer, "");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @NonNull
    public final Konfiguration deregisterSoft(@NotNull @NonNull final KeyObserver observer) {
        this.init().deregisterSoft(observer, "");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @NonNull
    public final Konfiguration subset(@NonNull @NotNull final String key) {
        return new SubsetView(this.init().name() + "::" + key, this, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Manager manager() {
        final Manager man = this.man.getAndSet(null);
        if (man == null)
            throw new KfgIllegalStateException(this, null, null, null, "manager is already taken out");
        return man;
    }


    boolean hasUpdate0() {
        return this.init().doReadLocked(() -> this.sources
                .stream()
                .filter(x -> x != this)
                .map(Konfiguration::manager)
                .anyMatch(Konfiguration.Manager::hasUpdate));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Synchronized("UPDATE_LOCK")
    Konfiguration update0() {
        this.init().doReadLocked(() -> {
            if (sources.stream().noneMatch(k -> k != this && k.manager().hasUpdate()))
                return null;

            final List<Konfiguration> up = this.sources
                    .stream()
                    .filter(x -> x != this)
                    .map(Konfiguration::manager)
                    .map(Konfiguration.Manager::update)
                    .collect(toList());


            final Map<String, ? extends K<?>> newStorage = storage
                    .entrySet()
                    .stream()
                    .map(TypedKey::new)
                    .peek(p -> p.b = up
                            .stream()
                            .filter(k -> k.has(p.a, p.b))
                            .map(k -> k.custom(p.a, p.b))
                            .findFirst()
                            .map(found -> k(p.a, p.b, found.v()))
                            .orElse(null)
                    ));
                    .collect(toMap(TypedKey::a, TypedKey::b));

            final Map<KeyObserver, Collection<String>> newObservers = this.keyObservers
                    .entrySet()
                    .stream()
                    .filter(e -> e.getKey() != null)
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

            final List<String> updatedKeys = storage
                    .entrySet()
                    .stream()
                    .filter(e -> !compare(e.getKey(), e.getValue(), newStorage.get(e.getKey())))
                    .map(Map.Entry::getKey)
                    .collect(toList());

            //            final Set<String> updated = storage
            //                    .entrySet()
            //                    .stream()
            //                    .filter(e -> updatedSources.stream().anyMatch(k ->
            //                            k.contains(key(e.getKey(), e.getValue().getType()))))
            //                    .map(Map.Entry::getKey)
            //                    .collect(toSet());

            doWriteLocked(() -> {
                for (final Map.Entry<String, Object> each : this.valueCache.entrySet()) {
                    final String name = each.getKey();
                    final Object value = each.getValue();
                    boolean found = false;
                    boolean changed = false;
                    for (final Konfiguration source : up) {
                        if (source.contains(name)) {
                            found = true;
                            final K<?> kVal = kvalCache.get(name);
                            final Object newVal = this.putValue(
                                    source,
                                    name,
                                    kVal.type);
                            newCache.put(name, newVal);
                            changed = !newVal.equals(value);
                            break;
                        }
                    }

                    if (!found || changed)
                        updatedKeys.add(name);
                }

                return null;
            });

            return null;
        });

        Lock lock = null;
        try {
            Lock oLock = null;
            try {
                oLock = this.KONFIG_LOCK.writeLock();
                if (!oLock.tryLock(LOCK_WAIT_MILLIS, TimeUnit.MILLISECONDS))
                    throw new KfgException(this, null, null, null, "could not acquire lock");


                if (updatedKeys.isEmpty()) {
                    observers = Collections.emptyMap();
                }
                else {
                    valueCache.clear();
                    valueCache.putAll(newCache);

                    this.sources.clear();
                    this.sources.addAll(updatedSources);

                    observers = new HashMap<>(this.keyObservers);
                }
            }
            finally {
                if (oLock != null)
                    oLock.unlock();
            }


            observers.entrySet()
                     .stream()
                     .filter(e -> e.getKey() != null)
                     .forEach(e -> {
                         final Collection<String> keys = e.getValue();
                         final KeyObserver observer = e.getKey();
                         updatedKeys.stream()
                                    .filter(keys.contains(KeyObserver.LISTEN_TO_ALL)
                                            ? x -> true
                                            : keys::contains)
                                    .forEach(observer);
                     });
        }
        catch (final InterruptedException e) {
            throw new KfgException(this, null, null, null, e);
        }
        finally {
            if (lock != null)
                lock.unlock();
        }

        return this;
    }

    // =========================================================================

    private Object putValue(@NotNull @NonNull final Konfiguration source,
                            @NotNull @NonNull final String name,
                            @Nullable final Q<?> dataType) {
        this.init();
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

    private <U> U getValue(@NonNull @NotNull String name,
                           U def,
                           boolean mustExist) {
        this.init();
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

    private <U> K<U> getWrappedValue(@NonNull @NotNull final String key0,
                                     @Nullable final Q<U> type) {
        this.init();
    }

    private <U> K<U> getWrappedValue0(@NotNull @NonNull final String key0,
                                      final Class<?> dataType,
                                      final Class<?> elementType) {
        this.init();
        final KonfigVImpl<?> r = this.kvalCache.get(key0);

        if (r.isSameAs(dataType, elementType)) {
            @SuppressWarnings("unchecked")
            final K<U> cast = (K<U>) r;
            return cast;
        }
        else {
            throw new KfgTypeException(this, key0, null, dataType, null, r);
        }
    }

    @NonNull
    @NotNull
    public Konfiguration deregisterSoft(@NonNull @NotNull final KeyObserver observer,
                                        @Nullable final String key) {
        this.init();
        Lock lock = null;
        try {
            lock = this.OBSERVERS_LOCK.writeLock();
            lock.lock();

            final Collection<String> keys = this.keyObservers.get(observer);
            if (keys == null)
                return null;
            keys.remove(key);
            if (keys.isEmpty())
                this.keyObservers.remove(observer);
        }
        finally {
            if (lock != null)
                lock.unlock();
        }
        return this;
    }

    @NonNull
    @NotNull
    public Konfiguration registerSoft(@NonNull @NotNull final KeyObserver observer,
                                      @Nullable final String key) {
        this.init();
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
        return this;
    }


    @Nullable
    protected Object getPrimitive(@NonNull @NotNull final String key,
                                  @Nullable final Q<?> type) {
        this.init();
        return null;
    }

    @Nullable
    protected Object getContainer(@NotNull @NonNull final String key,
                                  final Q<?> type) {
        this.init();
        return null;
    }

    @Nullable
    protected Object getCustom(@NonNull final String key,
                               @NotNull final Q<?> type) {
        this.init();
        return null;
    }


    @NotNull
    protected <U> K<U> k(@NonNull final String key,
                         @Nullable final Q<U> type,
                         @Nullable final Object value) {
        this.init();
        Lock readLock = null;
        try {
            readLock = VALUES_LOCK.readLock();
            readLock.lock();
            if (this.kvalCache.containsKey(key0))
                return this.getWrappedValue0(key0, type);
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
                return this.getWrappedValue0(key0, type);

            for (final Konfiguration source : sources)
                if (source.contains(key0)) {
                    this.putValue(source, key0, type);
                    break;
                }

            final KonfigVImpl<U> rr = new KonfigVImpl<>(this, key0, type);
            this.kvalCache.put(key0, rr);
            return rr;
        }
        finally {
            if (writeLock != null)
                writeLock.unlock();
        }

    }

    // =========================================================================

    private Kombiner init() {
        if (this.man.get() != null)
            throw new KfgIllegalStateException(this, "konfiguration manager is not taken out yet");
        return this;
    }

    private final AtomicReference<Konfiguration.Manager> man = new AtomicReference<>(new Konfiguration.Manager() {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasUpdate() {
            return hasUpdate0();
        }

        /**
         * {@inheritDoc}
         */
        @NotNull
        @Override
        public Konfiguration update() {
            return update0();
        }

    });

    @AllArgsConstructor
    private static final class KonfigVImpl<U> implements K<U> {

        @NonNull
        @NotNull
        private final Kombiner origin;

        @NonNull
        @NotNull
        @Getter(onMethod_ = {@NotNull})
        private final String key;

        @NonNull
        @NotNull
        @Getter(onMethod_ = {@NotNull})
        private final Q<U> type;

        boolean isSameAs(final Q<?> q) {
            return Q.matchesType(this.type, q);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nullable
        public U v() {
            return this.origin.getValue(key, null, true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean exists() {
            throw new UnsupportedOperationException("TODO");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull
        public K<U> deregister(@NonNull @NotNull final KeyObserver observer) {
            this.origin.deregisterSoft(observer, this.key);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull
        public K<U> register(@NonNull @NotNull final KeyObserver observer) {
            this.origin.registerSoft(observer, this.key);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            try {
                return format("K(%s=%s)", this.key, this.v().toString());
            }
            catch (final Exception e) {
                return format("K(%s)::error", this.key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            KonfigVImpl<?> konfigV = (KonfigVImpl<?>) o;
            return Objects.equals(origin, konfigV.origin) && Objects.equals(key, konfigV.key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(origin, key);
        }

    }

    @Accessors(fluent = true)
    @EqualsAndHashCode
    @Getter
    @AllArgsConstructor
    private final static class TypedKey {
        @NonNull
        @NotNull
        private final String a;

        private final Q<?> b;

        TypedKey(Map.Entry<String, K<?>> e) {
            this(e.getKey(), e.getValue().type());
        }
    }

}
