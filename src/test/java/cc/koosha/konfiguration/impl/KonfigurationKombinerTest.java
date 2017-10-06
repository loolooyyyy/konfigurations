package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.KonfigurationBadTypeException;
import cc.koosha.konfiguration.KonfigurationMissingKeyException;
import cc.koosha.konfiguration.SupplierX;
import lombok.val;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;


public final class KonfigurationKombinerTest {

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

    @Test
    public void testV1() throws Exception {

        assertEquals(k.int_("xxx").v(), (Integer) 12);

        returnFourTaee = !returnFourTaee;
        k.update();

        assertEquals(k.int_("xxx").v(), (Integer) 99);
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class)
    public void testV3() throws Exception {

        k.string("xxx");
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void testNoDefaultValue() {

        k.long_("someblablabla").v();
    }

    @Test
    public void testDefaultValue() {

        assertEquals(k.long_("someblablabla").v(9876L), (Long) 9876L);
    }

}
