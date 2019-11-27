package io.koosha.konfiguration;


import static java.lang.String.format;

@SuppressWarnings("WeakerAccess")
public class KfgException extends RuntimeException {

    public KfgException() {
    }

    public KfgException(String message) {
        super(message);
    }

    public KfgException(String message, Throwable cause) {
        super(message, cause);
    }

    public KfgException(Throwable cause) {
        super(cause);
    }


    static String msgOf(final Throwable t, final String context) {
        return "[" + context + "]->" + msgOf(t);
    }

    static String msgOf(final Throwable t) {
        if (t == null)
            return "[null exception]->[null exception]";
        return format("[throwable::%s]->[%s]", t.getClass().getName(), t.getMessage());
    }


/*
    private static final String PREFIX = "io.koosha.konfiguration.";
    private static final String SUFFIX = "Exception";

    /**
     * Pop the first element, which is the static factory method in this class.
     *
     * @param stackTrace see {@link super#setStackTrace(StackTraceElement[])}.
     * /
    @SuppressWarnings("ConstantConditions")
    @Override
    public final void setStackTrace(StackTraceElement[] stackTrace) {
        if (stackTrace != null
                && stackTrace.length > 0
                && stackTrace[0] != null
                && stackTrace[0].getClassName() != null
                && stackTrace[0].getClassName().startsWith(PREFIX)
                && stackTrace[0].getClassName().endsWith(SUFFIX)) {
            final StackTraceElement[] popped = new StackTraceElement[stackTrace.length];
            System.arraycopy(stackTrace, 1, popped, 0, popped.length);
            stackTrace = popped;
        }
        super.setStackTrace(stackTrace);
    }

*/

}
