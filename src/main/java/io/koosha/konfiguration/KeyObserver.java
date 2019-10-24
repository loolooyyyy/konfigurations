package io.koosha.konfiguration;


import java.util.function.Consumer;


/**
 * Konfiguration observer which observes change in the konfiguration source
 * related to a specific key.
 */
public interface KeyObserver extends Consumer<String> {

    /**
     * Called when the konfiguration for the {@code key} is changed (updated).
     *
     * @param key
     *         the konfiguration key that it's value was updated. use empty
     *         key (that is "") to receive update on all keys.
     */
    void accept(String key);

}
