package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.KeyObservable;
import io.koosha.konfiguration.KeyObserver;
import io.koosha.konfiguration.Q;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Observers implements KeyObservable {

    @NotNull
    @NonNull
    private final String name;

    private final Map<Handle, Kombiner_Observer> observers = new LinkedHashMap<>();

    @Synchronized
    private void put(@NotNull @NonNull final Handle handle,
                     @NotNull @NonNull final Kombiner_Observer o) {
        this.observers.put(handle, o);
    }

    @Synchronized
    boolean remove(@NonNull @NotNull final Handle handle) {
        return this.observers.remove(handle) != null;
    }


    @NotNull
    private Handle add(@NotNull @NonNull final KeyObserver observer,
                       @Nullable final String key,
                       @Nullable final Q<?> type) {
        final Kombiner_Observer o = new Kombiner_Observer(observer, key, type);
        this.put(o.handle(), o);
        return o.handle();
    }

    @NotNull
    private Handle addSoft(@NotNull @NonNull final KeyObserver observer,
                           @Nullable final String key,
                           @Nullable final Q<?> type) {
        final Kombiner_Observer o = new Kombiner_Observer(new WeakReference<>(observer), key, type);
        this.put(o.handle(), o);
        return o.handle();
    }

    @NotNull
    @Override
    public Handle register(@NotNull @NonNull final KeyObserver observer) {
        return add(observer, null, null);
    }

    @NotNull
    public Handle register(@NonNull @NotNull final KeyObserver observer,
                           @NotNull @NonNull final String key) {
        return add(observer, key, null);
    }

    @NotNull
    @Override
    public Handle register(@NotNull @NonNull final KeyObserver observer,
                           @NotNull @NonNull final Q<?> type) {
        return add(observer, null, type);
    }


    @NotNull
    @Override
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer) {
        return this.addSoft(observer, null, null);
    }

    @NotNull
    @Override
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer,
                               @NotNull @NonNull final Q<?> type) {
        return this.addSoft(observer, null, type);
    }

    @NotNull
    @Override
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer,
                               @NotNull @NonNull final String key) {
        return this.addSoft(observer, key, null);
    }


    @Override
    public void deregister(@NotNull @NonNull final Handle observer) {
        this.remove(observer);
    }


    @NotNull
    @Synchronized
    Collection<Runnable> get(@NonNull @NotNull final Q<?> type) {
        final String key = Objects.requireNonNull(type.key());
        return this.observers
                .values()
                .stream()
                .filter(x -> Objects.equals(key, x.key())
                        || Objects.equals(type, x.type()))
                .map(Kombiner_Observer::listener)
                .filter(Objects::nonNull)
                .map(x -> (Runnable) () -> x.accept(key))
                .collect(toList());
    }

}
