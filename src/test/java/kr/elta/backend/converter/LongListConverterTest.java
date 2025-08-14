package kr.elta.backend.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LongListConverterTest {

    private LongListConverter converter;

    @BeforeEach
    void setUp() {
        converter = new LongListConverter();
    }

    @Test
    void testConvertToDatabaseColumn_WithValidList() {
        // Given
        List<Long> productIds = Arrays.asList(1L, 2L, 3L, 4L);
        
        // When
        String json = converter.convertToDatabaseColumn(productIds);
        
        // Then
        assertEquals("[1,2,3,4]", json);
    }

    @Test
    void testConvertToDatabaseColumn_WithEmptyList() {
        // Given
        List<Long> emptyList = new ArrayList<>();
        
        // When
        String json = converter.convertToDatabaseColumn(emptyList);
        
        // Then
        assertEquals("[]", json);
    }

    @Test
    void testConvertToDatabaseColumn_WithNullList() {
        // When
        String json = converter.convertToDatabaseColumn(null);
        
        // Then
        assertEquals("[]", json);
    }

    @Test
    void testConvertToEntityAttribute_WithValidJson() {
        // Given
        String json = "[1,2,3,4]";
        
        // When
        List<Long> result = converter.convertToEntityAttribute(json);
        
        // Then
        assertEquals(Arrays.asList(1L, 2L, 3L, 4L), result);
    }

    @Test
    void testConvertToEntityAttribute_WithEmptyJson() {
        // Given
        String json = "[]";
        
        // When
        List<Long> result = converter.convertToEntityAttribute(json);
        
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertToEntityAttribute_WithNullJson() {
        // When
        List<Long> result = converter.convertToEntityAttribute(null);
        
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertToEntityAttribute_WithEmptyString() {
        // When
        List<Long> result = converter.convertToEntityAttribute("");
        
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testRoundTrip() {
        // Given
        List<Long> originalList = Arrays.asList(100L, 200L, 300L);
        
        // When
        String json = converter.convertToDatabaseColumn(originalList);
        List<Long> convertedBack = converter.convertToEntityAttribute(json);
        
        // Then
        assertEquals(originalList, convertedBack);
    }
}