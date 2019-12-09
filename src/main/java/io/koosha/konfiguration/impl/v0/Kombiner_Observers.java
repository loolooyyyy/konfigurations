package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.KeyObserver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@NotThreadSafe
final class Kombiner_Observers {

    @NotNull
    @NonNull
    private final String name;

    private final long lockTimeoutMillis;

    private final Map<Handle, Kombiner_Observer> observers = new LinkedHashMap<>();

    Handle registerSoft(@NonNull @NotNull final KeyObserver observer,
                        @Nullable final String key) throws InterruptedException {
        final Kombiner_Observer o = new Kombiner_Observer(new WeakReference<>(observer));
        this.observers.put(o.handle(), o);
        return o.handle();
    }

    Handle registerHard(@NonNull @NotNull final KeyObserver observer,
                        @Nullable final String key) throws InterruptedException {
        final Kombiner_Observer o = new Kombiner_Observer(observer);
        this.observers.put(o.handle(), o);
        return o.handle();
    }

    void deregister(@NonNull @NotNull final Handle handle) throws InterruptedException {
        this.observers.remove(handle);
    }

    void deregister(@NonNull @NotNull final Handle handle,
                    @NonNull @NotNull final String key) throws InterruptedException {
        final Kombiner_Observer o = this.observers.get(handle);
        if (o != null)
            o.remove(key);
    }

    Stream<Runnable> get(@NonNull @NotNull final String key) throws InterruptedException {
        return this.observers
                .values()
                .stream()
                .filter(x -> x.has(key))
                .map(Kombiner_Observer::listener)
                .filter(Objects::nonNull)
                .map(x -> () -> x.accept(key));
    }

}
