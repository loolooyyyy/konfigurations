package io.koosha.konfiguration;

import java.util.Objects;

import static java.lang.String.format;

@SuppressWarnings({"unused", "WeakerAccess"})
public class KfgInContextException extends KfgInSourceException {

    private final String key;
    private final TypeName neededTypeName;
    private final T<?> neededType;
    private final String actualValue;


    public KfgInContextException(final Konfiguration source,
                                 final String key,
                                 final TypeName neededTypeName,
                                 final T<?> neededType,
                                 final Object actualValue,
                                 final String message,
                                 final Throwable cause) {
        super(source, message, cause);
        this.key = key;
        this.neededTypeName = neededTypeName;
        this.neededType = neededType;
        this.actualValue = represent(actualValue);
    }

    public KfgInContextException(final Konfiguration source,
                                 final String key,
                                 final TypeName neededTypeName,
                                 final T<?> neededType,
                                 final Object actualValue,
                                 final String message) {
        super(source, message);
        this.key = key;
        this.neededTypeName = neededTypeName;
        this.neededType = neededType;
        this.actualValue = represent(actualValue);
    }

    public KfgInContextException(final Konfiguration source,
                                 final String key,
                                 final TypeName neededTypeName,
                                 final T<?> neededType,
                                 final Object actualValue,
                                 final Throwable cause) {
        super(source, cause);
        this.key = key;
        this.neededTypeName = neededTypeName;
        this.neededType = neededType;
        this.actualValue = represent(actualValue);
    }

    public KfgInContextException(final Konfiguration source,
                                 final String key,
                                 final TypeName neededTypeName,
                                 final T<?> neededType,
                                 final Object actualValue) {
        super(source);
        this.key = key;
        this.neededTypeName = neededTypeName;
        this.neededType = neededType;
        this.actualValue = represent(actualValue);
    }


    public final String getKey() {
        return key;
    }

    public final TypeName getNeededTypeName() {
        return neededTypeName;
    }

    public final T<?> getNeededType() {
        return neededType;
    }

    public final String getActualValue() {
        return actualValue;
    }


    private static String represent(Object value) {
        String representationC;
        try {
            representationC = value == null ? "null" : value.getClass().getName();
        }
        catch (Throwable t) {
            representationC = msgOf(t, "value.getClass().getName()");
        }

        String representationV;
        try {
            representationV = Objects.toString(value);
        }
        catch (Throwable t) {
            representationV = msgOf(t, "Objects.toString(value)");
        }

        return format("[%s]:[%s]", representationC, representationV);
    }

}
