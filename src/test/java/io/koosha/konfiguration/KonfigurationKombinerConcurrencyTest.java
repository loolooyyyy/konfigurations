package io.koosha.konfiguration;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;

// import java.io.File;
// import java.net.URL;
// import java.nio.charset.StandardCharsets;
// import java.util.Scanner;

@SuppressWarnings("WeakerAccess")
public class KonfigurationKombinerConcurrencyTest {

    KonfigurationKombinerConcurrencyTest() throws Exception{

        // URL url0 = getClass().getResource("sample0.json");
        // File file0 = new File(url0.toURI());
        // this.JSON0 = new Scanner(file0, "UTF8").useDelimiter("\\Z").next();
        this.JSON0 = SourceJacksonJsonTest.SAMPLE_0;

        // URL url1 = getClass().getResource("sample1.json");
        // File file1 = new File(url1.toURI());
        // this.JSON1 = new Scanner(file1, "UTF8").useDelimiter("\\Z").next();
        this.JSON1 = SourceJacksonJsonTest.SAMPLE_1;

        this.MAP0 = Map.of("aInt", 12, "aBool", false, "aIntList", asList(1, 0, 2), "aLong", 88L);


        this.MAP1 = Map.of("aInt", 99, "bBool", false, "aIntList", asList(2, 2));


        this.MAP2 = Map.of("xx", 44, "yy", true);


        // ----------------------

        this.reset();

        Konfiguration IN_MEM_2_SOURCE = Konfiguration.inMemory(() -> KonfigurationKombinerConcurrencyTest.this.MAP2);

        Konfiguration inMemSource = Konfiguration.inMemory(() -> KonfigurationKombinerConcurrencyTest.this.map);

        Konfiguration jsonSource = Konfiguration.jacksonJson(() -> KonfigurationKombinerConcurrencyTest.this.json);

        this.k = Konfiguration.kombine(inMemSource, IN_MEM_2_SOURCE, jsonSource);

    }

    private final Map<String, Object> MAP0;
    private final Map<String, Object> MAP1;
    private final Map<String, Object> MAP2;
    private final String JSON0;
    private final String JSON1;

    volatile boolean run = true;
    long c = 0;

    private Map<String, Object> map;
    private String json;
    private Konfiguration k;

    @BeforeMethod
    void reset() {

        this.map = this.MAP0;
        this.json = this.JSON0;
    }

    private void toggle() {

        this.json = Objects.equals(this.json, JSON0) ? JSON1 : JSON0;
        this.map = Objects.equals(this.map, MAP0) ? MAP1 : MAP0;
    }

    @Test(enabled = false)
    public void benchmark() {
        ExecutorService e = null;
        try {
            e = Executors.newSingleThreadExecutor();
            e.submit(() -> {
                while (run) {
                    KonfigurationKombinerConcurrencyTest.this.toggle();
                    k.update();
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
            if(e != null)
                e.shutdown();
        }
    }

    @Test(enabled = false)
    public void testMissedUpdates() {

        ExecutorService e = null;
        try {
            e = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
                e.submit(() -> {
                    while (run) {
                        KonfigurationKombinerConcurrencyTest.this.toggle();
                        k.update();
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
            if(e != null)
                e.shutdown();
        }
    }

}
