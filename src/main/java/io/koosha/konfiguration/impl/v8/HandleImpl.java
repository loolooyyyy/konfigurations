package io.koosha.konfiguration.impl.v8;

import io.koosha.konfiguration.Handle;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.atomic.AtomicLong;

@ThreadSafe
@Immutable
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
@ApiStatus.Internal
final class HandleImpl implements Handle {

    private static final AtomicLong id_pool = new AtomicLong(Long.MAX_VALUE);

    private final long id;

    HandleImpl() {
        final long id = id_pool.incrementAndGet();
        if (id == Long.MAX_VALUE)
            throw new RuntimeException("id pool overflow");
        this.id = id;
    }

}
