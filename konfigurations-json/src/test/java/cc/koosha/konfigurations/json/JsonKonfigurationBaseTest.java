package cc.koosha.konfigurations.json;

import lombok.val;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.util.*;

import static org.testng.Assert.assertEquals;


public class JsonKonfigurationBaseTest {

    protected static final String dummyFile = "/cc/koosha/konfigurations/json/dummyConfig.json";
    protected JsonKonfiguration konfiguration;

    @BeforeClass
    public void setup() throws Exception {

        final String content;

        try (val is = this.getClass().getResourceAsStream(dummyFile)) {
            content = new Scanner(is, "UTF-8")
                    .useDelimiter("\\A")
                    .next();
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }

        this.konfiguration = new JsonKonfiguration(content);
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

    @SafeVarargs
    protected static <T> List<T> list(final T t, T... ts) {

        final List<T> l = new ArrayList<>(1 + ts.length);
        l.add(t);
        Collections.addAll(l, ts);
        return l;
    }

}