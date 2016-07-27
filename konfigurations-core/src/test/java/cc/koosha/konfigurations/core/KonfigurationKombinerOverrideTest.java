package cc.koosha.konfigurations.core;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.*;


public class KonfigurationKombinerOverrideTest extends KonfigurationKombinerBaseTest {

    private KonfigurationKombiner konfig;
    private Konfiguration inMem;
    private Map<String, Object> konfigMap;

    @BeforeMethod
    public void setup() {

        konfigMap =  map(
            "str key", str1,
            "list key", l1
        );
        inMem = new InMemoryKonfiguration(() -> this.konfigMap);
        konfig = new KonfigurationKombiner(this.inMem, super.dummyKonfig);
    }


    @Test
    public void testOverride() throws Exception {

        assertEquals(konfig.string("str key").v(), str1);
        assertEquals(konfig.int_("int key").v(), (Integer) 42);
        assertEquals(konfig.list("list key", int.class).v(), l1);
    }

}