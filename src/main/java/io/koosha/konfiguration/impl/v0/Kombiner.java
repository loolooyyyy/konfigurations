package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Almost Thread-safe, <b>NOT</b> immutable.
 */
@ThreadSafe
final class Kombiner implements Konfiguration {

    @NonNull
    @NotNull
    @Getter
    @Accessors(fluent = true)
    private final String name;

    private final long lockWaitTime;

    final List<Konfiguration> sources;
    final Map<String, K<?>> storage = new HashMap<>();
    final Map<String, Q<?>> typeMap = new HashMap<>();
    final Kombiner_Lock lock;
    final Kombiner_Observers observers;

    Kombiner(@NotNull @NonNull final String name,
             @NotNull @NonNull final Collection<Konfiguration> sources,
             final long lockWaitTime) {
        this.name = name;

        this.lockWaitTime = lockWaitTime;
        if (lockWaitTime < 0)
            throw new KfgIllegalStateException(this.name(), null, Q.LONG, lockWaitTime, "wait time must be gte 0");

        this.lock = new Kombiner_Lock(name, lockWaitTime);
        this.observers = new Kombiner_Observers(this.name, lockWaitTime);

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
    }

    private final AtomicReference<Konfiguration.Manager> man = new AtomicReference<>(new Konfiguration.Manager() {


    });


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @NonNull
    public final Handle registerSoft(@NonNull @NotNull final KeyObserver observer) {
        return this.lock().doWriteLocked(() -> {
            try {
                return this.observers.registerSoft(observer, KeyObserver.LISTEN_TO_ALL);
            }
            catch (final InterruptedException e) {
                throw new KfgConcurrencyException(this.name(), e);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull @NotNull Handle register(@NotNull @NonNull final KeyObserver observer,
                                             @NotNull @NonNull final String key) {
        return this.lock().doWriteLocked(() -> {
            try {
                return this.observers.registerHard(observer, KeyObserver.LISTEN_TO_ALL);
            }
            catch (final InterruptedException e) {
                throw new KfgConcurrencyException(this.name(), e);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull @NotNull Konfiguration deregister(@NotNull @NonNull final Handle observer,
                                                      @NotNull @NonNull final String key) {
        return this.lock().doWriteLocked(() -> {
            try {
                this.observers.deregister(observer, key);
            }
            catch (final InterruptedException e) {
                throw new KfgConcurrencyException(this.name(), e);
            }
            return this;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @NonNull
    public final Konfiguration deregister(@NotNull @NonNull final Handle observer) {
        return this.lock().doWriteLocked(() -> {
            try {
                this.observers.deregister(observer);
            }
            catch (final InterruptedException e) {
                throw new KfgConcurrencyException(this.name(), e);
            }
            return this;
        });
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @NonNull
    public final Konfiguration subset(@NonNull @NotNull final String key) {
        this.lock();
        return new SubsetView(this.name() + "::" + key, this, key);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Manager manager() {
        final Manager man = this.man.getAndSet(null);
        if (man == null)
            throw new KfgIllegalStateException(this.name(), null, null, null, "manager is already taken out");
        return man;
    }


    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull @NonNull final String key,
                       @Nullable final Q<?> type) {
        return this.lock().doReadLocked(() ->
                this.storage.containsKey(key(key, type))
                        || this.sources
                        .stream()
                        .filter(x -> x != this)
                        .anyMatch(k -> k.has(key, type))
        );
    }


    private Object putValue(@NotNull @NonNull final Konfiguration source,
                            @NotNull @NonNull final String name,
                            @Nullable final Q<?> dataType) {
        this.lock();
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

    <U> U getValue(@NonNull @NotNull String name,
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
        final Kombiner_KonfigVImpl<?> r = this.kvalCache.get(key0);

        if (r.isSameAs(dataType, elementType)) {
            @SuppressWarnings("unchecked")
            final K<U> cast = (K<U>) r;
            return cast;
        }
        else {
            throw new KfgTypeException(this.name(), key0, null, dataType, null, r);
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

            final Kombiner_KonfigVImpl<U> rr = new Kombiner_KonfigVImpl<>(this, key0, type);
            this.kvalCache.put(key0, rr);
            return rr;
        }
        finally {
            if (writeLock != null)
                writeLock.unlock();
        }

    }

    // =========================================================================

    Kombiner_Lock lock() {
        if (this.man.get() != null)
            throw new KfgIllegalStateException(this.name(), "konfiguration manager is not taken out yet");
        return this.lock;
    }

    @NotNull
    @NonNull
    @Contract(pure = true)
    private static String key(@NotNull @NonNull final String key,
                              @Nullable final Q<?> type) {
        return (type == null ? "?" : type.toString()) + "::";
    }

}
