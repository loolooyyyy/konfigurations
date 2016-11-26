package cc.koosha.konfiguration;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.NoSuchElementException;


@Accessors(fluent = true)
@Getter
public class KonfigurationMissingKeyException extends NoSuchElementException {

    final String key;
    final String missingPart;

    public KonfigurationMissingKeyException() {

        this(null, null, null);
    }

    public KonfigurationMissingKeyException(String s) {

        this(s, null, null);
    }

    public KonfigurationMissingKeyException(String s, String key, String missingPart) {

        super(s);

        this.key = key;
        this.missingPart = missingPart;
    }

}
