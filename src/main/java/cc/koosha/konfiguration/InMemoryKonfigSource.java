package cc.koosha.konfiguration;

import java.util.*;

import static cc.koosha.konfiguration.TypeName.*;


/**
 * Reads konfig from a plain java map.
 *
 * <p>To fulfill contract of {@link KonfigSource}, all the values put in the
 * map of konfiguration key/values supplied to the konfiguration, should be
 * immutable.
 *
 * <p>Thread safe and immutable.
 */
public final class InMemoryKonfigSource implements KonfigSource {

    private final Map<String, Object> storage;
    private final SupplierX<Map<String, Object>> storageProvider;

    private KonfigurationTypeException checkType(final String required,
                                                 final String key) {
        return new KonfigurationTypeException(
                required, this.storage.get(key).getClass().toString(), key);
    }

    private KonfigurationTypeException checkType(final TypeName required,
                                                 final String key) {
        return this.checkType(required.getTName(), key);
    }

    /**
     * Important: {@link SupplierX#get()} might be called multiple
     * times in a short period (once call to see if it's changed and if so, one
     * mode call to get the new values afterward.
     *
     * @param storage konfig source.
     */
    @SuppressWarnings("WeakerAccess")
    public InMemoryKonfigSource(final SupplierX<Map<String, Object>> storage) {
        if (storage == null)
            throw new NullPointerException("storage");
        this.storageProvider = storage;

        final Map<String, Object> newStorage = this.storageProvider.get();
        if (newStorage == null)
            throw new KonfigurationSourceException("storage is null");

        this.storage = new HashMap<>(newStorage);
    }

    @SuppressWarnings("unused")
    public InMemoryKonfigSource(final Map<String, Object> storage) {
        this(new SupplierX<Map<String, Object>>() {
            private final Map<String, Object> s = new HashMap<>(Objects.requireNonNull(storage));

            @Override
            public Map<String, Object> get() {
                return s;
            }
        });
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean bool(final String key) {
        try {
            return (Boolean) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(BOOLEAN, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer int_(final String key) {
        try {
            return (Integer) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(INT, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long long_(final String key) {
        try {
            return (Long) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(LONG, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double double_(final String key) {
        try {
            return (Double) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(DOUBLE, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String string(final String key) {
        try {
            return (String) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(STRING, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> list(final String key, final Class<T> type) {
        // TODO check type

        try {
            return (List<T>) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(LIST, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Map<String, T> map(final String key, final Class<T> type) {
        // TODO check type

        try {
            return (Map<String, T>) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(MAP, key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Set<T> set(final String key, final Class<T> type) {
        // TODO check type

        try {
            return (Set<T>) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            try {
                final List<T> l = (List<T>) this.storage.get(key);
                final HashSet<T> s = new HashSet<>(l);
                return Collections.unmodifiableSet(s);
            }
            catch (final ClassCastException cceList) {
                throw checkType(SET, key);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T custom(final String key, final Class<T> type) {
        // TODO check type
        try {
            return (T) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw checkType(type.toString(), key);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final String key) {
        return this.storage.containsKey(key);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUpdatable() {
        final Map<String, Object> newStorage = this.storageProvider.get();
        return newStorage != null && !this.storage.equals(newStorage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KonfigSource copyAndUpdate() {
        return new InMemoryKonfigSource(this.storageProvider);
    }

}
