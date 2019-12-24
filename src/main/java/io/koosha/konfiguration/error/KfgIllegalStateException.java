package io.koosha.konfiguration.error;

import io.koosha.konfiguration.type.Q;
import lombok.Getter;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import static io.koosha.konfiguration.error.KfgException.toStringOf;

@Getter
@ThreadSafe
public class KfgIllegalStateException extends IllegalStateException {

    @Nullable
    private final String source;

    @Nullable
    private final Q<?> neededType;

    @Nullable
    private final String actualValue;

    public KfgIllegalStateException(@Nullable final String source,
                                    final String message) {
        this(source, null, null, message);
    }

    public KfgIllegalStateException(@Nullable final String source,
                                    @Nullable final Q<?> neededType,
                                    @Nullable final Object actualValue,
                                    final String message) {
        super(message);
        this.source = source;
        this.neededType = neededType;
        this.actualValue = toStringOf(actualValue);
    }
}
