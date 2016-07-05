package cc.koosha.konfigurations.json;

import lombok.val;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;


public class JsonKonfigurationCollectionTest extends JsonKonfigurationBaseTest {

    @Test
    public void test_intList() throws Exception {

        val actual = konfiguration.list("intArr", int.class).v();
        val expected = list(0, 1, 99);
        assertEquals(actual, expected);
    }

    @Test
    public void test_longList() throws Exception {

        val actual = konfiguration.list("lngArr", long.class).v();
        val expected = list(0L, 1L, 444L, 9223372036854775807L);
        assertEquals(actual, expected);
    }

    @Test
    public void test_stringList() throws Exception {

        val actual = konfiguration.list("strArr", String.class).v();
        val expected = list("a", "bb", "ccc");
        assertEquals(actual, expected);
    }

    @Test
    public void test_boolList() throws Exception {

        val actual = konfiguration.list("bolArr", boolean.class).v();
        val expected = list(true, false, true);
        assertEquals(actual, expected);
    }



    @Test
    public void test_intMap() throws Exception {

        val actual = konfiguration.map("intMap", int.class).v();
        val expected = map("a0", 0, "a1", 1, "a2", 99);
        assertEquals(actual, expected);
    }

    @Test
    public void test_longMap() throws Exception {

        val actual = konfiguration.map("lngMap", long.class).v();
        val expected = map("a0", 0L, "a1", 1L, "a2", Long.MAX_VALUE);
        assertEquals(actual, expected);
    }

    @Test
    public void test_stringMap() throws Exception {

        val actual = konfiguration.map("strMap", String.class).v();
        val expected = map("a0", "a", "a1", "bb", "a2", "ccc");
        assertEquals(actual, expected);
    }

    @Test
    public void test_boolMap() throws Exception {

        val actual = konfiguration.map("bolMap", boolean.class).v();
        val expected = map("a0", true, "a1", false, "a2", true);
        assertEquals(actual, expected);
    }

}