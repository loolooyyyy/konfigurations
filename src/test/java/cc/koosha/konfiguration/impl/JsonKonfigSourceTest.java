package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.SupplierX;
import lombok.val;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;


@SuppressWarnings("Duplicates")
public class JsonKonfigSourceTest {

    private String       json;
    private String       json0;
    private String       json1;
    private JsonKonfigSource k;

    @BeforeClass
    public void classSetup() throws Exception {

        val url0 = getClass().getResource("sample0.json");
        val url1 = getClass().getResource("sample1.json");

        this.json0 = new Scanner(new File(url0.toURI()), "UTF8").useDelimiter("\\Z")
                                                                .next();
        this.json1 = new Scanner(new File(url1.toURI()), "UTF8").useDelimiter("\\Z")
                                                                .next();
    }

    @BeforeMethod
    public void setup() throws Exception {

        json = json0;
        this.k = new JsonKonfigSource(new SupplierX<String>() {
            @Override
            public String get() {
                return json;
            }
        });
    }

    private void update() {

        json = json1;
        k = (JsonKonfigSource) k.copyAndUpdate();
    }

    @Test
    public void testNotUpdatable() throws Exception {

        assertFalse(k.isUpdatable());
    }

    @Test
    public void testUpdatable() throws Exception {

        json = json1;
        assertTrue(k.isUpdatable());
    }

    // GOOD CASES

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
    public void testStringConcat() throws Exception {

        assertEquals(k.string("aStringList"), "aBc");
        update();
        assertEquals(k.string("aStringList"), "ac");
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
        final Map<String, String>  after  = new HashMap<>(2);
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

    @Test
    public void testNestedKeyOfPrimitive() throws Exception {

        assertEquals(k.int_("some.nested.key"), Integer.valueOf(99));
    }

    @Test
    public void testNestedKeyOfCustom() throws Exception {

        final DummyCustom custom =
                k.custom("some.nested.userDefined", DummyCustom.class);

        assertEquals(custom.concat(), "I'm all set ::: 99");
    }

}
