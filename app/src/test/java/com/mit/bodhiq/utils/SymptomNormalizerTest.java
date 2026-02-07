package com.mit.bodhiq.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for SymptomNormalizer
 */
public class SymptomNormalizerTest {
    
    @Test
    public void testBasicNormalization() {
        String input = "I Have A HEADACHE";
        String result = SymptomNormalizer.normalize(input);
        assertEquals("i have a headache", result);
    }
    
    @Test
    public void testMisspellingCorrection() {
        assertEquals("i have a fever", SymptomNormalizer.normalize("I have a feaver"));
        assertEquals("headache", SymptomNormalizer.normalize("hedache"));
        assertEquals("diarrhea", SymptomNormalizer.normalize("diarhea"));
    }
    
    @Test
    public void testSynonymReplacement() {
        assertEquals("difficulty breathing", SymptomNormalizer.normalize("can't breathe"));
        assertEquals("difficulty breathing", SymptomNormalizer.normalize("cannot breathe"));
        assertEquals("vomiting", SymptomNormalizer.normalize("throwing up"));
    }
    
    @Test
    public void testTemperatureNormalization() {
        String result = SymptomNormalizer.normalize("fever of 102F");
        assertTrue(result.contains("°C"));
        assertTrue(result.contains("38.9") || result.contains("38.8")); // 102F ≈ 38.9°C
    }
    
    @Test
    public void testDurationNormalization() {
        assertEquals("symptoms for 2 days", SymptomNormalizer.normalize("symptoms for 2days"));
        assertEquals("pain for 1 day", SymptomNormalizer.normalize("pain for one day"));
    }
    
    @Test
    public void testComplexInput() {
        String input = "I've had a feaver of 102F and hedache for two days, can't breathe well";
        String result = SymptomNormalizer.normalize(input);
        
        assertTrue(result.contains("fever"));
        assertTrue(result.contains("headache"));
        assertTrue(result.contains("difficulty breathing"));
        assertTrue(result.contains("2 days"));
    }
    
    @Test
    public void testEmptyInput() {
        assertEquals("", SymptomNormalizer.normalize(""));
        assertEquals("", SymptomNormalizer.normalize(null));
    }
}
