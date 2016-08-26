package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.Konfiguration;
import lombok.NonNull;
import lombok.Synchronized;

import java.util.HashMap;
import java.util.Map;


/**
 * Thread-safe. Not really useful but anyway.
 */
public final class KonfigurationMover {

    private final Konfiguration from;
    private final Map<String, Object> storage;
    private final InMemoryKonfiguration inMem;
    private boolean fin;

    public KonfigurationMover(final Konfiguration from) {

        this.from = from;
        this.storage = new HashMap<>();

        this.inMem = new InMemoryKonfiguration(new InMemoryKonfiguration.KonfigMapProvider() {
            @Override
            public Map<String, Object> get() {
                return storage;
            }
        });

        fin = false;
    }

    @Synchronized
    public InMemoryKonfiguration get() {

        this.fin = true;
        return this.inMem;
    }

    public KonfigurationMover bool(@NonNull final String key) {

        return this.add(key, from.bool(key).v());
    }

    public KonfigurationMover int_(@NonNull final String key) {

        return this.add(key, from.int_(key).v());
    }

    public KonfigurationMover long_(@NonNull final String key) {

        return this.add(key, from.long_(key).v());
    }

    public KonfigurationMover string(@NonNull final String key) {

        return this.add(key, from.string(key).v());
    }

    public <T> KonfigurationMover list(@NonNull final String key,
                                       @NonNull final Class<T> type) {

        return this.add(key, from.list(key, type).v());
    }

    public <T> KonfigurationMover map(@NonNull final String key,
                                      @NonNull final Class<T> type) {

        return this.add(key, from.map(key, type).v());
    }

    public <T> KonfigurationMover custom(@NonNull final String key,
                                         @NonNull final Class<T> type) {

        return this.add(key, from.custom(key, type).v());
    }

    @Synchronized
    private KonfigurationMover add(@NonNull final String key,
                                   final Object value) {

        if(this.fin)
            throw new IllegalStateException("No more modifying is allowed");

        this.storage.put(key, value);

        return this;
    }

}
