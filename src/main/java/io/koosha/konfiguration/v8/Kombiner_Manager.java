package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.KonfigurationManager;
import io.koosha.konfiguration.Q;
import io.koosha.konfiguration.Source;
import lombok.NonNull;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Manager implements KonfigurationManager {

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


    @NotNull
    @NonNull
    final Kombiner origin;

    private final AtomicBoolean consumed = new AtomicBoolean(false);

    public Kombiner_Manager(@NotNull @NonNull Kombiner kombiner) {
        this.origin = kombiner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Kombiner getAndSetToNull() {
        if (this.consumed.getAndSet(true))
            return null;
        return this.origin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String name() {
        return this.origin.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasUpdate() {
        if (!this.consumed.get())
            throw new IllegalStateException("getAndSetToNull() not called yet");
        return origin.r(this::hasUpdate0);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @NotNull
    @Override
    public Collection<Runnable> update() {
        if (!this.consumed.get())
            throw new IllegalStateException("getAndSetToNull() not called yet");
        return origin.r(this::update0);
    }

    private boolean hasUpdate0() {
        return origin
                .sources
                .vs()
                .anyMatch(KonfigurationManager::hasUpdate);
    }

    private Collection<Runnable> update0() {
        if (!this.hasUpdate0())
            return emptyList();

        final Map<String, CheatingMan> newSources = origin.sources.copy();
        final Collection<Runnable> updateTasks = new ArrayList<>();

        newSources.entrySet().forEach(x -> {
            final CheatingMan cheat = x.getValue();
            updateTasks.addAll(
                    cheat.update().stream()
                         .map(Kombiner_Manager::wrap).collect(toList()));
            x.setValue(cheat.updated());
        });

        final Set<Q<?>> updated = new HashSet<>();
        final Map<Q<?>, Object> newCache = origin.values.copy();
        origin.values.origForEach(q -> {
            final String key = requireNonNull(q.key(), "key passed through kombiner is null");

            final Optional<Source> first = newSources
                    .values()
                    .stream()
                    .filter(x -> x.source().has(q))
                    .map(CheatingMan::source)
                    .findFirst();

            final Object newV = first.map(k -> k.custom(q)).orElse(null);
            final Object oldV = origin.has(q)
                                ? origin.values.v_(q, null, true)
                                : null;

            if (origin.values.has(q) != first.isPresent()
                    || !Objects.equals(newV, oldV))
                updated.add(q);

            if (first.isPresent())
                newCache.put(q, newV);
        });

        return origin.w(() -> {
            for (final Q<?> q : updated)
                updateTasks.addAll(this.origin.observers.get(q));

            origin.sources.replace(newSources);
            origin.values.replace(newCache);

            return updateTasks;
        });
    }

}
