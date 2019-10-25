package io.koosha.konfiguration;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;

import java.beans.ConstructorProperties;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.koosha.konfiguration.TypeName.*;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Reads konfig from a yaml source (supplied as string).
 *
 * <p>for {@link #custom(String, Class)} to work, the supplied yaml reader must
 * be configured to handle arbitrary types accordingly.
 *
 * <p>Thread safe and immutable.
 */
@SuppressWarnings("WeakerAccess")
public final class YamlKonfigSource implements KonfigSource {

    private final static String DOT = Pattern.quote(".");

    @SuppressWarnings("unchecked")
    private <T> Class<T> sanitizeType(final Class<T> type) {
        if (type == int.class) {
            return (Class<T>) Integer.class;
        }
        if (type == long.class) {
            return (Class<T>) Long.class;
        }
        if (type == double.class) {
            return (Class<T>) Double.class;
        }
        if (type == float.class) {
            return (Class<T>) Float.class;
        }
        return type;
    }

    private final Supplier<Yaml> mapperSupplier;
    private final Supplier<String> yaml;
    private final Map<?, ?> root;

    private int lastHash;

    private Object node(final String key) {
        if (key == null || key.isEmpty())
            throw new KonfigurationException("empty konfig key");

        boolean any = false;
        Object value = null;
        for (final String k : key.split(DOT)) {
            if (k.isEmpty())
                throw new KonfigurationException(
                        "empty konfig key part: " + key);
            if (!this.root.containsKey(k))
                throw new KonfigurationMissingKeyException(key);
            value = this.root.get(key);
            any = true;
        }

        if (!any)
            throw new KonfigurationMissingKeyException("empty konfig key: " + key);

        if (value == null)
            throw new KonfigurationMissingKeyException("empty konfig value: " + key);

        return value;
    }

    /**
     * Is <b>NOT</b> thread-safe, as noted in the library's doc.
     *
     * @return a parser instance.
     */
    private static Yaml defaultObjectMapper() {
        return new Yaml(constructByJavaBeanConstructorPropertiesAnnotation());
    }

    private static void checkType(final boolean isOk,
                                  final TypeName required,
                                  final Object node,
                                  final String key) {
        if (isOk)
            return;

        throw new KonfigurationTypeException(required.getTName(),
                                             node.getClass().getCanonicalName(),
                                             key);
    }


    public static <A extends Annotation> Constructor constructByAnnotation(
            final Class<? extends A> markerAnnotation,
            final Function<? super A, String[]> markerAnnotationValueExtractor) {
        return new ByConstructorConstructor<>(markerAnnotation, markerAnnotationValueExtractor);
    }

    public static Constructor constructByJavaBeanConstructorPropertiesAnnotation() {
        return constructByAnnotation(ConstructorProperties.class, ConstructorProperties::value);
    }


    /**
     * Wraps the provided yaml string in a {@link Supplier} and calls
     * {@link #YamlKonfigSource(Supplier)}.
     *
     * @param yaml
     *         constant yaml string as backing storage.
     */
    @SuppressWarnings("unused")
    public YamlKonfigSource(final String yaml) {
        this(() -> yaml);
    }

    /**
     * Calls {@link #YamlKonfigSource(Supplier, Supplier)} with a default
     * Yaml parser provided by {@link #defaultObjectMapper()} ()}.
     *
     * @param yaml
     *         constant yaml string as backing storage.
     */
    public YamlKonfigSource(final Supplier<String> yaml) {
        this(yaml, YamlKonfigSource::defaultObjectMapper);
    }

    /**
     * Creates a {@link YamlKonfigSource} with the given yaml provider and
     * object mapper provider.
     *
     * @param yaml
     *         backing store provider. Must always return a non-null valid yaml
     *         string.
     * @param objectMapper
     *         {@link Yaml} provider. Must always return a valid non-null Yaml,
     *         and if required, it must be able to deserialize custom types, so
     *         that {@link #custom(String, Class)} works as well.
     *
     * @throws NullPointerException
     *         if any of its arguments are null.
     * @throws KonfigurationSourceException
     *         if org.yaml.snakeyaml library is not in the classpath. it
     *         specifically looks for the class: "org.yaml.snakeyaml"
     * @throws KonfigurationSourceException
     *         if the storage (yaml string) returned by yaml string is null.
     * @throws KonfigurationSourceException
     *         if the provided yaml string can not be parsed by Yaml.
     * @throws KonfigurationSourceException
     *         if the the root element returned by yaml is null.
     */
    public YamlKonfigSource(final Supplier<String> yaml,
                            final Supplier<Yaml> objectMapper) {
        requireNonNull(yaml, "yamlSupplier");
        requireNonNull(objectMapper, "objectMapperSupplier");

        // Check early, so we're not fooled with a dummy object reader.
        try {
            Class.forName("org.yaml.snakeyaml.Yaml");
        }
        catch (final ClassNotFoundException e) {
            throw new KonfigurationSourceException(
                    getClass().getName() + " requires org.yaml.snakeyaml library to be present in the class path",
                    e);
        }

        this.yaml = yaml;
        this.mapperSupplier = objectMapper;

        final String newYaml = this.yaml.get();
        if (newYaml == null)
            throw new KonfigurationSourceException("storage is null");

        final Map<?, ?> update;
        try {
            update = this.mapperSupplier.get().load(newYaml);
        }
        catch (final Throwable e) {
            throw new KonfigurationSourceException("error parsing yaml string",
                                                   e);
        }

        if (update == null)
            throw new KonfigurationSourceException("root element is null");

        this.lastHash = newYaml.hashCode();
        this.root = update;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean bool(final String key) {
        final Object at = node(key);
        checkType(at instanceof Boolean, BOOLEAN, at, key);
        return (boolean) at;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer int_(final String key) {
        final Object at = node(key);
        checkType(at instanceof Integer, INT, at, key);
        return (int) at;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long long_(final String key) {
        final Object at = node(key);
        checkType(at instanceof Long, LONG, at, key);
        return (long) at;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double double_(final String key) {
        final Object at = node(key);
        checkType(at instanceof Double, DOUBLE, at, key);
        return (double) at;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String string(final String key) {
        final Object at = node(key);
        checkType(at instanceof String, STRING, at, key);
        return (String) at;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> list(final String key, Class<T> type) {
        type = sanitizeType(type);
        final List<T> l = new ArrayList<>();
        final Object at = node(key);
        checkType(at instanceof List, LIST, at, key);
        for (final Object o : ((List<?>) at)) {
            if (o == null) {
                l.add(null);
            }
            else if (type.isAssignableFrom(o.getClass())) {
                @SuppressWarnings("unchecked")
                final T kast = (T) o;
                l.add(kast);
            }
            else {
                throw new KonfigurationTypeException(
                        "bad type in list, expected=" + type.getCanonicalName() + ", got=" + o
                                .getClass()
                                .getCanonicalName());
            }
        }
        return unmodifiableList(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Map<String, T> map(final String key, Class<T> type) {
        type = sanitizeType(type);
        final Map<String, T> m = new HashMap<>();
        final Object at = node(key);
        checkType(at instanceof Map, MAP, at, key);
        for (final Map.Entry<?, ?> e : ((Map<?, ?>) at).entrySet()) {
            final Object k = e.getKey();
            if (k == null)
                throw new KonfigurationTypeException("null map key at key=" + key);
            if (!(k instanceof String))
                throw new KonfigurationTypeException(
                        "expecting string key, got=" + k.getClass() + ", at=" + key);

            // You'll never know.
            if (m.containsKey(k))
                throw new KonfigurationException("duplicate key in map, at key=" + key + ", offending key=" + k);

            final Object v = e.getValue();
            if (v == null) {
                m.put(((String) k), null);
            }
            else if (type.isAssignableFrom(v.getClass())) {
                @SuppressWarnings("unchecked")
                final T kast = (T) v;
                m.put(((String) k), kast);
            }
            else {
                throw new KonfigurationTypeException(
                        "bad type in map, expected=" + type.getCanonicalName() + ", got=" +
                                v.getClass()
                                 .getCanonicalName() + ", at=" + key);
            }
        }
        return Collections.unmodifiableMap(m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Set<T> set(final String key, Class<T> type) {
        type = sanitizeType(type);
        final List<T> list = this.list(key, type);
        final Set<T> set = new HashSet<>(list);
        if (list.size() != set.size())
            throw new KonfigurationException("duplicate value in set at key=" + key);
        return set;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T custom(final String key, Class<T> type) {
        // Whoops... You didn't see this.
        final Yaml yaml = this.mapperSupplier.get();
        new Yaml(new Constructor());
        return yaml.loadAs(yaml.dump(this.map(key, Object.class)), type);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final String key) {
        try {
            this.node(key);
            return true;
        }
        catch (KonfigurationMissingKeyException k) {
            return false;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUpdatable() {
        String newYaml = this.yaml.get();

        if (newYaml == null)
            return false;

        final int newHash = newYaml.hashCode();
        return newHash != this.lastHash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KonfigSource copyAndUpdate() {
        return new YamlKonfigSource(this.yaml, this.mapperSupplier);
    }


    private static class Param {
        String name;
        Class<?> type;
        Object value;

        Param(final String name) {
            this.name = name;
        }


        final Double double_() {
            return (Double) this.value;
        }

        final byte[] byteArray() {
            return (byte[]) this.value;
        }

        final boolean typeIs(Object... other) {
            for (final Object o : other)
                if (o == this.type)
                    return true;
            return false;
        }

    }

    private static final class ParamNode extends Param {
        Node node;

        public ParamNode(String name, Node node) {
            super(name);
            this.node = node;
        }

        Class<?>[] getActualTypeArguments() {
            return null;
        }
    }

    private static final class ByConstructorConstructor<A extends Annotation> extends Constructor {

        private final Class<? extends A> marker;
        private final Function<? super A, String[]> markerExtractor;

        ByConstructorConstructor(final Class<? extends A> marker,
                                 final Function<? super A, String[]> markerExtractor) {
            super();
            requireNonNull(marker, "marker");
            requireNonNull(markerExtractor, "markerExtractor");
            this.marker = marker;
            this.markerExtractor = markerExtractor;
            this.yamlClassConstructors.put(NodeId.mapping, new KonstructMapping());
        }

        private class KonstructMapping extends Constructor.ConstructMapping {

            @Override
            public Object construct(final Node node) {
                if (Map.class.isAssignableFrom(node.getType()) ||
                        Collection.class.isAssignableFrom(node.getType()) ||
                        typeDefinitions.containsKey(node.getType()))
                    return super.construct(node);

                if (node.isTwoStepsConstruction())
                    throw new YAMLException("encountered two step node: " + node);

                final MappingNode mnode = (MappingNode) node;
                flattenMapping(mnode);

                final List<ParamNode> consArgs = mnode
                        .getValue()
                        .stream()
                        .map(tuple -> {
                            if (!(tuple.getKeyNode() instanceof ScalarNode))
                                throw new YAMLException(
                                        "Keys must be scalars but found: " + tuple.getKeyNode());
                            final ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
                            keyNode.setType(String.class);
                            return new ParamNode((String) constructObject(keyNode), tuple.getValueNode());
                        })
                        .peek(t -> {
                            final Tag tag = t.node.getTag();
                            Class<?> tp = null;
                            if (tag == Tag.INT)
                                tp = Integer.class;
                            else if (tag == Tag.FLOAT)
                                tp = Float.class;
                            else if (tag == Tag.STR)
                                tp = String.class;
                            else if (tag == Tag.MAP)
                                tp = Map.class;
                            else if (tag == Tag.SEQ)
                                tp = List.class;
                            else if (tag == Tag.SET)
                                tp = Set.class;
                            else if (tag == Tag.BOOL)
                                tp = Boolean.class;
                            else if (tag == Tag.NULL)
                                tp = Object.class;
                            t.type = tp;
                            if (tp != null)
                                t.node.setType(tp);
                        })
                        .peek(t -> {
                            if (t.node.getNodeId() != NodeId.scalar) {
                                // only if there is no explicit TypeDescription
                                final Class<?>[] args = t.getActualTypeArguments();
                                if (args != null && args.length > 0) {
                                    // type safe (generic) collection may contain the proper class
                                    if (t.node.getNodeId() == NodeId.sequence) {
                                        ((SequenceNode) t.node).setListType(args[0]);
                                    }
                                    else if (Set.class.isAssignableFrom(t.node.getType())) {
                                        ((MappingNode) t.node).setOnlyKeyType(args[0]);
                                        t.node.setUseClassConstructor(true);
                                    }
                                    else if (Map.class.isAssignableFrom(t.node.getType())) {
                                        ((MappingNode) t.node).setTypes(args[0], args[1]);
                                        t.node.setUseClassConstructor(true);
                                    }
                                }
                            }
                        })
                        .peek(t -> t.value = constructObject(t.node))
                        .peek(t -> {
                            if (t.value instanceof Double && t.typeIs(Float.TYPE, Float.class))
                                t.value = t.double_().floatValue();

                            else if (t.value instanceof byte[] &&
                                    Objects.equals(t.node.getTag(), Tag.BINARY) &&
                                    t.typeIs(String.class))
                                t.value = new String(t.byteArray());
                        })
                        .collect(toList());

                final Map<String, ParamNode> byName = consArgs
                        .stream()
                        .collect(Collectors.toMap(ca -> ca.name, Function.identity()));

                final List<String> names = consArgs
                        .stream()
                        .map(t -> t.name)
                        .collect(toList());

                final Class<?>[] types = consArgs
                        .stream()
                        .map(t -> t.type)
                        .toArray(Class<?>[]::new);

                final Object[] values = consArgs
                        .stream()
                        .map(t -> t.value)
                        .toArray();

                java.lang.reflect.Constructor<?> c0;
                try {
                    c0 = find(marker,
                              markerExtractor,
                              node.getType(),
                              byName, names);
                }
                catch (YAMLException y) {
                    c0 = null;
                }

                if (c0 == null)
                    try {
                        c0 = node.getType().getDeclaredConstructor(types);
                    }
                    catch (NoSuchMethodException e) {
                        // ignore
                    }


                if (c0 == null)
                    try {
                        final Class<?>[] types2 = consArgs
                                .stream()
                                .map(t -> t.type)
                                .map(YamlKonfigSource::lower)
                                .toArray(Class<?>[]::new);
                        c0 = node.getType().getDeclaredConstructor(types2);
                    }
                    catch (NoSuchMethodException ex) {
                        c0 = null;
                    }

                if (c0 == null)
                    throw new YAMLException("no constructor found for: " + node);

                try {
                    c0.setAccessible(true);
                    return c0.newInstance(values);
                }
                catch (Exception e) {
                    throw new YAMLException(e);
                }
            }

        }

    }

    @SuppressWarnings("SameParameterValue")
    private static <A extends Annotation> java.lang.reflect.Constructor<?> find(
            final Class<? extends A> marker,
            final Function<? super A, String[]> markerExtractor,
            final Class<?> origin,
            final Map<String, ? extends Param> cArgsByName,
            final List<String> cArgNames) {
        final List<java.lang.reflect.Constructor<?>> constructors = Arrays
                .stream(origin.getDeclaredConstructors())
                .filter(it -> it.getAnnotation(marker) != null)
                .filter(it -> asList(markerExtractor.apply(it.getAnnotation(marker))).containsAll(cArgNames)
                        && cArgNames.containsAll(asList(markerExtractor.apply(it.getAnnotation(marker)))))
                .filter(it -> {
                    final Parameter[] ps = it.getParameters();
                    final String[] ns = markerExtractor.apply(it.getAnnotation(marker));
                    for (int i = 0; i < ns.length; i++)
                        if (!ps[i].getType().isAssignableFrom(cArgsByName.get(ns[i]).type))
                            return false;
                    return true;
                })
                .collect(toList());
        if (constructors.isEmpty())
            throw new YAMLException("no constructor with ConstructorProperties is liable");
        if (constructors.size() > 1)
            throw new YAMLException("multiple constructor with ConstructorProperties are liable");
        return constructors.get(0);
    }

    private static Class<?> lower(Class<?> c) {
        if (c == Boolean.class)
            return boolean.class;
        if (c == Integer.class)
            return int.class;
        if (c == Long.class)
            return long.class;
        if (c == Float.class)
            return float.class;
        if (c == Double.class)
            return double.class;
        return c;
    }

}
