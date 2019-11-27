package io.koosha.konfiguration;


import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Deserialize values out of config source.
 *
 * @param <S> konfig source.
 * @param <K> key type.
 */
public interface Deserializer<S, K> {

    Object custom(S source, Class<?> type);

    List<?> list(S source, Class<?> type);

    Set<?> set(S source, Class<?> type);

    Map<?, ?> map(S source, Class<?> type);

}
