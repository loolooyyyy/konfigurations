package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.KonfigV;
import cc.koosha.konfigurations.core.Konfiguration;
import cc.koosha.konfigurations.core.EverythingObserver;
import lombok.NonNull;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.Set;


final class KonfigurationSubsetView implements Konfiguration {

    private final Konfiguration wrapped;
    private final String baseKey;

    KonfigurationSubsetView(@NonNull final Konfiguration wrapped,
                            @NonNull final String baseKey) {

        this.wrapped = wrapped;
        this.baseKey = baseKey.endsWith(".") ? baseKey : baseKey + ".";
    }


    @Override
    public KonfigV<Boolean> bool(final String key) {

        return wrapped.bool(baseKey + key);
    }

    @Override
    public KonfigV<Integer> int_(final String key) {

        return wrapped.int_(baseKey + key);
    }

    @Override
    public KonfigV<Long> long_(final String key) {

        return wrapped.long_(baseKey + key);
    }

    @Override
    public KonfigV<Double> double_(final String key) {

        return wrapped.double_(baseKey + key);
    }

    @Override
    public KonfigV<String> string(final String key) {

        return wrapped.string(baseKey + key);
    }

    @Override
    public <T> KonfigV<List<T>> list(final String key, final Class<T> type) {

        return wrapped.list(baseKey + key, type);
    }

    @Override
    public <T> KonfigV<Map<String, T>> map(final String key, final Class<T> type) {

        return wrapped.map(baseKey + key, type);
    }

    @Override
    public <T> KonfigV<Set<T>> set(final String key, final Class<T> type) {

        return wrapped.set(baseKey + key, type);
    }

    @Override
    public <T> KonfigV<T> custom(final String key, final Class<T> type) {

        return wrapped.custom(baseKey + key, type);
    }


    @Override
    public KonfigV<Boolean> boolD(final String key) {

        return wrapped.boolD(baseKey + key);
    }

    @Override
    public KonfigV<Integer> intD(final String key) {

        return wrapped.intD(baseKey + key);
    }

    @Override
    public KonfigV<Long> longD(final String key) {

        return wrapped.longD(baseKey + key);
    }

    @Override
    public KonfigV<Double> doubleD(final String key) {

        return wrapped.doubleD(baseKey + key);
    }

    @Override
    public KonfigV<String> stringD(final String key) {

        return wrapped.stringD(baseKey + key);
    }

    @Override
    public <T> KonfigV<List<T>> listD(final String key, final Class<T> type) {

        return wrapped.listD(baseKey + key, type);
    }

    @Override
    public <T> KonfigV<Map<String, T>> mapD(final String key, final Class<T> type) {

        return wrapped.mapD(baseKey + key, type);
    }

    @Override
    public <T> KonfigV<Set<T>> setD(final String key, final Class<T> type) {

        return wrapped.setD(baseKey + key, type);
    }

    @Override
    public <T> KonfigV<T> customD(final String key, final Class<T> type) {

        return wrapped.customD(baseKey + key, type);
    }

    @Override
    public boolean update() {

        return wrapped.update();
    }

    @Override
    public Konfiguration subset(@NonNull final String key) {

        val newKey = this.baseKey + "." + key;
        return new KonfigurationSubsetView(this.wrapped, newKey);
    }

    @Override
    public Konfiguration register(final EverythingObserver observer) {

        return this.wrapped.register(observer);
    }

    @Override
    public Konfiguration deregister(final EverythingObserver observer) {

        return this.wrapped.deregister(observer);
    }

}
