package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.base.UpdatableSource;
import io.koosha.konfiguration.base.UpdatableSourceBase;
import io.koosha.konfiguration.error.KfgAssertionException;
import io.koosha.konfiguration.error.KfgTypeException;
import io.koosha.konfiguration.error.KfgUnsupportedOperationException;
import io.koosha.konfiguration.error.extended.KfgSnakeYamlAssertionError;
import io.koosha.konfiguration.error.extended.KfgSnakeYamlError;
import io.koosha.konfiguration.type.Q;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.beans.ConstructorProperties;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Reads konfig from a yaml source (supplied as string).
 *
 * <p>for {@link #custom(Q)} to work, the supplied yaml reader must be
 * configured to handle arbitrary types accordingly.
 *
 * <p>Thread safe and immutable.
 */
@ApiStatus.Internal
@Immutable
@ThreadSafe
final class ExtYamlSource extends UpdatableSourceBase {

    private static final Pattern DOT = Pattern.compile(Pattern.quote("."));

    private static final ThreadLocal<Yaml> defaultYamlSupplier = new ThreadLocal<>();

    private final boolean safe;

    private final Supplier<Yaml> mapper;

    private final Supplier<String> yaml;

    private final Map<String, ?> root;

    @NonNull
    @NotNull
    @Getter
    @Accessors(fluent = true)
    private final String name;

    private final int lastHash;

    /**
     * Creates an instance with the given Yaml parser.
     *
     * @param yaml   backing store provider. Must always return a non-null valid yaml
     *               string.
     * @param mapper {@link Yaml} provider. Must always return a valid non-null Yaml,
     *               and if required, it must be able to deserialize custom types, so
     *               that {@link #custom(Q)} works as well.
     * @throws NullPointerException if any of its arguments are null.
     * @throws KfgSnakeYamlError    if org.yaml.snakeyaml library is not in the classpath. it
     *                              specifically looks for the class: "org.yaml.snakeyaml"
     * @throws KfgSnakeYamlError    if the storage (yaml string) returned by yaml string is null.
     */
    ExtYamlSource(@NotNull @NonNull final String name,
                  @NotNull @NonNull final Supplier<String> yaml,
                  @NotNull @NonNull final Supplier<Yaml> mapper,
                  final boolean safe) {
        this.name = name;
        this.yaml = yaml;
        this.mapper = mapper;
        this.safe = safe;

        ensureDep(name);

        // Check early, so we 're not fooled with a dummy object reader.
        try {
            Class.forName("org.yaml.snakeyaml.Yaml");
        }
        catch (final ClassNotFoundException e) {
            throw new KfgSnakeYamlError(this.name(),
                    "org.yaml.snakeyaml library is required to be" +
                            " present in the class path, can not find the" +
                            " class: org.yaml.snakeyaml.Yaml", e);
        }

        final String newYaml = this.yaml.get();
        requireNonNull(newYaml, "supplied storage is null");
        this.lastHash = newYaml.hashCode();

        final Yaml newMapper = mapper.get();
        requireNonNull(newMapper, "supplied mapper is null");
        this.root = Collections.unmodifiableMap(newMapper.load(newYaml));
    }

    static Yaml getDefaultYamlSupplier() {
        ensureDep(Thread.currentThread().getName());
        Yaml y = defaultYamlSupplier.get();
        if (y == null) {
            y = new Yaml(new ExtYamlSourceByConstructorConstructor<>(
                    (Class<? extends ConstructorProperties>) ConstructorProperties.class,
                    (Function<? super ConstructorProperties, String[]>) ConstructorProperties::value
            ));
            defaultYamlSupplier.set(y);
        }
        return y;
    }

    /**
     * Make sure needed classes are on path: {@linkplain org.yaml.snakeyaml.Yaml}.
     *
     * @param source name of konfig source asking for Yaml.
     */
    static void ensureDep(@Nullable final String source) {
        final String klass = "org.yaml.snakeyaml.Yaml";
        try {
            Class.forName(klass);
        }
        catch (final ClassNotFoundException e) {
            throw new KfgUnsupportedOperationException(source,
                    "snakeyaml library missing: " + klass, e);
        }
    }

    @Contract(pure = true)
    @Override
    public boolean hasUpdate() {
        final String newYaml = this.yaml.get();
        return newYaml != null && newYaml.hashCode() != this.lastHash;
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @Override
    public UpdatableSource updatedSelf() {
        return this.hasUpdate()
               ? new ExtYamlSource(this.name(), this.yaml, this.mapper, this.safe)
               : this;
    }

    @Override
    protected boolean isNull(@NonNull @NotNull final Q<?> key) {
        try {
            return this.get(key.key()) == null;
        }
        catch (final KfgSnakeYamlAssertionError e) {
            return false;
        }
    }

    @Override
    public boolean has(@NotNull @NonNull final Q<?> key) {
        try {
            final Object o = this.get(key.key());
            if (key.matchesValue(o))
                return true;
            if (key.isSet() && List.class.isAssignableFrom(o.getClass()) &&
                    new HashSet<>((Collection<?>) o).size() == ((Collection<?>) o).size())
                return true;
            if (key.isNumber() && Number.class.isAssignableFrom(o.getClass())) {
                if (key.isFloat())
                    return ((Number) o).doubleValue() >= Float.MIN_VALUE
                            && ((Number) o).doubleValue() <= Float.MAX_VALUE;
                if (key.isDouble())
                    return true;

                // No coercing floating to int/long.
                if (o instanceof Double || o instanceof Float)
                    return false;

                final long min;
                final long max;
                if (key.isByte()) {
                    min = Byte.MIN_VALUE;
                    max = Byte.MIN_VALUE;
                }
                else if (key.isShort()) {
                    min = Short.MIN_VALUE;
                    max = Short.MIN_VALUE;
                }
                else if (key.isInt()) {
                    min = Integer.MIN_VALUE;
                    max = Integer.MIN_VALUE;
                }
                else if (key.isLong()) {
                    return true;
                }
                else {
                    throw new KfgAssertionException("expecting a number");
                }
                return ((Number) o).longValue() <= max && min <= ((Number) o).longValue();
            }
            return false;
        }
        catch (final KfgSnakeYamlAssertionError e) {
            return false;
        }
    }

    private Object get(@NotNull @NonNull final CharSequence key) {
        Map<?, ?> node = this.root;
        final String[] split = DOT.split(key);
        for (int i = 0; i < split.length; i++) {
            final String keyPart = split[i];
            final Object nd = node.get(keyPart);
            final boolean isLast = i == split.length - 1;

            if (isLast)
                return nd;
            if (!(nd instanceof Map))
                throw new KfgSnakeYamlAssertionError(this.name(), "assertion error");
            node = (Map<?, ?>) nd;
        }
        throw new KfgSnakeYamlAssertionError(this.name(), "assertion error");
    }

    private void ensureSafe(@Nullable final Q<?> type) {
        if (this.safe && type != null && !type.args().isEmpty())
            throw new KfgSnakeYamlError(this.name, "yaml does not support parameterized yet.");
    }

    @Override
    @NotNull
    protected Object bool0(@NotNull @NonNull final String key) {
        return this.get(key);
    }

    @Override
    @NotNull
    protected Object char0(@NotNull @NonNull final String key) {
        return this.get(key);
    }

    @Override
    @NotNull
    protected Object string0(@NotNull @NonNull final String key) {
        return this.get(key);
    }

    @Override
    @NotNull
    protected Number number0(@NotNull @NonNull final String key) {
        return (Number) this.get(key);
    }

    @Override
    @NotNull
    protected Number numberDouble0(@NotNull @NonNull final String key) {
        return (Number) this.get(key);
    }

    @Override
    @NotNull
    protected List<?> list0(@NotNull @NonNull final Q<? extends List<?>> type) {
        this.ensureSafe(type);

        final Object g = this.get(type.key());
        final Yaml mapper = this.mapper.get();
        final String yamlAgain = mapper.dump(g);
        return mapper.loadAs(yamlAgain, type.klass());
    }

    @Override
    @NotNull
    protected Set<?> set0(@NotNull @NonNull final Q<? extends Set<?>> key) {
        this.ensureSafe(key);

        final Object g = this.get(key.key());
        final Yaml mapper = this.mapper.get();
        final String yamlAgain = mapper.dump(g);
        return mapper.loadAs(yamlAgain, key.klass());
    }

    @Override
    @NotNull
    protected Map<?, ?> map0(@NotNull @NonNull final Q<? extends Map<?, ?>> key) {
        this.ensureSafe(key);

        final Object g = this.get(key.key());
        final Yaml mapper = this.mapper.get();
        final String yamlAgain = mapper.dump(g);
        return mapper.loadAs(yamlAgain, key.klass());
    }

    @Override
    @NotNull
    protected Object custom0(@NotNull @NonNull final Q<?> key) {
        this.ensureSafe(key);

        final Object g = this.get(key.key());
        final Yaml mapper = this.mapper.get();
        final String yamlAgain = mapper.dump(g);
        try {
            return mapper.loadAs(yamlAgain, key.klass());
        }
        catch (final YAMLException e) {
            throw new KfgTypeException(this.name, key, null, e);
        }
    }

}
