package io.koosha.konfiguration;


import org.testng.annotations.Test;

import java.util.*;

import static org.testng.Assert.*;


@SuppressWarnings({"RedundantThrows", "ConstantConditions"})
public class DummyVTest {

    private final static String key = "sample.key";

    @Test
    public void testRegister() throws Exception {
        K<?> dummyV = K.null_(Q.STRING);
        assertSame(dummyV.register(k -> {
        }), dummyV);
    }

    @Test
    public void testGetKey() throws Exception {
        K<?> dummyV = K.null_(Q.STRING);
        assertSame(dummyV.key(), key);
    }

    @Test
    public void testV() throws Exception {
        long value = 99L;
        K<?> dummyV = K.of(value, Q.LONG);
        assertSame(dummyV.v(), value);
    }

    @Test
    public void testVWithDefaultValue() throws Exception {
        long value = 99L;
        K<Long> dummyV = K.null_(Q.LONG);
        assertSame(dummyV.v(value), value);
    }

    @Test
    public void testFalse_() throws Exception {
        assertFalse(K.false_().v());
        assertFalse(K.false_().v(true));
    }

    @Test
    public void testTrue_() throws Exception {
        assertTrue(K.true_().v());
        assertTrue(K.true_().v(false));
    }

    @Test
    public void testInt_() throws Exception {
        assertEquals(K.int_(99).v(), (Integer) 99);
        assertEquals(K.int_(99).v(88), (Integer) 99);
    }

    @Test
    public void testLong_() throws Exception {
        assertEquals(K.long_(99L).v(), (Long) 99L);
        assertEquals(K.long_(99L).v(88L), (Long) 99L);
    }

    @Test
    public void testDouble_() throws Exception {
        // We are comparing doubles!!
        assertEquals((Object) K.double_(99.9D).v(), 99.9D);
        assertEquals((Object) K.double_(99.9D).v(99.9D), 99.9D);
    }

    @Test
    public void testString() throws Exception {
        String value = "xx yy ha ha";
        assertEquals(K.stringOf(value, "").v(), value);
        assertEquals(K.stringOf(value, "").v("something"), value);
    }


    @Test
    public void testList() throws Exception {
        List<Object> value = Arrays.asList(new Object(), new Object());
        List<Object> def = Arrays.asList(new Object(), 1, 2, 3);
        assertSame(K.list(value).v(), value);
        assertSame(K.list(value).v(def), value);
    }

    @Test
    public void testMap() throws Exception {
        Map<Object, Object> value = new HashMap<>();
        value.put(new Object(), new Object());
        value.put(new Object(), new Object());

        Map<Object, Object> def = new HashMap<>();
        value.put("a", new Object());
        value.put("b", new Object());

        assertSame(K.map(value).v(), value);
        assertSame(K.map(value).v(def), value);
    }

    @Test
    public void testSet() throws Exception {
        Set<Object> value = new HashSet<>(Arrays.asList(new Object(), new Object()));
        Set<Object> def = new HashSet<>(Arrays.asList(new Object(), 1, 2, 3));
        assertSame(K.set(value).v(), value);
        assertSame(K.set(value).v(def), value);
    }

    @Test
    public void testNull_() throws Exception {
        assertNull(K.null_(Q.STRING).v());
        assertNull(K.null_(Q.OBJECT).v(new Object()));
    }

}
