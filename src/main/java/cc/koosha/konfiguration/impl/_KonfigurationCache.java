package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.KonfigSource;
import cc.koosha.konfiguration.KonfigV;
import cc.koosha.konfiguration.KonfigurationBadTypeException;
import cc.koosha.konfiguration.KonfigurationMissingKeyException;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;
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
    private final _KonfigObserversManager konfigObserversManager;


    private static String typeName(Class<?> base, Class<?> aux) {

        if (base == null && aux == null)
            return "?";
        if (base == null)
            return aux.getName();
        if (aux == null)
            return base.getName();
        else
            return base.getName() + " / " + aux.getName();
    }

    private boolean contains(final String key) {

        return this.kvalCache.containsKey(key);
    }

    private <T> KonfigV<T> getK(String name, Class<?> dt, Class<?> el) {

        final _KonfigVImpl<?> r = this.kvalCache.get(name);

        if (Objects.equals(r.getDt(), dt) && Objects.equals(r.getEl(), el)) {
            @SuppressWarnings("unchecked")
            final KonfigV<T> cast = (KonfigV<T>) r;
            return cast;
        }
        else {
            throw new KonfigurationBadTypeException(
                    typeName(dt, el), typeName(r.getDt(), r.getEl()), name);
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

    private <T> _KonfigVImpl<T> putK(KonfigurationKombiner origin, String name, Class<?> dt, Class<?> el) {

        final _KonfigVImpl<T> rr = new _KonfigVImpl<>(origin, name, dt, el);
        kvalCache.put(name, rr);
        return rr;
    }

    private boolean isUpdatable() {

        for (final KonfigSource source : this.sources)
            if (source.isUpdatable())
                return true;

        return false;
    }


    <T> KonfigV<T> create(KonfigurationKombiner origin, String name, Class<?> dt, Class<?> el) {

        // We can not do this, in order to support default values.
        //    if(does not exist) throw new KonfigurationMissingKeyException(key);

        val readLock = LOCK.readLock();
        try {
            readLock.lock();
            if (this.contains(name))
                return this.getK(name, dt, el);
        }
        finally {
            readLock.unlock();
        }

        val writeLock = LOCK.writeLock();
        try {
            writeLock.lock();

            // Cache was already populated between two locks.
            if (this.contains(name))
                return this.getK(name, dt, el);

            for (val source : sources)
                if (source.contains(name)) {
                    this.putV(source, name, dt, el);
                    break;
                }

            return this.putK(origin, name, dt, el);
        }
        finally {
            writeLock.unlock();
        }
    }

    boolean update() {

        if(!this.isUpdatable())
            return false;

        // Copy also updates.
        final List<KonfigSource> updatedSources = new ArrayList<>(this.sources.size());
        for (final KonfigSource source : this.sources)
            updatedSources.add(source.copyAndUpdate());

        final Map<String, Object> newCache = new HashMap<>();
        final Set<String> updatedKeys = new HashSet<>();
        final _KonfigObserversHolder holder;

        val lock = LOCK.writeLock();
        try {
            lock.lock();
            for (val each : this.valuCache.entrySet()) {
                val name = each.getKey();
                val value = each.getValue();
                boolean found = false;
                boolean changed = false;
                for (val source : updatedSources)
                    if (source.contains(name)) {
                        found = true;
                        val kVal = kvalCache.get(name);
                        val newVal = this.putV(source, name, kVal.getDt(), kVal.getEl());
                        newCache.put(name, newVal);
                        changed = !newVal.equals(value);
                        break;
                    }

                if (!found || changed)
                    updatedKeys.add(name);
            }

            if (updatedKeys.isEmpty())
                return false;

            valuCache.clear();
            valuCache.putAll(newCache);
            holder = this.konfigObserversManager.copy();
        }
        finally {
            lock.unlock();
        }

        holder.notifyOfUpdate(updatedKeys);

        return true;
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
