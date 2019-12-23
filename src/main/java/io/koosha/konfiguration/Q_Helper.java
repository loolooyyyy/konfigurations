package io.koosha.konfiguration;

import io.koosha.konfiguration.error.KfgAssertionException;
import io.koosha.konfiguration.error.KfgIllegalArgumentException;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import io.koosha.konfiguration.error.KfgUnsupportedOperationException;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

final class Q_Helper {

    public static final int MAX_NESTING_LEVEL = 16;

    private Q_Helper() {
        // Utility class.
    }


    // =========================================================================

    @SuppressWarnings("rawtypes")
    @Contract(pure = true)
    static boolean match(@NotNull @NonNull final Q<?> a,
                         @NotNull @NonNull final Type b) {
        return match(a, new Q(X, b), 0);
    }

    @Contract(pure = true)
    static boolean match(@NotNull @NonNull final Q<?> a,
                         @NotNull @NonNull final Q<?> b) {
        return match(a, b, 0);
    }

    static boolean match(@NotNull @NonNull final Q<?> a,
                         @NotNull @NonNull final Q<?> b,
                         final int nestingLevel) {
        if (nestingLevel >= Q_Helper.MAX_NESTING_LEVEL)
            throw new KfgIllegalArgumentException(null, "max nesting level reached");
        if (!a.klass().isAssignableFrom(b.klass()))
            return false;
        if (a.getTypeArgs().size() != b.getTypeArgs().size())
            return false;
        final List<Q<?>> at = a.getTypeArgs();
        final List<Q<?>> bt = b.getTypeArgs();
        for (int i = 0; i < at.size(); i++)
            if (!match(at.get(i), bt.get(i)))
                return false;
        return true;
    }

    // =========================================================================

    @Contract(pure = true)
    static void checkIsConcrete(@NotNull @NonNull final Q<?> q) {
        checkIsConcrete(q.klass());
        for (Q<?> typeArg : q.getTypeArgs())
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
        checkIsConcrete(p, p);
        return p;
    }

    @Contract(pure = true)
    static ParameterizedType parametrizedTypeOf(@NotNull @NonNull final Object o) {
        final Type genericSuperclass = o.getClass().getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType))
            throw new KfgIllegalStateException(null, "encountered non generic type: " + o);
        return ((ParameterizedType) genericSuperclass);
    }

    @Contract(pure = true)
    static Type[] typeArgumentsOf(@NotNull @NonNull final Object o) {
        return parametrizedTypeOf(o).getActualTypeArguments();
    }

    static Class<?> raw(@NotNull @NonNull final Type t) {
        if (t instanceof Class)
            return upper((Class<?>) t);
        if (t instanceof ParameterizedType)
            return (Class<?>) ((ParameterizedType) t).getRawType();
        throw new KfgAssertionException("expected Class or ParameterizedType, got= " + t);
    }

    @Contract(pure = true)
    private static void checkIsConcrete(@NotNull @NonNull final Type p,
                                        @Nullable Type root) {
        if (root == null)
            root = p;

        if (!(p instanceof Class) && !(p instanceof ParameterizedType))
            throw new KfgUnsupportedOperationException(
                    "only Class and ParameterizedType are supported: "
                            + root + "::" + p);

        if (!(p instanceof ParameterizedType))
            return;

        final ParameterizedType pp = (ParameterizedType) p;
        checkIsConcrete(root, pp.getRawType());
        for (final Type ppp : pp.getActualTypeArguments())
            checkIsConcrete(root, ppp);
    }

    // =========================================================================

    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    static List<Q<?>> lq(@NotNull @NonNull final Class<?> c0) {
        return l(new Q(X, c0));
    }

    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    static List<Q<?>> lq(@NotNull @NonNull final Class<?> c0,
                         @NotNull @NonNull final Class<?> c1) {
        return l(new Q(X, c0), new Q(X, c1));
    }

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
        return Collections.unmodifiableList(Arrays.asList(a0, a1));
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

    static final String X = "io.koosha.konfiguration.Q.UNKEYED";


}
