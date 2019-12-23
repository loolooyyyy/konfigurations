package io.koosha.konfiguration;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

@SuppressWarnings({"RedundantThrows", "ConstantConditions"})
public class DummyVTest {

    private final static String key = "sample.key";

    @Test
    public void testGetKey() throws Exception {
        K<?> dummyV = K.null_(Q.string(key));
        assertSame(dummyV.key(), key);
        dummyV = K.null_(Q.string(key), key + key);
        assertSame(dummyV.key(), key + key);
    }

    @Test
    public void testV() throws Exception {
        long value = 99L;
        K<?> dummyV = K.of(value, Q.long_(key));
        assertSame(dummyV.v(), value);
    }

    @Test
    public void testVWithDefaultValue() throws Exception {
        long value = 99L;
        K<Long> dummyV = K.missing(Q.long_());
        assertSame(dummyV.v(value), value);
    }

    @Test
    public void testFalse_() throws Exception {
        assertFalse(K.false_().v());
        assertFalse(K.false_().v(true));
        assertFalse(K.false_(key).v());
        assertFalse(K.false_(key).v(true));
    }

    @Test
    public void testTrue_() throws Exception {
        assertTrue(K.true_().v());
        assertTrue(K.true_().v(false));
        assertTrue(K.true_(key).v());
        assertTrue(K.true_(key).v(false));
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
    public void testNull_() throws Exception {
        assertNull(K.null_(Q.string("")).v());
    }

}
