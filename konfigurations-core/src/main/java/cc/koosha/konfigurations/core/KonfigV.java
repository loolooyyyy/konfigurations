package cc.koosha.konfigurations.core;


public interface KonfigV<T> {

    KonfigV<T> deRegister(KeyObserver observer);

    KonfigV<T> register(KeyObserver observer);

    String key();

    T v();

    T v(T defaultValue);

}
