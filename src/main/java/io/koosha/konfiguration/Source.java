package io.koosha.konfiguration;

import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
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
        return (K) list(key, (Q) Q.UNKNOWN_LIST);
    }

    /**
     * Get a list of U konfiguration value.
     *
     * @param type type object of values in the list.
     * @param <U>  generic type of elements in the list.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default <U> K<List<U>> list(@NotNull @NonNull final Q<List<U>> type) {
        if (type.key() == null)
            throw new KfgIllegalArgumentException(this.name(), "provided type has no key");
        return this.list(type.key(), type);
    }

    /**
     * Get a list of U konfiguration value.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of values in the list.
     * @param <U>  generic type of elements in the list.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    <U> K<List<U>> list(@NotNull String key,
                        @Nullable Q<List<U>> type);


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
        return (K) map(key, (Q) Q.UNKNOWN_MAP);
    }

    /**
     * Get a map of U to V konfiguration value.
     *
     * @param type generic type of map
     * @param <U>  generic type of map, the key type.
     * @param <V>  generic type of map, the value type.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default <U, V> K<Map<U, V>> map(@NotNull @NonNull final Q<Map<U, V>> type) {
        if (type.key() == null)
            throw new KfgIllegalArgumentException(this.name(), "provided type has no key");
        return this.map(type.key(), type);
    }

    /**
     * Get a map of U to V konfiguration value.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type generic type of map
     * @param <U>  generic type of map, the key type.
     * @param <V>  generic type of map, the value type.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    <U, V> K<Map<U, V>> map(@NotNull String key,
                            @Nullable Q<Map<U, V>> type);


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
        return (K) set(key, (Q) Q.UNKNOWN_SET);
    }

    /**
     * Get a set of konfiguration value.
     *
     * @param type type object of values in the set.
     * @param <U>  generic type of elements in the set.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default <U> K<Set<U>> set(@NotNull @NonNull final Q<Set<U>> type) {
        if (type.key() == null)
            throw new KfgIllegalArgumentException(this.name(), "provided type has no key");
        return this.set(type.key(), type);
    }

    /**
     * Get a set of konfiguration value.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of values in the set.
     * @param <U>  generic type of elements in the set.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    <U> K<Set<U>> set(@NotNull String key,
                      @Nullable Q<Set<U>> type);

    // ========================================================================

    /**
     * Get a custom object, type depends on underlying sources.
     *
     * <p><b>Important:</b> the underlying konfiguration source must support
     * this!
     *
     * <p><b>Important:</b> this method must <em>NOT</em> be used to obtain
     * maps, lists or sets. Use the corresponding methods
     * {@link #map(String, Q)}, {@link #list(String, Q)} and
     * {@link #set(String, Q)}.
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
     * maps, lists or sets. Use the corresponding methods
     * {@link #map(String, Q)}, {@link #list(String, Q)} and
     * {@link #set(String, Q)}.
     *
     * @param type type object of the requested value.
     * @param <U>  generic type of requested value.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default <U> K<U> custom(@NotNull @NonNull final Q<U> type) {
        if (type.key() == null)
            throw new KfgIllegalArgumentException(this.name(), "provided type has no key");
        return this.custom(type.key(), type);
    }

    /**
     * Get a custom object of type Q konfiguration value.
     *
     * <p><b>Important:</b> the underlying konfiguration source must support
     * this!
     *
     * <p><b>Important:</b> this method must <em>NOT</em> be used to obtain
     * maps, lists or sets. Use the corresponding methods
     * {@link #map(String, Q)}, {@link #list(String, Q)} and
     * {@link #set(String, Q)}.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of the requested value.
     * @param <U>  generic type of requested value.
     * @return konfiguration value wrapper for the requested key.
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    <U> K<U> custom(@NotNull String key,
                    @Nullable Q<U> type);

    // ========================================================================

    /**
     * Check if {@code key} exists in the configuration.
     *
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean has(@NotNull @NonNull final Q<?> type) {
        if (type.key() == null)
            throw new KfgIllegalArgumentException(this.name(), "provided type has no key");
        return this.has(type.key(), type);
    }

    /**
     * Check if {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    boolean has(@NotNull String key, @Nullable Q<?> type);


    /**
     * Check if boolean {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean hasBool(@NotNull @NonNull final String key) {
        return this.has(key, Q.BOOL);
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
        return this.has(key, Q.CHAR);
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
        return this.has(key, Q.STRING);
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
        return this.has(key, Q.BYTE);
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
        return this.has(key, Q.SHORT);
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
        return this.has(key, Q.INT);
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
        return this.has(key, Q.LONG);
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
        return this.has(key, Q.DOUBLE);
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
        return this.has(key, Q.DOUBLE);
    }

}
