package io.koosha.konfiguration.v8;


import io.koosha.konfiguration.DummyCustom;
import io.koosha.konfiguration.KonfigValueTestMixin;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.base.UpdatableSource;
import io.koosha.konfiguration.error.KfgTypeException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


@SuppressWarnings("RedundantThrows")
public class JacksonJsonTest extends KonfigValueTestMixin {

    final AtomicReference<String> json = new AtomicReference<>();
    UpdatableSource k;

    @BeforeMethod
    public void setup() throws Exception {
        this.json.set(DummyCustom.JSON_SAMPLE_0);
        this.k = new ExtJacksonJsonSource(getClass().getSimpleName(),
                json::get,
                ExtJacksonJsonSource::defaultJacksonObjectMapper);
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test(expectedExceptions = KfgTypeException.class)
    public void testBadSet() throws Exception {
        this.k.set("aBadSet", int.class);
    }


}
