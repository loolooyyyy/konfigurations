package io.koosha.konfiguration;

import io.koosha.konfiguration.error.KfgIllegalArgumentException;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static io.koosha.konfiguration.Q_Helper.*;
import static java.util.Arrays.stream;

@SuppressWarnings("unused")
@ThreadSafe
@Immutable
@Accessors(fluent = true)
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public class Q<TYPE> {

    @NotNull
    private final String key;

    @NotNull
    private final Class<TYPE> klass;

    @NotNull
    private final List<Q<?>> typeArgs;

    protected Q(@NotNull @NonNull final String key) {
        this(key, 0);
    }

    public Q(@NotNull @NonNull final String key,
             @NotNull @NonNull final Type x) {
        this(key, x, 0);
    }

    public Q(@NotNull @NonNull final String key,
             @NotNull @NonNull Class<TYPE> klass) {
        this.key = key;
        this.klass = upper(klass);
        this.typeArgs = l();
        checkIsConcrete(this);
    }

    public Q(@NotNull @NonNull final String key,
             @NotNull @NonNull Class<TYPE> klass,
             @NotNull @NonNull final List<@NotNull Q<?>> typeArgs) {
        final List<Q<?>> cp = copy(typeArgs);
        if (cp.contains(null))
            throw new KfgIllegalArgumentException(null, "type contain null: " + typeArgs);
        this.key = key;
        this.klass = upper(klass);
        this.typeArgs = cp;
        checkIsConcrete(this);
    }

    // =========================================================================
    @SuppressWarnings("unchecked")
    private Q(@NotNull @NonNull final String key,
              final int nestingLevel) {
        if (nestingLevel >= Q_Helper.MAX_NESTING_LEVEL)
            throw new KfgIllegalArgumentException(null, "max nesting level reached");
        if (nestingLevel == 0)
            checkIsConcrete(this.getClass().getGenericSuperclass());

        final Type t = typeArgumentsOf(this)[0];

        this.key = key;
        this.klass = (Class<TYPE>) raw(t);

        if (!(t instanceof ParameterizedType)) {
            this.typeArgs = l();
        }
        else {
            //noinspection rawtypes
            this.typeArgs = (List) copy(stream(((ParameterizedType) t)
                    .getActualTypeArguments())
                    .map(x -> new Q(X, x, nestingLevel + 1))
                    .collect(Collectors.toList()));
        }

        if (nestingLevel == 0)
            checkIsConcrete(this);
    }

    @SuppressWarnings("unchecked")
    private Q(@NotNull @NonNull final String key,
              @NotNull @NonNull final Type t,
              final int nestingLevel) {
        if (nestingLevel >= Q_Helper.MAX_NESTING_LEVEL)
            throw new KfgIllegalArgumentException(null, "max nesting level reached");
        if (nestingLevel == 0)
            checkIsConcrete(this.getClass().getGenericSuperclass());
        if (nestingLevel == 0)
            checkIsConcrete(t);

        this.key = key;
        this.klass = (Class<TYPE>) raw(t);
        if (!(t instanceof ParameterizedType)) {
            this.typeArgs = l();
        }
        else {
            //noinspection rawtypes
            this.typeArgs = (List) copy(stream(((ParameterizedType) t)
                    .getActualTypeArguments())
                    .map(x -> new Q(X, x, nestingLevel + 1))
                    .collect(Collectors.toList()));
        }

        if (nestingLevel == 0)
            checkIsConcrete(this);
    }

    @Contract(pure = true)
    public final boolean matchesType(final Type other) {
        if (other == null)
            return true;
        if (!this.klass.isAssignableFrom(this.klass))
            return false;
        return match(this, other);
    }

    @Contract(pure = true)
    public final boolean matchesType(final Q<?> other) {
        if (other == null || other == this)
            return true;
        if (!this.klass.isAssignableFrom(other.klass))
            return false;
        return match(this, other);
    }

    @Contract(pure = true)
    public final boolean matchesValue(final Object v) {
        if (v == null || Objects.equals(this.klass, Object.class))
            return true;
        if (!this.klass.isAssignableFrom(v.getClass()))
            return false;
        return match(this, v.getClass());
    }

    // =================================

    @NotNull
    @Contract(pure = true)
    public final String key() {
        if (Objects.equals(this.key, X))
            throw new KfgIllegalStateException(null, "this Q type is not keyed: " + this);
        return this.key;
    }

    @NotNull
    @Contract(pure = true)
    public final Class<TYPE> klass() {
        return this.klass;
    }

    @NotNull
    @Contract(pure = true,
            value = "_->new")
    public final Q<TYPE> withKey(@NotNull @NonNull final String key) {
        if (key.isEmpty())
            throw new KfgIllegalArgumentException(null, "empty key");

        return Objects.equals(this.key, key)
               ? new Q<>(key, this.klass, this.typeArgs)
               : this;
    }

    // =================================

    @Contract(pure = true)
    @Nullable
    private Q<?> get(@Range(from = 0,
            to = Byte.MAX_VALUE) final int i) {
        return i < this.typeArgs.size() ? this.typeArgs.get(i) : null;
    }

    @Contract(pure = true)
    @NotNull
    public final Class<?> getCollectionContainedClass() {
        if (!Collection.class.isAssignableFrom(this.klass))
            throw new KfgIllegalStateException(null, "type is not a collection: " + this);
        final Q<?> type = this.get(0);
        if (type == null)
            throw new KfgIllegalStateException(null, "collection type is not present: " + this);
        return type.klass;
    }

    @Contract(pure = true)
    @NotNull
    public final Class<?> getMapKeyClass() {
        if (!Map.class.isAssignableFrom(this.klass))
            throw new KfgIllegalStateException(null, this.key, Q.unknownMap(key), null, "type is not a map");
        final Q<?> type = this.get(0);
        if (type == null)
            throw new KfgIllegalStateException(null, "map key type is not present: " + this);
        return type.klass;
    }

    @Contract(pure = true)
    @NotNull
    public final Class<?> getMapValueClass() {
        if (!Map.class.isAssignableFrom(this.klass))
            throw new KfgIllegalStateException(null, this.key, Q.unknownMap(key), null, "type is not a map");
        final Q<?> type = this.get(1);
        if (type == null)
            throw new KfgIllegalStateException(null, "map value type is not present: " + this);
        return type.klass;
    }

    @NotNull
    @Contract(pure = true)
    public final List<Q<?>> getTypeArgs() {
        return this.typeArgs;
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
        return this.key() != null && !this.key().isEmpty() && !Objects.equals(this.key, X);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Boolean> bool(@NotNull @NonNull final String key) {
        return new Q<>(key, Boolean.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Character> char_(@NotNull @NonNull final String key) {
        return new Q<>(key, Character.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Byte> byte_(@NotNull @NonNull final String key) {
        return new Q<>(key, Byte.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Short> short_(@NotNull @NonNull final String key) {
        return new Q<>(key, Short.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Integer> int_(@NotNull @NonNull final String key) {
        return new Q<>(key, Integer.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Long> long_(@NotNull @NonNull final String key) {
        return new Q<>(key, Long.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Float> float_(@NotNull @NonNull final String key) {
        return new Q<>(key, Float.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Double> double_(@NotNull @NonNull final String key) {
        return new Q<>(key, Double.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<String> string(@NotNull @NonNull final String key) {
        return new Q<>(key, String.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<?> unknown(@NotNull @NonNull final String key) {
        return new Q<>(key, Object.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Map<?, ?>> unknownMap(@NotNull @NonNull final String key) {
        return (Q) new Q<>(key, Map.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Set<?>> unknownSet(@NotNull @NonNull final String key) {
        return (Q) new Q<>(key, Set.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<List<?>> unknownList(@NotNull @NonNull final String key) {
        return (Q) new Q<>(key, List.class);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<Boolean> bool() {
        return bool(X);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<Character> char_() {
        return char_(X);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<Byte> byte_() {
        return byte_(X);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<Short> short_() {
        return short_(X);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<Integer> int_() {
        return int_(X);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<Long> long_() {
        return long_(X);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<Float> float_() {
        return float_(X);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<Double> double_() {
        return double_(X);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<String> string() {
        return string(X);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<?> unknown() {
        return unknown(X);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<Map<?, ?>> unknownMap() {
        return unknownMap(X);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<Set<?>> unknownSet() {
        return unknownSet(X);
    }

    @NotNull
    @Contract(value = "-> new",
            pure = true)
    public static Q<List<?>> unknownList() {
        return unknownList(X);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    public static <U> Q<List<U>> listOf(@NotNull @NonNull final String key,
                                        @NotNull @NonNull final Class<U> klass) {
        return new Q<List<U>>(key, (Class) List.class, lq(klass));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    public static <U> Q<Set<U>> setOf(@NonNull @NotNull final String key,
                                      @NotNull @NonNull final Class<U> klass) {
        return new Q<Set<U>>(key, (Class) Set.class, lq(klass));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_, _, _ -> new",
            pure = true)
    public static <U, V> Q<Map<U, V>> mapOf(@NotNull @NonNull final String key,
                                            @NotNull @NonNull final Class<U> klassK,
                                            @NotNull @NonNull final Class<V> klassV) {
        return new Q<Map<U, V>>(key, (Class) Map.class, lq(klassK, klassV));
    }

    @Override
    public final boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Q))
            return false;
        final Q<?> other = (Q<?>) o;
        return Objects.equals(this.key(), other.key())
                && Objects.equals(this.klass(), other.klass())
                && Objects.equals(this.typeArgs, other.typeArgs);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.key, this.klass, this.typeArgs);
    }

    @Override
    public final String toString() {
        return "Q(key=" + this.key() + ", klass=" + this.klass() + ", typeArgs=" + this.typeArgs + ")";
    }

}
