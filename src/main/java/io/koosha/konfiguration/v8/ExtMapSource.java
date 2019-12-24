package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.base.UpdatableSource;
import io.koosha.konfiguration.base.UpdatableSourceBase;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import io.koosha.konfiguration.error.KfgMissingKeyException;
import io.koosha.konfiguration.error.KfgTypeException;
import io.koosha.konfiguration.type.Q;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@ThreadSafe
@ApiStatus.Internal
final class ExtMapSource extends UpdatableSourceBase {

    private static final Pattern DOT = Pattern.compile(Pattern.quote("."));

    private final Supplier<Map<String, ?>> map;
    private final Map<String, ?> root;
    private final int lastHash;
    private final boolean enableNestedMap;

    @NonNull
    @NotNull
    @Accessors(fluent = true)
    @Getter
    private final String name;


    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    public boolean hasUpdate() {
        final Map<String, ?> newMap = map.get();
        if (newMap == null)
            return false;
        final int newHash = newMap.hashCode();
        return newHash != lastHash;
    }

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true,
            value = "-> new")
    @NotNull
    @Override
    public UpdatableSource updatedSelf() {
        return this.hasUpdate()
               ? new ExtMapSource(name(), map, enableNestedMap)
               : this;
    }


    @NotNull
    @Contract(pure = true)
    private Object node(@NonNull @NotNull String key) {
        if (!this.root.containsKey(key)) {
            if (enableNestedMap) {
                final List<String> parts = asList(DOT.split(key));
                Object m = this.root.get(parts.get(0));
                int i = 0;
                while (i++ < parts.size() && m instanceof Map) {
                    final String newKey = String.join(".", parts.subList(i, parts.size()));
                    final Map<?, ?> mm = (Map<?, ?>) m;
                    if (mm.containsKey(newKey))
                        return mm.get(newKey);
                    m = mm.get(parts.get(1));
                }
            }
            throw new KfgIllegalStateException(this.name(), "missing key: " + key);
        }

        if (this.root.get(key) == null)
            throw new KfgIllegalStateException(this.name(), "null key: " + key);

        return this.root.get(key);
    }

    private <T> T checkStoredType(@NotNull @NonNull final Q<?> required) {
        final Object value = node(required.key());
        if (!required.matchesValue(value))
            throw new KfgMissingKeyException(this.name(), required);
        @SuppressWarnings("unchecked")
        final T t = (T) value;
        return t;
    }

    ExtMapSource(@NotNull @NonNull final String name,
                 @NonNull @NotNull final Supplier<Map<String, ?>> map,
                 final boolean enableNestedMap) {
        this.name = name;
        this.map = map;
        this.root = new HashMap<>(map.get());
        this.enableNestedMap = enableNestedMap;
        this.lastHash = this.root.hashCode();
        requireNonNull(this.map.get(), "supplied map is null");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isNull(@NonNull @NotNull final Q<?> key) {
        return this.root.get(key.key()) == null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean has(@NotNull @NonNull final Q<?> type) {
        if (!this.root.containsKey(type.key()))
            return false;
        return super.has(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Boolean bool0(@NotNull @NonNull final String key) {
        return checkStoredType(Q.bool(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Character char0(@NotNull @NonNull final String key) {
        return checkStoredType(Q.char_(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected String string0(@NotNull @NonNull final String key) {
        try {
            return checkStoredType(Q.string(key));
        }
        catch (KfgTypeException k0) {
            try {
                return this.checkStoredType(Q.char_(key)).toString();
            }
            catch (KfgTypeException k1) {
                throw k0;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    protected Number number0(@NotNull @NonNull final String key) {
        final Object n = node(key);
        if (n instanceof Long || n instanceof Integer ||
                n instanceof Short || n instanceof Byte)
            return ((Number) n).longValue();
        return checkStoredType(Q.long_(key));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    protected Number numberDouble0(@NotNull @NonNull final String key) {
        final Object n = node(key);
        if (n instanceof Long || n instanceof Integer ||
                n instanceof Short || n instanceof Byte ||
                n instanceof Double || n instanceof Float)
            return ((Number) n).doubleValue();
        return checkStoredType(Q.long_(key));
    }


    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    protected List<?> list0(@NotNull @NonNull final Q<? extends List<?>> type) {
        return checkStoredType(type);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    protected Set<?> set0(@NotNull @NonNull Q<? extends Set<?>> type) {
        return checkStoredType(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Map<?, ?> map0(@NotNull @NonNull Q<? extends Map<?, ?>> type) {
        return checkStoredType(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object custom0(@NotNull @NonNull final Q<?> type) {
        return checkStoredType(type);
    }

}
