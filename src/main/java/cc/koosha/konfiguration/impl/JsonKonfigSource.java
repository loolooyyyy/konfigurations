package cc.koosha.konfiguration.impl;

import cc.koosha.konfiguration.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
    private       int               lastHash;
    private       JsonNode          root;


    private JsonNode node_(final String key) {

        if (key == null || key.isEmpty())
            throw new IllegalArgumentException("bad konfig key: " + key);

        val k = "/" + key.replace('.', '/');
        return this.root.at(k);
    }

    private JsonNode node(final String key) {

        val node = node_(key);

        if (node.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        return node;
    }


    private static ObjectMapper defaultObjectMapper() {

        val mapper = new ObjectMapper();

        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        return mapper;
    }

    private static void checkType(final boolean isOk,
                                  final String required,
                                  final JsonNode node,
                                  final String key) {

        if(isOk)
            return;

        throw new KonfigurationBadTypeException(required, node.getNodeType().toString(), key);
    }


    public JsonKonfigSource(@NonNull final String json) {

        this(new SupplierX<String>() {
            @Override
            public String get() {
                return json;
            }
        });
    }

    public JsonKonfigSource(@NonNull final SupplierX<String> json) {

        this(json, new SupplierX<ObjectMapper>() {
            private final ObjectMapper mapper = defaultObjectMapper();

            @Override
            public ObjectMapper get() {
                return mapper;
            }
        });
    }

    public JsonKonfigSource(@NonNull final SupplierX<String> json,
                            @NonNull final SupplierX<ObjectMapper> objectMapper) {

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

        val newJson = this.json.get();
        if (newJson == null)
            throw new KonfigurationException("storage is null");

        val newHash = newJson.hashCode();

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
        this.lastHash = newHash;
    }


    @Override
    public Boolean bool(final String key) {

        val at = node(key);

        checkType(at.isBoolean(),"boolean", at, key);

        return at.asBoolean();
    }

    @Override
    public Integer int_(final String key) {

        val at = node(key);
        checkType(at.isInt(),"int", at, key);
        return at.asInt();
    }

    @Override
    public Long long_(final String key) {

        val at = node(key);

        if (at.isInt())
            return (long) at.asInt();
        else if (at.isLong())
            return at.asLong();

        checkType(false,"long", at, key);
        throw new IllegalStateException("?!!");
    }

    @Override
    public Double double_(final String key) {

        val at = node(key);

        checkType(at.isDouble(),"double", at, key);

        return at.asDouble();
    }

    @Override
    public String string(final String key) {

        val at = node(key);

        if (at.isArray()) {
            val sb = new StringBuilder();
            for (final JsonNode jsonNode : at) {
                checkType(jsonNode.isTextual(),"string array", at, key);
                sb.append(jsonNode.textValue());
            }
            return sb.toString();
        }
        else if (at.isTextual()) {
            return at.asText();
        }

        checkType(false,"string", at, key);
        throw new IllegalStateException("?!!");
    }

    @Override
    public <T> List<T> list(final String key, final Class<T> el) {

        final JsonNode at = node(key);

        checkType(at.isArray(),"list", at, key);

        val reader = this.mapperSupplier.get();

        final JavaType javaType =
                reader.getTypeFactory().constructCollectionType(List.class, el);

        try {
            return reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KonfigurationException(e);
        }
    }

    @Override
    public <T> Map<String, T> map(final String key, final Class<T> el) {

        final JsonNode at = node(key);

        checkType(at.isObject(),"map", at, key);

        val reader = this.mapperSupplier.get();

        final JavaType javaType =
                reader.getTypeFactory()
                      .constructMapType(Map.class, String.class, el);

        try {
            return reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KonfigurationException(e);
        }
    }

    @Override
    public <T> Set<T> set(final String key, final Class<T> el) {

        final JsonNode at = node(key);

        checkType(at.isArray(),"set", at, key);

        val reader = this.mapperSupplier.get();

        final JavaType javaType =
                reader.getTypeFactory().constructCollectionType(List.class, el);

        final List<T> l;
        try {
            l = reader.readValue(at.traverse(), javaType);
        }
        catch (final IOException e) {
            throw new KonfigurationException(e);
        }

        return new HashSet<>(l);
    }

    @Override
    public <T> T custom(final String key, final Class<T> el) {

        val reader   = this.mapperSupplier.get();
        val traverse = this.node(key).traverse();

        try {
            return reader.readValue(traverse, el);
        }
        catch (final JsonMappingException e) {
            throw new KonfigurationBadTypeException(e);
        }
        catch (final IOException e) {
            throw new KonfigurationException(e);
        }
    }


    @Override
    public boolean contains(final String key) {

        return !this.node_(key).isMissingNode();
    }


    @Override
    public boolean isUpdatable() {

        val newJson = this.json.get();

        if(newJson == null)
            return false;

        final int newHash = newJson.hashCode();
        return newHash != this.lastHash;
    }

    @Override
    public KonfigSource copyAndUpdate() {

        return new JsonKonfigSource(this.json, this.mapperSupplier);
    }

}
