package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.*;
import io.koosha.konfiguration.error.KfgIllegalArgumentException;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * Almost Thread-safe, <b>NOT</b> immutable.
 */
@ThreadSafe
@ApiStatus.Internal
final class Kombiner implements Konfiguration {

    @NotNull
    @Getter
    @Accessors(fluent = true)
    private final String name;

    @NotNull
    final Kombiner_Sources sources;

    @NotNull
    final Kombiner_Lock _lock;

    @NotNull
    final Kombiner_Observers observers;

    @NotNull
    final Kombiner_Values values;

    private final AtomicReference<Kombiner_Manager> _man = new AtomicReference<>();

    public KonfigurationManager man() {
        return this._man.getAndSet(null);
    }

    Kombiner(@NotNull @NonNull final String name,
             @NotNull @NonNull final Collection<KonfigurationManager> sources,
             @Nullable final Long lockWaitTimeMillis,
             final boolean fairLock) {
        this.name = name;

        // Find duplicate names.
        final List<@NotNull String> duplicates = sources
                .stream()
                .flatMap(x ->// Unwrap.
                        x instanceof Kombiner_Manager
                        ? ((Kombiner_Manager) x).origin.sources.vs()
                        : Stream.of(x))
                .map(KonfigurationManager::name)
                .collect(groupingBy(identity(), counting()))
                .entrySet()
                .stream()
                .filter(p -> p.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(toList());
        if (!duplicates.isEmpty())
            throw new KfgIllegalArgumentException(name, "duplicate names: " +
                    join(", ", duplicates));

        // Wrap in CheatingMan.
        final Map<String, CheatingMan> s = new LinkedHashMap<>();
        sources.stream()
               .flatMap(x ->// Unwrap.
                       x instanceof Kombiner_Manager
                       ? ((Kombiner_Manager) x).origin.sources.vs()
                       : Stream.of(x))
               .map(CheatingMan::cheat)
               .forEach(x -> s.put(x.name(), x));
        if (s.isEmpty())
            throw new KfgIllegalArgumentException(name, "no source given");

        this._lock = new Kombiner_Lock(name, lockWaitTimeMillis, fairLock);
        this.observers = new Kombiner_Observers(this.name);
        this._man.set(new Kombiner_Manager(this));
        this.values = new Kombiner_Values(this);
        this.sources = new Kombiner_Sources(this);

        this.sources.replace(s);
    }

    Kombiner_Lock lock() {
        if (this._man.get() != null)
            throw new KfgIllegalStateException(this.name(), "konfiguration manager is not taken out yet");
        return this._lock;
    }

    <T> T r(@NonNull @NotNull final Supplier<T> func) {
        return lock().doReadLocked(func);
    }

    <T> T w(@NonNull @NotNull final Supplier<T> func) {
        return lock().doWriteLocked(func);
    }

    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Boolean> bool(@NotNull @NonNull final String key) {
        return this.values.k(Q.BOOL.withKey(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Byte> byte_(@NotNull @NonNull final String key) {
        return this.values.k(Q.BYTE.withKey(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Character> char_(@NotNull @NonNull final String key) {
        return this.values.k(Q.CHAR.withKey(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Short> short_(@NotNull @NonNull final String key) {
        return this.values.k(Q.SHORT.withKey(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Integer> int_(@NotNull @NonNull final String key) {
        return this.values.k(Q.INT.withKey(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Long> long_(@NotNull @NonNull final String key) {
        return this.values.k(Q.LONG.withKey(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Float> float_(@NotNull @NonNull final String key) {
        return this.values.k(Q.FLOAT.withKey(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<Double> double_(@NotNull @NonNull final String key) {
        return this.values.k(Q.DOUBLE.withKey(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<String> string(@NotNull @NonNull final String key) {
        return this.values.k(Q.STRING.withKey(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U> K<List<U>> list(@NotNull @NonNull final Q<List<U>> key) {
        final String k = key.key();
        if (k == null || k.isEmpty())
            throw new KfgIllegalArgumentException(this.name(), "provided type has no key");
        return this.values.k(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U, V> K<Map<U, V>> map(@NotNull @NonNull final Q<Map<U, V>> type) {
        return this.values.k(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U> K<Set<U>> set(@NotNull @NonNull final Q<Set<U>> type) {
        return this.values.k(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U> K<U> custom(@NotNull @NonNull final Q<U> type) {
        final String k = type.key();
        if (k == null || k.isEmpty())
            throw new KfgIllegalArgumentException(this.name(), "provided type has no key");
        return this.values.k(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull @NonNull final Q<?> type) {
        final String k = type.key();
        if (k == null || k.isEmpty())
            throw new KfgIllegalArgumentException(this.name(), "provided type has no key");
        final Q<?> t = Q.withKey0(type, k);
        return r(() -> this.values.has(t) ||
                this.sources.vs().filter(x -> x.source() != this)
                            .anyMatch(x -> x.source().has(type)));
    }

    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Handle register(@NotNull @NonNull final KeyObserver observer,
                           @NotNull @NonNull final String key) {
        return w(() -> observers.register(observer, key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Handle register(@NotNull @NonNull final KeyObserver observer,
                           @NotNull @NonNull final Q<?> key) {
        return w(() -> observers.register(observer, key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Handle register(@NotNull @NonNull final KeyObserver observer) {
        return w(() -> observers.register(observer));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer) {
        return w(() -> observers.registerSoft(observer));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer,
                               @NotNull @NonNull final String key) {
        return w(() -> observers.registerSoft(observer, key));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer,
                               @NotNull @NonNull final Q<?> key) {
        return w(() -> observers.registerSoft(observer, key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deregister(@NotNull @NonNull final Handle observer) {
        w(() -> {
            observers.deregister(observer);
            return null;
        });
    }

    // =========================================================================

    private static final Object HANDLE_LOCK = new Object();

    private static volatile long id_pool = Long.MAX_VALUE;

    private static volatile String str_pool = "";

    private static String next() {
        synchronized (HANDLE_LOCK) {
            id_pool++;
            str_pool = str_pool + "1";
        }
        return str_pool + id_pool;
    }

    @ThreadSafe
    @Immutable
    @Accessors(fluent = true)
    @EqualsAndHashCode(of = "id")
    @ApiStatus.Internal
    @RequiredArgsConstructor
    private static final class HandleImpl implements Handle {

        @NotNull
        @Getter
        private final String id;

    }

    public static Handle newHandle() {
        return new HandleImpl(next());
    }


}
