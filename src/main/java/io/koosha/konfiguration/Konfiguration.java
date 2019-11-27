package io.koosha.konfiguration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.prefs.Preferences;


/**
 * All methods are thread-safe (and should be implemented as such).
 */
@SuppressWarnings("unused")
public interface Konfiguration {

    /**
     * Get a boolean konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    K<Boolean> bool(String key);

    /**
     * Get a byte konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    K<Byte> byte_(String key);

    /**
     * Get a char konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    K<Character> char_(String key);

    /**
     * Get a short konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    K<Short> short_(String key);

    /**
     * Get an int konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    K<Integer> int_(String key);

    /**
     * Get a long konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    K<Long> long_(String key);

    /**
     * Get a float konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    K<Float> float_(String key);

    /**
     * Get a double konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    K<Double> double_(String key);

    /**
     * Get a string konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    K<String> string(String key);


    /**
     * Get a list of U konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of values in the list.
     * @param <U>  generic type of elements in the list.
     * @return konfiguration value wrapper for the requested key.
     */
    <U> K<List<U>> list(String key, T<List<U>> type);

    /**
     * Get a map of String to U konfiguration value.
     * <p>
     * Currently Keys are always of type string.
     *
     * <p>Thread-safe.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type generic type of map
     * @param <U>  generic type of map, the key type.
     * @param <V>  generic type of map, the value type.
     * @return konfiguration value wrapper for the requested key.
     */
    <U, V> K<Map<U, V>> map(String key, T<Map<U, V>> type);

    /**
     * Get a set of konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of values in the set.
     * @param <U>  generic type of elements in the set.
     * @return konfiguration value wrapper for the requested key.
     */
    <U> K<Set<U>> set(String key, T<Set<U>> type);

    /**
     * Get a custom object of type T konfiguration value.
     *
     * <p><b>Important:</b> the underlying konfiguration source must support
     * this!
     *
     * <p><b>Important:</b> this method must <em>NOT</em> be used to obtain
     * maps, lists or sets. Use the corresponding methods
     * {@link #map(String, T)}, {@link #list(String, T)} and
     * {@link #set(String, T)}.
     *
     * <p>Thread-safe
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of the requested value.
     * @param <U>  generic type of requested value.
     * @return konfiguration value wrapper for the requested key.
     */
    <U> K<U> custom(String key, T<U> type);


    /**
     * Get a subset view of this konfiguration representing all the values under
     * the namespace of supplied key.
     *
     * <p>Thread-safe.
     *
     * @param key the key to which the scope of returned konfiguration is
     *            limited.
     * @return a konfiguration whose scope is limited to the supplied key.
     */
    @SuppressWarnings("unused")
    Konfiguration subset(String key);

    /**
     * Check if {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    boolean contains(String key);

    /**
     * Name of this konfiguration. Helps with debugging and readability.
     *
     * @return Name of this configuration.
     */
    String getName();


    /**
     * Indicates whether if anything is actually updated in the origin of this
     * source.
     *
     * <p>This action must <b>NOT</b> modify the instance.</p>
     *
     * <p><b>VERY VERY IMPORTANT:</b> This method is to be called only from a
     * single thread, preferably by a manager class such as
     * {@link KonfigurationKombiner} or concurrency issues will arise.</p>
     *
     * <p>Why? To check and see if it's updatable, a source might ask it's
     * origin (a web url?) to get the new content, to compare with the old
     * content, and it asks it's origin for the new content once more, to
     * actually update the values. If this method is called during
     * KonfigurationKombiner is also calling it, this might interfere and lost
     * updates may happen.</p>
     *
     * <p>To help blocking issues, update() is allowed to block the current
     * thread, and update observers will continue to work in their own thread.
     * This mechanism also helps to notify them only after when <em>all</em>
     * the combined sources are updated.</p>
     * <p>
     * Is always false in a readonly konfig source.
     *
     * <p>NOT Thread-safe.
     *
     * @return true if the source obtained via {@link #update()} ()} will
     * differ from this source.
     */
    default boolean hasUpdate() {
        return false;
    }

    /**
     * Creates an <b>updated</b> copy of this source.
     *
     * <p>A call to this method must <b>NOT</b> modify the instance, but the
     * newly created source must contain the updated values.
     *
     * <p><b>NOT</b> Thread-safe
     *
     * <b>The exceptions to these rules, are <em>Combiners</em> which combine
     * multiple source into one:</b>
     * One implementation is {@link KonfigurationKombiner}
     * Update all the konfiguration values and notify the update observers.<br>
     * Important: the key observers might be notified <em>after</em> the
     * cache update, and it is implementation specific. So it's possible that a
     * call to {@link K#v()} returns the new value, while the observer is not
     * notified yet.
     * <p>
     * Had no effect in a readonly konifg source.
     *
     * @return an updated copy of this source, but not necessarily a new one:
     * can return this instance itself if no update is available.
     * @throws KfgException TODO
     */
    default Konfiguration update() {
        return readonly();
    }

