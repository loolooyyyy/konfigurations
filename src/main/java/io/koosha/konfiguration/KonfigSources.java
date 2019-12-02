package io.koosha.konfiguration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;

import java.beans.ConstructorProperties;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.koosha.konfiguration.Q.nn;
import static io.koosha.konfiguration.TypeName.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

final class KonfigSources {

    private KonfigSources() {
    }

    // =========================================================================

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

    private static int hashOf(final Konfiguration source,
                              final Preferences pref) {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            pref.exportSubtree(buffer);
        }
        catch (IOException | BackingStoreException e) {
            throw new KfgSourceException(source, null, null, null, "could not calculate hash of the java.util.prefs.Preferences source", null);
        }
        return Arrays.hashCode(buffer.toByteArray());
    }

    private static ObjectMapper defaultJacksontObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    private static Yaml defaultSnakeYamlObjectMapper() {
        return new Yaml(new ByConstructorConstructor<>(
                (Class<? extends ConstructorProperties>) ConstructorProperties.class,
                (Function<? super ConstructorProperties, String[]>) ConstructorProperties::value
        ));
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
            nn(marker, "marker");
            nn(markerExtractor, "markerExtractor");
            this.marker = marker;
            this.markerExtractor = markerExtractor;
            this.yamlClassConstructors.put(NodeId.mapping, new KonstructMapping());
        }

        private class KonstructMapping extends ConstructMapping {

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
                                .map(KonfigSources::lower)
                                .toArray(Class<?>[]::new);
                        c0 = node.getType().getDeclaredConstructor(types2);
                    }
                    catch (NoSuchMethodException ex) {
                        c0 = null;
                    }

                nn(c0, "no constructor found for: " + node);

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


    private final static String DOT = Pattern.quote(".");

    @SuppressWarnings("unchecked")
    private static <U> Class<U> sanitizeType(final Class<U> type) {
        if (type == int.class) {
            return (Class<U>) Integer.class;
        }
        if (type == long.class) {
            return (Class<U>) Long.class;
        }
        if (type == double.class) {
            return (Class<U>) Double.class;
        }
        if (type == float.class) {
            return (Class<U>) Float.class;
        }
        return type;
    }

    // =========================================================================


    private abstract static class BaseKonfigSource extends AbstractKonfiguration {

        final boolean readonly;

        BaseKonfigSource(final String name, final boolean readonly) {
            super(name);
            this.readonly = readonly;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration readonly() {
            throw new UnsupportedOperationException("do not use this directly, put this source in a kombiner");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration register(KeyObserver observer) {
            throw new UnsupportedOperationException("do not use this directly, put this source in a kombiner");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration deregister(KeyObserver observer) {
            throw new UnsupportedOperationException("do not use this directly, put this source in a kombiner");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration subset(String name, String key) {
            throw new UnsupportedOperationException("do not use this directly, put this source in a kombiner");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final boolean hasUpdate() {
            if (readonly)
                return false;
            else
                return hasUpdate0();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final Konfiguration update() {
            if (readonly)
                return this;
            else
                return update0();
        }


        abstract boolean hasUpdate0();

        abstract Konfiguration update0();

    }


    /**
     * Reads konfig from a plain java map.
     *
     * <p>To fulfill contract of {@link Konfiguration}, all the values put in
     * the map of konfiguration key/values supplied to the konfiguration,
     * should be immutable.
     *
     * <p>Thread safe and immutable.
     */
    static class MapKonfiguration extends BaseKonfigSource {

        private final Supplier<Map<String, ?>> storageProvider;
        private final Map<String, ?> storage;

        MapKonfiguration(final String name,
                         final boolean readonly,
                         final Supplier<Map<String, ?>> storage) {
            super(name, readonly);
            nn(storage, "storage");
            this.storageProvider = storage;

            final Map<String, ?> newStorage = this.storageProvider.get();
            nn(newStorage, "newStorage");

            this.storage = Collections.unmodifiableMap(new HashMap<>(newStorage));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        boolean hasUpdate0() {
            return !this.storageProvider.get().equals(this.storage);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        Konfiguration update0() {
            return new MapKonfiguration(this.getName(), this.readonly, this.storageProvider);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final boolean contains(String key) {
            return this.storage.containsKey(key);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        protected final Object getPrimitive(String key, TypeName type) {
            return this.storage.get(key);
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        protected final <U> U getCustom(String key, Q<U> type) {
            return (U) this.storage.get(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected final Object getContainer(String key, Q<?> type) {
            return this.storage.get(key);
        }

    }

    /**
     * Reads konfig from a {@link Preferences} source.
     *
     * <p>for {@link #custom(String, Q)} to work, the supplied deserializer
     * must be configured to handle arbitrary types accordingly.
     *
     * <p><b>IMPORTANT</b> Does not coup too well with keys being added / removed
     * from backing source. Only changes are supported (as stated in
     * {@link Preferences#addNodeChangeListener(NodeChangeListener)})
     *
     * <p>Thread safe and immutable.
     *
     * <p>For now, pref change listener is not used
     */
    static final class PreferencesKonfiguration extends BaseKonfigSource {

        private final Deserializer<Preferences> deser;
        private final Preferences source;
        private final int lastHash;

        PreferencesKonfiguration(final String name,
                                 final boolean readonly,
                                 final Preferences preferences,
                                 final Deserializer<Preferences> deserializer) {
            super(name, readonly);
            this.source = nn(preferences, "preferences");
            this.deser = deserializer;
            this.lastHash = hashOf(this, source);
        }


        private String sane(final String key) {
            nn(key, "empty konfig key");
            return key.replace('.', '/');
        }

        private boolean exists(final String key) {
            try {
                return source.nodeExists(sane(key));
            }
            catch (Throwable e) {
                throw new KfgSourceException(this, key, null, null, "error checking existence of key", e);
            }
        }

        private String check(final String key) {
            if (!exists(sane(key)))
                throw new KfgMissingKeyException(this, key, null, null);
            return key;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(String key) {
            return exists(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        boolean hasUpdate0() {
            return this.lastHash != hashOf(this, source);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        Konfiguration update0() {
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Object getPrimitive(final String key,
                                      final Q<?> type) {
            if (!this.contains(key))
                return null;

            switch (type) {
                case BOOL:
                    return this.source.getBoolean(key, false);

                case CHAR:
                    final String asString = (String) this.getPrimitive(key, Q.STRING);
                    if (asString == null)
                        return null;
                    if (asString.length() == 0)
                        return null;
                    else if (asString.length() == 1)
                        return asString.charAt(0);
                    else
                        throw new KfgTypeException(this, key, Q.BYTE, asString);

                case BYTE:
                case SHORT:
                case INT:
                    return this.source.getInt(key, 0);

                case LONG:
                    return this.source.getLong(key, 0);

                case FLOAT:
                    return this.source.getFloat(key, 0);

                case DOUBLE:
                    return this.source.getDouble(key, 0);

                case STRING:
                    return this.source.get(key, null);

                default:
                    throw new KfgIllegalStateException(
                            this, key, type, null, "assertion error: not a primitive");
            }
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        protected <U> U getCustom(String key, Q<U> type) {
            if (!this.contains(key))
                return null;

            if (this.deser == null)
                throw new UnsupportedOperationException("deserializer not set");

            return (U) this.deser.custom(this.source, type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Object getContainer(final String key, final Q<?> type) {
            nn(key, "key");
            nn(type, "type");

            if (!this.contains(key))
                return null;

            if (this.deser == null)
                throw new UnsupportedOperationException("deserializer not set");

            switch (type.typeName()) {
                case LIST:
                    return this.deser.list(this.source, type);

                case SET:
                    return this.deser.set(this.source, type);

                case MAP:
                    return this.deser.map(this.source, type);

                default:
                    throw new KfgException("assertion error, not a primitive");
            }
        }

    }

    /**
     * Reads konfig from a json source (supplied as string).
     *
     * <p>for {@link #custom(String, Q)} to work, the supplied json reader must
     * be configured to handle arbitrary types accordingly.
     *
     * <p>Thread safe and immutable.
     */
    static final class JsonKonfigSource extends BaseKonfigSource {

        private final Supplier<ObjectMapper> mapperSupplier;
        private final Supplier<String> json;
        private int lastHash;
        private JsonNode root;


        private JsonNode node_(final String key) {
            if (nn(key, "key").isEmpty())
                throw new KfgMissingKeyException(this, key, null, null, "empty konfig key");

            final String k = "/" + key.replace('.', '/');
            return this.root.at(k);
        }

        private JsonNode node(final String key) {
            if (nn(key, "key").isEmpty())
                throw new KfgMissingKeyException(this, key, null, null, "empty konfig key");

            final JsonNode node = node_(key);
            if (node.isMissingNode())
                throw new KfgMissingKeyException(this, key, null, null);
            return node;
        }


        private JsonNode checkJsonType(final boolean condition,
                                       final Q<?> required,
                                       final JsonNode node,
                                       final String key) {
            if (!condition)
                throw new KfgTypeException(this, key, required, node.toString());
            return node;
        }


        /**
         * Creates an instance with a default object mapper provided by
         * {@link #defaultJacksontObjectMapper()} ()}.
         *
         * @param name     name of this source
         * @param readonly if this source should never update itself.
         * @param json     constant json string as backing storage.
         */
        JsonKonfigSource(final String name,
                         final boolean readonly,
                         final Supplier<String> json) {
            this(name, readonly, json, new Supplier<ObjectMapper>() {
                private final ObjectMapper mapper = defaultJacksontObjectMapper();

                @Override
                public ObjectMapper get() {
                    return mapper;
                }
            });
        }

        /**
         * Creates an instance with a with the given json
         * provider and object mapper provider.
         *
         * @param name         name of this source
         * @param readonly     if this source should never update itself.
         * @param json         backing store provider. Must always return a non-null valid json
         *                     string.
         * @param objectMapper {@link ObjectMapper} provider. Must always return a valid
         *                     non-null ObjectMapper, and if required, it must be able to
         *                     deserialize custom types, so that {@link #custom(String, Q)}
         *                     works as well.
         * @throws NullPointerException if any of its arguments are null.
         * @throws KfgSourceException   if jackson library is not in the classpath. it specifically looks
         *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
         * @throws KfgSourceException   if the storage (json string) returned by json string is null.
         * @throws KfgSourceException   if the provided json string can not be parsed by jackson.
         * @throws KfgSourceException   if the the root element returned by jackson is null.
         */
        JsonKonfigSource(final String name,
                         final boolean readonly,
                         final Supplier<String> json,
                         final Supplier<ObjectMapper> objectMapper) {
            super(name, readonly);
            nn(json, "jsonSupplier");
            nn(objectMapper, "objectMapperSupplier");
            // Check early, so we're not fooled with a dummy object reader.
            try {
                Class.forName("com.fasterxml.jackson.databind.JsonNode");
            }
            catch (final ClassNotFoundException e) {
                throw new KfgSourceException(this, null, null, null, "jackson library is required to be present in the class path," +
                                        "can not find the class: com.fasterxml.jackson.databind.JsonNode", e);
            }

            this.json = json;
            this.mapperSupplier = objectMapper;

            final String newJson = this.json.get();
            nn(newJson, "storage is null");

            final JsonNode update;
            try {
                update = this.mapperSupplier.get().readTree(newJson);
            }
            catch (final IOException e) {
                throw new KfgSourceException(this, null, null, null, "error parsing json string", e);
            }

            nn(update, "root element is null");

            this.root = update;
            this.lastHash = newJson.hashCode();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        protected final Object getPrimitive(String key, TypeName type) {
            if (!this.contains(key))
                return null;

            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            switch (type) {
                case BOOL:
                    return checkJsonType(at.isBoolean(), BOOL, at, key).asBoolean();

                case CHAR:
                    final String asString = (String) this.getPrimitive(key, STRING);
                    if (asString == null)
                        return null;
                    if (asString.length() == 0)
                        return null;
                    else if (asString.length() == 1)
                        return asString.charAt(0);
                    else
                        throw new KfgTypeException(this, key, BYTE, null, asString);

                case BYTE:
                case SHORT:
                case INT:
                case LONG:
                    return checkJsonType(
                            at.isShort() || at.isInt() || at.isLong(),
                            type, at, key).longValue();

                case FLOAT:
                case DOUBLE:
                    return checkJsonType(
                            at.isFloat()
                                    || at.isDouble()
                                    || at.isShort()
                                    || at.isInt()
                                    || at.isLong(),
                            type, at, key).doubleValue();

                case STRING:
                    if (at.isArray()) {
                        final StringBuilder sb = new StringBuilder();
                        for (final JsonNode jsonNode : at)
                            sb.append(checkJsonType(jsonNode.isTextual(), STRING, at, key).textValue());
                        return sb.toString();
                    }
                    return checkJsonType(at.isTextual(), STRING, at, key).textValue();

                case CUSTOM:
                default:
                    return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected final <U> U getCustom(String key, Q<U> type) {
            final ObjectMapper reader = this.mapperSupplier.get();
            final JsonParser traverse = this.node(key).traverse();

            try {
                return reader.readValue(traverse, type.klass());
            }
            catch (final IOException e) {
                throw new KfgTypeException(this, key, type.typeName(), type, "?", e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected final Object getContainer(String key, Q<?> type) {
            TypeName typeName = type.typeName();
            if (typeName == LIST) {
                final JsonNode at = node(key);
                if (at.isNull())
                    return null;

                checkJsonType(at.isArray(), LIST, at, key);
                final ObjectMapper reader = this.mapperSupplier.get();
                final CollectionType javaType = reader.getTypeFactory().constructCollectionType(List.class, type.klass());

                try {
                    return reader.readValue(at.traverse(), javaType);
                }
                catch (final IOException e) {
                    throw new KfgTypeException(this, key, LIST, type, "?",
                            "can not read the key [" + key + "]" + " as a list of [" + type + "]", e);
                }
            }
            else if (typeName == MAP) {
                final JsonNode at = node(key);
                if (at.isNull())
                    return null;

                checkJsonType(at.isObject(), MAP, at, key);
                final ObjectMapper reader = this.mapperSupplier.get();
                final MapType javaType = reader.getTypeFactory().constructMapType(Map.class, String.class, type.klass());

                try {
                    return reader.readValue(at.traverse(), javaType);
                }
                catch (final IOException e) {
                    throw new KfgTypeException(this, key, LIST, type, "?",
                            "can not read the key [" + key + "]" + " as a map of [" + type + "]", e);
                }
            }
            else if (typeName == SET) {
                final JsonNode at = node(key);
                if (at.isNull())
                    return null;

                checkJsonType(at.isArray(), SET, at, key);
                final ObjectMapper reader = this.mapperSupplier.get();
                final CollectionType javaType = reader.getTypeFactory().constructCollectionType(Set.class, type.klass());

                final Set<?> s;
                try {
                    s = reader.readValue(at.traverse(), javaType);
                }
                catch (final IOException e) {
                    throw new KfgTypeException(this, key, LIST, type, "?",
                            "can not read the key [" + key + "]" + " as a set of [" + type + "]", e);

                }
                return s;
            }
            throw new IllegalStateException("unexpected collection type: " + type);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public final boolean contains(final String key) {
            return !this.node_(key).isMissingNode();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        boolean hasUpdate0() {
            String newJson = this.json.get();

            if (newJson == null)
                return false;

            final int newHash = newJson.hashCode();
            return newHash != this.lastHash;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        Konfiguration update0() {
            return new JsonKonfigSource(this.getName(), this.readonly, this.json, this.mapperSupplier);
        }

    }

    /**
     * Reads konfig from a yaml source (supplied as string).
     *
     * <p>for {@link #custom(String, Class)} to work, the supplied yaml reader must
     * be configured to handle arbitrary types accordingly.
     *
     * <p>Thread safe and immutable.
     */
    static final class SnakeYamlKonfigSource extends BaseKonfigSource {

        private final Supplier<Yaml> mapperSupplier;
        private final Supplier<String> yaml;
        private final Map<?, ?> root;

        private int lastHash;

        private Object node(final String key) {
            nn(key, "key");
            if (key.isEmpty())
                throw new KfgException("empty konfig key");

            boolean any = false;
            Object value = null;
            for (final String k : key.split(DOT)) {
                if (k.isEmpty())
                    throw new KfgException(
                            "empty konfig key part: " + key);
                if (!this.root.containsKey(k))
                    throw new KfgMissingKeyException(key);
                value = this.root.get(key);
                any = true;
            }

            if (!any)
                throw new KfgMissingKeyException("empty konfig key: " + key);

            nn(value, "empty konfig value: " + key);

            return value;
        }

        /**
         * Yaml is <b>NOT</b> thread-safe, as noted in the library's doc.
         */
        private static final ThreadLocal<Yaml> defaultSupplier =
                ThreadLocal.withInitial(KonfigSources::defaultSnakeYamlObjectMapper);

        @SuppressWarnings("unchecked")
        private <U> U checkYamlType(final boolean isOk,
                                    final TypeName required,
                                    final Object node,
                                    final String key) {
            if (!isOk)
                throw new KfgTypeException(this, key, required, null, node);
            return (U) node;
        }

        /**
         * Creates an instance with a default Yaml parser.
         *
         * @param yaml constant yaml string as backing storage.
         */
        SnakeYamlKonfigSource(final String name,
                              final boolean readonly,
                              final Supplier<String> yaml) {
            this(name, readonly, yaml, defaultSupplier::get);
        }

        /**
         * Creates an instance with the given Yaml parser.
         *
         * @param yaml         backing store provider. Must always return a non-null valid yaml
         *                     string.
         * @param objectMapper {@link Yaml} provider. Must always return a valid non-null Yaml,
         *                     and if required, it must be able to deserialize custom types, so
         *                     that {@link #custom(String, Class)} works as well.
         * @throws NullPointerException if any of its arguments are null.
         * @throws KfgSourceException   if org.yaml.snakeyaml library is not in the classpath. it
         *                              specifically looks for the class: "org.yaml.snakeyaml"
         * @throws KfgSourceException   if the storage (yaml string) returned by yaml string is null.
         * @throws KfgSourceException   if the provided yaml string can not be parsed by Yaml.
         * @throws KfgSourceException   if the the root element returned by yaml is null.
         */
        SnakeYamlKonfigSource(final String name,
                              final boolean readonly,
                              final Supplier<String> yaml,
                              final Supplier<Yaml> objectMapper) {
            super(name, readonly);
            nn(yaml, "yamlSupplier");
            nn(objectMapper, "objectMapperSupplier");

            // Check early, so we're not fooled with a dummy object reader.
            try {
                Class.forName("org.yaml.snakeyaml.Yaml");
            }
            catch (final ClassNotFoundException e) {
                throw new KfgSourceException(this, null, null, null, "org.yaml.snakeyaml library is required to be" +
                                        " present in the class path, can not find the class:" +
                                        "org.yaml.snakeyaml.Yaml", e);
            }

            this.yaml = yaml;
            this.mapperSupplier = objectMapper;

            final String newYaml = this.yaml.get();
            nn(yaml, "storage is null");

            final Map<?, ?> update;
            try {
                update = this.mapperSupplier.get().load(newYaml);
            }
            catch (final Throwable e) {
                throw new KfgSourceException(this, null, null, null, "error parsing yaml string", e);
            }

            nn(update, "root element is null");

            this.lastHash = newYaml.hashCode();
            this.root = update;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        protected final Object getPrimitive(final String key, final TypeName type) {
            if (!this.contains(key))
                return null;

            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            switch (type) {
                case BOOL:
                    return checkJsonType(at.isBoolean(), BOOL, at, key).asBoolean();

                case CHAR:
                    final String asString = (String) this.getPrimitive(key, STRING);
                    if (asString == null)
                        return null;
                    if (asString.length() == 0)
                        return null;
                    else if (asString.length() == 1)
                        return asString.charAt(0);
                    else
                        throw new KfgTypeException(this, key, BYTE, null, asString);

                case BYTE:
                case SHORT:
                case INT:
                case LONG:
                    return checkJsonType(
                            at.isShort() || at.isInt() || at.isLong(),
                            type, at, key).longValue();

                case FLOAT:
                case DOUBLE:
                    return checkJsonType(
                            at.isFloat()
                                    || at.isDouble()
                                    || at.isShort()
                                    || at.isInt()
                                    || at.isLong(),
                            type, at, key).doubleValue();

                case STRING:
                    if (at.isArray()) {
                        final StringBuilder sb = new StringBuilder();
                        for (final JsonNode jsonNode : at)
                            sb.append(checkJsonType(jsonNode.isTextual(), STRING, at, key).textValue());
                        return sb.toString();
                    }
                    return checkJsonType(at.isTextual(), STRING, at, key).textValue();

                case CUSTOM:
                default:
                    return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected final <U> U getCustom(final String key, final Q<U> type) {
            final ObjectMapper reader = this.mapperSupplier.get();
            final JsonParser traverse = this.node(key).traverse();

            try {
                return reader.readValue(traverse, type.klass());
            }
            catch (final IOException e) {
                throw new KfgTypeException(this, key, type.typeName(), type, "?", e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected final Object getContainer(final String key, final Q<?> type) {
            TypeName typeName = type.typeName();
            if (typeName == LIST) {
                final JsonNode at = node(key);
                if (at.isNull())
                    return null;

                checkJsonType(at.isArray(), LIST, at, key);
                final ObjectMapper reader = this.mapperSupplier.get();
                final CollectionType javaType = reader.getTypeFactory().constructCollectionType(List.class, type.klass());

                try {
                    return reader.readValue(at.traverse(), javaType);
                }
                catch (final IOException e) {
                    throw new KfgTypeException(this, key, LIST, type, "?",
                            "can not read the key [" + key + "]" + " as a list of [" + type + "]", e);
                }
            }
            else if (typeName == MAP) {
                type = sanitizeType(type);
                final Map<?, ?> m = new HashMap<>();
                final Object at = node(key);
                checkYamlType(at instanceof Map, MAP, at, key);
                for (final Map.Entry<?, ?> e : ((Map<?, ?>) at).entrySet()) {
                    final Object k = e.getKey();
                    nn(k, "null map key at key=" + key);

                    // You'll never know.
                    if (m.containsKey(k))
                        throw new KfgException("duplicate key in map, at key=" + key + ", offending key=" + k);

                    final Object v = e.getValue();
                    if (v == null) {
                        m.put(k, null);
                    }
                    else if (type.klass().isAssignableFrom(v.getClass())) {
                        ((Map) m).put(k, v);
                    }
                    else {
                        throw new KfgTypeException(
                                "bad type in map, expected=" + type.getName() + ", got=" +
                                        v.getClass()
                                         .getName() + ", at=" + key);
                    }
                }
                return Collections.unmodifiableMap(m);
            }
            else if (typeName == SET) {
                final JsonNode at = node(key);
                if (at.isNull())
                    return null;

                checkJsonType(at.isArray(), SET, at, key);
                final ObjectMapper reader = this.mapperSupplier.get();
                final CollectionType javaType = reader.getTypeFactory().constructCollectionType(Set.class, type.klass());

                final Set<?> s;
                try {
                    s = reader.readValue(at.traverse(), javaType);
                }
                catch (final IOException e) {
                    throw new KfgTypeException(this, key, LIST, type, "?",
                            "can not read the key [" + key + "]" + " as a set of [" + type + "]", e);

                }
                return s;
            }
            throw new IllegalStateException("unexpected collection type: " + type);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public <U> Map<String, Q> map(final String key, Class<U> type) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <U> Set<U> set(final String key, Class<U> type) {
            type = sanitizeType(type);
            final List<U> list = this.list(key, type);
            final Set<U> set = new HashSet<>(list);
            if (list.size() != set.size())
                throw new KfgException("duplicate value in set at key=" + key);
            return set;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <U> U custom(final String key, Class<U> type) {
            // Whoops... You didn't see this.
            final Yaml yaml = this.mapperSupplier.get();
            new Yaml(new Constructor());
            return yaml.loadAs(yaml.dump(this.map(key, Object.class)), type);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        protected final Object getPrimitive(String key, TypeName type) {
            if (!this.contains(key))
                return null;

            final Object at = node(key);
            switch (type) {
                case BOOL:
                    return checkYamlType(at instanceof Boolean, BOOL, at, key);

                case CHAR:
                    return at instanceof String && ((String) at).length() == 1
                           ? Character.valueOf(this.<String>checkYamlType(true, BOOL, at, key).charAt(0))
                           : checkYamlType(at instanceof Character, CHAR, at, key);

                case BYTE:
                case SHORT:
                case INT:
                case LONG:
                    return checkYamlType(
                            at instanceof Byte ||
                                    at instanceof Short ||
                                    at instanceof Integer ||
                                    at instanceof Long, INT, at, key);

                case FLOAT:
                case DOUBLE:
                    return checkYamlType(
                            at instanceof Float ||
                                    at instanceof Double ||
                                    at instanceof Byte ||
                                    at instanceof Short ||
                                    at instanceof Integer ||
                                    at instanceof Long, INT, at, key);

            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected final <U> U getCustom(String key, Q<U> type) {
            final ObjectMapper reader = this.mapperSupplier.get();
            final JsonParser traverse = this.node(key).traverse();

            try {
                return reader.readValue(traverse, el);
            }
            catch (final IOException e) {
                throw new KfgTypeException("can not read the key [" + key + "]" + " as a custom type [" + el.getName() + "]",
                        e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected final Object getContainer(String key, Q<?> type) {
            switch (type.typeName()) {
                case LIST:
                    final JsonNode at = node(key);
                    if (at.isNull())
                        return null;

                    checkJsonType(at.isArray(), LIST, at, key);
                    final ObjectMapper reader = this.mapperSupplier.get();
                    final CollectionType javaType = reader.getTypeFactory().constructCollectionType(List.class, el);

                    try {
                        return reader.readValue(at.traverse(), javaType);
                    }
                    catch (final IOException e) {
                        throw newTypeError_Collection(
                                LIST, el, key,
                                "can not read the key [" + key + "]" + " as a list of [" + el.getName() + "]"
                        );
                    }

                case MAP:
                    final JsonNode at = node(key);
                    if (at.isNull())
                        return null;

                    checkJsonType(at.isObject(), MAP, at, key);
                    final ObjectMapper reader = this.mapperSupplier.get();
                    final MapType javaType = reader.getTypeFactory().constructMapType(Map.class, String.class, el);

                    try {
                        return reader.readValue(at.traverse(), javaType);
                    }
                    catch (final IOException e) {
                        throw new KfgTypeException("can not read the key [" + key + "]" + " as a map of [" + el.getName() + "]",
                                e);
                    }

                case SET:
                    final JsonNode at = node(key);
                    if (at.isNull())
                        return null;

                    checkJsonType(at.isArray(), SET, at, key);
                    final ObjectMapper reader = this.mapperSupplier.get();
                    final CollectionType javaType = reader.getTypeFactory().constructCollectionType(List.class, el);

                    final List<U> l;
                    try {
                        l = reader.readValue(at.traverse(), javaType);
                    }
                    catch (final IOException e) {
                        throw new KfgTypeException("can not read the key [" + key + "]" + " as a set of [" + el.getName() + "]",
                                e);
                    }

                    return new HashSet<>(l);
            }
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public final boolean contains(final String key) {
            try {
                this.node(key);
                return true;
            }
            catch (KfgMissingKeyException k) {
                return false;
            }
        }


        /**
         * {@inheritDoc}
         */
        @Override
        boolean hasUpdate0() {
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
        Konfiguration update0() {
            return new SnakeYamlKonfigSource(this.yaml, this.readonly, this.readonly, this.mapperSupplier);
        }

    }

}
