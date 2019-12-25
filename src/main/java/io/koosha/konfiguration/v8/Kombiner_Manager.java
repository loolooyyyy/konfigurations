package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.K;
import io.koosha.konfiguration.KonfigurationManager;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.type.Q;
import lombok.NonNull;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Manager implements KonfigurationManager {

    @NotNull
    @NonNull
    final Kombiner origin;
    private final AtomicBoolean consumed = new AtomicBoolean(false);

    Kombiner_Manager(@NotNull @NonNull final Kombiner kombiner) {
        this.origin = kombiner;
    }

    @NotNull
    @Contract(value = "_ -> new",
            pure = true)
    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
    private static Runnable wrap(@NonNull @NotNull final Runnable r) {
        // We can not be sure if given runnable is safe to be put in a map
        // So we create a plain object wrapping it.
        return new Runnable() {
            @Override
            public void run() {
                r.run();
            }
        };
    }

    @Override
    public Kombiner getAndSetToNull() {
        if (this.consumed.getAndSet(true))
            return null;
        return this.origin;
    }

    @Override
    @NotNull
    public String name() {
        return this.origin.name();
    }

    @Override
    public boolean hasUpdate() {
        if (!this.consumed.get())
            throw new IllegalStateException("getAndSetToNull() not called yet");
        return this.origin.r(this::hasUpdate0);
    }

    @NotNull
    @Override
    public Collection<Runnable> update() {
        if (!this.consumed.get())
            throw new IllegalStateException("getAndSetToNull() not called yet");
        return this.origin.r(this::update0);
    }

    private boolean hasUpdate0() {
        return this.origin
                .sources
                .vs()
                .anyMatch(KonfigurationManager::hasUpdate);
    }

    private Collection<Runnable> update0() {
        if (!this.hasUpdate0())
            return emptyList();

        final Map<String, CheatingMan> newSources = this.origin.sources.copy();
        final Collection<Runnable> updateTasks = new ArrayList<>();

        newSources.entrySet().forEach(x -> {
            final CheatingMan cheat = x.getValue();
            updateTasks.addAll(
                    cheat.update().stream()
                         .map(Kombiner_Manager::wrap).collect(toList()));
            x.setValue(cheat.updated());
        });

        final Collection<Q<?>> updated = new HashSet<>();
        final Map<Q<?>, Object> newCache = this.origin.values.copy();
        this.origin.values.origForEach(q -> {
            final Optional<Source> first = newSources
                    .values()
                    .stream()
                    .filter(x -> x.source().has(q))
                    .map(CheatingMan::source)
                    .findFirst();

            @SuppressWarnings({"unchecked", "rawtypes"})
            final Object newV = first.map(x -> x.custom(q))
                                     .orElse(K.null_((Q) q)).v();
            final Object oldV = this.origin.has(q)
                                ? this.origin.values.v_(q, null, true)
                                : null;

            if (this.origin.values.has(q) != first.isPresent()
                    || !Objects.equals(newV, oldV))
                updated.add(q);

            if (first.isPresent())
                newCache.put(q, newV);
        });

        updateTasks.addAll(this.origin.observers.get());
        for (final Q<?> q : updated)
            updateTasks.addAll(this.origin.observers.get(q));

        return this.origin.w(() -> {
            this.origin.sources.replace(newSources);
            this.origin.values.replace(newCache);
            return updateTasks;
        });
    }

}
