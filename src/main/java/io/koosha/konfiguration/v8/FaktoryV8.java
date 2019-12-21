package io.koosha.konfiguration.v8;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.koosha.konfiguration.Faktory;
import io.koosha.konfiguration.KonfigurationBuilder;
import io.koosha.konfiguration.KonfigurationManager;
import io.koosha.konfiguration.base.Deserializer;
import io.koosha.konfiguration.base.UpdatableSource;
import lombok.NonNull;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import static java.util.Collections.singleton;

@ThreadSafe
@Immutable
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public final class FaktoryV8 implements Faktory {

    private FaktoryV8() {
    }

    private static final Faktory INSTANCE = new FaktoryV8();

    private static final String VERSION = "io.koosha.konfiguration:7.0.0";

    @Contract(pure = true)
    @NotNull
    public static Faktory defaultInstance() {
        return FaktoryV8.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    @NotNull
    public String getVersion() {
        return VERSION;
    }

    // ================================================================ KOMBINER

    @Override
    @Contract("_ ->new")
    @NotNull
    public KonfigurationBuilder builder(@NotNull @NonNull final String name) {
        return new Kombiner_Builder(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public KonfigurationManager kombine(@NotNull @NonNull final String name,
                                        @NonNull @NotNull final Collection<KonfigurationManager> sources) {
        return new Kombiner(name, sources, LOCK_WAIT_MILLIS__DEFAULT, true).man();
    }

    @NotNull
    @Contract("_, _ -> new")
    private KonfigurationManager kombine(@NotNull @NonNull final String name,
                                         @NotNull @NonNull final UpdatableSource source) {
        return new Kombiner(
                name,
                singleton(CheatingKonfigurationManager.cheat(source)),
                LOCK_WAIT_MILLIS__DEFAULT,
                true).man();
    }

    // ==================================================================== MAP

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    public KonfigurationManager map(@NotNull @NonNull final String name,
                                    @NotNull @NonNull final Supplier<Map<String, ?>> storage) {
        return kombine(name, new ExtMapSource(name, storage, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract(value = "_, _ -> new",
            pure = true)
    public KonfigurationManager mapWithNested(@NotNull @NonNull final String name,
                                              @NotNull @NonNull final Supplier<Map<String, ?>> storage) {
        return kombine(name, new ExtMapSource(name, storage, true));
    }

    // ============================================================ PREFERENCES

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public KonfigurationManager preferences(@NotNull @NonNull final String name,
                                            @NotNull @NonNull final Preferences storage) {
        return kombine(name, new ExtPreferencesSource(name, storage, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public KonfigurationManager preferences(@NotNull @NonNull final String name,
                                            @NotNull @NonNull final Preferences storage,
                                            @NotNull @NonNull final Deserializer deser) {
        return kombine(name, new ExtPreferencesSource(name, storage, deser));
    }


    // ================================================================ JACKSON

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _ -> new")
    public KonfigurationManager jacksonJson(@NotNull @NonNull final String name,
                                            @NotNull @NonNull final Supplier<String> json) {
        final ObjectMapper mapper = ExtJacksonJsonSource.defaultJacksonObjectMapper();
        return jacksonJson(name, json, () -> mapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public KonfigurationManager jacksonJson(@NotNull @NonNull final String name,
                                            @NotNull @NonNull final Supplier<String> json,
                                            @NonNull @NotNull final Supplier<ObjectMapper> objectMapper) {
        return kombine(name, new ExtJacksonJsonSource(name, json, objectMapper));
    }

    // ============================================================= SNAKE YAML

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @ApiStatus.Experimental
    @Contract("_, _ -> new")
    public KonfigurationManager snakeYaml(@NotNull @NonNull final String name,
                                          @NotNull @NonNull final Supplier<String> yaml) {
        ExtYamlSource.ensureDep(name);
        return kombine(name,
                new ExtYamlSource(name, yaml, () -> ExtYamlSource.getDefaultYamlSupplier(name), false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @ApiStatus.Experimental
    @Contract("_, _, _ -> new")
    public KonfigurationManager snakeYaml(@NotNull @NonNull final String name,
                                          @NotNull @NonNull final Supplier<String> yaml,
                                          @NonNull @NotNull final Supplier<Yaml> objectMapper) {
        ExtYamlSource.ensureDep(name);
        return kombine(name, new ExtYamlSource(name, yaml, objectMapper, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public KonfigurationManager snakeYaml_safe(@NotNull @NonNull final String name,
                                               @NotNull @NonNull final Supplier<String> yaml,
                                               @NonNull @NotNull final Supplier<Yaml> objectMapper) {
        ExtYamlSource.ensureDep(name);
        return kombine(name, new ExtYamlSource(name, yaml, objectMapper, false));
    }

}
