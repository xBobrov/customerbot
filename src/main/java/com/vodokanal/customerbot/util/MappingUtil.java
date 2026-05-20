package com.vodokanal.customerbot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility service for data conversion between JSON format and Java objects.
 * <p>
 * Utilizes the Jackson library for serializing RabbitMQ requests and
 * deserializing responses from external systems.
 * </p>
 */
@Service
public class MappingUtil {
    private final ObjectMapper objectMapper;

    @Autowired
    public MappingUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts Java object into JSON string.
     *
     * @param object java object to serialize.
     * @return JSON representation of the object.
     * @throws RuntimeException if an error occurs while writing JSON.
     */
    public String mapObjectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts JSON string into List of HashMaps.
     *
     * @param json JSON string.
     * @return {@link List} of {@link HashMap}, where every Map represents one single entity.
     * @throws RuntimeException If the JSON structure does not match the expected list of maps.
     */
    public List<HashMap<String, String>> mapJsonToHashMapList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<HashMap<String, String>>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts JSON string into a HashMap.
     *
     * @param json JSON string.
     * @return {@code  Map} with data or {@link Collections#emptyMap()}, if JSON is empty.
     * @throws RuntimeException if deserialization error occurs.
     */
    public Map<String, String> mapJsonToHashMap(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
