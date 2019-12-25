package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.DummyCustom;
import io.koosha.konfiguration.DummyCustom2;
import io.koosha.konfiguration.DummyVTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.testng.Assert.assertEquals;

public class YamlCustomValueTest {

    private boolean old;

    @BeforeClass
    public void prelude() {
        this.old = FaktoryV8.SAFE_YAML.getAndSet(false);
    }

    @AfterClass
    public void postLude() {
        FaktoryV8.SAFE_YAML.getAndSet(this.old);
    }

    @Test
    public void testCustomValue() {
        final String n = getClass().getSimpleName();
        final DummyCustom bang = new ExtYamlSource(n, () -> "bang:\n  str : hello\n  i: 99",
                ExtYamlSource::getDefaultYamlSupplier, FaktoryV8.SAFE_YAML.get())
                .custom("bang", DummyCustom.class).v();
        assertEquals(bang.i, 99);
        assertEquals(bang.str, "hello");
    }

    @Test
    public void testCustomValue2() throws URISyntaxException, IOException {
        final String n = getClass().getSimpleName();
        final String y = new Scanner(new File(
                getClass().getResource("/sample2.yaml").toURI()),
                StandardCharsets.UTF_8.name())
                .useDelimiter("\\Z").next();

        final DummyCustom2 bang = new ExtYamlSource(n, () -> y,
                ExtYamlSource::getDefaultYamlSupplier, FaktoryV8.SAFE_YAML.get())
                .custom("bang", DummyCustom2.class).v();

        assertEquals(bang.i, 99);
        assertEquals(bang.str, "hello");
        assertEquals(bang.olf, DummyVTest.of(
                "manga", "panga", "foo", "bar", "baz", "quo"));
        assertEquals(bang.again, "no");
        assertEquals(bang.i, 99);
    }


}
