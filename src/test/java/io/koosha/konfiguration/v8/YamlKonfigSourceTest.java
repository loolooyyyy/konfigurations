package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.Faktory;
import io.koosha.konfiguration.KonfigValueTestMixin;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.base.UpdatableSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static io.koosha.konfiguration.DummyCustom.YAML_SAMPLE_0;
import static io.koosha.konfiguration.DummyCustom.YAML_SAMPLE_1;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("RedundantThrows")
public class YamlKonfigSourceTest extends KonfigValueTestMixin {

    private final AtomicReference<String> yaml = new AtomicReference<>();
    UpdatableSource k;

    private boolean old;

    @BeforeClass
    public void prelude() {
        this.old = FaktoryV8.SAFE_YAML.getAndSet(false);
    }

    @AfterClass
    public void postLude() {
        FaktoryV8.SAFE_YAML.getAndSet(this.old);
    }

    @BeforeMethod
    public void setup() throws Exception {
        yaml.set(YAML_SAMPLE_0);
        this.k = new ExtYamlSource(getClass().getSimpleName(), yaml::get,
                ExtYamlSource::getDefaultYamlSupplier, Faktory.SAFE_YAML.get());
    }

    @Override
    protected Source k() {
        return this.k;
    }

    @Override
    protected void update() {
        yaml.set(YAML_SAMPLE_1);
        this.k = this.k.updatedSelf();
    }

    @Test
    public void testNotUpdatable() throws Exception {
        assertFalse(this.k.hasUpdate());
    }

    @Test
    public void testUpdatable() throws Exception {
        yaml.set(YAML_SAMPLE_1);
        assertTrue(this.k.hasUpdate());
    }

}
