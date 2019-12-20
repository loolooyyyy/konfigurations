package io.koosha.konfiguration;

import io.koosha.konfiguration.impl.base.KonfigurationManagerBase;
import lombok.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
@ThreadSafe
@ApiStatus.AvailableSince(Faktory.VERSION_8)
public interface KonfigurationBuilder {

    @NotNull
    default KonfigurationBuilder addReadonly(@NotNull @NonNull Konfiguration konfig) {
        return this.addReadonly(singleton(konfig));
    }

    @NotNull
    default KonfigurationBuilder addReadonly(@NotNull @NonNull final Konfiguration konfig,
                                             @NotNull @NonNull final Konfiguration... k) {
        final List<Konfiguration> l = new ArrayList<>();
        l.add(konfig);
        l.addAll(asList(k));
        for (final Konfiguration each : l)
            requireNonNull(each);
        return this.addReadonly(l);
    }

    @NotNull
    default KonfigurationBuilder addReadonly(@NotNull @NonNull final Collection<Konfiguration> konfig) {
        return this.add(konfig.stream()
                              .peek(Objects::requireNonNull)
                              .map(KonfigurationManagerBase::new)
                              .collect(toList()));
    }


    @NotNull
    default KonfigurationBuilder add(@NotNull @NonNull final KonfigurationManager<?> konfig) {
        return this.add(singleton(konfig));
    }

    @NotNull
    default KonfigurationBuilder add(@NotNull final KonfigurationManager<?> konfig,
                                     @NotNull final KonfigurationManager<?>... k) {
        final List<KonfigurationManager<?>> l = new ArrayList<>();
        l.add(konfig);
        l.addAll(Arrays.asList(k));
        for (final KonfigurationManager<?> each : l)
            requireNonNull(each);
        return this.add(l);
    }

    @NotNull
    String name();

    @NotNull
    KonfigurationBuilder add(@NotNull Collection<KonfigurationManager<?>> konfig);


    @NotNull
    KonfigurationBuilder fairLock(boolean fair);

    @NotNull
    KonfigurationBuilder lockWaitTime(@Range(from = 0,
            to = Long.MAX_VALUE) long waitTime);

    @NotNull
    KonfigurationBuilder lockNoWait();


    @NotNull
    KonfigurationManager<?> build();

}
