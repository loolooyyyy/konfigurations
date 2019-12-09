package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Read only subset view of a konfiguration. Prepends a pre-defined key
 * to all konfig values
 *
 * <p>Immutable and thread safe by itself, although the underlying wrapped
 * konfiguration's thread safety is not guarantied.
 */
@ThreadSafe
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
            throw new KfgIllegalArgumentException(this, "key must not start with a dot: " + baseKey);
        if (baseKey.contains(".."))
            throw new KfgIllegalArgumentException(this, "key can not contain subsequent dots: " + baseKey);

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
    @Override
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    public K<Boolean> bool(@NotNull @NonNull final String key) {
        return wrapped.bool(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    public K<Byte> byte_(@NotNull @NonNull final String key) {
        return wrapped.byte_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(mutates = "this")
    @NonNull
    @NotNull
    public K<Character> char_(@NotNull @NonNull String key) {
        return wrapped.char_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(mutates = "this")
    @NonNull
    @NotNull
    public K<Short> short_(@NotNull @NonNull String key) {
        return wrapped.short_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(mutates = "this")
    @NonNull
    @NotNull
    public K<Integer> int_(@NotNull @NonNull final String key) {
        return wrapped.int_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(mutates = "this")
    @NonNull
    @NotNull
    public K<Long> long_(@NotNull @NonNull final String key) {
        return wrapped.long_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(mutates = "this")
    @NonNull
    @NotNull
    public K<Float> float_(@NotNull @NonNull final String key) {
        return wrapped.float_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(mutates = "this")
    @NonNull
    @NotNull
    public K<Double> double_(@NotNull @NonNull final String key) {
        return wrapped.double_(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(mutates = "this")
    @NonNull
    @NotNull
    public K<String> string(@NotNull @NonNull final String key) {
        return wrapped.string(key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(mutates = "this")
    @NonNull
    @NotNull
    public <U> K<List<U>> list(@NonNull @NotNull final String key,
                               @Nullable final Q<List<U>> type) {
        return wrapped.list(key(key), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(mutates = "this")
    @NonNull
    @NotNull
    public <U, V> K<Map<U, V>> map(@NonNull @NotNull final String key,
                                   @Nullable final Q<Map<U, V>> type) {
        return wrapped.map(key(key), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(mutates = "this")
    @NonNull
    @NotNull
    public <U> K<Set<U>> set(@NotNull @NonNull final String key,
                             @Nullable final Q<Set<U>> type) {
        return wrapped.set(key(key), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(mutates = "this")
    @NonNull
    @NotNull
    public <U> K<U> custom(@NotNull @NonNull final String key,
                           @Nullable final Q<U> type) {
        return wrapped.custom(key(key), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    public boolean has(@NonNull @NotNull final String key,
                       @Nullable final Q<?> type) {
        return wrapped.has(key(key), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    @Contract(mutates = "this")
    @NotNull
    public Handle registerSoft(@NotNull @NonNull final KeyObserver observer,
                               @NotNull @NonNull final String key) {
        return this.wrapped.registerSoft(observer, key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    public Konfiguration deregister(@NotNull @NonNull final Handle observer,
                                    @NotNull @NonNull final String key) {
        return this.wrapped.deregister(observer, key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(mutates = "this")
    public @NonNull @NotNull Handle register(@NotNull @NonNull final KeyObserver observer,
                                             @NotNull @NonNull final String key) {
        return this.wrapped.register(observer, key(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    @NotNull
    @Contract(pure = true,
            value = "_ -> _")
    public Konfiguration subset(@NonNull @NotNull final String key) {
        return key.isEmpty()
               ? this
               : new SubsetView(
                       this.name.split("::")[0] + "::" + key,
                       this.wrapped,
                       this.baseKey + this.key(key)
               );
    }

    /**
     * Manager object associated with this konfiguration.
     *
     * @return On first invocation, a manager instance. An second invocation
     * and on, throws exception.
     * @throws KfgIllegalStateException if manager is already called once
     *                                  before.
     */
    @Override
    @NotNull

    @Contract(pure = true,
            value = "-> fail")
    public Manager manager() {
        throw new KfgIllegalStateException(this.name(), "do not call manager() in your code");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> _")
    private String key(@NonNull @NotNull final String key) {
        if (Objects.equals(key, KeyObserver.LISTEN_TO_ALL))
            return key;

        if (key.startsWith("."))
            throw new KfgIllegalArgumentException(this, "key must not start with a dot: " + key);

        return this.baseKey + key;
    }

}
