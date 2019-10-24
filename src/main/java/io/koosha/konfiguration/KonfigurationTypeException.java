package io.koosha.konfiguration;


@SuppressWarnings({"unused"})
public class KonfigurationTypeException extends KonfigurationException {

    public KonfigurationTypeException() {
    }

    public KonfigurationTypeException(String message) {
        super(message);
    }

    public KonfigurationTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public KonfigurationTypeException(Throwable cause) {
        super(cause);
    }

    public KonfigurationTypeException(final String required, final String actual, final String key) {

        this("required type=" + required + ", but found=" + actual + " for key=" + key);
    }

}
