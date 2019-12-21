package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.K;
import io.koosha.konfiguration.KeyObserver;
import io.koosha.konfiguration.Q;
import io.koosha.konfiguration.error.KfgMissingKeyException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;

@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@ThreadSafe
@ApiStatus.Internal
final class Kombiner_K<U> implements K<U> {

    @NonNull
    @NotNull
    private final Kombiner origin;

    @NonNull
    @NotNull
    @Getter
    private final Q<U> type;


    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public U v() {
        return this.origin.values.v(this.type, null, true);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public U vn() {
        final U v = this.v();

        if (v == null)
            throw new KfgMissingKeyException(this.origin.name(), null, this.type);

        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    public boolean exists() {
        return origin.has(this.type);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Handle registerSoft(@NonNull @NotNull KeyObserver observer) {
        return origin.registerSoft(observer, this.type);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    @NotNull
    public Handle register(@NonNull @NotNull final KeyObserver observer) {
        return this.origin.register(observer, this.type);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public void deregister(@NonNull @NotNull final Handle observer) {
        this.origin.deregister(observer);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public @NotNull String key() {
        //noinspection ConstantConditions
        return this.type.key();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String toString() {
        try {
            return format("K(%s/%s=%s)", this.type, this.type.key(), this.v());
        }
        catch (final Exception e) {
            return format("K(%s)::error", this.type.toString());
        }
    }

}
