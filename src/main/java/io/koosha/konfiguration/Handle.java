package io.koosha.konfiguration;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;

@ThreadSafe
@Immutable
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public interface Handle {

    String id();

}
