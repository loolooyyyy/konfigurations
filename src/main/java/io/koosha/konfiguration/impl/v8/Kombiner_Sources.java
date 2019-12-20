package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.Q;
import io.koosha.konfiguration.impl.base.KonfigurationManagerBase;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Sources {

    @NotNull
    @NonNull
    private final Kombiner origin;

    private final Map<Handle, KonfigurationManagerBase<?>> sources
            = new HashMap<>();

    boolean has(@NotNull @NonNull final String key,
                @Nullable final Q<?> type) {
        return this.sources
                .values()
                .stream()
                .filter(x -> x.origin0() != this.origin)
                .anyMatch(x -> x.origin0().has(key, type));
    }

    @Contract(pure = true)
    @NotNull
    Stream<KonfigurationManagerBase<?>> vs() {
        return sources.values().stream();
    }

    @Contract(mutates = "this")
    @NotNull
    Kombiner_Sources replace(@NotNull @NonNull final Map<Handle, KonfigurationManagerBase<?>> s) {
        this.sources.clear();
        this.sources.putAll(s);
        return this;
    }

    @Contract(pure = true)
    @NotNull
    Map<Handle, KonfigurationManagerBase<?>> copy() {
        return new HashMap<>(this.sources);
    }

}
