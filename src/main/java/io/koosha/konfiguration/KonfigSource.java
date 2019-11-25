package io.koosha.konfiguration;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.koosha.konfiguration.TypeName.*;
import static io.koosha.konfiguration.TypeName.MAP;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;


/**
 * Provides a konfiguration source to a {@link Konfiguration}
 *
 * <p>A config source must NOT be used directly, it must be supplied to
 * {@link Konfiguration}.<br>
 * Also see {@link #isUpdatable()}.
 *
 * <p>All the methods denoted with 'Thread-safe' in their comment section must be
 * implemented in a thread safe fashion.
 *
 * <p>All the implementations of this interface (and for the love of god all the
 * values returned by methods of this interface) must be immutable.
 *
 * <b>Important</b>: there is no {@code update()} method and there wont be.
 * there's only a {@link #copyAndUpdate()}, which updates too but does not modify the
 * original class.
 */
public interface KonfigSource {

    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (boolean).
     */
    Boolean bool(String key);

    /**
     * Read and return a int value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (int).
     */
    Integer int_(String key);

    /**
     * Read and return a long value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (long).
     */
    Long long_(String key);

    /**
     * Read and return a double value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (double).
     */
    Double double_(String key);

    /**
     * Read and return a string value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (string).
     */
    String string(String key);


    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     * @param type
     *         type of elements in the list.
     * @param <T>
     *         generic type of elements in the list.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (list of
     *         T).
     */
    <T> List<T> list(String key, Class<T> type);

    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     * @param type
     *         type of values of the map.
     * @param <T>
     *         generic type of value elements in the map.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (map from
     *         String to T).
     */
    <T> Map<String, T> map(String key, Class<T> type);

    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     * @param type
     *         type of elements in the set.
     * @param <T>
     *         generic type of elements in the set.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (set of
     *         T).
     */
    <T> Set<T> set(String key, Class<T> type);

    /**
     * Read and return a boolean value from this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the unique key of the konfiguration being requested.
     * @param type
     *         type of the custom object requested.
     * @param <T>
     *         generic type of the custom object requested.
     *
     * @return the value of konfiguration represented by supplied key in the
     * source.
     *
     * @throws KonfigurationMissingKeyException
     *         if the requested key is not present in this source.
     * @throws KonfigurationTypeException
     *         if the request key does not present the requested type (object
     *         of type T).
     */
    <T> T custom(String key, Class<T> type);


    /**
     * Check if requested key is present in this source.
     *
     * <p>Thread-safe.
     *
     * @param key
     *         the key to check.
     *
     * @return true if the requested key exists in this source.
     */
    boolean contains(String key);


    /**
     * Indicates whether if anything is actually updated in the origin of this
     * source (that is, the source returned by {@link #copyAndUpdate()} differs
     * from this source.)
     *
     * <p>This action must <b>NOT</b> modify this source.
     *
     * <p><b>VERY VERY IMPORTANT:</b> This method is to be called only from
     * {@link KonfigurationKombiner} or concurrency issues will arise.
     *
     * <br>Why? To check and see if it's updatable, a source might ask it's
     * origin (a web url?) to get the new content, to compare with the old
     * content, and it asks it's origin for the new content once more, to
     * actually update the values. If this method is called during
     * KonfigurationKombiner is also calling it, this might interfere and lost
     * updates may happen.
     *
     * <p>NOT Thread-safe.
     *
     * @return true if the source obtained via {@link #copyAndUpdate()} will
     * differ from this source.
     */
    boolean isUpdatable();

    /**
     * Creates an <b>updated</b> copy of this source.
     *
     * <p>A call to this method must <b>NOT</b> modify this source, but the
     * newly created source must contain the updated values.
     *
     * <p>NOT Thread-safe.
     *
     * @return an updated copy of this source.
     */
    KonfigSource copyAndUpdate();

    // ============================================================= PLAIN JAVA

    /**
     * Creates a {@link KonfigSource} with the given backing store.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param storage
     *         konfig source.
     *
     * @throws NullPointerException
     *         if storage is null.
     *
     * @return a konfig source.
     */
    public static KonfigSource inMemory(final Map<String, Object> storage) {
        return new KonfigSources.KonstInMemoryKonfigSource(storage);
    }

    /**
     * Important: {@link Supplier#get()} might be called multiple times in a
     * short period (once call to see if it's changed and if so, one mode call
     * to get the new values afterward.
     *
     * @param storage konfig source.
     *
     * @throws NullPointerException
     *         if provided storage provider is null
     * @throws KonfigurationSourceException
     *         if the provided storage by provider is null
     *
     * @return a konfig source.
     */
    public static KonfigSource inMemory(final Supplier<Map<String, Object>> storage) {
        return new KonfigSources.InMemoryKonfigSource(storage);
    }


    public static KonfigSource preferences(final Preferences preferences, final Deserializer<String> deserializer) {
        return new KonfigSources.PreferencesKonfigSource(preferences, deserializer);
    }

    public static KonfigSource preferences(final Preferences preferences) {
        return new KonfigSources.PreferencesKonfigSource(preferences);
    }


    // ================================================================ JACKSON

