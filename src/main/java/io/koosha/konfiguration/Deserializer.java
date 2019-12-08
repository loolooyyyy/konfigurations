package io.koosha.konfiguration;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Deserialize values out of config source.
 *
 * @param <S> konfig source.
 */
@ThreadSafe
public interface Deserializer<S> {

    @Contract(pure = true)
    Object custom(@NotNull S source, @NotNull Q<?> type);

    @Contract(pure = true)
    <U> List<U> list(@NotNull S source, @NotNull Q<List<U>> type);

    @Contract(pure = true)
    <U> Set<?> set(@NotNull S source, @NotNull Q<U> type);

    @Contract(pure = true)
    <U, V> Map<U, V> map(@NotNull S source, @NotNull Q<Map<U, V>> type);

}
