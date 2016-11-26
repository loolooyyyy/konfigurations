package cc.koosha.konfiguration;


/**
 * Konfig value wrapper.
 *
 * All the methods denoted with 'Thread-safe' in their comment section must be
 * implemented in a thread safe fashion.
 *
 * @param <T> type of value being wrapped
 */
public interface KonfigV<T> {

    /**
     * Register to receive update notifications for changes in value of this
     * konfiguration value, and this value only.
     *
     * listeners may register to multiple keys on different instances of this
     * interface, but registering to the same key multiple times has no special
     * effect (it's only registered once).
     *
     * Thread-safe.
     *
     * @param observer listener being registered for key {@link #key()}
     * @return this
     */
    KonfigV<T> register(KeyObserver observer);

    /**
     * De-register a listener previously registered via {@link #register(KeyObserver)}.
     *
     * De-registering a previously de-registered listener, or a listener not
     * previously registered at all has no effect.
     *
     * Thread-safe.
     *
     * @param observer listener being registered for key {@link #key()}
     * @return this
     */
    KonfigV<T> deregister(KeyObserver observer);

    /**
     * Unique key of this konfiguration.
     *
     * Thread-safe.
     *
     * @return unique key of this konfiguration.
     */
    String key();

    /**
     * Actual value of this konfiguration.
     *
     * Thread-safe.
     *
     * @return Actual value of this konfiguration.
     * @throws KonfigurationMissingKeyException if the value has been removed from original konfiguration source.
     */
    T v();

    /**
     * Similar to {@link #v()}, but returns the supplied default if the key of
     * this konfiguration no longer exists in the source.
     *
     * Thread-safe.
     *
     * @param defaultValue default value to use if key of this konfiguration has been removed from the original source.
     * @return actual value of this konfiguration, or defaultValue if the key of this konfiguration has been removed from the original source.
     */
    T v(T defaultValue);

}
