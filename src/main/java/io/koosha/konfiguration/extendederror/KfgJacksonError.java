package io.koosha.konfiguration.extendederror;

import io.koosha.konfiguration.KfgSourceException;

public class KfgJacksonError extends KfgSourceException {

    public KfgJacksonError(final String source,
                           final String message,
                           final Throwable cause) {
        super(source, message, cause);
    }

}
