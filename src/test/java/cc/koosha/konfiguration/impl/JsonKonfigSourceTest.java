package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.KonfigSource;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class JsonKonfigSourceTest extends JsonKonfigTestBase {

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
