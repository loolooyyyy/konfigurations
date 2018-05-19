package cc.koosha.konfiguration;


/**
 * Exceptions regarding the source (backing storage), or as a wrapper around
 * exceptions thrown by the backing storage.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class KonfigurationSourceException extends KonfigurationException {

    public KonfigurationSourceException() {
        super();
    }

    public KonfigurationSourceException(String message) {
        super(message);
    }

    public KonfigurationSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public KonfigurationSourceException(Throwable cause) {
        super(cause);
    }

}
