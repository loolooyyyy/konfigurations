package io.koosha.konfiguration;

import lombok.NonNull;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The most basic form of konfiguration source.
 * <p>
 * It will:
 * 1. Provides a konfiguration value, given a key and type
 * 2. Checks if it contains value for a given key and type.
 * <p>
 * <p>
 * Methods need not to cache their result, as
 * io.koosha.konfiguration.v8.Kombiner will take care of that.
 */
@SuppressWarnings("unused")
@NotThreadSafe
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public interface Source {

    /**
     * Name of this konfiguration. Helps with debugging and readability.
     *
     * @return Name of this configuration.
     */
    @NotNull
    @Contract(pure = true)
    String name();

    // ========================================================================

    /**
     * Get a boolean konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    K<Boolean> bool(@NotNull String key);

    /**
     * Get a byte konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    K<Byte> byte_(@NotNull String key);

    /**
     * Get a char konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    K<Character> char_(String key);

    /**
     * Get a short konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    K<Short> short_(String key);

    /**
     * Get an int konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    K<Integer> int_(String key);

    /**
     * Get a long konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    K<Long> long_(String key);

    /**
     * Get a float konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    K<Float> float_(String key);

    /**
     * Get a double konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    K<Double> double_(String key);

    /**
     * Get a string konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    K<String> string(String key);

    // ========================================================================

    /**
     * Get a list of konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(mutates = "this")
    default K<List<?>> list(@NotNull @NonNull final String key) {
        return (K) list((Q) Q.UNKNOWN_LIST.withKey(key));
    }

    /**
     * Get a list of U konfiguration value.
     *
     * @param key type object of values in the list.
     * @param <U> generic type of elements in the list.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    <U> K<List<U>> list(@NotNull Q<List<U>> key);


    /**
     * Get a map of U to V konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(mutates = "this")
    default K<Map<?, ?>> map(@NotNull @NonNull final String key) {
        return (K) map((Q) Q.UNKNOWN_MAP.withKey(key));
    }

    /**
     * Get a map of U to V konfiguration value.
     *
     * @param key generic type of map
     * @param <U> generic type of map, the key type.
     * @param <V> generic type of map, the value type.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    <U, V> K<Map<U, V>> map(@NotNull Q<Map<U, V>> key);


    /**
     * Get a set of konfiguration value.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(mutates = "this")
    default K<Set<?>> set(@NotNull @NonNull final String key) {
        return (K) set((Q) Q.UNKNOWN_SET.withKey(key));
    }

    /**
     * Get a set of konfiguration value.
     *
     * @param key type object of values in the set.
     * @param <U> generic type of elements in the set.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    <U> K<Set<U>> set(@NotNull Q<Set<U>> key);

    // ========================================================================

    /**
     * Get a custom object, type depends on underlying sources.
     *
     * <p><b>Important:</b> the underlying konfiguration source must support
     * this!
     *
     * <p><b>Important:</b> this method must <em>NOT</em> be used to obtain
     * maps, lists or sets. Use the corresponding methods {@link #map(String)},
     * {@link #list(String)} and {@link #set(String)}.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default <U> K<U> custom(@NotNull @NonNull final String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a custom object of type Q konfiguration value.
     *
     * <p><b>Important:</b> the underlying konfiguration source must support
     * this!
     *
     * <p><b>Important:</b> this method must <em>NOT</em> be used to obtain
     * maps, lists or sets. Use the corresponding methods {@link #map(Q)},
     * {@link #list(Q)} and {@link #set(Q)}.
     *
     * @param key type object of the requested value.
     * @param <U> generic type of requested value.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    <U> K<U> custom(@NotNull final Q<U> key);


    // ========================================================================

    /**
     * Check if {@code key} exists in the configuration.
     *
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    boolean has(@NotNull Q<?> key);

    /**
     * Check if map {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasMap(@NotNull @NonNull final Q<Map<?, ?>> key) {
        return this.has(key);
    }

    /**
     * Check if set {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasSet(@NotNull @NonNull final Q<Set<?>> key) {
        return this.has(key);
    }

    /**
     * Check if list {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasList(@NotNull @NonNull final Q<List<?>> key) {
        return this.has(key);
    }

    /**
     * Check if boolean {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasBool(@NotNull @NonNull final String key) {
        return this.has(Q.BOOL.withKey(key));
    }

    /**
     * Check if char {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasChar(@NotNull @NonNull final String key) {
        return this.has(Q.CHAR.withKey(key));
    }

    /**
     * Check if string {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasString(@NotNull @NonNull final String key) {
        return this.has(Q.STRING.withKey(key));
    }

    /**
     * Check if byte {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasByte(@NotNull @NonNull final String key) {
        return this.has(Q.BYTE.withKey(key));
    }

    /**
     * Check if short {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasShort(@NotNull @NonNull final String key) {
        return this.has(Q.SHORT.withKey(key));
    }

    /**
     * Check if int {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasInt(@NotNull @NonNull final String key) {
        return this.has(Q.INT.withKey(key));
    }

    /**
     * Check if long {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasLong(@NotNull @NonNull final String key) {
        return this.has(Q.LONG.withKey(key));
    }

    /**
     * Check if float {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasFloat(@NotNull @NonNull final String key) {
        return this.has(Q.FLOAT.withKey(key));
    }

    /**
     * Check if double {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasDouble(@NotNull @NonNull final String key) {
        return this.has(Q.DOUBLE.withKey(key));
    }

}
