package cc.koosha.konfigurations.json;

import cc.koosha.konfigurations.core.KonfigurationBadTypeException;
import cc.koosha.konfigurations.core.KonfigurationException;
import cc.koosha.konfigurations.core.KonfigurationMissingKeyException;
import cc.koosha.konfigurations.core.impl.BaseKonfiguration;
import cc.koosha.konfigurations.core.impl.KonfigKeyObservers;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.NonNull;
import lombok.val;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;


public final class JsonKonfiguration extends BaseKonfiguration {

    private static final ObjectReader defaultObjectReader = new ObjectMapper().reader();

    private Supplier<ObjectReader> readerSupplier;
    private final Supplier<JsonNode> rootSupplier;
    private JsonNode root;

    public JsonKonfiguration(@NonNull final String json) {

        this(() -> json);
    }

    public JsonKonfiguration(@NonNull final Supplier<String> json) {

        this(() -> {
            try {
                return defaultObjectReader.readTree(json.get());
            }
            catch (final IOException e) {
                throw new KonfigurationException(e);
            }
        }, new KonfigKeyObservers(), () -> defaultObjectReader);
    }

    public JsonKonfiguration(@NonNull final Supplier<JsonNode> json,
                             @NonNull final KonfigKeyObservers konfigKeyObservers,
                             @NonNull final Supplier<ObjectReader> objectReader) {

        super(konfigKeyObservers);

        this.rootSupplier = json;
        this.readerSupplier = objectReader;
        this.root = this.rootSupplier.get();

        if(this.root == null)
            throw new NullPointerException("initial root");
    }

    public final boolean update() {

        final JsonNode update = this.rootSupplier.get();

        if(update == null)
            return false;

        this.root = update;

        return true;
    }


    private static String key(@NonNull final String key) {

        if(key.length() < 1)
            throw new IllegalArgumentException();

        val k = key.replace('.', '/');
        return k.charAt(0) == '/' ? k : "/" + k;
    }

    @Override
    protected final String _string(@NonNull final String key) {

        val at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(at.isArray()) {
            val sb = new StringBuilder();
            for (final JsonNode jsonNode : at) {
                ensureNodeType(jsonNode, String.class);
                sb.append(jsonNode.textValue());
            }
            return sb.toString();
        }
        else if(at.isTextual()) {
            return at.asText();
        }

        throw new KonfigurationBadTypeException("not a string: " + key);
    }

    @Override
    protected final Boolean _bool(@NonNull final String key) {

        val at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(!at.isBoolean())
            throw new KonfigurationBadTypeException("not a boolean: " + key);

        return at.asBoolean();
    }

    @Override
    protected final Long _long(@NonNull final String key) {

        val at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(at.isInt())
            return (long) at.asInt();
        else if(at.isLong())
            return at.asLong();
        else
            throw new KonfigurationBadTypeException("not a long: " + key);
    }

    @Override
    protected final Integer _int(@NonNull final String key) {

        val at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(!at.isInt())
            throw new KonfigurationBadTypeException("not an int: " + key);

        return at.asInt();
    }

    private void ensureNodeType(final JsonNode jsonNode, final Class<?> el) {

        if(el == long.class && jsonNode.isInt())
            return;

        if(el == int.class && !jsonNode.isInt()
                || el == long.class && !jsonNode.isLong()
                || el == String.class && !jsonNode.isTextual()
                || el == boolean.class && !jsonNode.isBoolean())
            throw new KonfigurationBadTypeException("expected " + el + " got" + jsonNode.getNodeType());

    }

    @Override
    protected final <T> List<T> _list(@NonNull final String key,
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

        return Collections.unmodifiableList(list);
    }

    @Override
    protected final <T> Map<String, T> _map(@NonNull final String key,
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

        return Collections.unmodifiableMap(map);
    }

    @Override
    protected Object _custom(@NonNull final String key,
                             @NonNull final Class<?> el) {

        val jsonNode = this.root.at(key(key));

        if(jsonNode.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        val reader = this.readerSupplier.get();

        try {
            this.ensureNodeType(jsonNode, el);
            return reader.readValue(jsonNode.traverse(), el);
        }
        catch (final JsonMappingException e) {
            throw new KonfigurationBadTypeException(e);
        }
        catch (final IOException e) {
            throw new KonfigurationException(e);
        }
    }

}
