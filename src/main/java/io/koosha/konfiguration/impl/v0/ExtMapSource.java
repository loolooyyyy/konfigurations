package io.koosha.konfiguration.impl.v0;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import io.koosha.konfiguration.KfgAssertionException;
import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.Q;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

final class ExtMapSource extends AbstractKonfiguration {

    private static final Pattern DOT = Pattern.compile(Pattern.quote("."));

    private final Supplier<Map<String, ?>> map;
    private final Map<String, ?> root;
    private final int lastHash;
    private final boolean enableMapWithinMap;

    @NonNull
    @NotNull
    @Getter
    @Accessors(fluent = true)
    private final String name;

    @Accessors(fluent = true)
    @Getter
    private final Manager manager = new Manager() {
        @Override
        @Contract(pure = true)
        public boolean hasUpdate() {
            final Map<String, ?> newMap = map.get();
            if (newMap == null)
                return false;
            final int newHash = newMap.hashCode();
            return newHash != lastHash;
        }

        @Override
        @NotNull
        @Contract(mutates = "this")
        public Konfiguration update() {
            return new ExtMapSource(name(), map, enableMapWithinMap);
        }
    };

    @NotNull
    @Contract(pure = true)
    private Object node(@NonNull @NotNull String key) {
        if (this.root.containsKey(key))
            return this.root.get(key);
        if (enableMapWithinMap) {
            final List<String> parts = asList(DOT.split(key));
            Object m = this.root.get(parts.get(0));
            int i = 0;
            while (i++ < parts.size() && m instanceof Map) {
                final String newKey = String.join(".", parts.subList(i, parts.size()));
                final Map<?, ?> mm = (Map<?, ?>) m;
                if (mm.containsKey(newKey))
                    return mm.get(newKey);
                m = mm.get(parts.get(1));
            }
        }
        throw new KfgAssertionException("missing key: " + key);
    }

    private <T> T checkMapType(@Nullable final Q<?> required,
                               @Nullable final T value,
                               @NotNull @NonNull final String key) {
        if (!Q.matchesValue(required, value))
            throw new KfgTypeException(this, key, required, value);
        return value;
    }

    ExtMapSource(@NotNull @NonNull final String name,
                 @NonNull @NotNull final Supplier<Map<String, ?>> map,
                 final boolean enableMapWithinMap) {
        this.name = name;
        this.map = map;
        this.root = new HashMap<>(map.get());
        this.enableMapWithinMap = enableMapWithinMap;
        this.lastHash = this.root.hashCode();
        requireNonNull(this.map.get(), "map is null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Boolean bool0(@NotNull @NonNull final String key) {
        return checkMapType(Q.BOOL, (Boolean) node(key), key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Character char0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(at.isTextual() && at.textValue().length() == 1, Q.STRING, at, key)
                .textValue()
                .charAt(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    String string0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(at.isTextual(), Q.STRING, at, key).asText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Byte byte0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(
                at.isShort() || at.isInt() || at.isLong(),
                Q.BYTE, at, key).numberValue().byteValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Short short0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(
                at.isShort() || at.isInt() || at.isLong(),
                Q.SHORT, at, key).numberValue().shortValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Integer int0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(
                at.isShort() || at.isInt() || at.isLong(),
                Q.INT, at, key).numberValue().intValue();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    Long long0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(
                !at.isNull() && at.isShort() || at.isInt() || at.isLong(),
                Q.LONG, at, key).longValue();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    Float float0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(
                at.isFloat()
                        || at.isDouble()
                        || at.isShort()
                        || at.isInt()
                        || at.isLong(),
                Q.FLOAT, at, key).floatValue();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    Double double0(@NotNull @NonNull final String key) {
        final JsonNode at = node(key);
        return checkJsonType(
                at.isFloat()
                        || at.isDouble()
                        || at.isShort()
                        || at.isInt()
                        || at.isLong(),
                Q.DOUBLE, at, key).doubleValue();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    List<?> list0(@NotNull @NonNull final String key,
                  @NotNull Q<? extends List<?>> type) {
        final JsonNode at = node(key);
        checkJsonType(at.isArray(), Q.UNKNOWN_LIST, at, key);
        final ObjectMapper reader = this.mapperSupplier.get();
        final CollectionType javaType = reader
                .getTypeFactory()
                .constructCollectionType(List.class, type.klass());

        try {
            return reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KfgTypeException(this, key, type, at, "type mismatch", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    Set<?> set0(@NotNull @NonNull final String key,
                @NotNull Q<? extends Set<?>> type) {
        final JsonNode at = node(key);

        checkJsonType(at.isArray(), Q.UNKNOWN_SET, at, key);
        final ObjectMapper reader = this.mapperSupplier.get();
        final CollectionType javaType = reader
                .getTypeFactory()
                .constructCollectionType(Set.class, type.klass());

        final Set<?> s;

        try {
            s = reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KfgTypeException(this, key, Q.UNKNOWN_LIST, type, "type mismatch", e);
        }

        final List<?> l = this.list0(key, Q.UNKNOWN_LIST);
        if (l.size() != s.size())
            throw new KfgTypeException(this, key, type, at, "type mismatch, duplicate values");

        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Map<?, ?> map0(@NotNull @NonNull final String key,
                   @NotNull Q<? extends Map<?, ?>> type) {
        final JsonNode at = node(key);
        checkJsonType(at.isObject(), Q.UNKNOWN_MAP, at, key);
        final ObjectMapper reader = this.mapperSupplier.get();
        final MapType javaType = reader
                .getTypeFactory()
                .constructMapType(Map.class, String.class, type.klass());

        try {
            return reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KfgTypeException(this, key, Q.UNKNOWN_LIST, type, "type mismatch", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    Object custom0(@NotNull @NonNull final String key,
                   @NotNull @NonNull final Q<?> type) {
        final ObjectMapper reader = this.mapperSupplier.get();
        final JsonParser traverse = this.node(key).traverse();

        try {
            return reader.readValue(traverse, type.klass());
        }
        catch (final IOException e) {
            throw new KfgTypeException(this, key, type, null, "jackson error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isNull(@NonNull @NotNull String key) {
        return node(key).isNull();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull @NonNull final String key,
                       @Nullable final Q<?> type) {
        if (this.node_(key).isMissingNode())
            return false;
        if (type == null)
            return true;

        final JsonNode node = this.node(key);

        if (type.isNull() && node.isNull()
                || type.isBool() && node.isBoolean()
                || type.isChar() && node.isTextual() && node.asText().length() == 1
                || type.isString() && node.isTextual()
                || type.isByte() && node.isShort() && node.asInt() <= Byte.MAX_VALUE && Byte.MIN_VALUE <= node.asInt()
                || type.isShort() && node.isShort()
                || type.isInt() && node.isInt()
                || type.isLong() && node.isLong()
                || type.isFloat() && node.isFloat()
                || type.isDouble() && node.isDouble()
                || type.isList() && node.isArray()
                || type.isSet() && node.isArray() &&
                this.set0(key, Q.UNKNOWN_SET).size() != this.list0(key, Q.UNKNOWN_LIST).size())
            return true;

        try {
            this.custom0(key, type);
            return true;
        }
        catch (Throwable t) {
            return false;
        }
    }

}
