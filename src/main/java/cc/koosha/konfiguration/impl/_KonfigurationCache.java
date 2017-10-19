package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.K;
import cc.koosha.konfiguration.KonfigSource;
import cc.koosha.konfiguration.KonfigurationBadTypeException;
import cc.koosha.konfiguration.KonfigurationMissingKeyException;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * A mechanism for caching values obtained from configuration sources.
 * <p>
 * <b>This interface is ONLY used for extending konfiguration sources or
 * implementing a new one. it must NOT be used by clients.</b>
 * <p>
 * <p>
 * <p>
 * caches konfiguration values in a {@link HashMap}.
 * <p>
 * <b>This class is ONLY used for extending konfiguration sources or
 * implementing a new one. it must NOT be used by clients.</b>
 * <p>
 * TODO add expiry.
 * <p>
 * Thread-safe
 */
@RequiredArgsConstructor
final class _KonfigurationCache {

    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    private final Map<String, Object> valuCache = new HashMap<>();
    private final Map<String, _KonfigVImpl<?>> kvalCache = new HashMap<>();

    private final Collection<KonfigSource> sources;
    private final _KonfigObserversHolder konfigObserversHolder;


    private <T> K<T> getK(String name, Class<?> dt, Class<?> el) {

        final _KonfigVImpl<?> r = this.kvalCache.get(name);

        if (Objects.equals(r.getDt(), dt) && Objects.equals(r.getEl(), el)) {
            @SuppressWarnings("unchecked")
            final K<T> cast = (K<T>) r;
            return cast;
        }
        else {
            throw new KonfigurationBadTypeException(
                    TypeName.typeName(dt, el), TypeName.typeName(r.getDt(), r.getEl()), name);
        }
    }

    private Object putV(KonfigSource source, String name, Class<?> dt, Class<?> el) {

        Object result;

        if (dt == Boolean.class)
            result = source.bool(name);
        else if (dt == Integer.class)
            result = source.int_(name);
        else if (dt == Long.class)
            result = source.long_(name);
        else if (dt == Double.class)
            result = source.double_(name);
        else if (dt == String.class)
            result = source.string(name);
        else if (dt == List.class)
            result = source.list(name, el);
        else if (dt == Map.class)
            result = source.map(name, el);
        else if (dt == Set.class)
            result = source.set(name, el);
        else
            result = source.custom(name, el);

        valuCache.put(name, result);
        return result;
    }

    <T> K<T> create(KonfigurationKombiner origin, String name, Class<?> dt, Class<?> el) {

        // We can not do this, in order to support default values.
        //    if(does not exist) throw new KonfigurationMissingKeyException(key);

        val readLock = LOCK.readLock();
        try {
            readLock.lock();

            if (this.kvalCache.containsKey(name))
                return this.getK(name, dt, el);
        }
        finally {
            readLock.unlock();
        }

        val writeLock = LOCK.writeLock();
        try {
            writeLock.lock();

            // Cache was already populated between two locks.

            if (this.kvalCache.containsKey(name))
                return this.getK(name, dt, el);

            for (val source : sources)
                if (source.contains(name)) {
                    this.putV(source, name, dt, el);
                    break;
                }

            val rr = new _KonfigVImpl<T>(origin, name, dt, el);
            kvalCache.put(name, rr);
            return rr;
        }
        finally {
            writeLock.unlock();
        }
    }

    boolean update() {

        boolean isUpdatable = false;
        for (val source : this.sources)
            if (source.isUpdatable()) {
                isUpdatable = true;
                break;
            }
        if (!isUpdatable)
            return false;

        val updatedSources = new ArrayList<KonfigSource>(this.sources.size());
        for (val source : this.sources)
            updatedSources.add(source.copyAndUpdate());

        final Map<String, Object> newCache = new HashMap<>();
        final Set<String> updatedKeys = new HashSet<>();

        val holder = _update(updatedSources, newCache, updatedKeys);
        if (holder == null)
            return false;

        for (val listener : holder.getEverythingObservers().entrySet())
            listener.getKey().accept();
        for (val observer : holder.getKeyObservers().entrySet())
            for (val updatedKey : updatedKeys)
                if (observer.getValue().contains(updatedKey))
                    observer.getKey().accept(updatedKey);
        holder.getKeyObservers().clear();
        holder.getEverythingObservers().clear();

        return true;
    }

    _KonfigObserversHolder _update(ArrayList<KonfigSource> updatedSources,
                                   Map<String, Object> newCache,
                                   Set<String> updatedKeys) {

        @Cleanup("unlock")
        Lock lock = LOCK.writeLock();
        lock.lock();

        // Bad idea, I know.
        @Cleanup("unlock")
        Lock oLock = this.konfigObserversHolder.getOBSERVERS_LOCK().writeLock();
        oLock.lock();

        for (val each : this.valuCache.entrySet()) {
            val name = each.getKey();
            val value = each.getValue();
            boolean found = false;
            boolean changed = false;
            for (val source : updatedSources) {
                if (source.contains(name)) {
                    found = true;
                    val kVal = kvalCache.get(name);
                    val newVal = this.putV(source, name, kVal.getDt(), kVal.getEl());
                    newCache.put(name, newVal);
                    changed = !newVal.equals(value);
                    break;
                }
            }

            if (!found || changed)
                updatedKeys.add(name);
        }

        if (updatedKeys.isEmpty())
            return null;

        valuCache.clear();
        valuCache.putAll(newCache);
        return this.konfigObserversHolder.copy();
    }

    /**
     * Only to be called by {@link _KonfigVImpl}
     */
    <T> T get(final String name, final T def, final boolean mustExist) {

        val lock = LOCK.readLock();
        try {
            lock.lock();
            if (valuCache.containsKey(name)) {
                @SuppressWarnings("unchecked")
                val get = (T) valuCache.get(name);
                return get;
            }
        }
        finally {
            lock.unlock();
        }

        if (mustExist)
            throw new KonfigurationMissingKeyException(name);

        return def;
    }

}
