package io.deeplay.camp.botfarm.bots.denis_bots.tools;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Класс, реализующий сериализацию и десериализацию данных
 */
public class JsonConverter {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static String serialize(Object object) throws JsonProcessingException {
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    return objectMapper.writeValueAsString(object);
  }

  public static <T> T deserialize(String jsonString, Class<T> clazz)
      throws JsonProcessingException {
    return objectMapper.readValue(jsonString, clazz);
  }
}
