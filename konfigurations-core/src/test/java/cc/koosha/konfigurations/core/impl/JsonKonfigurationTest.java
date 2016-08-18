package cc.koosha.konfigurations.core.impl;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.val;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class JsonKonfigurationTest {

    private JsonKonfiguration konfig;

    @BeforeClass
    private void setup() throws IOException {

        val is = getClass().getResourceAsStream("/sample.json");
        if(is == null)
            throw new NullPointerException("sample.json not found");

        val buffer = new byte[1024];
        val os = new ByteArrayOutputStream();

        int len;
        while ((len = is.read(buffer)) > -1)
            os.write(buffer, 0, len);

        os.flush();
        val json = new String(os.toByteArray(), Charset.forName("US-ASCII"));
        this.konfig = new JsonKonfiguration(json);
    }

    @Test
    public void testListSimple() throws Exception {

        final List<String> v = konfig.list("a.list", String.class).v();

        assertEquals(v.size(), 3);
        assertTrue(v.contains("a"));
        assertTrue(v.contains("b"));
        assertTrue(v.contains("c"));
    }

    @Test
    public void testMapSimple() throws Exception {

        final Map<String, Integer> v = konfig.map("some.map", int.class).v();

        assertEquals(v.size(), 3);
        assertEquals(v.get("a"), Integer.valueOf(1));
        assertEquals(v.get("b"), Integer.valueOf(2));
        assertEquals(v.get("c"), Integer.valueOf(0));
    }

    @Test
    public void testCustom() {

        val v = konfig.custom("cus.tom", Custom.class).v();
        assertEquals(v, new Custom(23, "hello", "me"));
    }

    @Test
    public void testListOfCustom() {

        val v = konfig.list("listOf.customs", Custom.class).v();
        assertEquals(v.size(), 3);

        final Custom c0 = v.get(0);
        final Custom c1 = v.get(1);
        final Custom c2 = v.get(2);

        val expect0 = new Custom(10, "name10", "family10");
        val expect1 = new Custom(20, "name20", "family20");
        val expect2 = new Custom(30, "name30", "family30");

        assertEquals(c0, expect0);
        assertEquals(c1, expect1);
        assertEquals(c2, expect2);
    }

    @Test
    public void testMapOfCustom() {

        final Map<String, Custom> v = konfig.map("mapOf", Custom.class).v();

        final Custom c0 = v.get("a");
        final Custom c1 = v.get("b");
        final Custom c2 = v.get("c");

        val expect0 = new Custom(10, "name10", "family10");
        val expect1 = new Custom(20, "name20", "family20");
        val expect2 = new Custom(30, "name30", "family30");

        assertEquals(c0, expect0);
        assertEquals(c1, expect1);
        assertEquals(c2, expect2);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    static final class Custom {
        int a;
        String name;
        String family;
    }

}