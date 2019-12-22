package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.Q;
import io.koosha.konfiguration.base.Deserializer;
import io.koosha.konfiguration.base.UpdatableSource;
import io.koosha.konfiguration.base.UpdatableSourceBase;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import io.koosha.konfiguration.error.KfgSourceException;
import io.koosha.konfiguration.error.KfgTypeException;
import io.koosha.konfiguration.error.extended.KfgPreferencesError;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;

/**
 * Reads konfig from a {@link Preferences} source.
 *
 * <p>for {@link #custom(Q)} to work, the supplied deserializer must be
 * configured to handle arbitrary types accordingly.
 *
 * <p><b>IMPORTANT</b> Does not coup too well with keys being added / removed
 * from backing source. Only changes are supported (as stated in
 * {@link Preferences#addNodeChangeListener(NodeChangeListener)})
 *
 * <p>Thread safe and immutable.
 *
 * <p>For now, pref change listener is not used
 */
@ApiStatus.Internal
@ThreadSafe
final class ExtPreferencesSource extends UpdatableSourceBase {

    private final Deserializer deser;
    private final Preferences source;
    private final int lastHash;

    @NonNull
    @NotNull
    @Getter
    @Accessors(fluent = true)
    private final String name;

    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    public boolean hasUpdate() {
        return lastHash != hashOf();
    }

    /**
     * {@inheritDoc}
     */
    @Contract(mutates = "this")
    @Override
    @NotNull
    public UpdatableSource updatedSelf() {
        return this;
    }


    ExtPreferencesSource(@NotNull @NonNull final String name,
                         @NonNull @NotNull final Preferences preferences,
                         @Nullable final Deserializer deserializer) {
        this.name = name;
        this.source = preferences;
        this.deser = deserializer;
        this.lastHash = hashOf();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isNull(@NonNull @NotNull final Q<?> key) {
        return this.source.get(sane(key.key()), "") == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull @NonNull final Q<?> type) {
        final String sane;
        try {
            sane = this.sane(type.key());
        }
        catch (final KfgIllegalStateException e) {
            return false;
        }

        if (type.isByte())
            return this.source.getInt(sane, 0) == this.source.getInt(sane, 1)
                    && this.source.getInt(sane, 0) <= Byte.MAX_VALUE
                    && this.source.getInt(sane, 0) >= Byte.MIN_VALUE;
        if (type.isShort())
            return this.source.getInt(sane, 0) == this.source.getInt(sane, 1)
                    && this.source.getInt(sane, 0) <= Short.MAX_VALUE
                    && this.source.getInt(sane, 0) >= Short.MIN_VALUE;
        if (type.isInt())
            return this.source.getInt(sane, 0) == this.source.getInt(sane, 1);
        if (type.isLong())
            return this.source.getLong(sane, 0) == this.source.getLong(sane, 1);
        if (type.isFloat())
            return this.source.getFloat(sane, 0) == this.source.getFloat(sane, 1);
        if (type.isDouble())
            return this.source.getDouble(sane, 0) == this.source.getDouble(sane, 1);

        if (type.isBool())
            return this.source.getBoolean(sane, false) == this.source.getBoolean(sane, true);
        if (type.isChar())
            return this.source.get(sane, "").length() == 1;
        if (type.isString())
            return Objects.equals(this.source.get(sane, ""), this.source.get(sane, " "));


        //noinspection ArrayObjectsEquals
        if (Objects.equals(this.source.getByteArray(sane, null),
                this.source.getByteArray(sane, new byte[]{1})))
            try {
                this.deser.apply(this.source.getByteArray(sane(type.key()), new byte[0]), type);
                return true;
            }
            catch (UnsupportedOperationException u) {
                return false;
            }

        return false;
    }

    @SneakyThrows
    @Contract(pure = true,
            value = "_->new")
    private String sane(@NotNull @NonNull final String key) {
        final String sane = key.replace('.', '/');
        try {
            if (!this.source.nodeExists(sane))
                throw new KfgIllegalStateException(this.name(), "missing key; " + key);
        }
        catch (Throwable e) {
            throw new KfgSourceException(this.name(), "error checking existence of key: " + key, e);
        }
        return sane;
    }

    private int hashOf() {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            this.source.exportSubtree(buffer);
        }
        catch (IOException | BackingStoreException e) {
            throw new KfgSourceException(this.name(), "could not calculate hash of the java.util.prefs.Preferences source", e);
        }
        return Arrays.hashCode(buffer.toByteArray());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object bool0(@NotNull @NonNull final String key) {
        return this.source.getBoolean(sane(key), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object char0(@NotNull @NonNull final String key) {
        final String s = ((String) this.string0(sane(key)));
        if (s.length() != 1)
            throw new KfgTypeException(this.name(), key, Q.char_(key), s);
        return ((String) this.string0(sane(key))).charAt(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object string0(@NotNull @NonNull final String key) {
        return this.source.get(sane(key), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Number number0(@NotNull @NonNull final String key) {
        return this.source.getLong(sane(key), 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Number numberDouble0(@NotNull @NonNull final String key) {
        return this.source.getDouble(sane(key), 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected List<?> list0(@NotNull @NonNull final Q<? extends List<?>> type) {
        if (this.deser == null)
            throw new KfgPreferencesError(this.name(), "deserializer not set");
        return this.deser.apply(this.source.getByteArray(sane(type.key()), new byte[0]), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Set<?> set0(@NotNull @NonNull final Q<? extends Set<?>> type) {
        if (this.deser == null)
            throw new KfgPreferencesError(this.name(), "deserializer not set");
        return this.deser.apply(this.source.getByteArray(sane(type.key()), new byte[0]), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Map<?, ?> map0(@NotNull @NonNull final Q<? extends Map<?, ?>> type) {
        if (this.deser == null)
            throw new KfgPreferencesError(this.name(), "deserializer not set");
        return this.deser.apply(this.source.getByteArray(sane(type.key()), new byte[0]), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected Object custom0(@NotNull @NonNull final Q<?> type) {
        if (this.deser == null)
            throw new KfgPreferencesError(this.name(), "deserializer not set");
        return this.deser.apply(this.source.getByteArray(sane(type.key()), new byte[0]), type);
    }

}
