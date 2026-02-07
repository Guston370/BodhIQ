package com.mit.bodhiq.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for RedFlagDetector
 */
public class RedFlagDetectorTest {
    
    @Test
    public void testChestPainDetection() {
        assertTrue(RedFlagDetector.hasRedFlags("I have severe chest pain"));
        assertTrue(RedFlagDetector.hasRedFlags("crushing chest pressure"));
        assertEquals(RedFlagDetector.RedFlagType.CHEST_PAIN, 
                    RedFlagDetector.getRedFlagType("chest pain and sweating"));
    }
    
    @Test
    public void testBreathingDifficultyDetection() {
        assertTrue(RedFlagDetector.hasRedFlags("I can't breathe"));
        assertTrue(RedFlagDetector.hasRedFlags("difficulty breathing"));
        assertTrue(RedFlagDetector.hasRedFlags("shortness of breath"));
        assertEquals(RedFlagDetector.RedFlagType.BREATHING_DIFFICULTY,
                    RedFlagDetector.getRedFlagType("I cannot breathe properly"));
    }
    
    @Test
    public void testConsciousnessDetection() {
        assertTrue(RedFlagDetector.hasRedFlags("I passed out"));
        assertTrue(RedFlagDetector.hasRedFlags("feeling unconscious"));
        assertEquals(RedFlagDetector.RedFlagType.LOSS_OF_CONSCIOUSNESS,
                    RedFlagDetector.getRedFlagType("I fainted"));
    }
    
    @Test
    public void testBleedingDetection() {
        assertTrue(RedFlagDetector.hasRedFlags("severe bleeding"));
        assertTrue(RedFlagDetector.hasRedFlags("heavy bleeding won't stop"));
        assertEquals(RedFlagDetector.RedFlagType.SEVERE_BLEEDING,
                    RedFlagDetector.getRedFlagType("bleeding heavily"));
    }
    
    @Test
    public void testStrokeDetection() {
        assertTrue(RedFlagDetector.hasRedFlags("face drooping"));
        assertTrue(RedFlagDetector.hasRedFlags("sudden arm weakness"));
        assertEquals(RedFlagDetector.RedFlagType.STROKE_SIGNS,
                    RedFlagDetector.getRedFlagType("slurred speech and confusion"));
    }
    
    @Test
    public void testAnaphylaxisDetection() {
        assertTrue(RedFlagDetector.hasRedFlags("throat swelling"));
        assertTrue(RedFlagDetector.hasRedFlags("severe allergic reaction"));
        assertEquals(RedFlagDetector.RedFlagType.ANAPHYLAXIS,
                    RedFlagDetector.getRedFlagType("anaphylaxis symptoms"));
    }
    
    @Test
    public void testNoRedFlags() {
        assertFalse(RedFlagDetector.hasRedFlags("mild headache"));
        assertFalse(RedFlagDetector.hasRedFlags("sore throat"));
        assertFalse(RedFlagDetector.hasRedFlags("slight fever"));
        assertEquals(RedFlagDetector.RedFlagType.NONE,
                    RedFlagDetector.getRedFlagType("mild cough"));
    }
    
    @Test
    public void testCaseInsensitive() {
        assertTrue(RedFlagDetector.hasRedFlags("CHEST PAIN"));
        assertTrue(RedFlagDetector.hasRedFlags("Can't Breathe"));
    }
    
    @Test
    public void testEmptyInput() {
        assertFalse(RedFlagDetector.hasRedFlags(""));
        assertFalse(RedFlagDetector.hasRedFlags(null));
        assertEquals(RedFlagDetector.RedFlagType.NONE,
                    RedFlagDetector.getRedFlagType(""));
    }
}
