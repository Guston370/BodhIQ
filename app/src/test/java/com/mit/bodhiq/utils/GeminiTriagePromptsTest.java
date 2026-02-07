package com.mit.bodhiq.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for GeminiTriagePrompts
 */
public class GeminiTriagePromptsTest {
    
    @Test
    public void testSystemPromptContainsFewShotExamples() {
        String prompt = GeminiTriagePrompts.TRIAGE_SYSTEM_PROMPT;
        
        // Verify few-shot examples are present
        assertTrue("Should contain emergency example", prompt.contains("I can't breathe"));
        assertTrue("Should contain urgent example", prompt.contains("40°C"));
        assertTrue("Should contain routine example", prompt.contains("Mild sore throat"));
        
        // Verify urgency levels in examples
        assertTrue("Should show emergency urgency", prompt.contains("\"urgency\": \"emergency\""));
        assertTrue("Should show urgent urgency", prompt.contains("\"urgency\": \"urgent\""));
        assertTrue("Should show routine urgency", prompt.contains("\"urgency\": \"routine\""));
    }
    
    @Test
    public void testSystemPromptContainsJSONSchema() {
        String prompt = GeminiTriagePrompts.TRIAGE_SYSTEM_PROMPT;
        
        assertTrue("Should contain extracted field", prompt.contains("\"extracted\""));
        assertTrue("Should contain triage field", prompt.contains("\"triage\""));
        assertTrue("Should contain suggestions field", prompt.contains("\"suggestions\""));
        assertTrue("Should contain confidence_score field", prompt.contains("\"confidence_score\""));
    }
    
    @Test
    public void testCreateUserMessageWithContext() {
        String symptoms = "headache and fever";
        String patientContext = "Age: 30, Gender: Male";
        String recentMessages = "User: I feel tired\nBot: Rest is important";
        
        String message = GeminiTriagePrompts.createUserMessage(symptoms, patientContext, recentMessages);
        
        assertTrue("Should contain symptoms", message.contains(symptoms));
        assertTrue("Should contain patient context", message.contains(patientContext));
        assertTrue("Should contain recent messages", message.contains(recentMessages));
    }
    
    @Test
    public void testCreateUserMessageWithoutContext() {
        String symptoms = "cough";
        
        String message = GeminiTriagePrompts.createUserMessage(symptoms, null, null);
        
        assertTrue("Should contain symptoms", message.contains(symptoms));
        assertFalse("Should not contain context label when null", message.contains("Patient context:"));
    }
    
    @Test
    public void testRetryPromptIsStricter() {
        String originalInput = "chest pain";
        String retryPrompt = GeminiTriagePrompts.createRetryPrompt(originalInput);
        
        assertTrue("Should contain original input", retryPrompt.contains(originalInput));
        assertTrue("Should mention malformed", retryPrompt.toLowerCase().contains("malformed"));
        assertTrue("Should emphasize JSON only", retryPrompt.toLowerCase().contains("only valid json"));
    }
}
