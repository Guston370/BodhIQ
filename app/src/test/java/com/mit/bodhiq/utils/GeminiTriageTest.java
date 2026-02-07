package com.mit.bodhiq.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import static org.junit.Assert.*;
import com.google.gson.Gson;
import com.mit.bodhiq.data.model.TriageResponse;
import com.mit.bodhiq.data.model.ChatMessage;

@RunWith(RobolectricTestRunner.class)
public class GeminiTriageTest {

    @Test
    public void testTriageResponseParsing() {
        String json = "{\n" +
                "  \"extracted\": {\n" +
                "    \"presenting_symptoms\": [\"chest pain\"],\n" +
                "    \"severity\": \"high\"\n" +
                "  },\n" +
                "  \"triage\": {\n" +
                "    \"urgency\": \"emergency\",\n" +
                "    \"reasons\": [\"Possible cardiac event\"]\n" +
                "  },\n" +
                "  \"suggestions\": {\n" +
                "    \"immediate_actions\": [\"Call 911\"],\n" +
                "    \"red_flag_wording\": \"Heart Attack\",\n" +
                "    \"recommended_specialist\": \"ER\"\n" +
                "  },\n" +
                "  \"confidence_score\": 0.95,\n" +
                "  \"disclaimer\": \"Not a diagnosis\"\n" +
                "}";

        Gson gson = new Gson();
        TriageResponse response = gson.fromJson(json, TriageResponse.class);

        assertNotNull(response);
        assertNotNull(response.getExtracted());
        assertEquals("chest pain", response.getExtracted().getPresentingSymptoms().get(0));
        assertEquals("emergency", response.getTriage().getUrgency());
        assertEquals(0.95, response.getConfidenceScore(), 0.01);
    }

    @Test
    public void testRedFlagDetection() {
        String criticalInput = "I have severe chest pain and cannot breathe";
        ChatMessage.Severity severity = EmpathicMedicalResponseGenerator.detectSeverityFromInput(criticalInput);
        assertEquals(ChatMessage.Severity.CRITICAL, severity);

        String routineInput = "I have a mild headache";
        ChatMessage.Severity routineSeverity = EmpathicMedicalResponseGenerator.detectSeverityFromInput(routineInput);
        assertEquals(ChatMessage.Severity.LOW, routineSeverity);
    }

    @Test
    public void testEmergencyResponseStructure() {
        // Simulate the emergency response creation logic from GeminiApiService
        String userMessage = "I am bleeding heavily";
        String json = "{\n" +
                "  \"extracted\": {\n" +
                "    \"presenting_symptoms\": [\"" + userMessage + "\"],\n" +
                "    \"severity\": \"CRITICAL\"\n" +
                "  },\n" +
                "  \"triage\": {\n" +
                "    \"urgency\": \"emergency\",\n" +
                "    \"reasons\": [\"Red-flag keywords detected in user input\"]\n" +
                "  },\n" +
                "  \"suggestions\": {\n" +
                "    \"immediate_actions\": [\"Call emergency services immediately\"],\n" +
                "    \"red_flag_wording\": \"Potential medical emergency detected\",\n" +
                "    \"recommended_specialist\": \"Emergency Room\"\n" +
                "  },\n" +
                "  \"confidence_score\": 1.0,\n" +
                "  \"disclaimer\": \"This is an automated safety alert based on keywords.\"\n" +
                "}";

        Gson gson = new Gson();
        TriageResponse response = gson.fromJson(json, TriageResponse.class);

        assertEquals("emergency", response.getTriage().getUrgency());
        assertEquals(1.0, response.getConfidenceScore(), 0.01);
    }
}
