package io.koosha.konfiguration;


@SuppressWarnings({"WeakerAccess", "unused"})
public class KfgTypeNullException extends KfgTypeException {

    public KfgTypeNullException(final Konfiguration source,
                                final String key,
                                final Q<?> neededType,
                                final String message,
                                final Throwable cause) {
        super(source, key, neededType, null, message, cause);
    }

    public KfgTypeNullException(final Konfiguration source,
                                final String key,
                                final Q<?> neededType,
                                final String message) {
        super(source, key, neededType, null, message);
    }

    public KfgTypeNullException(final Konfiguration source,
                                final String key,
                                final Q<?> neededType,
                                final Throwable cause) {
        super(source, key, neededType, null, cause);
    }

    public KfgTypeNullException(final Konfiguration source,
                                final String key,
                                final Q<?> neededType) {
        super(source, key, neededType, null);
    }

}
