package cc.koosha.konfigurations.core;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;


public class KonfigurationKombinerTest extends KonfigurationKombinerBaseTest {

    @Test
    public void testUpdate() throws Exception {

        final KonfigV<String> string = kk.string("str key");
        final KonfigV<List<Integer>> list = kk.list("list key", int.class);
        final KonfigV<Integer> i = kk.int_("int key");

        string.register(s -> updatedKey0 = s);
        list.register(s -> updatedKey1 = s);
        i.register(s -> {throw new RuntimeException();});

        assertNull(updatedKey0);
        assertNull(updatedKey1);
        assertEquals(string.v(), str0);
        assertEquals(list.v(), l0);
        assertEquals(i.v(), (Integer) 42);

        sample = sample1;
        assertTrue(kk.update());

        assertEquals("str key", updatedKey0);
        assertEquals("list key", updatedKey1);
        assertEquals(string.v(), str1);
        assertEquals(list.v(), l1);
        assertEquals(i.v(), (Integer) 42);
    }

}