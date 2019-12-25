package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationManager;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.base.UpdatableSource;
import io.koosha.konfiguration.error.KfgAssertionException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * Cheats by always providing a reference to it's origin.
 */
@Accessors(fluent = true)
final class CheatingMan implements KonfigurationManager {

    @NotNull
    @NonNull
    @Getter
    private final String name;

    @Nullable
    @Getter(AccessLevel.PROTECTED)
    private final KonfigurationManager man;

    // =========================================================================
    @NonNull
    @NotNull
    @Getter(AccessLevel.PROTECTED)
    private final Source source;

    private CheatingMan(@NonNull @NotNull final KonfigurationManager origin) {
        this.name = origin.name();
        this.man = origin;
        this.source = requireNonNull(origin.getAndSetToNull());
    }

    private CheatingMan(@NonNull @NotNull final Source origin) {
        this.name = origin.name();
        this.man = null;
        this.source = origin;
    }

    @NotNull
    @Contract("_->new")
    static CheatingMan cheat(@NotNull @NonNull final KonfigurationManager origin) {
        if (origin instanceof CheatingMan)
            return (CheatingMan) origin;
        return new CheatingMan(origin);
    }

    @NotNull
    @Contract("_->new")
    static CheatingMan cheat(@NotNull @NonNull final Source origin) {
        return new CheatingMan(origin);
    }

    @Contract("->fail")
    @Nullable
    @Override
    public Konfiguration getAndSetToNull() {
        throw new KfgAssertionException("shouldn't be called");
    }


    @Override
    public boolean hasUpdate() {
        return this.source instanceof UpdatableSource
                && ((UpdatableSource) this.source).hasUpdate()
                || this.man != null && this.man.hasUpdate();
    }

    @NotNull
    @Override
    public Collection<Runnable> update() {
        return this.man == null
               ? emptyList()
               : this.man.update();
    }

    @NotNull
    public CheatingMan updated() {
        return this.source instanceof UpdatableSource
               ? new CheatingMan(((UpdatableSource) this.source).updatedSelf())
               : this;
    }

}
