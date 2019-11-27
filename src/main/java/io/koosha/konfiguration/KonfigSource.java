package io.koosha.konfiguration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.prefs.Preferences;


/**
 * Provides a konfiguration source to a {@link Konfiguration}
 *
 * <p>A config source must NOT be used directly, it must be supplied to
 * {@link Konfiguration}.
 *
 * <p>All the methods denoted with 'Thread-safe' in their comment section must
 * be implemented in a thread safe fashion.
 *
 * <p>All the implementations of this interface (and for the love of god all the
 * values returned by methods of this interface) must be immutable.
 */
@SuppressWarnings("unused")
public interface KonfigSource {

    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (boolean).
     */
    Boolean bool(String key);

    /**
     * Read and return a byte value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (boolean).
     */
    Byte byte_(String key);

    /**
     * Read and return a char value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (boolean).
     */
    Character char_(String key);

    /**
     * Read and return a short value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (boolean).
     */
    Short short_(String key);

    /**
     * Read and return a int value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (int).
     */
    Integer int_(String key);

    /**
     * Read and return a long value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (long).
     */
    Long long_(String key);

    /**
     * Read and return a float value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (double).
     */
    Float float_(String key);

    /**
     * Read and return a double value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (double).
     */
    Double double_(String key);

    /**
     * Read and return a string value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (string).
     */
    String string(String key);


    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key  the unique key of the konfiguration being requested.
     * @param type type of elements in the list.
     * @param <U>  generic type of elements in the list.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (list of
     *                                T).
     */
    <U> List<U> list(String key, Class<U> type);

    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key  the unique key of the konfiguration being requested.
     * @param type type of values of the map.
     * @param <U>  generic type of value elements in the map.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (map from
     *                                String to T).
     */
    <U> Map<String, T> map(String key, Class<U> type);

    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key  the unique key of the konfiguration being requested.
     * @param type type of elements in the set.
     * @param <U>  generic type of elements in the set.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (set of
     *                                T).
     */
    <U> Set<U> set(String key, Class<U> type);

    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key  the unique key of the konfiguration being requested.
     * @param type type of the custom object requested.
     * @param <U>  generic type of the custom object requested.
     * @return the value of konfiguration represented by supplied key in the
     * source.
     * @throws KfgMissingKeyException if the requested key is not present in this source.
     * @throws KfgTypeException       if the request key does not present the requested type (object
     *                                of type T).
     */
    <U> U custom(String key, Class<U> type);


    /**
     * Check if requested key is present in this source.
     *
     * <p>Thread-safe.
     *
     * @param key the key to check.
     * @return true if the requested key exists in this source.
     */
    boolean contains(String key);

    String getName();

}
