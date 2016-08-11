package cc.koosha.konfigurations.core;


public interface Converter<F, T> {

    T apply(final F f);

}
