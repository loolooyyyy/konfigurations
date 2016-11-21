package cc.koosha.konfigurations.core;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Provides a konfiguration source to a {@link Konfiguration}
 *
 * A config source must NOT be used directly, it must be supplied to {@link Konfiguration}.
 *
 * All the methods denoted with 'Thread-safe' in their comment section must be
 * implemented in a thread safe fashion.
 *
 * All the implementations of this interface (and for the love of god all the
 * values returned by methods of this interface) must be immutable.
 */
public interface KonfigSource {

    /**
     * Read and return a boolean value from this source.
     *
     * Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the source.
     * @throws KonfigurationMissingKeyException if the requested key is not present in this source.
     * @throws KonfigurationBadTypeException if the request key does not present the requested type (boolean).
     */
    Boolean bool(String key);

    /**
     * Read and return a int value from this source.
     *
     * Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the source.
     * @throws KonfigurationMissingKeyException if the requested key is not present in this source.
     * @throws KonfigurationBadTypeException if the request key does not present the requested type (int).
     */
    Integer int_(String key);

    /**
     * Read and return a long value from this source.
     *
     * Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the source.
     * @throws KonfigurationMissingKeyException if the requested key is not present in this source.
     * @throws KonfigurationBadTypeException if the request key does not present the requested type (long).
     */
    Long long_(String key);

    /**
     * Read and return a double value from this source.
     *
     * Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the source.
     * @throws KonfigurationMissingKeyException if the requested key is not present in this source.
     * @throws KonfigurationBadTypeException if the request key does not present the requested type (double).
     */
    Double double_(String key);

    /**
     * Read and return a string value from this source.
     *
     * Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @return the value of konfiguration represented by supplied key in the source.
     * @throws KonfigurationMissingKeyException if the requested key is not present in this source.
     * @throws KonfigurationBadTypeException if the request key does not present the requested type (string).
     */
    String string(String key);


    /**
     * Read and return a boolean value from this source.
     *
     * Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @param type type of elements in the list.
     * @param <T> generic type of elements in the list.
     * @return the value of konfiguration represented by supplied key in the source.
     * @throws KonfigurationMissingKeyException if the requested key is not present in this source.
     * @throws KonfigurationBadTypeException if the request key does not present the requested type (list of T).
     */
    <T> List<T> list(String key, Class<T> type);

    /**
     * Read and return a boolean value from this source.
     *
     * Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @param type type of values of the map.
     * @param <T> generic type of value elements in the map.
     * @return the value of konfiguration represented by supplied key in the source.
     * @throws KonfigurationMissingKeyException if the requested key is not present in this source.
     * @throws KonfigurationBadTypeException if the request key does not present the requested type (map from String to T).
     */
    <T> Map<String, T> map(String key, Class<T> type);

    /**
     * Read and return a boolean value from this source.
     *
     * Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @param type type of elements in the set.
     * @param <T> generic type of elements in the set.
     * @return the value of konfiguration represented by supplied key in the source.
     * @throws KonfigurationMissingKeyException if the requested key is not present in this source.
     * @throws KonfigurationBadTypeException if the request key does not present the requested type (set of T).
     */
    <T> Set<T> set(String key, Class<T> type);

    /**
     * Read and return a boolean value from this source.
     *
     * Thread-safe.
     *
     * @param key the unique key of the konfiguration being requested.
     * @param type type of the custom object requested.
     * @param <T> generic type of the custom object requested.
     * @return the value of konfiguration represented by supplied key in the source.
     * @throws KonfigurationMissingKeyException if the requested key is not present in this source.
     * @throws KonfigurationBadTypeException if the request key does not present the requested type (object of type T).
     */
    <T> T custom(String key, Class<T> type);


    /**
     * Check if requested key is present in this source.
     *
     * Thread-safe.
     *
     * @param key the key to check.
     * @return true if the requested key exists in this source.
     */
    boolean contains(String key);

    /**
     * Indicates whether if anything is actually updated in the origin of this
     * source (that is, the source returned by {@link #copy()} differs from this
     * source.
     *
     * This action must <b>NOT</b> modify this source.
     *
     * Thread-safe.
     *
     * @return true if the source obtained via {@link #copy()} will differ from
     * this source.
     */
    boolean isUpdatable();

    /**
     * Creates an <em>updated</em> copy of this source.
     *
     * A call to this method must <b>NOT</b> modify this source, but the newly
     * created source must contain the updated values.
     *
     * Thread-safe.
     *
     * @return an updated copy of this source.
     */
    KonfigSource copy();

}
