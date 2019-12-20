package io.koosha.konfiguration;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

@ThreadSafe
public class KfgTypeNullException extends KfgTypeException {

    public KfgTypeNullException(@Nullable final String source,
                                @Nullable final String key,
                                @Nullable final Q<?> neededType) {
        super(source, key, neededType, null);
    }

}
