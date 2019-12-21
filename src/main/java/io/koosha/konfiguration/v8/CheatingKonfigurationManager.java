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
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

/**
 * Cheats by always providing a reference to it's origin.
 */
@Accessors(fluent = true)
final class CheatingKonfigurationManager implements KonfigurationManager {

    @NotNull
    @Contract("_->new")
    static CheatingKonfigurationManager cheat(@NotNull @NonNull final KonfigurationManager origin) {
        if (origin instanceof CheatingKonfigurationManager)
            return (CheatingKonfigurationManager) origin;
        return new CheatingKonfigurationManager(origin);
    }

    @NotNull
    @Contract("_->new")
    static CheatingKonfigurationManager cheat(@NotNull @NonNull final Konfiguration origin) {
        return new CheatingKonfigurationManager(origin);
    }

    @NotNull
    @Contract("_->new")
    static CheatingKonfigurationManager cheat(@NotNull @NonNull final Source origin) {
        return new CheatingKonfigurationManager(origin);
    }

    // =========================================================================

    @NotNull
    @NonNull
    @Getter
    private final String name;

    @NonNull
    @NotNull
    @Getter(AccessLevel.PROTECTED)
    private final Object origin;

    @NonNull
    @NotNull
    @Getter(AccessLevel.PROTECTED)
    private final Source source;


    private CheatingKonfigurationManager(@NonNull @NotNull KonfigurationManager origin) {
        this.name = origin.name();
        this.origin = origin;
        this.source = requireNonNull(origin.getAndSetToNull());
    }

    private CheatingKonfigurationManager(@NonNull @NotNull Konfiguration origin) {
        this.name = origin.name();
        this.origin = origin;
        this.source = origin;
    }

    private CheatingKonfigurationManager(@NonNull @NotNull Source origin) {
        this.name = origin.name();
        this.origin = origin;
        this.source = origin;
    }


    @Contract("->fail")
    @Nullable
    @Override
    public Konfiguration getAndSetToNull() {
        throw new KfgAssertionException("shouldn't be called");
    }


    @Override
    public final boolean hasUpdate() {
        return this.origin instanceof UpdatableSource
                && ((UpdatableSource) this.origin).hasUpdate()
                || this.origin instanceof KonfigurationManager
                && ((KonfigurationManager) this.origin).hasUpdate();
    }

    @NotNull
    @Override
    public Map<String, Collection<Runnable>> update() {
        if (!(this.origin instanceof KonfigurationManager))
            return emptyMap();
        return ((KonfigurationManager) this.origin).update();
    }

    @NotNull
    public CheatingKonfigurationManager updated() {
        if (!(this.origin instanceof UpdatableSource))
            return this;
        return new CheatingKonfigurationManager(((UpdatableSource) this.origin).updatedSelf());
    }

    public Source toSource() {
        return this.source;
    }

}
