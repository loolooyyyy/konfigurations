package io.koosha.konfiguration;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

import static java.util.Collections.emptyList;


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
        K<DummyCustom> custom = k.custom(key, new Q<U>((String) DummyCustom.class, (Class<U>) null, emptyList()) {});
        Assert.assertSame(custom.v(), value);
    }

}
