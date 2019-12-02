package io.koosha.konfiguration;


@SuppressWarnings({"WeakerAccess", "unused"})
public class KfgTypeException extends KfgException {

    public KfgTypeException(final Konfiguration source,
                            final String key,
                            final Q<?> neededType,
                            final Object actualValue,
                            final String message,
                            final Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
    }

    public KfgTypeException(final Konfiguration source,
                            final String key,
                            final Q<?> neededType,
                            final Object actualValue,
                            final String message) {
        super(source, key, neededType, actualValue, message);
    }

    public KfgTypeException(final Konfiguration source,
                            final String key,
                            final Q<?> neededType,
                            final Object actualValue,
                            final Throwable cause) {
        super(source, key, neededType, actualValue, cause);
    }

    public KfgTypeException(final Konfiguration source,
                            final String key,
                            final Q<?> neededType,
                            final Object actualValue) {
        super(source, key, neededType, actualValue);
    }

}
