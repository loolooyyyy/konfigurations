package io.koosha.konfiguration.error.extended;

import io.koosha.konfiguration.error.KfgSourceException;

public class KfgPreferencesError extends KfgSourceException {

    public KfgPreferencesError(final String source,
                               final String message) {
        super(source, message);
    }

}
