package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.SupplierX;
import lombok.Getter;
import lombok.val;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.util.Arrays.asList;


public abstract class InMemTestBase extends KonfigValueTestMixin {

    protected Map<String, Object> map;
    protected Map<String, Object> map0;
    protected Map<String, Object> map1;

    @Getter
    private InMemoryKonfigSource k;

    @BeforeClass
    public void classSetup() throws Exception {

        this.map0 = new HashMap<>();

        this.map0.put("aInt", 12);
        this.map0.put("aBool", true);
        this.map0.put("aIntList", asList(1, 0, 2));
        this.map0.put("aStringList", asList("a", "B", "c"));
        this.map0.put("aLong", Long.MAX_VALUE);
        this.map0.put("aDouble", 3.14D);
        this.map0.put("aString", "hello world");

        val m0 = new HashMap<Object, Object>();
        m0.put("a", 99);
        m0.put("c", 22);
        this.map0.put("aMap", m0);

        val s0 = new HashSet<Integer>();
        s0.addAll(asList(1, 2));
        this.map0.put("aSet", s0);
        this.map0 = Collections.unmodifiableMap(this.map0);

        // --------------

        this.map1 = new HashMap<>();

        this.map1.put("aInt", 99);
        this.map1.put("aBool", false);
        this.map1.put("aIntList", asList(2, 2));
        this.map1.put("aStringList", asList("a", "c"));
        this.map1.put("aLong", Long.MIN_VALUE);
        this.map1.put("aDouble", 4.14D);
        this.map1.put("aString", "goodbye world");

        val m1 = new HashMap<Object, Object>();
        m1.put("a", "b");
        m1.put("c", "e");
        this.map1.put("aMap", m1);

        val s1 = new HashSet<Integer>();
        s1.addAll(asList(1, 2, 3));
        this.map1.put("aSet", s1);

        this.map1 = Collections.unmodifiableMap(this.map1);
    }

    @BeforeMethod
    public void setup() throws Exception {

        this.map = this.map0;
        this.k = new InMemoryKonfigSource(new SupplierX<Map<String, Object>>() {
            @Override
            public Map<String, Object> get() {
                return map;
            }
        });
    }

    @Override
    protected void update() {

        this.map = this.map1;
        this.k = (InMemoryKonfigSource) this.k.copyAndUpdate();
    }

}