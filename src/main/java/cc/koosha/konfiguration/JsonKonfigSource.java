package cc.koosha.konfiguration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.IOException;
import java.util.*;

import static cc.koosha.konfiguration.TypeName.*;


/**
 * Reads konfig from a json source (supplied as string).
 * <p>
 * for {@link #custom(String, Class)} to work, the supplied json reader must be
 * configured to handle arbitrary types accordingly.
 * <p>
 * Thread safe and immutable.
 */
public final class JsonKonfigSource implements KonfigSource {

    private final SupplierX<ObjectMapper> mapperSupplier;
    private final SupplierX<String> json;
    private int lastHash;
    private JsonNode root;


    private JsonNode node_(final String key) {
        if (key == null || key.isEmpty())
            throw new IllegalArgumentException("bad konfig key: " + key);

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

    private static void checkType(final boolean isOk,
                                  final TypeName required,
                                  final JsonNode node,
                                  final String key) {
        if (isOk)
            return;

        throw new KonfigurationBadTypeException(required.getTName(), node.getNodeType().toString(), key);
    }


    @SuppressWarnings("unused")
    public JsonKonfigSource(final String json) {
        this(new SupplierX<String>() {
            @Override
            public String get() {
                return json;
            }
        });
    }

    public JsonKonfigSource(final SupplierX<String> json) {
        this(json, new SupplierX<ObjectMapper>() {
            private final ObjectMapper mapper = defaultObjectMapper();

            @Override
            public ObjectMapper get() {
                return mapper;
            }
        });
    }

    public JsonKonfigSource(final SupplierX<String> json,
                            final SupplierX<ObjectMapper> objectMapper) {
        Objects.requireNonNull(json, "jsonSupplier");
        Objects.requireNonNull(objectMapper, "objectMapperSupplier");
        // Check early, so we're not fooled with a dummy object reader.
        try {
            Class.forName("com.fasterxml.jackson.databind.JsonNode");
        }
        catch (final ClassNotFoundException e) {
            throw new KonfigurationException(getClass().getName() + " requires " +
                                                     "jackson library to be present in the class path", e);
        }

        this.json = json;
        this.mapperSupplier = objectMapper;

        final String newJson = this.json.get();
        if (newJson == null)
            throw new KonfigurationException("storage is null");

        final JsonNode update;
        try {
            update = this.mapperSupplier.get().readTree(newJson);
        }
        catch (final IOException e) {
            throw new KonfigurationException(e);
        }

        if (update == null)
            throw new KonfigurationException("root element is null");

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
        throw new IllegalStateException("?!!");
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
        }
        else if (at.isTextual()) {
            return at.asText();
        }

        //noinspection ConstantConditions
        checkType(false, STRING, at, key);
        throw new IllegalStateException("?!!");
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
            throw new KonfigurationException(e);
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
            throw new KonfigurationException(e);
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
            throw new KonfigurationException(e);
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
            throw new KonfigurationBadTypeException(e);
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
