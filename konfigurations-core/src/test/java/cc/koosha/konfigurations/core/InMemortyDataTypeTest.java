package cc.koosha.konfigurations.core;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;


public class InMemortyDataTypeTest {

    private KonfigurationKombiner konfig;
    private Konfiguration inMem;
    private Map<String, Object> konfigMap;

    @BeforeMethod
    public void setup() {

        this.konfigMap = new HashMap<>();
        this.konfigMap.put("str key", "i'm string");
        this.konfigMap.put("me int", 42);

        this.inMem = new InMemoryKonfiguration(new InMemoryKonfiguration.KonfigMapProvider() {
            @Override
            public Map<String, Object> get() {
                return InMemortyDataTypeTest.this.konfigMap;
            }
        });
        this.konfig = new KonfigurationKombiner(this.inMem);
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class)
    public void testDataTypeInt() throws Exception {

        konfig.int_("str key");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class)
    public void testDataTypeString() throws Exception {

        konfig.string("me int");
    }

}
