package cc.koosha.konfigurations.core.impl;


public interface Converter<F, T> {

    T apply(final F f);

}
