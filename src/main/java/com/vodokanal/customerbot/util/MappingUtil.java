package com.vodokanal.customerbot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vodokanal.customerbot.dto.DatabaseRequestDto;
import com.vodokanal.customerbot.dto.DatabaseResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MappingUtil {
    private final ObjectMapper objectMapper;

    @Autowired
    public MappingUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String mapDtoToJson(DatabaseRequestDto databaseRequestDto) {
        try {
            return objectMapper.writeValueAsString(databaseRequestDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public DatabaseResponseDto mapJsonToDto(String response) {
        try {
            return objectMapper.readValue(response, DatabaseResponseDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
