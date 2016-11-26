package cc.koosha.konfiguration.impl;


import cc.koosha.konfiguration.KonfigSource;
import cc.koosha.konfiguration.KonfigurationMissingKeyException;
import lombok.val;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


final class KonfigurationCacheSingleImpl implements KonfigurationCache {

    private final ReadWriteLock LOCK0 = new ReentrantReadWriteLock();

    private final Map<KonfigKey, Object> vCache = new HashMap<>();
    private final Collection<KonfigSource> sources;
    private final KonfigurationKombiner origin;


    KonfigurationCacheSingleImpl(final KonfigurationKombiner origin,
                                 final Collection<KonfigSource> sources) {

        this.origin = origin;
        this.sources = new ArrayList<>(sources);
    }

    @Override
    public void create(final KonfigKey key, final boolean mustExist) {

        try {

            LOCK0.readLock().lock();
            try {

                LOCK0.readLock().lock();
                if (vCache.containsKey(key))
                    return;
            }
            finally {

                LOCK0.readLock().unlock();
            }
        }
        finally {

            LOCK0.readLock().unlock();
        }

        try {

            LOCK0.writeLock().lock();
            if (!vCache.containsKey(key))
                for (val source : sources)
                    if (source.contains(key.name())) {
                        vCache.put(key, KonfigurationKombiner.getInSource(source, key));
                        return;
                    }
        }
        finally {

            LOCK0.writeLock().unlock();
        }

        if (mustExist)
            throw new KonfigurationMissingKeyException(key.name());
    }

    @Override
    public boolean update() {

        boolean up = false;
        for (final KonfigSource source : this.sources)
            if(source.isUpdatable()) {
                up = true;
                break;
            }

        if(!up)
            return false;

        final List<KonfigSource> sourcesCopy = new ArrayList<>(sources.size());
        for (final KonfigSource source : sources)
            sourcesCopy.add(source.copy());

        final Map<KonfigKey, Object> newCache = new HashMap<>(vCache.size());
        final Set<String> updatedKeys = new HashSet<>();

        final KonfigObserversHolder holder;

        try {

            LOCK0.writeLock().lock();
            for (val each: this.vCache.entrySet()) {
                boolean found = false;
                boolean changed = false;
                for (val source : sourcesCopy) {
                    if (source.contains(each.getKey().name())) {
                        val newVal = KonfigurationKombiner.getInSource(source, each.getKey());
                        newCache.put(each.getKey(), newVal);
                        found = true;
                        changed = !newVal.equals(each.getValue());
                        break;
                    }
                }

                if(!found || changed)
                    updatedKeys.add(each.getKey().name());
            }

            if(updatedKeys.size() == 0)
                return false;

            this.sources.clear();
            this.sources.addAll(sourcesCopy);
            vCache.clear();
            vCache.putAll(newCache);
            holder = origin.konfigObserversManager().get();
        }
        finally {

            LOCK0.writeLock().unlock();
        }

        // Notify observers of specific keys.
        for (val listener : holder.keyObservers().entrySet())
            for (val updatedKey : updatedKeys)
                if (listener.getValue().contains(updatedKey))
                    listener.getKey().accept(updatedKey);

        // Notify observers of source updates.
        for (val listener : holder.everythingObservers().entrySet())
            listener.getKey().accept();

        return true;
    }

    @Override
    public <T> T v(final KonfigKey key, final T def, final boolean mustExist) {

        try {

            LOCK0.readLock().lock();
            if (vCache.containsKey(key)) {
                @SuppressWarnings("unchecked")
                final T get = (T) vCache.get(key);
                return get;
            }
        }
        finally {

            LOCK0.readLock().unlock();
        }

        if(mustExist)
            throw new KonfigurationMissingKeyException(key.name());

        return def;
    }
}
