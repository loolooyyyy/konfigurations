package io.koosha.konfiguration.base;

import io.koosha.konfiguration.Faktory;
import io.koosha.konfiguration.K;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import io.koosha.konfiguration.error.KfgMissingKeyException;
import io.koosha.konfiguration.error.KfgTypeException;
import io.koosha.konfiguration.error.KfgTypeNullException;
import io.koosha.konfiguration.type.Q;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@inheritDoc}
 */
@SuppressWarnings("unused")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public abstract class SourceBase implements Source {

    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    private static Boolean toBool(@Nullable final Object o) {
        if (o instanceof Boolean)
            return (Boolean) o;

        if (!(o instanceof Number))
            return null;

        final Long l = toLong((Number) o);
        if (l == null)
            return null;

        //noinspection RedundantConditionalExpression
        return l == 0 ? false : true;
    }

    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    private static Long toByte(@Nullable final Number o) {
        return toIntegral(o, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    private static Long toShort(@Nullable final Number o) {
        return toIntegral(o, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    private static Long toInt(@Nullable final Number o) {
        return toIntegral(o, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    private static Long toLong(@Nullable final Number o) {
        return toIntegral(o, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Contract(pure = true,
            value = "null, _, _ -> null")
    @Nullable
    private static Long toIntegral(@Nullable final Number o,
                                   final long min,
                                   final long max) {
        if (o == null || o instanceof Double || o instanceof Float)
            return null;

        if (o.longValue() < min || max < o.longValue())
            return null;

        return o.longValue();
    }

    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    private static Float toFloat(@Nullable final Number o) {
        if (o == null)
            return null;

        if (o.doubleValue() < Float.MIN_VALUE
                || Float.MAX_VALUE < o.doubleValue())
            return null;

        return o.floatValue();
    }

    @Contract(pure = true,
            value = "null -> null")
    @Nullable
    private static Double toDouble(@Nullable final Number o) {
        if (o == null)
            return null;

        return o.doubleValue();
    }

    @NotNull
    @ApiStatus.OverrideOnly
    protected abstract Object bool0(@NotNull final String key);

    @NotNull
    @ApiStatus.OverrideOnly
    // TODO check len if from str
    protected abstract Object char0(@NotNull final String key);


    // =========================================================================

    @NotNull
    @ApiStatus.OverrideOnly
    protected abstract Object string0(@NotNull final String key);

    @NotNull
    @ApiStatus.OverrideOnly
    protected abstract Number number0(@NotNull final String key);

    @NotNull
    @ApiStatus.OverrideOnly
    protected abstract Number numberDouble0(@NotNull final String key);

    @NotNull
    @ApiStatus.OverrideOnly
    protected abstract List<?> list0(@NotNull Q<? extends List<?>> type);

    @NotNull
    @ApiStatus.OverrideOnly
    protected abstract Set<?> set0(@NotNull Q<? extends Set<?>> key);

    // =========================================================================

    @NotNull
    @ApiStatus.OverrideOnly
    protected abstract Map<?, ?> map0(@NotNull Q<? extends Map<?, ?>> key);

    @NotNull
    @ApiStatus.OverrideOnly
    protected abstract Object custom0(@NotNull Q<?> key);

    @ApiStatus.OverrideOnly
    protected abstract boolean isNull(@NotNull Q<?> key);

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Override
    public final <U> K<U> custom(@NotNull @NonNull final String key) {
        return this.custom((Q) Q.unknown(key));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public final <U> K<U> custom(@NotNull @NonNull final String key,
                                 @NotNull @NonNull final Class<U> type) {
        return custom(Q.of(key, type));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public final <T> K<List<T>> list(@NotNull @NonNull final String key,
                                     @NotNull @NonNull final Class<T> type) {
        return list(Q.listOf(key, type));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public final <U, V> K<Map<U, V>> map(@NotNull @NonNull final String key,
                                         @NotNull @NonNull final Class<U> keyKlass,
                                         @NotNull @NonNull final Class<V> valueKlass) {
        return map(Q.mapOf(key, keyKlass, valueKlass));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public final <T> K<Set<T>> set(@NotNull @NonNull final String key,
                                   @NotNull @NonNull final Class<T> type) {
        return set(Q.setOf(key, type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull final Q<?> key) {
        try {
            //noinspection ResultOfMethodCallIgnored
            this.custom(key);
            return true;
        }
        catch (KfgMissingKeyException | KfgTypeException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean has(@NotNull @NonNull final String key,
                             @NotNull @NonNull final Class<?> type) {
        return this.has(Q.of(key, type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasMap(@NotNull @NonNull final Q<Map<?, ?>> key) {
        return this.has(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final boolean hasMap(@NotNull @NonNull final String key,
                                @NotNull @NonNull final Class<?> keyType,
                                @NotNull @NonNull final Class<?> valueType) {
        return this.hasSet((Q) Q.mapOf(key, keyType, valueType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasSet(@NotNull @NonNull final Q<Set<?>> key) {
        return this.has(key);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final boolean hasSet(@NotNull @NonNull final String key,
                                @NotNull @NonNull final Class<?> type) {
        return this.hasSet((Q) Q.setOf(key, type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasList(@NotNull @NonNull final Q<List<?>> key) {
        return this.has(key);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final boolean hasList(@NotNull @NonNull final String key,
                                 @NotNull @NonNull final Class<?> type) {
        return this.hasList((Q) Q.listOf(key, type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasBool(@NotNull @NonNull final String key) {
        return this.has(Q.bool(key));
    }


    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasChar(@NotNull @NonNull final String key) {
        return this.has(Q.char_(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasString(@NotNull @NonNull final String key) {
        return this.has(Q.string(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasByte(@NotNull @NonNull final String key) {
        return this.has(Q.byte_(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasShort(@NotNull @NonNull final String key) {
        return this.has(Q.short_(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasInt(@NotNull @NonNull final String key) {
        return this.has(Q.int_(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasLong(@NotNull @NonNull final String key) {
        return this.has(Q.long_(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasFloat(@NotNull @NonNull final String key) {
        return this.has(Q.float_(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasDouble(@NotNull @NonNull final String key) {
        return this.has(Q.double_(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Boolean> bool(@NonNull @NotNull final String key) {
        final Q<Boolean> type = Q.bool(key);

        if (!this.has(type))
            throw new KfgMissingKeyException(this.name(), type);

        if (this.isNull(type))
            return this.null_(type);

        final Object v = this.bool0(key);
        final Boolean vv = toBool(v);
        if (vv == null)
            throw new KfgMissingKeyException(this.name(), type);
        return this.k(type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Character> char_(@NonNull @NotNull final String key) {
        final Q<Character> type = Q.char_(key);

        if (!this.has(type))
            throw new KfgMissingKeyException(this.name(), type);

        if (this.isNull(type))
            return this.null_(type);

        final Object v = this.char0(key);
        char vv;
        try {
            vv = (Character) v;
        }
        catch (final ClassCastException cc0) {
            try {
                final String str = (String) v;
                if (str.length() != 1)
                    throw cc0;
                else
                    vv = str.charAt(0);
            }
            catch (final ClassCastException cce1) {
                throw new KfgMissingKeyException(this.name(), type);
            }
        }
        return this.k(type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<String> string(@NonNull @NotNull final String key) {
        final Q<String> type = Q.string(key);

        if (!this.has(type))
            throw new KfgMissingKeyException(this.name(), type);

        if (this.isNull(type))
            return this.null_(type);

        final Object v = this.string0(key);

        final String vv;
        try {
            vv = (String) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgMissingKeyException(this.name(), type);
        }

        return this.k(type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Byte> byte_(@NonNull @NotNull final String key) {
        final Q<Byte> type = Q.byte_(key);

        if (!this.has(type))
            throw new KfgMissingKeyException(this.name(), type);

        if (this.isNull(type))
            return this.null_(type);

        final Number v = this.number0(key);

        final Long vv = toByte(v);
        if (vv == null)
            throw new KfgMissingKeyException(this.name(), type);

        return this.k(type, vv.byteValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Short> short_(@NonNull @NotNull final String key) {
        final Q<Short> type = Q.short_(key);

        if (!this.has(type))
            throw new KfgMissingKeyException(this.name(), type);

        if (this.isNull(type))
            return this.null_(type);

        final Number v = this.number0(key);

        final Long vv = toShort(v);
        if (vv == null)
            throw new KfgMissingKeyException(this.name(), type);

        return this.k(type, vv.shortValue());
    }

    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Integer> int_(@NonNull @NotNull final String key) {
        final Q<Integer> type = Q.int_(key);

        if (!this.has(type))
            throw new KfgMissingKeyException(this.name(), type);

        if (this.isNull(type))
            return this.null_(type);

        final Number v = this.number0(key);

        final Long vv = toInt(v);
        if (vv == null)
            throw new KfgMissingKeyException(this.name(), type);

        return this.k(type, vv.intValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Long> long_(@NonNull @NotNull final String key) {
        final Q<Long> type = Q.long_(key);

        if (!this.has(type))
            throw new KfgMissingKeyException(this.name(), type);

        if (this.isNull(type))
            return this.null_(type);

        final Number v = this.number0(key);

        final Long vv = toLong(v);
        if (vv == null)
            throw new KfgMissingKeyException(this.name(), type);

        return this.k(type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Float> float_(@NonNull @NotNull final String key) {
        final Q<Float> type = Q.float_(key);

        if (!this.has(type))
            throw new KfgMissingKeyException(this.name(), type);

        if (this.isNull(type))
            return null_(type);

        final Number v = this.numberDouble0(key);

        final Float vv = toFloat(v);
        if (vv == null)
            throw new KfgMissingKeyException(this.name(), type);

        return this.k(type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final K<Double> double_(@NonNull @NotNull final String key) {
        final Q<Double> type = Q.double_(key);

        if (!this.has(type))
            throw new KfgMissingKeyException(this.name(), type);

        if (this.isNull(type))
            return null_(type);

        final Number v = this.numberDouble0(key);

        final Double vv = toDouble(v);
        if (vv == null)
            throw new KfgMissingKeyException(this.name(), type);

        return this.k(type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final <U> K<List<U>> list(@NotNull @NonNull final Q<List<U>> type) {
        if (!this.has(type))
            throw new KfgMissingKeyException(this.name(), type);

        if (this.isNull(type))
            return null_(type);

        final Object v = this.list0(type);

        final List<?> vv;
        try {
            vv = (List<?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgMissingKeyException(this.name(), type);
        }

        this.checkCollectionType(type, vv);

        return this.k(type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final <U> K<Set<U>> set(@NotNull @NonNull Q<Set<U>> type) {
        if (!this.has(type))
            throw new KfgMissingKeyException(this.name(), type);

        if (this.isNull(type))
            return null_(type);

        final Object v = this.set0(type);

        final Set<?> vv;
        try {
            vv = (Set<?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgMissingKeyException(this.name(), type);
        }

        this.checkCollectionType(type, vv);

        return this.k(type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public final <U, V> K<Map<U, V>> map(@NotNull @NonNull final Q<Map<U, V>> type) {
        if (!this.has(type))
            throw new KfgMissingKeyException(this.name(), type);

        if (this.isNull(type))
            return null_(type);

        final Object v = this.map0(type);

        final Map<?, ?> vv;
        try {
            vv = (Map<?, ?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgMissingKeyException(this.name(), type);
        }

        this.checkCollectionType(type, vv);

        return this.k(type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public final <U> K<U> custom(@NotNull @NonNull final Q<U> type) {
        if (this.isNull(type))
            return null_(type);

/*
        if (type.isBool())
            return (K<U>) bool(type.key());
        if (type.isChar())
            return (K<U>) char_(type.key());
        if (type.isString())
            return (K<U>) string(type.key());

        if (type.isByte())
            return (K<U>) byte_(type.key());
        if (type.isShort())
            return (K<U>) short_(type.key());
        if (type.isInt())
            return (K<U>) int_(type.key());
        if (type.isLong())
            return (K<U>) long_(type.key());
        if (type.isDouble())
            return (K<U>) float_(type.key());
        if (type.isFloat())
            return (K<U>) double_(type.key());

        @SuppressWarnings("rawtypes")
        final Q raw = type;
        if (type.isList())
            return (K<U>) list(raw);
        if (type.isMap())
            return (K<U>) map(raw);
        if (type.isSet())
            return (K<U>) set(raw);

*/
        final Object v = this.custom0(type);
        this.checkType(type, v);

        return this.k(type, v);
    }

    private void checkType0(@NonNull @NotNull final Q<?> neededType,
                            @NonNull @NotNull final Object value) {
        if (!neededType.matchesValue(value))
            throw new KfgTypeException(this.name(), neededType, value);
    }

    private void checkCollectionType0(@NonNull @NotNull final Q<?> neededType0,
                                      @NonNull @NotNull final Object value) {
        if (neededType0.isMap()) {
            if (!(value instanceof Map))
                throw new KfgIllegalStateException(this.name(), neededType0, value, "expecting a map");

            if (((Map<?, ?>) value).containsKey(null) && !allowNullInCollection_(neededType0, value))
                throw new KfgTypeNullException(this.name(), neededType0);

            final Q<?> neededKeyType = neededType0.getMapKeyQ();
            final Q<?> neededValueType = neededType0.getMapValueQ();

            for (final Map.Entry<?, ?> o : ((Map<?, ?>) value).entrySet())
                try {
                    checkType0(neededKeyType, o.getKey());
                    checkType0(neededValueType, o.getValue());
                }
                catch (KfgTypeException k) {
                    throw new KfgTypeException(this.name(), neededType0, value);
                }
        }
        else {
            if (!(value instanceof Collection))
                throw new KfgIllegalStateException(
                        this.name(), neededType0, value, "expecting a collection");

            final Q<?> neededType = neededType0.getCollectionContainedQ();

            for (final Object o : (Collection<?>) value)
                if (o != null)
                    try {
                        checkType0(neededType, o);
                    }
                    catch (KfgTypeException k) {
                        throw new KfgTypeException(this.name(), neededType, value);
                    }
                else if (!allowNullInCollection_(neededType, value))
                    throw new KfgTypeNullException(this.name(), neededType);
        }
    }

    /**
     * Handle the case where value of a key is null.
     *
     * @param type type of requested konfig.
     * @return true if it's ok to have null values.
     */
    @NotNull
    private <U> K<U> null_(@NotNull @NonNull final Q<U> type) {
        return k(type, null);
    }

    /**
     * Handle the case where value in a collection is null
     *
     * @param collection the map, list or set collection in question.
     * @param type       type of requested konfig.
     * @return true if it's ok to have null values.
     */
    private boolean allowNullInCollection_(@NotNull final Q<?> type,
                                           @NotNull final Object collection) {
        return true;
    }

    /**
     * Make sure the value is of the requested type.
     *
     * @param neededType type asked for.
     * @param value      the value in question.
     * @throws KfgTypeException if the requested type does not match the type
     *                          of value in the given in.
     */
    private void checkType(@NotNull @NonNull final Q<?> neededType,
                           @NotNull @NonNull final Object value) {
        checkType0(neededType, value);
    }

    /**
     * Make sure the value is of the requested type.
     *
     * @param neededType type asked for.
     * @param value      the value in question
     * @throws KfgTypeException if the requested type does not match the type
     *                          of value in the given in.
     */
    private void checkCollectionType(@NotNull @NonNull final Q<?> neededType,
                                     @NotNull @NonNull final Object value) {
        checkCollectionType0(neededType, value);
    }

    /**
     * Wrap the actual sanitized value in a  {@link K} instance.
     *
     * @param type type holder of wanted value
     * @param <U>  generic type of wanted konfig.
     * @return the wrapped value in K.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private <U> K<U> k(@NotNull @NonNull final Q<U> type,
                       @Nullable final Object value) {
        return K.of((U) value, type);
    }

}
