package cc.koosha.konfiguration;


/**
 * Konfig value wrapper.
 *
 * <p>All the methods denoted with 'Thread-safe' in their comment section must be
 * implemented in a thread safe fashion.
 *
 * <p>Thread-safe
 *
 * @param <T> type of value being wrapped
 */
public interface K<T> {

    /**
     * Register to receive update notifications for changes in value of this
     * konfiguration value, and this value only.
     * <p>
     * listeners may register to multiple keys on different instances of this
     * interface, but registering to the same key multiple times has no special
     * effect (it's only registered once).
     * <p>
     * Thread-safe.
     * <p>
     * <b>IMPORTANT:</b> Do NOT just pass in lambdas, as this method stores
     * only weak references and the observer will be garbage collected. Keep a
     * reference to the observer yourself.
     *
     * @param observer listener being registered for key {@link #getKey()}
     * @return this
     * @see #deregister(KeyObserver)
     */
    K<T> register(KeyObserver observer);

    /**
     * De-register a listener previously registered via
     * {@link #register(KeyObserver)}.
     * <p>
     * De-registering a previously de-registered listener, or a listener not
     * previously registered at all has no effect.
     * <p>
     * Thread-safe.
     * <p>
     * <b>IMPORTANT:</b> Do NOT just pass in lambdas, as this method stores
     * only weak references and the observer will be garbage collected. Keep a
     * reference to the observer yourself.
     *
     * @param observer listener being registered for key {@link #getKey()}
     * @return this
     * @see #register(KeyObserver)
     */
    K<T> deregister(KeyObserver observer);

    /**
     * Unique key of this konfiguration.
     * <p>
     * Thread-safe.
     *
     * @return unique key of this konfiguration.
     */
    String getKey();

    /**
     * Actual value of this konfiguration.
     * <p>
     * Thread-safe.
     *
     * @return Actual value of this konfiguration.
     * @throws KonfigurationMissingKeyException if the value has been removed
     *                                          from original konfiguration
     *                                          source.
     * @see #v(Object)
     */
    T v();

    /**
     * Similar to {@link #v()}, but returns the supplied default if this
     * konfiguration's key no longer exists in the source.
     * <p>
     * Thread-safe.
     *
     * @param defaultValue default value to use if key of this konfiguration has
     *                     been removed from the original source.
     * @return actual value of this konfiguration, or defaultValue if the key of
     * this konfiguration has been removed from the original source.
     * @see #v()
     */
    T v(T defaultValue);

}
