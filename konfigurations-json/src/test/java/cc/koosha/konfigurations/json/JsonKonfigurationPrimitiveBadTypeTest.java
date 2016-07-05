package cc.koosha.konfigurations.json;

import cc.koosha.konfigurations.core.KonfigurationBadTypeException;
import lombok.val;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


@SuppressWarnings("SpellCheckingInspection")
public class JsonKonfigurationPrimitiveBadTypeTest {

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          dataProvider = "strings")
    public void test_string(final String key) throws Exception {

        konfiguration.string(key);
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          dataProvider = "bools")
    public void test_bool(final String key) throws Exception {

        konfiguration.bool(key);
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          dataProvider = "longs")
    public void test_long(final String key) throws Exception {

        konfiguration.long_(key);
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          dataProvider = "ints")
    public void test_int(final String key) throws Exception {

        konfiguration.int_(key);
    }


    @DataProvider
    public Object[][] strings() {

        final List<String> keys = Arrays.asList(
                "something",
                "meaning of life",
                "someArr",
                "obj",
                "obj.a",
                "obj.n",
                "hahaTmp",
                "hahaTmp.ahah",
                "aa",
                "aa.bb",
                "dd",
                "veryInt",
                "veryLong"
        );

        final Object[][] args = new Object[keys.size()][];

        for (int i = 0; i < args.length; i++)
            args[i] = new Object[]{keys.get(i)};

        return args;
    }

    @DataProvider
    public Object[][] bools() {

        final List<String> keys = Arrays.asList(
                "something",
                "meaning of life",
                "someArr",
                "obj",
                "obj.a",
                "obj.n",
                "hahaTmp",
                "hahaTmp.ahah",
                "hahaTmp.ahah.hoho",
                "aa",
                "veryInt",
                "veryLong",
                "veryString"
        );

        final Object[][] args = new Object[keys.size()][];

        for (int i = 0; i < args.length; i++)
            args[i] = new Object[]{keys.get(i)};

        return args;
    }

    @DataProvider
    public Object[][] longs() {

        final List<String> keys = Arrays.asList(
                "someArr",
                "obj",
                "hahaTmp",
                "hahaTmp.ahah",
                "hahaTmp.ahah.hoho",
                "aa",
                "aa.bb",
                "dd",
                "veryString"
        );

        final Object[][] args = new Object[keys.size()][];

        for (int i = 0; i < args.length; i++)
            args[i] = new Object[]{keys.get(i)};

        return args;
    }

    @DataProvider
    public Object[][] ints() {

        final List<String> keys = Arrays.asList(
                "someArr",
                "obj",
                "hahaTmp",
                "hahaTmp.ahah",
                "hahaTmp.ahah.hoho",
                "aa",
                "aa.bb",
                "dd",
                "veryLong",
                "veryString"
        );

        final Object[][] args = new Object[keys.size()][];

        for (int i = 0; i < args.length; i++)
            args[i] = new Object[]{keys.get(i)};

        return args;
    }


    // -------------------------------------------------------------------------

    private static final String dummyFile = "/cc/koosha/konfigurations/json/dummyConfig.json";
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

        this.konfiguration = new JsonKonfiguration(content);
    }

}