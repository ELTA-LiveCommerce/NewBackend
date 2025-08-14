package kr.elta.backend.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Converter
@Slf4j
public class LongListConverter implements AttributeConverter<List<Long>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        
        try {
            String json = objectMapper.writeValueAsString(attribute);
            log.debug("Converting List<Long> to JSON: {} -> {}", attribute, json);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Failed to convert List<Long> to JSON: {}", attribute, e);
            return "[]";
        }
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            List<Long> list = objectMapper.readValue(dbData, new TypeReference<List<Long>>() {});
            log.debug("Converting JSON to List<Long>: {} -> {}", dbData, list);
            return list != null ? list : new ArrayList<>();
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to List<Long>: {}", dbData, e);
            return new ArrayList<>();
        }
    }
}