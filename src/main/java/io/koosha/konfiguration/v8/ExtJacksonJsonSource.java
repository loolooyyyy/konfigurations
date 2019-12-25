package io.koosha.konfiguration.v8;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import io.koosha.konfiguration.base.UpdatableSource;
import io.koosha.konfiguration.base.UpdatableSourceBase;
import io.koosha.konfiguration.error.*;
import io.koosha.konfiguration.error.extended.KfgJacksonError;
import io.koosha.konfiguration.type.Q;
import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.experimental.Accessors;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Reads konfig from a json source (supplied as string).
 *
 * <p>for {@link #custom(Q)} to work, the supplied json reader must be
 * configured to handle arbitrary types accordingly.
 *
 * <p>Thread safe and immutable.
 */
@Immutable
@ThreadSafe
@ApiStatus.Internal
final class ExtJacksonJsonSource extends UpdatableSourceBase {

    private final Supplier<ObjectMapper> mapperSupplier;
    private final Supplier<String> json;
    private final int lastHash;
    private final JsonNode root;

    @NonNull
    @NotNull
    @Getter
    @Accessors(fluent = true)
    private final String name;

    /**
     * Creates an instance with a with the given json
     * provider and object mapper provider.
     *
     * @param name         name of this source
     * @param json         backing store provider. Must always return a
     *                     non-null valid json string.
     * @param objectMapper {@link ObjectMapper} provider. Must always return a
     *                     valid non-null ObjectMapper, and if required, it must
     *                     be able to deserialize custom types, so that
     *                     {@link #custom(Q)} works as well.
     *
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSourceException   if jackson library is not in the classpath.
     *                              it specifically looks for the class:
     *                              "com.fasterxml.jackson.databind.JsonNode"
     * @throws KfgSourceException   if the storage (json string) returned by
     *                              json string is null.
     * @throws KfgSourceException   if the provided json string can not be
     *                              parsed by jackson.
     * @throws KfgSourceException   if the the root element returned by jackson
     *                              is null.
     */
    ExtJacksonJsonSource(@NotNull @NonNull final String name,
                         @NotNull @NonNull final Supplier<String> json,
                         @NonNull @NotNull final Supplier<ObjectMapper> objectMapper) {
        this.name = name;
        // Check early, so we're not fooled with a dummy object reader.
        ensureDep(name);

        this.json = json;
        this.mapperSupplier = objectMapper;

        requireNonNull(this.json.get(), "supplied json is null");
        requireNonNull(this.mapperSupplier.get(), "supplied mapper is null");

        final JsonNode update;
        try {
            update = this.mapperSupplier.get().readTree(this.json.get());
        }
        catch (final IOException e) {
            // XXX
            throw new KfgJacksonError(this.name(), "error parsing json string", e);
        }

        requireNonNull(update, "root element is null");

        this.root = update;
        this.lastHash = this.json.get().hashCode();
    }

    @Contract(pure = true,
            value = "->new")
    @NotNull
    static ObjectMapper defaultJacksonObjectMapper() {
        ensureDep(null);
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    private static void ensureDep(@Nullable final String source) {
        try {
            Class.forName("com.fasterxml.jackson.databind.JsonNode");
        }
        catch (final ClassNotFoundException e) {
            throw new KfgUnsupportedOperationException(source,
                    "jackson library missing: com.fasterxml.jackson.databind.JsonNode",
                    e);
        }
    }

    @Contract(pure = true)
    @Override
    public boolean hasUpdate() {
        final String newJson = this.json.get();
        return newJson != null && newJson.hashCode() != this.lastHash;
    }

    @Contract(pure = true,
            value = "-> new")
    @NotNull
    @Override
    public UpdatableSource updatedSelf() {
        return this.hasUpdate()
               ? new ExtJacksonJsonSource(this.name(), this.json, this.mapperSupplier)
               : this;
    }

    private JsonNode node_(@NonNull @NotNull final String key) {
        if (key.isEmpty())
            throw new KfgIllegalArgumentException(this.name(), "empty konfig key");

        //noinspection HardcodedFileSeparator
        final String saneKey = key.replace('.', '/');
        return this.root.findPath(saneKey);
    }

    @Synchronized
    private JsonNode node(@NotNull @NonNull final String key) {
        if (key.isEmpty())
            throw new KfgIllegalArgumentException(this.name(), "empty konfig key");

        final JsonNode node = this.node_(key);
        if (node.isMissingNode())
            throw new KfgMissingKeyException(this.name(), Q.unknown(key));
        return node;
    }

    @NotNull
    @Contract("false, _, _, _ -> fail")
    private JsonNode ensureJsonType(final boolean condition,
                                    final Q<?> required,
                                    final JsonNode node,
                                    final String key) {
        if (!condition)
            throw new KfgMissingKeyException(this.name(), required.withKey(key));
        if (node.isNull())
            throw new KfgTypeNullException(this.name(), required.withKey(key));
        return node;
    }

    @Override
    @Synchronized
    protected boolean isNull(@NonNull @NotNull final Q<?> key) {
        return this.node(key.key()).isNull();
    }

    @Override
    @Synchronized
    public boolean has(@NotNull @NonNull final Q<?> key) {

        if (this.node_(key.key()).isMissingNode())
            return false;

        final JsonNode node = this.node(key.key());

        if (key.isNull() && node.isNull()
                || key.isBool() && node.isBoolean()
                || key.isChar() && node.isTextual() && node.asText().length() == 1
                || key.isString() && node.isTextual()
                || key.isByte() && node.isLong() && node.asLong() <= Byte.MAX_VALUE && Byte.MIN_VALUE <= node.asLong()
                || key.isShort() && node.isLong() && node.asLong() <= Short.MAX_VALUE && Short.MIN_VALUE <= node.asLong()
                || key.isInt() && node.isLong() && node.asLong() <= Integer.MAX_VALUE && Integer.MIN_VALUE <= node.asLong()
                || key.isLong() && node.isLong()
                || key.isFloat() && node.isFloat()
                || key.isDouble() && node.isDouble()
                || key.isList() && node.isArray()
                || key.isSet() && node.isArray() &&
                this.set0(Q.unknownSet(key.key())).size() !=
                        this.list0(Q.unknownList(key.key())).size())
            return true;

        try {
            this.custom0(key);
            return true;
        }
        catch (final Throwable t) {
            return false;
        }
    }


    @Override
    @NotNull
    @Synchronized
    protected Boolean bool0(@NotNull @NonNull final String key) {
        final JsonNode at = this.node(key);
        return this.ensureJsonType(at.isBoolean(), Q.bool(key), at, key).asBoolean();
    }

    @Override
    @NotNull
    @Synchronized
    protected Character char0(@NotNull @NonNull final String key) {
        final JsonNode at = this.node(key);
        return this.ensureJsonType(at.isTextual() && at.textValue().length() == 1, Q.string(key), at, key)
                   .textValue()
                   .charAt(0);
    }

    @Override
    @NotNull
    @Synchronized
    protected String string0(@NotNull @NonNull final String key) {
        final JsonNode at = this.node(key);
        return this.ensureJsonType(at.isTextual(), Q.string(key), at, key).asText();
    }

    @NotNull
    @Override
    @Synchronized
    protected Number number0(@NotNull @NonNull final String key) {
        final JsonNode at = this.node(key);
        return this.ensureJsonType(at.isShort() || at.isInt() || at.isLong(),
                Q.long_(key), at, key).longValue();
    }

    @NotNull
    @Override
    @Synchronized
    protected Number numberDouble0(@NotNull @NonNull final String key) {
        final JsonNode at = this.node(key);
        return this.ensureJsonType(
                at.isFloat()
                        || at.isDouble()
                        || at.isShort()
                        || at.isInt()
                        || at.isLong(),
                Q.double_(key), at, key).doubleValue();
    }


    @NotNull
    @Override
    @Synchronized
    protected List<?> list0(@NotNull final Q<? extends List<?>> type) {
        final JsonNode at = this.node(type.key());
        this.ensureJsonType(at.isArray(), Q.unknownList(type.key()), at, type.key());
        final ObjectMapper reader = this.mapperSupplier.get();
        final CollectionType javaType = reader
                .getTypeFactory()
                .constructCollectionType(List.class, type.getCollectionContainedClass());

        try {
            return reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KfgTypeException(this.name(), type, at, "type mismatch", e);
        }
    }

    @NotNull
    @Override
    @Synchronized
    protected Set<?> set0(@NotNull final Q<? extends Set<?>> key) {
        final JsonNode at = this.node(key.key());

        this.ensureJsonType(at.isArray(), Q.unknownSet(key.key()), at, key.key());
        final ObjectMapper reader = this.mapperSupplier.get();
        final CollectionType javaType = reader
                .getTypeFactory()
                .constructCollectionType(Set.class, key.getCollectionContainedClass());

        final Set<?> set;

        try {
            set = reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KfgTypeException(this.name(), Q.unknownList(key.key()), key, "type mismatch", e);
        }

        final List<?> list = this.list0(Q.unknownList(key.key()));
        if (list.size() != set.size())
            throw new KfgTypeException(this.name(), key, at, "type mismatch, duplicate values in set");

        return set;
    }

    @Override
    @NotNull
    @Synchronized
    protected Map<?, ?> map0(@NotNull final Q<? extends Map<?, ?>> key) {
        final JsonNode at = this.node(key.key());
        this.ensureJsonType(at.isObject(), Q.unknownMap(key.key()), at, key.key());
        final ObjectMapper reader = this.mapperSupplier.get();
        final MapType javaType = reader
                .getTypeFactory()
                .constructMapType(Map.class, key.getMapKeyClass(), key.getMapValueClass());

        try {
            return reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KfgTypeException(this.name(), Q.unknownList(key.key()), key, "type mismatch", e);
        }
    }

    @Override
    @NotNull
    @Synchronized
    protected Object custom0(@NotNull @NonNull final Q<?> key) {
        final ObjectMapper reader = this.mapperSupplier.get();
        final JsonParser traverse = this.node(key.key()).traverse();

        try {
            return reader.readValue(traverse, key.klass());
        }
        catch (final IOException e) {
            throw new KfgTypeException(this.name(), key, null, "jackson error", e);
        }
    }

}
