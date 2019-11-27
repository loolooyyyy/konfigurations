package io.koosha.konfiguration;


@SuppressWarnings({"unused", "WeakerAccess"})
public class KfgMissingKeyException extends KfgInContextException {

    public KfgMissingKeyException(final Konfiguration source,
                                  final String key,
                                  final TypeName neededTypeName,
                                  final T<?> neededType,
                                  final String message,
                                  final Throwable cause) {
        super(source, key, neededTypeName, neededType, message, cause);
    }

    public KfgMissingKeyException(final Konfiguration source,
                                  final String key,
                                  final TypeName neededTypeName,
                                  final T<?> neededType,
                                  final String message) {
        super(source, key, neededTypeName, neededType, message);
    }

    public KfgMissingKeyException(final Konfiguration source,
                                  final String key,
                                  final TypeName neededTypeName,
                                  final T<?> neededType,
                                  final Throwable cause) {
        super(source, key, neededTypeName, neededType, cause);
    }

    public KfgMissingKeyException(final Konfiguration source,
                                  final String key,
                                  final TypeName neededTypeName,
                                  final T<?> neededType) {
        super(source, key, neededTypeName, neededType, null);
    }

    public KfgMissingKeyException(final String key) {
        this(null, key, null, null, null, null);
    }

}
