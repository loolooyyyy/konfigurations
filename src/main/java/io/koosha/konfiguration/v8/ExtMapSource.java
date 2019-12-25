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

    @Contract(pure = true)
    @Override
    public boolean hasUpdate() {
        final Map<String, ?> newMap = this.map.get();
        if (newMap == null)
            return false;
        final int newHash = newMap.hashCode();
        return newHash != this.lastHash;
    }

    @Contract(pure = true,
            value = "-> new")
    @NotNull
    @Override
    public UpdatableSource updatedSelf() {
        return this.hasUpdate()
               ? new ExtMapSource(this.name(), this.map, this.enableNestedMap)
               : this;
    }

    @NotNull
    @Contract(pure = true)
    private Object node(@NonNull @NotNull final String key) {
        if (!this.root.containsKey(key)) {
            if (this.enableNestedMap) {
                final List<String> parts = asList(DOT.split(key));
                Object storedObject = this.root.get(parts.get(0));
                int i = 0;
                while (i++ < parts.size() && storedObject instanceof Map) {
                    final String newKey = String.join(".", parts.subList(i, parts.size()));
                    final Map<?, ?> mm = (Map<?, ?>) storedObject;
                    if (mm.containsKey(newKey))
                        return mm.get(newKey);
                    storedObject = mm.get(parts.get(1));
                }
            }
            throw new KfgIllegalStateException(this.name(), "missing key: " + key);
        }

        if (this.root.get(key) == null)
            throw new KfgIllegalStateException(this.name(), "null key: " + key);

        return this.root.get(key);
    }

    private <T> T ensureStoredType(@NotNull @NonNull final Q<?> required) {
        final Object value = this.node(required.key());
        if (!required.matchesValue(value))
            throw new KfgMissingKeyException(this.name(), required);
        @SuppressWarnings("unchecked")
        final T t = (T) value;
        return t;
    }

    @Override
    protected boolean isNull(@NonNull @NotNull final Q<?> key) {
        return this.root.get(key.key()) == null;
    }

    public boolean has(@NotNull @NonNull final Q<?> key) {
        if (!this.root.containsKey(key.key()))
            return false;
        return super.has(key);
    }

    @Override
    @NotNull
    protected Boolean bool0(@NotNull @NonNull final String key) {
        return this.ensureStoredType(Q.bool(key));
    }

    @Override
    @NotNull
    protected Character char0(@NotNull @NonNull final String key) {
        return this.ensureStoredType(Q.char_(key));
    }

    @Override
    @NotNull
    protected String string0(@NotNull @NonNull final String key) {
        try {
            return this.ensureStoredType(Q.string(key));
        }
        catch (final KfgTypeException k0) {
            try {
                return this.ensureStoredType(Q.char_(key)).toString();
            }
            catch (final KfgTypeException k1) {
                throw k0;
            }
        }
    }

    @NotNull
    @Override
    protected Number number0(@NotNull @NonNull final String key) {
        final Object number = this.node(key);
        if (number instanceof Long || number instanceof Integer ||
                number instanceof Short || number instanceof Byte)
            return ((Number) number).longValue();
        return this.ensureStoredType(Q.long_(key));
    }

    @NotNull
    @Override
    protected Number numberDouble0(@NotNull @NonNull final String key) {
        final Object number = this.node(key);
        if (number instanceof Long || number instanceof Integer ||
                number instanceof Short || number instanceof Byte ||
                number instanceof Double || number instanceof Float)
            return ((Number) number).doubleValue();
        return this.ensureStoredType(Q.long_(key));
    }


    @NotNull
    @Override
    protected List<?> list0(@NotNull @NonNull final Q<? extends List<?>> type) {
        return this.ensureStoredType(type);
    }

    @NotNull
    @Override
    protected Set<?> set0(@NotNull @NonNull final Q<? extends Set<?>> key) {
        return this.ensureStoredType(key);
    }

    @Override
    @NotNull
    protected Map<?, ?> map0(@NotNull @NonNull final Q<? extends Map<?, ?>> key) {
        return this.ensureStoredType(key);
    }

    @Override
    @NotNull
    protected Object custom0(@NotNull @NonNull final Q<?> key) {
        return this.ensureStoredType(key);
    }

}
