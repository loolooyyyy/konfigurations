package io.koosha.konfiguration.error;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true)
@Getter
@ThreadSafe
public class KfgUnsupportedOperationException extends UnsupportedOperationException {

    @Nullable
    private final String source;

    public KfgUnsupportedOperationException(@Nullable final String source,
                                            final String message,
                                            final Throwable cause) {
        super(message, cause);
        this.source = source;
    }

    public KfgUnsupportedOperationException(@Nullable final String source,
                                            final String message) {
        super(message);
        this.source = source;
    }
}
