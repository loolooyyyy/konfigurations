package io.koosha.konfiguration;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;

@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored", "FieldCanBeLocal", "DefaultAnnotationParam"})
public class KonfigurationKombinerConcurrencyTest {

    public static final boolean NONDETERMINISTIC_TESTS = true;

    final Faktory fac = Faktory.defaultImplementation();

    private final Map<String, Object> MAP0 = Map.of("aInt", 12, "aBool", false, "aIntList", asList(1, 0, 2), "aLong", 88L);
    private final Map<String, Object> MAP1 = Map.of("aInt", 99, "bBool", false, "aIntList", asList(2, 2));
    private final Map<String, Object> MAP2 = Map.of("xx", 44, "yy", true);
    private final String JSON0 = DummyCustom.JSON_SAMPLE_0;
    private final String JSON1 = DummyCustom.JSON_SAMPLE_1;

    volatile boolean run = true;
    long c = 0;

    Map<String, Object> map;
    String json;
    Konfiguration k;
    KonfigurationManager km;

    @BeforeMethod
    void reset() {
        this.map = this.MAP0;
        this.json = this.JSON0;
    }

    void toggle() {
        this.json = Objects.equals(this.json, JSON0) ? JSON1 : JSON0;
        this.map = Objects.equals(this.map, MAP0) ? MAP1 : MAP0;
    }

    @BeforeMethod
    public void setup() {
        this.reset();
        KonfigurationManager IN_MEM_2_SOURCE = fac.map(() -> this.MAP2);
        KonfigurationManager inMemSource = fac.map(() -> this.map);
        KonfigurationManager jsonSource = fac.jacksonJson_(() -> this.json);
        this.km = fac.kombine(inMemSource, IN_MEM_2_SOURCE, jsonSource);
        this.k = km.getAndSetToNull();
    }

    @Test(enabled = NONDETERMINISTIC_TESTS)
    public void benchmark() {
        ExecutorService e = null;
        try {
            e = Executors.newSingleThreadExecutor();
            e.submit(() -> {
                while (run) {
                    toggle();
                    km.update();
                    c++;
                }
            });

            final long loops = 1_000_000L;

            long total = 0;
            for (int i = 0; i < loops; i++) {
                final long b = System.currentTimeMillis();
                k.int_("xxx" + i).v(2);
                k.int_("aInt").v();
                total += System.currentTimeMillis() - b;
            }

            run = false;

            System.out.println("total time: " + total);
            System.out.println("each cycle time:" + ((double) total) / loops);
            System.out.println("total update count: " + c);
        }
        finally {
            if (e != null)
                e.shutdown();
        }
    }

    @Test(enabled = NONDETERMINISTIC_TESTS)
    public void testMissedUpdates() {
        ExecutorService e = null;
        try {
            e = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
                e.submit(() -> {
                    while (run) {
                        toggle();
                        km.update();
                    }
                });
            }

            for (int i = 0; i < 10_000; i++) {
                k.int_("aInt").v();
                // Uncomment to make sure update happens
                //            Assert.assertEquals(value, (Integer) 12);
                // Add the damn Sl4j already!
                if (i % 1000 == 0)
                    System.out.println(i);
            }

            run = false;
        }
        finally {
            if (e != null)
                e.shutdown();
        }
    }

}
