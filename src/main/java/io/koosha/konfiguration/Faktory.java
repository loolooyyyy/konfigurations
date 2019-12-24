package io.koosha.konfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.koosha.konfiguration.base.Deserializer;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import io.koosha.konfiguration.error.KfgSourceException;
import io.koosha.konfiguration.type.Q;
import io.koosha.konfiguration.v8.FaktoryV8;
import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableMap;

@SuppressWarnings("unused")
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public interface Faktory {

    String VERSION_1 = "1.0.0";

    String VERSION_8 = "8.0.0";

    String VERSION = VERSION_8;

    String DEFAULT_KONFIG_NAME = "io.koosha.konfiguration.Faktory.DEFAULT";

    @NotNull
    @Contract(pure = true)
    static Faktory implementationV8() {
        return FaktoryV8.defaultInstance();
    }

    @NotNull
    @Contract(pure = true)
    static Faktory defaultImplementation() {
        return implementationV8();
    }

    boolean ALLOW_MIXED_TYPES__DEFAULT = false;

    boolean FAIR_LOCk__DEFAULT = true;

    long LOCK_WAIT_MILLIS__DEFAULT = 300;
    AtomicBoolean UNSAFE_YAML = new AtomicBoolean(true);

    /**
     * Implementation version.
     *
     * @return implementation version.
     */
    @NotNull
    @Contract(pure = true)
    String getVersion();

    // =========================================================================

    @Contract("_ -> new")
    @NotNull
    KonfigurationBuilder builder(@NotNull final String name);

    @Contract("-> new")
    @NotNull
    default KonfigurationBuilder builder() {
        return this.builder(DEFAULT_KONFIG_NAME);
    }


    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0 first source
     * @return kombined sources.
     */
    @Contract("_, _ -> new")
    @NotNull
    default KonfigurationManager kombine(@NotNull final String name,
                                         @NotNull final KonfigurationManager k0) {
        return this.kombine(name, singleton(k0));
    }

    @NotNull
    @Contract("_, _, _ -> new")
    default KonfigurationManager kombine(@NonNull @NotNull final String name,
                                         @NonNull @NotNull final KonfigurationManager k0,
                                         @NonNull @NotNull final KonfigurationManager... sources) {
        final List<KonfigurationManager> l = new ArrayList<>();
        l.add(k0);
        l.addAll(asList(sources));
        return this.kombine(name, l);
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
    KonfigurationManager kombine(@NotNull String name,
                                 @NotNull Collection<KonfigurationManager> sources);

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0 first source
     * @return kombined sources.
     */
    @Contract("_ -> new")
    default KonfigurationManager kombine(@NotNull final KonfigurationManager k0) {
        return this.kombine(DEFAULT_KONFIG_NAME, k0);
    }

    /**
     * Create a new konfiguration object from given sources.
     *
     * @param k0      first source
     * @param sources rest of sources
     * @return kombined sources.
     */
    @Contract("_, _ -> new")
    default KonfigurationManager kombine(@NotNull final KonfigurationManager k0,
                                         @NotNull final KonfigurationManager... sources) {
        return this.kombine(DEFAULT_KONFIG_NAME, k0, sources);
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
    default KonfigurationManager kombine(@NotNull final Collection<KonfigurationManager> sources) {
        return this.kombine(DEFAULT_KONFIG_NAME, sources);
    }

    // =========================================================================

    /**
     * Creates a {@link KonfigurationManager} with the given backing store.
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
    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    KonfigurationManager map(@NotNull String name,
                             @NotNull Supplier<Map<String, ?>> storage);

    /**
     * Creates a {@link KonfigurationManager} with the given backing store.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param name    name of the created source.
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if storage is null.
     */
    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    default KonfigurationManager map(@NotNull String name,
                                     @NotNull final Map<String, ?> storage) {
        final Map<String, ?> copy = unmodifiableMap(new HashMap<>(storage));
        return map(name, () -> copy);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given backing store.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if storage is null.
     */
    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    default KonfigurationManager map(@NotNull final Map<String, ?> storage) {
        return this.map(DEFAULT_KONFIG_NAME, storage);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given backing store.
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
    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    default KonfigurationManager map(@NotNull final Supplier<Map<String, ?>> storage) {
        return this.map(DEFAULT_KONFIG_NAME, storage);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given backing store.
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
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    KonfigurationManager mapWithNested(@NotNull String name,
                                       @NotNull Supplier<Map<String, ?>> storage);

    /**
     * Creates a {@link KonfigurationManager} with the given backing store.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param name    name of the created source.
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if storage is null.
     */
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    default KonfigurationManager mapWithNested(@NotNull final String name,
                                               @NotNull final Map<String, ?> storage) {
        final Map<String, ?> copy = unmodifiableMap(new HashMap<>(storage));
        return mapWithNested(name, () -> copy);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given backing store.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if storage is null.
     */
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    default KonfigurationManager mapWithNested(@NotNull final Map<String, ?> storage) {
        return this.mapWithNested(DEFAULT_KONFIG_NAME, storage);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given backing store.
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
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    default KonfigurationManager mapWithNested(@NotNull final Supplier<Map<String, ?>> storage) {
        return this.mapWithNested(DEFAULT_KONFIG_NAME, storage);
    }

    // =========================================================================

    /**
     * Creates a {@link KonfigurationManager} with the given backing store.
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if provided storage provider is null
     * @throws KfgSourceException   if the provided storage by provider is null
     */
    @NotNull
    @Contract("_ -> new")
    default KonfigurationManager preferences(@NotNull final Preferences storage) {
        return this.preferences(DEFAULT_KONFIG_NAME, storage);
    }

    @NotNull
    @Contract("_, _ -> new")
    KonfigurationManager preferences(@NotNull String name,
                                     @NotNull Preferences storage);

    /**
     * Creates a {@link KonfigurationManager} with the given backing store.
     *
     * @param storage konfig source.
     * @return a konfig source.
     * @throws NullPointerException if provided storage provider is null
     * @throws KfgSourceException   if the provided storage by provider is null
     */
    @NotNull
    @Contract("_, _ -> new")
    default KonfigurationManager preferences(@NotNull final Preferences storage,
                                             @NotNull final Deserializer deser) {
        return this.preferences(DEFAULT_KONFIG_NAME, storage, deser);
    }

    @NotNull
    @Contract("_, _, _ -> new")
    KonfigurationManager preferences(@NotNull String name,
                                     @NotNull Preferences storage,
                                     @NotNull Deserializer deser);

    // =========================================================================

    /**
     * Creates a {@link KonfigurationManager} with the given json provider and a
     * default object mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
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
    @NotNull
    @Contract("_, _ -> new")
    KonfigurationManager jacksonJson(@NotNull String name,
                                     @NotNull Supplier<String> json);

    /**
     * Creates a {@link KonfigurationManager} with the given json string as source.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
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
    @NotNull
    @Contract("_ -> new")
    default KonfigurationManager jacksonJson_(@NotNull final String json) {
        return this.jacksonJson(DEFAULT_KONFIG_NAME, json);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given json string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param json         backing store.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _ -> new")
    default KonfigurationManager jacksonJson_(@NotNull final String json,
                                              @NotNull final Supplier<ObjectMapper> objectMapper) {
        return this.jacksonJson(DEFAULT_KONFIG_NAME, json, objectMapper);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given json provider and a
     * default object mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
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
    default KonfigurationManager jacksonJson_(@NotNull final Supplier<String> json) {
        return this.jacksonJson(DEFAULT_KONFIG_NAME, json);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given json provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * @param json         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _ -> new")
    default KonfigurationManager jacksonJson_(@NotNull final Supplier<String> json,
                                              @NotNull final Supplier<ObjectMapper> objectMapper) {
        return this.jacksonJson(DEFAULT_KONFIG_NAME, json, objectMapper);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given json string as source.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
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
    @NotNull
    @Contract("_, _ -> new")
    default KonfigurationManager jacksonJson(@NotNull final String name,
                                             @NotNull final String json) {
        return jacksonJson(name, () -> json);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given json string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param json         backing store.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    default KonfigurationManager jacksonJson(@NotNull final String name,
                                             @NotNull final String json,
                                             @NotNull final Supplier<ObjectMapper> objectMapper) {
        return jacksonJson(name, () -> json, objectMapper);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given json provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * @param json         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link ObjectMapper} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    KonfigurationManager jacksonJson(@NotNull String name,
                                     @NotNull Supplier<String> json,
                                     @NotNull Supplier<ObjectMapper> objectMapper);

    // =========================================================================

    /**
     * Creates a {@link KonfigurationManager} with the given yaml string as source.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
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
    @NotNull
    @Contract("_ -> new")
    default KonfigurationManager snakeYaml_(@NotNull final String yaml) {
        return this.snakeYaml(DEFAULT_KONFIG_NAME, yaml);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given yaml string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _ -> new")
    default KonfigurationManager snakeYaml_(@NotNull final String yaml,
                                            @NotNull final Supplier<Yaml> objectMapper) {
        return this.snakeYaml(DEFAULT_KONFIG_NAME, yaml, objectMapper);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given yaml provider and a
     * default object mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
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
    @NotNull
    @Contract("_ -> new")
    default KonfigurationManager snakeYaml_(@NotNull final Supplier<String> yaml) {
        return this.snakeYaml(DEFAULT_KONFIG_NAME, yaml);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given yaml provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _ -> new")
    default KonfigurationManager snakeYaml_(@NotNull final Supplier<String> yaml,
                                            @NotNull final Supplier<Yaml> objectMapper) {
        return this.snakeYaml(DEFAULT_KONFIG_NAME, yaml, objectMapper);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given yaml string as source.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
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
    @NotNull
    @Contract("_, _ -> new")
    default KonfigurationManager snakeYaml(@NotNull final String name,
                                           @NotNull final String yaml) {
        return snakeYaml(name, () -> yaml);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given yaml string and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    default KonfigurationManager snakeYaml(@NotNull final String name,
                                           @NotNull final String yaml,
                                           @NotNull final Supplier<Yaml> objectMapper) {
        return snakeYaml(name, () -> yaml, objectMapper);
    }

    /**
     * Creates a {@link KonfigurationManager} with the given yaml provider and a
     * default object mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
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
    @NotNull
    @Contract("_, _ -> new")
    KonfigurationManager snakeYaml(@NotNull String name,
                                   @NotNull Supplier<String> yaml);

    /**
     * Creates a {@link KonfigurationManager} with the given yaml provider and object
     * mapper provider.
     * <p>
     * When reading a custom type, if you do not provide the actual requested
     * type (instance of {@link Q}) the source will act as if it does not
     * contain that key.
     *
     * <b>Important: the source will NEVER update. It's a const source.</b>
     *
     * @param yaml         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper A {@link Yaml} provider. Must always return
     *                     a valid non-null ObjectMapper, and if required, it
     *                     ust be able to deserialize custom types, so that
     *                     {@link Konfiguration#custom(Q)} works as well.
     * @return a konfig source.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
     *                              for the class: "org.yaml.snakeyaml.Yaml".
     * @throws KfgSourceException   if the storage (json string) returned by json string is null.
     * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson is null.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    KonfigurationManager snakeYaml(@NotNull String name,
                                   @NotNull Supplier<String> yaml,
                                   @NotNull Supplier<Yaml> objectMapper);

    /**
     * Same as {@link #snakeYaml(String, String, Supplier)} but explicitly rejects
     * parameterized types.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    KonfigurationManager snakeYaml_safe(@NotNull String name,
                                        @NotNull Supplier<String> yaml,
                                        @NotNull Supplier<Yaml> objectMapper);

}
