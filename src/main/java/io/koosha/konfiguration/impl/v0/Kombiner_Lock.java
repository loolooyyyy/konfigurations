package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.KfgConcurrencyException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RequiredArgsConstructor
final class Kombiner_Lock {

    @NotNull
    @NonNull
    private final String name;

    private final long lockWaitTime;

    private final ReadWriteLock LOCK = new ReentrantReadWriteLock(true);

    private void acquire(@NonNull @NotNull final Lock lock) {
        try {
            if (!lock.tryLock(this.lockWaitTime, MILLISECONDS))
                throw new KfgConcurrencyException(this.name, "could not acquire lock");
        }
        catch (final InterruptedException e) {
            throw new KfgConcurrencyException(this.name, "could not acquire lock", e);
        }
    }

    private void release(@Nullable final Lock lock) {
        if (lock != null)
            lock.unlock();
    }

    <T> T doReadLocked(@NonNull @NotNull final Supplier<T> func) {
        Lock lock = null;
        try {
            lock = LOCK.readLock();
            acquire(lock);
            return func.get();
        }
        finally {
            release(lock);
        }
    }

    <T> T doWriteLocked(@NonNull @NotNull final Supplier<T> func) {
        Lock lock = null;
        try {
            lock = LOCK.readLock();
            acquire(lock);
            return func.get();
        }
        finally {
            release(lock);
        }
    }

}
