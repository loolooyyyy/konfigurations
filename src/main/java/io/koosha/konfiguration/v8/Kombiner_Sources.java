package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.Handle;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Sources {

    @SuppressWarnings("unused")
    @NotNull
    @NonNull
    private final Kombiner origin;

    private final Map<Handle, CheatingKonfigurationManager> sources = new HashMap<>();

    @Contract(pure = true)
    @NotNull
    Stream<CheatingKonfigurationManager> vs() {
        return sources.values().stream();
    }

    @Contract(mutates = "this")
    @NotNull
    Kombiner_Sources replace(@NotNull @NonNull final Map<Handle, CheatingKonfigurationManager> s) {
        this.sources.clear();
        this.sources.putAll(s);
        return this;
    }

    @Contract(pure = true)
    @NotNull
    Map<Handle, CheatingKonfigurationManager> copy() {
        return new HashMap<>(this.sources);
    }

}
