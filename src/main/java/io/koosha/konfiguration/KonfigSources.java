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

import static io.koosha.konfiguration.TypeName.*;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

final class KonfigSources {

    private KonfigSources() {
    }

    // =========================================================================

    private abstract static class BaseKonfigSource extends AbstractKonfiguration {

        protected BaseKonfigSource(String name) {
            super(name);
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
        public Konfiguration subset(String key) {
            throw new UnsupportedOperationException("do not use this directly, put this source in a kombiner");
        }

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

        MapKonfiguration(final String name, final Supplier<Map<String, ?>> storage) {
            super(name);
            requireNonNull(storage, "storage");
            this.storageProvider = storage;

            final Map<String, ?> newStorage = this.storageProvider.get();
            requireNonNull(newStorage, "newStorage");

            this.storage = Collections.unmodifiableMap(new HashMap<>(newStorage));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasUpdate() {
            return !this.storageProvider.get().equals(this.storage);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration update() {
            return new MapKonfiguration(this.storageProvider);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(String key) {
            return this.storage.containsKey(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName() {
            return this.name;
        }


        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        protected <U> U getPrimitive(String key, TypeName type) {
            return (U) this.storage.get(key);
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        protected <U> U getCustom(String key, Class<U> type) {
            return (U) this.storage.get(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Object getContainer(String key, Class<?> type, TypeName containerType) {
            return this.storage.get(key);
        }
    }

    /**
     * This is like it's parent except that it'll never be updated.
     */
    static final class KonstMapKonfiguration extends MapKonfiguration {

        /**
         * Wraps the provided storage in a {@link Supplier} and calls super().
         *
         * @param storage konfig source.
         * @throws NullPointerException if storage is null.
         */
        KonstMapKonfiguration(final String name, final Map<String, ?> storage) {
            super(name, new Supplier<Map<String, ?>>() {
                private final Map<String, ?> s = new HashMap<>(requireNonNull(storage));

                @Override
                public Map<String, ?> get() {
                    return s;
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasUpdate() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration update() {
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
    static class PreferencesKonfiguration extends BaseKonfigSource {

        private final Deserializer<Preferences, String> deser;
        private final Preferences source;
        private final int lastHash;

        PreferencesKonfiguration(final String name,
                                 final Preferences preferences,
                                 final Deserializer<Preferences, String> deserializer) {
            super(name);
            this.source = requireNonNull(preferences, "preferences");
            this.deser = deserializer;
            this.lastHash = hashOf(source);
        }


        private static int hashOf(Preferences pref) {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
                pref.exportSubtree(buffer);
            }
            catch (IOException | BackingStoreException e) {
                throw new KfgInSourceException(
                        "could not calculate hash of the source required for" +
                                " tracking changes");
            }
            return Arrays.hashCode(buffer.toByteArray());
        }

        private String sane(final String key) {
            requireNonNull(key, "empty konfig key");
            return key.replace('.', '/');
        }

        private boolean exists(final String key) {
            try {
                return source.nodeExists(sane(key));
            }
            catch (Throwable e) {
                throw new KfgInSourceException("error checking existence of key", e);
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

        @SuppressWarnings("unchecked")
        @Override
        protected <U> U getPrimitive(String key, TypeName type) {
            switch (type) {
                case BOOL:
                    break;

                case CHAR:
                    break;

                case BYTE:
                    break;

                case SHORT:
                    return (U) this.source.getInt(key, null);

                case INT:
                    return (U) this.source.getInt(key, null);

                case LONG:
                    return (U) this.source.getLong(key, null);

                case FLOAT:
                    return (U) this.source.getFloat(key, null);

                case DOUBLE:
                    return (U) this.source.getDouble(key, null);

                case STRING:
                    return (U) this.source.get(key, null);

                case LIST:
                case MAP:
                case SET:
                case CUSTOM:
                default:
                    throw new IllegalArgumentException("unexpected type: " + type);
            }
        }

        @Override
        protected <U> U getCustom(String key, Class<U> type) {
            if (this.deser == null)
                throw new UnsupportedOperationException("deserializer not set");
        }

        @Override
        protected Object getContainer(String key, Class<?> type, TypeName containerType) {
            if (this.deser == null)
                throw new UnsupportedOperationException("deserializer not set");
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isReadonly() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasUpdate() {
            return this.lastHash != hashOf(source);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration copyAndUpdate() {
            return this;
        }

    }

    static final class KonstPreferencesKonfiguration extends PreferencesKonfiguration {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isReadonly() {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasUpdate() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration copyAndUpdate() {
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
    static class JsonKonfigSource implements Konfiguration {

        private final Supplier<ObjectMapper> mapperSupplier;
        private final Supplier<String> json;
        private int lastHash;
        private JsonNode root;


        private JsonNode node_(final String key) {
            if (requireNonNull(key, "key").isEmpty())
                throw new KfgMissingKeyException("empty konfig key");

            final String k = "/" + key.replace('.', '/');
            return this.root.at(k);
        }

        private JsonNode node(final String key) {
            if (requireNonNull(key, "key").isEmpty())
                throw new KfgMissingKeyException("empty konfig key");

            final JsonNode node = node_(key);
            if (node.isMissingNode())
                throw new KfgMissingKeyException(key);
            return node;
        }


        private static ObjectMapper defaultObjectMapper() {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            return mapper;
        }

        private static JsonNode checkType(final boolean isOk,
                                          final TypeName required,
                                          final JsonNode node,
                                          final String key) {
            if (isOk)
                return node;
            throw KfgTypeException.newTypeError(
                    required.name(), key, node.getNodeType().toString());
        }


        /**
         * Creates an instance with a default object mapper provided by
         * {@link #defaultObjectMapper()}.
         *
         * @param json constant json string as backing storage.
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
         * @param json         backing store provider. Must always return a non-null valid json
         *                     string.
         * @param objectMapper {@link ObjectMapper} provider. Must always return a valid
         *                     non-null ObjectMapper, and if required, it must be able to
         *                     deserialize custom types, so that {@link #custom(String, Class)}
         *                     works as well.
         * @throws NullPointerException if any of its arguments are null.
         * @throws KfgInSourceException if jackson library is not in the classpath. it specifically looks
         *                              for the class: "com.fasterxml.jackson.databind.JsonNode"
         * @throws KfgInSourceException if the storage (json string) returned by json string is null.
         * @throws KfgInSourceException if the provided json string can not be parsed by jackson.
         * @throws KfgInSourceException if the the root element returned by jackson is null.
         */
        JsonKonfigSource(final Supplier<String> json, final Supplier<ObjectMapper> objectMapper) {
            requireNonNull(json, "jsonSupplier");
            requireNonNull(objectMapper, "objectMapperSupplier");
            // Check early, so we're not fooled with a dummy object reader.
            try {
                Class.forName("com.fasterxml.jackson.databind.JsonNode");
            }
            catch (final ClassNotFoundException e) {
                throw new KfgInSourceException(getClass().getName() + " requires " + "jackson library to be present in the class path",
                        e);
            }

            this.json = json;
            this.mapperSupplier = objectMapper;

            final String newJson = this.json.get();
            requireNonNull(newJson, "storage is null");

            final JsonNode update;
            try {
                update = this.mapperSupplier.get().readTree(newJson);
            }
            catch (final IOException e) {
                throw new KfgInSourceException("error parsing json string", e);
            }

            requireNonNull(update, "root element is null");

            this.root = update;
            this.lastHash = newJson.hashCode();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean bool(final String key) {
            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            return checkType(at.isBoolean(), BOOL, at, key).asBoolean();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Byte byte_(String key) {
            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            final int i = checkType(at.isInt(), BYTE, at, key).asInt();
            checkType(Byte.MIN_VALUE <= i && i <= Byte.MAX_VALUE, SHORT, at, key);
            return (byte) i;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Character char_(String key) {
            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            final String i = checkType(at.isTextual(), CHAR, at, key).asText();
            checkType(i.length() == 1, CHAR, at, key);
            return i.charAt(0);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Short short_(String key) {
            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            final int i = checkType(at.isShort(), SHORT, at, key).asInt();
            checkType(Short.MIN_VALUE <= i && i <= Short.MAX_VALUE, SHORT, at, key);
            return (short) i;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer int_(final String key) {
            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            return checkType(at.isInt(), INT, at, key).asInt();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Long long_(final String key) {
            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            if (at.isInt())
                return (long) at.asInt();
            else if (at.isLong())
                return at.asLong();

            //noinspection ConstantConditions
            checkType(false, LONG, at, key);
            throw new AssertionError("?!!");
        }

        @Override
        public Float float_(String key) {
            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            final double i = checkType(at.isShort(), FLOAT, at, key).asDouble();
            // TODO In accurate as f..., is it ok?
            checkType(Float.MIN_VALUE <= i && i <= Float.MAX_VALUE, FLOAT, at, key);
            return (float) i;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Double double_(final String key) {
            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            checkType(at.isDouble(), DOUBLE, at, key).asDouble()
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String string(final String key) {
            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            if (at.isArray()) {
                final StringBuilder sb = new StringBuilder();
                for (final JsonNode jsonNode : at)
                    sb.append(checkType(jsonNode.isTextual(), STRING, at, key).textValue());
                return sb.toString();
            }
            else if (at.isTextual()) {
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
        public <U> List<U> list(final String key, final Class<U> el) {
            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            checkType(at.isArray(), LIST, at, key);
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
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <U> Map<String, T> map(final String key, final Class<U> el) {
            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            checkType(at.isObject(), MAP, at, key);
            final ObjectMapper reader = this.mapperSupplier.get();
            final MapType javaType = reader.getTypeFactory().constructMapType(Map.class, String.class, el);

            try {
                return reader.readValue(at.traverse(), javaType);
            }
            catch (final IOException e) {
                throw new KfgTypeException("can not read the key [" + key + "]" + " as a map of [" + el.getName() + "]",
                        e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <U> Set<U> set(final String key, final Class<U> el) {
            final JsonNode at = node(key);
            if (at.isNull())
                return null;

            checkType(at.isArray(), SET, at, key);
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

        /**
         * {@inheritDoc}
         */
        @Override
        public <U> U custom(final String key, final Class<U> el) {
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
        public boolean contains(final String key) {
            return !this.node_(key).isMissingNode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isReadonly() {
            return false;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasUpdate() {
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
        public Konfiguration copyAndUpdate() {
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
        public boolean isReadonly() {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasUpdate() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration copyAndUpdate() {
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
    static class SnakeYamlKonfigSource implements Konfiguration {

        private final static String DOT = Pattern.quote(".");

        @SuppressWarnings("unchecked")
        private <U> Class<U> sanitizeType(final Class<U> type) {
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

        private final Supplier<Yaml> mapperSupplier;
        private final Supplier<String> yaml;
        private final Map<?, ?> root;

        private int lastHash;

        private Object node(final String key) {
            requireNonNull(key, "key");
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

            requireNonNull(value, "empty konfig value: " + key);

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

            throw KfgTypeException.newTypeError(required.name(),
                    node.getClass().getName(),
                    key);
        }


        /**
         * Creates an instance with a default Yaml parser.
         *
         * @param yaml constant yaml string as backing storage.
         */
        SnakeYamlKonfigSource(final Supplier<String> yaml) {
            this(yaml, defaultSupplier::get);
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
         * @throws KfgInSourceException if org.yaml.snakeyaml library is not in the classpath. it
         *                              specifically looks for the class: "org.yaml.snakeyaml"
         * @throws KfgInSourceException if the storage (yaml string) returned by yaml string is null.
         * @throws KfgInSourceException if the provided yaml string can not be parsed by Yaml.
         * @throws KfgInSourceException if the the root element returned by yaml is null.
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
                throw new KfgInSourceException(
                        getClass().getName() + " requires org.yaml.snakeyaml library to be present in the class path",
                        e);
            }

            this.yaml = yaml;
            this.mapperSupplier = objectMapper;

            final String newYaml = this.yaml.get();
            requireNonNull(yaml, "storage is null");

            final Map<?, ?> update;
            try {
                update = this.mapperSupplier.get().load(newYaml);
            }
            catch (final Throwable e) {
                throw new KfgInSourceException("error parsing yaml string",
                        e);
            }

            requireNonNull(update, "root element is null");

            this.lastHash = newYaml.hashCode();
            this.root = update;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean bool(final String key) {
            final Object at = node(key);
            checkType(at instanceof Boolean, BOOL, at, key);
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
        public <U> List<U> list(final String key, Class<U> type) {
            type = sanitizeType(type);
            final List<U> l = new ArrayList<>();
            final Object at = node(key);
            checkType(at instanceof List, LIST, at, key);
            for (final Object o : ((List<?>) at)) {
                if (o == null) {
                    l.add(null);
                }
                else if (type.isAssignableFrom(o.getClass())) {
                    @SuppressWarnings("unchecked")
                    final U kast = (U) o;
                    l.add(kast);
                }
                else {
                    throw newTypeError_Collection(LIST, type, key, o);
                }
            }
            return unmodifiableList(l);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <U> Map<String, T> map(final String key, Class<U> type) {
            type = sanitizeType(type);
            final Map<String, T> m = new HashMap<>();
            final Object at = node(key);
            checkType(at instanceof Map, MAP, at, key);
            for (final Map.Entry<?, ?> e : ((Map<?, ?>) at).entrySet()) {
                final Object k = e.getKey();
                requireNonNull(k, "null map key at key=" + key);
                if (!(k instanceof String))
                    throw new KfgTypeException(
                            "expecting string key, got=" + k.getClass() + ", at=" + key);

                // You'll never know.
                if (m.containsKey(k))
                    throw new KfgException("duplicate key in map, at key=" + key + ", offending key=" + k);

                final Object v = e.getValue();
                if (v == null) {
                    m.put(((String) k), null);
                }
                else if (type.isAssignableFrom(v.getClass())) {
                    @SuppressWarnings("unchecked")
                    final U kast = (U) v;
                    m.put(((String) k), kast);
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
        public boolean contains(final String key) {
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
        public boolean hasUpdate() {
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
        public Konfiguration copyAndUpdate() {
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

                    requireNonNull(c0, "no constructor found for: " + node);

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
        public boolean hasUpdate() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Konfiguration copyAndUpdate() {
            throw new UnsupportedOperationException("source is readonly");
        }

    }


}
