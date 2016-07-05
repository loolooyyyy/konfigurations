package cc.koosha.konfigurations.json;

import cc.koosha.konfigurations.core.KonfigV;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.testng.Assert.assertEquals;


@SuppressWarnings("SpellCheckingInspection")
public class JsonKonfigurationPrimitiveTest {

    @Test
    public void test_string() throws Exception {

        final Map<String, String> m = map(
                "hahaTmp.ahah.hoho", "you got me!"
        );

        m.forEach((k, v) -> {
            final KonfigV<String> konfig = konfiguration.string(k);
            assertEquals(konfig.key(), k);
            assertEquals(konfig.v(), v);
        });
    }

    @Test
    public void test_bool() throws Exception {

        final Map<String, Boolean> m = map(
                "aa.bb", true,
                "dd", false
        );

        m.forEach((k, v) -> {
            final KonfigV<Boolean> konfig = konfiguration.bool(k);
            assertEquals(konfig.key(), k);
            assertEquals(konfig.v(), v);
        });
    }

    @Test
    public void test_long() throws Exception {

        final Map<String, Long> m = map(
                "something", 0L,
                "meaning of life", 42L,
                "veryLong", Long.MAX_VALUE
        );

        m.forEach((k, v) -> {
            final KonfigV<Long> konfig = konfiguration.long_(k);
            assertEquals(konfig.key(), k);
            assertEquals(konfig.v(), v);
        });
    }

    @Test
    public void test_int() throws Exception {

        final Map<String, Integer> m = map(
                "something", 0,
                "meaning of life", 42,
                "veryInt", Integer.MAX_VALUE,
                "obj.a", 3,
                "obj.n", 5
        );

        m.forEach((k, v) -> {
            final KonfigV<Integer> konfig = konfiguration.int_(k);
            assertEquals(konfig.key(), k);
            assertEquals(konfig.v(), v);
        });
    }



    // -------------------------------------------------------------------------

    private static final String dummyFile = "/cc/koosha/konfigurations/json/dummyConfig.json";
    private static final ObjectMapper om = new ObjectMapper();
    private JsonKonfiguration konfiguration;

    @BeforeClass
    public void setUp() throws Exception {

        final String content;

        try (val is = this.getClass().getResourceAsStream(dummyFile)) {
            content = new Scanner(is, "UTF-8")
                    .useDelimiter("\\A")
                    .next();
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }

        this.konfiguration = new JsonKonfiguration(() -> {
            try {
                return om.readTree(content);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> map(final String k0, final T v0, final Object... os) {

        assertEquals(os.length % 2, 0);

        final Map<String, T> m = new HashMap<>();
        m.put(k0, v0);

        for (int i = 0; i < os.length; i+=2)
            m.put(((String) os[i]), ((T) os[i + 1]));

        return m;
    }

}