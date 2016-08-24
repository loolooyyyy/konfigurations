package cc.koosha.konfigurations.core;

import cc.koosha.konfigurations.core.impl.KonfigurationKombiner;
import org.testng.annotations.BeforeMethod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cc.koosha.konfigurations.core.DummyV.dummy;
import static org.testng.Assert.assertEquals;


public class KonfigurationKombinerBaseTest {

    protected final String str0 = "something else";
    protected final String str1 ="something else appended";
    protected final List<Integer> l0 = Arrays.asList(0, 1, 2, Integer.MAX_VALUE, Integer.MAX_VALUE);
    protected final List<Integer> l1 = Arrays.asList(42, 43, 434);

    protected final Map<String, Object> sample0 = KonfigurationKombinerBaseTest.<Object>map(
            "str key", dummy(str0),
            "list key", dummy(l0),
            "int key", dummy(42)
    );

    protected final Map<String, Object> sample1 = KonfigurationKombinerBaseTest.<Object>map(
            "str key", dummy(str1),
            "list key", dummy(l1),
            "int key", dummy(42)
    );

    protected Map<String, Object> sample = sample0;
    protected String updatedKey0;
    protected String updatedKey1;

    protected Map<String, Object> sample() {

        return this.sample;
    }

    @SuppressWarnings("unchecked")
    protected final Konfiguration dummyKonfig = new Konfiguration() {
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

        @Override
        public Konfiguration subset(String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Konfiguration parent() {
            throw new UnsupportedOperationException();
        }
    };

    protected final KonfigurationKombiner kk = new KonfigurationKombiner(dummyKonfig);


    @SuppressWarnings({"unchecked", "Duplicates"})
    protected static <T> Map<String, T> map(final String k0, final T v0, final Object... os) {

        assertEquals(os.length % 2, 0);

        final Map<String, T> m = new HashMap<>();
        m.put(k0, v0);

        for (int i = 0; i < os.length; i+=2)
            m.put(((String) os[i]), ((T) os[i + 1]));

        return m;
    }




    @BeforeMethod
    public void setup() {

        updatedKey0 = null;
        updatedKey1 = null;
        sample = sample0;
    }

}
