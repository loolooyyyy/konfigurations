package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.K;
import io.koosha.konfiguration.KeyObserver;
import io.koosha.konfiguration.Q;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.lang.String.format;

@AllArgsConstructor
@Accessors(fluent = true)
final class Kombiner_KonfigVImpl<U> implements K<U> {

    @NonNull
    @NotNull
    private final Kombiner origin;

    @NonNull
    @NotNull
    @Getter(onMethod_ = {@NotNull})
    private final String key;

    @NonNull
    @NotNull
    @Getter(onMethod_ = {@NotNull})
    private final Q<U> type;

    boolean isSameAs(final Q<?> q) {
        return Q.matchesType(this.type, q);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public U v() {
        return this.origin.getValue(key, null, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<U> deregister(@NonNull @NotNull final KeyObserver observer) {
        this.origin.deregisterSoft(observer, this.key);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public K<U> register(@NonNull @NotNull final KeyObserver observer) {
        this.origin.registerSoft(observer, this.key);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        try {
            return format("K(%s=%s)", this.key, this.v());
        }
        catch (final Exception e) {
            return format("K(%s)::error", this.key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Kombiner_KonfigVImpl<?> konfigV = (Kombiner_KonfigVImpl<?>) o;
        return Objects.equals(origin, konfigV.origin) && Objects.equals(key, konfigV.key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(origin, key);
    }

}
