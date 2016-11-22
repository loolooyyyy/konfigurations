package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.KonfigSource;
import cc.koosha.konfigurations.core.KonfigurationBadTypeException;
import cc.koosha.konfigurations.core.KonfigurationException;
import cc.koosha.konfigurations.core.KonfigurationMissingKeyException;
import com.fasterxml.jackson.databind.*;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Reads konfig from a json source (supplied as string).
 *
 * for {@link #custom(String, Class)} to work, the supplied json reader must be
 * configured to handle arbitrary types accordingly.
 *
 * Thread safe and immutable.
 */
public final class JsonKonfigSource implements KonfigSource {

    /**
     * Similar to java 8's Supplier.
     */
    public interface KSupplier<T> {

        T get();

    }

    private final KSupplier<ObjectReader> readerSupplier;

    private final KSupplier<String> json;
    private int lastHash;
    private JsonNode root;

    private JsonNode node(final String key) {

        if (key.length() < 1)
            throw new IllegalArgumentException();

        String k = key.replace('.', '/');
        k = k.charAt(0) == '/' ? k : "/" + k;

        val node = this.root.at(k);

        if (node.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        return node;
    }


    @SuppressWarnings("unused")
    public JsonKonfigSource(@NonNull final String json) {

        this(new KSupplier<String>() {
            @Override
            public String get() {
                return json;
            }
        });
    }

    public JsonKonfigSource(@NonNull final KSupplier<String> json) {

        this(json, new KSupplier<ObjectReader>() {

            private final ObjectReader reader = new ObjectMapper().reader();

            @Override
            public ObjectReader get() {
                return reader;
            }
        });
    }

    public JsonKonfigSource(@NonNull final KSupplier<String> json,
                            @NonNull final KSupplier<ObjectReader> objectReader) {

        // Check early, so we're not fooled with a dummy object reader.
        try {
            Class.forName("com.fasterxml.jackson.databind.JsonNode");
        }
        catch (final ClassNotFoundException e) {
            throw new KonfigurationException(getClass().getName() + " requires " +
                    "jackson library to be present in the class path");
        }

        this.json = json;
        this.readerSupplier = objectReader;

        val newJson = this.json.get();
        if (newJson == null)
            throw new KonfigurationException("storage is null");

        val newHash = newJson.hashCode();

        final JsonNode update;
        try {
            update = this.readerSupplier.get().readTree(newJson);
        }
        catch (final IOException e) {
            throw new KonfigurationException(e);
        }

        if (update == null)
            throw new KonfigurationException("root element is null");

        this.root = update;
        this.lastHash = newHash;
    }




    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean bool(final String key) {

        val at = node(key);

        if (!at.isBoolean())
            throw new KonfigurationBadTypeException("not a boolean: " + key);

        return at.asBoolean();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer int_(final String key) {

        val at = node(key);

        if (!at.isInt())
            throw new KonfigurationBadTypeException("not an int: " + key);

        return at.asInt();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Double double_(final String key) {

        val at = node(key);

        if (!at.isDouble())
            throw new KonfigurationBadTypeException("not a double: " + key);

        return at.asDouble();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> list(final String key, final Class<T> el) {

        final JsonNode at = node(key);

        if (!at.isArray())
            throw new KonfigurationBadTypeException("not a list: " + key);

        val reader = this.readerSupplier.get();

        final JavaType javaType =
                reader.getTypeFactory().constructCollectionType(List.class, el);

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

        if (!at.isObject())
            throw new KonfigurationBadTypeException("not a map: " + key);

        val reader = this.readerSupplier.get();

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

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Set<T> set(final String key, final Class<T> el) {

        final JsonNode at = node(key);

        if (!at.isArray())
            throw new KonfigurationBadTypeException("not a set: " + key);

        val reader = this.readerSupplier.get();

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

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T custom(final String key, final Class<T> el) {

        val jsonNode = this.root.at(key);
        val reader = this.readerSupplier.get();
        val traverse = jsonNode.traverse();

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




    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final String key) {

        return !this.root.at(key).isMissingNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUpdatable() {

        val newJson = this.json.get();
        if (newJson == null)
            throw new KonfigurationException("storage is null");

        return newJson.hashCode() != this.lastHash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KonfigSource copy() {

        return new JsonKonfigSource(this.json, this.readerSupplier);
    }

}
