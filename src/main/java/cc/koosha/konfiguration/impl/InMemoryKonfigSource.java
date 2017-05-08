package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.KonfigSource;
import cc.koosha.konfiguration.KonfigurationBadTypeException;
import cc.koosha.konfiguration.KonfigurationException;
import cc.koosha.konfiguration.SupplierX;
import lombok.NonNull;
import lombok.val;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Reads konfig from a plain java map.
 * <p>
 * To fulfill contract of {@link KonfigSource}, all the values put in the
 * map of konfiguration key/values supplied to the konfiguration, should be
 * immutable.
 * <p>
 * Thread safe and immutable.
 */
public final class InMemoryKonfigSource implements KonfigSource {

    private final Map<String, Object>            storage;
    private final SupplierX<Map<String, Object>> storageProvider;

    /**
     * Important: {@link SupplierX#get()} might be called multiple
     * times in a short period (once call to see if it's changed and if so, one
     * mode call to get the new values afterward.
     *
     * @param storage konfig source.
     */
    public InMemoryKonfigSource(@NonNull final SupplierX<Map<String, Object>> storage) {

        this.storageProvider = storage;

        val newStorage = this.storageProvider.get();
        if (newStorage == null)
            throw new KonfigurationException("storage is null");

        this.storage = new HashMap<>(newStorage);
    }

    public InMemoryKonfigSource(@NonNull final Map<String, Object> storage) {

        this(new SupplierX<Map<String, Object>>() {
            @Override
            public Map<String, Object> get() {
                return storage;
            }
        });
    }


    @Override
    public Boolean bool(final String key) {

        try {
            return (Boolean) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw new KonfigurationBadTypeException("not a boolean: " + key);
        }
    }

    @Override
    public Integer int_(final String key) {

        try {
            return (Integer) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw new KonfigurationBadTypeException("not an int: " + key);
        }
    }

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

    @Override
    public Double double_(final String key) {

        try {
            return (Double) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw new KonfigurationBadTypeException("not a double: " + key);
        }
    }

    @Override
    public String string(final String key) {

        try {
            return (String) this.storage.get(key);
        }
        catch (final ClassCastException cce) {
            throw new KonfigurationBadTypeException("not a string: " + key);
        }
    }

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


    @Override
    public boolean contains(final String key) {

        return this.storage.containsKey(key);
    }

    @Override
    public boolean isUpdatable() {

        val newStorage = this.storageProvider.get();
        return newStorage != null && !this.storage.equals(newStorage);
    }

    @Override
    public KonfigSource copyAndUpdate() {

        return new InMemoryKonfigSource(this.storageProvider);
    }

}
