package io.koosha.konfiguration;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.function.Supplier;

import static java.util.Collections.singletonMap;

@SuppressWarnings("WeakerAccess")
public class KonfigurationKombinerCustomValueTest {

    final DummyCustom value = new DummyCustom();
    final String key = "theKey";
    Faktory fac;
    private Konfiguration k;

    @BeforeMethod
    public void setup() {
        fac = Faktory.defaultImplementation();
        final Supplier<Map<String, ?>> mapSupplier = () -> singletonMap(key, value);
        final KonfigurationManager map = fac.map("map-konf", mapSupplier);
        final KonfigurationManager kombine = fac.kombine(map);
        this.k = kombine.getAndSetToNull();
        this.fac = Faktory.defaultImplementation();
    }

    @Test
    public void testCustomValue() {
        K<DummyCustom> custom = k.custom(key, DummyCustom.class);
        Assert.assertSame(custom.v(), value);
    }

}
