package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.KonfigSource;
import cc.koosha.konfigurations.core.KonfigurationBadTypeException;
import lombok.val;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;


public class InMemoryKonfigSourceTest {

    private Map<String, Object> map;
    private Map<String, Object> map0;
    private Map<String, Object> map1;
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

        this.map1 = new HashMap<>();

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

        this.map1 = Collections.unmodifiableMap(this.map1);
    }

    @BeforeMethod
    public void setup() throws Exception {

        map = map0;
        this.k = new InMemoryKonfigSource(new InMemoryKonfigSource.KonfigMapProvider() {
            @Override
            public Map<String, Object> get() {
                return map;
            }
        });
    }

    private void update() {

        map = map1;
        k = k.copy();
    }

    @Test
    public void testNotUpdatable() throws Exception {

        assertFalse(k.isUpdatable());
    }

    @Test
    public void testUpdatable() throws Exception {

        map = map1;
        assertTrue(k.isUpdatable());
    }

    @Test
    public void testBool() throws Exception {

        assertEquals(k.bool("aBool"), Boolean.TRUE);
        update();
        assertEquals(k.bool("aBool"), Boolean.FALSE);
    }

    @Test
    public void testInt() throws Exception {

        assertEquals(k.int_("aInt"), Integer.valueOf(12));
        update();
        assertEquals(k.int_("aInt"), Integer.valueOf(99));
    }

    @Test
    public void testLong() throws Exception {

        assertEquals(k.long_("aLong"), (Object) Long.MAX_VALUE);
        update();
        assertEquals(k.long_("aLong"), (Object) Long.MIN_VALUE);
    }

    @Test
    public void testLongOfInt() throws Exception {

        assertEquals(k.long_("aInt"), (Object) 12L);
        update();
        assertEquals(k.long_("aInt"), (Object) 99L);
    }


    @Test
    public void testDouble() throws Exception {

        assertEquals(k.double_("aDouble"), 3.14);
        update();
        assertEquals(k.double_("aDouble"), 4.14);
    }

    @Test
    public void testString() throws Exception {

        assertEquals(k.string("aString"), "hello world");
        update();
        assertEquals(k.string("aString"), "goodbye world");
    }

    @Test
    public void testList() throws Exception {

        val before = k.list("aIntList", int.class);
        assertEquals(before, asList(1, 0, 2));

        update();

        val after = k.list("aIntList", int.class);
        assertEquals(after, asList(2, 2));
    }

    @Test
    public void testMap() throws Exception {

        // Not wise to change type, but it can happen.

        final Map<String, Integer> before = new HashMap<>(2);
        final Map<String, String> after = new HashMap<>(2);
        before.put("a", 99);
        before.put("c", 22);
        after.put("a", "b");
        after.put("c", "e");

        assertEquals(k.map("aMap", int.class), before);
        update();
        assertEquals(k.map("aMap", String.class), after);
    }

    @Test
    public void testSet() throws Exception {

        assertEquals(k.set("aSet", int.class), new HashSet<>(asList(1, 2)));
        update();
        assertEquals(k.set("aSet", int.class), new HashSet<>(asList(1, 2, 3)));
    }



    // BAD CASES


    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not an int.*")
    public void testBadInt0() throws Exception {

        k.int_("aBool");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not an int.*")
    public void testBadInt1() throws Exception {

        k.int_("aLong");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not an int.*")
    public void testBadInt2() throws Exception {

        k.int_("aString");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not an int.*")
    public void testBadInt3() throws Exception {

        k.int_("aDouble");
    }



    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not a double.*")
    public void testBadDouble0() throws Exception {

        k.double_("aBool");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not a double.*")
    public void testBadDouble1() throws Exception {

        k.double_("aLong");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not a double.*")
    public void testBadDouble() throws Exception {

        k.double_("aString");
    }



    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not a long.*")
    public void testBadLong0() throws Exception {

        k.long_("aBool");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not a long.*")
    public void testBadLong1() throws Exception {

        k.long_("aString");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not a long.*")
    public void testBadLong2() throws Exception {

        k.long_("aDouble");
    }


    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not a string.*")
    public void testBadString0() throws Exception {

        k.string("aInt");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not a string.*")
    public void testBadString1() throws Exception {

        k.string("aBool");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not a string.*")
    public void testBadString2() throws Exception {

        k.string("aIntList");
    }


    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not a list.*")
    public void testBadList0() throws Exception {

        k.list("aInt", Integer.class);
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          expectedExceptionsMessageRegExp = "not a list.*")
    public void testBadList1() throws Exception {

        k.list("aString", String.class);
    }
}