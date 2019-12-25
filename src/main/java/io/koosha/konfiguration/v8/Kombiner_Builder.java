package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationBuilder;
import io.koosha.konfiguration.KonfigurationManager;
import io.koosha.konfiguration.base.KonfigurationBuilderBase;
import io.koosha.konfiguration.error.KfgUnsupportedOperationException;
import lombok.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@ThreadSafe
@ApiStatus.Internal
final class Kombiner_Builder extends KonfigurationBuilderBase implements KonfigurationBuilder {

    Kombiner_Builder(@NonNull @NotNull final String name) {
        super(name);
    }

    @Override
    protected KonfigurationManager build0(@NotNull @NonNull final String name,
                                          final boolean fairLock,
                                          final boolean mixedTypes,
                                          @Nullable final Long lockWaitTime,
                                          @NotNull @NonNull final Collection<KonfigurationManager> sources) {
        final Kombiner kombiner = new Kombiner(name, sources, lockWaitTime, fairLock, mixedTypes);
        return kombiner.man();
    }

    @Override
    public @NotNull KonfigurationBuilder add_(@NotNull @NonNull final Collection<Konfiguration> konfig) {
        throw new KfgUnsupportedOperationException(this.name(), "TODO");
    }
}
