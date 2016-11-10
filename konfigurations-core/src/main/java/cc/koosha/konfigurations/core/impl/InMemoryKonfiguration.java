package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.*;
import lombok.NonNull;
import lombok.val;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cc.koosha.konfigurations.core.DummyV.dummy;


public final class InMemoryKonfiguration implements Konfiguration {

    public interface KonfigMapProvider {

        Map<String, Object> get();

    }

    private final Map<String, Object> storage = new HashMap<>();
    private final KonfigMapProvider storageProvider;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public InMemoryKonfiguration(@NonNull final KonfigMapProvider storage) {

        this.storageProvider = storage;
        this.update();
    }

    public InMemoryKonfiguration(@NonNull final Map<String, Object> storage) {

        this(new KonfigMapProvider() {
            @Override
            public Map<String, Object> get() {
                return storage;
            }
        });
    }


    @Override
    public KonfigV<Boolean> bool(final String key) {

        return get(key, Boolean.class);
    }

    @Override
    public KonfigV<Integer> int_(final String key) {

        return get(key, Integer.class);
    }

    @Override
    public KonfigV<Long> long_(final String key) {

        return get(key, Long.class);
    }

    @Override
    public KonfigV<String> string(final String key) {

        return get(key, String.class);
    }

    // TODO check type
    @Override
    public <T> KonfigV<List<T>> list(final String key, final Class<T> type) {

        return get(key);
    }

    // TODO check type
    @Override
    public <T> KonfigV<Map<String, T>> map(final String key, final Class<T> type) {

        return get(key);
    }

    @Override
    public <T> KonfigV<Set<T>> set(final String key, final Class<T> type) {

        return get(key);
    }

    @Override
    public <T> KonfigV<T> custom(final String key, final Class<T> type) {

        return get(key);
    }

    private <T> KonfigV<T> get(final String key) {

        final Lock lock = this.lock.readLock();
        try {
            lock.lock();
            if(!this.storage.containsKey(key))
                throw new KonfigurationMissingKeyException(key);
            return dummy(this.storage.get(key));
        }
        finally {
            lock.unlock();
        }
    }

    private <T> KonfigV<T> get(final String key, final Class<?> el) {

        final KonfigV<T> t = this.get(key);
        if(!el.isAssignableFrom(t.v().getClass()))
            throw new KonfigurationBadTypeException(key + " is not " + el);

        return t;
    }


    @Override
    public boolean update() {

        val newStorage = this.storageProvider.get();
        if(storage == null)
            throw new KonfigurationException("storage is null");

        boolean same = true;

        if(this.storage.size() != newStorage.size()
                || !this.storage.keySet().containsAll(newStorage.keySet())
                || !newStorage.keySet().containsAll(this.storage.keySet())) {
            same = false;
        }
        else {
            for (val e : this.storage.entrySet())
                if (!Objects.equals(this.storage.get(e.getKey()), newStorage.get(e.getKey()))) {
                    same = false;
                    break;
                }
        }

        if(!same) {
            final Lock lock = this.lock.writeLock();
            try {
                lock.lock();
                this.storage.clear();
                this.storage.putAll(newStorage);
            }
            finally {
                lock.unlock();
            }
        }

        return same;
    }

    @Override
    public Konfiguration subset(@NonNull final String key) {

        return new KonfigurationSubsetView(this, key);
    }

    @Override
    public Konfiguration parent() {

        return this;
    }

}
