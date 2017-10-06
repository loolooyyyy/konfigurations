package cc.koosha.konfiguration;


public class KonfigurationBadTypeException extends KonfigurationException {

    public KonfigurationBadTypeException() {
    }

    public KonfigurationBadTypeException(String message) {
        super(message);
    }

    public KonfigurationBadTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public KonfigurationBadTypeException(Throwable cause) {
        super(cause);
    }


    public KonfigurationBadTypeException(final String required,
                                         final String actual,
                                         final String key) {

        this("is not " + required + ", is " + actual + ": " + key);
    }

}
