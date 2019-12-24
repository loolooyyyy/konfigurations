package io.koosha.konfiguration.type;

import io.koosha.konfiguration.error.KfgAssertionException;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import io.koosha.konfiguration.error.KfgUnsupportedOperationException;
import lombok.NonNull;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static java.lang.String.format;
import static java.util.Arrays.asList;

@Immutable
@ThreadSafe
final class Q_Helper {

    public static final int MAX_NESTING_LEVEL = 16;

    private Q_Helper() {
        // Utility class.
    }

    // =========================================================================

    @Contract(pure = true)
    static void checkIsConcrete(@NotNull @NonNull final Q<?> q) {
        checkIsConcrete(q.klass());
        for (Q<?> typeArg : q.args())
            checkIsConcrete(typeArg);
    }

    @SuppressWarnings("unchecked")
    @Contract(pure = true,
            value = "null->null; _ -> _ ")
    static <T> Class<T> upper(@Nullable final Class<T> t) {
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
    static Type checkIsConcrete(@NotNull @NonNull final Type p) {
        if (!isConcrete(p, p))
            throw new KfgUnsupportedOperationException(
                    "only Class and ParameterizedType are supported: " + p);
        return p;
    }

    @Contract(pure = true)
    static Type typeArgumentsOf(@NotNull @NonNull final Object o) {
        final Type genericSuperclass = o.getClass().getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType))
            throw new KfgIllegalStateException(null, "encountered non generic type: " + o);
        final Type[] a = ((ParameterizedType) genericSuperclass)
                .getActualTypeArguments();
        if (a.length == 0)
            throw new KfgIllegalStateException(null, "encountered non generic type: " + o);
        if (a.length > 1)
            throw new KfgIllegalStateException(null, "too many generic types, expecting one: " + o);
        return a[0];
    }

    static List<Type> typeArgumentsOf(@NotNull @NonNull final Type t) {
        return t instanceof ParameterizedType
               ? asList(((ParameterizedType) t).getActualTypeArguments())
               : l();
    }

    @SuppressWarnings("unchecked")
    @Contract(pure = true)
    static <T> Class<T> raw(@NotNull @NonNull final Type t) {
        if (t instanceof Class)
            return upper((Class<T>) t);
        if (t instanceof ParameterizedType)
            return upper((Class<T>) ((ParameterizedType) t).getRawType());
        throw new KfgAssertionException("expected Class or ParameterizedType, got= " + t);
    }

    @Contract(pure = true)
    private static boolean isConcrete(@NotNull @NonNull final Type p,
                                      @Nullable Type root) {
        if (root == null)
            root = p;

        if (!(p instanceof Class) && !(p instanceof ParameterizedType))
            return false;

        if (!(p instanceof ParameterizedType))
            return true;

        final ParameterizedType pp = (ParameterizedType) p;
        if (!isConcrete(root, pp.getRawType()))
            return false;
        for (final Type ppp : pp.getActualTypeArguments())
            if (!isConcrete(root, ppp))
                return false;
        return true;
    }

    // =========================================================================

    @Contract(pure = true,
            value = "->new")
    @NotNull
    static <T> List<T> l() {
        return Collections.emptyList();
    }

    @Contract(pure = true,
            value = "_->new")
    @NotNull
    static <T> List<T> l(final T a) {
        return Collections.singletonList(a);
    }

    @Contract(pure = true,
            value = "_, _->new")
    @NotNull
    static <T> List<T> l(final T a0,
                         final T a1) {
        return Collections.unmodifiableList(asList(a0, a1));
    }

    @Contract(pure = true,
            value = "_->new")
    @NotNull
    static <T> List<T> copy(@NotNull @NonNull final Collection<T> c) {
        if (c.size() == 0)
            return Collections.emptyList();
        if (c.size() == 1)
            for (final T t : c)
                return Collections.singletonList(t);
        return Collections.unmodifiableList(new ArrayList<>(c));
    }

    static String toString_(@NotNull @NonNull final Q<?> q) {
        String s = q.args.toString();
        if (s.startsWith("[") && s.endsWith("]"))
            s = s.substring(1, s.length() - 1);

        String klass = q.klass.getName();
        if (klass.startsWith("java.lang.") || klass.startsWith("java.util."))
            klass = klass.substring(10);

        return format("%s%s%s",
                q.isRoot ? "Q:" + q.key + ":" : "",
                klass,
                q.args.isEmpty() ? "" : "<" + s + ">");
    }

    static final String X = "io.koosha.konfiguration.type.Q.UNKEYED";

}
