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

    private JsonNode node(final String key) {

        if (key.isEmpty())
            throw new IllegalArgumentException();

        final String k    = "/" + key.replace('.', '/');
        val          node = this.root.at(k);

        if (node.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        return node;
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
            private final ObjectMapper mapper;

            {
                mapper = new ObjectMapper();
                mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            }

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

        if (!at.isBoolean())
            throw new KonfigurationBadTypeException("not a boolean: " + key);

        return at.asBoolean();
    }

    @Override
    public Integer int_(final String key) {

        val at = node(key);

        if (!at.isInt())
            throw new KonfigurationBadTypeException("not an int: " + key);

        return at.asInt();
    }

    @Override
    public Long long_(final String key) {

        val at = node(key);

        if (at.isInt())
            return (long) at.asInt();
        else if (at.isLong())
            return at.asLong();
        else
            throw new KonfigurationBadTypeException("not a long: " + key);
    }

    @Override
    public Double double_(final String key) {

        val at = node(key);

        if (!at.isDouble())
            throw new KonfigurationBadTypeException("not a double: " + key);

        return at.asDouble();
    }

    @Override
    public String string(final String key) {

        val at = node(key);

        if (at.isArray()) {
            val sb = new StringBuilder();
            for (final JsonNode jsonNode : at) {
                if (!jsonNode.isTextual())
                    throw new KonfigurationBadTypeException("not a string array: " + key);
                sb.append(jsonNode.textValue());
            }
            return sb.toString();
        }
        else if (at.isTextual()) {
            return at.asText();
        }

        throw new KonfigurationBadTypeException("not a string: " + key);
    }

    @Override
    public <T> List<T> list(final String key, final Class<T> el) {

        final JsonNode at = node(key);

        if (!at.isArray())
            throw new KonfigurationBadTypeException("not a list: " + key);

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

        if (!at.isObject())
            throw new KonfigurationBadTypeException("not a map: " + key);

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

        if (!at.isArray())
            throw new KonfigurationBadTypeException("not a set: " + key);

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

        final String k = "/" + key.replace('.', '/');
        return !this.root.at(k).isMissingNode();
    }

    @Override
    public boolean isUpdatable() {

        val newJson = this.json.get();
        return newJson != null && newJson.hashCode() != this.lastHash;
    }

    @Override
    public KonfigSource copyAndUpdate() {

        return new JsonKonfigSource(this.json, this.mapperSupplier);
    }

}
