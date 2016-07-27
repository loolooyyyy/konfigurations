package cc.koosha.konfigurations.core;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertThrows;


public class InMemoryKonfigurationTest {

    private KonfigurationKombiner konfig;
    private Konfiguration inMem;
    private Map<String, Object> konfigMap;

    @BeforeMethod
    public void setup() {

        konfigMap = new HashMap<>();
        konfigMap.put("str key", "i'm string");
        konfigMap.put("me int", 42);

        inMem = new InMemoryKonfiguration(() -> this.konfigMap);
        konfig = new KonfigurationKombiner(this.inMem);
    }

    @Test
    public void testDataTypeInt(final String key) throws Exception {

        assertThrows(KonfigurationBadTypeException.class,
                () -> konfig.int_("str key"));
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class)
    public void testDataTypeString(final String key) throws Exception {

        assertThrows(KonfigurationBadTypeException.class,
                () -> konfig.string("me int"));
    }


}