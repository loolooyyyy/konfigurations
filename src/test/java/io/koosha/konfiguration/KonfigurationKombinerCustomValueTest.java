package io.koosha.konfiguration;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

@SuppressWarnings("WeakerAccess")
public class KonfigurationKombinerCustomValueTest {

    final DummyCustom value = new DummyCustom();

    Faktory fac;

    final String key = "theKey";

    private Konfiguration k;

    @BeforeMethod
    public void setup() {
        this.k = fac.kombine(fac.map("map-konf", () -> Collections.singletonMap(
                key,
                value))).getAndSetToNull();
        this.fac = Faktory.defaultImplementation();
    }

    @Test
    public void testCustomValue() {
        K<DummyCustom> custom = k.custom(new Q<>(key, DummyCustom.class));
        Assert.assertSame(custom.v(), value);
    }

}
