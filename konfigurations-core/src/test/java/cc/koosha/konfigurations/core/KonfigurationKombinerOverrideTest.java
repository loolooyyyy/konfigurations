package cc.koosha.konfigurations.core;

import cc.koosha.konfigurations.core.impl.InMemoryKonfiguration;
import cc.koosha.konfigurations.core.impl.KonfigurationKombiner;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;


public class KonfigurationKombinerOverrideTest extends KonfigurationKombinerBaseTest {

    private KonfigurationKombiner konfig;
    private Konfiguration inMem;
    private Map<String, Object> konfigMap;

    @BeforeMethod
    public void setup() {

        konfigMap = new HashMap<>();
        konfigMap.put("str key", str1);
        konfigMap.put("list key", l1);

        inMem = new InMemoryKonfiguration(new InMemoryKonfiguration.KonfigMapProvider() {
            @Override
            public Map<String, Object> get() {
                return KonfigurationKombinerOverrideTest.this.konfigMap;
            }
        });
        konfig = new KonfigurationKombiner(this.inMem, super.dummyKonfig);
    }


    @Test
    public void testOverride() throws Exception {

        assertEquals(konfig.string("str key").v(), str1);
        assertEquals(konfig.int_("int key").v(), (Integer) 42);
        assertEquals(konfig.list("list key", int.class).v(), l1);
    }

}