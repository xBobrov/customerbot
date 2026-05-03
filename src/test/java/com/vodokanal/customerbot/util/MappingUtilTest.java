package com.vodokanal.customerbot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MappingUtilTest {
    private final MappingUtil mappingUtil = new MappingUtil(new ObjectMapper());

    @Test
    public void shouldMapHashmapToJson() {
        Map<String, String> map = new HashMap<>();
        map.put("operation", "meter_validation");
        map.put("meterNumber", "987654");
        map.put("chatID", "659832417");

        String result = mappingUtil.mapObjectToJson(map);

        assertTrue(result.contains("\"operation\":\"meter_validation\""));
        assertTrue(result.contains("\"meterNumber\":\"987654\""));
        assertTrue(result.contains("\"chatID\":\"659832417\""));
    }

    @Test
    public void shouldThrownExceptionWhenMapMapToJsonInvalid() {
        Map<Object, Object> map = new HashMap<>();
        map.put(new Object(), new Object());

        assertThrows(RuntimeException.class, () ->
                mappingUtil.mapObjectToJson(map));
    }

    @Test
    public void shouldMapJsonToHashMap() {
        String json = "[{\"number\":\"987654\"}, {\"service\":\"cold_water\"}]";

        List<HashMap<String, String>> result = mappingUtil.mapJsonToHashMapList(json);

        assertEquals(2, result.size());
        assertEquals("987654", result.getFirst().get("number"));
        assertEquals("cold_water", result.get(1).get("service"));
    }

    @Test
    public void shouldThrownExceptionWhenMapJsonToHashMapInvalid() {
        String json = "not:json";

        assertThrows(RuntimeException.class, () ->
                mappingUtil.mapJsonToHashMap(json));
    }
}
