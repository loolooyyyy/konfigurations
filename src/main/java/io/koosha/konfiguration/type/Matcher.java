package io.koosha.konfiguration.type;

import io.koosha.konfiguration.error.KfgIllegalArgumentException;
import lombok.NonNull;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import static io.koosha.konfiguration.type.Q.of;

@Immutable
@ThreadSafe
public final class Matcher {

    private Matcher() {

    }

    // =========================================================================

    @Contract(pure = true)
    public static boolean matchValue0(@NotNull @NonNull final Q<?> a,
                                      @Nullable final Object v) {
        return v == null ||
                Objects.equals(a.klass(), Object.class) ||
                a.klass().isAssignableFrom(v.getClass())
                        && matchValue(a, v.getClass(), 0);
    }

    @Contract(pure = true)
    static boolean matchValue(@NotNull @NonNull final Q<?> q0,
                              @NotNull @NonNull final Type q1,
                              final int nestingLevel) {
        if (nestingLevel >= Q_Helper.MAX_NESTING_LEVEL)
            throw new KfgIllegalArgumentException(null, "max nesting level reached");

        if (q1 instanceof Class)
            return q0.klass.isAssignableFrom((Class<?>) q1);

        if (!(q1 instanceof ParameterizedType))
            throw new KfgIllegalArgumentException(null,
                    "encountered non concrete type: " + q1 + " while checking: " + q0);

        final Type[] ac = ((ParameterizedType) q1).getActualTypeArguments();
        if (ac.length != q0.args.size())
            return false;
        for (int i = 0; i < ac.length; i++)
            if (!match(q0.args.get(i), ac[i], nestingLevel + 1))
                return false;
        return false;
    }

    // =========================================================================

    @Contract(pure = true)
    public static boolean match(@NotNull @NonNull final Q<?> q0,
                                @NotNull @NonNull final Type q1) {
        return match(q0, q1, 0);
    }

    @Contract(pure = true)
    static boolean match(@NotNull @NonNull final Q<?> q0,
                         @NotNull @NonNull final Type q1,
                         final int nestingLevel) {
        return match(q0, of(q1, true), nestingLevel);
    }

    @Contract(pure = true)
    public static boolean match(@NotNull @NonNull final Q<?> q0,
                                @NotNull @NonNull final Q<?> q1) {
        return match(q0, q1, 0);
    }

    @Contract(pure = true)
    static boolean match(@NotNull @NonNull final Q<?> q0,
                         @NotNull @NonNull final Q<?> q1,
                         final int nestingLevel) {
        if (nestingLevel >= Q_Helper.MAX_NESTING_LEVEL)
            throw new KfgIllegalArgumentException(null, "max nesting level reached");
        if (!q0.klass().isAssignableFrom(q1.klass()))
            return false;
        if (q0.args().size() != q1.args().size())
            return false;
        for (int i = 0; i < q0.args().size(); i++)
            if (!match(q0.args().get(i), q1.args().get(i)))
                return false;
        return true;
    }


}
