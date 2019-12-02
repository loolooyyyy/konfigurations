package io.koosha.konfiguration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.koosha.konfiguration.Q.nn;

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
        this.name = nn(name, "name");
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
        nn(key, "key");

        final Q<Boolean> q = Q.BOOL;
        final Object v = this.getPrimitive(key, q);

        if (v == null && !isNullOk(key, q))
            throw new KfgTypeNullException(this, key, q);
        if (v == null)
            return this.null_(key, q);

        final Boolean vv = toBool(v);
        if (vv == null)
            throw new KfgTypeException(this, key, q, v);

        return this.k(key, q, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Character> char_(final String key) {
        nn(key, "key");

        final Q<Character> q = Q.CHAR;
        final Object v = this.getPrimitive(key, q);

        if (v == null && !isNullOk(key, q))
            throw new KfgTypeNullException(this, key, q);
        if (v == null)
            return this.null_(key, q);

        char vv;
        try {
            vv = (Character) v;
        }
        catch (final ClassCastException cc0) {
            try {
                final String str = (String) v;
                if (str.length() != 1)
                    throw cc0;
                else
                    vv = str.charAt(0);
            }
            catch (final ClassCastException cce1) {
                throw new KfgTypeException(this, key, q, v);
            }
        }

        return this.k(key, q, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<String> string(final String key) {
        nn(key, "key");

        final Q<String> q = Q.STRING;
        final Object v = this.getPrimitive(key, q);

        if (v == null && !isNullOk(key, Q.STRING))
            throw new KfgTypeNullException(this, key, q);
        if (v == null)
            return null_(key, q);

        final String vv;
        try {
            vv = (String) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this, key, q, v);
        }

        return this.k(key, q, vv);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Byte> byte_(final String key) {
        nn(key, "key");

        final Q<Byte> q = Q.BYTE;
        final long min = Byte.MIN_VALUE;
        final long max = Byte.MAX_VALUE;

        final Object v = this.getPrimitive(key, q);

        if (v == null && !isNullOk(key, q))
            throw new KfgTypeNullException(this, key, q);
        if (v == null)
            return null_(key, q);

        final Long vv = toIntegral(v, min, max);
        if (vv == null)
            throw new KfgTypeException(this, key, q, v);

        return this.k(key, q, vv.byteValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Short> short_(final String key) {
        nn(key, "key");

        final Q<Short> q = Q.SHORT;
        final long min = Short.MIN_VALUE;
        final long max = Short.MAX_VALUE;

        final Object v = this.getPrimitive(key, q);

        if (v == null && !isNullOk(key, q))
            throw new KfgTypeNullException(this, key, q);
        if (v == null)
            return null_(key, q);

        final Long vv = toIntegral(v, min, max);
        if (vv == null)
            throw new KfgTypeException(this, key, q, v);

        return this.k(key, q, vv.shortValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Integer> int_(final String key) {
        nn(key, "key");

        final Q<Integer> q = Q.INT;
        final long min = Integer.MIN_VALUE;
        final long max = Integer.MAX_VALUE;

        final Object v = this.getPrimitive(key, q);

        if (v == null && !isNullOk(key, q))
            throw new KfgTypeNullException(this, key, q);
        if (v == null)
            return null_(key, q);

        final Long vv = toIntegral(v, min, max);
        if (vv == null)
            throw new KfgTypeException(this, key, q, v);

        return this.k(key, q, vv.intValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Long> long_(final String key) {
        nn(key, "key");

        final Q<Long> q = Q.LONG;
        final long min = Long.MIN_VALUE;
        final long max = Long.MAX_VALUE;

        final Object v = this.getPrimitive(key, q);

        if (v == null && !isNullOk(key, q))
            throw new KfgTypeNullException(this, key, q);
        if (v == null)
            return null_(key, q);

        final Long vv = toIntegral(v, min, max);
        if (vv == null)
            throw new KfgTypeException(this, key, q, v);

        return this.k(key, q, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Float> float_(final String key) {
        nn(key, "key");

        final Q<Float> q = Q.FLOAT;
        final Object v = this.getPrimitive(key, q);

        if (v == null && !isNullOk(key, q))
            throw new KfgTypeNullException(this, key, q);
        if (v == null)
            return null_(key, q);

        final Float vv = toFloat(v);
        if (vv == null)
            throw new KfgTypeException(this, key, q, v);

        return this.k(key, q, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final K<Double> double_(final String key) {
        nn(key, "key");

        final Q<Double> q = Q.DOUBLE;
        final Object v = this.getPrimitive(key, q);

        if (v == null && !isNullOk(key, q))
            throw new KfgTypeNullException(this, key, q);
        if (v == null)
            return null_(key, q);

        final Double vv = toDouble(v);
        if (vv == null)
            throw new KfgTypeException(this, key, q, v);

        return this.k(key, q, vv);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final <U> K<List<U>> list(final String key,
                                     final Q<List<U>> type) {
        nn(key, "key");

        final Object v = this.getContainer(key, type);

        if (v == null && !isNullOk(key, type))
            throw new KfgTypeNullException(this, key, type);
        if (v == null)
            return null_(key, type);

        final List<?> vv;
        try {
            vv = (List<?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this, key, type, v);
        }

        if (type != null)
            this.checkCollectionType(key, type, vv);

        return this.k(key, type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <U> K<Set<U>> set(final String key,
                                   final Q<Set<U>> type) {
        nn(key, "key");

        final Object v = this.getContainer(key, type);

        if (v == null && !isNullOk(key, type))
            throw new KfgTypeNullException(this, key, type);
        if (v == null)
            return null_(key, type);

        final Set<?> vv;
        try {
            vv = (Set<?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this, key, type, v);
        }

        if (type != null)
            this.checkCollectionType(key, type, vv);

        return this.k(key, type, vv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <U, V> K<Map<U, V>> map(final String key,
                                         final Q<Map<U, V>> type) {
        nn(key, "key");

        final Object v = this.getContainer(key, type);

        if (v == null && !isNullOk(key, type))
            throw new KfgTypeNullException(this, key, type);
        if (v == null)
            return null_(key, type);

        final Map<?, ?> vv;
        try {
            vv = (Map<?, ?>) v;
        }
        catch (final ClassCastException cce) {
            throw new KfgTypeException(this, key, type, v);
        }

        if (type != null)
            this.checkCollectionType(key, type, vv);

        return this.k(key, type, vv);
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <U> K<U> custom(final String key,
                                 final Q<U> type) {
        nn(key, "key");

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
            @SuppressWarnings("rawtypes")
            final Q raw = type;
            if (type.typeName().isList())
                return (K<U>) list(key, raw);
            if (type.typeName().isMap())
                return (K<U>) map(key, raw);
            if (type.typeName().isSet())
                return (K<U>) set(key, raw);
        }

        final U v = this.getCustom(key, type);

        if (v == null && !this.isNullOk(key, type))
            throw new KfgTypeNullException(this, key, type);
        if (v == null)
            return null_(key, null);

        if (type != null)
            this.checkType(key, type, v);

        return this.k(key, type, v);
    }


    /**
     * Call {@link #k(String, Q, Object)} with {@code null} value.
     *
     * @param key  config key
     * @param type type holder of wanted value
     * @param <U>  generic type of wanted konfig.
     * @return the wrapped null value in K.
     */
    protected final <U> K<U> null_(final String key,
                                   final Q<U> type) {
        return k(key, type, null);
    }


    /**
     * Get config value of a primitive type according to {@link TypeName#isPrimitive()}.
     *
     * @param key  config key
     * @param type generic type of container
     * @return the konfig value for the given key.
     */
    protected abstract Object getPrimitive(String key, Q<?> type);

    /**
     * Get config value of a collection type (set, map or list) of generic type Q.
     * <p>
     * The generic type of the collection will be U.
     *
     * @param key  config key
     * @param type generic type of container
     * @return a container type (list, map, set) of generic type {@code type}.
     */
    protected abstract Object getContainer(String key, Q<?> type);

    /**
     * Get config value of custom type {@code U}.
     *
     * @param key  config key
     * @param type generic type of container
     * @param <U>  generic type of wanted konfig.
     * @return the konfig value for the given key.
     */
    protected abstract <U> U getCustom(String key, Q<U> type);

    /**
     * Wrap the actual sanitized value in a  {@link K} instance.
     *
     * @param key  config key
     * @param type type holder of wanted value
     * @param <U>  generic type of wanted konfig.
     * @return the wrapped value in K.
     */
    protected abstract <U> K<U> k(String key, Q<U> type, Object value);


    /**
     * Handle the case where value of a key is null.
     *
     * @param key  the config key who's value is null.
     * @param type type of requested konfig.
     * @return true if it's ok to have null values.
     */
    @SuppressWarnings("unused")
    protected boolean isNullOk(final String key,
                               final Q<?> type) {
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
    protected boolean isNullOkInCollection(@SuppressWarnings("unused") final String key,
                                           @SuppressWarnings("unused") final Q<?> type,
                                           @SuppressWarnings("unused") Object value) {
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
                             final Q<?> neededType,
                             final Object value) {
        checkType0(neededType, key, value);
    }

    /**
     * Make sure the value is of the requested type.
     *
     * @param key        the config key whose value is being checked
     * @param neededType type asked for.
     * @param value      the value in question
     * @throws KfgTypeException if the requested type does not match the type
     *                          of value in the given in.
     */
    protected void checkCollectionType(final String key,
                                       final Q<?> neededType,
                                       final Object value) {
        if (neededType == null)
            return;

        nn(key, "key");
        nn(value, "value");

        if (neededType.typeName().isMap()) {
            if (!(value instanceof Map) || neededType.typeName().isMap())
                throw new KfgIllegalStateException(this, key, neededType, value, "expecting a map");


            for (final Object o : ((Map<?, ?>) value).values())
                if (o != null)
                    try {
                        checkType0(neededType, key, o);
                    }
                    catch (KfgTypeException k) {
                        throw new KfgTypeException(this, key, neededType, value);
                    }
                else if (!isNullOkInCollection(key, neededType, value))
                    throw new KfgTypeNullException(this, key, neededType);

            for (final Object o : ((Map<?, ?>) value).keySet())
                if (o != null)
                    try {
                        checkType0(neededType, key, o);
                    }
                    catch (KfgTypeException k) {
                        throw new KfgTypeException(this, key, neededType, value);
                    }
                else if (!isNullOkInCollection(key, neededType, value))
                    throw new KfgTypeNullException(this, key, neededType);
        }
        else {
            if (!(value instanceof Collection))
                throw new KfgIllegalStateException(this, key, neededType, value, "expecting a collection");

            for (final Object o : (Collection<?>) value)
                if (o != null)
                    try {
                        checkType0(neededType, key, o);
                    }
                    catch (KfgTypeException k) {
                        throw new KfgTypeException(this, key, neededType, value);
                    }
                else if (!isNullOkInCollection(key, neededType, value))
                    throw new KfgTypeNullException(this, key, neededType);
        }
    }

    /**
     * Default implementation of {@link #checkType(String, Q, Object)} .
     *
     * @param neededType see checkType().
     * @param key        see checkType().
     * @param value      see checkType().
     * @throws KfgTypeException see checkType().
     * @see #checkType(String, Q, Object)
     */
    protected final void checkType0(final Q<?> neededType,
                                    final String key,
                                    final Object value) {
        if (neededType != null && value != null && !neededType.matchesValue(value))
            throw new KfgTypeException(this, key, neededType, value);
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
        if (!(o instanceof Number) || o instanceof Double || o instanceof Float)
            return null;

        if (((Number) o).longValue() < min || max < ((Number) o).longValue())
            return null;

        return ((Number) o).longValue();
    }

    private static Float toFloat(final Object o) {
        if (!(o instanceof Number))
            return null;

        if (((Number) o).doubleValue() < Float.MIN_VALUE
                || Float.MAX_VALUE < ((Number) o).doubleValue())
            return null;

        return ((Number) o).floatValue();
    }

    private static Double toDouble(final Object o) {
        if (!(o instanceof Number))
            return null;

        return ((Number) o).doubleValue();
    }

}
