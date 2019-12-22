package io.koosha.konfiguration;

import io.koosha.konfiguration.error.KfgIllegalArgumentException;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import io.koosha.konfiguration.error.KfgUnsupportedOperationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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

import static java.util.Arrays.asList;
import static java.util.Collections.*;

@SuppressWarnings("unused")
@ThreadSafe
@Immutable
@Accessors(fluent = true)
@EqualsAndHashCode
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public abstract class Q<TYPE> {

    @SuppressWarnings("unchecked")
    protected Q(@NotNull @NonNull final String key) {
        this.key = key;

        final Type t = ((ParameterizedType) this
                .getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];

        checkIsClassOrParametrizedType(t, null);

        if (t instanceof ParameterizedType) {
            final ParameterizedType pt = (ParameterizedType) t;
            this.generic = unmodifiableList(asList(pt.getActualTypeArguments()));
            this.klass = (Class<TYPE>) pt.getRawType();
        }
        else {
            this.generic = emptyList();
            this.klass = (Class<TYPE>) t;
        }
    }

    private Q(@NotNull @NonNull final String key,
              @NotNull @NonNull Class<TYPE> klass,
              @NotNull @NonNull final List<Type> generic) {
        klass = upper(klass);
        this.key = key;
        this.generic = generic.isEmpty()
                       ? emptyList()
                       : unmodifiableList(new ArrayList<>(generic));

        for (final Type type : generic)
            if (!isConcrete(type))
                throw new KfgUnsupportedOperationException(
                        "only concrete types are supported, given= " + type);

        this.klass = klass;
    }

    @Contract(pure = true)
    private boolean matchesType(final Q<?> other) {
        if (other == null || other == this)
            return true;
        if (!other.klass.isAssignableFrom(this.klass))
            return false;
        return match(this.klass, other.klass);
    }

    @Contract(pure = true)
    public boolean matchesValue(final Object v) {
        if (v == null || Objects.equals(this.klass, Object.class))
            return true;
        if (!this.klass.isAssignableFrom(v.getClass()))
            return false;
        return match(this.klass, v.getClass());
    }

    @Contract(pure = true)
    private boolean match(@NotNull @NonNull final Type a,
                          @NotNull @NonNull final Type b) {
        if (Objects.equals(a, b))
            return true;
        List<Integer> m = new ArrayList<>();
        List<Integer> l = new ArrayList<>();

        final Type t = ((ParameterizedType) this
                .getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];

        return false;
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

    @Contract(pure = true)
    public final boolean isVoid() {
        return Void.class.isAssignableFrom(this.klass);
    }

    @Contract(pure = true)
    public final boolean hasKey() {
        //noinspection ConstantConditions
        return this.key() != null && !this.key().isEmpty();
    }

    // =========================================================================

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Boolean> bool(@NotNull @NonNull final String key) {
        return new Q<Boolean>(key, Boolean.class, emptyList()) {};
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Character> char_(@NotNull @NonNull final String key) {
        return new Q<Character>(key, Character.class, emptyList()) {};
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Byte> byte_(@NotNull @NonNull final String key) {
        return new Q<Byte>(key, Byte.class, emptyList()) {};
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Short> short_(@NotNull @NonNull final String key) {
        return new Q<Short>(key, Short.class, emptyList()) {};
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Integer> int_(@NotNull @NonNull final String key) {
        return new Q<Integer>(key, Integer.class, emptyList()) {};
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Long> long_(@NotNull @NonNull final String key) {
        return new Q<Long>(key, Long.class, emptyList()) {};
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Float> float_(@NotNull @NonNull final String key) {
        return new Q<Float>(key, Float.class, emptyList()) {};
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Double> double_(@NotNull @NonNull final String key) {
        return new Q<Double>(key, Double.class, emptyList()) {};
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<String> string(@NotNull @NonNull final String key) {
        return new Q<String>(key, String.class, emptyList()) {};
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<?> unknown(@NotNull @NonNull final String key) {
        return new Q<Object>(key, Object.class, emptyList()) {};
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Map<?, ?>> unknownMap(@NotNull @NonNull final String key) {
        return (Q) new Q<Map>(key, Map.class, emptyList()) {};
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Set<?>> unknownSet(@NotNull @NonNull final String key) {
        return (Q) new Q<Set>(key, Set.class, emptyList()) {};
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<List<?>> unknownList(@NotNull @NonNull final String key) {
        return (Q) new Q<List>(key, List.class, emptyList()) {};
    }

    // =========================================================================

    /**
     * Faktory method.
     *
     * @param klass the type to create a Q for.
     * @param <U>   Generic type of requested class.
     * @return a Q instance representing Class&lt;U&gt;
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    public static <U> Q<List<U>> listOf(@NotNull @NonNull final String key,
                                        @NotNull @NonNull final Class<U> klass) {
        if (!isConcrete(klass))
            throw new KfgUnsupportedOperationException(
                    "only concrete types are supported, given= " + klass);
        return new Q<List<U>>(key, (Class) List.class, singletonList(klass)) {};
    }

    /**
     * Faktory method.
     *
     * @param klass the type to create a Q for.
     * @param <U>   Generic type of requested class.
     * @return a Q instance representing Class&lt;U&gt;
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    public static <U> Q<Set<U>> setOf(@NonNull @NotNull final String key,
                                      @NotNull @NonNull final Class<U> klass) {
        if (!isConcrete(klass))
            throw new KfgUnsupportedOperationException(
                    "only concrete types are supported, given= " + klass);
        return new Q<Set<U>>(key, (Class) Set.class, singletonList(klass)) {};
    }

    /**
     * Faktory method.
     *
     * @param klassK the type to create a Q for.
     * @param klassV the type to create a Q for.
     * @param <U>    Generic key type of requested map class.
     * @param <V>    Generic value type of requested map class.
     * @return a Q instance representing Class&lt;U&gt;
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_, _, _ -> new",
            pure = true)
    public static <U, V> Q<Map<U, V>> mapOf(@NotNull @NonNull final String key,
                                            @NotNull @NonNull final Class<U> klassK,
                                            @NotNull @NonNull final Class<V> klassV) {
        if (!isConcrete(klassK) || isConcrete(klassV))
            throw new KfgUnsupportedOperationException(
                    "only concrete types are supported, given= " + klassK + " - " + klassV);
        return new Q<Map<U, V>>(key, (Class) Map.class, asList(klassK, klassV)) {};
    }

    // =========================================================================

    @SuppressWarnings("unchecked")
    @Contract(pure = true,
            value = "null->null; _ -> _ ")
    private static <T> Class<T> upper(@Nullable final Class<T> t) {
        if (Objects.equals(t, boolean.class))
            return (Class<T>) Boolean.class;
        if (Objects.equals(t, char.class))
            return (Class<T>) Character.class;
        if (Objects.equals(t, byte.class))
            return (Class<T>) Byte.class;
        if (Objects.equals(t, short.class))
            return (Class<T>) Short.class;
        if (Objects.equals(t, int.class))
            return (Class<T>) Integer.class;
        if (Objects.equals(t, long.class))
            return (Class<T>) Long.class;
        if (Objects.equals(t, float.class))
            return (Class<T>) Float.class;
        if (Objects.equals(t, double.class))
            return (Class<T>) Double.class;
        return t;
    }

    @Contract(pure = true)
    private static void checkIsClassOrParametrizedType(@Nullable final Type p,
                                                       @Nullable Type root) {
        if (root == null)
            root = p;

        if (p == null)
            return;

        if (!(p instanceof Class) && !(p instanceof ParameterizedType))
            throw new KfgUnsupportedOperationException(
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
    private static boolean isConcrete(@NotNull @NonNull final Type p) {
        return p instanceof Class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return String.format("Q[%s/%s]", this.klass.getTypeName(), this.key);
    }

    @NotNull
    @Contract(pure = true,
            value = "_->new")
    public Q<TYPE> withKey(@NotNull @NonNull final String key) {
        if (key.isEmpty())
            throw new KfgIllegalArgumentException(null, "empty key");

        return Objects.equals(this.key, key)
               ? new Q<TYPE>(key, this.klass, this.generic) {}
               : this;
    }

    @Contract(pure = true)
    public final boolean isParametrized() {
        return !this.generic.isEmpty();
    }

    @Nullable
    protected final Type get(int i) {
        return this.generic.size() >= i + 1 ? this.generic.get(i) : null;
    }

    /**
     * If Q represents a collection, get type argument of the collection.
     *
     * @return type argument of the collection represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a collection.
     */
    @Contract(pure = true)
    @Nullable
    public final Class<?> getCollectionContainedClass() {
        if (!this.isSet() && !this.isList())
            throw new KfgIllegalStateException(null, "type is not a set or list");
        final Type type = this.get(0);
        if (type == null)
            return null;
        if (!(type instanceof Class))
            throw new KfgIllegalStateException(null, this.key, Q.unknownMap(key), null, "collection type is not concrete");
        return (Class<?>) type;
    }

    /**
     * If Q represents a map, get type argument of the map's key.
     *
     * @return type argument of the map's key represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a map.
     */
    @Contract(pure = true)
    @Nullable
    public final Class<?> getMapKeyClass() {
        if (!this.isMap())
            throw new KfgIllegalStateException(null, this.key, Q.unknownMap(key), null, "type is not a map");
        final Type type = this.get(0);
        if (type == null)
            return null;
        if (!(type instanceof Class))
            throw new KfgIllegalStateException(null, this.key, Q.unknownMap(key), null, "key type is not concrete");
        return (Class<?>) type;
    }

    /**
     * If Q represents a map, get type argument of the map's value.
     *
     * @return type argument of the map's value represented by Q instance.
     * @throws KfgIllegalStateException if Q does not represent a map.
     */
    @Contract(pure = true)
    @Nullable
    public final Class<?> getMapValueClass() {
        if (!this.isMap())
            throw new KfgIllegalStateException(null, this.key, Q.unknownMap(key), null, "type is not a map");
        final Type type = this.get(1);
        if (type == null)
            return null;
        if (!(type instanceof Class))
            throw new KfgIllegalStateException(null, this.key, Q.unknownMap(key), null, "value type is not concrete");
        return (Class<?>) type;
    }


    @NotNull
    @Getter
    protected final String key;

    @NotNull
    protected final List<Type> generic;

    @NotNull
    @Getter
    protected final Class<TYPE> klass;


}
