package cc.koosha.konfigurations.json;

import cc.koosha.konfigurations.core.KonfigurationBadTypeException;
import cc.koosha.konfigurations.core.KonfigurationException;
import cc.koosha.konfigurations.core.KonfigurationMissingKeyException;
import cc.koosha.konfigurations.core.impl.BaseKonfiguration;
import cc.koosha.konfigurations.core.impl.KonfigKeyObservers;
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


    public JsonKonfiguration(@NonNull final Supplier<JsonNode> json) {

        this(json, new KonfigKeyObservers(), () -> defaultObjectReader);
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

        val read = this.rootSupplier.get();

        if(read == null)
            return false;

        this.root = read;

        return true;
    }


    private static String key(@NonNull final String key) {

        if(key.length() < 1)
            throw new IllegalArgumentException();

        val k = key.replace('.', '/');
        return k.charAt(0) == '/' ? k : "/" + k;
    }

    private static String concat(@NonNull final String delimiter,
                                 @NonNull final Iterable<?> values) {

        final StringBuilder sb = new StringBuilder();

        for (final Object value : values)
            sb.append(value.toString()).append(delimiter);

        if(sb.length() == 0)
            return "";
        else
            return sb.delete(sb.length() - delimiter.length(), sb.length()).toString();
    }

    @Override
    protected final String _string(@NonNull final String key) {

        val at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        if(at.isArray())
            return concat("", at);

        if(!at.isTextual())
            throw new KonfigurationBadTypeException("not a string: " + key);

        return at.asText();
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
                list.add(reader.readValue(at));
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
            final T nextParsed;
            try {
                nextParsed = reader.readValue(next.getValue());
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
                             @NonNull final Class<?> clazz) {

        val at = this.root.at(key(key));

        if(at.isMissingNode())
            throw new KonfigurationMissingKeyException(key);

        val reader = this.readerSupplier.get();

        try {
            return reader.readValue(at);
        }
        catch (final IOException e) {
            throw new KonfigurationException(e);
        }
    }

}
