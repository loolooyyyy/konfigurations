package io.koosha.konfiguration;


import lombok.NonNull;
import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * All methods are thread-safe (and should be implemented as such).
 * <p>
 * <p>
 * TODO what happens if Subset view goes into kombine?
 */
@SuppressWarnings("unused")
@ThreadSafe
public interface Konfiguration {

    /**
     * Get a boolean konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    K<Boolean> bool(@NotNull String key);

    /**
     * Get a byte konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    K<Byte> byte_(@NotNull String key);

    /**
     * Get a char konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    K<Character> char_(@NonNull String key);

    /**
     * Get a short konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    K<Short> short_(@NonNull String key);

    /**
     * Get an int konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    K<Integer> int_(@NonNull String key);

    /**
     * Get a long konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    K<Long> long_(@NonNull String key);

    /**
     * Get a float konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    K<Float> float_(@NonNull String key);

    /**
     * Get a double konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    K<Double> double_(@NonNull String key);

    /**
     * Get a string konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    K<String> string(@NonNull String key);


    /**
     * Get a list of konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    default K<List<?>> list(@NotNull @NonNull final String key) {
        return (K) list(key, (Q) Q.UNKNOWN_LIST);
    }

    /**
     * Get a map of U to V konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    default K<Map<?, ?>> map(@NotNull @NonNull final String key) {
        return (K) map(key, (Q) Q.UNKNOWN_MAP);
    }

    /**
     * Get a set of konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    default K<Set<?>> set(@NotNull @NonNull final String key) {
        return (K) set(key, (Q) Q.UNKNOWN_SET);
    }

    /**
     * Get a list of U konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of values in the list.
     * @param <U>  generic type of elements in the list.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    <U> K<List<U>> list(@NonNull String key, @Nullable Q<List<U>> type);

    /**
     * Get a map of U to V konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type generic type of map
     * @param <U>  generic type of map, the key type.
     * @param <V>  generic type of map, the value type.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    <U, V> K<Map<U, V>> map(@NotNull String key, @Nullable Q<Map<U, V>> type);

    /**
     * Get a set of konfiguration value.
     *
     * <p>Thread-safe.
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of values in the set.
     * @param <U>  generic type of elements in the set.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    <U> K<Set<U>> set(@NotNull String key, @Nullable Q<Set<U>> type);


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
     * <p>Thread-safe
     *
     * @param key unique key of the konfiguration being requested.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    default <U> K<U> custom(@NotNull final String key) {
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
     * <p>Thread-safe
     *
     * @param key  unique key of the konfiguration being requested.
     * @param type type object of the requested value.
     * @param <U>  generic type of requested value.
     * @return konfiguration value wrapper for the requested key.
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    <U> K<U> custom(@NonNull String key, @Nullable Q<U> type);


    /**
     * Check if {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    boolean has(@NotNull String key, @Nullable Q<?> type);


    /**
     * Check if boolean {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    default boolean hasBool(@NotNull String key, @NotNull Q<?> type) {
        return this.has(key, Q.BOOL);
    }

    /**
     * Check if char {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    default boolean hasChar(@NotNull String key) {
        return this.has(key, Q.CHAR);
    }

    /**
     * Check if string {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    default boolean hasString(@NotNull String key) {
        return this.has(key, Q.STRING);
    }

    /**
     * Check if byte {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    default boolean hasByte(@NotNull String key) {
        return this.has(key, Q.BYTE);
    }

    /**
     * Check if short {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    default boolean hasShort(@NotNull String key) {
        return this.has(key, Q.SHORT);
    }

    /**
     * Check if int {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    default boolean hasInt(@NotNull String key) {
        return this.has(key, Q.INT);
    }

    /**
     * Check if long {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    default boolean hasLong(@NotNull String key) {
        return this.has(key, Q.LONG);
    }

    /**
     * Check if float {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    default boolean hasFloat(@NotNull String key) {
        return this.has(key, Q.DOUBLE);
    }

    /**
     * Check if double {@code key} exists in the configuration.
     *
     * @param key the config key to check it's existence
     * @return true if the key exists, false otherwise.
     */
    @Contract(pure = true)
    default boolean hasDouble(@NotNull String key) {
        return this.has(key, Q.DOUBLE);
    }


    // =========================================================================

