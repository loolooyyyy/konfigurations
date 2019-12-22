package io.koosha.konfiguration;

import io.koosha.konfiguration.error.KfgMissingKeyException;
import lombok.NonNull;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Konfig value wrapper.
 *
 * <p>All the methods denoted with 'Thread-safe' in their comment section must
 * be implemented in a thread safe fashion.
 *
 * @param <U> type of value being wrapped
 */
@SuppressWarnings("unused")
@ThreadSafe
@Immutable
@ApiStatus.AvailableSince(Faktory.VERSION_1)
public interface K<U> {

    /**
     * Register to receive update notifications for changes in value of this
     * konfiguration value, and this value only.
     *
     * <p>listeners may register to multiple keys on different instances of this
     * interface, but registering to the same key multiple times has no special
     * effect (it's only registered once).
     *
     * @param observer listener being registered for key {@link #key()}
     * @return this
     * @see #deregister(Handle)
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_1)
    Handle registerSoft(@NonNull @NotNull KeyObserver observer);

    /**
     * Register to receive update notifications for changes in value of this
     * konfiguration value, and this value only.
     *
     * <p>listeners may register to multiple keys on different instances of this
     * interface, but registering to the same key multiple times has no special
     * effect (it's only registered once).
     *
     * @param observer listener being registered for key {@link #key()}
     * @return this
     * @see #deregister(Handle)
     */
    @NotNull
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_1)
    Handle register(@NonNull @NotNull KeyObserver observer);

    /**
     * De-register a listener previously registered via
     * {@link #register(KeyObserver)}.
     *
     * <p>De-registering a previously de-registered listener, or a listener not
     * previously registered at all has no effect.
     *
     * <p>Thread-safe.
     *
     * <p><b>IMPORTANT:</b> Do NOT just pass in lambdas, as this method stores
     * only weak references and the observer will be garbage collected. Keep a
     * reference to the observer yourself.
     *
     * @param observer listener being registered for key {@link #key()}
     * @see #register(KeyObserver)
     */
    @Contract(mutates = "this")
    @ApiStatus.AvailableSince(Faktory.VERSION_1)
    void deregister(@NonNull @NotNull Handle observer);


    /**
     * Unique key of this konfiguration.
     *
     * <p>Thread-safe.
     *
     * @return unique key of this konfiguration.
     */
    @NotNull
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_1)
    String key();

    /**
     * Underlying type represented by this konfig key.
     *
     * @return Underlying type represented by this konfig key.
     */
    @Nullable
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_1)
    Q<U> type();

    /**
     * If the value denoted by {@link #key()} in the original source exists.
     *
     * <p>Thread-safe.
     *
     * @return If the value denoted by {@link #key()} in the original source
     * exists.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_1)
    boolean exists();

    /**
     * If the key exists and actual value of the konfiguration is null.
     *
     * @return if the key exists and actual value of the konfiguration is null.
     */
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    default boolean isNull() {
        return this.exists() && this.v() == null;
    }

    /**
     * Actual value of this konfiguration.
     *
     * <p>Thread-safe.
     *
     * @return Actual value of this konfiguration.
     * @throws KfgMissingKeyException if the value has been removed from
     *                                original konfiguration source.
     * @see #v(Object)
     */
    @Nullable
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_1)
    U v();

    /**
     * Same as {@link #v()} but in case of null throws {@link KfgMissingKeyException}.
     *
     * @see #v(Object)
     */
    @NotNull
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_1)
    U vn();

    /**
     * Similar to {@link #v()}, but returns the supplied default if this
     * konfiguration's key no longer exists in the source.
     *
     * <p>Thread-safe.
     *
     * @param defaultValue default value to use if key of this konfiguration
     *                     has been removed from the original source.
     * @return actual value of this konfiguration, or defaultValue if the key
     * of this konfiguration has been removed from the original source.
     * @see #v()
     */
    @SuppressWarnings("unused")
    @Nullable
    @Contract(pure = true)
    @ApiStatus.AvailableSince(Faktory.VERSION_1)
    default U v(@Nullable U defaultValue) {
        // Operation is not atomic.
        try {
            return this.exists() ? this.v() : defaultValue;
        }
        catch (final KfgMissingKeyException mk) {
            return defaultValue;
        }
    }


    // =========================================================================

    @NotNull
    @Contract(pure = true,
            value = " _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<U> null_(@Nullable final Q<U> type) {
        return null_(type, "");
    }

    @NotNull
    @Contract(pure = true,
            value = " _, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<U> null_(@Nullable final Q<U> type,
                          @NonNull @NotNull final String key) {
        return of(null, type, key);
    }


    @NotNull
    @Contract(pure = true,
            value = " _, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<U> of(@Nullable final U u,
                       @NotNull @NonNull final Q<U> type) {
        return of(u, type, type.key());
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<U> of(@Nullable final U u,
                       @Nullable final Q<U> type,
                       @NonNull @NotNull final String key) {
        return new DummyV<>(key, u, true, type);
    }


    @NotNull
    @Contract(pure = true,
            value = " _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<U> missing(@Nullable final Q<U> type) {
        return missing(type, "");
    }

    @NotNull
    @Contract(pure = true,
            value = " _, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<U> missing(@Nullable final Q<U> type,
                            @NonNull @NotNull final String key) {
        return new DummyV<>(key, null, false, type);
    }


    @NotNull
    @Contract(pure = true,
            value = " _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Boolean> false_(@NonNull @NotNull final String key) {
        return of(false, Q.bool(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = " _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Boolean> true_(@NonNull @NotNull final String key) {
        return of(true, Q.bool(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = " -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Boolean> false_() {
        return false_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Boolean> true_() {
        return true_("");
    }


    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Integer> mOne() {
        return mOne("");
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Integer> zero() {
        return zero("");
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Integer> one() {
        return one("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Integer> mOne(@NonNull @NotNull final String key) {
        return of(-1, Q.int_(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Integer> zero(@NonNull @NotNull final String key) {
        return of(0, Q.int_(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Integer> one(@NonNull @NotNull final String key) {
        return of(1, Q.int_(key), key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Boolean> bool(@Nullable final Boolean v) {
        return bool(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Boolean> bool(@Nullable final Boolean v,
                           @NonNull @NotNull final String key) {
        if (v == null)
            return null_(Q.bool(key));
        return v ? true_(key) : false_(key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Boolean> bool() {
        return missing(Q.bool(""));
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Boolean> bool(@NonNull @NotNull final String key) {
        return missing(Q.bool(key), key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Character> char_(@NotNull final Character v) {
        return of(v, Q.char_(""));
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Character> char_(final Character v,
                              @NonNull @NotNull final String key) {
        return of(v, Q.char_(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Character> char_() {
        return char_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Character> char_(@NonNull @NotNull final String key) {
        return missing(Q.char_(key), key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<String> stringOf(@Nullable final Object v,
                              @NonNull @NotNull final String key) {
        return of(String.valueOf(v), Q.string(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<String> stringMissing() {
        return stringMissing("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<String> stringMissing(@NonNull @NotNull final String key) {
        return missing(Q.string(key), key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Byte> byte_(@Nullable final Byte v) {
        return byte_(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Byte> byte_(@Nullable final Byte v,
                         @NonNull @NotNull final String key) {
        return of(v, Q.byte_(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Byte> byte_() {
        return byte_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Byte> byte_(@NonNull @NotNull final String key) {
        return missing(Q.byte_(key), key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Short> short_(@Nullable final Short v) {
        return short_(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Short> short_(final Short v,
                           @NonNull @NotNull final String key) {
        return of(v, Q.short_(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Short> short_() {
        return short_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Short> short_(@NonNull @NotNull final String key) {
        return missing(Q.short_(key), key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Integer> int_(@Nullable final Integer v) {
        return int_(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Integer> int_(final Integer v,
                           @NonNull @NotNull final String key) {
        return of(v, Q.int_(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Integer> int_() {
        return int_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Integer> int_(@NonNull @NotNull final String key) {
        return missing(Q.int_(key), key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Long> long_(@Nullable final Long v) {
        return long_(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Long> long_(@Nullable final Long v,
                         @NonNull @NotNull final String key) {
        return of(v, Q.long_(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Long> long_() {
        return long_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Long> long_(@NonNull @NotNull final String key) {
        return missing(Q.long_(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Float> float_(@Nullable final Float v) {
        return float_(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Float> float_(@Nullable final Float v,
                           @NonNull @NotNull final String key) {
        return of(v, Q.float_(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Float> float_() {
        return float_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Float> float_(@NonNull @NotNull final String key) {
        return missing(Q.float_(key), key);
    }


    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Double> double_(@Nullable final Double v) {
        return double_(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Double> double_(@Nullable final Double v,
                             @NonNull @NotNull final String key) {
        return of(v, Q.double_(key), key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Double> double_() {
        return double_("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static K<Double> double_(@NonNull @NotNull final String key) {
        return missing(Q.double_(key), key);
    }

    // =========================================================================
/*

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<List<U>> list(@Nullable final List<U> v) {
        return list(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<List<U>> list(@Nullable final List<U> v,
                               @NonNull @NotNull final String key) {
        return list(v, key, (Q) Q.UNKNOWN_LIST.withKey(key));
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<List<U>> list(@Nullable final List<U> v,
                               @Nullable final Q<List<U>> type) {
        return list(v, "", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<List<U>> list(@Nullable final List<U> v,
                               @NonNull @NotNull final String key,
                               @Nullable final Q<List<U>> type) {
        return of(v, type, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<List<U>> list() {
        return list("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<List<U>> list(@NonNull @NotNull final String key) {
        return list(key, (Q) Q.UNKNOWN_LIST.withKey(key));
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<List<U>> list(@Nullable final Q<List<U>> type) {
        return list("", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<List<U>> list(@NonNull @NotNull final String key,
                               @Nullable final Q<List<U>> type) {
        return missing(type, key);
    }

*/
    // =========================================================================
/*

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Set<U>> set(@Nullable final Set<U> v) {
        return set(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Set<U>> set(@Nullable final Set<U> v,
                             @NonNull @NotNull final String key) {
        return set(v, key, (Q) Q.UNKNOWN_SET.withKey(key));
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Set<U>> set(@Nullable final Set<U> v,
                             @Nullable final Q<Set<U>> type) {
        return set(v, "", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Set<U>> set(@Nullable final Set<U> v,
                             @NonNull @NotNull final String key,
                             @Nullable final Q<Set<U>> type) {
        return of(v, type, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Set<U>> set() {
        return set("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Set<U>> set(@NonNull @NotNull final String key) {
        return set(key, (Q) Q.UNKNOWN_SET.withKey(key));
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Set<U>> set(@Nullable final Q<Set<U>> type) {
        return set("", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Set<U>> set(@NonNull @NotNull final String key,
                             @Nullable final Q<Set<U>> type) {
        return missing(type, key);
    }

*/
    // =========================================================================
/*

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Collection<U>> collection(@Nullable final Collection<U> v) {
        return collection(v, "");
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Collection<U>> collection(@Nullable final Collection<U> v,
                                           @NonNull @NotNull final String key) {
        return collection(v, key, (Q) Q.UNKNOWN_COLLECTION);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Collection<U>> collection(@Nullable final Collection<U> v,
                                           @Nullable final Q<Collection<U>> type) {
        return collection(v, "", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Collection<U>> collection(@Nullable final Collection<U> v,
                                           @NonNull @NotNull final String key,
                                           @Nullable final Q<Collection<U>> type) {
        return of(v, type, key);
    }


    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Collection<U>> collection() {
        return collection("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Collection<U>> collection(@NonNull @NotNull final String key) {
        return collection(key, (Q) Q.UNKNOWN_COLLECTION);
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Collection<U>> collection(@NonNull @NotNull final Q<Collection<U>> type) {
        return collection("", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U> K<Collection<U>> collection(@NonNull @NotNull final String key,
                                           @Nullable final Q<Collection<U>> type) {
        return missing(type, key);
    }


*/
    // =========================================================================

/*
    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U, V> K<Map<U, V>> map(@Nullable final Map<U, V> v) {
        return map(v, "");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U, V> K<Map<U, V>> map(@Nullable final Map<U, V> v,
                                   @NonNull @NotNull final String key) {
        return map(v, key, (Q) Q.unknownMap(key));
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U, V> K<Map<U, V>> map(@Nullable final Map<U, V> v,
                                   @Nullable final Q<Map<U, V>> type) {
        return map(v, "", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U, V> K<Map<U, V>> map(@Nullable final Map<U, V> v,
                                   @NonNull @NotNull final String key,
                                   @Nullable final Q<Map<U, V>> type) {
        return of(v, type, key);
    }

    @NotNull
    @Contract(pure = true,
            value = "-> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U, V> K<Map<U, V>> map() {
        return map("");
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U, V> K<Map<U, V>> map(@NonNull @NotNull final String key) {
        return map(key, (Q) Q.UNKNOWN_LIST.withKey(key));
    }

    @NotNull
    @Contract(pure = true,
            value = "_ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U, V> K<Map<U, V>> map(@Nullable final Q<Map<U, V>> type) {
        return map("", type);
    }

    @NotNull
    @Contract(pure = true,
            value = "_, _ -> new")
    @ApiStatus.AvailableSince(Faktory.VERSION_8)
    static <U, V> K<Map<U, V>> map(@NonNull @NotNull final String key,
                                   @Nullable final Q<Map<U, V>> type) {
        return missing(type, key);
    }

*/

}
