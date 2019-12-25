package io.koosha.konfiguration.type;

import io.koosha.konfiguration.Faktory;
import io.koosha.konfiguration.error.KfgIllegalArgumentException;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;

import static io.koosha.konfiguration.type.Matcher.match;
import static io.koosha.konfiguration.type.Matcher.matchValue0;
import static io.koosha.konfiguration.type.Q_Helper.*;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
@ThreadSafe
@Immutable
@Accessors(fluent = true)
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public class Q<TYPE> {

    @NotNull
    final String key;

    @NotNull
    final Class<TYPE> klass;

    @NotNull
    final List<Q<?>> args;

    final boolean isRoot;

    protected Q(@NotNull @NonNull final String key) {
        this(key, 0);
    }

    private Q(@NotNull @NonNull final String key,
              @NotNull @NonNull final Class<TYPE> klass,
              @NotNull @NonNull final List<@NotNull Q<?>> args) {
        this.key = key;
        this.klass = upper(klass);
        this.isRoot = true;
        this.args = copy(args);
        this.args.forEach(Objects::requireNonNull);
        ensureIsConcrete(this);
    }

    private Q(@NotNull @NonNull final String key,
              final int nestingLevel) {
        if (nestingLevel >= MAX_NESTING_LEVEL)
            throw new KfgIllegalArgumentException(null, "max nesting level reached");
        if (nestingLevel == 0)
            ensureIsConcrete(this.getClass().getGenericSuperclass());

        final Type t = typeArgumentsOf(this);

        this.isRoot = nestingLevel == 0;
        this.key = key;
        this.klass = raw(t);
        this.args = copy(typeArgumentsOf(t)
                .stream()
                .map(x -> new Q<>(X, x, nestingLevel + 1))
                .collect(toList()));

        if (this.isRoot)
            ensureIsConcrete(this);
    }

    private Q(@NotNull @NonNull final String key,
              @NotNull @NonNull final Type t,
              final int nestingLevel) {
        if (nestingLevel >= MAX_NESTING_LEVEL)
            throw new KfgIllegalArgumentException(null, "max nesting level reached");
        if (nestingLevel == 0)
            ensureIsConcrete(this.getClass().getGenericSuperclass());
        if (nestingLevel == 0)
            ensureIsConcrete(t);

        this.isRoot = nestingLevel == 0;
        this.key = key;
        this.klass = raw(t);
        this.args = copy(typeArgumentsOf(t)
                .stream()
                .map(x -> new Q<>(X, x, nestingLevel + 1))
                .collect(toList()));

        if (nestingLevel == 0)
            ensureIsConcrete(this);
    }

    @NotNull
    @Contract("_, _ -> new")
    static Q<?> of(@NotNull @NonNull final Type type,
                   final boolean isRoot) {
        return new Q<>(X, type, isRoot ? 0 : 1);
    }

    @NotNull
    @Contract("_, _ -> new")
    public static Q<?> of(@NotNull @NonNull final String key,
                          @NotNull @NonNull final Type type) {
        return new Q<>(key, type, 0);
    }

    @NotNull
    @Contract("_, _ -> new")
    public static <T> Q<T> of(@NotNull @NonNull final String key,
                              @NotNull @NonNull final Class<T> klass) {
        return new Q<>(key, klass, 0);
    }

    @NotNull
    @Contract("_, _, _ -> new")
    public static <T> Q<T> of(@NotNull @NonNull final String key,
                              @NotNull @NonNull final Class<T> klass,
                              @NotNull @NonNull final List<? extends @NotNull Type> args) {
        return new Q<>(key, klass, args.stream()
                                       .map(Objects::requireNonNull)
                                       .map(x -> of(x, false))
                                       .collect(toList()));
    }

    @NotNull
    @Contract("_, _, _ -> new")
    public static <T> Q<T> construct(@NotNull @NonNull final String key,
                                     @NotNull @NonNull final Class<T> klass,
                                     @NotNull @NonNull final List<@NotNull Q<?>> args) {
        args.forEach(Objects::requireNonNull);
        return new Q<>(key, klass, args);
    }


    // =========================================================================

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Boolean> bool(@NotNull @NonNull final String key) {
        return of(key, Boolean.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Character> char_(@NotNull @NonNull final String key) {
        return of(key, Character.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Byte> byte_(@NotNull @NonNull final String key) {
        return of(key, Byte.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Short> short_(@NotNull @NonNull final String key) {
        return of(key, Short.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Integer> int_(@NotNull @NonNull final String key) {
        return of(key, Integer.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Long> long_(@NotNull @NonNull final String key) {
        return of(key, Long.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Float> float_(@NotNull @NonNull final String key) {
        return of(key, Float.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Double> double_(@NotNull @NonNull final String key) {
        return of(key, Double.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<String> string(@NotNull @NonNull final String key) {
        return of(key, String.class);
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<?> unknown(@NotNull @NonNull final String key) {
        return of(key, Object.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Map<?, ?>> unknownMap(@NotNull @NonNull final String key) {
        return (Q) of(key, Map.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<Set<?>> unknownSet(@NotNull @NonNull final String key) {
        return (Q) of(key, Set.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    public static Q<List<?>> unknownList(@NotNull @NonNull final String key) {
        return (Q) of(key, List.class);
    }

    // =========================================================================

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
        return of(key, (Class) List.class, l(klass));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    public static <U> Q<Set<U>> setOf(@NonNull @NotNull final String key,
                                      @NotNull @NonNull final Class<U> klass) {
        return of(key, (Class) Set.class, l(klass));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(value = "_, _, _ -> new",
            pure = true)
    public static <U, V> Q<Map<U, V>> mapOf(@NotNull @NonNull final String key,
                                            @NotNull @NonNull final Class<U> klassK,
                                            @NotNull @NonNull final Class<V> klassV) {
        return of(key, (Class) Map.class, l(klassK, klassV));
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
        return matchValue0(this, v);
    }

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
    public final Q<TYPE> withKey(@NotNull @NonNull final String newKey) {
        if (newKey.isEmpty())
            throw new KfgIllegalArgumentException(null, "empty key");

        return Objects.equals(this.key, newKey)
               ? construct(newKey, this.klass, this.args)
               : this;
    }

    @Contract(pure = true)
    @NotNull
    public final Class<?> getCollectionContainedClass() {
        if (!Collection.class.isAssignableFrom(this.klass))
            throw new KfgIllegalStateException(null, "type is not a collection: " + this);
        return this.args.size() < 1 ? Object.class : this.args.get(0).klass;
    }

    @Contract(pure = true,
            value = "->new")
    @NotNull
    public final Q<?> getCollectionContainedQ() {
        final Class<?> containedClass = this.getCollectionContainedClass();
        return of(this.key + "::collection", containedClass);
    }

    @Contract(pure = true)
    @NotNull
    public final Class<?> getMapKeyClass() {
        if (!Map.class.isAssignableFrom(this.klass))
            throw new KfgIllegalStateException(null, unknownMap(this.key), null, "type is not a map");
        return this.args.size() < 1 ? Object.class : this.args.get(0).klass;
    }

    @Contract(pure = true)
    @NotNull
    public final Class<?> getMapValueClass() {
        if (!Map.class.isAssignableFrom(this.klass))
            throw new KfgIllegalStateException(null, unknownMap(this.key), null, "type is not a map");
        return this.args.size() < 2 ? Object.class : this.args.get(1).klass;
    }

    @Contract(pure = true,
            value = "->new")
    @NotNull
    public final Q<?> getMapKeyQ() {
        return of(this.key + "::map-key", this.getMapKeyClass());
    }

    @Contract(pure = true,
            value = "->new")
    @NotNull
    public final Q<?> getMapValueQ() {
        return of(this.key + "::map-value", this.getMapValueClass());
    }

    @NotNull
    @Contract(pure = true)
    public final List<Q<?>> args() {
        return this.args;
    }

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
    public final boolean isNumber() {
        return isByte() || isShort() || isInt() || isLong() || isFloat() || isDouble();
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
        return this.isVoid();
    }

    @Contract(pure = true)
    public final boolean isVoid() {
        return Void.class.isAssignableFrom(this.klass);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Q))
            return false;
        final Q<?> other = (Q<?>) obj;
        return Objects.equals(this.key(), other.key())
                && Objects.equals(this.klass(), other.klass())
                && Objects.equals(this.args, other.args);
    }

    @Override
    public final int hashCode() {
        return this.args.hashCode() + 31 * (
                this.klass.hashCode() + 31 * (
                        31 + this.key.hashCode()));
    }

    @Override
    public final String toString() {
        return toString_(this);
    }

}
