package io.koosha.konfiguration;


import lombok.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * All methods are thread-safe (and should be implemented as such).
 */
@SuppressWarnings("unused")
@ThreadSafe
@ApiStatus.AvailableSince(Faktory.VERSION_1)
public interface Konfiguration extends KeyObservable, Source {

    /**
     * Get a subset view of this konfiguration representing all the values under
     * the namespace of supplied key.
     *
     * @param key the key to which the scope of returned konfiguration is
     *            limited.
     * @return a konfiguration whose scope is limited to the supplied key.
     */
    @NotNull
    @Contract(pure = true)
    default Konfiguration subset(@NotNull @NonNull String key) {
        return new SubsetView(this.name() + "::" + key, this, key);
    }

}
