package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.KonfigV;
import cc.koosha.konfiguration.SupplierX;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

public class KonfigurationKombinerCustomValueTest {

    final DummyCustom value = new DummyCustom();

    final String key = "theKey";

    private KonfigurationKombiner k = new KonfigurationKombiner(new InMemoryKonfigSource(new SupplierX<Map<String, Object>>() {
        @Override
        public Map<String, Object> get() {
            return Collections.<String, Object>singletonMap(key, value);
        }
    }));

    @Test
    public void testCustomValue() {

        KonfigV<DummyCustom> custom = k.custom(key, DummyCustom.class);
        Assert.assertSame(custom.v(), value);
    }

}
