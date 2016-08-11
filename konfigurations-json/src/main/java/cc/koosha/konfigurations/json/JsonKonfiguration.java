package cc.koosha.konfigurations.json;

import cc.koosha.konfigurations.core.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.util.*;

import static cc.koosha.konfigurations.core.DummyV.dummy;


public final class JsonKonfiguration implements Konfiguration {

    private static final ObjectReader defaultObjectReader = new ObjectMapper().reader();

    private final Provider<ObjectReader> readerSupplier;
    private final Provider<String> json;
    private int lastHash;
    private JsonNode root;

    private void ensureNodeType(final JsonNode jsonNode, final Class<?> el) {

        if(el == long.class && jsonNode.isInt())
            return;

        if(
                el == int.class && !jsonNode.isInt()
                || el == long.class && !jsonNode.isLong()
                || el == String.class && !jsonNode.isTextual()
                || el == boolean.class && !jsonNode.isBoolean())
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

        this(new Provider<String>() {
            @Override
            public String get() {
                return json;
            }
        });
    }

    public JsonKonfiguration(@NonNull final Provider<String> json) {

        this(json, new Provider<ObjectReader>() {
            @Override
            public ObjectReader get() {
                return defaultObjectReader;
            }
        });
    }

    public JsonKonfiguration(@NonNull final Provider<String> json,
                             @NonNull final Provider<ObjectReader> objectReader) {

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

        val at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(!at.isArray())
            throw new KonfigurationBadTypeException("not an array: " + key);

        final List<T> list = new ArrayList<>(at.size());

        val reader = this.readerSupplier.get();
        for (final JsonNode jsonNode : at)
            try {
                this.ensureNodeType(jsonNode, el);
                final T toAdd = reader.readValue(jsonNode.traverse(), el);
                list.add(toAdd);
            }
            catch (final JsonMappingException e) {
                throw new KonfigurationBadTypeException(e);
            }
            catch (final IOException e) {
                throw new KonfigurationException(e);
            }

        return dummy(Collections.unmodifiableList(list));
    }

    @Override
    public <T> KonfigV<Map<String, T>> map(@NonNull final String key,
                                           @NonNull final Class<T> el) {

        val at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(!at.isObject())
            throw new KonfigurationBadTypeException("not a map: " + key);

        final Map<String, T> map = new HashMap<>(at.size());

        val reader = this.readerSupplier.get();

        final Iterator<Map.Entry<String, JsonNode>> iter = at.fields();
        while(iter.hasNext()) {
            final Map.Entry<String, JsonNode> next = iter.next();
            this.ensureNodeType(next.getValue(), el);
            final T nextParsed;
            try {
                nextParsed = reader.readValue(next.getValue().traverse(), el);
            }
            catch (final JsonMappingException e) {
                throw new KonfigurationBadTypeException(e);
            }
            catch (final IOException e) {
                throw new KonfigurationException(e);
            }
            map.put(next.getKey(), nextParsed);
        }

        return dummy(Collections.unmodifiableMap(map));
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
