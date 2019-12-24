package io.koosha.konfiguration;

import io.koosha.konfiguration.error.KfgMissingKeyException;
import io.koosha.konfiguration.type.Q;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;

/**
 * Dummy konfig value, holding a constant konfig value with no source.
 *
 * <p>Regarding equals and hashcode: Each instance of DummyV is considered to be
 * from a different origin (in contrast to _KonfigVImpl, so only each
 * object is equal to itself only, even with same key and values.
 *
 * @param <U> type of konfig value this object holds.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@ThreadSafe
@Immutable
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@ApiStatus.Internal
final class DummyV<U> implements K<U> {

    private final static Handle M_1 = new Handle() {
        @Override
        public String id() {
            return this.getClass().getName();
        }

        @Override
        public int hashCode() {
            return id().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }
    };

    @Accessors(fluent = true)
    @Getter
    @NotNull
    @NonNull

    private final String key;

    @Nullable
    private final U v;

    private final boolean exists;
    
    @Accessors(fluent = true)
    @Getter
    @Nullable
    private final Q<U> type;

    /**
     * {@inheritDoc}
     */
    @Contract(mutates = "this")
    @Override
    public void deregister(@NonNull @NotNull final Handle observer) {
    }

    /**
     * {@inheritDoc}
     */
    @Contract(mutates = "this")
    @NotNull
    @Override
    public Handle registerSoft(@NonNull @NotNull final KeyObserver observer) {
        return M_1;
    }

    /**
     * {@inheritDoc}
     */
    @Contract(mutates = "this")
    @NotNull
    @Override
    public Handle register(@NonNull @NotNull final KeyObserver observer) {
        return M_1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    @Contract(pure = true)
    public U v() {
        if (this.exists())
            return this.v;

        throw new KfgMissingKeyException(null,
                this.type == null ? Q.unknown(this.key) : this.type.withKey(this.key));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public U vn() {
        final U v = this.v();

        if (v == null)
            throw new KfgMissingKeyException(null,
                    this.type == null ? Q.unknown(this.key) : this.type.withKey(this.key));

        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    public boolean exists() {
        return this.exists;
    }

    // ________________________________________________________________________

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String vStr;
        String keyStr = this.key;
        try {
            vStr = String.valueOf(this.v);
        }
        catch (final Throwable e) {
            vStr = "";
            keyStr = "!" + key;
        }
        return format("K[exists=%b,%s=%s]", this.exists, keyStr, vStr);
    }

}

