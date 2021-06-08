package jsonparsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Json {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode parse(String source) throws JsonProcessingException {
        return objectMapper.readTree(source);
    }
}
