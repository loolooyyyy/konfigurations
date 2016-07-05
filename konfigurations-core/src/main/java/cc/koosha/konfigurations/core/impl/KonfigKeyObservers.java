package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.Konfiguration;
import lombok.Synchronized;
import lombok.val;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;


public final class KonfigKeyObservers {

    private final Map<String, Set<WeakReference<Consumer<String>>>> byKey = new HashMap<>();

    @Synchronized
    public void register(final String key, final Consumer<String> observer) {

        if(!byKey.containsKey(key))
            byKey.put(key, new HashSet<>());

        byKey.get(key).add(new WeakReference<>(observer));
    }

    @Synchronized
    public void unRegister(final String key, final Consumer<String> observer) {

        val listeners = byKey.getOrDefault(key, Collections.emptySet());

        for (val ref : listeners)
            if (ref.get() == observer) {
                listeners.remove(ref);
                return;
            }
    }

    @Synchronized
    private Collection<Consumer<String>> getListeners(final String key) {

        if(!byKey.containsKey(key))
            return Collections.emptyList();

        final Set<Consumer<String>> listeners = new HashSet<>();

        for (val ref : byKey.get(key)) {
            val listener = ref.get();
            if(listener != null)
                listeners.add(listener);
        }

        return listeners;
    }

    public void notifyKeyListeners(final String key, final Konfiguration source) {

        for (val listener : getListeners(key))
            listener.accept(key);
    }

    @Synchronized
    public void cleanup() {

        val all = byKey.entrySet();

        for (val each : all)
            if (each.getValue().size() == 0)
                all.remove(each);
    }

}
