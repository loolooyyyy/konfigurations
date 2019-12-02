package io.koosha.konfiguration;


import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Deserialize values out of config source.
 *
 * @param <S> konfig source.
 */
public interface Deserializer<S> {

    Object custom(S source, Q<?> type);

    <U> List<U> list(S source, Q<List<U>> type);

    <U> Set<?> set(S source, Q<U> type);

    <U, V> Map<U, V> map(S source, Q<Map<U, V>> type);

}
