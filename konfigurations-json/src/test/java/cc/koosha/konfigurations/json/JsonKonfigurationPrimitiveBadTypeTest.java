package cc.koosha.konfigurations.json;

import cc.koosha.konfigurations.core.KonfigurationBadTypeException;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;


@Slf4j
@SuppressWarnings("SpellCheckingInspection")
public class JsonKonfigurationPrimitiveBadTypeTest extends JsonKonfigurationBaseTest{

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          dataProvider = "strings")
    public void test_string(final String key) throws Exception {

        konfiguration.string(key);
        log.error("=> {}", key);
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

}