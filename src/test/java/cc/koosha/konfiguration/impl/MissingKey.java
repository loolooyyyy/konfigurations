package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.KonfigurationMissingKeyException;
import cc.koosha.konfiguration.SupplierX;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static java.util.Collections.singletonMap;


public class MissingKey {


    private boolean returnFourTaee = true;

    private final SupplierX<Map<String, Object>> sup = new SupplierX<Map<String, Object>>() {
        @Override
        public Map<String, Object> get() {
            return returnFourTaee
                   ? singletonMap("xxx", (Object) 12)
                   : singletonMap("xxx", (Object) 99);
        }
    };

    private KonfigurationKombiner k;

    @BeforeMethod
    public void setup() {

        this.returnFourTaee = true;
        this.k = new KonfigurationKombiner(new InMemoryKonfigSource(sup));
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class,
            enabled = false)
    public void testMissingKey() {

        k.string("i.do.not.exist");
    }

}
