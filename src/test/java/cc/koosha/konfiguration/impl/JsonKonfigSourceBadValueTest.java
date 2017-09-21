package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.KonfigSource;
import cc.koosha.konfiguration.KonfigurationBadTypeException;
import cc.koosha.konfiguration.SupplierX;
import lombok.val;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Scanner;

public class JsonKonfigSourceBadValueTest {


    private String json;
    private String json0;
    private KonfigSource k;

    @BeforeClass
    public void classSetup() throws Exception {

        val url0 = getClass().getResource("sample0.json");
        val url1 = getClass().getResource("sample1.json");

        this.json0 = new Scanner(new File(url0.toURI()), "UTF8").useDelimiter("\\Z")
                .next();
    }

    @BeforeMethod
    public void setup() throws Exception {

        json = json0;
        this.k = new JsonKonfigSource(new SupplierX<String>() {
            @Override
            public String get() {
                return json;
            }
        });
    }


    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not an int.*")
    public void testBadInt0() throws Exception {

        k.int_("aBool");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not an int.*")
    public void testBadInt1() throws Exception {

        k.int_("aLong");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not an int.*")
    public void testBadInt2() throws Exception {

        k.int_("aString");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not an int.*")
    public void testBadInt3() throws Exception {

        k.int_("aDouble");
    }


    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not a double.*")
    public void testBadDouble0() throws Exception {

        k.double_("aBool");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not a double.*")
    public void testBadDouble1() throws Exception {

        k.double_("aLong");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not a double.*")
    public void testBadDouble() throws Exception {

        k.double_("aString");
    }


    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not a long.*")
    public void testBadLong0() throws Exception {

        k.long_("aBool");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not a long.*")
    public void testBadLong1() throws Exception {

        k.long_("aString");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not a long.*")
    public void testBadLong2() throws Exception {

        k.long_("aDouble");
    }


    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not a string.*")
    public void testBadString0() throws Exception {

        k.string("aInt");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not a string.*")
    public void testBadString1() throws Exception {

        k.string("aBool");
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not a string.*")
    public void testBadString2() throws Exception {

        k.string("aIntList");
    }


    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not a list.*")
    public void testBadList0() throws Exception {

        k.list("aInt", Integer.class);
    }

    @Test(expectedExceptions = KonfigurationBadTypeException.class,
            expectedExceptionsMessageRegExp = "not a list.*")
    public void testBadList1() throws Exception {

        k.list("aString", String.class);
    }

}
