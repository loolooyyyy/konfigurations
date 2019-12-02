package io.koosha.konfiguration;

import static io.koosha.konfiguration.KfgException.nameOf;
import static io.koosha.konfiguration.KfgException.toStringOf;

public class KfgIllegalStateException extends IllegalStateException {

    private final String source;
    private final String key;
    private final Q<?> neededType;
    private final String actualValue;

    public KfgIllegalStateException(final Konfiguration source,
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

    public KfgIllegalStateException(final Konfiguration source,
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

    public KfgIllegalStateException(final Konfiguration source,
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

    public KfgIllegalStateException(final Konfiguration source,
                                    final String key,
                                    final Q<?> neededType,
                                    final Object actualValue) {
        this.source = nameOf(source);
        this.key = key;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
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

}
