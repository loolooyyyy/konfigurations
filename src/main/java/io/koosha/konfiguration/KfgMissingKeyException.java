package io.koosha.konfiguration;


@SuppressWarnings({"unused", "WeakerAccess"})
public class KfgMissingKeyException extends KfgException {

    public KfgMissingKeyException(final Konfiguration source,
                                  final String key,
                                  final Q<?> neededType,
                                  final Object actualValue,
                                  final String message,
                                  final Throwable cause) {
        super(source, key, neededType, actualValue, message, cause);
    }

    public KfgMissingKeyException(final Konfiguration source,
                                  final String key,
                                  final Q<?> neededType,
                                  final Object actualValue,
                                  final String message) {
        super(source, key, neededType, actualValue, message);
    }

    public KfgMissingKeyException(final Konfiguration source,
                                  final String key,
                                  final Q<?> neededType,
                                  final Object actualValue,
                                  final Throwable cause) {
        super(source, key, neededType, actualValue, cause);
    }

    public KfgMissingKeyException(final Konfiguration source,
                                  final String key,
                                  final Q<?> neededType,
                                  final Object actualValue) {
        super(source, key, neededType, actualValue);
    }

}
