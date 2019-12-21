package io.koosha.konfiguration.base;

import io.koosha.konfiguration.Source;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A source that manages itself (regarding updates).
 * <p>
 * These methods could simply be in {@link Source} but to keep source simple,
 * they are in a separate interface not forcing all sources to be updateable.
 * <p>
 * The update task can be delegated to an auxiliary class/object or the source
 * can implement this interface and itself be that auxiliary class/object.
 */
public interface UpdatableSource extends Source {

    @NotNull
    @Contract(pure = true,
            value = "->new")
    UpdatableSource updatedSelf();

    @Contract(pure = true)
    boolean hasUpdate();

}
