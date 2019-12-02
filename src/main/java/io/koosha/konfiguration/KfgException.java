package io.koosha.konfiguration;


import java.util.Objects;

import static java.lang.String.format;


@SuppressWarnings("WeakerAccess")
public class KfgException extends RuntimeException {

    private final String source;
    private final String key;
    private final Q<?> neededType;
    private final String actualValue;

    public KfgException(final Konfiguration source,
                        final String key,
                        final Q<?> neededType,
                        final Object actualValue,
                        final String message,
                        final Throwable cause) {
        super(message, cause);
        this.source = nameOf(source);
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }

    public KfgException(final Konfiguration source,
                        final String key,
                        final Q<?> neededType,
                        final Object actualValue,
                        String message) {
        super(message);
        this.source = nameOf(source);
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }

    public KfgException(final Konfiguration source,
                        final String key,
                        final Q<?> neededType,
                        final Object actualValue,
                        Throwable cause) {
        super(cause);
        this.source = nameOf(source);
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }

    public KfgException(final Konfiguration source,
                        final String key,
                        final Q<?> neededType,
                        final Object actualValue) {
        this.source = nameOf(source);
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return format("%s[key=%s,  neededType=%s, actualValue=%s]",
                this.getClass().getName(),
                this.getKey(),
                this.getNeededType(),
                this.getActualValue());
    }


    public String getSource() {
        return source;
    }

    public String getKey() {
        return key;
    }

    public Q<?> getNeededType() {
        return neededType;
    }

    public String getActualValue() {
        return actualValue;
    }


    static String nameOf(final Konfiguration k) {
        if (k == null)
            return null;
        try {
            return k.getName();
        }
        catch (Throwable t) {
            return "[Konfiguration.getName()]->" + msgOf(t);
        }
    }

    static String msgOf(final Throwable t) {
        if (t == null)
            return "[null exception]->[null exception]";
        return format("[throwable::%s]->[%s]", t.getClass().getName(), t.getMessage());
    }

    static String toStringOf(final Object value) {
        String representationC;
        try {
            representationC = value == null ? "null" : value.getClass().getName();
        }
        catch (Throwable t) {
            representationC = "[" + "value.getClass().getName()" + "]->" + msgOf(t);
        }

        String representationV;
        try {
            representationV = Objects.toString(value);
        }
        catch (Throwable t) {
            representationV = "[" + "Objects.toString(value)" + "]->" + msgOf(t);
        }

        return format("[%s]:[%s]", representationC, representationV);
    }

}
