package io.koosha.konfiguration;

import java.beans.ConstructorProperties;
import java.util.*;

import static java.util.Arrays.asList;

@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess", "unused"})
public class DummyCustom {

    public static final Map<String, Object> MAP0 = map(
            "aInt", 12,
            "aBool", true,
            "aIntList", asList(1, 0, 2),
            "aStringList", asList("a", "B", "c"),
            "aLong", Long.MAX_VALUE,
            "aDouble", 3.14D,
            "aString", "hello world",
            "aMap", map("a", 99, "c", 22),
            "aSet", new HashSet<>(asList(1, 2))
    );
    public static final Map<String, Object> MAP1 = map(
            "aInt", 99,
            "aBool", false,
            "aIntList", asList(2, 2),
            "aStringList", asList("a", "c"),
            "aLong", Long.MIN_VALUE,
            "aDouble", 4.14D,
            "aString", "goodbye world",
            "aMap", map("a", "b", "c", "e"),
            "aSet", new HashSet<>(asList(1, 2, 3))
    );
    public static final String JSON_SAMPLE_0 = "{ \"aInt\": 12, \"aBool\": true, " +
            "\"aIntList\": [1, 0, 2], \"aStringList\": [\"a\", \"B\", \"c\"], " +
            "\"aLong\": 9223372036854775807, \"aDouble\": 3.14, \"aMap\": " +
            "{ \"a\": 99, \"c\": 22 }, \"aSet\": [1, 2], \"aString\": " +
            "\"hello world\", \"some\": { \"nested\": { \"key\": 99, " +
            "\"userDefined\" : { \"str\": \"I'm all set\", \"i\": 99 } } }, " +
            " \"aBadSet\": [1, 2, 1, 2]" +
            "}";
    public static final String JSON_SAMPLE_1 = "{ \"aInt\": 99, \"aBool\": false, " +
            "\"aIntList\": [2, 2], \"aStringList\": [\"a\", \"c\"], \"aLong\": " +
            "-9223372036854775808, \"aDouble\": 4.14, \"aMap\": { \"a\": \"b\", " +
            "\"c\": \"e\" }, \"aSet\": [3, 2, 1], \"aString\": \"goodbye world\" }";
    public static final String YAML_SAMPLE_0 = "aInt: 12\n" +
            "aBool: true\n" +
            "aIntList: \n" +
            "  - 1\n" +
            "  - 0\n" +
            "  - 2\n" +
            "aStringList: [\"a\", \"B\", \"c\"]\n" +
            "aLong: 9223372036854775807\n" +
            "aDouble: 3.14\n" +
            "aMap:\n" +
            "    \"a\": 99\n" +
            "    \"c\": 22\n" +
            "aSet: [1, 2]\n" +
            "aString: \"hello world\"\n" +
            "\n" +
            "some:\n" +
            "    nested: \n" +
            "        key: 99\n" +
            "        userDefined: \n" +
            "            str: \"I'm all set\"\n" +
            "            i: 99\n" +
            "        \n";
    public static final String YAML_SAMPLE_1 = "aInt: 99\n" +
            "aBool: false\n" +
            "aIntList: [2, 2]\n" +
            "aStringList: [\"a\", \"c\"]\n" +
            "aLong: -9223372036854775808\n" +
            "aDouble: 4.14\n" +
            "aMap: \n" +
            "   a: \"b\"\n" +
            "   c: \"e\"\n" +
            "aSet: [3, 2, 1]\n" +
            "aString: \"goodbye world\"\n";
    public String str = "";
    public int i = 0;

    public DummyCustom() {
    }

    @ConstructorProperties({"str", "i"})
    public DummyCustom(final String str, final int i) {
        this.str = str;
        this.i = i;
    }

    public String concat() {
        return this.str + " ::: " + this.i;
    }

    // =========================================================================

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> map(final Object... objects) {
        Objects.requireNonNull(objects);
        if (objects.length % 2 != 0)
            throw new IllegalArgumentException("count: " + objects.length +
                    " " + Arrays.toString(objects));
        final Map<K, V> m = new HashMap<>();
        for (int i = 0; i < objects.length; i += 2) {
            final K k = (K) objects[i];
            final V v = (V) objects[i + 1];
            m.put(k, v);
        }
        return Collections.unmodifiableMap(m);
    }

}
