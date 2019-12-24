package io.koosha.konfiguration.v8;

import io.koosha.konfiguration.DummyCustom;
import io.koosha.konfiguration.KonfigValueTestMixin;
import io.koosha.konfiguration.Source;
import io.koosha.konfiguration.base.UpdatableSource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings({"RedundantThrows", "WeakerAccess"})
public class MapKonfigSourceTest extends KonfigValueTestMixin {

    final AtomicReference<Map<String, Object>> map = new AtomicReference<>();

    UpdatableSource k;

    @BeforeMethod
    public void setup() throws Exception {
        this.map.set(DummyCustom.MAP0);
        this.k = new ExtMapSource(getClass().getSimpleName(), map::get, false);
    }

    @Override
    protected void update() {
        this.map.set(DummyCustom.MAP1);
        this.k = this.k.updatedSelf();
    }

    public Source k() {
        return this.k;
    }

    @Test
    public void testNotUpdatable() throws Exception {
        assertFalse(this.k.hasUpdate());
    }

    @Test
    public void testUpdatable() throws Exception {
        map.set(DummyCustom.MAP1);
        assertTrue(this.k.hasUpdate());
    }

}
