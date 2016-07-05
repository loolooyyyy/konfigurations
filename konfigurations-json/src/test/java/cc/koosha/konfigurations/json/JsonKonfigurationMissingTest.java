package cc.koosha.konfigurations.json;

import cc.koosha.konfigurations.core.KonfigurationMissingKeyException;
import org.testng.annotations.Test;


@SuppressWarnings("SpellCheckingInspection")
public class JsonKonfigurationMissingTest extends JsonKonfigurationBaseTest{

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

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void test_listL0() throws Exception {

        konfiguration.list("i.do.not.exist", Object.class);
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void test_listL1() throws Exception {

        konfiguration.list("obj.nonExisting", Object.class);
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void test_map0() throws Exception {

        konfiguration.map("i.do.not.exist", Object.class);
    }

    @Test(expectedExceptions = KonfigurationMissingKeyException.class)
    public void test_map1() throws Exception {

        konfiguration.map("obj.nonExisting", Object.class);
    }

}