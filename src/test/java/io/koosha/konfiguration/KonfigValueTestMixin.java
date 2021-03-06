package io.koosha.konfiguration;

import io.koosha.konfiguration.error.KfgMissingKeyException;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class KonfigValueTestMixin {

    protected abstract Source k();

    protected abstract void update();


    @Test
    public void testBool() throws Exception {
        assertEquals(this.k().bool("aBool").v(), Boolean.TRUE);
        this.update();
        assertEquals(this.k().bool("aBool").v(), Boolean.FALSE);
    }

    @Test
    public void testInt() throws Exception {
        assertEquals(this.k().int_("aInt").v(), Integer.valueOf(12));
        this.update();
        assertEquals(this.k().int_("aInt").v(), Integer.valueOf(99));
    }

    @Test
    public void testLong() throws Exception {
        assertEquals(this.k().long_("aLong").v(), (Long) Long.MAX_VALUE);
        this.update();
        assertEquals(this.k().long_("aLong").v(), (Long) Long.MIN_VALUE);
    }

    @Test
    public void testDouble() throws Exception {
        assertEquals(this.k().double_("aDouble").v(), (Double) 3.14);
        this.update();
        assertEquals(this.k().double_("aDouble").v(), (Double) 4.14);
    }

    @Test
    public void testString() throws Exception {
        assertEquals(this.k().string("aString").v(), "hello world");
        this.update();
        assertEquals(this.k().string("aString").v(), "goodbye world");
    }

    @Test
    public void testList() throws Exception {
        final List<Integer> before = this.k().list("aIntList", Integer.class).v();
        assertEquals(before, asList(1, 0, 2));

        this.update();

        List<Integer> after = this.k().list("aIntList", int.class).v();
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

        final Map<String, Integer> aMap = this.k().map("aMap", String.class, int.class).v();
        assertEquals(aMap, before);
        this.update();

        final Map<String, String> aMap1 = this.k().map("aMap", String.class, String.class).v();
        assertEquals(aMap1, after);
    }

    @Test
    public void testSet() throws Exception {
        assertEquals(this.k().set("aSet", int.class).v(), new HashSet<>(asList(1, 2)));
        this.update();
        assertEquals(this.k().set("aSet", int.class).v(), new HashSet<>(asList(1, 2, 3)));
    }


    // =========================================================== FAILING CASES

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadInt0() throws Exception {
        this.k().int_("aBool");
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadInt1() throws Exception {
        this.k().int_("aLong");
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadInt2() throws Exception {
        this.k().int_("aString");
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadInt3() throws Exception {
        this.k().int_("aDouble");
    }


    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadDouble0() throws Exception {
        this.k().double_("aBool");
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadDouble() throws Exception {
        this.k().double_("aString");
    }


    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadLong0() throws Exception {
        this.k().long_("aBool");
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadLong1() throws Exception {
        this.k().long_("aString");
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadLong2() throws Exception {
        this.k().long_("aDouble");
    }


    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadString0() throws Exception {
        this.k().string("aInt");
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadString1() throws Exception {
        this.k().string("aBool");
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadString2() throws Exception {
        this.k().string("aIntList");
    }


    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadList0() throws Exception {
        this.k().list("aInt", Integer.class);
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testBadList1() throws Exception {
        this.k().list("aString", String.class);
    }

}
