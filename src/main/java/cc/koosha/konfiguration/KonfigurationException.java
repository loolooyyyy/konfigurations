package cc.koosha.konfiguration;


@SuppressWarnings("WeakerAccess")
public class KonfigurationException extends RuntimeException {

    public KonfigurationException() {
    }

    public KonfigurationException(String message) {
        super(message);
    }

    public KonfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public KonfigurationException(Throwable cause) {
        super(cause);
    }

}
