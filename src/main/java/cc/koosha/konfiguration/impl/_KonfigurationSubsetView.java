package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.EverythingObserver;
import cc.koosha.konfiguration.K;
import cc.koosha.konfiguration.Konfiguration;
import cc.koosha.konfiguration.KonfigurationException;
import lombok.NonNull;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.Set;


final class _KonfigurationSubsetView implements Konfiguration {

    private final Konfiguration wrapped;
    private final String baseKey;

    _KonfigurationSubsetView(@NonNull final Konfiguration wrapped,
                             @NonNull final String baseKey) {

        this.wrapped = wrapped;
        this.baseKey = baseKey.endsWith(".") ? baseKey : baseKey + ".";
    }


    @Override
    public K<Boolean> bool(final String key) {

        return wrapped.bool(baseKey + key);
    }

    @Override
    public K<Integer> int_(final String key) {

        return wrapped.int_(baseKey + key);
    }

    @Override
    public K<Long> long_(final String key) {

        return wrapped.long_(baseKey + key);
    }

    @Override
    public K<Double> double_(final String key) {

        return wrapped.double_(baseKey + key);
    }

    @Override
    public K<String> string(final String key) {

        return wrapped.string(baseKey + key);
    }

    @Override
    public <T> K<List<T>> list(final String key, final Class<T> type) {

        return wrapped.list(baseKey + key, type);
    }

    @Override
    public <T> K<Map<String, T>> map(final String key, final Class<T> type) {

        return wrapped.map(baseKey + key, type);
    }

    @Override
    public <T> K<Set<T>> set(final String key, final Class<T> type) {

        return wrapped.set(baseKey + key, type);
    }

    @Override
    public <T> K<T> custom(final String key, final Class<T> type) {

        return wrapped.custom(baseKey + key, type);
    }


    @Override
    public boolean update() {

        throw new KonfigurationException("update is not available from subset view");
    }


    @Override
    public Konfiguration subset(@NonNull final String key) {

        val newKey = this.baseKey + "." + key;
        return new _KonfigurationSubsetView(this.wrapped, newKey);
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
