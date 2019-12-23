package io.koosha.konfiguration;

import lombok.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
@ThreadSafe
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public interface KonfigurationBuilder {

    @NotNull
    String name();


    @NotNull
    default KonfigurationBuilder add(@NotNull @NonNull Konfiguration konfig) {
        return this.add_(singleton(konfig));
    }

    @NotNull
    default KonfigurationBuilder add(@NotNull @NonNull final Konfiguration konfig,
                                     @NotNull @NonNull final Konfiguration... k) {
        final List<Konfiguration> l = new ArrayList<>();
        l.add(konfig);
        l.addAll(asList(k));
        for (final Konfiguration each : l)
            requireNonNull(each);
        return this.add_(l);
    }

    @NotNull
    KonfigurationBuilder add_(@NotNull @NonNull final Collection<Konfiguration> konfig);

    @NotNull
    default KonfigurationBuilder add(@NotNull @NonNull final KonfigurationManager konfig) {
        return this.add(singleton(konfig));
    }

    @NotNull
    default KonfigurationBuilder add(@NotNull final KonfigurationManager konfig,
                                     @NotNull final KonfigurationManager... k) {
        final List<KonfigurationManager> l = new ArrayList<>();
        l.add(konfig);
        l.addAll(Arrays.asList(k));
        for (final KonfigurationManager each : l)
            requireNonNull(each);
        return this.add(l);
    }

    @NotNull
    KonfigurationBuilder add(@NotNull Collection<KonfigurationManager> konfig);


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