    /**
     * Register a listener to be notified of any updates to the konfigurations.
     * <p>
     * Does <em>NOT</em> hold an strong reference to the observer, uses weak
     * references.
     *
     * @param observer the listener to register.
     * @return this.
     * @see #deregisterSoft(KeyObserver, String)
     * @see #deregisterSoft(KeyObserver)
     * @see #registerSoft(KeyObserver, String)
     * @see #registerSoft(KeyObserver)
     * @see #deregisterHard(KeyObserver, String)
     * @see #deregisterHard(KeyObserver)
     * @see #registerHard(KeyObserver, String)
     * @see #registerHard(KeyObserver)
     * @see KeyObserver#LISTEN_TO_ALL
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    default Konfiguration registerSoft(@NotNull @NonNull final KeyObserver observer) {
        return this.registerSoft(observer, KeyObserver.LISTEN_TO_ALL);
    }

    /**
     * De-Register a previously registered listener from all events.
     *
     * @param observer the listener to de-register.
     * @return this.
     * @see #deregisterSoft(KeyObserver, String)
     * @see #deregisterSoft(KeyObserver)
     * @see #registerSoft(KeyObserver, String)
     * @see #registerSoft(KeyObserver)
     * @see #deregisterHard(KeyObserver, String)
     * @see #deregisterHard(KeyObserver)
     * @see #registerHard(KeyObserver, String)
     * @see #registerHard(KeyObserver)
     * @see KeyObserver#LISTEN_TO_ALL
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    default Konfiguration deregisterSoft(@NotNull @NonNull final KeyObserver observer) {
        return this.deregisterSoft(observer, KeyObserver.LISTEN_TO_ALL);
    }

    /**
     * Register a listener to be notified of any updates to the konfiguration.
     * <p>
     * <em>Does</em> hold an strong reference to the observer.
     *
     * @param observer the listener to register.
     * @return this.
     * @see #deregisterSoft(KeyObserver, String)
     * @see #deregisterSoft(KeyObserver)
     * @see #registerSoft(KeyObserver, String)
     * @see #registerSoft(KeyObserver)
     * @see #deregisterHard(KeyObserver, String)
     * @see #deregisterHard(KeyObserver)
     * @see #registerHard(KeyObserver, String)
     * @see #registerHard(KeyObserver)
     * @see KeyObserver#LISTEN_TO_ALL
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    default Konfiguration registerHard(@NotNull @NonNull final KeyObserver observer) {
        return this.registerHard(observer, KeyObserver.LISTEN_TO_ALL);
    }

    /**
     * De-Register a previously registered listener from all events.
     *
     * <p>Thread-safe.
     *
     * @param observer the listener to de-register.
     * @return this.
     * @see #deregisterSoft(KeyObserver, String)
     * @see #deregisterSoft(KeyObserver)
     * @see #registerSoft(KeyObserver, String)
     * @see #registerSoft(KeyObserver)
     * @see #deregisterHard(KeyObserver, String)
     * @see #deregisterHard(KeyObserver)
     * @see #registerHard(KeyObserver, String)
     * @see #registerHard(KeyObserver)
     * @see KeyObserver#LISTEN_TO_ALL
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    default Konfiguration deregisterHard(@NotNull @NonNull final KeyObserver observer) {
        return this.deregisterHard(observer, KeyObserver.LISTEN_TO_ALL);
    }

    // =================================

    /**
     * Register a listener to be notified of updates to a key.
     * <p>
     * Does <em>NOT</em> hold an strong reference to the observer, uses weak
     * references.
     *
     * @param observer the listener to register.
     * @param key      the key to listen too.
     * @return this.
     * @see #deregisterSoft(KeyObserver, String)
     * @see #deregisterSoft(KeyObserver)
     * @see #registerSoft(KeyObserver, String)
     * @see #registerSoft(KeyObserver)
     * @see #deregisterHard(KeyObserver, String)
     * @see #deregisterHard(KeyObserver)
     * @see #registerHard(KeyObserver, String)
     * @see #registerHard(KeyObserver)
     * @see KeyObserver#LISTEN_TO_ALL
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    Konfiguration registerSoft(@NotNull KeyObserver observer,
                               @NotNull String key);

    /**
     * De-Register a previously registered listener of a key.
     *
     * <p>Thread-safe.
     *
     * @param observer the listener to de-register.
     * @return this.
     * @see #deregisterSoft(KeyObserver, String)
     * @see #deregisterSoft(KeyObserver)
     * @see #registerSoft(KeyObserver, String)
     * @see #registerSoft(KeyObserver)
     * @see #deregisterHard(KeyObserver, String)
     * @see #deregisterHard(KeyObserver)
     * @see #registerHard(KeyObserver, String)
     * @see #registerHard(KeyObserver)
     * @see KeyObserver#LISTEN_TO_ALL
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    Konfiguration deregisterSoft(@NotNull KeyObserver observer,
                                 @NotNull String key);


    /**
     * Register a listener to be notified of updates to a key.
     *
     * <em>Does</em> hold an strong reference to the observer.
     *
     * @param observer the listener to register.
     * @param key      the key to listen too.
     * @return this.
     * @see #deregisterSoft(KeyObserver, String)
     * @see #deregisterSoft(KeyObserver)
     * @see #registerSoft(KeyObserver, String)
     * @see #registerSoft(KeyObserver)
     * @see #deregisterHard(KeyObserver, String)
     * @see #deregisterHard(KeyObserver)
     * @see #registerHard(KeyObserver, String)
     * @see #registerHard(KeyObserver)
     * @see KeyObserver#LISTEN_TO_ALL
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    Konfiguration registerHard(@NotNull KeyObserver observer,
                               @NotNull String key);

    /**
     * De-Register a previously registered listener of a key.
     *
     * <p>Thread-safe.
     *
     * @param observer the listener to de-register.
     * @return this.
     * @see #deregisterSoft(KeyObserver, String)
     * @see #deregisterSoft(KeyObserver)
     * @see #registerSoft(KeyObserver, String)
     * @see #registerSoft(KeyObserver)
     * @see #deregisterHard(KeyObserver, String)
     * @see #deregisterHard(KeyObserver)
     * @see #registerHard(KeyObserver, String)
     * @see #registerHard(KeyObserver)
     * @see KeyObserver#LISTEN_TO_ALL
     */
    @NonNull
    @NotNull
    @Contract(mutates = "this")
    Konfiguration deregisterHard(@NotNull KeyObserver observer,
                                 @NotNull String key);


