package io.koosha.konfiguration.error;

import io.koosha.konfiguration.type.Q;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;

@ThreadSafe
public class KfgMissingKeyException extends KfgException {

    public KfgMissingKeyException(@Nullable final String source,
                                  @Nullable final Q<?> type) {
        super(source, type, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return format("KfgMissingKeyException[%s/%s]",
                this.source(),
                this.neededType()
        );
    }

}
