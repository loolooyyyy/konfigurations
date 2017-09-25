package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.KonfigSource;
import cc.koosha.konfiguration.KonfigurationMissingKeyException;
import lombok.val;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * A {@link KonfigurationCache} implementation that caches konfiguration values
 * in a {@link HashMap}.
 * <p>
 * <b>This class is ONLY used for extending konfiguration sources or
 * implementing a new one. it must NOT be used by clients.</b>
 * <p>
 * TODO add expiry.
 * <p>
 * Thread-safe
 */
final class KonfigurationCacheSingleImpl implements KonfigurationCache {

    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private final Map<KonfigKey, Object> vCache = new HashMap<>();
    private final List<KonfigSource>    sources;
    private final KonfigurationKombiner origin;

    KonfigurationCacheSingleImpl(final KonfigurationKombiner origin,
                                 final Collection<KonfigSource> sources) {

        this.origin = origin;
        this.sources = new ArrayList<>(sources);
    }

    @Override
    public boolean create(final KonfigKey key) {

        val readLock = LOCK.readLock();
        try {
            readLock.lock();
            if (vCache.containsKey(key))
                return true;
        }
        finally {
            readLock.unlock();
        }

        val writeLock = LOCK.writeLock();
        try {
            writeLock.lock();

            // Cache was already populated between two locks.
            if (vCache.containsKey(key))
                return true;

            for (val source : sources)
                if (source.contains(key.name())) {
                    vCache.put(key, KonfigurationKombiner.getInSource(source, key));
                    // Don't look any further. The first source containing
                    // the key wins.
                    return true;
                }
        }
        finally {
            writeLock.unlock();
        }

        return false;
    }

    /**
     * This implementation calls observers after it has fully updated the cache.
     * <p>
     * This implementation calls everything observers before key observers.
     */
    @Override
    public boolean update() {

        // Find out if any konfiguration source is updatable.
        boolean up = false;
        for (final KonfigSource source : this.sources)
            if (source.isUpdatable()) {
                up = true;
                break;
            }

        if (!up)
            return false;

        // Copy also updates.
        final List<KonfigSource> updatedSources = new ArrayList<>(this.sources.size());
        for (final KonfigSource source : this.sources)
            updatedSources.add(source.copyAndUpdate());

        final Map<KonfigKey, Object> newCache    = new HashMap<>(vCache.size());
        final Set<String>            updatedKeys = new HashSet<>();

        final KonfigObserversHolder holder;

        val lock = LOCK.writeLock();
        try {
            lock.lock();
            for (val each : this.vCache.entrySet()) {
                boolean found   = false;
                boolean changed = false;
                for (val source : updatedSources)
                    if (source.contains(each.getKey().name())) {
                        found = true;
                        val newVal = KonfigurationKombiner.getInSource(source, each
                                .getKey());
                        newCache.put(each.getKey(), newVal);
                        changed = !newVal.equals(each.getValue());
                        break;
                    }

                if (!found || changed)
                    updatedKeys.add(each.getKey().name());
            }

            if (updatedKeys.isEmpty())
                return false;

            vCache.clear();
            vCache.putAll(newCache);
            holder = origin.konfigObserversManager().copy();
        }
        finally {
            lock.unlock();
        }

        // Notify observers of source updates.
        for (val listener : holder.everythingObservers().entrySet())
            listener.getKey().accept();

        for (val observer : holder.keyObservers().entrySet())
            for (val updatedKey : updatedKeys)
                if (observer.getValue().contains(updatedKey))
                    observer.getKey().accept(updatedKey);

        return true;
    }

    @Override
    public <T> T v(final KonfigKey key, final T def, final boolean mustExist) {

        val lock = LOCK.readLock();
        try {
            lock.lock();
            if (vCache.containsKey(key)) {
                @SuppressWarnings("unchecked")
                val get = (T) vCache.get(key);
                return get;
            }
        }
        finally {
            lock.unlock();
        }

        if (mustExist)
            throw new KonfigurationMissingKeyException(key.name());

        return def;
    }

}
