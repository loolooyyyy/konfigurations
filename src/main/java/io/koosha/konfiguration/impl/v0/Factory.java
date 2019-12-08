package io.koosha.konfiguration.impl.v0;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.koosha.konfiguration.KfgIllegalStateException;
import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.Q;
import lombok.NonNull;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

@SuppressWarnings("unused")
@ThreadSafe
@Immutable
final class Factory {

    private static final boolean J9;

    static {
        boolean j;
        try {
            j = !System.getProperty("java.version").startsWith("1.");
        }
        catch (final Throwable t) {
            j = false;
        }
        J9 = j;
    }

    @SuppressWarnings("Since15")
    @NotNull
    @Contract(pure = true)
    static <K, V> Map<K, V> map(K k1, V v1) {
        return J9 ? Map.of(k1, v1) : Collections.singletonMap(k1, v1);
    }

    @SuppressWarnings("Since15")
    @NotNull
    @Contract(pure = true)
    static <K, V> Map<K, V> map(K k1, V v1, K k2, V v2) {
        if (J9)
            return Map.of(k1, v1, k2, v2);
        final Map<K, V> m = new HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return Collections.unmodifiableMap(m);
    }

    @SuppressWarnings("Since15")
    @NotNull
    @Contract(pure = true)
    static <K, V> Map<K, V> map(K k1, V v1, K k2, V v2, K k3, V v3) {
        if (J9)
            return Map.of(k1, v1, k2, v2, k3, v3);
        final Map<K, V> m = new HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        m.put(k3, v3);
        return Collections.unmodifiableMap(m);
    }

    @SuppressWarnings("Since15")
    @NotNull
    @Contract(pure = true)
    static <K, V> Map<K, V> map(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        if (J9)
            return Map.of(k1, v1, k2, v2, k3, v3, k4, v4);
        final Map<K, V> m = new HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        m.put(k3, v3);
        m.put(k4, v4);
        return Collections.unmodifiableMap(m);
    }

    static String msgOf(final Throwable t) {
        return t == null
               ? "[null exception]->[null exception]"
               : format("[throwable::%s]->[%s]", t.getClass().getName(), t.getMessage());
    }

    static String toStringOf(final Object value) {
        String representationC;
        try {
            representationC = value == null ? "null" : value.getClass().getName();
        }
        catch (Throwable t) {
            representationC = "[" + "value.getClass().getName()" + "]->" + msgOf(t);
        }

        String representationV;
        try {
            representationV = Objects.toString(value);
        }
        catch (Throwable t) {
            representationV = "[" + "Objects.toString(value)" + "]->" + msgOf(t);
        }

        return format("[%s]:[%s]", representationC, representationV);
    }


    private Factory() {
    }

    private static final Factory INSTANCE = new Factory();

    @Contract(pure = true)
    @NotNull
    static Factory defaultInstance() {
        return Factory.INSTANCE;
    }

