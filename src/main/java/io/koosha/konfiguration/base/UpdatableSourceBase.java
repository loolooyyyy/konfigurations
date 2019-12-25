package io.koosha.konfiguration.base;

import io.koosha.konfiguration.Faktory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@ThreadSafe
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public abstract class UpdatableSourceBase extends SourceBase implements UpdatableSource {

}
