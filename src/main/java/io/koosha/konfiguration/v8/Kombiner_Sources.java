package io.koosha.konfiguration.v8;

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

    private final Map<String, CheatingMan> sources = new HashMap<>();

    @Contract(pure = true)
    @NotNull
    Stream<CheatingMan> vs() {
        return this.sources.values().stream();
    }

    @Contract(mutates = "this")
    @NotNull
    Kombiner_Sources replace(@NotNull @NonNull final Map<String, CheatingMan> newSources) {
        this.sources.clear();
        this.sources.putAll(newSources);
        return this;
    }

    @Contract(pure = true)
    @NotNull
    Map<String, CheatingMan> copy() {
        return new HashMap<>(this.sources);
    }

}
