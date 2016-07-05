package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.KonfigV;
import cc.koosha.konfigurations.core.Konfiguration;
import cc.koosha.konfigurations.core.KonfigurationMissingKeyException;
import lombok.NonNull;

import java.util.*;


public final class KonfigurationCombiner implements Konfiguration {

    private final List<Konfiguration> konfigs;


    public KonfigurationCombiner(@NonNull final Collection<Konfiguration> konfigs) {

        this.konfigs = new ArrayList<>(konfigs.size());
        this.konfigs.addAll(konfigs);
    }

    public KonfigurationCombiner(@NonNull final Konfiguration... konfigs) {

        this.konfigs = new ArrayList<>(konfigs.length);
        Collections.addAll(this.konfigs, konfigs);
    }


    @Override
    public KonfigV<Boolean> bool(final String key) {

        for (final Konfiguration konfig : this.konfigs)
            try {
                return konfig.bool(key);
            }
            catch (final KonfigurationMissingKeyException e) {
                // try next
            }

        throw new KonfigurationMissingKeyException(key);
    }

    @Override
    public KonfigV<Integer> int_(final String key) {

        for (final Konfiguration konfig : this.konfigs)
            try {
                return konfig.int_(key);
            }
            catch (final KonfigurationMissingKeyException e) {
                // try next
            }

        throw new KonfigurationMissingKeyException(key);
    }

    @Override
    public KonfigV<Long> long_(final String key) {

        for (final Konfiguration konfig : this.konfigs)
            try {
                return konfig.long_(key);
            }
            catch (final KonfigurationMissingKeyException e) {
                // try next
            }

        throw new KonfigurationMissingKeyException(key);
    }

    @Override
    public KonfigV<String> string(final String key) {

        for (final Konfiguration konfig : this.konfigs)
            try {
                return konfig.string(key);
            }
            catch (final KonfigurationMissingKeyException e) {
                // try next
            }

        throw new KonfigurationMissingKeyException(key);
    }

    @Override
    public <T> KonfigV<List<T>> list(final String key, final Class<T> type) {

        for (final Konfiguration konfig : this.konfigs)
            try {
                return konfig.list(key, type);
            }
            catch (final KonfigurationMissingKeyException e) {
                // try next
            }

        throw new KonfigurationMissingKeyException(key);
    }

    @Override
    public <T> KonfigV<Map<String, T>> map(final String key, final Class<T> type) {

        for (final Konfiguration konfig : this.konfigs)
            try {
                return konfig.map(key, type);
            }
            catch (final KonfigurationMissingKeyException e) {
                // try next
            }

        throw new KonfigurationMissingKeyException(key);
    }


    @Override
    public <T> KonfigV<T> custom(final String key, final Class<T> clazz) {

        for (final Konfiguration konfig : this.konfigs)
            try {
                return konfig.custom(key, clazz);
            }
            catch (final KonfigurationMissingKeyException e) {
                // try next
            }

        throw new KonfigurationMissingKeyException(key);
    }

}
