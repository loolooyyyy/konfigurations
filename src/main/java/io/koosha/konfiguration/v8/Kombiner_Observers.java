package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.KeyObservable;
import io.koosha.konfiguration.KeyObserver;
import io.koosha.konfiguration.type.Q;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
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

    @Contract("_, null, null -> new; _, null, _ -> new; _, _, null -> new; _, _, _->fail")
    @NotNull
    private Handle addSoft(@NotNull @NonNull final KeyObserver observer,
                           @Nullable final String key,
                           @Nullable final Q<?> type) {
        if (key != null && type != null)
            throw new IllegalStateException("both string key and Q type can't be set at the same time: "
                    + key + " | " + type);
        final Kombiner_Observer o = new Kombiner_Observer(new WeakReference<>(observer), key, type);
        this.put(o.handle(), o);
        return o.handle();
    }

    @NotNull
    @Override
    public Handle register(@NotNull @NonNull final KeyObserver observer) {
        return this.add(observer, null, null);
    }

    @NotNull
    public Handle register(@NonNull @NotNull final KeyObserver observer,
                           @NotNull @NonNull final String key) {
        return this.add(observer, key, null);
    }

    @NotNull
    @Override
    public Handle register(@NotNull @NonNull final KeyObserver observer,
                           @NotNull @NonNull final Q<?> key) {
        return this.add(observer, null, key);
    }


    @NotNull
    @Override
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer) {
        return this.addSoft(observer, null, null);
    }

    @NotNull
    @Override
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer,
                               @NotNull @NonNull final Q<?> key) {
        return this.addSoft(observer, null, key);
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
        return this.observers
                .values()
                .stream()
                .filter(x -> Objects.equals(type.key(), x.key())
                        || Objects.equals(type, x.type()))
                .map(Kombiner_Observer::listener)
                .filter(Objects::nonNull)
                .map(x -> (Runnable) () -> x.accept(type.key()))
                .collect(toList());
    }

    @NotNull
    @Synchronized
    public Collection<? extends Runnable> get() {
        return this.observers
                .values()
                .stream()
                .filter(x -> Objects.isNull(x.type()))
                .filter(x -> Objects.isNull(x.key()))
                .map(Kombiner_Observer::listener)
                .filter(Objects::nonNull)
                .map(x -> (Runnable) () -> x.accept(""))
                .collect(toList());
    }

}
