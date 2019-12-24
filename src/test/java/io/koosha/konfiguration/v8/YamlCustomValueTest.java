package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.DummyCustom;
import io.koosha.konfiguration.DummyCustom2;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

import static org.testng.Assert.assertEquals;

public class YamlCustomValueTest {

    @Test
    public void testCustomValue() {
        final DummyCustom bang = new ExtYamlSource(getClass().getSimpleName(), () -> "bang:\n  str : hello\n  i: 99",
                () -> ExtYamlSource.getDefaultYamlSupplier("test"), true)
                .custom("bang", DummyCustom.class).v();
        assertEquals(bang.i, 99);
        assertEquals(bang.str, "hello");
    }

    @Test
    public void testCustomValue2() throws URISyntaxException, IOException {
        final String y = new Scanner(new File(
                getClass().getResource("/sample2.yaml").toURI()),
                StandardCharsets.UTF_8)
                .useDelimiter("\\Z").next();

        final DummyCustom2 bang = new ExtYamlSource(getClass().getSimpleName(), () -> y,
                () -> ExtYamlSource.getDefaultYamlSupplier("test"), true)
                .custom("bang", DummyCustom2.class).v();

        assertEquals(bang.i, 99);
        assertEquals(bang.str, "hello");
        assertEquals(bang.olf, Map.of(
                "manga", "panga", "foo", "bar", "baz", "quo"));
        assertEquals(bang.again, "no");
        assertEquals(bang.i, 99);
    }

}
