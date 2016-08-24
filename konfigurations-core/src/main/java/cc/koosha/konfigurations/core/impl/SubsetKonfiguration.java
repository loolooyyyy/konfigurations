package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.KonfigV;
import cc.koosha.konfigurations.core.Konfiguration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
public final class SubsetKonfiguration implements Konfiguration {

    @NonNull
    private final Konfiguration wrapped;

    @NonNull
    private final String baseKey;

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
    public <T> KonfigV<T> custom(final String key, final Class<T> type) {

        return wrapped.custom(baseKey + key, type);
    }

    @Override
    public boolean update() {

        return wrapped.update();
    }

    @Override
    public Konfiguration subset(@NonNull final String key) {

        return new SubsetKonfiguration(this.wrapped, this.baseKey + key);
    }

    @Override
    public Konfiguration parent() {

        return this.wrapped;
    }

}
