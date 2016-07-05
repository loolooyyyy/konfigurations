package cc.koosha.konfigurations.json;

import cc.koosha.konfigurations.core.KonfigurationBadTypeException;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;


@Slf4j
public class JsonKonfigurationCollectionBadTypeTest extends JsonKonfigurationBaseTest{

    @DataProvider
    public static Object[][] list0Args() {

        return new Object[][] {
                {"bolArr", int.class},
                {"bolArr", String.class},
                {"bolArr", long.class},
                {"strArr", int.class},
                {"strArr", boolean.class},
                {"intArr", boolean.class},
                {"lngArr", boolean.class},
        };
    }

    @DataProvider
    public static Object[][] map0Args() {

        return new Object[][] {
                {"bolMap", int.class},
                {"bolMap", String.class},
                {"bolMap", long.class},
                {"strMap", int.class},
                {"strMap", boolean.class},
                {"intMap", boolean.class},
                {"lngMap", int.class},
                {"lngMap", boolean.class},
        };
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          dataProvider = "list0Args")
    public void test_list0(final String key, final Class<?> el) throws Exception {

        final List<?> debug = konfiguration.list(key, el).v();
        log.error("{} >=< {}", key, el);
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
          dataProvider = "map0Args")
    public void test_map(final String key, final Class<?> el) throws Exception {

        final Map<String, ?> debug = konfiguration.map(key, el).v();
        log.error("{} >=< {}", key, el);
    }

}