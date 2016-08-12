package cc.koosha.konfigurations.core.impl;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;


public class JsonKonfigurationException extends JsonProcessingException {

    public JsonKonfigurationException(final String msg, final JsonLocation loc, final Throwable rootCause) {
        super(msg, loc, rootCause);
    }

    public JsonKonfigurationException(final String msg) {
        super(msg);
    }

    public JsonKonfigurationException(final String msg, final JsonLocation loc) {
        super(msg, loc);
    }

    public JsonKonfigurationException(final String msg, final Throwable rootCause) {
        super(msg, rootCause);
    }

    public JsonKonfigurationException(final Throwable rootCause) {
        super(rootCause);
    }

}
