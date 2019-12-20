package io.koosha.konfiguration.extendederror;

import io.koosha.konfiguration.KfgSourceException;

public class KfgPreferencesError extends KfgSourceException {

    public KfgPreferencesError(final String source,
                               final String message) {
        super(source, message);
    }

}
