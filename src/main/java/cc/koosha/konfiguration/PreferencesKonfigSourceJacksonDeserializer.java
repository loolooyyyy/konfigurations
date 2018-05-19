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

import static cc.koosha.konfiguration.TypeName.LIST;
import static cc.koosha.konfiguration.TypeName.MAP;


public final class PreferencesKonfigSourceJacksonDeserializer implements Deserializer<String> {

    private final SupplierX<ObjectMapper> mapperSupplier;

    private static ObjectMapper defaultObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public PreferencesKonfigSourceJacksonDeserializer() {
        this(new SupplierX<ObjectMapper>() {
            private final ObjectMapper mapper = defaultObjectMapper();

            @Override
            public ObjectMapper get() {
                return mapper;
            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    public PreferencesKonfigSourceJacksonDeserializer(final SupplierX<ObjectMapper> objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapperSupplier");
        // Check early, so we're not fooled with a dummy object reader.
        try {
            Class.forName("com.fasterxml.jackson.databind.JsonNode");
        }
        catch (final ClassNotFoundException e) {
            throw new KonfigurationSourceException(getClass().getName() + " requires " +
                                                           "jackson library to be present in the class path", e);
        }

        this.mapperSupplier = objectMapper;
    }


    @Override
    public <T> T custom(String from, Class<T> type) throws IOException {
        final ObjectMapper reader = this.mapperSupplier.get();
        final JsonParser root = reader.readTree(from).traverse();
        return reader.readValue(root, type);
    }

    @Override
    public <T> List<T> list(String from, Class<T> type) throws IOException {
        final ObjectMapper reader = this.mapperSupplier.get();
        final JsonNode root = reader.readTree(from);
        checkType(root.isArray(), LIST, root);
        final CollectionType javaType = reader.getTypeFactory().constructCollectionType(List.class, type);
        return reader.readValue(root.traverse(), javaType);
    }

    @Override
    public <T> Map<String, T> map(String from, Class<T> type) throws IOException {
        final ObjectMapper reader = this.mapperSupplier.get();
        final JsonNode root = reader.readTree(from);
        checkType(root.isObject(), MAP, root);
        final MapType javaType = reader.getTypeFactory().constructMapType(Map.class, String.class, type);
        return reader.readValue(root.traverse(), javaType);
    }

    @Override
    public <T> Set<T> set(String from, Class<T> type) throws IOException {
        return new HashSet<>(list(from, type));
    }


    private static void checkType(final boolean isOk,
                                  final TypeName required,
                                  final JsonNode node) {
        if (isOk)
            return;

        throw new KonfigurationTypeException(required.getTName(),
                                             node.getNodeType().toString(),
                                             "/");
    }

}
