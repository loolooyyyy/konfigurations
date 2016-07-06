package cc.koosha.konfigurations.core;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cc.koosha.konfigurations.core.DummyV.dummy;
import static org.testng.Assert.*;


public class KonfigurationKombinerTest {

    private final String str0 = "something else";
    private final String str1 ="something else appended";
    private final List<Integer> l0 = Arrays.asList(0, 1, 2, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final List<Integer> l1 = Arrays.asList(42, 43, 434);

    private final Map<String, Object> sample0 = map(
            "str key", dummy(str0),
            "list key", dummy(l0),
            "int key", dummy(42)
    );

    private final Map<String, Object> sample1 = map(
            "str key", dummy(str1),
            "list key", dummy(l1),
            "int key", dummy(42)
    );

    private Map<String, Object> sample;
    private String updatedKey0;
    private String updatedKey1;

    private Map<String, Object> sample() {

        return this.sample;
    }

    @SuppressWarnings("unchecked")
    private final Konfiguration dummyKonfig = new Konfiguration() {
        @Override
        public KonfigV<Boolean> bool(final String key) {
            return (KonfigV<Boolean>) sample().get(key);
        }

        @Override
        public KonfigV<Integer> int_(final String key) {
            return (KonfigV<Integer>) sample().get(key);
        }

        @Override
        public KonfigV<Long> long_(final String key) {
            return (KonfigV<Long>) sample().get(key);
        }

        @Override
        public KonfigV<String> string(final String key) {
            return (KonfigV<String>) sample().get(key);
        }

        @Override
        public <T> KonfigV<List<T>> list(final String key, final Class<T> type) {
            return (KonfigV<List<T>>) sample().get(key);
        }

        @Override
        public <T> KonfigV<Map<String, T>> map(final String key, final Class<T> type) {
            return (KonfigV<Map<String, T>>) sample().get(key);
        }

        @Override
        public <T> KonfigV<T> custom(final String key, final Class<T> type) {
            return (KonfigV<T>) sample().get(key);
        }

        @Override
        public boolean update() {
            return true;
        }
    };

    private final KonfigurationKombiner kk = new KonfigurationKombiner(dummyKonfig);


    @BeforeMethod
    public void setup() {

        updatedKey0 = null;
        updatedKey1 = null;
        sample = sample0;
    }


    @Test
    public void testUpdate() throws Exception {

        final KonfigV<String> string = kk.string("str key");
        final KonfigV<List<Integer>> list = kk.list("list key", int.class);
        final KonfigV<Integer> i = kk.int_("int key");

        string.register(s -> updatedKey0 = s);
        list.register(s -> updatedKey1 = s);
        i.register(s -> {throw new RuntimeException();});

        assertNull(updatedKey0);
        assertNull(updatedKey1);
        assertEquals(string.v(), str0);
        assertEquals(list.v(), l0);
        assertEquals(i.v(), (Integer) 42);

        sample = sample1;
        assertTrue(kk.update());

        assertEquals("str key", updatedKey0);
        assertEquals("list key", updatedKey1);
        assertEquals(string.v(), str1);
        assertEquals(list.v(), l1);
        assertEquals(i.v(), (Integer) 42);
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    protected static <T> Map<String, T> map(final String k0, final T v0, final Object... os) {

        assertEquals(os.length % 2, 0);

        final Map<String, T> m = new HashMap<>();
        m.put(k0, v0);

        for (int i = 0; i < os.length; i+=2)
            m.put(((String) os[i]), ((T) os[i + 1]));

        return m;
    }

}