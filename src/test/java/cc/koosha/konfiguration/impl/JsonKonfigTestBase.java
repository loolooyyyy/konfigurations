package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.SupplierX;
import lombok.val;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.util.Scanner;

public abstract class JsonKonfigTestBase extends KonfigValueTestMixin {

    protected String json;
    protected String json0;
    protected String json1;

    private JsonKonfigSource k;

    @BeforeClass
    public void classSetup() throws Exception {

        val url0 = getClass().getResource("sample0.json");
        val file0 = new File(url0.toURI());
        this.json0 = new Scanner(file0, "UTF8")
                .useDelimiter("\\Z")
                .next();

        val url1 = getClass().getResource("sample1.json");
        val file1 = new File(url1.toURI());
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
}
