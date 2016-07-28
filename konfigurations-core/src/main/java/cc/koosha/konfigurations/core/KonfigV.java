package cc.koosha.konfigurations.core;


public interface KonfigV<T> {

    KonfigV<T> deRegister(KeyObserver observer);

    KonfigV<T> register(KeyObserver observer);

    KonfigV<T> deRegister(SimpleObserver observer);

    KonfigV<T> register(SimpleObserver observer);

    KonfigV<T> registerAndCall(KeyObserver observer);

    KonfigV<T> registerAndCall(SimpleObserver observer);





    String key();

    T v();

    T v(T defaultValue);

}
