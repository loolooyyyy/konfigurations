package io.koosha.konfiguration;

import io.koosha.konfiguration.error.KfgIllegalArgumentException;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@SuppressWarnings("unused")
@ThreadSafe
@Immutable
@Accessors(fluent = true)
@EqualsAndHashCode
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public abstract class Q<TYPE> {

    @Nullable
    private final String key;

    @Nullable
    private final ParameterizedType pt;

    @NotNull
    private final Class<TYPE> klass;

    private Q(@NotNull @NonNull final Class<TYPE> type) {
        this.key = null;
        this.pt = null;
        this.klass = type;
    }

    private Q(@Nullable final String key,
              @Nullable final ParameterizedType pt,
              @NotNull @NonNull final Class<TYPE> klass) {
        this.key = key;
        this.pt = pt;
        this.klass = klass;
    }

    @SuppressWarnings("unchecked")
    protected Q() {
        final Type t = ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];

        checkIsClassOrParametrizedType(t, null);

        if (t instanceof ParameterizedType) {
            this.pt = (ParameterizedType) t;
            this.klass = (Class<TYPE>) this.pt.getRawType();
        }
        else {
            this.pt = null;
            this.klass = (Class<TYPE>) t;
        }

        this.key = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return String.format("Q[%s/%s]",
                this.klass().getTypeName(),
                this.key == null ? "" : this.key
        );
    }

    public Q<TYPE> withKey(@NotNull @NonNull final String key) {
        if (key.isEmpty())
            throw new KfgIllegalArgumentException(null, "empty key");

        return Objects.equals(this.key, key)
               ? new Q<TYPE>(key, this.pt, this.klass) {}
               : this;
    }

    public final boolean isParametrized() {
        return this.pt != null;
    }


    @Contract(pure = true)
    final boolean matchesType(final Q<?> other) {
        if (other == null || other == this)
            return true;
        // TODO
        return other.klass().isAssignableFrom(this.klass());
    }

    @Contract(pure = true)
    final boolean matchesValue(final Object v) {
        if (v == null)
            return true;
        if (Objects.equals(this.klass, Object.class))
            return true;
        // TODO
        //        return v.getClass().isAssignableFrom(this.klass());
        throw new UnsupportedOperationException("todo");
    }


    @Contract(pure = true)
    public static boolean matchesType(final Q<?> q0, final Q<?> q1) {
        if (q0 == null || q1 == null)
            return true;
        return q0.matchesType(q1);
    }

    @Contract(pure = true)
    public static boolean matchesValue(final Q<?> q0, final Object value) {
        if (q0 == null)
            return true;
        return q0.matchesValue(value);
    }

    // =========================================================================

    /**
     * If Q represents a collection, get type argument of the collection.
     *
     * @return type argument of the collection represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a collection.
     */
    @Nullable
    public final Class<?> getCollectionContainedClass() {
        if (!this.isSet() && !this.isList())
            throw new KfgIllegalStateException(null, "type is not a set or list");
        final Type type = this.pt == null ? null : this.pt.getActualTypeArguments()[0];
        if (type == null)
            return null;
        if (!(type instanceof Class))
            throw new KfgIllegalStateException(null, this.key, Q.UNKNOWN_MAP, null, "collection type is not concrete");
        return (Class<?>) type;
    }

    /**
     * If Q represents a map, get type argument of the map's key.
     *
     * @return type argument of the map's key represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a map.
     */
    @Nullable
    public final Class<?> getMapKeyClass() {
        if (!this.isMap())
            throw new KfgIllegalStateException(null, this.key, Q.UNKNOWN_MAP, null, "type is not a map");
        final Type type = this.pt == null ? null : this.pt.getActualTypeArguments()[0];
        if (type == null)
            return null;
        if (!(type instanceof Class))
            throw new KfgIllegalStateException(null, this.key, Q.UNKNOWN_MAP, null, "key type is not concrete");
        return (Class<?>) type;
    }

    /**
     * If Q represents a map, get type argument of the map's value.
     *
     * @return type argument of the map's value represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a map.
     */
    @Nullable
    public final Class<?> getMapValueClass() {
        if (!this.isMap())
            throw new KfgIllegalStateException(null, this.key, Q.UNKNOWN_MAP, null, "type is not a map");
        final Type type = this.pt == null ? null : this.pt.getActualTypeArguments()[1];
        if (type == null)
            return null;
        if (!(type instanceof Class))
            throw new KfgIllegalStateException(null, this.key, Q.UNKNOWN_MAP, null, "value type is not concrete");
        return (Class<?>) type;
    }

    // =========================================================================

    @Contract(pure = true)
    public final boolean isBool() {
        return Boolean.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isChar() {
        return Character.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isString() {
        return String.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isByte() {
        return Byte.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isShort() {
        return Short.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isInt() {
        return Integer.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isLong() {
        return Long.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isFloat() {
        return Float.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isDouble() {
        return Double.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isSet() {
        return Set.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isList() {
        return List.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isMap() {
        return Map.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean isNull() {
        return isVoid();
    }

    public final boolean isVoid() {
        return Void.class.isAssignableFrom(this.klass);
    }

    public final boolean hasKey() {
        //noinspection ConstantConditions
        return this.key() != null && !this.key().isEmpty();
    }

    // =========================================================================

    /**
     * Faktory method.
     *
     * @param klass the type to create a Q for.
     * @param <U>   Generic type of requested class.
     * @return a Q instance representing Class&lt;U&gt;
     */
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static <U> Q<U> of(@NotNull @NonNull final Class<U> klass) {
        return new Q<U>(klass) {};
    }

    public static final Q<Boolean> BOOL = of(Boolean.class);
    public static final Q<Character> CHAR = of(Character.class);

    public static final Q<Byte> BYTE = of(Byte.class);
    public static final Q<Short> SHORT = of(Short.class);
    public static final Q<Integer> INT = of(Integer.class);
    public static final Q<Long> LONG = of(Long.class);
    public static final Q<Float> FLOAT = of(Float.class);
    public static final Q<Double> DOUBLE = of(Double.class);

    public static final Q<String> STRING = of(String.class);

    public static final Q<Map<?, ?>> UNKNOWN_MAP = of_(Map.class);
    public static final Q<Set<?>> UNKNOWN_SET = of_(Set.class);
    public static final Q<List<?>> UNKNOWN_LIST = of_(List.class);
    public static final Q<Collection<?>> UNKNOWN_COLLECTION = of_(Collection.class);

    public static final Q<Object> OBJECT = of_(Object.class);
    public static final Q<?> UNKNOWN = OBJECT;


    public static final Q<?> _VOID = of_(Void.class);

    // =========================================================================

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    private static <U> Q<U> of_(@NotNull @NonNull final Class<?> klass) {
        return (Q<U>) of(klass);
    }

    @Contract(pure = true)
    private static void checkIsClassOrParametrizedType(@Nullable final Type p, @Nullable Type root) {
        if (root == null)
            root = p;

        if (p == null)
            return;

        if (!(p instanceof Class) && !(p instanceof ParameterizedType))
            throw new UnsupportedOperationException(
                    "only Class and ParameterizedType are supported: "
                            + root + "::" + p);

        if (!(p instanceof ParameterizedType))
            return;
        final ParameterizedType pp = (ParameterizedType) p;

        checkIsClassOrParametrizedType(root, pp.getRawType());
        for (final Type ppp : pp.getActualTypeArguments())
            checkIsClassOrParametrizedType(root, ppp);
    }

    @Contract(pure = true)
    public static Q<?> withKey0(@Nullable final Q<?> type,
                                @NotNull @NonNull final String key) {
        return type == null ? Q.UNKNOWN.withKey(key) : type.withKey(key);
    }

    @Nullable
    @Contract(pure = true,
            value = "->_")
    public String key() {
        return this.key;
    }

    @Nullable
    @Contract(pure = true,
            value = "->_")
    public ParameterizedType pt() {
        return this.pt;
    }

    @NotNull
    @Contract(pure = true,
            value = "->_")
    public Class<TYPE> klass() {
        return this.klass;
    }

}
