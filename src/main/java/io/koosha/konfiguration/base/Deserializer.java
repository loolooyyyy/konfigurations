package io.koosha.konfiguration.base;

import io.koosha.konfiguration.type.Q;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

@ThreadSafe
@FunctionalInterface
public interface Deserializer {

    /**
     * Deserialize a byte array to requested type.
     *
     * @param bytes source bytes.
     * @param q     requested type.
     * @param <T>   generic type of requested type.
     * @return deserialized type out of {@code bytes}.
     * @throws UnsupportedOperationException if the byte is not of type q.
     */
    <T> T apply(@NotNull byte[] bytes, @NotNull Q<@NotNull T> q);

}
