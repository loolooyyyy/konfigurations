package cc.koosha.konfigurations.core;


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

}
