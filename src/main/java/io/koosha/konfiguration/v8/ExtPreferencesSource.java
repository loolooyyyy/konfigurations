package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.base.Deserializer;
import io.koosha.konfiguration.base.UpdatableSource;
import io.koosha.konfiguration.base.UpdatableSourceBase;
import io.koosha.konfiguration.error.KfgIllegalStateException;
import io.koosha.konfiguration.error.KfgSourceException;
import io.koosha.konfiguration.error.KfgTypeException;
import io.koosha.konfiguration.error.extended.KfgPreferencesError;
import io.koosha.konfiguration.type.Q;
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

    ExtPreferencesSource(@NotNull @NonNull final String name,
                         @NonNull @NotNull final Preferences preferences,
                         @Nullable final Deserializer deserializer) {
        this.name = name;
        this.source = preferences;
        this.deser = deserializer;
        this.lastHash = this.hashOf();
    }

    @Contract(pure = true)
    @Override
    public boolean hasUpdate() {
        return this.lastHash != this.hashOf();
    }

    @Contract(mutates = "this")
    @Override
    @NotNull
    public UpdatableSource updatedSelf() {
        return this;
    }

    @Override
    protected boolean isNull(@NonNull @NotNull final Q<?> key) {
        return this.source.get(this.sane(key.key()), "") == null;
    }

    @Override
    public boolean has(@NotNull @NonNull final Q<?> key) {
        final String sane;
        try {
            sane = this.sane(key.key());
        }
        catch (final KfgIllegalStateException e) {
            return false;
        }

        if (key.isByte())
            return this.source.getInt(sane, 0) == this.source.getInt(sane, 1)
                    && this.source.getInt(sane, 0) <= Byte.MAX_VALUE
                    && this.source.getInt(sane, 0) >= Byte.MIN_VALUE;
        if (key.isShort())
            return this.source.getInt(sane, 0) == this.source.getInt(sane, 1)
                    && this.source.getInt(sane, 0) <= Short.MAX_VALUE
                    && this.source.getInt(sane, 0) >= Short.MIN_VALUE;
        if (key.isInt())
            return this.source.getInt(sane, 0) == this.source.getInt(sane, 1);
        if (key.isLong())
            return this.source.getLong(sane, 0) == this.source.getLong(sane, 1);
        if (key.isFloat())
            return this.source.getFloat(sane, 0) == this.source.getFloat(sane, 1);
        if (key.isDouble())
            return this.source.getDouble(sane, 0) == this.source.getDouble(sane, 1);

        if (key.isBool())
            return this.source.getBoolean(sane, false) == this.source.getBoolean(sane, true);
        if (key.isChar())
            return this.source.get(sane, "").length() == 1;
        if (key.isString())
            return Objects.equals(this.source.get(sane, ""), this.source.get(sane, " "));


        //noinspection ArrayObjectsEquals
        if (Objects.equals(this.source.getByteArray(sane, null),
                this.source.getByteArray(sane, new byte[]{1})))
            try {
                this.deser.apply(this.source.getByteArray(this.sane(key.key()), new byte[0]), key);
                return true;
            }
            catch (final UnsupportedOperationException u) {
                return false;
            }

        return false;
    }

    @SneakyThrows
    @Contract(pure = true,
            value = "_->new")
    private String sane(@NotNull @NonNull final String key) {
        //noinspection HardcodedFileSeparator
        final String sane = key.replace('.', '/');
        try {
            if (!this.source.nodeExists(sane))
                throw new KfgIllegalStateException(this.name(), "missing key; " + key);
        }
        catch (final Throwable e) {
            throw new KfgSourceException(this.name(), "error checking existence of key: " + key, e);
        }
        return sane;
    }

    private int hashOf() {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            this.source.exportSubtree(buffer);
        }
        catch (final IOException | BackingStoreException e) {
            throw new KfgSourceException(this.name(), "could not calculate hash of the java.util.prefs.Preferences source", e);
        }
        return Arrays.hashCode(buffer.toByteArray());
    }


    @Override
    @NotNull
    protected Object bool0(@NotNull @NonNull final String key) {
        return this.source.getBoolean(this.sane(key), false);
    }

    @Override
    @NotNull
    protected Object char0(@NotNull @NonNull final String key) {
        final String s = ((String) this.string0(this.sane(key)));
        if (s.length() != 1)
            throw new KfgTypeException(this.name(), Q.char_(key), s);
        return ((CharSequence) this.string0(this.sane(key))).charAt(0);
    }

    @Override
    @NotNull
    protected Object string0(@NotNull @NonNull final String key) {
        return this.source.get(this.sane(key), null);
    }

    @Override
    @NotNull
    protected Number number0(@NotNull @NonNull final String key) {
        return this.source.getLong(this.sane(key), 0);
    }

    @Override
    @NotNull
    protected Number numberDouble0(@NotNull @NonNull final String key) {
        return this.source.getDouble(this.sane(key), 0);
    }

    @Override
    @NotNull
    protected List<?> list0(@NotNull @NonNull final Q<? extends List<?>> type) {
        if (this.deser == null)
            throw new KfgPreferencesError(this.name(), "deserializer not set");
        return this.deser.apply(this.source.getByteArray(this.sane(type.key()), new byte[0]), type);
    }

    @Override
    @NotNull
    protected Set<?> set0(@NotNull @NonNull final Q<? extends Set<?>> key) {
        if (this.deser == null)
            throw new KfgPreferencesError(this.name(), "deserializer not set");
        return this.deser.apply(this.source.getByteArray(this.sane(key.key()), new byte[0]), key);
    }

    @Override
    @NotNull
    protected Map<?, ?> map0(@NotNull @NonNull final Q<? extends Map<?, ?>> key) {
        if (this.deser == null)
            throw new KfgPreferencesError(this.name(), "deserializer not set");
        return this.deser.apply(this.source.getByteArray(this.sane(key.key()), new byte[0]), key);
    }

    @Override
    @NotNull
    protected Object custom0(@NotNull @NonNull final Q<?> key) {
        if (this.deser == null)
            throw new KfgPreferencesError(this.name(), "deserializer not set");
        return this.deser.apply(this.source.getByteArray(this.sane(key.key()), new byte[0]), key);
    }

}
