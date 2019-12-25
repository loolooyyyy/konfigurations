package io.koosha.konfiguration;

import lombok.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
@ThreadSafe
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public interface KonfigurationBuilder {

    @NotNull
    String name();


    @NotNull
    default KonfigurationBuilder add(@NotNull @NonNull final Konfiguration konfig) {
        return this.add_(singleton(konfig));
    }

    @NotNull
    default KonfigurationBuilder add(@NotNull @NonNull final Konfiguration konfig,
                                     @NotNull @NonNull final Konfiguration... rest) {
        final Collection<Konfiguration> list = new ArrayList<>();
        list.add(konfig);
        list.addAll(asList(rest));
        for (final Konfiguration each : list)
            requireNonNull(each);
        return this.add_(list);
    }

    @NotNull
    KonfigurationBuilder add_(@NotNull @NonNull final Collection<Konfiguration> konfig);

    @NotNull
    default KonfigurationBuilder add(@NotNull @NonNull final KonfigurationManager konfig) {
        return this.add(singleton(konfig));
    }

    @NotNull
    default KonfigurationBuilder add(@NotNull final KonfigurationManager konfig,
                                     @NotNull final KonfigurationManager... rest) {
        final Collection<KonfigurationManager> list = new ArrayList<>();
        list.add(konfig);
        list.addAll(asList(rest));
        for (final KonfigurationManager each : list)
            requireNonNull(each);
        return this.add(list);
    }

    @NotNull
    KonfigurationBuilder add(@NotNull Collection<? extends KonfigurationManager> konfig);


    @NotNull
    KonfigurationBuilder fairLock(boolean fair);

    @NotNull
    KonfigurationBuilder lockWaitTime(@Range(from = 0,
            to = Long.MAX_VALUE) long waitTime);

    @NotNull
    KonfigurationBuilder lockNoWait();

    @NotNull
    KonfigurationBuilder mixedTypes(boolean allow);

    @NotNull
    KonfigurationManager build();

}
