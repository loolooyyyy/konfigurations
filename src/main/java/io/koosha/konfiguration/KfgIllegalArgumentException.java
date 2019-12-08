package io.koosha.konfiguration;

import lombok.Getter;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import static io.koosha.konfiguration.KfgException.nameOf;

@Getter
@ThreadSafe
public class KfgIllegalArgumentException extends IllegalArgumentException {

    @Nullable
    private final String source;

    public KfgIllegalArgumentException(@Nullable final Konfiguration source,
                                       final String message) {
        super(message);
        this.source = nameOf(source);
    }

}
