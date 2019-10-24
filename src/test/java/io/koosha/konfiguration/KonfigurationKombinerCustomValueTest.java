package io.koosha.konfiguration;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;


public class KonfigurationKombinerCustomValueTest {

    final DummyCustom value = new DummyCustom();

    final String key = "theKey";

    private KonfigurationKombiner k = new KonfigurationKombiner(new InMemoryKonfigSource(() -> Collections.singletonMap(
            key,
            value)));

    @Test
    public void testCustomValue() {

        K<DummyCustom> custom = k.custom(key, DummyCustom.class);
        Assert.assertSame(custom.v(), value);
    }

}
