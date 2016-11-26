package cc.koosha.konfiguration.impl;

import cc.koosha.konfigurations.core.EverythingObserver;
import cc.koosha.konfiguration.KeyObserver;
import lombok.NonNull;
import lombok.val;

import java.util.HashSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


final class KonfigObserversManager {

    private final ReadWriteLock OBSERVERS_LOCK = new ReentrantReadWriteLock();

    private final KonfigObserversHolder konfigObserversHolder = new KonfigObserversHolder();

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

        try {
            lock.lock();
            val keys = konfigObserversHolder.keyObservers().get(observer);
            if(keys == null)
                return;
            keys.remove(key);
            if(keys.size() == 0)
                konfigObserversHolder.keyObservers().remove(observer);
        }
        finally {
            lock.unlock();
        }
    }

    void register(@NonNull final KeyObserver observer, final String key) {

        val lock = this.OBSERVERS_LOCK.writeLock();

        try {
            lock.lock();
            if (!konfigObserversHolder.keyObservers().containsKey(observer))
                konfigObserversHolder.keyObservers().put(observer, new HashSet<String>());
            konfigObserversHolder.keyObservers().get(observer).add(key);
        }
        finally {
            lock.unlock();
        }
    }

    KonfigObserversHolder get() {

        val obsLock = OBSERVERS_LOCK.readLock();

        try {
            obsLock.lock();
            return new KonfigObserversHolder(this.konfigObserversHolder);
        }
        finally {
            obsLock.unlock();
        }
    }

}
