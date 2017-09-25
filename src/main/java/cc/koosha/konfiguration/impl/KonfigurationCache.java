package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.KonfigV;


/**
 * A mechanism for caching values obtained from configuration sources.
 * <p>
 * <b>This interface is ONLY used for extending konfiguration sources or
 * implementing a new one. it must NOT be used by clients.</b>
 * <p>
 * Thread-safe, except for {@link #update()} (read it's comment for more info).
 */
interface KonfigurationCache {

    /**
     * Populate cache for a key.
     * <p>
     * Thread-safe.
     *
     * @param key key of the konfiguration being populated.
     * @return If key was found in any of sources (key exists).
     */
    boolean create(KonfigKey key);

    /**
     * Get cached value of a konfiguration key.
     * <p>
     * Thread-safe
     *
     * @param key       key of the konfiguration being obtained.
     * @param def       default value if the key did not exist.
     * @param mustExist whether to throw an exception if the desired key was not
     *                  found in any source
     * @param <T>       type of the konfiguration being obtained.
     *
     * @return the konfiguration value of the provided key.
     */
    <T> T v(KonfigKey key, T def, boolean mustExist);

    /**
     * Update the cache
     * <p>
     * <b>NOT</b> thread-safe.
     * <p>
     * Important: This method is not thread-safe, but does not affect
     * thread-safety of other methods in this class. That is, it must not be
     * called from multiple threads, but it doesn't matter if it's invoked
     * concurrently with it.
     * <p>
     * Important: the key observers will might be notified <em>after</em> the
     * cache is updated based on the implementation. So it's possible that a
     * call to {@link KonfigV#v()} returns the new value, while the observer is
     * not notified yet.
     * <p>
     * Important: the order of calling everything observers and key observers is
     * implementation specific.
     *
     * @return if this action changed anything in the cache (any update took
     * place).
     */
    boolean update();

}
