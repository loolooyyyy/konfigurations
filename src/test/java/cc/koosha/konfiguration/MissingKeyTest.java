package cc.koosha.konfiguration;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static java.util.Collections.singletonMap;


public class MissingKeyTest {


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
    public void testMissingKeyNotRaisedUntilVIsNotCalled() {

        k.string("i.do.not.exist");
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void testMissingKey() {

        k.string("i.do.not.exist").v();
    }

}