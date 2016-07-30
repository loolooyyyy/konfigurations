package cc.koosha.konfigurations.json;

import cc.koosha.konfigurations.core.KonfigV;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AllArgsConstructor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;


public class JsonKonfigurationCustomTypeTest extends JsonKonfigurationBaseTest {

    @AllArgsConstructor
    static final class CustomType {
        final int hour;
        final int minute;
    }

    static final class CustomTypeJacksonDeserializer extends StdDeserializer<CustomType> {

        public CustomTypeJacksonDeserializer() {
            this(null);
        }

        public CustomTypeJacksonDeserializer(final Class<?> vc) {
            super(vc);
        }

        @Override
        public CustomType deserialize(final JsonParser jp,
                                      final DeserializationContext ctxt) throws IOException {

            final String[] split = jp.getValueAsString().split(":");
            Assert.assertEquals(split.length, 2);
            final int h = Integer.parseInt(split[0]);
            final int m = Integer.parseInt(split[1]);

            return new CustomType(h, m);
        }
    }

    @Test
    public void testCustomType() throws Exception {

        final String c = super.content();
        final ObjectMapper r = new ObjectMapper();
        final SimpleModule sm = new SimpleModule();
        sm.addDeserializer(CustomType.class, new CustomTypeJacksonDeserializer());
        r.registerModule(sm);

        this.konfiguration = new JsonKonfiguration(() -> c, r::reader);

        final CustomType customValue = this.konfiguration
                .custom("customValue", CustomType.class).v();

        Assert.assertEquals(customValue.hour, 3);
        Assert.assertEquals(customValue.minute, 15);
    }
}