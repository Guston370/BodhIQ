package com.mit.bodhiq;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mit.bodhiq.data.model.TriageResponse;
import com.mit.bodhiq.utils.GeminiApiService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Comprehensive diagnostic test for Gemini chatbot
 * Captures full request/response traces for analysis
 */
@RunWith(AndroidJUnit4.class)
public class GeminiDiagnosticTest {
    
    private Context context;
    private GeminiApiService geminiService;
    private Gson gson;
    private AtomicInteger testCounter;
    private File reportDir;
    
    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        geminiService = new GeminiApiService(context);
        gson = new GsonBuilder().setPrettyPrinting().create();
        testCounter = new AtomicInteger(0);
        
        // Create report directory
        reportDir = new File(context.getExternalFilesDir(null), "patch_report");
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }
    }
    
    @Test
    public void test1_EmergencyInput_CantBreathe() throws Exception {
        String input = "I can't breathe";
        runDiagnosticTest(1, input, "emergency");
    }
    
    @Test
    public void test2_RoutineInput_MildSoreThroat() throws Exception {
        String input = "Mild sore throat, no fever";
        runDiagnosticTest(2, input, "routine");
    }
    
    @Test
    public void test3_UrgentInput_SevereChestPain() throws Exception {
        String input = "Severe chest pain and sweating";
        runDiagnosticTest(3, input, "emergency");
    }
    
    private void runDiagnosticTest(int testNum, String input, String expectedUrgency) throws Exception {
        System.out.println("\n=== TEST " + testNum + " ===");
        System.out.println("Input: " + input);
        System.out.println("Expected urgency: " + expectedUrgency);
        
        long startTime = System.currentTimeMillis();
        
        // Save request
        saveRequest(testNum, input);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<TriageResponse> responseRef = new AtomicReference<>();
        AtomicReference<String> errorRef = new AtomicReference<>();
        
        geminiService.triageSymptoms(input, new GeminiApiService.TriageCallback() {
            @Override
            public void onSuccess(TriageResponse response) {
                long duration = System.currentTimeMillis() - startTime;
                System.out.println("✅ Success in " + duration + "ms");
                System.out.println("Urgency: " + response.getTriage().getUrgency());
                System.out.println("Confidence: " + response.getConfidenceScore());
                
                responseRef.set(response);
                saveResponse(testNum, response, null, duration);
                latch.countDown();
            }
            
            @Override
            public void onError(String error) {
                long duration = System.currentTimeMillis() - startTime;
                System.out.println("❌ Error in " + duration + "ms");
                System.out.println("Error: " + error);
                
                errorRef.set(error);
                saveResponse(testNum, null, error, duration);
                latch.countDown();
            }
        });
        
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        
        if (!completed) {
            System.out.println("⏱️ TIMEOUT after 30 seconds");
            saveTimeout(testNum);
            throw new AssertionError("Request timed out after 30 seconds");
        }
        
        if (errorRef.get() != null) {
            throw new AssertionError("Request failed: " + errorRef.get());
        }
        
        TriageResponse response = responseRef.get();
        if (response == null || response.getTriage() == null) {
            throw new AssertionError("Response or triage is null");
        }
        
        System.out.println("✅ TEST " + testNum + " PASSED");
    }
    
    private void saveRequest(int testNum, String input) {
        try {
            File requestFile = new File(reportDir, "request_" + testNum + ".json");
            FileWriter writer = new FileWriter(requestFile);
            
            writer.write("{\n");
            writer.write("  \"test_number\": " + testNum + ",\n");
            writer.write("  \"timestamp\": " + System.currentTimeMillis() + ",\n");
            writer.write("  \"input\": \"" + escapeJson(input) + "\",\n");
            writer.write("  \"input_length\": " + input.length() + "\n");
            writer.write("}\n");
            
            writer.close();
            System.out.println("Saved request to: " + requestFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save request: " + e.getMessage());
        }
    }
    
    private void saveResponse(int testNum, TriageResponse response, String error, long duration) {
        try {
            File responseFile = new File(reportDir, "response_" + testNum + ".json");
            FileWriter writer = new FileWriter(responseFile);
            
            if (response != null) {
                String json = gson.toJson(response);
                writer.write("{\n");
                writer.write("  \"success\": true,\n");
                writer.write("  \"duration_ms\": " + duration + ",\n");
                writer.write("  \"response\": " + json + "\n");
                writer.write("}\n");
            } else {
                writer.write("{\n");
                writer.write("  \"success\": false,\n");
                writer.write("  \"duration_ms\": " + duration + ",\n");
                writer.write("  \"error\": \"" + escapeJson(error) + "\"\n");
                writer.write("}\n");
            }
            
            writer.close();
            System.out.println("Saved response to: " + responseFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save response: " + e.getMessage());
        }
    }
    
    private void saveTimeout(int testNum) {
        try {
            File timeoutFile = new File(reportDir, "timeout_" + testNum + ".txt");
            FileWriter writer = new FileWriter(timeoutFile);
            writer.write("Test " + testNum + " timed out after 30 seconds\n");
            writer.write("Timestamp: " + System.currentTimeMillis() + "\n");
            writer.close();
        } catch (IOException e) {
            System.err.println("Failed to save timeout: " + e.getMessage());
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