    /**
     * Implementation version.
     *
     * @return implementation version.
     */
    @Contract(pure = true)
    @NotNull
    public String getVersion() {
        return ExportV0.getVersion();
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0      first source
     * @param sources rest of sources
     * @return kombined sources.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    public Konfiguration kombine(@NonNull @NotNull final String name,
                                 @NonNull @NotNull final Konfiguration k0,
                                 @NonNull @NotNull final Konfiguration... sources) {
        final List<Konfiguration> l = new ArrayList<>(singleton(k0));
        l.addAll(asList(sources));
        return new Kombiner(name, false, l, Kombiner.LOCK_WAIT_MILLIS__DEFAULT);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param sources sources to combine.
     * @return kombined sources.
     * @throws NullPointerException     if sources is null.
     * @throws KfgIllegalStateException is sources is empty.
     */
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration kombine(@NotNull @NonNull final String name,
                                 @NonNull @NotNull final Collection<Konfiguration> sources) {
        return new Kombiner(name, false, sources, Kombiner.LOCK_WAIT_MILLIS__DEFAULT);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0 first source
     * @return kombined sources.
     */
    @Contract("_, _ -> new")
    public Konfiguration kombine(@NotNull @NonNull final String name,
                                 @NotNull @NonNull final Konfiguration k0) {
        return kombine(name, k0, new Konfiguration[0]);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0 first source
     * @return kombined sources.
     */
    @Contract("_ -> new")
    public Konfiguration kombine(@NotNull @NonNull final Konfiguration k0) {
        return kombine("", k0);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0      first source
     * @param sources rest of sources
     * @return kombined sources.
     */
    @Contract("_, _ -> new")
    public Konfiguration kombine(@NotNull @NonNull final Konfiguration k0,
                                 @NotNull @NonNull final Konfiguration... sources) {
        return kombine("", k0, sources);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param sources sources to combine.
     * @return kombined sources.
     * @throws NullPointerException     if sources is null.
     * @throws KfgIllegalStateException is sources is empty.
     */
    @Contract("_ -> new")
    public Konfiguration kombine(@NotNull @NonNull final Collection<Konfiguration> sources) {
        return kombine("", sources);
    }


    // ============================================================= PLAIN JAVA


    /**
     * Creates a {@link Konfiguration} with the given backing store.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param name    name of the created source.
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if storage is null.
     */
    @NotNull
    @Contract("_, _ -> new")
    public Konfiguration map(@NotNull @NonNull final String name,
                             @NotNull @NonNull final Map<String, ?> storage) {
        final Storage<String> s = Storage.fromMap(name, storage);
        return new StorageSource(name, true, () -> s);
    }

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     * <p>
     * Important: {@link Supplier#get()} might be called multiple times in a
     * short period (once call to see if it's changed and if so, one mode call
     * to get the new values afterward.
     *
     * @param name    name of the created source.
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if provided storage provider is null
     * @throws KfgSourceException   if the provided storage by provider is null
     */
    public Konfiguration map(@NotNull @NonNull final String name,
                             @NotNull @NonNull final Supplier<Map<String, ?>> storage) {
        return kombine(name, new StorageSource(name, false, () -> Storage.fromMap(name, storage.get())));
    }


    /**
     * Creates a {@link Konfiguration} with the given backing store.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if storage is null.
     */
    @Contract("_ -> new")
    public Konfiguration map(@NotNull @NonNull final Map<String, ?> storage) {
        return map("", storage);
    }

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     * <p>
     * Important: {@link Supplier#get()} might be called multiple times in a
     * short period (once call to see if it's changed and if so, one mode call
     * to get the new values afterward.
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if provided storage provider is null
     * @throws KfgSourceException   if the provided storage by provider is null
     */
    @Contract("_ -> new")
    public Konfiguration map(@NotNull @NonNull final Supplier<Map<String, ?>> storage) {
        return map("", storage);
    }

    /**
     * Creates a {@link Konfiguration} with the given backing store.
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if provided storage provider is null
     * @throws KfgSourceException   if the provided storage by provider is null
     */
    @Contract("_ -> new")
    public Konfiguration preferences(@NotNull @NonNull final Preferences storage) {
        return preferences("", storage);
    }

    public Konfiguration preferences(@NotNull @NonNull final String name,
                                     @NotNull @NonNull final Preferences storage) {
        final Konfiguration k =
                new ExtPreferencesSource(name, false, storage, null);
        return kombine(name, k);
    }

    // ================================================================ JACKSON

    /**
     * Creates a {@link Konfiguration} with the given json provider and a
     * default object mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * @param json backing store provider. Must always return a
     *             non-null valid json string.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration jacksonJson(@NotNull @NonNull final String name,
                                     @NotNull @NonNull final Supplier<String> json) {
        return kombine(name, new ExtJacksonJsonSource(name, false, json));
    }

    /**
     * Creates a {@link Konfiguration} with the given json provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * @param json         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration jacksonJson(@NotNull @NonNull final String name,
                                     @NotNull @NonNull final Supplier<String> json,
                                     @NonNull @NotNull final Supplier<ObjectMapper> objectMapper) {
        return kombine(name, new ExtJacksonJsonSource(name, false, json, objectMapper));
    }

    /**
     * Creates a {@link Konfiguration} with the given json string as source.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param json backing store.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @Contract("_ -> new")
    public Konfiguration jacksonJson_(@NotNull @NonNull final String json) {
        return jacksonJson("", json);
    }

    /**
     * Creates a {@link Konfiguration} with the given json string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param json         backing store.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration jacksonJson_(@NonNull @NotNull final String json,
                                      @NonNull @NotNull final Supplier<ObjectMapper> objectMapper) {
        return jacksonJson("", json, objectMapper);
    }

    /**
     * Creates a {@link Konfiguration} with the given json provider and a
     * default object mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * @param json backing store provider. Must always return a
     *             non-null valid json string.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @Contract("_ -> new")
    public Konfiguration jacksonJson_(@NotNull @NonNull final Supplier<String> json) {
        return jacksonJson("", json);
    }

    /**
     * Creates a {@link Konfiguration} with the given json provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * @param json         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration jacksonJson_(@NotNull @NonNull final Supplier<String> json,
                                      @NotNull @NonNull final Supplier<ObjectMapper> objectMapper) {
        return jacksonJson("", json, objectMapper);
    }

    /**
     * Creates a {@link Konfiguration} with the given json string as source.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param json backing store.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration jacksonJson(@NotNull @NonNull final String name,
                                     @NotNull @NonNull final String json) {
        return jacksonJson(name, () -> json);
    }

    /**
     * Creates a {@link Konfiguration} with the given json string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param json         backing store.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration jacksonJson(@NotNull @NonNull final String name,
                                     @NotNull @NonNull final String json,
                                     @NonNull @NotNull final Supplier<ObjectMapper> objectMapper) {
        return jacksonJson(name, () -> json, objectMapper);
    }

    // ============================================================= SNAKE YAML

    /**
     * Creates a {@link Konfiguration} with the given yaml string as source.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * @param yaml backing store.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if snake yaml library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration snakeYaml_(@NotNull @NonNull final String yaml) {
        return snakeYaml("", yaml);
    }

    /**
     * Creates a {@link Konfiguration} with the given yaml string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration snakeYaml_(@NotNull @NonNull final String yaml,
                                    @NotNull @NonNull final Supplier<Yaml> objectMapper) {
        return snakeYaml("", yaml, objectMapper);
    }

    /**
     * Creates a {@link Konfiguration} with the given yaml provider and a
     * default object mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml backing store provider. Must always return a
     *             non-null valid json string.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration snakeYaml_(@NotNull @NonNull final Supplier<String> yaml) {
        return snakeYaml("", yaml);
    }

    /**
     * Creates a {@link Konfiguration} with the given yaml provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration snakeYaml_(@NotNull @NonNull final Supplier<String> yaml,
                                    @NotNull @NonNull final Supplier<Yaml> objectMapper) {
        return snakeYaml("", yaml, objectMapper);
    }

    /**
     * Creates a {@link Konfiguration} with the given yaml string as source.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml backing store.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if snake yaml library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration snakeYaml(@NotNull @NonNull final String name,
                                   @NotNull @NonNull final String yaml) {
        return snakeYaml(name, () -> yaml);
    }

    /**
     * Creates a {@link Konfiguration} with the given yaml string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration snakeYaml(@NonNull @NotNull final String name,
                                   @NotNull @NonNull final String yaml,
                                   @NotNull @NonNull final Supplier<Yaml> objectMapper) {
        return snakeYaml(name, () -> yaml, objectMapper);
    }

    /**
     * Creates a {@link Konfiguration} with the given yaml provider and a
     * default object mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml backing store provider. Must always return a
     *             non-null valid json string.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration snakeYaml(@NotNull @NonNull final String name,
                                   @NotNull @NonNull final Supplier<String> yaml) {
        return kombine(name, new ExtYamlSource(name, false, yaml));
    }

    /**
     * Creates a {@link Konfiguration} with the given yaml provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) this source will act as if it does not
     * contain that key.
     *
     * <b>Important: this source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(String, Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    public Konfiguration snakeYaml(@NotNull @NonNull final String name,
                                   @NotNull @NonNull final Supplier<String> yaml,
                                   @NonNull @NotNull final Supplier<Yaml> objectMapper) {
        return kombine(name, new ExtYamlSource(name, false, yaml, objectMapper));
    }

}
