package com.mit.bodhiq.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Pre-check detector for medical red flags that require immediate emergency response
 * Bypasses AI model for critical situations to ensure fastest response
 */
public class RedFlagDetector {
    
    private static final List<String> CHEST_PAIN_KEYWORDS = Arrays.asList(
        "chest pain", "chest pressure", "crushing chest", "tight chest",
        "squeezing chest", "chest discomfort", "pain in chest"
    );
    
    private static final List<String> BREATHING_KEYWORDS = Arrays.asList(
        "can't breathe", "cannot breathe", "difficulty breathing",
        "shortness of breath", "gasping", "choking", "suffocating",
        "hard to breathe", "trouble breathing"
    );
    
    private static final List<String> CONSCIOUSNESS_KEYWORDS = Arrays.asList(
        "unconscious", "passed out", "fainted", "fainting",
        "losing consciousness", "blacking out", "unresponsive"
    );
    
    private static final List<String> BLEEDING_KEYWORDS = Arrays.asList(
        "severe bleeding", "heavy bleeding", "bleeding heavily",
        "blood loss", "hemorrhage", "bleeding won't stop",
        "profuse bleeding", "gushing blood"
    );
    
    private static final List<String> STROKE_KEYWORDS = Arrays.asList(
        "stroke", "face drooping", "arm weakness", "speech difficulty",
        "slurred speech", "sudden confusion", "sudden numbness",
        "sudden severe headache", "vision loss", "sudden dizziness"
    );
    
    private static final List<String> ANAPHYLAXIS_KEYWORDS = Arrays.asList(
        "anaphylaxis", "throat swelling", "tongue swelling",
        "severe allergic reaction", "throat closing", "hives all over",
        "difficulty swallowing", "swollen throat"
    );
    
    /**
     * Check if input contains any red flag keywords
     */
    public static boolean hasRedFlags(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        String normalized = SymptomNormalizer.normalize(input);
        
        return containsAny(normalized, CHEST_PAIN_KEYWORDS) ||
               containsAny(normalized, BREATHING_KEYWORDS) ||
               containsAny(normalized, CONSCIOUSNESS_KEYWORDS) ||
               containsAny(normalized, BLEEDING_KEYWORDS) ||
               containsAny(normalized, STROKE_KEYWORDS) ||
               containsAny(normalized, ANAPHYLAXIS_KEYWORDS);
    }
    
    /**
     * Get specific red flag type
     */
    public static RedFlagType getRedFlagType(String input) {
        if (input == null || input.trim().isEmpty()) {
            return RedFlagType.NONE;
        }
        
        String normalized = SymptomNormalizer.normalize(input);
        
        if (containsAny(normalized, CHEST_PAIN_KEYWORDS)) {
            return RedFlagType.CHEST_PAIN;
        }
        if (containsAny(normalized, BREATHING_KEYWORDS)) {
            return RedFlagType.BREATHING_DIFFICULTY;
        }
        if (containsAny(normalized, CONSCIOUSNESS_KEYWORDS)) {
            return RedFlagType.LOSS_OF_CONSCIOUSNESS;
        }
        if (containsAny(normalized, BLEEDING_KEYWORDS)) {
            return RedFlagType.SEVERE_BLEEDING;
        }
        if (containsAny(normalized, STROKE_KEYWORDS)) {
            return RedFlagType.STROKE_SIGNS;
        }
        if (containsAny(normalized, ANAPHYLAXIS_KEYWORDS)) {
            return RedFlagType.ANAPHYLAXIS;
        }
        
        return RedFlagType.NONE;
    }
    
    private static boolean containsAny(String text, List<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    public enum RedFlagType {
        NONE,
        CHEST_PAIN,
        BREATHING_DIFFICULTY,
        LOSS_OF_CONSCIOUSNESS,
        SEVERE_BLEEDING,
        STROKE_SIGNS,
        ANAPHYLAXIS
    }
}
