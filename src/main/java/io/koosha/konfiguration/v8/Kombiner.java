package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.*;
import io.koosha.konfiguration.error.KfgIllegalArgumentException;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import io.koosha.konfiguration.type.Q;
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

    private static final Object HANDLE_LOCK = new Object();
    private static final long START = -1L;
    private static volatile long id_pool = START;
    private static volatile String str_pool = "H#";
    @NotNull
    final Kombiner_Sources sources;
    @NotNull
    final Kombiner_Lock _lock;
    @NotNull
    final Kombiner_Observers observers;
    @NotNull
    final Kombiner_Values values;
    @NotNull
    @Getter
    @Accessors(fluent = true)
    private final String name;
    private final AtomicReference<Kombiner_Manager> _man = new AtomicReference<>();

    Kombiner(@NotNull @NonNull final String name,
             @NotNull @NonNull final Collection<KonfigurationManager> sources,
             @Nullable final Long lockWaitTimeMillis,
             final boolean fairLock,
             final boolean allowMixedTypes) {
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
        final Map<String, CheatingMan> managers = new LinkedHashMap<>();
        sources.stream()
               .flatMap(x ->// Unwrap.
                       x instanceof Kombiner_Manager
                       ? ((Kombiner_Manager) x).origin.sources.vs()
                       : Stream.of(x))
               .map(CheatingMan::cheat)
               .forEach(x -> managers.put(x.name(), x));
        if (managers.isEmpty())
            throw new KfgIllegalArgumentException(name, "no source given");

        this._lock = new Kombiner_Lock(name, lockWaitTimeMillis, fairLock);
        this.observers = new Kombiner_Observers(this.name);
        this._man.set(new Kombiner_Manager(this));
        this.values = new Kombiner_Values(this, allowMixedTypes);
        this.sources = new Kombiner_Sources(this);

        this.sources.replace(managers);
    }

    // =========================================================================

    private static String next() {
        synchronized (HANDLE_LOCK) {
            id_pool++;
            if (id_pool == START)
                str_pool += "F_";
        }
        return str_pool + id_pool;
    }

    public static Handle newHandle() {
        return new HandleImpl(next());
    }

    public KonfigurationManager man() {
        return this._man.getAndSet(null);
    }

    Kombiner_Lock lock() {
        if (this._man.get() != null)
            throw new KfgIllegalStateException(this.name(), "konfiguration manager is not taken out yet");
        return this._lock;
    }

    <T> T r(@NonNull @NotNull final Supplier<T> func) {
        return this.lock().doReadLocked(func);
    }

    <T> T w(@NonNull @NotNull final Supplier<T> func) {
        return this.lock().doWriteLocked(func);
    }

    @Override
    @NotNull
    public K<Boolean> bool(@NotNull @NonNull final String key) {
        return this.values.k(Q.bool(key));
    }

    @Override
    @NotNull
    public K<Byte> byte_(@NotNull @NonNull final String key) {
        return this.values.k(Q.byte_(key));
    }

    @Override
    @NotNull
    public K<Character> char_(@NotNull @NonNull final String key) {
        return this.values.k(Q.char_(key));
    }

    @Override
    @NotNull
    public K<Short> short_(@NotNull @NonNull final String key) {
        return this.values.k(Q.short_(key));
    }

    @Override
    @NotNull
    public K<Integer> int_(@NotNull @NonNull final String key) {
        return this.values.k(Q.int_(key));
    }

    @Override
    @NotNull
    public K<Long> long_(@NotNull @NonNull final String key) {
        return this.values.k(Q.long_(key));
    }

    @Override
    @NotNull
    public K<Float> float_(@NotNull @NonNull final String key) {
        return this.values.k(Q.float_(key));
    }

    @Override
    @NotNull
    public K<Double> double_(@NotNull @NonNull final String key) {
        return this.values.k(Q.double_(key));
    }

    // =========================================================================

    @Override
    @NotNull
    public K<String> string(@NotNull @NonNull final String key) {
        return this.values.k(Q.string(key));
    }

    @Override
    @NotNull
    public <U> K<List<U>> list(@NotNull @NonNull final Q<List<U>> key) {
        if (key.key().isEmpty())
            throw new KfgIllegalArgumentException(this.name(), "provided type has no key");
        return this.values.k(key);
    }

    @Override
    @NotNull
    public <U, V> K<Map<U, V>> map(@NotNull @NonNull final Q<Map<U, V>> key) {
        return this.values.k(key);
    }

    @Override
    @NotNull
    public <U> K<Set<U>> set(@NotNull @NonNull final Q<Set<U>> key) {
        return this.values.k(key);
    }

    @Override
    @NotNull
    public <U> K<U> custom(@NotNull @NonNull final Q<U> key) {
        if (key.key().isEmpty())
            throw new KfgIllegalArgumentException(this.name(), "provided type has no key");
        return this.values.k(key);
    }

    @Override
    public boolean has(@NotNull @NonNull final Q<?> key) {
        if (key.key().isEmpty())
            throw new KfgIllegalArgumentException(this.name(), "provided type has no key");
        return this.r(() -> this.values.has(key) ||
                this.sources.vs().filter(x -> x.source() != this)
                            .anyMatch(x -> x.source().has(key)));
    }

    @NotNull
    @Override
    public Handle register(@NotNull @NonNull final KeyObserver observer,
                           @NotNull @NonNull final String key) {
        return w(() -> observers.register(observer, key));
    }

    // =========================================================================

    @Override
    @NotNull
    public Handle register(@NotNull @NonNull final KeyObserver observer,
                           @NotNull @NonNull final Q<?> key) {
        return w(() -> observers.register(observer, key));
    }

    @Override
    @NotNull
    public Handle register(@NotNull @NonNull final KeyObserver observer) {
        return w(() -> observers.register(observer));
    }

    @Override
    @NotNull
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer) {
        return w(() -> observers.registerSoft(observer));
    }

    @NotNull
    @Override
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer,
                               @NotNull @NonNull final String key) {
        return w(() -> observers.registerSoft(observer, key));
    }

    @NotNull
    @Override
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer,
                               @NotNull @NonNull final Q<?> key) {
        return w(() -> observers.registerSoft(observer, key));
    }

    @Override
    public void deregister(@NotNull @NonNull final Handle observer) {
        w(() -> {
            observers.deregister(observer);
            return null;
        });
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


}
