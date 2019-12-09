package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.K;
import io.koosha.konfiguration.KeyObserver;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.Konfiguration.Manager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@NotThreadSafe
@RequiredArgsConstructor
final class Kombiner_Manager implements Manager {

    @NotNull
    @NonNull
    private final Kombiner kombiner;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasUpdate() {
        return kombiner.lock().doReadLocked(() -> kombiner.sources
                .stream()
                .filter(x -> x != this.kombiner)
                .map(Konfiguration::manager)
                .anyMatch(Manager::hasUpdate));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    @NotNull
    public Map<String, Stream<Runnable>> update() {
        kombiner.lock().doReadLocked(() -> {
            update0();
            return null;
        });

        if (updatedKeys.isEmpty()) {
            observers = Collections.emptyMap();
        }
        else {
            valueCache.clear();
            valueCache.putAll(newCache);

            Kombiner.this.sources.clear();
            Kombiner.this.sources.addAll(updatedSources);

            observers = new HashMap<>(Kombiner.this.keyObservers);
        }

        observers.entrySet()
                 .stream()
                 .filter(e -> e.getKey() != null)
                 .forEach(e -> {
                     final Collection<String> keys = e.getValue();
                     final KeyObserver observer = e.getKey();
                     updatedKeys.stream()
                                .filter(keys.contains(KeyObserver.LISTEN_TO_ALL)
                                        ? x -> true
                                        : keys::contains)
                                .forEach(observer);
                 });
    }

    private void update0() {
        if (kombiner.sources.stream().noneMatch(k -> k != kombiner && k.manager().hasUpdate()))
            return;

        final List<Konfiguration> up = kombiner
                .sources
                .stream()
                .filter(x -> x != kombiner)
                .map(Konfiguration::manager)
                .map(Manager::update)
                .collect(toList());

        final Map<String, K<?>> newStorage = new HashMap<>();
        for (final Map.Entry<String, K<?>> e : storage.entrySet()) {
            up.stream()
              .filter(k -> k.has(p.a, p.b))
              .map(k -> k.custom(p.a, p.b))
              .findFirst()
              .map(found -> k(p.a, p.b, found.v()))
              .orElse(null);
        }

        final Map<String, ? extends K<?>> newStorage = storage
                .entrySet()
                .stream()
                .map(e -> {
                            final
                        }
                ));

        final Map<KeyObserver, Collection<String>> newObservers = Kombiner.this.keyObservers
                .entrySet()
                .stream()
                .filter(e -> e.getKey() != null)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final List<String> updatedKeys = storage
                .entrySet()
                .stream()
                .filter(e -> !compare(e.getKey(), e.getValue(), newStorage.get(e.getKey())))
                .map(Map.Entry::getKey)
                .collect(toList());

        //            final Set<String> updated = storage
        //                    .entrySet()
        //                    .stream()
        //                    .filter(e -> updatedSources.stream().anyMatch(k ->
        //                            k.contains(key(e.getKey(), e.getValue().getType()))))
        //                    .map(Map.Entry::getKey)
        //                    .collect(toSet());

        doWriteLocked(() -> {
            for (final Map.Entry<String, Object> each : Kombiner.this.valueCache.entrySet()) {
                final String name1 = each.getKey();
                final Object value = each.getValue();
                boolean found = false;
                boolean changed = false;
                for (final Konfiguration source : up) {
                    if (source.contains(name1)) {
                        found = true;
                        final K<?> kVal = kvalCache.get(name1);
                        final Object newVal = Kombiner.this.putValue(
                                source,
                                name1,
                                kVal.type);
                        newCache.put(name1, newVal);
                        changed = !newVal.equals(value);
                        break;
                    }
                }

                if (!found || changed)
                    updatedKeys.add(name1);
            }

            return null;
        });


    }

}
