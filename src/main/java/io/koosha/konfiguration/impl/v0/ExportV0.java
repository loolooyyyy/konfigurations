package io.koosha.konfiguration.impl.v0;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ThreadSafe
@Immutable
public final class ExportV0 {

    private ExportV0() {
    }

    @NotNull
    @Contract(pure = true)
    public static Factory factory() {
        return Factory.defaultInstance();
    }

    @NotNull
    @Contract(pure = true)
    public static String getVersion() {
        return "io.koosha.konfiguration:7.0.0";
    }

}
