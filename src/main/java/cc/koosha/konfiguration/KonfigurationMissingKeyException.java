package cc.koosha.konfiguration;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.NoSuchElementException;


@Accessors(fluent = true)
@Getter
public class KonfigurationMissingKeyException extends NoSuchElementException {

    final String key;

    public KonfigurationMissingKeyException(final String s) {

        super(s);

        this.key = s;
    }

}
