package com.mit.bodhiq.chatbot;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import com.mit.bodhiq.data.model.ChatMessage;
import com.mit.bodhiq.data.model.TriageResponse;
import com.mit.bodhiq.utils.GeminiApiService;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * End-to-end diagnostic test for chatbot functionality
 * Tests the complete pipeline from input to UI-ready output
 */
public class ChatbotEndToEndTest {
    
    private Context context;
    private GeminiApiService geminiService;
    private StringBuilder traceLog;
    private AtomicInteger requestCounter;
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        geminiService = new GeminiApiService(context);
        traceLog = new StringBuilder();
        requestCounter = new AtomicInteger(0);
        
        traceLog.append("=== CHATBOT END-TO-END DIAGNOSTIC TEST ===\n");
        traceLog.append("Timestamp: ").append(System.currentTimeMillis()).append("\n\n");
    }
    
    @Test
    public void testEmergencyInput_CantBreathe() throws Exception {
        String input = "I can't breathe";
        traceLog.append("TEST 1: Emergency Input\n");
        traceLog.append("Input: ").append(input).append("\n");
        
        TriageResponse response = executeTriageWithTrace(input, 1);
        
        assertNotNull("Response should not be null", response);
        assertNotNull("Triage should not be null", response.getTriage());
        assertEquals("Should be emergency", "emergency", response.getTriage().getUrgency());
        
        traceLog.append("✅ TEST 1 PASSED\n\n");
    }
    
    @Test
    public void testRoutineInput_MildSoreThroat() throws Exception {
        String input = "Mild sore throat, no fever";
        traceLog.append("TEST 2: Routine Input\n");
        traceLog.append("Input: ").append(input).append("\n");
        
        TriageResponse response = executeTriageWithTrace(input, 2);
        
        assertNotNull("Response should not be null", response);
        assertNotNull("Triage should not be null", response.getTriage());
        assertTrue("Should be routine or selfcare", 
                  response.getTriage().getUrgency().equals("routine") || 
                  response.getTriage().getUrgency().equals("selfcare"));
        
        traceLog.append("✅ TEST 2 PASSED\n\n");
    }
    
    @Test
    public void testUrgentInput_SevereChestPain() throws Exception {
        String input = "Severe chest pain and sweating";
        traceLog.append("TEST 3: Urgent/Emergency Input\n");
        traceLog.append("Input: ").append(input).append("\n");
        
        TriageResponse response = executeTriageWithTrace(input, 3);
        
        assertNotNull("Response should not be null", response);
        assertNotNull("Triage should not be null", response.getTriage());
        assertEquals("Should be emergency", "emergency", response.getTriage().getUrgency());
        
        traceLog.append("✅ TEST 3 PASSED\n\n");
    }
    
    private TriageResponse executeTriageWithTrace(String input, int testNumber) throws Exception {
        int requestId = requestCounter.incrementAndGet();
        
        traceLog.append("--- Request ").append(requestId).append(" ---\n");
        traceLog.append("Input: ").append(input).append("\n");
        traceLog.append("Timestamp: ").append(System.currentTimeMillis()).append("\n");
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<TriageResponse> responseRef = new AtomicReference<>();
        AtomicReference<String> errorRef = new AtomicReference<>();
        
        long startTime = System.currentTimeMillis();
        
        geminiService.triageSymptoms(input, new GeminiApiService.TriageCallback() {
            @Override
            public void onSuccess(TriageResponse response) {
                long duration = System.currentTimeMillis() - startTime;
                traceLog.append("✅ Success in ").append(duration).append("ms\n");
                traceLog.append("Response urgency: ").append(response.getTriage().getUrgency()).append("\n");
                traceLog.append("Confidence: ").append(response.getConfidenceScore()).append("\n");
                
                if (response.getSuggestions() != null && 
                    response.getSuggestions().getImmediateActions() != null) {
                    traceLog.append("Actions: ").append(response.getSuggestions().getImmediateActions().size()).append("\n");
                }
                
                responseRef.set(response);
                saveRequestTrace(testNumber, input, response, null, duration);
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                long duration = System.currentTimeMillis() - startTime;
                traceLog.append("❌ Error in ").append(duration).append("ms\n");
                traceLog.append("Error: ").append(error).append("\n");
                
                errorRef.set(error);
                saveRequestTrace(testNumber, input, null, error, duration);
                latch.countDown();
            }
        });
        
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        
        if (!completed) {
            traceLog.append("⏱️ TIMEOUT after 30 seconds\n");
            saveTraceLog();
            fail("Request timed out after 30 seconds");
        }
        
        if (errorRef.get() != null) {
            traceLog.append("Request failed with error: ").append(errorRef.get()).append("\n");
            saveTraceLog();
            fail("Request failed: " + errorRef.get());
        }
        
        saveTraceLog();
        return responseRef.get();
    }
    
    private void saveRequestTrace(int testNumber, String input, TriageResponse response, 
                                  String error, long duration) {
        try {
            File reportDir = new File("tests/patch_report");
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }
            
            File traceFile = new File(reportDir, "request_" + testNumber + ".json");
            FileWriter writer = new FileWriter(traceFile);
            
            writer.write("{\n");
            writer.write("  \"test_number\": " + testNumber + ",\n");
            writer.write("  \"input\": \"" + escapeJson(input) + "\",\n");
            writer.write("  \"duration_ms\": " + duration + ",\n");
            writer.write("  \"timestamp\": " + System.currentTimeMillis() + ",\n");
            
            if (response != null) {
                writer.write("  \"success\": true,\n");
                writer.write("  \"response\": {\n");
                writer.write("    \"urgency\": \"" + response.getTriage().getUrgency() + "\",\n");
                writer.write("    \"confidence\": " + response.getConfidenceScore() + ",\n");
                
                if (response.getExtracted() != null && 
                    response.getExtracted().getPresentingSymptoms() != null) {
                    writer.write("    \"symptoms\": [");
                    for (int i = 0; i < response.getExtracted().getPresentingSymptoms().size(); i++) {
                        if (i > 0) writer.write(", ");
                        writer.write("\"" + escapeJson(response.getExtracted().getPresentingSymptoms().get(i)) + "\"");
                    }
                    writer.write("],\n");
                }
                
                if (response.getSuggestions() != null && 
                    response.getSuggestions().getImmediateActions() != null) {
                    writer.write("    \"actions_count\": " + response.getSuggestions().getImmediateActions().size() + "\n");
                }
                
                writer.write("  }\n");
            } else {
                writer.write("  \"success\": false,\n");
                writer.write("  \"error\": \"" + escapeJson(error) + "\"\n");
            }
            
            writer.write("}\n");
            writer.close();
            
        } catch (IOException e) {
            System.err.println("Failed to save trace: " + e.getMessage());
        }
    }
    
    private void saveTraceLog() {
        try {
            File reportDir = new File("tests/patch_report");
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }
            
            File logFile = new File(reportDir, "trace_log.txt");
            FileWriter writer = new FileWriter(logFile);
            writer.write(traceLog.toString());
            writer.close();
            
        } catch (IOException e) {
            System.err.println("Failed to save trace log: " + e.getMessage());
        }
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
