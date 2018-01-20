package cc.koosha.konfiguration;

import java.util.NoSuchElementException;


public class KonfigurationMissingKeyException extends NoSuchElementException {

    private final String key;

    public KonfigurationMissingKeyException(final String s) {

        super(s);

        this.key = s;
    }

    public String getKey() {
        return this.key;
    }

}
