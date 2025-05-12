package cwchoiit.chat.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Slf4j
public class Serializer {

    private static final ObjectMapper objectMapper = initialize();

    private static ObjectMapper initialize() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> Optional<T> deserialize(String payload, Class<T> clazz) {
        try {
            return Optional.of(objectMapper.readValue(payload, clazz));
        } catch (JsonProcessingException e) {
            log.error("[deserialize] Failed to deserialize payload: {}", payload, e);
            return Optional.empty();
        }
    }

    public static <T> Optional<T> deserialize(InputStream payload, Class<T> clazz) {
        try {
            return Optional.of(objectMapper.readValue(payload, clazz));
        } catch (Exception e) {
            log.error("[deserialize] Failed to deserialize payload: {}", payload, e);
            return Optional.empty();
        }
    }

    public static Optional<String> serialize(Object payload) {
        try {
            return Optional.of(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            log.error("[serialize] Failed to serialize payload: {}", payload, e);
            return Optional.empty();
        }
    }
}
