package io.koosha.konfiguration.impl.v0;

import io.koosha.konfiguration.Deserializer;
import io.koosha.konfiguration.KfgSourceException;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.Q;
import io.koosha.konfiguration.ext.KfgPreferencesError;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;

/**
 * Reads konfig from a {@link Preferences} source.
 *
 * <p>for {@link #custom(String, Q)} to work, the supplied deserializer
 * must be configured to handle arbitrary types accordingly.
 *
 * <p><b>IMPORTANT</b> Does not coup too well with keys being added / removed
 * from backing source. Only changes are supported (as stated in
 * {@link Preferences#addNodeChangeListener(NodeChangeListener)})
 *
 * <p>Thread safe and immutable.
 *
 * <p>For now, pref change listener is not used
 */
final class ExtPreferencesSource extends AbstractKonfiguration {

    private final Deserializer<Preferences> deser;
    private final Preferences source;
    private final int lastHash;

    @Accessors(fluent = true)
    @Getter
    private final Manager manager = new Manager() {
        @Override
        @Contract(pure = true)
        public boolean hasUpdate() {
            return lastHash != hashOf(source);
        }

        @Override
        @NotNull
        @Contract(mutates = "this")
        public Konfiguration update() {
            return ExtPreferencesSource.this;
        }
    };

    ExtPreferencesSource(@NotNull @NonNull final String name,
                         final boolean readonly,
                         @NonNull @NotNull final Preferences preferences,
                         @Nullable final Deserializer<Preferences> deserializer) {
        super(readonly, name);
        this.source = preferences;
        this.deser = deserializer;
        this.lastHash = hashOf(source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull Object bool0(@NotNull String key) {
        return this.source.getBoolean(sane(key), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull Object char0(@NotNull String key) {
        return ((String) this.string0(sane(key))).charAt(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull Object string0(@NotNull String key) {
        return this.source.get(sane(key), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull Object byte0(@NotNull String key) {
        return this.source.getInt(sane(key), 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull Object short0(@NotNull String key) {
        return this.source.getInt(sane(key), 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull Object int0(@NotNull String key) {
        return this.source.getInt(sane(key), 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull Object long0(@NotNull String key) {
        return this.source.getLong(sane(key), 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull Object float0(@NotNull String key) {
        return this.source.getFloat(sane(key), 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull Object double0(@NotNull String key) {
        return this.source.getDouble(sane(key), 0);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @NotNull
    List<?> list0(@NotNull String key, @NotNull Q<? extends List<?>> type) {
        if (this.deser == null)
            throw new KfgPreferencesError(this, "deserializer not set");
        return this.deser.list(this.source.node(sane(key)), (Q) type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    Set<?> set0(@NotNull String key, @NotNull Q<? extends Set<?>> type) {
        if (this.deser == null)
            throw new KfgPreferencesError(this, "deserializer not set");
        return this.deser.set(this.source.node(sane(key)), (Q) type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    Map<?, ?> map0(@NotNull String key, @NotNull Q<? extends Map<?, ?>> type) {
        if (this.deser == null)
            throw new KfgPreferencesError(this, "deserializer not set");
        return this.deser.map(this.source.node(sane(key)), (Q) type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull Object custom0(@NotNull String key, @NotNull Q<?> type) {
        if (this.deser == null)
            throw new KfgPreferencesError(this, "deserializer not set");
        return this.deser.custom(this.source, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isNull(@NonNull @NotNull String key) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(@NotNull String key,
                       @Nullable Q<?> type) {
        try {
            return source.nodeExists(sane(key));
        }
        catch (Throwable e) {
            throw new KfgSourceException(this, key, null, null, "error checking existence of key", e);
        }
    }

    private int hashOf(@NonNull @NotNull final Preferences pref) {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            pref.exportSubtree(buffer);
        }
        catch (IOException | BackingStoreException e) {
            throw new KfgSourceException(this, null, null, null, "could not calculate hash of the java.util.prefs.Preferences source", null);
        }
        return Arrays.hashCode(buffer.toByteArray());
    }

    private static String sane(@NotNull @NonNull final String key) {
        return key.replace('.', '/');
    }

}
