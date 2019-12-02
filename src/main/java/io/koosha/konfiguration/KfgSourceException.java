package io.koosha.konfiguration;


/**
 * Exceptions regarding the source (backing storage), or as a wrapper around
 * exceptions thrown by the backing storage.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class KfgSourceException extends KfgException {

    public KfgSourceException(final Konfiguration source,
                              final String key,
                              final Q<?> neededType,
                              final Object actualValue,
                              final String message,
                              final Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
    }

    public KfgSourceException(final Konfiguration source,
                              final String key,
                              final Q<?> neededType,
                              final Object actualValue,
                              final String message) {
        super(source, key, neededType, actualValue, message);
    }

    public KfgSourceException(final Konfiguration source,
                              final String key,
                              final Q<?> neededType,
                              final Object actualValue,
                              final Throwable cause) {
        super(source, key, neededType, actualValue, cause);
    }

    public KfgSourceException(final Konfiguration source,
                              final String key,
                              final Q<?> neededType,
                              final Object actualValue) {
        super(source, key, neededType, actualValue);
    }

}
