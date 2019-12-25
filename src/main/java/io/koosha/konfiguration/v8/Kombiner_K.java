package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.K;
import io.koosha.konfiguration.KeyObserver;
import io.koosha.konfiguration.error.KfgMissingKeyException;
import io.koosha.konfiguration.type.Q;
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


    @Override
    @Nullable
    public U v() {
        return this.origin.values.v(this.type, null, true);
    }

    @NotNull
    @Override
    public U vn() {
        final U v = this.v();

        if (v == null)
            throw new KfgMissingKeyException(this.origin.name(), this.type);

        return v;
    }

    @Override
    @Contract(pure = true)
    public boolean exists() {
        return this.origin.has(this.type);
    }


    @Override
    @NotNull
    public Handle registerSoft(@NonNull @NotNull final KeyObserver observer) {
        return this.origin.registerSoft(observer, this.type);
    }

    @Override
    @NotNull
    public Handle register(@NonNull @NotNull final KeyObserver observer) {
        return this.origin.register(observer, this.type);
    }

    @Override
    public void deregister(@NonNull @NotNull final Handle observer) {
        this.origin.deregister(observer);
    }

    @Override
    @NotNull
    public String key() {
        return this.type.key();
    }

    @NotNull
    @Override
    public String toString() {
        try {
            //noinspection HardcodedFileSeparator
            return format("K(%s/%s)", this.type, this.type.key());
        }
        catch (final Exception e) {
            return format("K(%s)::error", this.type);
        }
    }

}
