package cc.koosha.konfiguration;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.singletonMap;
import static org.testng.Assert.*;


public final class KonfigurationKombinerTest {

    private AtomicBoolean flag = new AtomicBoolean(true);

    private final SupplierX<Map<String, Object>> sup = new SupplierX<Map<String, Object>>() {
        @Override
        public Map<String, Object> get() {
            return flag.get()
                   ? singletonMap("xxx", (Object) 12)
                   : singletonMap("xxx", (Object) 99);
        }
    };

    private KonfigurationKombiner k;

    @BeforeMethod
    public void setup() {

        this.flag.set(true);
        this.k = new KonfigurationKombiner(new InMemoryKonfigSource(sup));
    }

    @Test
    public void testV1() throws Exception {

        assertEquals(k.int_("xxx").v(), (Integer) 12);

        flag.set(!flag.get());
        k.update();

        assertEquals(k.int_("xxx").v(), (Integer) 99);
    }

    @Test(expectedExceptions = KonfigurationTypeException.class)
    public void testV3() throws Exception {

        k.string("xxx");
    }


    @Test
    public void testDoublyUpdate() throws Exception {

        assertEquals(k.int_("xxx").v(), (Integer) 12);

        flag.set(!flag.get());
        assertTrue(k.update());
        assertFalse(k.update());

        assertEquals(k.int_("xxx").v(), (Integer) 99);

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
