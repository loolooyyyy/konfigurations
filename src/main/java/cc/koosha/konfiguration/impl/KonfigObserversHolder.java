package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.EverythingObserver;
import cc.koosha.konfiguration.KeyObserver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * Helper class to manage collection of observers.
 * <p>
 * Thread-safe and immutable <b>BUT</b> the maps obtained from it's methods are
 * not immutable and are not thread-safe.
 */
final class KonfigObserversHolder {

    /**
     * Reference to KeyObserver must be weak.
     */
    private final Map<KeyObserver, Collection<String>> keyObservers;

    /**
     * Observers who observe everything, aka all the keys.
     * <p>
     * Reference to EverythingObserver must be weak, and that's why a map.
     */
    private final Map<EverythingObserver, Void> everythingObservers;

    KonfigObserversHolder() {

        everythingObservers = new WeakHashMap<>();
        keyObservers = new WeakHashMap<>();
    }

    KonfigObserversHolder(final KonfigObserversHolder from) {

        this.keyObservers = new HashMap<>(from.keyObservers);
        this.everythingObservers = new HashMap<>(from.everythingObservers);
    }

    /**
     * Observers mapped to the keys they are observing.
     *
     * @return Observers mapped to the keys they are observing.
     */
    Map<KeyObserver, Collection<String>> keyObservers() {

        return keyObservers;
    }

    /**
     * Observers who observe everything, aka all the keys.
     *
     * @return Observers who observe everything, aka all the keys.
     */
    Map<EverythingObserver, Void> everythingObservers() {

        return everythingObservers;
    }

}
