package io.koosha.konfiguration.error;

import io.koosha.konfiguration.type.Q;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

@ThreadSafe
public class KfgMissingKeyException extends KfgException {

    public KfgMissingKeyException(@Nullable final String source,
                                  @Nullable final Q<?> type) {
        super(source, type, null);
    }

}
