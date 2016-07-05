package cc.koosha.konfigurations.core;

import java.util.function.Consumer;


public interface KonfigV<T> {

    KonfigV<T> register(Consumer<String> observer);

    KonfigV<T> callAndRegister(Consumer<String> observer);

    String key();

    T v();

}
