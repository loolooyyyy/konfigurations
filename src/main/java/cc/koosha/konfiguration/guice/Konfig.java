package cc.koosha.konfiguration.guice;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Documented
@Retention(RUNTIME)
public @interface Konfig {

    String value();

}
