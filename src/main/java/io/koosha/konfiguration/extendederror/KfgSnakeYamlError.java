package io.koosha.konfiguration.extendederror;

import io.koosha.konfiguration.KfgSourceException;

public class KfgSnakeYamlError extends KfgSourceException {

    public KfgSnakeYamlError(final String source,
                             final String message) {
        super(source, message);
    }

    public KfgSnakeYamlError(final String source,
                             final String message,
                             final Throwable cause) {
        super(source, message, cause);
    }

}
