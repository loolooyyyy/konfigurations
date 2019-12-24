package io.koosha.konfiguration.v8;


import io.koosha.konfiguration.Faktory;
import io.koosha.konfiguration.Konfiguration;
import io.koosha.konfiguration.KonfigurationManager;
import io.koosha.konfiguration.error.KfgMissingKeyException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.function.Supplier;

import static java.util.Collections.singletonMap;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class MissingKeyTest {

    Faktory fac;
    KonfigurationManager man;
    Konfiguration k;
    private boolean testAAA = true;
    final Supplier<Map<String, Object>> sup = () -> testAAA
                                                    ? singletonMap("xxx", (Object) 12)
                                                    : singletonMap("xxx", (Object) 99);

    @BeforeMethod
    public void setup() {
        fac = FaktoryV8.defaultInstance();
        this.testAAA = true;
        this.man = fac.map(getClass().getSimpleName(), sup::get);
        this.k = man.getAndSetToNull();
    }

    @Test
    public void testMissingKeyNotRaisedUntilVIsNotCalled() {
        k.string("i.do.not.exist");
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testMissingKey() {
        k.string("i.do.not.exist").v();
    }

}
