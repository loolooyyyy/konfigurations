package io.koosha.konfiguration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.koosha.konfiguration.T.typeNameOf;
import static io.koosha.konfiguration.TypeName.*;
import static java.util.Objects.requireNonNull;

/**
 * Reads konfig from a plain java map.
 *
 * <p>To fulfill contract of {@link Konfiguration}, all the values put in the
 * map of konfiguration key/values supplied to the konfiguration, should be
 * immutable.
 *
 * <p>Thread safe and immutable.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractKonfiguration implements Konfiguration {

    private final String name;

    protected AbstractKonfiguration(final String name) {
        this.name = requireNonNull(name, "name");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getName() {
        return name;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Boolean> bool(final String key) {
        requireNonNull(key, "key");
        final Object v = this.getPrimitive(key, BOOL);

        if (v == null && !isNullOk(key, T.BOOLEAN))
            throw new KfgTypeNullException(this, key, BOOL, T.BOOLEAN);
        if (v == null)
            return DummyV.of(key, null);

        final Boolean vv = toBool(v);
        if (vv == null)
            throw new KfgTypeException(this, key, BOOL, T.BOOLEAN, v);

        return DummyV.of(key, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Character> char_(final String key) {
        requireNonNull(key, "key");

        final Object v = this.getPrimitive(key, CHAR);
        if (v == null && !isNullOk(key, T.CHARACTER))
            throw new KfgTypeException(this, key, BYTE, T.BYTE, null);
        if (v == null)
            return DummyV.of(key, null);

        final Character vv;
        try {
            vv = (Character) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this, key, CHAR, T.CHARACTER, null);
        }

        return DummyV.of(key, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<String> string(final String key) {
        requireNonNull(key, "key");

        final Object v = this.getPrimitive(key, STRING);

        if (v == null && !isNullOk(key, T.STRING))
            throw new KfgTypeException(this, key, STRING, T.STRING, null);
        if (v == null)
            return DummyV.of(key, null);

        final String vv;
        try {
            vv = (String) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this, key, STRING, T.STRING, v);
        }

        return DummyV.of(key, vv);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Byte> byte_(final String key) {
        requireNonNull(key, "key");

        final Object v = this.getPrimitive(key, BYTE);
        if (v == null && !isNullOk(key, T.BYTE))
            throw new KfgTypeException(this, key, BYTE, T.BYTE, null);
        if (v == null)
            return DummyV.of(key, null);


        final Long vv = toIntegral(v, Byte.MIN_VALUE, Byte.MAX_VALUE);
        if (vv == null)
            throw new KfgTypeException(this, key, BYTE, T.BYTE, v);

        return DummyV.of(key, vv.byteValue());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Short> short_(final String key) {
        requireNonNull(key, "key");

        final Object v = this.getPrimitive(key, BYTE);
        if (v == null && !isNullOk(key, T.SHORT))
            throw new KfgTypeException(this, key, SHORT, T.SHORT, null);
        if (v == null)
            return null;

        final Long vv = toIntegral(v, Short.MIN_VALUE, Short.MAX_VALUE);
        if (vv == null)
            throw new KfgTypeException(this, key, SHORT, T.SHORT, v);

        return DummyV.of(key, vv.shortValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Integer> int_(final String key) {
        requireNonNull(key, "key");

        final Object v = this.getPrimitive(key, INT);
        if (v == null && !isNullOk(key, T.INTEGER))
            throw new KfgTypeException(this, key, INT, T.INTEGER, null);
        if (v == null)
            return DummyV.of(key, null);

        final Long vv = toIntegral(v, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (vv == null)
            throw new KfgTypeException(this, key, INT, T.INTEGER, v);

        return DummyV.of(key, vv.intValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Long> long_(final String key) {
        requireNonNull(key, "key");

        final Object v = this.getPrimitive(key, LONG);
        if (v == null && !isNullOk(key, T.LONG))
            throw new KfgTypeException(this, key, LONG, T.LONG, null);
        if (v == null)
            return DummyV.of(key, null);

        final Long vv = toIntegral(v, Long.MIN_VALUE, Long.MAX_VALUE);
        if (vv == null)
            throw new KfgTypeException(this, key, LONG, T.LONG, v);

        return DummyV.of(key, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Float> float_(final String key) {
        requireNonNull(key, "key");

        final Object v = this.getPrimitive(key, FLOAT);
        if (v == null && !isNullOk(key, T.FLOAT))
            throw new KfgTypeException(this, key, FLOAT, T.FLOAT, null);

        if (v == null)
            return DummyV.of(key, null);

        final Float vv = toFloat(v);
        if (vv == null)
            throw new KfgTypeException(this, key, FLOAT, T.FLOAT, vv);

        return DummyV.of(key, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Double> double_(final String key) {
        requireNonNull(key, "key");

        final Object v = this.getPrimitive(key, DOUBLE);
        if (v == null && !isNullOk(key, T.DOUBLE))
            throw new KfgTypeException(this, key, DOUBLE, T.DOUBLE, null);

        if (v == null)
            return DummyV.of(key, null);

        final Double vv = toDouble(v);
        if (vv == null)
            throw new KfgTypeException(this, key, DOUBLE, T.DOUBLE, vv);

        return DummyV.of(key, vv);
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <U> K<List<U>> list(final String key,
                                     final T<List<U>> type) {
        requireNonNull(key, "key");

        final Object v = this.getContainer(key, type);

        if (v == null && !isNullOk(key, type))
            throw new KfgTypeException(this, key, LIST, type, null);

        if (v == null)
            return DummyV.of(key, null);

        final List<?> vv;
        try {
            vv = (List<?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this, key, LIST, type, v);
        }

        if (type != null)
            this.checkCollectionType(key, LIST, type, vv);

        return DummyV.of(key, (List<U>) vv);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <U> K<Set<U>> set(final String key,
                                   final T<Set<U>> type) {
        requireNonNull(key, "key");

        final Object v = this.getContainer(key, type);

        if (v == null && !isNullOk(key, type))
            throw new KfgTypeException(this, key, SET, type, null);
        if (v == null)
            return DummyV.of(key, null);

        final Set<?> vv;
        try {
            vv = (Set<?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this, key, SET, type, v);
        }

        if (type != null)
            this.checkCollectionType(key, SET, type, vv);

        return DummyV.of(key, (Set<U>) vv);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <U, V> K<Map<U, V>> map(final String key,
                                         final T<Map<U, V>> type) {
        requireNonNull(key, "key");

        final Object v = this.getContainer(key, type);
        if (v == null && !isNullOk(key, type))
            throw new KfgTypeException(this, key, MAP, type, null);
        if (v == null)
            return DummyV.of(key, null);

        final Map<U, V> vv;
        try {
            vv = (Map<U, V>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this, key, MAP, type, v);
        }

        if (type != null)
            this.checkCollectionType(key, MAP, type, vv);

        return DummyV.of(key, vv);
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <U> K<U> custom(final String key,
                                 final T<U> type) {
        requireNonNull(key, "key");

        if (type != null && type.typeName().isPrimitive()) {
            if (type.typeName().isBool())
                return (K<U>) bool(key);
            if (type.typeName().isChar())
                return (K<U>) char_(key);
            if (type.typeName().isString())
                return (K<U>) string(key);

            if (type.typeName().isByte())
                return (K<U>) byte_(key);
            if (type.typeName().isShort())
                return (K<U>) short_(key);
            if (type.typeName().isInt())
                return (K<U>) int_(key);
            if (type.typeName().isLong())
                return (K<U>) long_(key);
            if (type.typeName().isDouble())
                return (K<U>) float_(key);
            if (type.typeName().isFloat())
                return (K<U>) double_(key);
        }

        if (type != null && type.typeName().isContainer()) {
            if (type.typeName().isList())
                return (K<U>) list(key, (T) type);
            if (type.typeName().isMap())
                return (K<U>) map(key, (T) type);
            if (type.typeName().isSet())
                return (K<U>) set(key, (T) type);
        }

        final T t = (T) this.getCustom(key, type);

        if (t == null && !this.isNullOk(key, type))
            throw new KfgTypeNullException(this, key, typeNameOf(type), type);
        if (t == null)
            return DummyV.of(key, null);

        if (type != null)
            this.checkType(key, type, t);

        return DummyV.of(key, (U) t);
    }


    /**
     * Get config value of a primitive type according to {@link TypeName#isPrimitive()}.
     *
     * @param key  config key
     * @param type generic type of container
     * @return the konfig value for the given key.
     */
    protected abstract Object getPrimitive(String key, TypeName type);

    /**
     * Get config value of custom type {@code U}.
     *
     * @param key  config key
     * @param type generic type of container
     * @param <U>  generic type of wanted konfig.
     * @return the konfig value for the given key.
     */
    protected abstract <U> U getCustom(String key, T<U> type);

    /**
     * Get config value of a collection type (set, map or list) of generic type T.
     * <p>
     * The generic type of the collection will be U.
     *
     * @param key  config key
     * @param type generic type of container
     * @return a container type (list, map, set) of generic type {@code type}.
     */
    protected abstract Object getContainer(String key, T<?> type);


    /**
     * Handle the case where value of a key is null.
     *
     * @param key  the config key who's value is null.
     * @param type type of requested konfig.
     * @return true if it's ok to have null values.
     */
    @SuppressWarnings("unused")
    protected boolean isNullOk(final String key,
                               final T<?> type) {
        return true;
    }

    /**
     * Handle the case where value in a collection is null
     *
     * @param key   the config key who's collection has a null.
     * @param value the map, list or set collection in question.
     * @param type  type of requested konfig.
     * @return true if it's ok to have null values.
     */
    @SuppressWarnings("unused")
    protected boolean isNullOkInCollection(final String key,
                                           final T<?> type,
                                           Object value) {
        return true;
    }

    /**
     * Make sure the value is of the requested type.
     *
     * @param neededType type asked for.
     * @param key        the config key whose value is being checked.
     * @param value      the value in question.
     * @throws KfgTypeException if the requested type does not match the type
     *                          of value in the given in.
     */
    protected void checkType(final String key,
                             final T<?> neededType,
                             final Object value) {
        checkType0(neededType, key, value);
    }

    /**
     * Make sure the value is of the requested type.
     *
     * @param key            the config key whose value is being checked
     * @param collectionType type type of collection requested (list, set, or
     *                       map).
     * @param neededType     type asked for.
     * @param value          the value in question
     * @throws KfgTypeException if the requested type does not match the type
     *                          of value in the given in.
     */
    protected void checkCollectionType(final String key,
                                       final TypeName collectionType,
                                       final T<?> neededType,
                                       final Object value) {
        if (neededType == null)
            return;

        requireNonNull(key, "key");
        requireNonNull(value, "value");
        requireNonNull(collectionType, "collectionType");

        if (collectionType == MAP) {
            for (final Object o : ((Map<?, ?>) value).values())
                if (o != null)
                    try {
                        checkType0(neededType, key, o);
                    }
                    catch (KfgTypeException k) {
                        throw new KfgTypeException(this, key, collectionType, neededType, value);
                    }
                else if (!isNullOkInCollection(key, neededType, value))
                    throw new KfgTypeNullException(this, key, collectionType, neededType);

            for (final Object o : ((Map<?, ?>) value).keySet())
                if (o != null)
                    try {
                        checkType0(neededType, key, o);
                    }
                    catch (KfgTypeException k) {
                        throw new KfgTypeException(this, key, collectionType, neededType, value);
                    }
                else if (!isNullOkInCollection(key, neededType, value))
                    throw new KfgTypeNullException(this, key, collectionType, neededType);
        }
        else {
            for (final Object o : (Collection<?>) value)
                if (o != null)
                    try {
                        checkType0(neededType, key, o);
                    }
                    catch (KfgTypeException k) {
                        throw new KfgTypeException(this, key, collectionType, neededType, value);
                    }
                else if (!isNullOkInCollection(key, neededType, value))
                    throw new KfgTypeNullException(this, key, collectionType, neededType);
        }
    }

    /**
     * Default implementation of {@link #checkType(String, T, Object)} .
     *
     * @param neededType see checkType().
     * @param key        see checkType().
     * @param value      see checkType().
     * @throws KfgTypeException see checkType().
     * @see #checkType(String, T, Object)
     */
    protected final void checkType0(final T<?> neededType,
                                    final String key,
                                    final Object value) {
        if (neededType != null && value != null && !neededType.matches(value))
            throw new KfgTypeException(this, key, neededType.typeName(), neededType, value);
    }


    private static Boolean toBool(final Object o) {
        final Long l = toIntegral(o, 0, 1);
        if (l != null)
            //noinspection RedundantConditionalExpression
            return l == 0 ? false : true;
        if (o instanceof Boolean)
            return (Boolean) o;
        return null;
    }

    private static Long toIntegral(final Object o, final long min, final long max) {
        if (!(o instanceof Byte || o instanceof Short ||
                o instanceof Integer || o instanceof Long))
            return null;

        if (((Number) o).longValue() < min || max < ((Number) o).longValue())
            return null;

        return ((Number) o).longValue();
    }

    private static Float toFloat(final Object o) {
        if (!(o instanceof Float || o instanceof Double))
            return null;

        if (((Number) o).doubleValue() < Float.MIN_VALUE
                || Float.MAX_VALUE < ((Number) o).doubleValue())
            return null;

        return ((Number) o).floatValue();
    }

    private static Double toDouble(final Object o) {
        if (!(o instanceof Float || o instanceof Double))
            return null;

        return ((Number) o).doubleValue();
    }

}
