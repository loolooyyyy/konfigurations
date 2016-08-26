package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.Konfiguration;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.val;

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
        this.inMem.update();
        return this.inMem;
    }

    public KonfigurationMover bool(@NonNull final String... keys) {

        for (val key : keys)
            this.add(key, from.bool(key).v());

        return this;
    }

    public KonfigurationMover int_(@NonNull final String... keys) {

        for (val key : keys)
            this.add(key, from.int_(key).v());

        return this;
    }

    public KonfigurationMover long_(@NonNull final String... keys) {

        for (val key : keys)
            this.add(key, from.long_(key).v());

        return this;
    }

    public KonfigurationMover string(@NonNull final String... keys) {

        for (val key : keys)
            this.add(key, from.string(key).v());

        return this;
    }

    public <T> KonfigurationMover list(@NonNull final Class<T> type,
                                       @NonNull final String... keys) {

        for (val key : keys)
            this.add(key, from.list(key, type).v());

        return this;
    }

    public <T> KonfigurationMover map(@NonNull final Class<T> type,
                                      @NonNull final String... keys) {

        for (val key : keys)
            this.add(key, from.map(key, type).v());

        return this;
    }

    public <T> KonfigurationMover custom(@NonNull final Class<T> type,
                                         @NonNull final String... keys) {

        for (val key : keys)
            this.add(key, from.custom(key, type).v());

        return this;
    }


    public KonfigurationMover subBool(@NonNull final String subSection,
                                      @NonNull final String... keys) {

        for (val key : keys)
            this.bool(subSection + key);

        return this;
    }

    public KonfigurationMover subInt_(@NonNull final String subSection,
                                      @NonNull final String... keys) {

        for (val key : keys)
            this.int_(subSection + key);

        return this;
    }

    public KonfigurationMover subLong_(@NonNull final String subSection,
                                       @NonNull final String... keys) {

        for (val key : keys)
            this.long_(subSection + key);

        return this;
    }

    public KonfigurationMover subString(@NonNull final String subSection,
                                        @NonNull final String... keys) {

        for (val key : keys)
            this.string(subSection + key);

        return this;
    }

    public <T> KonfigurationMover subList(@NonNull final String subSection,
                                          @NonNull final Class<T> type,
                                          @NonNull final String... keys) {

        for (val key : keys)
            this.list(type, subSection + key);

        return this;
    }

    public <T> KonfigurationMover subMap(@NonNull final String subSection,
                                         @NonNull final Class<T> type,
                                         @NonNull final String... keys) {

        for (val key : keys)
            this.map(type, subSection + key);

        return this;
    }

    public <T> KonfigurationMover subCustom(@NonNull final String subSection,
                                            @NonNull final Class<T> type,
                                            @NonNull final String... keys) {

        for (val key : keys)
            this.custom(type, subSection + key);

        return this;
    }



    @Synchronized
    private KonfigurationMover add(@NonNull final String key,
                                   final Object value) {

        if(this.fin)
            throw new IllegalStateException("No more modifying is allowed");

        if(this.storage.containsKey(key))
            throw new IllegalArgumentException("duplicate konfig key: " + key);

        this.storage.put(key, value);

        return this;
    }

}
