package cc.koosha.konfiguration.impl;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class InMemoryKonfigSourceTest extends InMemTestBase {

    @Test
    public void testNotUpdatable() throws Exception {

        assertFalse(this.k().isUpdatable());
    }

    @Test
    public void testUpdatable() throws Exception {

        map = map1;
        assertTrue(this.k().isUpdatable());
    }


}
