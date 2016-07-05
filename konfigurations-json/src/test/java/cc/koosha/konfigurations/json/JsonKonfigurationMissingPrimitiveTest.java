package cc.koosha.konfigurations.json;

import cc.koosha.konfigurations.core.KonfigurationMissingKeyException;
import lombok.val;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Scanner;


@SuppressWarnings("SpellCheckingInspection")
public class JsonKonfigurationMissingPrimitiveTest {

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void test_stringLevel0() throws Exception {

        konfiguration.string("i.do.not.exist");
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void test_stringLevel1() throws Exception {

        konfiguration.string("hahaTmp.ahah.hohoNON");
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void test_boolL0() throws Exception {

        konfiguration.bool("i.do.not.exist");
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void test_boolL1() throws Exception {

        konfiguration.bool("aa.bbb");
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void test_longL0() throws Exception {

        konfiguration.long_("i.do.not.exist");
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void test_longL1() throws Exception {

        // TODO
        throw new KonfigurationMissingKeyException();
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void test_intL0() throws Exception {

        konfiguration.int_("i.do.not.exist");
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void test_intL1() throws Exception {

        konfiguration.int_("obj.nonExisting");
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