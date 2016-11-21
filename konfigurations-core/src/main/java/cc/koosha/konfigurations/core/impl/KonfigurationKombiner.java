package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.KonfigSource;
import cc.koosha.konfigurations.core.KonfigV;
import cc.koosha.konfigurations.core.KonfigurationMissingKeyException;
import lombok.NonNull;
import lombok.val;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Memory leak: once a configuration is read, it always stays in the cache, even
 * if it's not needed anymore.
 */
public final class KonfigurationKombiner extends BaseKonfiguration {

    private final ReadWriteLock READ_LOCK = new ReentrantReadWriteLock();
    private final ReadWriteLock WRITE_LOCK = new ReentrantReadWriteLock();

    private final Map<KonfigKey, Object> vCache = new HashMap<>();

    private final Collection<KonfigSource> sources;


    @SuppressWarnings("unused")
    public KonfigurationKombiner(@NonNull final KonfigSource... sources) {

        this(Arrays.asList(sources));
    }

    public KonfigurationKombiner(@NonNull final Collection<KonfigSource> sources) {

        if(sources.size() < 1)
            throw new IllegalArgumentException("no source given");

        this.sources = new ArrayList<>(sources);
    }


    @Override
    protected <T> KonfigV<T> get(final KonfigKey key, final boolean mustExist) {

        val rLock = READ_LOCK.readLock();
        final KonfigVImpl<T> ret = new KonfigVImpl<>(this, key);

        try {
            rLock.lock();
            if(vCache.containsKey(key))
                return ret;
        }
        finally {
            rLock.unlock();
        }

        boolean found = false;
        val rwLock = READ_LOCK.writeLock();
        val wwLock = WRITE_LOCK.writeLock();

        try {
            rwLock.lock();
            try {
                wwLock.lock();
                if (!vCache.containsKey(key))
                    for (val source : sources)
                        if (source.contains(key.name())) {
                            vCache.put(key, BaseKonfiguration.getInSource(source, key));
                            found = true;
                            break;
                        }
            }
            finally {
                wwLock.unlock();
            }
        }
        finally {
            rwLock.unlock();
        }


        if (!found && mustExist)
            throw new KonfigurationMissingKeyException(key.name());

        return ret;
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    @Override
    protected <T> T v(final KonfigKey key) {

        val rLock = READ_LOCK.readLock();

        try {
            rLock.lock();
            if(vCache.containsKey(key))
                return (T) vCache.get(key);
        }
        finally {
            rLock.unlock();
        }

        throw new KonfigurationMissingKeyException(key.name());
    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    @Override
    protected <T> T v(final KonfigKey key, final T def) {

        val rLock = READ_LOCK.readLock();

        try {
            rLock.lock();
            if(vCache.containsKey(key))
                return (T) vCache.get(key);
        }
        finally {
            rLock.unlock();
        }

        return def;
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

        val rLock = READ_LOCK.readLock();
        val wLock = WRITE_LOCK.writeLock();
        try {
            rLock.lock();
            for (val each: this.vCache.entrySet()) {
                boolean found = false;
                boolean changed = false;
                for (val source : sourcesCopy) {
                    if (source.contains(each.getKey().name())) {
                        val newVal = BaseKonfiguration.getInSource(source, each.getKey());
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

            try {
                wLock.lock();
                this.sources.clear();
                this.sources.addAll(sourcesCopy);
                vCache.clear();
                vCache.putAll(newCache);
                holder = konfigObserversManager().get();
            }
            finally {
                wLock.unlock();
            }
        }
        finally {
            rLock.unlock();
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

}
