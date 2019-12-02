package io.koosha.konfiguration;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;


public abstract class Q<TYPE> {

    private final ParameterizedType pt;
    private final Class<TYPE> klass;

    Q(final Class<TYPE> type) {
        this.pt = null;
        this.klass = type;
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return String.format("Q<>::%s::%s",
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
        if (!(obj instanceof Q))
            return false;
        final Q<?> other = (Q<?>) obj;
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


    /**
     * Check if given object matches type of Q instance.
     *
     * @param value value to check
     * @return true if types are matching (value is assignable to the type
     * represented by Q instance).
     */
    public final boolean matches(Object value) {
        requireNonNull(value, "value");
        return value.getClass().isAssignableFrom(this.klass());
    }

    public final boolean matchesType(Q<?> other) {
        requireNonNull(other, "type");
        return other.getClass().isAssignableFrom(this.klass())
                ;
    }


    /**
     * Getter: type parameters TYPE of the Q instance
     *
     * @return getter: type parameters TYPE of the Q instance
     */
    public final Class<TYPE> klass() {
        return klass;
    }

    /**
     * Getter: {@link TypeName} of the Q instance.
     *
     * @return getter: {@link TypeName} of the Q instance.
     */
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
     * If Q represents a collection, get type argument of the collection.
     *
     * @return type argument of the collection represented by Q instance.
     * @throws IllegalStateException if Q does not represent a collection.
     */
    public final Type getCollectionContainedType() {
        if (!this.typeName().isSet() && !this.typeName().isList())
            throw new IllegalStateException("not a list or set");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[0];
    }

    /**
     * If Q represents a map, get type argument of the map's key.
     *
     * @return type argument of the map's key represented by Q instance.
     * @throws IllegalStateException if Q does not represent a map.
     */
    public final Type getMapKeyType() {
        if (!this.typeName().isMap())
            throw new IllegalStateException("not a map");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[0];
    }

    /**
     * If Q represents a map, get type argument of the map's value.
     *
     * @return type argument of the map's value represented by Q instance.
     * @throws IllegalStateException if Q does not represent a map.
     */
    public final Type getMapValueType() {
        if (!this.typeName().isMap())
            throw new IllegalStateException("not a map");
        return this.pt == null ? null : this.pt.getActualTypeArguments()[1];
    }

    // =========================================================================

    /**
     * Factory method.
     *
     * @param klass the type to create a Q for.
     * @param <U>   Generic type of requested class.
     * @return a Q instance representing Class&lt;U&gt;
     */
    public static <U> Q<U> of(final Class<U> klass) {
        return new QImpl<>(klass);
    }

    public static Q<Boolean> BOOL = of(Boolean.class);
    public static Q<Character> CHAR = of(Character.class);

    public static Q<Byte> BYTE = of(Byte.class);
    public static Q<Short> SHORT = of(Short.class);
    public static Q<Integer> INT = of(Integer.class);
    public static Q<Long> LONG = of(Long.class);
    public static Q<Float> FLOAT = of(Float.class);
    public static Q<Double> DOUBLE = of(Double.class);

    public static Q<String> STRING = of(String.class);

    public static Q<Map<?, ?>> UNKNOWN_MAP = of_(Map.class);
    public static Q<Set<?>> UNKNOWN_SET = of_(Set.class);
    public static Q<List<?>> UNKNOWN_LIST = of_(List.class);


    // =========================================================================

    // Nowhere else to put this. alias of Objects.requireNonNull(...).
    static <T> T nn(final T t, final String name) {
        return requireNonNull(t, name);
    }

    private static final class QImpl<U> extends Q<U> {
        private QImpl(final Class<U> type) {
            super(type);
        }
    }

    @SuppressWarnings("unchecked")
    private static <U> Q<U> of_(final Class<?> klass) {
        return (Q<U>) of(klass);
    }

    private static class J<A extends J<A>> extends Q<A> {
    };

    private static class Z extends J<Z> {
    }

    private static void checkIsClassOrParametrizedType(final Type p, Type root) {
        new Q<J<Z>>(){};
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

}
