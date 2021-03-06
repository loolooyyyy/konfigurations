package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.Handle;
import io.koosha.konfiguration.KeyObserver;
import io.koosha.konfiguration.type.Q;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

@SuppressWarnings("unused")
@Accessors(fluent = true)
@EqualsAndHashCode(of = "handle")
@NotThreadSafe
@ApiStatus.Internal
final class Kombiner_Observer {

    @NotNull
    private final Handle handle = Kombiner.newHandle();

    @Nullable
    private final WeakReference<? extends KeyObserver> soft;

    @Nullable
    private final KeyObserver hard;

    @Nullable
    private final String key;

    @Nullable
    private final Q<?> type;

    Kombiner_Observer(final @NonNull @NotNull WeakReference<? extends KeyObserver> soft,
                      @Nullable final String key,
                      @Nullable final Q<?> type) {
        if (key != null && type != null)
            throw new IllegalStateException("both string key and Q type can't be set at the same time: "
                    + key + " | " + type);
        this.soft = soft;
        this.hard = null;
        this.key = key;
        this.type = type;
    }

    Kombiner_Observer(@NonNull @NotNull final KeyObserver hard,
                      @Nullable final String key,
                      @Nullable final Q<?> type) {
        if (key != null && type != null)
            throw new IllegalStateException("both string key and Q type can't be set at the same time: "
                    + key + " | " + type);
        this.soft = null;
        this.hard = hard;
        this.key = key;
        this.type = type;
    }

    @Contract(pure = true)
    @Nullable
    KeyObserver listener() {
        return this.soft != null ? this.soft.get() : this.hard;
    }

    @Nullable
    public String key() {
        return this.key;
    }

    @Nullable
    public Q<?> type() {
        return this.type;
    }

    @NotNull
    public Handle handle() {
        return this.handle;
    }

}
