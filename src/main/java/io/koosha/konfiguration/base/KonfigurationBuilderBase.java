package io.koosha.konfiguration.base;

import io.koosha.konfiguration.Faktory;
import io.koosha.konfiguration.KonfigurationBuilder;
import io.koosha.konfiguration.KonfigurationManager;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@ThreadSafe
@RequiredArgsConstructor
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public abstract class KonfigurationBuilderBase implements KonfigurationBuilder {

    /**
     * See {@link KonfigurationBuilder#name()}.
     *
     * @see KonfigurationBuilder#name()
     */
    @NonNull
    @NotNull
    @Getter
    @Accessors(fluent = true)
    private final String name;

    /**
     * See {@link KonfigurationBuilderBase#ensure()}.
     *
     * @see KonfigurationBuilderBase#ensure()
     */
    private final AtomicBoolean isConsumed = new AtomicBoolean(false);

    /**
     * See {@link KonfigurationBuilder#add(Collection)}.
     *
     * @see KonfigurationBuilder#add(Collection)
     */
    private final List<KonfigurationManager> sources = new ArrayList<>();

    /**
     * See {@link KonfigurationBuilder#fairLock(boolean)}.
     *
     * @see KonfigurationBuilder#fairLock(boolean)
     */
    private boolean fairLock = Faktory.FAIR_LOCk__DEFAULT;

    /**
     * See {@link KonfigurationBuilder#mixedTypes(boolean)}.
     *
     * @see KonfigurationBuilder#mixedTypes(boolean)
     */
    private boolean mixedTypes = Faktory.ALLOW_MIXED_TYPES__DEFAULT;

    /**
     * See {@link KonfigurationBuilder#lockWaitTime(long)}.
     *
     * @see KonfigurationBuilder#lockWaitTime(long)
     */
    private Long lockWaitTime = Faktory.LOCK_WAIT_MILLIS__DEFAULT;

    @Contract(value = "_, _, _, _, _ -> new",
            pure = true)
    @ApiStatus.OverrideOnly
    protected abstract KonfigurationManager build0(
            @NotNull String name,
            boolean fairLock,
            boolean mixedTypes,
            @Nullable Long lockWaitTime,
            @NotNull Collection<KonfigurationManager> sources);

    @Contract(mutates = "this")
    @NotNull
    @Synchronized
    @Override
    public final KonfigurationBuilder add(
            @NotNull @NonNull final Collection<? extends KonfigurationManager> konfig) {
        this.ensure();
        this.sources.addAll(konfig);
        return this;
    }

    @Contract(mutates = "this")
    @NotNull
    @Synchronized
    @Override
    public final KonfigurationBuilder fairLock(final boolean fair) {
        this.ensure();
        this.fairLock = fair;
        return this;
    }

    @Override
    @NotNull
    public final KonfigurationBuilder mixedTypes(final boolean allow) {
        this.ensure();
        this.mixedTypes = allow;
        return this;
    }


    @Contract(mutates = "this")
    @NotNull
    @Synchronized
    @Override
    public final KonfigurationBuilder lockWaitTime(final long waitTime) {
        this.ensure();
        if (waitTime < 0)
            throw new IllegalArgumentException("lock wait time must be gte 0, given: " + waitTime);
        this.lockWaitTime = waitTime;
        return this;
    }

    @Contract(mutates = "this")
    @NotNull
    @Synchronized
    @Override
    public final KonfigurationBuilder lockNoWait() {
        this.ensure();
        this.lockWaitTime = null;
        return this;
    }

    @Contract(mutates = "this")
    @NotNull
    @Synchronized
    @Override
    public final KonfigurationManager build() {
        this.ensure();
        this.isConsumed.set(true);
        return this.build0(
                this.name(),
                this.fairLock,
                this.mixedTypes,
                this.lockWaitTime,
                this.sources);
    }

    /**
     * Make sure {@link #build()} is not called yet.
     * <p>
     * The method {@link #build()} may be called once and once only. This method
     * will ensure that.
     */
    @Synchronized
    private void ensure() {
        if (this.isConsumed.get())
            throw new IllegalStateException("this builder is already consumed.");
    }

}
