import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.StringJoiner;
import java.util.zip.DeflaterOutputStream;

/**
 * Date: 21.11.13
 * Time: 15:31
 */
public final class JsonParser {

    private static final Logger log = LoggerFactory.getLogger(JsonParser.class);

    private JsonParser() {
    }

    //it is threadsafe
    public static final ObjectMapper MAPPER = init();

    static {
//        MAPPER.addMixIn(Server.Message.class, Server.MessageMixIn.class);
//        MAPPER.addMixIn(Server.Delta.class, Server.DeltaMixIn.class);
    }

    public static ObjectMapper init() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }


    private static byte[] writeJsonAsCompressedBytes(ObjectWriter objectWriter, Object o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStream out = new DeflaterOutputStream(baos)) {
            objectWriter.writeValue(out, o);
        } catch (Exception e) {
            log.error("Error compressing data.", e);
            return null;
        }
        return baos.toByteArray();
    }

    @Nonnull
    public static String toJson(@Nonnull Object o) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (Exception e) {
            log.error("Error jsoning object.", e);
            throw new RuntimeException(e);
        }
    }

    public static String valueToJsonAsString(String[] values) {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        for (String value : values) {
            sj.add(makeJsonStringValue(value));
        }
        return sj.toString();
    }

    public static String valueToJsonAsString(String value) {
        return "[\"" + value  + "\"]";
    }

    private static String makeJsonStringValue(String value) {
        return "\"" + value  + "\"";
    }

}
