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
import java.io.IOException;
import java.io.OutputStream;
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

import static io.koosha.konfiguration.TypeName.*;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

final class KonfigSources {

    private KonfigSources() {
    }


    /**
     * Reads konfig from a plain java map.
     *
     * <p>To fulfill contract of {@link KonfigSource}, all the values put in the
     * map of konfiguration key/values supplied to the konfiguration, should be
     * immutable.
     *
     * <p>Thread safe and immutable.
     */
    static class InMemoryKonfigSource implements KonfigSource {

        private final Map<String, Object> storage;
        private final Supplier<Map<String, Object>> storageProvider;

        private KonfigurationTypeException checkType(final String required, final String key) {
            return new KonfigurationTypeException(required, this.storage.get(key).getClass().toString(), key);
        }

        private KonfigurationTypeException checkType(final TypeName required, final String key) {
            return this.checkType(required.getTName(), key);
        }

        /**
         * Important: {@link Supplier#get()} might be called multiple
         * times in a short period (once call to see if it's changed and if so, one
         * mode call to get the new values afterward.
         *
         * @param storage
         *         konfig source.
         *
         * @throws NullPointerException
         *         if provided storage provider is null
         * @throws KonfigurationSourceException
         *         if the provided storage by provider is null
         */
        InMemoryKonfigSource(final Supplier<Map<String, Object>> storage) {
            if (storage == null)
                throw new NullPointerException("storage");
            this.storageProvider = storage;

            final Map<String, Object> newStorage = this.storageProvider.get();
            if (newStorage == null)
                throw new KonfigurationSourceException("storage is null");

            this.storage = new HashMap<>(newStorage);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean bool(final String key) {
            try {
                return (Boolean) this.storage.get(key);
            }
            catch (final ClassCastException cce) {
                throw checkType(BOOLEAN, key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer int_(final String key) {
            try {
                return (Integer) this.storage.get(key);
            }
            catch (final ClassCastException cce) {
                throw checkType(INT, key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Long long_(final String key) {
            try {
                return (Long) this.storage.get(key);
            }
            catch (final ClassCastException cce) {
                throw checkType(LONG, key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Double double_(final String key) {
            try {
                return (Double) this.storage.get(key);
            }
            catch (final ClassCastException cce) {
                throw checkType(DOUBLE, key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String string(final String key) {
            try {
                return (String) this.storage.get(key);
            }
            catch (final ClassCastException cce) {
                throw checkType(STRING, key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public <T> List<T> list(final String key, final Class<T> type) {
            // TODO check type

            try {
                return (List<T>) this.storage.get(key);
            }
            catch (final ClassCastException cce) {
                throw checkType(LIST, key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public <T> Map<String, T> map(final String key, final Class<T> type) {
            // TODO check type

            try {
                return (Map<String, T>) this.storage.get(key);
            }
            catch (final ClassCastException cce) {
                throw checkType(MAP, key);
            }
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public <T> Set<T> set(final String key, final Class<T> type) {
            // TODO check type

            try {
                return (Set<T>) this.storage.get(key);
            }
            catch (final ClassCastException cce) {
                try {
                    final List<T> l = (List<T>) this.storage.get(key);
                    final HashSet<T> s = new HashSet<>(l);
                    return Collections.unmodifiableSet(s);
                }
                catch (final ClassCastException cceList) {
                    throw checkType(SET, key);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public <T> T custom(final String key, final Class<T> type) {
            // TODO check type
            try {
                return (T) this.storage.get(key);
            }
            catch (final ClassCastException cce) {
                throw checkType(type.toString(), key);
            }
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(final String key) {
            return this.storage.containsKey(key);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isUpdatable() {
            final Map<String, Object> newStorage = this.storageProvider.get();
            return newStorage != null && !this.storage.equals(newStorage);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KonfigSource copyAndUpdate() {
            return new InMemoryKonfigSource(this.storageProvider);
        }

    }

    /**
     * This is like it's parent except that it'll never be updated.
     */
    static final class KonstInMemoryKonfigSource extends InMemoryKonfigSource {

        /**
         * Wraps the provided storage in a {@link Supplier} and calls
         * {@link super#InMemoryKonfigSource(Supplier)}
         *
         * @param storage
         *         konfig source.
         *
         * @throws NullPointerException
         *         if storage is null.
         */
        KonstInMemoryKonfigSource(final Map<String, Object> storage) {
            super(new Supplier<Map<String, Object>>() {
                private final Map<String, Object> s = new HashMap<>(requireNonNull(storage));

                @Override
                public Map<String, Object> get() {
                    return s;
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isUpdatable() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KonfigSource copyAndUpdate() {
            throw new UnsupportedOperationException("source is readonly");
        }

    }


    /**
     * Reads konfig from a json source (supplied as string).
     *
     * <p>for {@link #custom(String, Class)} to work, the supplied json reader must
     * be configured to handle arbitrary types accordingly.
     *
     * <p>Thread safe and immutable.
     */
    static class JsonKonfigSource implements KonfigSource {

        private final Supplier<ObjectMapper> mapperSupplier;
        private final Supplier<String> json;
        private int lastHash;
        private JsonNode root;


        private JsonNode node_(final String key) {
            if (key == null || key.isEmpty())
                throw new KonfigurationMissingKeyException("empty konfig key");

            final String k = "/" + key.replace('.', '/');
            return this.root.at(k);
        }

        private JsonNode node(final String key) {
            final JsonNode node = node_(key);

            if (node.isMissingNode())
                throw new KonfigurationMissingKeyException(key);

            return node;
        }


        private static ObjectMapper defaultObjectMapper() {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            return mapper;
        }

        private static void checkType(final boolean isOk, final TypeName required, final JsonNode node, final String key) {
            if (isOk)
                return;

            throw new KonfigurationTypeException(required.getTName(), node.getNodeType().toString(), key);
        }


        /**
         * Creates an instance with a default object mapper provided by
         * {@link #defaultObjectMapper()}.
         *
         * @param json
         *         constant json string as backing storage.
         */
        JsonKonfigSource(final Supplier<String> json) {
            this(json, new Supplier<ObjectMapper>() {
                private final ObjectMapper mapper = defaultObjectMapper();

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
         * @param json
         *         backing store provider. Must always return a non-null valid json
         *         string.
         * @param objectMapper
         *         {@link ObjectMapper} provider. Must always return a valid
         *         non-null ObjectMapper, and if required, it must be able to
         *         deserialize custom types, so that {@link #custom(String, Class)}
         *         works as well.
         *
         * @throws NullPointerException
         *         if any of its arguments are null.
         * @throws KonfigurationSourceException
         *         if jackson library is not in the classpath. it specifically looks
         *         for the class: "com.fasterxml.jackson.databind.JsonNode"
         * @throws KonfigurationSourceException
         *         if the storage (json string) returned by json string is null.
         * @throws KonfigurationSourceException
         *         if the provided json string can not be parsed by jackson.
         * @throws KonfigurationSourceException
         *         if the the root element returned by jackson is null.
         */
        JsonKonfigSource(final Supplier<String> json, final Supplier<ObjectMapper> objectMapper) {
            requireNonNull(json, "jsonSupplier");
            requireNonNull(objectMapper, "objectMapperSupplier");
            // Check early, so we're not fooled with a dummy object reader.
            try {
                Class.forName("com.fasterxml.jackson.databind.JsonNode");
            }
            catch (final ClassNotFoundException e) {
                throw new KonfigurationSourceException(getClass().getName() + " requires " + "jackson library to be present in the class path",
                                                       e);
            }

            this.json = json;
            this.mapperSupplier = objectMapper;

            final String newJson = this.json.get();
            if (newJson == null)
                throw new KonfigurationSourceException("storage is null");

            final JsonNode update;
            try {
                update = this.mapperSupplier.get().readTree(newJson);
            }
            catch (final IOException e) {
                throw new KonfigurationSourceException("error parsing json string", e);
            }

            if (update == null)
                throw new KonfigurationSourceException("root element is null");

            this.root = update;
            this.lastHash = newJson.hashCode();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean bool(final String key) {
            final JsonNode at = node(key);
            checkType(at.isBoolean(), BOOLEAN, at, key);
            return at.asBoolean();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer int_(final String key) {
            final JsonNode at = node(key);
            checkType(at.isInt(), INT, at, key);
            return at.asInt();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Long long_(final String key) {
            final JsonNode at = node(key);

            if (at.isInt())
                return (long) at.asInt();
            else if (at.isLong())
                return at.asLong();

            //noinspection ConstantConditions
            checkType(false, LONG, at, key);
            throw new AssertionError("?!!");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Double double_(final String key) {
            final JsonNode at = node(key);
            checkType(at.isDouble(), DOUBLE, at, key);
            return at.asDouble();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String string(final String key) {
            final JsonNode at = node(key);

            if (at.isArray()) {
                final StringBuilder sb = new StringBuilder();
                for (final JsonNode jsonNode : at) {
                    checkType(jsonNode.isTextual(), STRING_ARRAY, at, key);
                    sb.append(jsonNode.textValue());
                }
                return sb.toString();
            } else if (at.isTextual()) {
                return at.asText();
            }

            //noinspection ConstantConditions
            checkType(false, STRING, at, key);
            throw new AssertionError("?!!");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> List<T> list(final String key, final Class<T> el) {
            final JsonNode at = node(key);
            checkType(at.isArray(), LIST, at, key);
            final ObjectMapper reader = this.mapperSupplier.get();
            final CollectionType javaType = reader.getTypeFactory().constructCollectionType(List.class, el);

            try {
                return reader.readValue(at.traverse(), javaType);
            }
            catch (final IOException e) {
                throw new KonfigurationTypeException("can not read the key [" + key + "]" + " as a list of [" + el.getCanonicalName() + "]",
                                                     e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Map<String, T> map(final String key, final Class<T> el) {
            final JsonNode at = node(key);
            checkType(at.isObject(), MAP, at, key);
            final ObjectMapper reader = this.mapperSupplier.get();
            final MapType javaType = reader.getTypeFactory().constructMapType(Map.class, String.class, el);

            try {
                return reader.readValue(at.traverse(), javaType);
            }
            catch (final IOException e) {
                throw new KonfigurationTypeException("can not read the key [" + key + "]" + " as a map of [" + el.getCanonicalName() + "]",
                                                     e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Set<T> set(final String key, final Class<T> el) {
            final JsonNode at = node(key);
            checkType(at.isArray(), SET, at, key);
            final ObjectMapper reader = this.mapperSupplier.get();
            final CollectionType javaType = reader.getTypeFactory().constructCollectionType(List.class, el);

            final List<T> l;
            try {
                l = reader.readValue(at.traverse(), javaType);
            }
            catch (final IOException e) {
                throw new KonfigurationTypeException("can not read the key [" + key + "]" + " as a set of [" + el.getCanonicalName() + "]",
                                                     e);
            }

            return new HashSet<>(l);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T custom(final String key, final Class<T> el) {
            final ObjectMapper reader = this.mapperSupplier.get();
            final JsonParser traverse = this.node(key).traverse();

            try {
                return reader.readValue(traverse, el);
            }
            catch (final IOException e) {
                throw new KonfigurationTypeException("can not read the key [" + key + "]" + " as a custom type [" + el.getCanonicalName() + "]",
                                                     e);
            }
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(final String key) {
            return !this.node_(key).isMissingNode();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isUpdatable() {
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
        public KonfigSource copyAndUpdate() {
            return new JsonKonfigSource(this.json, this.mapperSupplier);
        }

    }

    /**
     * This is like it's parent except that it'll never be updated.
     */
    static final class KonstJsonKonfigSource extends JsonKonfigSource {

        KonstJsonKonfigSource(String json) {
            super(() -> json);
            requireNonNull(json, "json source konfig");
        }

        KonstJsonKonfigSource(String json, Supplier<ObjectMapper> objectMapper) {
            super(() -> json, objectMapper);
            requireNonNull(json, "json source konfig");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isUpdatable() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KonfigSource copyAndUpdate() {
            throw new UnsupportedOperationException("source is readonly");
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
    static class SnakeYamlKonfigSource implements KonfigSource {

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
            return new Yaml(new ByConstructorConstructor<>(
                    (Class<? extends ConstructorProperties>) ConstructorProperties.class,
                    (Function<? super ConstructorProperties, String[]>) ConstructorProperties::value
            ));
        }

        private static final ThreadLocal<Yaml> defaultSupplier =
                ThreadLocal.withInitial(SnakeYamlKonfigSource::defaultObjectMapper);

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


        /**
         * Creates an instance with a default Yaml parser.
         *
         * @param yaml
         *         constant yaml string as backing storage.
         */
        SnakeYamlKonfigSource(final Supplier<String> yaml) {
            this(yaml, defaultSupplier::get);
        }

        /**
         * Creates an instance with the given Yaml parser.
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
        SnakeYamlKonfigSource(final Supplier<String> yaml,
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
            return new SnakeYamlKonfigSource(this.yaml, this.mapperSupplier);
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
                                    .map(SnakeYamlKonfigSource::lower)
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

    static final class KonstSnakeYamlKonfigSource extends SnakeYamlKonfigSource {

        KonstSnakeYamlKonfigSource(String yaml) {
            super(() -> yaml);
            requireNonNull(yaml, "yaml source konfig");
        }

        KonstSnakeYamlKonfigSource(String yaml, Supplier<Yaml> objectMapper) {
            super(() -> yaml, objectMapper);
            requireNonNull(yaml, "yaml source konfig");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isUpdatable() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KonfigSource copyAndUpdate() {
            throw new UnsupportedOperationException("source is readonly");
        }

    }


    /**
     * Reads konfig from a {@link Preferences} source.
     *
     * <p>for {@link #custom(String, Class)} to work, the supplied deserializer
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
    static final class PreferencesKonfigSource implements KonfigSource {

        private final Deserializer<String> deser;
        private final Preferences pref;
        private final int lastHash;

        PreferencesKonfigSource(final Preferences preferences, final Deserializer<String> deserializer) {
            this.deser = requireNonNull(deserializer, "deserializer");
            this.pref = requireNonNull(preferences, "preferences");
            this.lastHash = hashOf(pref);
        }

        PreferencesKonfigSource(final Preferences preferences) {
            this(preferences, new PreferencesKonfigSourceJacksonDeserializer());
        }


        private String sane(String key) {
            if (key == null || key.isEmpty())
                throw new KonfigurationMissingKeyException("empty konfig key");
            return key.replace('.', '/');
        }

        private boolean exists(String key) {
            try {
                return pref.nodeExists(key);
            }
            catch (BackingStoreException e) {
                throw new KonfigurationSourceException("error checking existence of key", e);
            }
        }

        private String check(String key) {
            if (!exists(key))
                throw new KonfigurationMissingKeyException(key);
            return key;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean bool(String key) {
            return pref.getBoolean(check(sane(key)), false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer int_(String key) {
            return pref.getInt(check(sane(key)), 0);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Long long_(String key) {
            return pref.getLong(check(sane(key)), 0);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Double double_(String key) {
            return pref.getDouble(check(sane(key)), 0);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String string(String key) {
            return pref.get(check(sane(key)), null);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public <T> List<T> list(String key, Class<T> type) {
            try {
                return deser.list(string(key), type);
            }
            catch (final IOException e) {
                throw new KonfigurationTypeException("can not read the key [" + key + "]" + " as a list of [" + type.getCanonicalName() + "]",
                        e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Map<String, T> map(String key, Class<T> type) {
            try {
                return deser.map(string(key), type);
            }
            catch (final IOException e) {
                throw new KonfigurationTypeException("can not read the key [" + key + "]" + " as a map of [" + type.getCanonicalName() + "]",
                        e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Set<T> set(String key, Class<T> type) {
            try {
                return deser.set(string(key), type);
            }
            catch (final IOException e) {
                throw new KonfigurationTypeException("can not read the key [" + key + "]" + " as a set of [" + type.getCanonicalName() + "]",
                        e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T custom(String key, Class<T> type) {
            try {
                return deser.custom(string(key), type);
            }
            catch (final IOException e) {
                throw new KonfigurationTypeException("can not read the key [" + key + "]" + " as a custom type [" + type.getCanonicalName() + "]",
                        e);
            }
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
        public boolean isUpdatable() {
            return this.lastHash != hashOf(pref);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KonfigSource copyAndUpdate() {
            return this;
        }


        // ================================================================

        private static int hashOf(Preferences pref) {
            final HashingOutputStream out = new HashingOutputStream();
            try {
                pref.exportSubtree(out);
            }
            catch (IOException | BackingStoreException e) {
                throw new KonfigurationSourceException("could not calculate hash of " + "the source required for" + " tracking changes");
            }
            return out.crc;
        }

        // from Guava, modified:
        private static final class HashingOutputStream extends OutputStream {

            private int crc = 0;

            //            void update(byte b) {
            //                crc ^= 0xFFFFFFFF;
            //                // See Hacker's Delight 2nd Edition, Figure 14-7.
            //                crc = ~((crc >>> 8) ^ CRC_TABLE[(crc ^ b) & 0xFF]);
            //            }

            @Override
            public void write(int b) {
                crc ^= 0xFFFFFFFF;
                // See Hacker's Delight 2nd Edition, Figure 14-7.
                crc = ~((crc >>> 8) ^ CRC_TABLE[(crc ^ ((byte) b)) & 0xFF]);
            }

            @Override
            public void write(byte[] bytes, int off, int len) {
                for (int i = off; i < off + len; i++)
                    write(bytes[i]);
            }

            @Override
            public void write(byte[] b) {
                this.write(b, 0, b.length);
            }


            @Override
            public void close() {
            }

        }


        // from Guava, modified:
        // The CRC table, generated from the polynomial 0x11EDC6F41.
        private static final int[] CRC_TABLE = {0x00000000,
                0xf26b8303,
                0xe13b70f7,
                0x1350f3f4,
                0xc79a971f,
                0x35f1141c,
                0x26a1e7e8,
                0xd4ca64eb,
                0x8ad958cf,
                0x78b2dbcc,
                0x6be22838,
                0x9989ab3b,
                0x4d43cfd0,
                0xbf284cd3,
                0xac78bf27,
                0x5e133c24,
                0x105ec76f,
                0xe235446c,
                0xf165b798,
                0x030e349b,
                0xd7c45070,
                0x25afd373,
                0x36ff2087,
                0xc494a384,
                0x9a879fa0,
                0x68ec1ca3,
                0x7bbcef57,
                0x89d76c54,
                0x5d1d08bf,
                0xaf768bbc,
                0xbc267848,
                0x4e4dfb4b,
                0x20bd8ede,
                0xd2d60ddd,
                0xc186fe29,
                0x33ed7d2a,
                0xe72719c1,
                0x154c9ac2,
                0x061c6936,
                0xf477ea35,
                0xaa64d611,
                0x580f5512,
                0x4b5fa6e6,
                0xb93425e5,
                0x6dfe410e,
                0x9f95c20d,
                0x8cc531f9,
                0x7eaeb2fa,
                0x30e349b1,
                0xc288cab2,
                0xd1d83946,
                0x23b3ba45,
                0xf779deae,
                0x05125dad,
                0x1642ae59,
                0xe4292d5a,
                0xba3a117e,
                0x4851927d,
                0x5b016189,
                0xa96ae28a,
                0x7da08661,
                0x8fcb0562,
                0x9c9bf696,
                0x6ef07595,
                0x417b1dbc,
                0xb3109ebf,
                0xa0406d4b,
                0x522bee48,
                0x86e18aa3,
                0x748a09a0,
                0x67dafa54,
                0x95b17957,
                0xcba24573,
                0x39c9c670,
                0x2a993584,
                0xd8f2b687,
                0x0c38d26c,
                0xfe53516f,
                0xed03a29b,
                0x1f682198,
                0x5125dad3,
                0xa34e59d0,
                0xb01eaa24,
                0x42752927,
                0x96bf4dcc,
                0x64d4cecf,
                0x77843d3b,
                0x85efbe38,
                0xdbfc821c,
                0x2997011f,
                0x3ac7f2eb,
                0xc8ac71e8,
                0x1c661503,
                0xee0d9600,
                0xfd5d65f4,
                0x0f36e6f7,
                0x61c69362,
                0x93ad1061,
                0x80fde395,
                0x72966096,
                0xa65c047d,
                0x5437877e,
                0x4767748a,
                0xb50cf789,
                0xeb1fcbad,
                0x197448ae,
                0x0a24bb5a,
                0xf84f3859,
                0x2c855cb2,
                0xdeeedfb1,
                0xcdbe2c45,
                0x3fd5af46,
                0x7198540d,
                0x83f3d70e,
                0x90a324fa,
                0x62c8a7f9,
                0xb602c312,
                0x44694011,
                0x5739b3e5,
                0xa55230e6,
                0xfb410cc2,
                0x092a8fc1,
                0x1a7a7c35,
                0xe811ff36,
                0x3cdb9bdd,
                0xceb018de,
                0xdde0eb2a,
                0x2f8b6829,
                0x82f63b78,
                0x709db87b,
                0x63cd4b8f,
                0x91a6c88c,
                0x456cac67,
                0xb7072f64,
                0xa457dc90,
                0x563c5f93,
                0x082f63b7,
                0xfa44e0b4,
                0xe9141340,
                0x1b7f9043,
                0xcfb5f4a8,
                0x3dde77ab,
                0x2e8e845f,
                0xdce5075c,
                0x92a8fc17,
                0x60c37f14,
                0x73938ce0,
                0x81f80fe3,
                0x55326b08,
                0xa759e80b,
                0xb4091bff,
                0x466298fc,
                0x1871a4d8,
                0xea1a27db,
                0xf94ad42f,
                0x0b21572c,
                0xdfeb33c7,
                0x2d80b0c4,
                0x3ed04330,
                0xccbbc033,
                0xa24bb5a6,
                0x502036a5,
                0x4370c551,
                0xb11b4652,
                0x65d122b9,
                0x97baa1ba,
                0x84ea524e,
                0x7681d14d,
                0x2892ed69,
                0xdaf96e6a,
                0xc9a99d9e,
                0x3bc21e9d,
                0xef087a76,
                0x1d63f975,
                0x0e330a81,
                0xfc588982,
                0xb21572c9,
                0x407ef1ca,
                0x532e023e,
                0xa145813d,
                0x758fe5d6,
                0x87e466d5,
                0x94b49521,
                0x66df1622,
                0x38cc2a06,
                0xcaa7a905,
                0xd9f75af1,
                0x2b9cd9f2,
                0xff56bd19,
                0x0d3d3e1a,
                0x1e6dcdee,
                0xec064eed,
                0xc38d26c4,
                0x31e6a5c7,
                0x22b65633,
                0xd0ddd530,
                0x0417b1db,
                0xf67c32d8,
                0xe52cc12c,
                0x1747422f,
                0x49547e0b,
                0xbb3ffd08,
                0xa86f0efc,
                0x5a048dff,
                0x8ecee914,
                0x7ca56a17,
                0x6ff599e3,
                0x9d9e1ae0,
                0xd3d3e1ab,
                0x21b862a8,
                0x32e8915c,
                0xc083125f,
                0x144976b4,
                0xe622f5b7,
                0xf5720643,
                0x07198540,
                0x590ab964,
                0xab613a67,
                0xb831c993,
                0x4a5a4a90,
                0x9e902e7b,
                0x6cfbad78,
                0x7fab5e8c,
                0x8dc0dd8f,
                0xe330a81a,
                0x115b2b19,
                0x020bd8ed,
                0xf0605bee,
                0x24aa3f05,
                0xd6c1bc06,
                0xc5914ff2,
                0x37faccf1,
                0x69e9f0d5,
                0x9b8273d6,
                0x88d28022,
                0x7ab90321,
                0xae7367ca,
                0x5c18e4c9,
                0x4f48173d,
                0xbd23943e,
                0xf36e6f75,
                0x0105ec76,
                0x12551f82,
                0xe03e9c81,
                0x34f4f86a,
                0xc69f7b69,
                0xd5cf889d,
                0x27a40b9e,
                0x79b737ba,
                0x8bdcb4b9,
                0x988c474d,
                0x6ae7c44e,
                0xbe2da0a5,
                0x4c4623a6,
                0x5f16d052,
                0xad7d5351
        };


        //    private volatile boolean isUpdatable;
        //    @SuppressWarnings("FieldCanBeLocal")
        //    private final PreferenceChangeListener listen = new PreferenceChangeListener() {
        //        @Override
        //        public void preferenceChange(PreferenceChangeEvent evt) {
        //            isUpdatable = true;
        //        }
        //    };
        //        preferences.addPreferenceChangeListener(listen);

    }

    private static final class PreferencesKonfigSourceJacksonDeserializer implements Deserializer<String> {

        private final Supplier<ObjectMapper> mapperSupplier;

        private static ObjectMapper defaultObjectMapper() {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            return mapper;
        }

        @SuppressWarnings({"WeakerAccess",
                "unused"
        })
        public PreferencesKonfigSourceJacksonDeserializer() {
            this(new Supplier<ObjectMapper>() {
                private final ObjectMapper mapper = defaultObjectMapper();

                @Override
                public ObjectMapper get() {
                    return mapper;
                }
            });
        }

        @SuppressWarnings("WeakerAccess")
        public PreferencesKonfigSourceJacksonDeserializer(final Supplier<ObjectMapper> objectMapper) {
            requireNonNull(objectMapper, "objectMapperSupplier");
            // Check early, so we're not fooled with a dummy object reader.
            try {
                Class.forName("com.fasterxml.jackson.databind.JsonNode");
            }
            catch (final ClassNotFoundException e) {
                throw new KonfigurationSourceException(getClass().getName() + " requires " + "jackson library to be present in the class path",
                        e);
            }

            this.mapperSupplier = objectMapper;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T custom(String from, Class<T> type) throws IOException {
            final ObjectMapper reader = this.mapperSupplier.get();
            final JsonParser root = reader.readTree(from).traverse();
            return reader.readValue(root, type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> List<T> list(String from, Class<T> type) throws IOException {
            final ObjectMapper reader = this.mapperSupplier.get();
            final JsonNode root = reader.readTree(from);
            checkType(root.isArray(), LIST, root);
            final CollectionType javaType = reader.getTypeFactory().constructCollectionType(List.class, type);
            return reader.readValue(root.traverse(), javaType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Map<String, T> map(String from, Class<T> type) throws IOException {
            final ObjectMapper reader = this.mapperSupplier.get();
            final JsonNode root = reader.readTree(from);
            checkType(root.isObject(), MAP, root);
            final MapType javaType = reader.getTypeFactory().constructMapType(Map.class, String.class, type);
            return reader.readValue(root.traverse(), javaType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Set<T> set(String from, Class<T> type) throws IOException {
            return new HashSet<>(list(from, type));
        }


        private static void checkType(final boolean isOk, final TypeName required, final JsonNode node) {
            if (isOk)
                return;

            throw new KonfigurationTypeException(required.getTName(), node.getNodeType().toString(), "/");
        }

    }

}