    // =========================================================================

    /**
     * Name of this konfiguration. Helps with debugging and readability.
     *
     * @return Name of this configuration.
     */
    @NonNull
    @NotNull
    @Contract(pure = true)
    String name();


    /**
     * Get a subset view of this konfiguration representing all the values under
     * the namespace of supplied key.
     *
     * <p>Thread-safe.
     *
     * @param key the key to which the scope of returned konfiguration is
     *            limited.
     * @return a konfiguration whose scope is limited to the supplied key.
     */
    @NonNull
    @NotNull
    @Contract(pure = true)
    Konfiguration subset(@NotNull String key);

    // =========================================================================

    /**
     * Manager object associated with this konfiguration.
     *
     * @return On first invocation, a manager instance. An second invocation
     * and on, throws exception.
     * @throws KfgIllegalStateException if manager is already called once
     *                                  before.
     */
    @NotNull
    @Contract(mutates = "this")
    Manager manager();

    @NotThreadSafe
    interface Manager {

        /**
         * Indicates whether if anything is actually updated in the origin of
         * this source.
         *
         * <p>This action must <b>NOT</b> modify the instance.</p>
         *
         * <p><b>VERY VERY IMPORTANT:</b> This method is to be called only from
         * a single thread or concurrency issues will arise.</p>
         *
         * <p>Why? To check and see if it's updatable, a source might ask it's
         * origin (a web url?) to get the new content, to compare with the old
         * content, and it asks it's origin for the new content once more, to
         * actually update the values. If this method is called during
         * KonfigurationKombiner is also calling it, this might interfere and
         * lost updates may happen.</p>
         *
         * <p>To help blocking issues, update() is allowed to block the current
         * thread, and update observers will continue to work in their own
         * thread. This mechanism also helps to notify them only after when
         * <em>all</em> the combined sources are updated.</p>
         *
         * <p>NOT Thread-safe.
         *
         * @return true if the source obtained via {@link #update()} ()} will
         * differ from this source.
         */
        @Contract(pure = true)
        default boolean hasUpdate() {
            return false;
        }

        /**
         * Creates an <b>updated</b> copy of it's source.
         *
         * <p>A call to this method must <b>NOT</b> modify the instance, but the
         * newly created source must contain the updated values.
         *
         * <p><b>NOT</b> Thread-safe
         *
         * <b>The exceptions to these rules, are <em>Combiners</em> which combine
         * multiple source into one:</b>
         * One implementation is KonfigurationKombiner.class
         * Update all the konfiguration values and notify the update observers.<br>
         * Important: the key observers might be notified <em>after</em> the
         * cache update, and it is implementation specific. So it's possible that a
         * call to {@link K#v()} returns the new value, while the observer is not
         * notified yet.
         * <p>
         *
         * @return an updated copy of this source, but not necessarily a new one:
         * can return this instance itself if no update is available.
         * @throws KfgException TODO
         */
        @NotNull
        @Contract(mutates = "this")
        Konfiguration update();

    }

}
