package cc.koosha.konfiguration.impl;

import lombok.val;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;


public class KonfigurationKombinerConcurrencyTest {

    private Map<String, Object> mapA;
    private Map<String, Object> map0;
    private Map<String, Object> map1;
    private Map<String, Object> mapB;

    private String json;
    private String json0;
    private String json1;

    private KonfigurationKombiner k;

    @BeforeClass
    public void classSetup() throws Exception {

        val url0 = getClass().getResource("sample0.json");
        val url1 = getClass().getResource("sample1.json");

        this.json0 = new Scanner(new File(url0.toURI()), "UTF8").useDelimiter("\\Z").next();
        this.json1 = new Scanner(new File(url1.toURI()), "UTF8").useDelimiter("\\Z").next();

        this.map0 = new HashMap<>();
        map0.put("aInt", 12);
        map0.put("aBool", false);
        map0.put("aIntList", asList(1, 0, 2));
        map0.put("aLong", 88L);
        this.map0 = Collections.unmodifiableMap(this.map0);

        // --------------

        this.map1 = new HashMap<>();
        map1.put("aInt", 99);
        map1.put("bBool", false);
        map1.put("aIntList", asList(2, 2));
        this.map1 = Collections.unmodifiableMap(this.map1);

        // --------------

        this.mapB = new HashMap<>();
        mapB.put("xx", 44);
        mapB.put("yy", true);
        this.mapB = Collections.unmodifiableMap(this.mapB);
    }

    private void reset() {

        mapA = map0;
        val inMemA = new InMemoryKonfigSource(new InMemoryKonfigSource.KonfigMapProvider() {
            @Override
            public Map<String, Object> get() {
                return mapA;
            }
        });

        val inMemB = new InMemoryKonfigSource(new InMemoryKonfigSource.KonfigMapProvider() {
            @Override
            public Map<String, Object> get() {
                return mapB;
            }
        });

        json = json0;
        val j = new JsonKonfigSource(new JsonKonfigSource.KSupplier<String>() {
            @Override
            public String get() {
                return json;
            }
        });

        this.k = new KonfigurationKombiner(inMemA, inMemB, j);
    }

    @BeforeMethod
    public void setup() throws Exception {

        reset();
    }

    @SuppressWarnings("StringEquality")
    private void update() {

        mapA = mapA == map0 ?  map1 : map0;
        json = json == json0 ? json1 : json0;
    }


    boolean run = true;
    long c = 0;

    @Test(enabled = false)
    public void benchmark() {

        val e = Executors.newSingleThreadExecutor();
        e.submit(new Runnable() {
            @Override
            public void run() {
                while (run) {
                    update();
                    k.update();
                    c++;
                }
            }
        });

        final long loops = 1_000_000L;

        long total = 0;
        for (int i = 0; i < loops; i++) {
            final long b = System.currentTimeMillis();
            k.intD("xxx" + i).v(2);
            k.int_("aInt").v();
            total += System.currentTimeMillis() - b;
        }

        run = false;
        e.shutdown();
        e.shutdownNow();

        System.out.println("total: " + total + " - " + ((double)total) / loops);
        System.out.println("up: " + c);
    }

    @Test
    public void testMissedUpdates() {

        val e = Executors.newSingleThreadExecutor();
        e.submit(new Runnable() {
            @Override
            public void run() {
                while (run) {
                    update();
                    k.update();
                }
            }
        });

        long total = 0;
        for (int i = 0; i < 1_000_000; i++) {
            final long b = System.currentTimeMillis();
            k.intD("xxx" + i).v(2);
            k.int_("aInt").v();
            total += System.currentTimeMillis() - b;
        }

        run = false;
        e.shutdown();
        e.shutdownNow();
    }

}
