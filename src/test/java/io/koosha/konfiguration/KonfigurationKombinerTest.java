package io.koosha.konfiguration;

import io.koosha.konfiguration.error.KfgMissingKeyException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static java.util.Collections.singletonMap;
import static org.testng.Assert.*;

@SuppressWarnings("RedundantThrows")
public final class KonfigurationKombinerTest {

    final Faktory fac = Faktory.defaultImplementation();

    private AtomicBoolean flag = new AtomicBoolean(false);

    private final Supplier<Map<String, ?>> sup = () -> flag.get()
                                                       ? singletonMap("xxx", (Object) 12)
                                                       : singletonMap("xxx", (Object) 99);

    private KonfigurationManager man;
    private Konfiguration k;

    @BeforeMethod
    public void setup() {
        this.flag.set(true);
        this.man = fac.kombine(fac.map("map-sup", sup));
        this.k = man.getAndSetToNull();
    }

    @Test
    public void testV1() throws Exception {

        assertEquals(k.int_("xxx").v(), (Integer) 12);

        flag.set(!flag.get());
        man.updateNow();

        assertEquals(k.int_("xxx").v(), (Integer) 99);
    }

    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testV3() throws Exception {
        //noinspection ResultOfMethodCallIgnored
        k.string("xxx").v();
    }


    @Test
    public void testDoublyUpdate() throws Exception {
        assertEquals(k.int_("xxx").v(), (Integer) 12);

        k.register(key -> {});
        assertEquals(k.int_("xxx").v(), (Integer) 12);

        flag.set(!flag.get());

        k.register(key -> {});
        assertEquals(k.int_("xxx").v(), (Integer) 12);

        assertFalse(man.update().isEmpty());
        assertEquals(k.int_("xxx").v(), (Integer) 99);

        assertTrue(man.update().isEmpty());
        assertEquals(k.int_("xxx").v(), (Integer) 99);
    }


    @Test(expectedExceptions = KfgMissingKeyException.class)
    public void testNoDefaultValue() {
        //noinspection ResultOfMethodCallIgnored
        k.long_("some bla bla bla").v();
    }

    @Test
    public void testDefaultValue() {
        assertEquals(k.long_("some.bla.bla.bla").v(9876L), (Long) 9876L);
    }

}
