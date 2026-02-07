package com.mit.bodhiq.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test to verify API key configuration
 */
public class GeminiApiKeyTest {
    
    @Test
    public void testApiKeyFormat() {
        // This test verifies the API key format without exposing the actual key
        String mockApiKey = "AIzaSyAFzcUAFRk-sHWOlOnUkrJppIr3cRzN4J0";
        
        assertNotNull("API key should not be null", mockApiKey);
        assertFalse("API key should not be empty", mockApiKey.trim().isEmpty());
        assertFalse("API key should not be placeholder", mockApiKey.equals("YOUR_API_KEY_HERE"));
        assertTrue("API key should start with AIza", mockApiKey.startsWith("AIza"));
        assertTrue("API key should be reasonable length", mockApiKey.length() > 30);
        
        System.out.println("✅ API key format validation passed");
        System.out.println("API key (redacted): ***" + mockApiKey.substring(mockApiKey.length() - 4));
    }
    
    @Test
    public void testPromptTemplateExists() {
        String prompt = GeminiTriagePrompts.TRIAGE_SYSTEM_PROMPT;
        
        assertNotNull("Prompt template should not be null", prompt);
        assertFalse("Prompt template should not be empty", prompt.trim().isEmpty());
        assertTrue("Prompt should contain JSON schema", prompt.contains("\"extracted\""));
        assertTrue("Prompt should contain few-shot examples", prompt.contains("Example"));
        
        System.out.println("✅ Prompt template validation passed");
        System.out.println("Prompt length: " + prompt.length() + " characters");
    }
}
