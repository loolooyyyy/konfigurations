package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.*;
import lombok.NonNull;
import lombok.val;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;


public final class KonfigurationKombiner implements Konfiguration {

    public KonfigurationKombiner(@NonNull final Konfiguration... sources) {

        this(Arrays.asList(sources));
    }

    public KonfigurationKombiner(@NonNull final Collection<Konfiguration> sources) {

        this.sources = new ArrayList<>(sources);
    }

    // _________________________________________________________________________

    @Override
    public final KonfigV<Boolean> bool(@NonNull final String key) {

        return new KonfigVImpl<>(key, KonfigDataType.BOOLEAN, null);
    }

    @Override
    public final KonfigV<Integer> int_(@NonNull final String key) {

        return new KonfigVImpl<>(key, KonfigDataType.INT, null);
    }

    @Override
    public final KonfigV<Long> long_(@NonNull final String key) {

        return new KonfigVImpl<>(key, KonfigDataType.LONG, null);
    }

    @Override
    public final KonfigV<String> string(@NonNull final String key) {

        return new KonfigVImpl<>(key, KonfigDataType.STRING, null);
    }

    @Override
    public final <T> KonfigV<List<T>> list(@NonNull final String key,
                                           @NonNull final Class<T> type) {

        return new KonfigVImpl<>(key, KonfigDataType.LIST, type);
    }

