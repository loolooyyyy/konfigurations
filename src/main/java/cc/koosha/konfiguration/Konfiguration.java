package cc.koosha.konfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;


@SuppressWarnings("unused")
public interface Konfiguration {

    /**
     * Get a boolean konfiguration value.
     *
     * Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     * @throws KonfigurationBadTypeException if the requested key does not match the requested type.
     * @throws KonfigurationMissingKeyException if the requested key does not exist in any source.
     */
    KonfigV<Boolean> bool(String key);

    /**
     * Get an int konfiguration value.
     *
     * Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     * @throws KonfigurationBadTypeException if the requested key does not match the requested type.
     * @throws KonfigurationMissingKeyException if the requested key does not exist in any source.
     */
    KonfigV<Integer> int_(String key);

    /**
     * Get a long konfiguration value.
     *
     * Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     * @throws KonfigurationBadTypeException if the requested key does not match the requested type.
     * @throws KonfigurationMissingKeyException if the requested key does not exist in any source.
     */
    KonfigV<Long> long_(String key);

    /**
     * Get a double konfiguration value.
     *
     * Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     * @throws KonfigurationBadTypeException if the requested key does not match the requested type.
     * @throws KonfigurationMissingKeyException if the requested key does not exist in any source.
     */
    KonfigV<Double> double_(String key);

    /**
     * Get a string konfiguration value.
     *
     * Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     * @throws KonfigurationBadTypeException if the requested key does not match the requested type.
     * @throws KonfigurationMissingKeyException if the requested key does not exist in any source.
     */
    KonfigV<String> string(String key);

    /**
     * Get a list of T konfiguration value.
     *
     * Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @param type type object of values in the list.
     * @param <T> generic type of elements in the list.
     * @return konfiguration value wrapper for the requested key.
     * @throws KonfigurationBadTypeException if the requested key does not match the requested type.
     * @throws KonfigurationMissingKeyException if the requested key does not exist in any source.
     */
    <T> KonfigV<List<T>> list(String key, Class<T> type);

    /**
     * Get a map of String to T konfiguration value. Keys are always of type
     * string.
     *
     * Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @param type type object of map values. (keys are always of type String.class).
     * @param <T> generic type of elements in the map (for map values).
     * @return konfiguration value wrapper for the requested key.
     * @throws KonfigurationBadTypeException if the requested key does not match the requested type.
     * @throws KonfigurationMissingKeyException if the requested key does not exist in any source.
     */
    <T> KonfigV<Map<String, T>> map(String key, Class<T> type);

    /**
     * Get a set of T konfiguration value.
     *
     * Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @param type type object of values in the set.
     * @param <T> generic type of elements in the set.
     * @return konfiguration value wrapper for the requested key.
     * @throws KonfigurationBadTypeException if the requested key does not match the requested type.
     * @throws KonfigurationMissingKeyException if the requested key does not exist in any source.
     */
    <T> KonfigV<Set<T>> set(String key, Class<T> type);

    /**
     * Get a custom object of type T konfiguration value.
     *
     * Thread-safe.
     *
     * <b>Important:</b> the underlying konfiguration source must support this!
     *
     * <b>Important:</b> this method must <em>NOT</em> be used to obtain maps,
     * lists or sets. use the corresponding methods {@link #map(String, Class)},
     * {@link #list(String, Class)} and {@link #set(String, Class)}.
     *
     * @param key unique key of the konfiguration being requested.
     * @param type type object of the requested value.
     * @param <T> generic type of requested value.
     * @return konfiguration value wrapper for the requested key.
     * @throws KonfigurationBadTypeException if the requested key does not match the requested type.
     * @throws KonfigurationMissingKeyException if the requested key does not exist in any source.
     */
    <T> KonfigV<T> custom(String key, Class<T> type);



    /**
     * Same as {@link #bool(String)} but does not require the key to exist.
     * (so default values can be used).
     *
     * @param key key
     * @return r
     */
    KonfigV<Boolean> boolD(String key);

    /**
     * Same as {@link #int_(String)} but does not require the key to exist.
     * (so default values can be used).
     *
     * @param key key
     * @return r
     */
    KonfigV<Integer> intD(String key);

    /**
     * Same as {@link #long_(String)} but does not require the key to exist.
     * (so default values can be used).
     *
     * @param key key
     * @return r
     */
    KonfigV<Long> longD(String key);

    /**
     * Same as {@link #double_(String)} but does not require the key to exist.
     * (so default values can be used).
     *
     * @param key key
     * @return r
     */
    KonfigV<Double> doubleD(String key);

    /**
     * Same as {@link #string(String)} but does not require the key to exist.
     * (so default values can be used).
     *
     * @param key key
     * @return r
     */
    KonfigV<String> stringD(String key);

    /**
     * Same as {@link #list(String, Class)} but does not require the key to exist.
     * (so default values can be used).
     *
     * @param key key
     * @param type type
     * @param <T> t
     * @return r
     */
    <T> KonfigV<List<T>> listD(String key, Class<T> type);

    /**
     * Same as {@link #map(String, Class)} but does not require the key to exist.
     * (so default values can be used).
     *
     * @param key key
     * @param type type
     * @param <T> t
     * @return r
     */
    <T> KonfigV<Map<String, T>> mapD(String key, Class<T> type);

    /**
     * Same as {@link #set(String, Class)} but does not require the key to exist.
     * (so default values can be used).
     *
     * @param key key
     * @param type type
     * @param <T> t
     * @return r
     */
    <T> KonfigV<Set<T>> setD(String key, Class<T> type);

    /**
     * Same as {@link #custom(String, Class)} but does not require the key to exist
     * (so default values can be used).
     *
     * @param key key
     * @param type type
     * @param <T> t
     * @return r
     */
    <T> KonfigV<T> customD(String key, Class<T> type);





    /**
     * Update all the konfiguration values and notify the update observers.
     *
     * <b>Not thread safe by itself!!!</b> this is not necessarily thread safe
     * and must not be called from multiple threads, but calling it does not
     * compromise thread safety of other methods.
     *
     * @return true if anything was changed during this update.
     */
    boolean update();

    /**
     * Get a subset view of this konfiguration representing all the values under
     * the namespace of supplied key.
     *
     * Thread-safe.
     *
     * @param key the key to which the scope of returned konfiguration is limited.
     * @return a konfiguration whose scope is limited to the supplied key.
     */
    Konfiguration subset(String key);

    /**
     * Register a listener to be notified of any updates to this konfiguration.
     *
     * Thread-safe.
     *
     * @param observer the listener to register.
     * @return this.
     */
    Konfiguration register(EverythingObserver observer);

    /**
     * Register a previously registered listener via {@link #register(EverythingObserver)}
     *
     * Thread-safe.
     *
     * @param observer the listener to de-register.
     * @return this.
     */
    Konfiguration deregister(EverythingObserver observer);

}
