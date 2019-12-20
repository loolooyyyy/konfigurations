package io.koosha.konfiguration;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

@ThreadSafe
@FunctionalInterface
public interface Deserializer<@NotNull T> extends BiFunction<byte[], @NotNull Q<@NotNull T>, @NotNull T> {

    T apply(@NotNull byte[] bytes, @NotNull Q<@NotNull T> q);

}
