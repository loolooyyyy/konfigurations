package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.KfgTypeException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.Q;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static io.koosha.konfiguration.Q.matchesValue;
import static java.util.Objects.requireNonNull;

/**
 * Reads konfig from a plain java map.
 *
 * <p>To fulfill contract of {@link Konfiguration}, all the values put in
 * the map of konfiguration key/values supplied to the konfiguration,
 * should be immutable.
 *
 * <p>Thread safe and immutable.
 */
class StorageSource extends AbstractKonfiguration {

    protected Supplier<Storage<String>> storageProvider;
    private final Storage<String> storage;

    @Accessors(fluent = true)
    @Getter
    private final Manager manager = new Manager() {
        @Override
        @Contract(pure = true)
        public boolean hasUpdate() {
            return !storageProvider.get().equals(storage);
        }

        @Override
        @NotNull
        @Contract(mutates = "this")
        public Konfiguration update() {
            return new StorageSource(name(), readonly(), storageProvider);
        }
    };

    StorageSource(@NotNull @NonNull final String name,
                  final boolean readonly,
                  @NotNull @NonNull final Supplier<Storage<String>> storage) {
        super(readonly, name);
        this.storageProvider = storage;
        this.storage = requireNonNull(this.storageProvider.get(), "newStorage");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isNull(@NonNull @NotNull String key) {
        return this.storage.isNull(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull String key, @Nullable Q<?> type) {
        if (!this.storage.has(key, type))
            return false;
        return matchesValue(type, this.storage.get(key, type));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final Object bool0(@NotNull String key) {
        final Q<Boolean> type = Q.BOOL;
        final Object v = this.storage.get(key, type);
        checkType(key, type, v);
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final Object char0(@NotNull String key) {
        final Q<Character> type = Q.CHAR;
        final Object v = this.storage.get(key, type);
        try {
            checkType(key, Q.BOOL, v);
            return v;
        }
        catch (final KfgTypeException e) {
            try {
                final Q<String> type1 = Q.STRING;
                checkType(key, type1, v);
                return ((String) v).charAt(0);
            }
            catch (final KfgTypeException ee) {
                throw e;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final Object string0(@NotNull String key) {
        final Q<String> type = Q.STRING;
        final Object v = this.storage.get(key, type);
        checkType(key, type, v);
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final Object byte0(@NotNull String key) {
        final Object v = this.storage.get(key);
        final Long b = toByte(v);
        if (b != null)
            return b.byteValue();
        checkType(key, Q.BYTE, v);
        throw new AssertionError();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final Object short0(@NotNull String key) {
        final Object v = this.storage.get(key);
        final Long b = toShort(v);
        if (b != null)
            return b.shortValue();
        checkType(key, Q.SHORT, v);
        throw new AssertionError();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final Object int0(@NotNull String key) {
        final Object v = this.storage.get(key);
        final Long b = toInt(v);
        if (b != null)
            return b.intValue();
        checkType(key, Q.INT, v);
        throw new AssertionError();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final Object long0(@NotNull String key) {
        final Object v = this.storage.get(key);
        final Long b = toLong(v);
        if (b != null)
            return b;
        checkType(key, Q.LONG, v);
        throw new AssertionError();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final Object float0(@NotNull String key) {
        final Object v = this.storage.get(key);
        final Float b = toFloat(v);
        if (b != null)
            return b;
        checkType(key, Q.FLOAT, v);
        throw new AssertionError();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final Object double0(@NotNull String key) {
        final Object v = this.storage.get(key);
        final Double b = toDouble(v);
        if (b != null)
            return b;
        checkType(key, Q.DOUBLE, v);
        throw new AssertionError();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final List<?> list0(@NotNull String key, @NotNull Q<? extends List<?>> q) {
        final Object v = this.storage.get(key);
        checkType(key, q, v);
        return (List<?>) v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final Set<?> set0(@NotNull String key, @NotNull Q<? extends Set<?>> type) {
        final Object v = this.storage.get(key);
        checkType(key, type, v);
        return (Set<?>) v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final Map<?, ?> map0(@NotNull String key, @NotNull Q<? extends Map<?, ?>> type) {
        final Object v = this.storage.get(key);
        checkType(key, type, v);
        return (Map<?, ?>) v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    final Object custom0(@NotNull String key, @NotNull Q<?> type) {
        final Object v = this.storage.get(key);
        checkType(key, type, v);
        return v;
    }

}
