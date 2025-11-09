package com.mit.bodhiq.utils;

import android.content.Context;
import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.mit.bodhiq.R;
import com.mit.bodhiq.data.model.ChatMessage;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Service class for interacting with Google's Gemini API
 * Provides medical chatbot functionality with specialized prompts
 */
@Singleton
public class GeminiApiService {
    
    private static final String TAG = "GeminiApiService";
    private final GenerativeModelFutures model;
    private final Executor executor;
    private final EmpathicResponseBuilder responseBuilder;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    // Model configuration constants
    private static final String GEMINI_MODEL = "gemini-2.5-pro";
    private static final int MAX_OUTPUT_TOKENS = 65536;
    private static final double TEMPERATURE = 0.7; // Slightly lower for medical accuracy
    
    // Base medical system prompt for report analysis
    private static final String MEDICAL_SYSTEM_PROMPT = 
        "You are BodhIQ, a compassionate medical assistant designed to provide supportive, " +
        "empathetic health guidance. Your role is to help users understand their health " +
        "information in a reassuring, educational manner while maintaining appropriate " +
        "medical boundaries.\n\n" +
        "CORE PRINCIPLES:\n" +
        "- Always respond with empathy and understanding\n" +
        "- Provide balanced, non-alarming information\n" +
        "- Focus on education and empowerment\n" +
        "- Encourage appropriate medical consultation when needed\n" +
        "- Never provide definitive diagnoses\n" +
        "- Maintain a warm, supportive tone\n\n";
    


    @Inject
    public GeminiApiService(Context context) {
        String apiKey = context.getString(R.string.gemini_api_key);
        
        GenerativeModel gm = new GenerativeModel(
            GEMINI_MODEL, // Using Gemini 2.5 Pro for enhanced medical reasoning
            apiKey
        );
        
        this.model = GenerativeModelFutures.from(gm);
        this.executor = Executors.newSingleThreadExecutor();
        this.responseBuilder = new EmpathicResponseBuilder(context);
    }

