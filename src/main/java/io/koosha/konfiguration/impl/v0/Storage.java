package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.KfgAssertionException;
import io.koosha.konfiguration.Q;
import lombok.NonNull;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@NotThreadSafe
interface Storage<K> {

    @NotNull
    @Contract(pure = true)
    String name();

    @Contract(mutates = "this")
    boolean isNull(@NotNull K key);

    @Contract(mutates = "this")
    boolean has(@NotNull K key, @Nullable Q<?> type);

    @Nullable
    @Contract(mutates = "this")
    Object get(@NotNull K key, @Nullable Q<?> type);

    @NotNull
    @Contract(pure = true)
    static <U> Storage<U> fromMap(@NotNull @NonNull final String name,
                                  @NotNull @NonNull final Map<U, ?> map) {
        final Map<U, ?> copy = new HashMap<>(map);
        return new Storage<U>() {

            @Override
            @NotNull
            public String name() {
                return name;
            }

            @Override
            public boolean isNull(@NotNull final U key) {
                if (!copy.containsKey(key))
                    // Should've been prevented by kombiner.
                    throw new KfgAssertionException("no such key", Factory.map("key", key));
                return copy.containsKey(key) && copy.get(key) == null;
            }

            @Override
            public boolean has(@NotNull @NonNull final U key,
                               @Nullable final Q<?> type) {
                return copy.containsKey(key) && Q.matchesValue(type, copy.get(key));
            }

            @Override
            public Object get(@NotNull final U key,
                              @Nullable final Q<?> type) {
                final Object ret = copy.get(key);
                if (!Q.matchesValue(type, ret))
                    // Should've been prevented by kombiner.
                    throw new KfgAssertionException("type mismatch", Factory.map(
                            "key", key,
                            "type", type
                    ));
                return ret;
            }
        };
    }

}
