package io.koosha.konfiguration;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;


@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class T<TYPE> {

    private final ParameterizedType pt;
    private final Class<?> klass;

    private static void check(final Type p, Type root) {
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

        check(root, pp.getRawType());
        for (final Type ppp : pp.getActualTypeArguments())
            check(root, ppp);
    }

    T(final Class<TYPE> type) {
        this.pt = null;
        this.klass = type;
    }

    protected T() {
        final Type t = ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];

        check(t, null);

        if (t instanceof ParameterizedType) {
            this.pt = (ParameterizedType) t;
            this.klass = (Class<?>) this.pt.getRawType();
        }
        else {
            this.pt = null;
            this.klass = (Class<?>) t;
        }
    }

    /**
     * Get type parameters TYPE of {@code this} concrete class.
     *
     * @return as said.
     */
    public final Class<?> klass() {
        return klass;
    }

    public final TypeName typeName() {
        if (klass() == null)
            return TypeName.CUSTOM;

        if (List.class.isAssignableFrom(klass()))
            return TypeName.LIST;
        else if (Map.class.isAssignableFrom(klass()))
            return TypeName.MAP;
        else if (Set.class.isAssignableFrom(klass()))
            return TypeName.SET;

        else if (Double.class.isAssignableFrom(klass()))
            return TypeName.DOUBLE;
        else if (Float.class.isAssignableFrom(klass()))
            return TypeName.FLOAT;
        else if (Long.class.isAssignableFrom(klass()))
            return TypeName.LONG;
        else if (Integer.class.isAssignableFrom(klass()))
            return TypeName.INT;
        else if (Short.class.isAssignableFrom(klass()))
            return TypeName.SHORT;
        else if (Byte.class.isAssignableFrom(klass()))
            return TypeName.BYTE;

        else if (Boolean.class.isAssignableFrom(klass()))
            return TypeName.BOOL;
        else if (Character.class.isAssignableFrom(klass()))
            return TypeName.CHAR;

        else if (String.class.isAssignableFrom(klass()))
            return TypeName.STRING;

        else
            return TypeName.CUSTOM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return String.format("T<>::%s::%s",
                this.typeName(),
                this.klass() == null ? "?" : this.klass().getTypeName()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return Objects.hash(this.pt, this.klass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof T))
            return false;
        final T other = (T) obj;
        return Objects.equals(this.pt, other.pt) &&
                Objects.equals(this.klass, other.klass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }


    public final boolean matches(Object value) {
        requireNonNull(value, "value");
        if (!value.getClass().isAssignableFrom(this.klass()))
            return false;
        return true;
    }


    public static <U> T<U> of(final Class<U> klass) {
        return new TImpl<>(klass);
    }

    public static TypeName typeNameOf(final T t) {
        return t == null ? null : t.typeName();
    }

    // =========================================================================

    final Type getCollectionContainedType() {
        if (!this.typeName().isSet() && !this.typeName().isList())
            throw new IllegalStateException("not a list or set");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[0];
    }

    final Type getMapKeyType() {
        if (!this.typeName().isMap())
            throw new IllegalStateException("not a map");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[0];
    }

    final Type getMapValueType() {
        if (!this.typeName().isMap())
            throw new IllegalStateException("not a map");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[1];
    }

    private static final class TImpl<U> extends T<U> {
        private TImpl(final Class<U> type) {
            super(type);
        }
    }

    @SuppressWarnings("unchecked")
    private static <U> T<U> of_(final Class<?> klass) {
        return (T<U>) of(klass);
    }

    // =========================================================================

    public static T<Boolean> BOOLEAN = of(Boolean.class);
    public static T<Character> CHARACTER = of(Character.class);

    public static T<Byte> BYTE = of(Byte.class);
    public static T<Short> SHORT = of(Short.class);
    public static T<Integer> INTEGER = of(Integer.class);
    public static T<Long> LONG = of(Long.class);
    public static T<Float> FLOAT = of(Float.class);
    public static T<Double> DOUBLE = of(Double.class);

    public static T<String> STRING = of(String.class);

    public static T<Map<?, ?>> UNKNOWN_MAP = of_(Map.class);
    public static T<Set<?>> UNKNOWN_SET = of_(Set.class);
    public static T<List<?>> UNKNOWN_LIST = of_(List.class);

}
