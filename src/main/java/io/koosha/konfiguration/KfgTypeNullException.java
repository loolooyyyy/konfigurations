package io.koosha.konfiguration;

@SuppressWarnings({"WeakerAccess", "unused"})
public class KfgTypeNullException extends KfgTypeException {

    public KfgTypeNullException(final Konfiguration source,
                                final String key,
                                final TypeName neededTypeName,
                                final T<?> neededType,
                                final String message,
                                final Throwable cause) {
        super(source, key, neededTypeName, neededType, null, message, cause);
    }

    public KfgTypeNullException(final Konfiguration source,
                                final String key,
                                final TypeName neededTypeName,
                                final T<?> neededType,
                                final String message) {
        super(source, key, neededTypeName, neededType, null, message);
    }

    public KfgTypeNullException(final Konfiguration source,
                                final String key,
                                final TypeName neededTypeName,
                                final T<?> neededType,
                                final Throwable cause) {
        super(source, key, neededTypeName, neededType, null, cause);
    }

    public KfgTypeNullException(final Konfiguration source,
                                final String key,
                                final TypeName neededTypeName,
                                final T<?> neededType) {
        super(source, key, neededTypeName, neededType, null);
    }

}
