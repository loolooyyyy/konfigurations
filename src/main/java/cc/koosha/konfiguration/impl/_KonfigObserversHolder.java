package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.EverythingObserver;
import cc.koosha.konfiguration.KeyObserver;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Helper class to manage collection of observers.
 * <p>
 * Thread-safe and immutable <b>BUT</b> the maps obtained from it's methods are
 * not immutable and are not thread-safe.
 */
final class _KonfigObserversHolder {

    /**
     * Observers mapped to the keys they are observing.
     * <p>
     * Reference to KeyObserver must be weak.
     */
    @Getter(AccessLevel.PACKAGE)
    private final Map<KeyObserver, Collection<String>> keyObservers;

    /**
     * Observers who observe everything, aka all the keys.
     * <p>
     * Reference to EverythingObserver must be weak, and that's why a map.
     */
    @Getter(AccessLevel.PACKAGE)
    private final Map<EverythingObserver, Void> everythingObservers;

    @Getter(AccessLevel.PACKAGE)
    private final ReadWriteLock OBSERVERS_LOCK = new ReentrantReadWriteLock();

    _KonfigObserversHolder() {

        everythingObservers = new WeakHashMap<>();
        keyObservers = new WeakHashMap<>();
    }

    /**
     * This constructor is used to create temporary snapshots of another
     * instance (and will be entirely GCed shortly after) so it's ok not to
     * use weak hash map.
     */
    private _KonfigObserversHolder(final _KonfigObserversHolder from) {

        this.keyObservers = new HashMap<>(from.keyObservers);
        this.everythingObservers = new HashMap<>(from.everythingObservers);
    }

    void register(final EverythingObserver observer) {

        val lock = this.OBSERVERS_LOCK.writeLock();

        try {
            lock.lock();
            this.everythingObservers.put(observer, null);
        }
        finally {
            lock.unlock();
        }
    }

    void deregister(final EverythingObserver observer) {

        val lock = this.OBSERVERS_LOCK.writeLock();

        try {
            lock.lock();
            this.everythingObservers.remove(observer);
        }
        finally {
            lock.unlock();
        }
    }

    void deregister(final KeyObserver observer, final String key) {

        val lock = OBSERVERS_LOCK.writeLock();

        try {
            lock.lock();
            val keys = this.keyObservers.get(observer);
            if (keys == null)
                return;
            keys.remove(key);
            if (keys.isEmpty())
                this.keyObservers.remove(observer);
        }
        finally {
            lock.unlock();
        }
    }

    void register(@NonNull final KeyObserver observer, final String key) {

        val lock = this.OBSERVERS_LOCK.writeLock();

        try {
            lock.lock();
            if (!this.keyObservers.containsKey(observer)) {
                val put = new HashSet<String>();
                put.add(key);
                this.keyObservers.put(observer, put);
            }
            else {
                this.keyObservers.get(observer).add(key);
            }
        }
        finally {
            lock.unlock();
        }
    }

    _KonfigObserversHolder copy() {

        val lock = OBSERVERS_LOCK.readLock();

        try {
            lock.lock();
            return new _KonfigObserversHolder(this);
        }
        finally {
            lock.unlock();
        }
    }

}
