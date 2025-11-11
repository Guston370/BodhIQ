package com.mit.bodhiq.chatbot;

/**
 * Detects emergency situations from user messages
 */
public class EmergencyDetector {
    
    public enum EmergencyType {
        CHEST_PAIN,
        BREATHING_DIFFICULTY,
        SEVERE_BLEEDING,
        LOSS_OF_CONSCIOUSNESS,
        STROKE_SYMPTOMS,
        SEVERE_ALLERGIC_REACTION,
        GENERAL_EMERGENCY
    }
    
    /**
     * Detect if message contains emergency keywords
     */
    public boolean detectEmergency(String message) {
        String lower = message.toLowerCase();
        
        // Critical emergency keywords
        String[] emergencyKeywords = {
            "chest pain", "can't breathe", "cannot breathe", "difficulty breathing",
            "shortness of breath", "severe bleeding", "heavy bleeding", "blood loss",
            "unconscious", "passed out", "fainted", "not breathing",
            "heart attack", "stroke", "seizure", "choking",
            "severe pain", "extreme pain", "unbearable pain",
            "allergic reaction", "anaphylaxis", "swelling throat",
            "call 911", "emergency", "help me", "dying"
        };
        
        for (String keyword : emergencyKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get specific emergency type
     */
    public EmergencyType getEmergencyType(String message) {
        String lower = message.toLowerCase();
        
        // Chest pain
        if (lower.contains("chest pain") || lower.contains("chest pressure") || 
            lower.contains("heart attack")) {
            return EmergencyType.CHEST_PAIN;
        }
        
        // Breathing difficulty
        if (lower.contains("can't breathe") || lower.contains("cannot breathe") ||
            lower.contains("difficulty breathing") || lower.contains("shortness of breath") ||
            lower.contains("choking")) {
            return EmergencyType.BREATHING_DIFFICULTY;
        }
        
        // Severe bleeding
        if (lower.contains("severe bleeding") || lower.contains("heavy bleeding") ||
            lower.contains("blood loss") || lower.contains("bleeding heavily")) {
            return EmergencyType.SEVERE_BLEEDING;
        }
        
        // Loss of consciousness
        if (lower.contains("unconscious") || lower.contains("passed out") ||
            lower.contains("fainted") || lower.contains("not breathing")) {
            return EmergencyType.LOSS_OF_CONSCIOUSNESS;
        }
        
        // Stroke symptoms
        if (lower.contains("stroke") || lower.contains("face drooping") ||
            lower.contains("arm weakness") || lower.contains("speech difficulty") ||
            lower.contains("sudden confusion")) {
            return EmergencyType.STROKE_SYMPTOMS;
        }
        
        // Severe allergic reaction
        if (lower.contains("allergic reaction") || lower.contains("anaphylaxis") ||
            lower.contains("swelling throat") || lower.contains("hives all over")) {
            return EmergencyType.SEVERE_ALLERGIC_REACTION;
        }
        
        return EmergencyType.GENERAL_EMERGENCY;
    }
}
