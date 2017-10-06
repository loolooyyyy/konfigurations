package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.EverythingObserver;
import cc.koosha.konfiguration.KeyObserver;
import lombok.NonNull;
import lombok.val;

import java.util.HashSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * A wrapper around {@link _KonfigObserversHolder} to make it thread-safe.
 * <p>
 * Thread-safe.
 */
final class _KonfigObserversManager {

    private final ReadWriteLock OBSERVERS_LOCK = new ReentrantReadWriteLock();

    private final _KonfigObserversHolder konfigObserversHolder = new _KonfigObserversHolder();

    void register(final EverythingObserver observer) {

        val lock = this.OBSERVERS_LOCK.writeLock();

        try {
            lock.lock();
            konfigObserversHolder.everythingObservers().put(observer, null);
        }
        finally {
            lock.unlock();
        }
    }

    void deregister(final EverythingObserver observer) {

        val lock = this.OBSERVERS_LOCK.writeLock();

        try {
            lock.lock();
            konfigObserversHolder.everythingObservers().remove(observer);
        }
        finally {
            lock.unlock();
        }
    }

    void deregister(final KeyObserver observer, final String key) {

        val lock = OBSERVERS_LOCK.writeLock();

        // Each observer can observe multiple keys, that's why we remove from
        // the set first.

        try {
            lock.lock();
            val keys = konfigObserversHolder.keyObservers().get(observer);
            if (keys == null)
                return;
            keys.remove(key);
            if (keys.isEmpty())
                konfigObserversHolder.keyObservers().remove(observer);
        }
        finally {
            lock.unlock();
        }
    }

    void register(@NonNull final KeyObserver observer, final String key) {

        val lock = this.OBSERVERS_LOCK.writeLock();

        // Note: Each observer can observe multiple keys, that's why a HashSet
        // is created for each observer.

        try {
            lock.lock();
            if (!konfigObserversHolder.keyObservers().containsKey(observer))
                konfigObserversHolder.keyObservers()
                                     .put(observer, new HashSet<String>());
            konfigObserversHolder.keyObservers().get(observer).add(key);
        }
        finally {
            lock.unlock();
        }
    }

    _KonfigObserversHolder copy() {

        val lock = OBSERVERS_LOCK.readLock();

        try {
            lock.lock();
            return new _KonfigObserversHolder(this.konfigObserversHolder);
        }
        finally {
            lock.unlock();
        }
    }

}
