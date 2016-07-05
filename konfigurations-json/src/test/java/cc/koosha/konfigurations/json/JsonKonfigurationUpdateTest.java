package cc.koosha.konfigurations.json;


import cc.koosha.konfigurations.core.KonfigV;
import lombok.val;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static cc.koosha.konfigurations.json.JsonKonfigurationBaseTest.list;
import static cc.koosha.konfigurations.json.JsonKonfigurationBaseTest.map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class JsonKonfigurationUpdateTest {

    protected static final String dummyFile0 = "/cc/koosha/konfigurations/json/dummyConfig.json";
    protected static final String dummyFile1 = "/cc/koosha/konfigurations/json/dummyConfigUpdated.json";
    protected JsonKonfiguration konfiguration;

    private String json0;
    private String json1;
    private String currentJson;

    private boolean iWasCalled = false;

    @BeforeMethod
    public void setup() throws Exception {

        try (val is0 = this.getClass().getResourceAsStream(dummyFile0);
             val is1 = this.getClass().getResourceAsStream(dummyFile1)) {
            json0 = new Scanner(is0, "UTF-8")
                    .useDelimiter("\\A")
                    .next();
            json1 = new Scanner(is1, "UTF-8")
                    .useDelimiter("\\A")
                    .next();
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }

        this.currentJson = json0;
        this.konfiguration = new JsonKonfiguration(() -> currentJson);
    }

    private void update() {

        currentJson = json1;
        assertTrue(konfiguration.update());
    }

    @Test
    public void testUpdate() throws Exception {

        final Integer somethingExpectBefore = 0;
        final Integer lifeExpectBefore = 42;
        final List<Integer> someArrExpectBefore = list(1, 2, 3, 4);
        final String stringExpectBefore = "you got me!";
        final Boolean boolExpectBefore = true;
        final Boolean ddExpectBefore = false;
        final Map<String, Integer> intMapExpectBefore = map("a0", 0, "a1", 1, "a2", 99);
        final Map<String, Long> lngMapExpectBefore = map("a0", 0L, "a1", 1L, "a2", Long.MAX_VALUE);
        final Map<String, String> strMapExpectBefore = map("a0", "a", "a1", "bb", "a2", "ccc");
        final String splitStrExpectBefore = "this is one sentence split over multiple lines.";

        final Integer somethingExpectAfter = 1;
        final Integer lifeExpectAfter = 42;
        final List<Integer> someArrExpectAfter = list(2, 3, 4, 5);
        final String stringExpectAfter = "you got me not!";
        final Boolean boolExpectAfter = false;
        final Boolean ddExpectAfter = true;
        final Map<String, Integer> intMapExpectAfter = map("a0", 1, "a1", 2, "a22", 99);
        final Map<String, Long> lngMapExpectAfter = map("a0", 1L, "a1", 2L, "a22", Long.MAX_VALUE);
        final Map<String, String> strMapExpectAfter = map("a0", "aaa", "a1", "bb", "a22", "c");
        final String splitStrExpectAfter = "this is not one sentence split over multiple lines.";


        final KonfigV<Integer> something0 = konfiguration.int_("something");
        final KonfigV<Integer> life0 = konfiguration.int_("meaning of life");
        final KonfigV<List<Integer>> someArr0 = konfiguration.list("someArr", int.class);
        final KonfigV<String> string0 = konfiguration.string("hahaTmp.ahah.hoho");
        final KonfigV<Boolean> bool0 = konfiguration.bool("aa.bb");
        final KonfigV<Boolean> dd0 = konfiguration.bool("dd");
        final KonfigV<Map<String, Integer>> intMap0 = konfiguration.map("intMap", int.class);
        final KonfigV<Map<String, Long>> lngMap0 = konfiguration.map("lngMap", long.class);
        final KonfigV<Map<String, String>> strMap0 = konfiguration.map("strMap", String.class);
        final KonfigV<String> splitStr0 = konfiguration.string("splitStr");


        final Integer somethingBefore = konfiguration.int_("something").v();
        final Integer lifeBefore = konfiguration.int_("meaning of life").v();
        final List<Integer> someArrBefore = konfiguration.list("someArr", int.class).v();
        final String stringBefore = konfiguration.string("hahaTmp.ahah.hoho").v();
        final Boolean boolBefore = konfiguration.bool("aa.bb").v();
        final Boolean ddBefore = konfiguration.bool("dd").v();
        final Map<String, Integer> intMapBefore = konfiguration.map("intMap", int.class).v();
        final Map<String, Long> lngMapBefore = konfiguration.map("lngMap", long.class).v();
        final Map<String, String> strMapBefore = konfiguration.map("strMap", String.class).v();
        final String splitStrBefore = konfiguration.string("splitStr").v();


        this.update();


        final Integer somethingAfter = konfiguration.int_("something").v();
        final Integer lifeAfter = konfiguration.int_("meaning of life").v();
        final List<Integer> someArrAfter = konfiguration.list("someArr", int.class).v();
        final String stringAfter = konfiguration.string("hahaTmp.ahah.hoho").v();
        final Boolean boolAfter = konfiguration.bool("aa.bb").v();
        final Boolean ddAfter = konfiguration.bool("dd").v();
        final Map<String, Integer> intMapAfter = konfiguration.map("intMap", int.class).v();
        final Map<String, Long> lngMapAfter = konfiguration.map("lngMap", long.class).v();
        final Map<String, String> strMapAfter = konfiguration.map("strMap", String.class).v();
        final String splitStrAfter = konfiguration.string("splitStr").v();


        assertEquals(somethingBefore, somethingExpectBefore);
        assertEquals(somethingAfter, somethingExpectAfter);
        assertEquals(lifeBefore, lifeExpectBefore);
        assertEquals(lifeAfter, lifeExpectAfter);
        assertEquals(someArrBefore, someArrExpectBefore);
        assertEquals(someArrAfter, someArrExpectAfter);
        assertEquals(stringBefore, stringExpectBefore);
        assertEquals(stringAfter, stringExpectAfter);
        assertEquals(boolBefore, boolExpectBefore);
        assertEquals(boolAfter, boolExpectAfter);
        assertEquals(ddBefore, ddExpectBefore);
        assertEquals(ddAfter, ddExpectAfter);
        assertEquals(intMapBefore, intMapExpectBefore);
        assertEquals(intMapAfter, intMapExpectAfter);
        assertEquals(lngMapBefore, lngMapExpectBefore);
        assertEquals(lngMapAfter, lngMapExpectAfter);
        assertEquals(strMapBefore, strMapExpectBefore);
        assertEquals(strMapAfter, strMapExpectAfter);
        assertEquals(splitStrBefore, splitStrExpectBefore);
        assertEquals(splitStrAfter, splitStrExpectAfter);
    }

    @Test
    public void testRegister() throws Exception {


        final KonfigV<Integer> something0 = konfiguration.int_("something");

        something0.register(s -> iWasCalled = true);

        this.update();

        assertTrue(iWasCalled);

    }
}
