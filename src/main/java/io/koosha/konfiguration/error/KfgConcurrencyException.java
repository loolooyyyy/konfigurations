package io.koosha.konfiguration.error;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

@ThreadSafe
public class KfgConcurrencyException extends KfgException {

    public KfgConcurrencyException(@Nullable String source,
                                   @Nullable String message) {
        super(source, null, null, message);
    }

    public KfgConcurrencyException(@Nullable String source,
                                   @Nullable String message,
                                   @Nullable Throwable cause) {
        super(source, null, null, message, cause);
    }

}
