package io.koosha.konfiguration;

import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public interface KeyObservable {

    /**
     * Register a listener to be notified of any updates to the konfiguration.
     * <p>
     * <em>DOES</em> hold an strong reference to the observer.
     * <p>
     * {@link #registerSoft(KeyObserver)} on the other hand, holds an string
     * reference to the observer until it is deregistered.
     *
     * @param observer the listener to register.
     * @return handle usable for deregister.
     * @see #registerSoft(KeyObserver)
     * @see #register(KeyObserver, String)
     */
    @NotNull
    @Contract(mutates = "this")
    default Handle register(@NotNull @NonNull final KeyObserver observer) {
        return this.register(observer, KeyObserver.LISTEN_TO_ALL);
    }

    /**
     * Register a listener to be notified of updates to a key.
     *
     * <em>DOES</em> hold an strong reference to the observer.
     * <p>
     * {@link #registerSoft(KeyObserver, String)} on the other hand, does
     * <em>NOT</em> holds an strong reference to the observer until it is
     * deregistered.
     *
     * @param observer the listener to register.
     * @param key      the key to listen too.
     * @return handle usable for deregister().
     * @see #registerSoft(KeyObserver, String)
     */
    @NotNull
    @Contract(mutates = "this")
    Handle register(@NotNull KeyObserver observer,
                    @NotNull String key);


    /**
     * Register a listener to be notified of any updates to the konfigurations.
     * <p>
     * Does <em>NOT</em> hold an strong reference to the observer, uses weak
     * references.
     * <p>
     * {@link #register(KeyObserver)} on the other hand, holds an string
     * reference to the observer until it is deregistered.
     *
     * @param observer the listener to register.
     * @return handle usable for deregister.
     * @see #register(KeyObserver)
     */
    @NotNull
    @Contract(mutates = "this")
    default Handle registerSoft(@NotNull @NonNull final KeyObserver observer) {
        return this.registerSoft(observer, KeyObserver.LISTEN_TO_ALL);
    }

    /**
     * Register a listener to be notified of updates to a key.
     * <p>
     * Does <em>NOT</em> hold an strong reference to the observer, uses weak
     * references.
     * <p>
     * {@link #register(KeyObserver, String)} on the other hand, holds an string
     * reference to the observer until it is deregistered.
     *
     * @param observer the listener to register.
     * @param key      the key to listen too.
     * @return handle usable for deregister().
     * @see #register(KeyObserver, String)
     */
    @NotNull
    @Contract(mutates = "this")
    Handle registerSoft(@NotNull KeyObserver observer,
                        @NotNull String key);


    /**
     * Deregister a previously registered listener of a key, form that key.
     *
     * If {@link KeyObserver#LISTEN_TO_ALL} is given, the observer is
     * de-registered from all keys.
     *
     * @param observer handle returned by one of register methods.
     */
    @Contract(mutates = "this")
    void deregister(@NotNull Handle observer,
                    @NotNull String key);

    /**
     * Deregister a previously registered listener from <em>ALL</em> keys.
     *
     * @param observer handle returned by one of register methods.
     */
    @Contract(mutates = "this")
    default void deregister(@NotNull Handle observer) {
        this.deregister(observer, KeyObserver.LISTEN_TO_ALL);
    }

}