    /**
     * Generate an empathetic medical response using Gemini API
     */
    public void generateMedicalResponse(String userMessage, String userContext, GeminiCallback callback) {
        // Detect severity from user input
        ChatMessage.Severity detectedSeverity = EmpathicMedicalResponseGenerator.detectSeverityFromInput(userMessage);
        
        // Generate empathetic system prompt based on severity
        String systemPrompt = EmpathicMedicalResponseGenerator.generateEmpathicSystemPrompt(detectedSeverity);
        
        // Construct the full prompt with empathetic context
        String fullPrompt = systemPrompt + 
                           (userContext != null ? "User Context: " + userContext + "\n\n" : "") +
                           "User Message: " + userMessage + "\n\n" +
                           "Please respond with empathy, reassurance, and appropriate medical guidance based on the detected severity level (" + detectedSeverity.name() + "):";
        
        Content content = new Content.Builder()
                .addText(fullPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String responseText = result.getText();
                    if (responseText != null && !responseText.trim().isEmpty()) {
                        // Create ChatMessage from response with detected severity
                        ChatMessage chatMessage = createEmpathicChatMessage(responseText, userMessage, detectedSeverity);
                        callback.onSuccess(chatMessage);
                    } else {
                        callback.onError("Empty response from Gemini API");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing Gemini response", e);
                    callback.onError("Error processing response: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Gemini API call failed", t);
                callback.onError("Failed to get response: " + t.getMessage());
            }
        }, executor);
    }

    /**
     * Generate empathetic symptom analysis using Gemini API
     */
    public void analyzeSymptoms(String symptoms, String patientHistory, GeminiCallback callback) {
        // Detect severity from symptoms
        ChatMessage.Severity detectedSeverity = EmpathicMedicalResponseGenerator.detectSeverityFromInput(symptoms);
        
        // Create empathetic symptom analysis prompt
        String symptomPrompt = EmpathicMedicalResponseGenerator.createSymptomAnalysisPrompt(
            symptoms, patientHistory, detectedSeverity);

        Content content = new Content.Builder()
                .addText(symptomPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String responseText = result.getText();
                    if (responseText != null && !responseText.trim().isEmpty()) {
                        ChatMessage chatMessage = createEmpathicChatMessage(responseText, symptoms, detectedSeverity);
                        chatMessage.setRequiresFollowUp(shouldRequireFollowUp(responseText, detectedSeverity));
                        callback.onSuccess(chatMessage);
                    } else {
                        callback.onError("Empty response from Gemini API");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing symptom analysis", e);
                    callback.onError("Error analyzing symptoms: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Symptom analysis failed", t);
                callback.onError("Failed to analyze symptoms: " + t.getMessage());
            }
        }, executor);
    }

    /**
     * Analyze medical report data using Gemini API
     */
    public void analyzeMedicalReport(String reportData, String patientContext, GeminiCallback callback) {
        String reportPrompt = MEDICAL_SYSTEM_PROMPT +
                             "MEDICAL REPORT ANALYSIS:\n" +
                             "Patient Context: " + (patientContext != null ? patientContext : "Not provided") + "\n" +
                             "Report Data: " + reportData + "\n\n" +
                             "Please provide:\n" +
                             "1. Explanation of key findings in simple terms\n" +
                             "2. What values are normal vs. abnormal\n" +
                             "3. Potential implications (educational)\n" +
                             "4. Questions to ask your healthcare provider\n" +
                             "5. Follow-up recommendations\n\n" +
                             "Focus on patient education and empowerment.";

        Content content = new Content.Builder()
                .addText(reportPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String responseText = result.getText();
                    if (responseText != null && !responseText.trim().isEmpty()) {
                        ChatMessage chatMessage = createChatMessageFromResponse(responseText, reportData);
                        chatMessage.setType(ChatMessage.MessageType.AI_MEDICAL_CARD);
                        chatMessage.setRequiresFollowUp(true);
                        callback.onSuccess(chatMessage);
                    } else {
                        callback.onError("Empty response from Gemini API");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing report analysis", e);
                    callback.onError("Error analyzing report: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Report analysis failed", t);
                callback.onError("Failed to analyze report: " + t.getMessage());
            }
        }, executor);
    }

    /**
     * Create empathetic ChatMessage from Gemini response
     */
    private ChatMessage createEmpathicChatMessage(String responseText, String originalQuery, ChatMessage.Severity severity) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessage.MessageType.AI_RESPONSE);
        chatMessage.setFromUser(false);
        chatMessage.setContent(responseText);
        chatMessage.setTimestamp(System.currentTimeMillis());
        chatMessage.setSeverity(severity);
        
        // Add appropriate medical disclaimer based on severity
        String disclaimer = EmpathicMedicalResponseGenerator.generateMedicalDisclaimer(severity);
        chatMessage.setMedicalDisclaimer(disclaimer);
        
        return chatMessage;
    }
    
    /**
     * Legacy method for backward compatibility
     */
    private ChatMessage createChatMessageFromResponse(String responseText, String originalQuery) {
        ChatMessage.Severity detectedSeverity = EmpathicMedicalResponseGenerator.detectSeverityFromInput(originalQuery);
        return createEmpathicChatMessage(responseText, originalQuery, detectedSeverity);
    }

    /**
     * Determine severity based on response content
     */
    private ChatMessage.Severity determineSeverityFromResponse(String response) {
        String lowerResponse = response.toLowerCase();
        
        if (lowerResponse.contains("emergency") || 
            lowerResponse.contains("immediately") || 
            lowerResponse.contains("urgent") ||
            lowerResponse.contains("911") ||
            lowerResponse.contains("critical")) {
            return ChatMessage.Severity.CRITICAL;
        } else if (lowerResponse.contains("serious") || 
                   lowerResponse.contains("concerning") ||
                   lowerResponse.contains("see a doctor soon")) {
            return ChatMessage.Severity.HIGH;
        } else if (lowerResponse.contains("monitor") || 
                   lowerResponse.contains("watch for") ||
                   lowerResponse.contains("follow up")) {
            return ChatMessage.Severity.MEDIUM;
        } else {
            return ChatMessage.Severity.LOW;
        }
    }

    /**
     * Determine if follow-up is required based on response and severity
     */
    private boolean shouldRequireFollowUp(String response, ChatMessage.Severity severity) {
        String lowerResponse = response.toLowerCase();
        
        // Always require follow-up for high and critical severity
        if (severity == ChatMessage.Severity.HIGH || severity == ChatMessage.Severity.CRITICAL) {
            return true;
        }
        
        // Check response content for follow-up indicators
        return lowerResponse.contains("follow up") ||
               lowerResponse.contains("see a doctor") ||
               lowerResponse.contains("consult") ||
               lowerResponse.contains("medical attention") ||
               lowerResponse.contains("healthcare provider") ||
               lowerResponse.contains("monitor") ||
               lowerResponse.contains("if symptoms persist");
    }
    
    /**
     * Legacy method for backward compatibility
     */
    private boolean shouldRequireFollowUp(String response) {
        return shouldRequireFollowUp(response, ChatMessage.Severity.MEDIUM);
    }

    /**
     * Callback interface for Gemini API responses
     */
    public interface GeminiCallback {
        void onSuccess(ChatMessage response);
        void onError(String error);
    }
}