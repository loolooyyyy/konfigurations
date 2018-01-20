package cc.koosha.konfiguration;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.util.Scanner;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class JsonKonfigSourceTest extends KonfigValueTestMixin {

    protected String json;
    protected String json0;
    protected String json1;

    private JsonKonfigSource k;

    @BeforeClass
    public void classSetup() throws Exception {

        URL url0 = getClass().getResource("sample0.json");
        File file0 = new File(url0.toURI());
        this.json0 = new Scanner(file0, "UTF8")
                .useDelimiter("\\Z")
                .next();

        URL url1 = getClass().getResource("sample1.json");
        File file1 = new File(url1.toURI());
        this.json1 = new Scanner(file1, "UTF8")
                .useDelimiter("\\Z")
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

    protected void update() {

        this.json = this.json1;
        this.k = (JsonKonfigSource) this.k.copyAndUpdate();
    }

    public JsonKonfigSource k() {
        return this.k;
    }

    @Test
    public void testNotUpdatable() throws Exception {

        assertFalse(this.k().isUpdatable());
    }

    @Test
    public void testUpdatable() throws Exception {

        json = json1;
        assertTrue(this.k().isUpdatable());
    }

}
