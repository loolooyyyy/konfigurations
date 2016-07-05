package cc.koosha.konfigurations.core;

import java.util.List;
import java.util.Map;


/**
 * TODO: change this behaviour:
 * Whenever a listener is notified, it does <b>NOT</b> necessarily mean an
 * actual change in the value. It's clients responsibility to check if it
 * matters.
 */
public interface Konfiguration {

    KonfigV<Boolean> bool(String key);

    KonfigV<Integer> int_(String key);

    KonfigV<Long> long_(String key);

    KonfigV<String> string(String key);


    <T> KonfigV<List<T>> list(String key, Class<T> type);

    <T> KonfigV<Map<String, T>> map(String key, Class<T> type);


    <T> KonfigV<T> custom(String key, Class<T> type);

}
