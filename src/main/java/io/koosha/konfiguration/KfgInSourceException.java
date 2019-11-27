package io.koosha.konfiguration;


/**
 * Exceptions regarding the source (backing storage), or as a wrapper around
 * exceptions thrown by the backing storage.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class KfgInSourceException extends KfgException {

    private final String source;

    public KfgInSourceException(final Konfiguration source) {
        super();
        this.source = nameOf(source);
    }

    public KfgInSourceException(final Konfiguration source,
                                String message) {
        super(message);
        this.source = nameOf(source);
    }

    public KfgInSourceException(final Konfiguration source,
                                final String message,
                                final Throwable cause) {
        super(message, cause);
        this.source = nameOf(source);
    }

    public KfgInSourceException(final Konfiguration source,
                                final Throwable cause) {
        super(cause);
        this.source = nameOf(source);
    }


    public final String getSource() {
        return source;
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

}