    @Override
    public final <T> KonfigV<Map<String, T>> map(@NonNull final String key,
                                                 @NonNull final Class<T> type) {

        return new KonfigVImpl<>(key, KonfigDataType.MAP, type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> KonfigV<T> custom(@NonNull final String key,
                                       final Class<T> type) {

        if(Boolean.class.equals(type))
            return (KonfigV<T>) this.bool(key);

        if(Integer.class.equals(type))
            return (KonfigV<T>) this.int_(key);

        if(Long.class.equals(type))
            return (KonfigV<T>) this.long_(key);

        if(String.class.equals(type))
            return (KonfigV<T>) this.string(key);

        return new KonfigVImpl<>(key, KonfigDataType.CUSTOM, type);
    }

    // @TODO notify each observer in a separate thread?
    public final boolean update() {

        boolean up = false;
        for (final Konfiguration source : this.sources)
            up |= source.update();
        if(!up)
            return false;

        final Set<String> updatedKeys = new HashSet<>();
        for (val konfigV : new HashMap<>(this.values).keySet())
            if(konfigV.update())
                updatedKeys.add(konfigV.key());

        if(updatedKeys.size() == 0)
            return false;

        final Map<KeyObserver, Collection<String>> copy;
        val lock = this.observersLock.readLock();
        try {
            lock.lock();
            copy = new HashMap<>(this.observers);
        }
        finally {
            lock.unlock();
        }

        for (final String updatedKey : updatedKeys)
            for (val each : copy.entrySet())
                if (each.getValue().contains(updatedKey))
                    each.getKey().accept(updatedKey);

        return true;
    }

    @Override
    public Konfiguration subset(final String key) {

        return new SubsetKonfiguration(this, key);
    }


    // _________________________________________________________________________

    private final ReadWriteLock observersLock
            = new ReentrantReadWriteLock();

    private final Map<KeyObserver, Collection<String>> observers
            = new WeakHashMap<>();

    private final Map<KonfigurationKombiner.KonfigVImpl, Void> values
            = Collections.synchronizedMap(new WeakHashMap<KonfigurationKombiner.KonfigVImpl, Void>());

    private void addObserver(final String key, final KeyObserver observer) {

        final Collection<String> interestedIn;
        val wLock = this.observersLock.writeLock();

        try {
            wLock.lock();
            if (!observers.containsKey(observer))
                observers.put(observer, new HashSet<String>());
            interestedIn = observers.get(observer);
        }
        finally {
            wLock.unlock();
        }

        interestedIn.add(key);
    }

    private void removeObserver(final String key, final KeyObserver observer) {

        final Collection<String> keys;

        val rLock = this.observersLock.readLock();
        try {
            rLock.lock();
            keys = observers.get(observer);
        }
        finally {
            rLock.unlock();
        }

        if(keys == null)
            return;

        keys.remove(key);

        if(keys.size() == 0) {
            val wLock = this.observersLock.writeLock();
            try {
                wLock.lock();
                observers.remove(observer);
            }
            finally {
                wLock.unlock();
            }
        }
    }


    private final Collection<Konfiguration> sources;

    private Object fetch(final String key,
                         final KonfigDataType dataType,
                         final Class<?> el) {

        for (val source : sources)
            try {
                switch (dataType) {
                    case BOOLEAN:
                        return source.bool(key).v();

                    case INT:
                        return source.int_(key).v();

                    case LONG:
                        return source.long_(key).v();

                    case STRING:
                        return source.string(key).v();

                    case LIST:
                        return source.list(key, el).v();

                    case MAP:
                        return source.map(key, el).v();

                    case CUSTOM:
                        return source.custom(key, el).v();

                    default:
                        // shouldn't happen
                        throw new RuntimeException("bad type: " + dataType);
                }
            }
            catch (final KonfigurationMissingKeyException e) {
                // try next source
            }

        throw new KonfigurationMissingKeyException(key);
    }

    private final class KonfigVImpl<T> implements KonfigV<T> {

        private final String key;
        private final KonfigDataType dataType;
        private final Class<?> el;

        // volatile? see this.update()
        private T value;
        private boolean hasValue;

        private KonfigVImpl(final String key,
                            final KonfigDataType dataType,
                            final Class<?> el) {

            this.key = key;
            this.dataType = dataType;
            this.el = el;

            values.put(this, null);

            this.hasValue = false;
            this.update();
        }

        /**
         * Thread safety measures:
         *
         * When removing this.value (setting it to null), this.hasValue is set
         * to false beforehand. This way unwanted null values wont be handed to
         * clients by this.v() or this.v(defaultValue)
         *
         * When adding a value (setting a previously null this.value),
         * this.hasValue is set *afterwards*. This way clients wont get an
         * unwanted null value until this.value is fully set.
         *
         * Nothing more, no volatile, sync or locks.
         *
         * A note on compiler optimization:
         * Access to this.value and this.hasValue might not be in the order
         * discussed above. If this causes any problem they should be made volatile.
         *
         * @return true if the value was actually changed.
         */
        boolean update() {

            T updatedValue;
            boolean gotValue;

            try {
                @SuppressWarnings("unchecked")
                final T uv = (T) fetch(this.key, this.dataType, this.el);
                updatedValue = uv;
                gotValue = true;
            }
            catch (final KonfigurationMissingKeyException e) {
                updatedValue = null;
                gotValue = false;
            }

            // value removed
            if(!gotValue && this.hasValue) {
                // set hasValue first, as we don't sync and lock, it's better
                // this way.
                this.hasValue = false;
                this.value = null;
                return true;
            }
            else if(gotValue) {
                // no change
                if(this.hasValue && Objects.equals(updatedValue, this.value)) {
                    return false;
                }
                // value updated or added
                else {
                    // set value first, as we don't sync and lock, it's better
                    // this way.
                    this.value = updatedValue;
                    this.hasValue = true;
                    return true;
                }
            }
            else {
                return false;
            }
        }

        @Override
        public KonfigV<T> deRegister(final KeyObserver observer) {

            removeObserver(this.key, observer);
            return this;
        }

        @Override
        public KonfigV<T> register(@NonNull final KeyObserver observer) {

            addObserver(this.key, observer);
            return this;
        }

        @Override
        public KonfigV<T> deRegister(final SimpleObserver observer) {

            addObserver(this.key, new SimpleObserverWrapper(observer));
            return this;
        }

        @Override
        public KonfigV<T> register(final SimpleObserver observer) {

            addObserver(this.key, new SimpleObserverWrapper(observer));
            return this;
        }

        @Override
        public KonfigV<T> registerAndCall(final KeyObserver observer) {

            addObserver(this.key, observer);
            observer.accept(this.key);
            return this;
        }

        @Override
        public KonfigV<T> registerAndCall(final SimpleObserver observer) {

            addObserver(this.key, new SimpleObserverWrapper(observer));
            observer.accept();
            return this;
        }

        @Override
        public String key() {

            return this.key;
        }

        @Override
        public T v() {

            // Thread safety: see this.update()

            if(!hasValue)
                throw new KonfigurationMissingKeyException(this.key);

            return this.value;
        }

        @Override
        public T v(final T defaultValue) {

            // Thread safety: see this.update()

            return this.hasValue ? this.value : defaultValue;
        }


        // ---------------------------------------------------------------------

        private Konfiguration origin() {

            return KonfigurationKombiner.this;
        }

        @Override
        public String toString() {

            return format("Config(%s=%s)", this.key, this.value);
        }

        @Override
        public boolean equals(final Object o) {

            if (o == this)
                return true;

            if (!(o instanceof KonfigVImpl))
                return false;

            final KonfigVImpl other = (KonfigVImpl) o;

            return Objects.equals(this.origin(), other.origin()) &&
                    Objects.equals(this.key, other.key);
        }

        @Override
        public int hashCode() {

            final int PRIME = 59;
            int result = 1;

            result = result * PRIME + this.origin().hashCode();
            result = result * PRIME + this.key.hashCode();

            return result;
        }

    }

    private static final class SimpleObserverWrapper implements KeyObserver {

        private final SimpleObserver wrapped;

        private SimpleObserverWrapper(final SimpleObserver wrapped) {

            this.wrapped = wrapped;
        }

        @Override
        public void accept(final String key) {

            this.wrapped.accept();
        }
    }

}
