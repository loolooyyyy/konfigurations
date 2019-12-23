package io.koosha.konfiguration;

import io.koosha.konfiguration.error.KfgIllegalArgumentException;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read only subset view of a konfiguration. Prepends a pre-defined key
 * to all konfig values
 * <p>
 * Ignore the J prefix.
 *
 * <p>Immutable and thread safe by itself, although the underlying wrapped
 * konfiguration's thread safety is not guarantied.
 */
@ThreadSafe
@ApiStatus.Internal
final class SubsetView implements Konfiguration {

    @Accessors(fluent = true)
    @Getter
    private final String name;
    private final Konfiguration wrapped;
    private final String baseKey;

    SubsetView(@NonNull @NotNull final String name,
               @NotNull @NonNull final Konfiguration wrapped,
               @NotNull @NonNull final String baseKey) {
        this.name = name;
        this.wrapped = wrapped;

        if (baseKey.startsWith(".")) // covers baseKey == "." too.
            throw new KfgIllegalArgumentException(this.name(), "key must not start with a dot: " + baseKey);
        if (baseKey.contains(".."))
            throw new KfgIllegalArgumentException(this.name(), "key can not contain subsequent dots: " + baseKey);

        if (baseKey.isEmpty())
            this.baseKey = "";
        else if (baseKey.endsWith("."))
            this.baseKey = baseKey;
        else
            this.baseKey = baseKey + ".";
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    @NotNull
    public K<Boolean> bool(@NotNull @NonNull final String key) {
        return wrapped.bool(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @NotNull
    @Override
    public K<Byte> byte_(@NotNull @NonNull final String key) {
        return wrapped.byte_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @NotNull
    @Override
    public K<Character> char_(@NotNull @NonNull final String key) {
        return wrapped.char_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @NotNull
    @Override
    public K<Short> short_(@NotNull @NonNull final String key) {
        return wrapped.short_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @NotNull
    @Override
    public K<Integer> int_(@NotNull @NonNull final String key) {
        return wrapped.int_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @NotNull
    @Override
    public K<Long> long_(@NotNull @NonNull final String key) {
        return wrapped.long_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @NotNull
    @Override
    public K<Float> float_(@NotNull @NonNull final String key) {
        return wrapped.float_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @NotNull
    @Override
    public K<Double> double_(@NotNull @NonNull final String key) {
        return wrapped.double_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @NotNull
    @Override
    public K<String> string(@NotNull @NonNull final String key) {
        return wrapped.string(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U> K<List<U>> list(@NotNull @NonNull final Q<List<U>> key) {
        return wrapped.list(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <T> K<List<T>> list(@NotNull @NonNull final String key,
                               @NotNull @NonNull final Class<T> type) {
        return wrapped.list(key(key), type);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public <U> K<Set<U>> set(@NotNull @NonNull final Q<Set<U>> key) {
        return wrapped.set(key(key));
    }

    @NotNull
    @Override
    public <T> K<Set<T>> set(@NotNull @NonNull final String key,
                             @NotNull @NonNull final Class<T> type) {
        return wrapped.set(key(key), type);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public <U, V> K<Map<U, V>> map(@NotNull @NonNull final Q<Map<U, V>> key) {
        return this.wrapped.map(key);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public <U, V> K<Map<U, V>> map(@NotNull @NonNull final String key,
                                   @NotNull @NonNull final Class<U> keyKlass,
                                   @NotNull @NonNull final Class<V> valueKlass) {
        return this.wrapped.map(key(key), keyKlass, valueKlass);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public <U> K<U> custom(@NotNull @NonNull final String key) {
        return wrapped.custom(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public <U> K<U> custom(@NotNull @NonNull final Q<U> key) {
        return wrapped.custom(key(key));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public <U> K<U> custom(@NotNull @NonNull final String key,
                           @NotNull @NonNull final Class<U> type) {
        return this.wrapped.custom(key(key), type);
    }

    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull @NonNull final Q<?> key) {
        return this.wrapped.has(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull @NonNull final String key,
                       @NotNull @NonNull final Class<?> type) {
        return this.wrapped.has(key(key), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasBool(@NotNull @NonNull final String key) {
        return this.wrapped.hasBool(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasChar(@NotNull @NonNull final String key) {
        return this.wrapped.hasChar(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasString(@NotNull @NonNull final String key) {
        return this.wrapped.hasString(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasByte(@NotNull @NonNull final String key) {
        return this.wrapped.hasBool(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasShort(@NotNull @NonNull final String key) {
        return this.wrapped.hasShort(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasInt(@NotNull @NonNull final String key) {
        return this.wrapped.hasInt(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLong(@NotNull @NonNull final String key) {
        return this.wrapped.hasLong(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasFloat(@NotNull @NonNull final String key) {
        return this.wrapped.hasFloat(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDouble(@NotNull @NonNull final String key) {
        return this.wrapped.hasDouble(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMap(@NotNull @NonNull final Q<Map<?, ?>> key) {
        return this.wrapped.hasMap(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMap(@NotNull @NonNull final String key,
                          @NotNull @NonNull final Class<?> keyType,
                          @NotNull @NonNull final Class<?> valueType) {
        return this.wrapped.hasMap(key(key), keyType, valueType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSet(@NotNull @NonNull final Q<Set<?>> key) {
        return this.wrapped.hasSet(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSet(@NotNull @NonNull final String key,
                          @NotNull @NonNull final Class<?> type) {
        return this.wrapped.hasSet(key(key), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasList(@NotNull @NonNull final Q<List<?>> key) {
        return this.wrapped.hasList(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasList(@NotNull @NonNull final String key,
                           @NotNull @NonNull final Class<?> type) {
        return this.wrapped.hasList(key, type);
    }


    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer) {
        return this.wrapped.registerSoft(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @NotNull
    @Override
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer,
                               @NotNull @NonNull final String key) {
        return this.wrapped.registerSoft(observer, key(key));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer,
                               @NotNull @NonNull final Q<?> key) {
        return this.wrapped.registerSoft(observer, key(key));
    }

    // =================================

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Handle register(@NotNull @NonNull final KeyObserver observer) {
        return this.wrapped.register(observer);
    }


    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Handle register(@NotNull final KeyObserver observer,
                           @NotNull final Q<?> key) {
        return this.wrapped.register(observer, key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    @NotNull
    public Handle register(@NotNull @NonNull final KeyObserver observer,
                           @NotNull @NonNull final String key) {
        return this.wrapped.register(observer, key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deregister(@NotNull @NonNull final Handle observer) {
        this.wrapped.deregister(observer);
    }

    // =================================

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true,
            value = "_ -> _")
    @NotNull
    @Override
    public Konfiguration subset(@NonNull @NotNull final String key) {
        return key.isEmpty()
               ? this
               : new SubsetView(
                       this.wrapped + "::" + this.key(key),
                       this.wrapped,
                       this.key(key)
               );
    }

    @Contract(pure = true,
            value = "_ -> _")
    @NotNull
    private String key(@NonNull @NotNull final String key) {
        if (key.startsWith("."))
            throw new KfgIllegalArgumentException(this.name(), "key must not start with a dot: " + key);

        return this.baseKey + key;
    }

    @Contract(pure = true,
            value = "_ -> new")
    private <T> Q<T> key(@NotNull @NonNull final Q<T> key) {
        final String k = key.key();
        final String kk = key(k);
        return key.withKey(kk);
    }


}
