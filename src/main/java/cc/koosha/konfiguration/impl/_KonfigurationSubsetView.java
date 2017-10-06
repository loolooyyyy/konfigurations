package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.EverythingObserver;
import cc.koosha.konfiguration.KonfigV;
import cc.koosha.konfiguration.Konfiguration;
import lombok.NonNull;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.Set;


final class _KonfigurationSubsetView implements Konfiguration {

    private final Konfiguration wrapped;
    private final String        baseKey;

    _KonfigurationSubsetView(@NonNull final Konfiguration wrapped,
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
    public boolean update() {

        return wrapped.update();
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
