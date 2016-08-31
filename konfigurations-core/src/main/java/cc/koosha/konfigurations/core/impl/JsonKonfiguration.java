package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cc.koosha.konfigurations.core.DummyV.dummy;


public final class JsonKonfiguration implements Konfiguration {

    private final KSupplier<ObjectReader> readerSupplier;
    private final KSupplier<String> json;
    private int lastHash;
    private JsonNode root;

    private void ensureNodeType(final JsonNode jsonNode, final Class<?> el) {

        if(el == Long.class && jsonNode.isInt())
            return;

        if(
                el == Integer.class && !jsonNode.isInt()
                || el == Long.class && !jsonNode.isLong()
                || el == String.class && !jsonNode.isTextual()
                || el == Boolean.class && !jsonNode.isBoolean())
            throw new KonfigurationBadTypeException("expected " + el + " got" + jsonNode.getNodeType());

    }

    private static String key(@NonNull final String key) {

        if(key.length() < 1)
            throw new IllegalArgumentException();

        val k = key.replace('.', '/');
        return k.charAt(0) == '/' ? k : "/" + k;
    }


    // _________________________________________________________________________

    public JsonKonfiguration(@NonNull final String json) {

        this(new KSupplier<String>() {
            @Override
            public String get() {
                return json;
            }
        });
    }

    public JsonKonfiguration(@NonNull final KSupplier<String> json) {

        final ObjectReader reader = new ObjectMapper().reader();
        this.json = json;
        this.readerSupplier = new KSupplier<ObjectReader>() {
            @Override
            public ObjectReader get() {
                return reader;
            }
        };
        this.lastHash = -1;

        this.update();
    }

    public JsonKonfiguration(@NonNull final KSupplier<String> json,
                             @NonNull final KSupplier<ObjectReader> objectReader) {

        this.json = json;
        this.readerSupplier = objectReader;
        this.lastHash = -1;

        this.update();
    }


    // _________________________________________________________________________

    @Override
    public boolean update() {

        val newJson = this.json.get();
        if(newJson == null)
            return false;

        val newHash = newJson.hashCode();
        if(newHash == this.lastHash)
            return false;

        final JsonNode update;
        try {
            update = this.readerSupplier.get().readTree(newJson);
        }
        catch (final IOException e) {
            throw new KonfigurationException(e);
        }

        if(update == null)
            throw new KonfigurationException("root element is null");

        this.root = update;
        this.lastHash = newHash;

        return true;
    }

    @Override
    public Konfiguration subset(final String key) {

        return new SubsetKonfiguration(this, key);
    }

    @Override
    public Konfiguration parent() {

        return this;
    }


    @Override
    public KonfigV<String> string(@NonNull final String key) {

        val at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(at.isArray()) {
            val sb = new StringBuilder();
            for (final JsonNode jsonNode : at) {
                ensureNodeType(jsonNode, String.class);
                sb.append(jsonNode.textValue());
            }
            return dummy(sb.toString());
        }
        else if(at.isTextual()) {
            return dummy(at.asText());
        }

        throw new KonfigurationBadTypeException("not a string: " + key);
    }

    @Override
    public KonfigV<Boolean> bool(@NonNull final String key) {

        val at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(!at.isBoolean())
            throw new KonfigurationBadTypeException("not a boolean: " + key);

        return dummy(at.asBoolean());
    }

    @Override
    public KonfigV<Long> long_(@NonNull final String key) {

        val at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(at.isInt())
            return dummy((long) at.asInt());
        else if(at.isLong())
            return dummy(at.asLong());
        else
            throw new KonfigurationBadTypeException("not a long: " + key);
    }

    @Override
    public KonfigV<Integer> int_(@NonNull final String key) {

        val at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(!at.isInt())
            throw new KonfigurationBadTypeException("not an int: " + key);

        return dummy(at.asInt());
    }

    @Override
    public <T> KonfigV<List<T>> list(@NonNull final String key,
                                     @NonNull final Class<T> el) {

        final JsonNode at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(!at.isArray())
            throw new KonfigurationBadTypeException("not an array: " + key);

        val reader = this.readerSupplier.get();

        final JavaType javaType =
                reader.getTypeFactory().constructCollectionType(List.class, el);

        try {
            final List<T> list = reader.readValue(at.traverse(), javaType);
            return dummy(list);
        }
        catch (final IOException e) {
            throw new KonfigurationException(e);
        }
    }

    @Override
    public <T> KonfigV<Map<String, T>> map(@NonNull final String key,
                                           @NonNull final Class<T> el) {

        final JsonNode at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(!at.isObject())
            throw new KonfigurationBadTypeException("not a map: " + key);

        val reader = this.readerSupplier.get();

        final JavaType javaType =
                reader.getTypeFactory().constructMapType(Map.class, String.class, el);

        try {
            final Map<String, T> map = reader.readValue(at.traverse(), javaType);
            return dummy(map);
        }
        catch (final IOException e) {
            throw new KonfigurationException(e);
        }
    }

    @Override
    public <T> KonfigV<Set<T>> set(final String key, final Class<T> el) {

        final List<T> asList = this.list(key, el).v();
        final Set<T> asSet = new HashSet<>(asList);
        return dummy(asSet);
    }

    @Override
    public <T> KonfigV<T> custom(@NonNull final String key,
                                 @NonNull final Class<T> el) {

        val jsonNode = this.root.at(key(key));

        if(jsonNode.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        val reader = this.readerSupplier.get();

        try {
            this.ensureNodeType(jsonNode, el);
            final JsonParser traverse = jsonNode.traverse();
            final T readT = reader.readValue(traverse, el);
            return dummy(readT);
        }
        catch (final JsonMappingException e) {
            throw new KonfigurationBadTypeException(e);
        }
        catch (final IOException e) {
            throw new KonfigurationException(e);
        }
    }

}
