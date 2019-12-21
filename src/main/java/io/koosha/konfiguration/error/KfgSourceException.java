package io.koosha.konfiguration.error;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

/**
 * Exceptions regarding the source (backing storage), or as a wrapper around
 * exceptions thrown by the backing storage.
 */
@ThreadSafe
public class KfgSourceException extends KfgException {

    public KfgSourceException(@Nullable final String source,
                              @Nullable final String message) {
        super(source, null, null, null, message);
    }

    public KfgSourceException(@Nullable final String source,
                              @Nullable final String message,
                              @Nullable final Throwable cause) {
        super(source, null, null, null, message, cause);
    }

}
