package io.koosha.konfiguration.impl.base;

import io.koosha.konfiguration.Faktory;
import io.koosha.konfiguration.KfgIllegalStateException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.experimental.Accessors;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
@NotThreadSafe
@Accessors(fluent = true)
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public class KonfigurationManagerBase<F extends Konfiguration> implements KonfigurationManager<F> {

    @NotNull
    @NonNull
    @Getter
    private final String name;

    @NonNull
    @NotNull
    @Getter(AccessLevel.PROTECTED)
    private final F origin;

    @Nullable
    @Getter(AccessLevel.PROTECTED)
    private final KonfigurationManager<F> manager;

    private final AtomicBoolean consumed = new AtomicBoolean(false);


    public KonfigurationManagerBase(@NonNull @NotNull final F origin) {
        this.name = origin.name();
        this.origin = origin;
        this.manager = null;
    }

    public KonfigurationManagerBase(@NonNull @NotNull final KonfigurationManager<F> origin) {
        this.name = origin.name();
        this.origin = requireNonNull(origin.getAndSetToNull());
        this.manager = origin;
    }


    @Override
    @Synchronized
    public final F getAndSetToNull() {
        if (consumed.get())
            throw new KfgIllegalStateException(this.name, "source already taken out");
        this.consumed.set(true);
        return this.origin;
    }

    @Contract("->false")
    @Override
    public final boolean hasUpdate() {
        return this.manager != null && this.manager.hasUpdate();
    }

    @NotNull
    @Override
    public final Map<String, Collection<Runnable>> update() {
        return this.manager == null ? emptyMap() : this.manager.update();
    }

}
