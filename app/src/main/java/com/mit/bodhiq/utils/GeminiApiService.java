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
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    // Model configuration constants
    private static final String GEMINI_MODEL = "gemini-2.5-pro";
    private static final int MAX_OUTPUT_TOKENS = 65536;
    private static final double TEMPERATURE = 0.7; // Slightly lower for medical accuracy
    
    // Enhanced medical prompt template for Gemini 2.5 Pro
    private static final String MEDICAL_SYSTEM_PROMPT = 
        "You are BodhIQ Medical Assistant, powered by Google's advanced Gemini 2.5 Pro AI. " +
        "Your mission is to provide accurate, empathetic, and actionable health information to empower patients. " +
        "\n\n🎯 **CORE RESPONSIBILITIES:**\n" +
        "• Provide evidence-based health education and guidance\n" +
        "• Analyze symptoms and suggest appropriate care levels\n" +
        "• Explain medical reports and test results in simple terms\n" +
        "• Offer wellness recommendations and preventive care advice\n" +
        "• Guide users on when to seek professional medical care\n\n" +
        "⚠️ **CRITICAL SAFETY GUIDELINES:**\n" +
        "1. **Medical Disclaimer**: Always emphasize this is educational information only\n" +
        "2. **Professional Consultation**: Encourage consulting healthcare providers for medical decisions\n" +
        "3. **Emergency Recognition**: If symptoms suggest emergency, advise immediate medical attention\n" +
        "4. **Medication Safety**: Never provide specific dosing; refer to healthcare professionals\n" +
        "5. **Diagnostic Limitations**: Clarify that AI cannot replace professional diagnosis\n\n" +
        "📝 **RESPONSE FORMAT:**\n" +
        "• Use clear headings with relevant emojis (🩺 💊 🏥 ⚠️ 📋)\n" +
        "• Structure with bullet points for easy scanning\n" +
        "• Include severity indicators when appropriate\n" +
        "• Provide actionable next steps\n" +
        "• End with appropriate medical disclaimer\n\n" +
        "💡 **COMMUNICATION STYLE:**\n" +
        "• Be empathetic and supportive\n" +
        "• Use patient-friendly language\n" +
        "• Acknowledge concerns and validate feelings\n" +
        "• Provide hope while being realistic\n" +
        "• Include relevant health tips and prevention advice\n\n";

    @Inject
    public GeminiApiService(Context context) {
        String apiKey = context.getString(R.string.gemini_api_key);
        
        GenerativeModel gm = new GenerativeModel(
            GEMINI_MODEL, // Using Gemini 2.5 Pro for enhanced medical reasoning
            apiKey
        );
        
        this.model = GenerativeModelFutures.from(gm);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Generate a medical response using Gemini API
     */
    public void generateMedicalResponse(String userMessage, String userContext, GeminiCallback callback) {
        // Construct the full prompt with medical context
        String fullPrompt = MEDICAL_SYSTEM_PROMPT + 
                           (userContext != null ? "User Context: " + userContext + "\n\n" : "") +
                           "User Question: " + userMessage + "\n\n" +
                           "Please provide a helpful, medically accurate response:";
        
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
                        // Create ChatMessage from response
                        ChatMessage chatMessage = createChatMessageFromResponse(responseText, userMessage);
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
     * Generate symptom analysis using Gemini API
     */
    public void analyzeSymptoms(String symptoms, String patientHistory, GeminiCallback callback) {
        String symptomPrompt = MEDICAL_SYSTEM_PROMPT +
                              "SYMPTOM ANALYSIS REQUEST:\n" +
                              "Patient History: " + (patientHistory != null ? patientHistory : "Not provided") + "\n" +
                              "Current Symptoms: " + symptoms + "\n\n" +
                              "Please provide:\n" +
                              "1. Possible causes (educational purposes)\n" +
                              "2. When to seek medical attention\n" +
                              "3. General self-care recommendations\n" +
                              "4. Red flag symptoms to watch for\n\n" +
                              "Remember to emphasize consulting healthcare professionals.";

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
                        ChatMessage chatMessage = createChatMessageFromResponse(responseText, symptoms);
                        // Set higher severity for symptom analysis
                        chatMessage.setSeverity(determineSeverityFromResponse(responseText));
                        chatMessage.setRequiresFollowUp(shouldRequireFollowUp(responseText));
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
     * Create ChatMessage from Gemini response
     */
    private ChatMessage createChatMessageFromResponse(String responseText, String originalQuery) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessage.MessageType.AI_RESPONSE);
        chatMessage.setFromUser(false);
        chatMessage.setContent(responseText);
        chatMessage.setTimestamp(System.currentTimeMillis());
        
        // Add medical disclaimer if not already present
        if (!responseText.toLowerCase().contains("disclaimer") && 
            !responseText.toLowerCase().contains("consult") &&
            !responseText.toLowerCase().contains("healthcare provider")) {
            chatMessage.setMedicalDisclaimer(
                "⚠️ This information is for educational purposes only. Always consult your healthcare provider for medical advice."
            );
        }
        
        return chatMessage;
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
     * Determine if follow-up is required based on response
     */
    private boolean shouldRequireFollowUp(String response) {
        String lowerResponse = response.toLowerCase();
        return lowerResponse.contains("follow up") ||
               lowerResponse.contains("see a doctor") ||
               lowerResponse.contains("consult") ||
               lowerResponse.contains("medical attention") ||
               lowerResponse.contains("healthcare provider");
    }

    /**
     * Callback interface for Gemini API responses
     */
    public interface GeminiCallback {
        void onSuccess(ChatMessage response);
        void onError(String error);
    }
}