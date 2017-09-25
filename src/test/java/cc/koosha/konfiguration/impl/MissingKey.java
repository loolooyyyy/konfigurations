package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.KonfigSource;
import cc.koosha.konfiguration.SupplierX;
import lombok.val;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;


public class MissingKey {

    private Map<String, Object> map;
    private Map<String, Object> map0;
    private KonfigSource k;

    @BeforeClass
    public void classSetup() throws Exception {

        this.map0 = new HashMap<>();

        map0.put("aInt", 12);
        map0.put("aBool", true);
        map0.put("aIntList", asList(1, 0, 2));
        map0.put("aStringList", asList("a", "B", "c"));
        map0.put("aLong", Long.MAX_VALUE);
        map0.put("aDouble", 3.14D);
        map0.put("aString", "hello world");

        val m0 = new HashMap<Object, Object>();
        m0.put("a", 99);
        m0.put("c", 22);
        map0.put("aMap", m0);

        val s0 = new HashSet<Integer>();
        s0.addAll(asList(1, 2));
        map0.put("aSet", s0);
        this.map0 = Collections.unmodifiableMap(this.map0);

        // --------------

        Map<String, Object> map1 = new HashMap<>();

        map1.put("aInt", 99);
        map1.put("aBool", false);
        map1.put("aIntList", asList(2, 2));
        map1.put("aStringList", asList("a", "c"));
        map1.put("aLong", Long.MIN_VALUE);
        map1.put("aDouble", 4.14D);
        map1.put("aString", "goodbye world");

        val m1 = new HashMap<Object, Object>();
        m1.put("a", "b");
        m1.put("c", "e");
        map1.put("aMap", m1);

        val s1 = new HashSet<Integer>();
        s1.addAll(asList(1, 2, 3));
        map1.put("aSet", s1);

        map1 = Collections.unmodifiableMap(map1);
    }

    @BeforeMethod
    public void setup() throws Exception {

        map = map0;
        this.k = new InMemoryKonfigSource(new SupplierX<Map<String, Object>>() {
            @Override
            public Map<String, Object> get() {
                return map;
            }
        });
    }

    @Test
    public void testBool() throws Exception {

        assertEquals(k.bool("aBool"), Boolean.TRUE);
    }
}
