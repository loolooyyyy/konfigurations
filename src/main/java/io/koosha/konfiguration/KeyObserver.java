package io.koosha.konfiguration;


import java.util.function.Consumer;


/**
 * Konfiguration observer which observes change in the konfiguration source.
 * <p>
 * This observer only receives the key. This means if you want to listen to
 * change in multiple sources and differentiate between them, you need to use
 * intermediate objects, one listener for each source, yourself.
 */
public interface KeyObserver extends Consumer<String> {

    /**
     * Listen to this key to listen to all changes.
     */
    String LISTEN_TO_ALL = "*";

    /**
     * Called when the konfiguration for the {@code key} is changed (updated).
     *
     * @param key the konfiguration key that it's value was updated. use empty
     *            key (that is "") to receive update on all keys.
     */
    void accept(String key);

}
