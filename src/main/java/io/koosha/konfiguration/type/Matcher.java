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
    static boolean matchValue(@NotNull @NonNull final Q<?> a,
                              @NotNull @NonNull final Type b,
                              final int nestingLevel) {
        if (nestingLevel >= Q_Helper.MAX_NESTING_LEVEL)
            throw new KfgIllegalArgumentException(null, "max nesting level reached");

        if (b instanceof Class)
            return a.klass.isAssignableFrom((Class<?>) b);

        if (!(b instanceof ParameterizedType))
            throw new KfgIllegalArgumentException(null,
                    "encountered non concrete type: " + b + " while checking: " + a);

        final Type[] ac = ((ParameterizedType) b).getActualTypeArguments();
        if (ac.length != a.args.size())
            return false;
        for (int i = 0; i < ac.length; i++)
            if (!match(a.args.get(i), ac[i], nestingLevel + 1))
                return false;
        return false;
    }

    // =========================================================================

    @Contract(pure = true)
    public static boolean match(@NotNull @NonNull final Q<?> a,
                                @NotNull @NonNull final Type b) {
        return match(a, b, 0);
    }

    @Contract(pure = true)
    static boolean match(@NotNull @NonNull final Q<?> a,
                         @NotNull @NonNull final Type b,
                         final int nestingLevel) {
        return match(a, of(b, true), nestingLevel);
    }

    @Contract(pure = true)
    public static boolean match(@NotNull @NonNull final Q<?> a,
                                @NotNull @NonNull final Q<?> b) {
        return match(a, b, 0);
    }

    @Contract(pure = true)
    static boolean match(@NotNull @NonNull final Q<?> a,
                         @NotNull @NonNull final Q<?> b,
                         final int nestingLevel) {
        if (nestingLevel >= Q_Helper.MAX_NESTING_LEVEL)
            throw new KfgIllegalArgumentException(null, "max nesting level reached");
        if (!a.klass().isAssignableFrom(b.klass()))
            return false;
        if (a.args().size() != b.args().size())
            return false;
        for (int i = 0; i < a.args().size(); i++)
            if (!match(a.args().get(i), b.args().get(i)))
                return false;
        return true;
    }


}
