package io.koosha.konfiguration.error.extended;

import io.koosha.konfiguration.error.KfgSourceException;

public class KfgJacksonError extends KfgSourceException {

    public KfgJacksonError(final String source,
                           final String message,
                           final Throwable cause) {
        super(source, message, cause);
    }

}
