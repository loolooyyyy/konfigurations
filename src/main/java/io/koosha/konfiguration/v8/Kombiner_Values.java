package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.K;
import io.koosha.konfiguration.Q;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.error.KfgMissingKeyException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor
@Accessors(fluent = true)
@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Values {

    @NonNull
    @NotNull
    private final Kombiner origin;

    @NotNull
    final Set<Q<?>> issuedKeys = new HashSet<>();

    @NotNull
    final Map<Q<?>, ? super Object> cache = new HashMap<>();

    <U> K<U> k(@NotNull @NonNull final Q<U> type) {
        this.issue(type);
        return new Kombiner_K<>(this.origin, type);
    }

    @Contract(mutates = "this")
    @Nullable
    @SuppressWarnings("unchecked")
    <U> U v(@NotNull @NonNull final Q<?> type,
            @Nullable final U def,
            final boolean mustExist) {
        return this.origin.r(() -> {
            if (cache.containsKey(type))
                return ((U) cache.get(type));
            return this.origin.w(() -> ((U) v_(type, def, mustExist)));
        });
    }

    Object v_(@NotNull final Q<?> key,
              final Object def,
              final boolean mustExist) {
        final Optional<Source> first = this
                .origin
                .sources
                .vs()
                .map(CheatingKonfigurationManager::source)
                .filter(source -> source.has(key))
                .findFirst();
        if (!first.isPresent() && mustExist)
            throw new KfgMissingKeyException(this.origin.name(), null, key);
        this.issue(key);
        if (!first.isPresent())
            return def;
        final Object value = first.get().custom(key).v();
        this.cache.put(key, value);
        return value;
    }

    boolean has(@NonNull @NotNull final Q<?> t) {
        requireNonNull(t.key());
        return this.cache.containsKey(t);
    }

    private void issue(@NotNull @NonNull final Q<?> q) {
        this.issuedKeys.add(q);
    }


    @NotNull
    Map<Q<?>, Object> copy() {
        return new HashMap<>(this.cache);
    }

    Kombiner_Values replace(@NotNull @NonNull final Map<Q<?>, Object> copy) {
        this.cache.clear();
        this.cache.putAll(copy);
        return this;
    }

    void origForEach(Consumer<Q<?>> action) {
        this.issuedKeys.forEach(action);
    }

}
