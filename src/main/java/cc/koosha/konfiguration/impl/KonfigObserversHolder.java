package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.EverythingObserver;
import cc.koosha.konfiguration.KeyObserver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;


final class KonfigObserversHolder {

    private final Map<KeyObserver, Collection<String>> keyObservers;

    private final Map<EverythingObserver, Void> everythingObservers;

    KonfigObserversHolder() {

        everythingObservers = new WeakHashMap<>();
        keyObservers = new WeakHashMap<>();
    }

    KonfigObserversHolder(final KonfigObserversHolder from) {

        this.keyObservers = new HashMap<>(from.keyObservers);
        this.everythingObservers = new HashMap<>(from.everythingObservers);
    }

    Map<KeyObserver, Collection<String>> keyObservers() {

        return keyObservers;
    }

    Map<EverythingObserver, Void> everythingObservers() {

        return everythingObservers;
    }

}
