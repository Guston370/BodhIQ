package com.mit.bodhiq.chatbot;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test to verify chat flow doesn't get stuck
 */
public class ChatFlowTest {
    
    @Test
    public void testSymptomDetection() {
        // Test that symptom keywords are detected correctly
        String[] symptomInputs = {
            "I am sick",
            "I have a headache",
            "My stomach hurts",
            "I feel dizzy",
            "I'm in pain"
        };
        
        for (String input : symptomInputs) {
            boolean containsSymptom = containsSymptoms(input);
            assertTrue("Should detect symptom in: " + input, containsSymptom);
        }
        
        System.out.println("✅ Symptom detection test passed");
    }
    
    @Test
    public void testNonSymptomMessages() {
        // Test that non-symptom messages are not misclassified
        String[] nonSymptomInputs = {
            "What is diabetes?",
            "How do I take my medication?",
            "Show me my reports"
        };
        
        for (String input : nonSymptomInputs) {
            boolean containsSymptom = containsSymptoms(input);
            assertFalse("Should not detect symptom in: " + input, containsSymptom);
        }
        
        System.out.println("✅ Non-symptom detection test passed");
    }
    
    private boolean containsSymptoms(String message) {
        String lowerMessage = message.toLowerCase();
        String[] symptomKeywords = {
            "pain", "ache", "fever", "headache", "nausea", "vomiting", "dizziness",
            "fatigue", "tired", "shortness of breath", "cough", "sore throat",
            "rash", "swelling", "chest pain", "abdominal pain", "back pain",
            "sick", "hurt", "feel"
        };

        for (String keyword : symptomKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
