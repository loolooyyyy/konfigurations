package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.error.KfgConcurrencyException;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import lombok.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ThreadSafe
@ApiStatus.Internal
final class Kombiner_Lock {

    @NotNull
    @NonNull
    private final String name;

    @Nullable
    private final Long lockWaitTimeMillis;

    @NotNull
    private final ReadWriteLock LOCK;

    Kombiner_Lock(@NotNull @NonNull final String name,
                  @Nullable final Long lockWaitTimeMillis,
                  final boolean fair) {
        if (lockWaitTimeMillis != null && lockWaitTimeMillis < 0)
            throw new KfgIllegalStateException(name, "wait time must be gte 0: " + lockWaitTimeMillis);
        this.name = name;
        this.lockWaitTimeMillis = lockWaitTimeMillis;
        this.LOCK = new ReentrantReadWriteLock(fair);
    }

    @SuppressWarnings("LockAcquiredButNotSafelyReleased")
    private void acquire(@NonNull @NotNull final Lock lock) {
        if (this.lockWaitTimeMillis == null)
            lock.lock();
        else
            try {
                if (!lock.tryLock(this.lockWaitTimeMillis, MILLISECONDS))
                    throw new KfgConcurrencyException(this.name, "could not acquire lock");
            }
            catch (final InterruptedException e) {
                throw new KfgConcurrencyException(this.name, "could not acquire lock", e);
            }
    }

    private static void release(@Nullable final Lock lock) {
        if (lock != null)
            lock.unlock();
    }

    <T> T doReadLocked(@NonNull @NotNull final Supplier<T> func) {
        Lock lock = null;
        try {
            lock = this.LOCK.readLock();
            this.acquire(lock);
            return func.get();
        }
        finally {
            release(lock);
        }
    }

    <T> T doWriteLocked(@NonNull @NotNull final Supplier<T> func) {
        Lock lock = null;
        try {
            lock = this.LOCK.readLock();
            this.acquire(lock);
            return func.get();
        }
        finally {
            release(lock);
        }
    }

}
