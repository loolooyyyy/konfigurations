package cc.koosha.konfigurations.core;

import lombok.val;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class KonfigurationKombinerMemoryLeakTest extends KonfigurationKombinerBaseTest {

    private final AtomicBoolean run = new AtomicBoolean(false);

    @Test
    public void testMemLeak() throws Exception {

        val f = KonfigurationKombiner.class.getDeclaredField("observers");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        val observers = (WeakHashMap<KeyObserver, Collection<String>>) f.get(kk);

        KeyObserver obs = new KeyObserver() {
            @Override
            public void accept(final String key) {

            }

            @Override
            protected void finalize() throws Throwable {
                run.set(true);
                super.finalize();
            }
        };

        kk.string("str key").register(obs);
        assertEquals(observers.size(), 1);

        obs = null;

        System.runFinalization();
        System.gc();
        System.runFinalization();
        System.gc();

        assertTrue(run.get());
    }

}
