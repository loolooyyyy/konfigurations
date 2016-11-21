package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.KonfigSource;
import cc.koosha.konfigurations.core.KonfigurationBadTypeException;
import cc.koosha.konfigurations.core.KonfigurationException;
import lombok.NonNull;
import lombok.val;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Reads konfig from a plain java map.
 *
 * To fulfill contract of {@link KonfigSource}, all the values put in the
 * map of konfiguration key/values supplied to the konfiguration, should be
 * immutable.
 *
 * Thread safe and immutable.
 */
public final class InMemoryKonfigSource implements KonfigSource {

    public interface KonfigMapProvider {

        Map<String, Object> get();
    }

    private final Map<String, Object> storage;
    private final KonfigMapProvider storageProvider;

    public InMemoryKonfigSource(@NonNull final KonfigMapProvider storage) {

        this.storageProvider = storage;

        val newStorage = this.storageProvider.get();
        if (newStorage == null)
            throw new KonfigurationException("storage is null");

        this.storage = new HashMap<>(newStorage);
    }

    @SuppressWarnings("unused")
    public InMemoryKonfigSource(@NonNull final Map<String, Object> storage) {

        this(new KonfigMapProvider() {
            @Override
            public Map<String, Object> get() {
                return storage;
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
            throw new KonfigurationBadTypeException("not a boolean: " + key);
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
            throw new KonfigurationBadTypeException("not an int: " + key);
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
            try {
                final int i = (Integer) this.storage.get(key);
                return (long) i;
            }
            catch (final ClassCastException cce0) {
                throw new KonfigurationBadTypeException("not a long: " + key);
            }
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
            throw new KonfigurationBadTypeException("not a double: " + key);
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
            throw new KonfigurationBadTypeException("not a string: " + key);
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
            throw new KonfigurationBadTypeException("not a list: " + key);
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
            throw new KonfigurationBadTypeException("not a map: " + key);
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
            throw new KonfigurationBadTypeException("not a set: " + key);
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
            throw new KonfigurationBadTypeException(cce);
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

        val newStorage = this.storageProvider.get();
        if (storage == null)
            throw new KonfigurationException("storage is null");

        return !this.storage.equals(newStorage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KonfigSource copy() {

        return new InMemoryKonfigSource(this.storageProvider);
    }

}