    /**
     * Creates a {@link KonfigSource} with the given json string as source.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param json         backing store.
     *
     * @throws NullPointerException         if any of its arguments are null.
     * @throws KonfigurationSourceException if jackson library is not in the classpath. it specifically looks
     *                                      for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KonfigurationSourceException if the storage (json string) returned by json string is null.
     * @throws KonfigurationSourceException if the provided json string can not be parsed by jackson.
     * @throws KonfigurationSourceException if the the root element returned by jackson is null.
     *
     * @return a konfig source.
     */
    public static KonfigSource jacksonJson(final String json) {
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
     *
     * @throws NullPointerException         if any of its arguments are null.
     * @throws KonfigurationSourceException if jackson library is not in the classpath. it specifically looks
     *                                      for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KonfigurationSourceException if the storage (json string) returned by json string is null.
     * @throws KonfigurationSourceException if the provided json string can not be parsed by jackson.
     * @throws KonfigurationSourceException if the the root element returned by jackson is null.
     *
     * @return a konfig source.
     */
    public static KonfigSource jacksonJson(String json, final Supplier<ObjectMapper> objectMapper) {
        return new KonfigSources.KonstJsonKonfigSource(json, objectMapper);
    }


    /**
     * Creates a {@link KonfigSource} with the given json provider and a
     * default object mapper provider.
     *
     * @param json         backing store provider. Must always return a
     *                     non-null valid json string.
     *
     * @throws NullPointerException         if any of its arguments are null.
     * @throws KonfigurationSourceException if jackson library is not in the classpath. it specifically looks
     *                                      for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KonfigurationSourceException if the storage (json string) returned by json string is null.
     * @throws KonfigurationSourceException if the provided json string can not be parsed by jackson.
     * @throws KonfigurationSourceException if the the root element returned by jackson is null.
     *
     * @return a konfig source.
     */
    public static KonfigSource jacksonJson(final Supplier<String> json) {
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
     *
     * @throws NullPointerException         if any of its arguments are null.
     * @throws KonfigurationSourceException if jackson library is not in the classpath. it specifically looks
     *                                      for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KonfigurationSourceException if the storage (json string) returned by json string is null.
     * @throws KonfigurationSourceException if the provided json string can not be parsed by jackson.
     * @throws KonfigurationSourceException if the the root element returned by jackson is null.
     *
     * @return a konfig source.
     */
    public static KonfigSource jacksonJson(final Supplier<String> json, final Supplier<ObjectMapper> objectMapper) {
        return new KonfigSources.JsonKonfigSource(json, objectMapper);
    }


    // ============================================================= SNAKE YAML

    /**
     * Creates a {@link KonfigSource} with the given yaml string as source.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store.
     *
     * @throws NullPointerException         if any of its arguments are null.
     * @throws KonfigurationSourceException if snake yaml library is not in the classpath. it specifically looks
     *                                      for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KonfigurationSourceException if the storage (json string) returned by json string is null.
     * @throws KonfigurationSourceException if the provided json string can not be parsed by jackson.
     * @throws KonfigurationSourceException if the the root element returned by jackson is null.
     *
     * @return a konfig source.
     */
    public static KonfigSource snakeYaml(final String yaml) {
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
     *
     * @throws NullPointerException         if any of its arguments are null.
     * @throws KonfigurationSourceException if jackson library is not in the classpath. it specifically looks
     *                                      for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KonfigurationSourceException if the storage (json string) returned by json string is null.
     * @throws KonfigurationSourceException if the provided json string can not be parsed by jackson.
     * @throws KonfigurationSourceException if the the root element returned by jackson is null.
     *
     * @return a konfig source.
     */
    public static KonfigSource snakeYaml(String yaml, final Supplier<Yaml> objectMapper) {
        return new KonfigSources.KonstSnakeYamlKonfigSource(yaml, objectMapper);
    }


    /**
     * Creates a {@link KonfigSource} with the given yaml provider and a
     * default object mapper provider.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store provider. Must always return a
     *                     non-null valid json string.
     *
     * @throws NullPointerException         if any of its arguments are null.
     * @throws KonfigurationSourceException if jackson library is not in the classpath. it specifically looks
     *                                      for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KonfigurationSourceException if the storage (json string) returned by json string is null.
     * @throws KonfigurationSourceException if the provided json string can not be parsed by jackson.
     * @throws KonfigurationSourceException if the the root element returned by jackson is null.
     *
     * @return a konfig source.
     */
    public static KonfigSource snakeYaml(final Supplier<String> yaml) {
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
     *
     * @throws NullPointerException         if any of its arguments are null.
     * @throws KonfigurationSourceException if jackson library is not in the classpath. it specifically looks
     *                                      for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KonfigurationSourceException if the storage (json string) returned by json string is null.
     * @throws KonfigurationSourceException if the provided json string can not be parsed by jackson.
     * @throws KonfigurationSourceException if the the root element returned by jackson is null.
     *
     * @return a konfig source.
     */
    public static KonfigSource snakeYaml(final Supplier<String> yaml, final Supplier<Yaml> objectMapper) {
        return new KonfigSources.SnakeYamlKonfigSource(yaml, objectMapper);
    }

}
