package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.KonfigurationBuilder;
import io.koosha.konfiguration.KonfigurationManager;
import io.koosha.konfiguration.impl.base.KonfigurationBuilderBase;
import lombok.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@ThreadSafe
@ApiStatus.Internal
final class Kombiner_Builder extends KonfigurationBuilderBase implements KonfigurationBuilder {

    Kombiner_Builder(@NonNull @NotNull String name) {
        super(name);
    }

    @Override
    protected KonfigurationManager<?> build0(@NotNull @NonNull String name,
                                             boolean fairLock,
                                             @Nullable Long lockWaitTime,
                                             @NotNull @NonNull Collection<KonfigurationManager<?>> sources) {
        final Kombiner k = new Kombiner(name, sources, lockWaitTime, fairLock);
        return k.man();
    }

}
