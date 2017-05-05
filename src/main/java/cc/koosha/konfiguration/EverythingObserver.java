package cc.koosha.konfiguration;


/**
 * Konfiguration observer which observe any change in the konfiguration source
 * (not just a specific key).
 */
public interface EverythingObserver {

    /**
     * Called when the konfiguration is changed (updated).
     */
    void accept();

}
