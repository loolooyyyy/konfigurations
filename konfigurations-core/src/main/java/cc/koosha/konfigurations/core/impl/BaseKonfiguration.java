package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.KonfigV;
import cc.koosha.konfigurations.core.Konfiguration;
import lombok.NonNull;

import java.util.List;
import java.util.Map;


public abstract class BaseKonfiguration implements Konfiguration {

    private final KonfigKeyObservers observers;

    protected BaseKonfiguration(final KonfigKeyObservers observers) {

        this.observers = observers;
    }

    // __________________________________________________________________ PUBLIC

    @Override
    public final KonfigV<Boolean> bool(@NonNull final String key) {

        return new KonfigVImpl<>(this, this.observers, key, () -> this._bool(key));
    }

    @Override
    public final KonfigV<Integer> int_(@NonNull final String key) {

        return new KonfigVImpl<>(this, this.observers, key, () -> this._int(key));
    }

    @Override
    public final KonfigV<Long> long_(@NonNull final String key) {

        return new KonfigVImpl<>(this, this.observers, key, () -> this._long(key));
    }

    @Override
    public final KonfigV<String> string(@NonNull final String key) {

        return new KonfigVImpl<>(this, this.observers, key, () -> this._string(key));
    }

    @Override
    public final <T> KonfigV<List<T>> list(@NonNull final String key,
                                           @NonNull final Class<T> type) {

        return new KonfigVImpl<>(this, this.observers, key, () -> this._list(key, type));
    }

    @Override
    public final <T> KonfigV<Map<String, T>> map(@NonNull final String key,
                                                 @NonNull final Class<T> type) {

        return new KonfigVImpl<>(this, this.observers, key, () -> this._map(key, type));
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> KonfigV<T> custom(@NonNull final String key,
                                       @NonNull final Class<T> type) {

        if(Boolean.class.equals(type))
            return (KonfigV<T>) this.bool(key);

        if(Integer.class.equals(type))
            return (KonfigV<T>) this.int_(key);

        if(Long.class.equals(type))
            return (KonfigV<T>) this.long_(key);

        if(String.class.equals(type))
            return (KonfigV<T>) this.string(key);

        return new KonfigVImpl(this, this.observers, key, () -> this._custom(key, type));
    }


    // __________________________________________________________ ACTUAL GETTERS

    protected abstract String _string(String key);

    protected abstract Boolean _bool(String key);

    protected abstract Long _long(String key);

    protected abstract Integer _int(String key);

    protected abstract <T> List<T> _list(String key, Class<T> elementsType);

    protected abstract <T> Map<String, T> _map(String key, Class<T> elementsType);

    protected Object _custom(String key, Class<?> clazz) {

        throw new UnsupportedOperationException("config of type: " + clazz.getName() + ", for key: " + key);
    }

}
