package cc.koosha.konfigurations.json;

import cc.koosha.konfigurations.core.KonfigV;
import lombok.val;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;


@SuppressWarnings("SpellCheckingInspection")
public class JsonKonfigurationPrimitiveTest extends JsonKonfigurationBaseTest {

    @Test
    public void test_string() throws Exception {

        final Map<String, String> m = map(
                "hahaTmp.ahah.hoho", "you got me!"
        );

        for (val e : m.entrySet()) {
            final KonfigV<String> konfig = konfiguration.string(e.getKey());
            assertEquals(konfig.v(), e.getValue());
        }
    }

    @Test
    public void test_bool() throws Exception {

        final Map<String, Boolean> m = map(
                "aa.bb", true,
                "dd", false
        );

        for (val e : m.entrySet()) {
            final KonfigV<Boolean> konfig = konfiguration.bool(e.getKey());
            assertEquals(konfig.v(), e.getValue());
        }
    }

    @Test
    public void test_long() throws Exception {

        final Map<String, Long> m = map(
                "something", 0L,
                "meaning of life", 42L,
                "veryLong", Long.MAX_VALUE
        );

        for (val e : m.entrySet()) {
            final KonfigV<Long> konfig = konfiguration.long_(e.getKey());
            assertEquals(konfig.v(), e.getValue());
        }
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

        for (val e : m.entrySet()) {
            final KonfigV<Integer> konfig = konfiguration.int_(e.getKey());
            assertEquals(konfig.v(), e.getValue());
        }
    }


    @Test
    public void test_splitString() throws Exception {

        final String expected = "this is one sentence split over multiple lines.";
        final String actual = konfiguration.string("splitStr").v();

        assertEquals(actual, expected);
    }

}