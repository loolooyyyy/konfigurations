package io.koosha.konfiguration.error;

import io.koosha.konfiguration.type.Q;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

@ThreadSafe
public class KfgTypeNullException extends KfgTypeException {

    public KfgTypeNullException(@Nullable final String source,
                                @Nullable final Q<?> neededType) {
        super(source, neededType, null);
    }

}
