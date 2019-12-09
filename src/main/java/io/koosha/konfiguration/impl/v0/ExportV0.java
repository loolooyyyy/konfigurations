package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.Factory;
import lombok.experimental.UtilityClass;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ThreadSafe
@SuppressWarnings("unused")
@Immutable
@UtilityClass
public final class ExportV0 {

    static final String DEFAULT_KONFIG_NAME = "default_konfig";

    @NotNull
    @Contract(pure = true)
    public static String defaultKonfigName() {
        return DEFAULT_KONFIG_NAME;
    }

    @NotNull
    @Contract(pure = true)
    public static Factory factory() {
        return FactoryImpl.defaultInstance();
    }

    @NotNull
    @Contract(pure = true)
    public static String getVersion() {
        return factory().getVersion();
    }

}
