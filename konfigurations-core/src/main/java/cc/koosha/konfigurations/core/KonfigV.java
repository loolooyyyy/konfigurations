package cc.koosha.konfigurations.core;

import java.util.function.Consumer;


public interface KonfigV<T> {

    KonfigV<T> deRegister(Consumer<String> observer);

    KonfigV<T> register(Consumer<String> observer);

    String key();

    T v();

    T v(T defaultValue);

}
