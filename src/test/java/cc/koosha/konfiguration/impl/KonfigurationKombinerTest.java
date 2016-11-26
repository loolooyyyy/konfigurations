package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.KonfigurationBadTypeException;
import cc.koosha.konfiguration.KonfigurationMissingKeyException;
import lombok.val;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;


public final class KonfigurationKombinerTest {

    private Map<String, Object> mapA;
    private Map<String, Object> map0;
    private Map<String, Object> map1;
    private Map<String, Object> mapB;

    private KonfigurationKombiner k;

    @BeforeClass
    public void classSetup() throws Exception {

        this.map0 = new HashMap<>();
        map0.put("aInt", 12);
        map0.put("aBool", false);
        map0.put("aIntList", asList(1, 0, 2));
        map0.put("aLong", 88L);
        this.map0 = Collections.unmodifiableMap(this.map0);

        // --------------

        this.map1 = new HashMap<>();
        map1.put("aInt", 99);
        map1.put("bBool", false);
        map1.put("aIntList", asList(2, 2));
        this.map1 = Collections.unmodifiableMap(this.map1);

        // --------------

        this.mapB = new HashMap<>();
        mapB.put("xx", 44);
        mapB.put("yy", true);
        this.mapB = Collections.unmodifiableMap(this.mapB);
    }

    private void reset() {

        mapA = map0;
        val inMemA = new InMemoryKonfigSource(new InMemoryKonfigSource.KonfigMapProvider() {
            @Override
            public Map<String, Object> get() {
                return mapA;
            }
        });

        val inMemB = new InMemoryKonfigSource(new InMemoryKonfigSource.KonfigMapProvider() {
            @Override
            public Map<String, Object> get() {
                return mapB;
            }
        });

        this.k = new KonfigurationKombiner(inMemA, inMemB);
    }

    @BeforeMethod
    public void setup() throws Exception {

        reset();
    }

    private void update() {

         mapA = mapA == map0 ?  map1 : map0;
    }


    @Test
    public void testV1() throws Exception {

        assertEquals(k.int_("aInt").v(), (Integer) 12);
        update();
        k.update();
        assertEquals(k.int_("aInt").v(), (Integer) 99);
    }

    @Test
    public void testV2() throws Exception {

        val aInt = k.int_("aInt");
        assertEquals(aInt.v(), (Integer) 12);
        update();
        k.update();
        assertEquals(aInt.v(), (Integer) 99);
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class)
    @SuppressWarnings("unused")
    public void testV3() throws Exception {

        val aInt = k.string("aInt");
    }


    @Test
    @SuppressWarnings("unused")
    public void testLongOfInt() {

        val l0 = k.long_("aLong");
        assertEquals(l0.v(), (Long) 88L);

        reset();

        val l1 = k.int_("aInt");
        val l2 = k.long_("aInt");
        assertEquals(l2.v(), (Long) 12L);
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void testNoDefaultValue() {

        k.longD("someblablabla").v();
    }

    @Test
    public void testDefaultValue() {

        final long v = k.longD("someblablabla").v(9876L);
        assertEquals(v, 9876L);
    }

}
