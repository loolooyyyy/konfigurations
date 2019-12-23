package io.koosha.konfiguration.v8;


import io.koosha.konfiguration.DummyCustom;
import io.koosha.konfiguration.KonfigValueTestMixin;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.base.UpdatableSource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


@SuppressWarnings("RedundantThrows")
public class SourceJacksonJsonTest extends KonfigValueTestMixin {

    final AtomicReference<String> json = new AtomicReference<>();
    UpdatableSource k;

    @BeforeClass
    public void classSetup() throws Exception {
        this.json.set(DummyCustom.JSON_SAMPLE_0);
        this.k = new ExtJacksonJsonSource("test",
                json::get,
                ExtJacksonJsonSource::defaultJacksonObjectMapper);
    }

    @BeforeMethod
    public void setup() throws Exception {
        json.set(DummyCustom.JSON_SAMPLE_0);
    }

    @Override
    protected void update() {
        this.json.set(DummyCustom.JSON_SAMPLE_1);
        this.k = this.k.updatedSelf();
    }

    @Override
    public Source k() {
        return this.k;
    }

    @Test
    public void testNotUpdatable() throws Exception {
        assertFalse(this.k.hasUpdate());
    }

    @Test
    public void testUpdatable() throws Exception {
        this.json.set(DummyCustom.JSON_SAMPLE_1);
        assertTrue(this.k.hasUpdate());
    }

}
