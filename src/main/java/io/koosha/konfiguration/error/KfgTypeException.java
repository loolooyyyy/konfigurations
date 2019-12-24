package io.koosha.konfiguration.error;

import io.koosha.konfiguration.type.Q;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

@ThreadSafe
public class KfgTypeException extends KfgException {

    public KfgTypeException(@Nullable final String source,
                            @Nullable final Q<?> neededType,
                            @Nullable final Object actualValue,
                            @Nullable final String message,
                            @Nullable final Throwable cause) {
        super(source, neededType, actualValue, message, cause);
    }

    public KfgTypeException(@Nullable final String source,
                            @Nullable final Q<?> neededType,
                            @Nullable final Object actualValue,
                            @Nullable final String message) {
        super(source, neededType, actualValue, message);
    }

    public KfgTypeException(@Nullable final String source,
                            @Nullable final Q<?> neededType,
                            @Nullable final Object actualValue,
                            @Nullable final Throwable e) {
        super(source, neededType, actualValue, e);
    }


    public KfgTypeException(@Nullable final String source,
                            @Nullable final Q<?> neededType,
                            @Nullable final Object actualValue) {
        super(source, neededType, actualValue);
    }

}
