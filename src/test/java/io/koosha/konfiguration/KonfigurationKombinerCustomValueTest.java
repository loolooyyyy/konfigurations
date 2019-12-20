package io.koosha.konfiguration;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;


@SuppressWarnings("WeakerAccess")
public class KonfigurationKombinerCustomValueTest {

    final DummyCustom value = new DummyCustom();

    final Faktory fac = Faktory.def();

    final String key = "theKey";

    private Konfiguration k = fac.kombine(fac.map("map-konf", () -> Collections.singletonMap(
            key,
            value)));

    @Test
    public void testCustomValue() {
        K<DummyCustom> custom = k.custom(key, Q.of(DummyCustom.class));
        Assert.assertSame(custom.v(), value);
    }

}
