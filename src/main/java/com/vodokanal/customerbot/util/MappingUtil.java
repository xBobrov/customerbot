package com.vodokanal.customerbot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class MappingUtil {
    private final ObjectMapper objectMapper;

    @Autowired
    public MappingUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String mapObjectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T mapJsonToObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<HashMap<String, String>> mapJsonToHashMapList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<HashMap<String, String>>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, String> mapJsonToHashMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<HashMap<String, String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String parseFGISResponse(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            return rootNode.path("result").path("items").path(0).path("valid_date").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