    /**
     * In order to stop clients from calling update(), use this method and
     * obtain a readonly view of the updateable konfiguration.
     *
     * @return a readonly view of the konfiguration instance.
     */
    default Konfiguration readonly() {
        return this;
    }


    /**
     * Register a listener to be notified of any updates to this konfiguration.
     * register to empty key (that is "") to receive update on all keys.
     *
     * <p>Thread-safe.
     *
     * <p><b>IMPORTANT:</b> Do NOT just pass in lambdas, as this method stores
     * only weak references and the observer will be garbage collected. Keep a
     * reference to the observer yourself.
     *
     * @param observer the listener to register.
     * @return this.
     */
    Konfiguration register(KeyObserver observer);

    /**
     * De-Register a previously registered listener via
     * {@link #register(KeyObserver)}.
     * register to empty key (that is "") to receive update on all keys.
     *
     * <p>Thread-safe.
     *
     * @param observer the listener to de-register.
     * @return this.
     * @see #register(KeyObserver)
     */
    Konfiguration deregister(KeyObserver observer);


    // ========================================================================


    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0 first source
     * @return kombined sources.
     */
    static Konfiguration kombine(final Konfiguration k0) {
        return new KonfigurationKombiner("", k0);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0 first source
     * @param k1 second source
     * @return kombined sources.
     */
    static Konfiguration kombine(final Konfiguration k0,
                                 final Konfiguration k1) {
        return new KonfigurationKombiner("", k0, k1);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0 first source
     * @param k1 second source
     * @param k2 third source
     * @return kombined sources.
     */
    static Konfiguration kombine(final Konfiguration k0,
                                 final Konfiguration k1,
                                 final Konfiguration k2) {
        return new KonfigurationKombiner("", k0, k1, k2);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0      first source
     * @param k1      second source
     * @param k2      third source
     * @param sources rest of sources
     * @return kombined sources.
     */
    static Konfiguration kombine(final Konfiguration k0,
                                 final Konfiguration k1,
                                 final Konfiguration k2,
                                 final Konfiguration... sources) {
        return new KonfigurationKombiner("", k0, k1, k2, sources);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param sources sources to combine.
     * @return kombined sources.
     * @throws NullPointerException     if sources is null.
     * @throws IllegalArgumentException is sources is empty.
     */
    static Konfiguration kombine(final Collection<Konfiguration> sources) {
        return new KonfigurationKombiner("", sources);
    }


    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0 first source
     * @return kombined sources.
     */
    static Konfiguration kombine(final String name,
                                 final Konfiguration k0) {
        return new KonfigurationKombiner(name, k0);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0 first source
     * @param k1 second source
     * @return kombined sources.
     */
    static Konfiguration kombine(final String name,
                                 final Konfiguration k0,
                                 final Konfiguration k1) {
        return new KonfigurationKombiner(name, k0, k1);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0 first source
     * @param k1 second source
     * @param k2 third source
     * @return kombined sources.
     */
    static Konfiguration kombine(final String name,
                                 final Konfiguration k0,
                                 final Konfiguration k1,
                                 final Konfiguration k2) {
        return new KonfigurationKombiner(name, k0, k1, k2);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0      first source
     * @param k1      second source
     * @param k2      third source
     * @param sources rest of sources
     * @return kombined sources.
     */
    static Konfiguration kombine(final String name,
                                 final Konfiguration k0,
                                 final Konfiguration k1,
                                 final Konfiguration k2,
                                 final Konfiguration... sources) {
        return new KonfigurationKombiner(name, k0, k1, k2, sources);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param sources sources to combine.
     * @return kombined sources.
     * @throws NullPointerException     if sources is null.
     * @throws IllegalArgumentException is sources is empty.
     */
    static Konfiguration kombine(final String name,
                                 final Collection<Konfiguration> sources) {
        return new KonfigurationKombiner(name, sources);
    }


    // ============================================================= PLAIN JAVA

    /**
     * Creates a {@link KonfigSource} with the given backing store.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if storage is null.
     */
    static KonfigSource map(final Map<String, ?> storage) {
        return map("", storage);
    }

    /**
     * Important: {@link Supplier#get()} might be called multiple times in a
     * short period (once call to see if it's changed and if so, one mode call
     * to get the new values afterward.
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if provided storage provider is null
     * @throws KfgInSourceException if the provided storage by provider is null
     */
    static KonfigSource map(final Supplier<Map<String, ?>> storage) {
        return new KonfigSources.MapKonfiguration(storage);
    }


    static Konfiguration preferences(final Preferences preferences) {
        return new KonfigSources.PreferencesKonfiguration("", preferences, null);
    }

    // ================================================================ JACKSON

    /**
     * Creates a {@link KonfigSource} with the given json string as source.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param json backing store.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgInSourceException if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgInSourceException if the storage (json string) returned by json string is null.
     * @throws KfgInSourceException if the provided json string can not be parsed by jackson.
     * @throws KfgInSourceException if the the root element returned by jackson is null.
     */
    static KonfigSource jacksonJson(final String json) {
        return new KonfigSources.KonstJsonKonfigSource(json);
    }

    /**
     * Creates a {@link KonfigSource} with the given json string and object
     * mapper provider.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param json         backing store.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link KonfigSource#custom(String, Class)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgInSourceException if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgInSourceException if the storage (json string) returned by json string is null.
     * @throws KfgInSourceException if the provided json string can not be parsed by jackson.
     * @throws KfgInSourceException if the the root element returned by jackson is null.
     */
    static KonfigSource jacksonJson(String json, final Supplier<ObjectMapper> objectMapper) {
        return new KonfigSources.KonstJsonKonfigSource(json, objectMapper);
    }


    /**
     * Creates a {@link KonfigSource} with the given json provider and a
     * default object mapper provider.
     *
     * @param json backing store provider. Must always return a
     *             non-null valid json string.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgInSourceException if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgInSourceException if the storage (json string) returned by json string is null.
     * @throws KfgInSourceException if the provided json string can not be parsed by jackson.
     * @throws KfgInSourceException if the the root element returned by jackson is null.
     */
    static KonfigSource jacksonJson(final Supplier<String> json) {
        return new KonfigSources.JsonKonfigSource(json);
    }

    /**
     * Creates a {@link KonfigSource} with the given json provider and object
     * mapper provider.
     *
     * @param json         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link KonfigSource#custom(String, Class)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgInSourceException if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgInSourceException if the storage (json string) returned by json string is null.
     * @throws KfgInSourceException if the provided json string can not be parsed by jackson.
     * @throws KfgInSourceException if the the root element returned by jackson is null.
     */
    static KonfigSource jacksonJson(final Supplier<String> json, final Supplier<ObjectMapper> objectMapper) {
        return new KonfigSources.JsonKonfigSource(json, objectMapper);
    }


    // ============================================================= SNAKE YAML

    /**
     * Creates a {@link KonfigSource} with the given yaml string as source.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml backing store.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgInSourceException if snake yaml library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgInSourceException if the storage (json string) returned by json string is null.
     * @throws KfgInSourceException if the provided json string can not be parsed by jackson.
     * @throws KfgInSourceException if the the root element returned by jackson is null.
     */
    static KonfigSource snakeYaml(final String yaml) {
        return new KonfigSources.KonstSnakeYamlKonfigSource(yaml);
    }

    /**
     * Creates a {@link KonfigSource} with the given yaml string and object
     * mapper provider.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link KonfigSource#custom(String, Class)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgInSourceException if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgInSourceException if the storage (json string) returned by json string is null.
     * @throws KfgInSourceException if the provided json string can not be parsed by jackson.
     * @throws KfgInSourceException if the the root element returned by jackson is null.
     */
    static KonfigSource snakeYaml(String yaml, final Supplier<Yaml> objectMapper) {
        return new KonfigSources.KonstSnakeYamlKonfigSource(yaml, objectMapper);
    }


    /**
     * Creates a {@link KonfigSource} with the given yaml provider and a
     * default object mapper provider.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml backing store provider. Must always return a
     *             non-null valid json string.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgInSourceException if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgInSourceException if the storage (json string) returned by json string is null.
     * @throws KfgInSourceException if the provided json string can not be parsed by jackson.
     * @throws KfgInSourceException if the the root element returned by jackson is null.
     */
    static KonfigSource snakeYaml(final Supplier<String> yaml) {
        return new KonfigSources.SnakeYamlKonfigSource(yaml);
    }

    /**
     * Creates a {@link KonfigSource} with the given yaml provider and object
     * mapper provider.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link KonfigSource#custom(String, Class)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgInSourceException if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgInSourceException if the storage (json string) returned by json string is null.
     * @throws KfgInSourceException if the provided json string can not be parsed by jackson.
     * @throws KfgInSourceException if the the root element returned by jackson is null.
     */
    static KonfigSource snakeYaml(final Supplier<String> yaml, final Supplier<Yaml> objectMapper) {
        return new KonfigSources.SnakeYamlKonfigSource(yaml, objectMapper);
    }


}
