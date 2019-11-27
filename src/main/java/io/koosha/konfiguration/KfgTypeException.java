package io.koosha.konfiguration;


@SuppressWarnings({"WeakerAccess", "unused"})
public class KfgTypeException extends KfgInContextException {

    public KfgTypeException(final Konfiguration source,
                            final String key,
                            final TypeName neededTypeName,
                            final T<?> neededType,
                            final Object actualValue,
                            final String message,
                            final Throwable cause) {
        super(source, key, neededTypeName, neededType, actualValue, message, cause);
    }

    public KfgTypeException(final Konfiguration source,
                            final String key,
                            final TypeName neededTypeName,
                            final T<?> neededType,
                            final Object actualValue,
                            final String message) {
        super(source, key, neededTypeName, neededType, actualValue, message);
    }

    public KfgTypeException(final Konfiguration source,
                            final String key,
                            final TypeName neededTypeName,
                            final T<?> neededType,
                            final Object actualValue,
                            final Throwable cause) {
        super(source, key, neededTypeName, neededType, actualValue, cause);
    }

    public KfgTypeException(final Konfiguration source,
                            final String key,
                            final TypeName neededTypeName,
                            final T<?> neededType,
                            final Object actualValue) {
        super(source, key, neededTypeName, neededType, actualValue);
    }

}
