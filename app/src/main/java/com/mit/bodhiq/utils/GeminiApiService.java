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
    private final GenerativeModelFutures triageModelFutures;
    private final Executor executor;
    private final EmpathicResponseBuilder responseBuilder;
    private final com.google.gson.Gson gson = new com.google.gson.Gson();

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    // Model configuration constants
    private static final String GEMINI_MODEL = "gemini-2.5-pro";
    private static final int MAX_OUTPUT_TOKENS = 65536;
    private static final double TEMPERATURE = 0.7; // Slightly lower for medical accuracy
    private static final double TRIAGE_TEMPERATURE = 0.0; // Deterministic for triage

    // Rate limiting
    private static final long RATE_LIMIT_WINDOW_MS = 3600000; // 1 hour
    private static final int MAX_REQUESTS_PER_WINDOW = 20;
    private long rateLimitWindowStart = 0;
    private int requestCount = 0;

    // Use centralized prompt templates with few-shot examples
    private static final String TRIAGE_SYSTEM_PROMPT = GeminiTriagePrompts.TRIAGE_SYSTEM_PROMPT;

    // Base medical system prompt for report analysis
    private static final String MEDICAL_SYSTEM_PROMPT = "You are BodhIQ, a compassionate medical assistant designed to provide supportive, "
            +
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
        
        // Validate API key
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            Log.e(TAG, "GEMINI_API_KEY missing or invalid");
            throw new IllegalStateException("GEMINI_API_KEY missing - check strings.xml");
        }
        
        Log.i(TAG, "Initializing GeminiApiService with model: " + GEMINI_MODEL);
        Log.i(TAG, "API key loaded: " + (apiKey.length() > 10 ? "***" + apiKey.substring(apiKey.length() - 4) : "***"));

        GenerativeModel gm = new GenerativeModel(
                GEMINI_MODEL, // Using Gemini 2.5 Pro for enhanced medical reasoning
                apiKey);

        this.model = GenerativeModelFutures.from(gm);

        // Initialize triage model with low temperature for deterministic output
        GenerativeModel triageGm = new GenerativeModel(
                GEMINI_MODEL,
                apiKey);
        this.triageModelFutures = GenerativeModelFutures.from(triageGm);

        this.executor = Executors.newSingleThreadExecutor();
        this.responseBuilder = new EmpathicResponseBuilder(context);
        
        Log.i(TAG, "GeminiApiService initialized successfully");
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
                "Please respond with empathy, reassurance, and appropriate medical guidance based on the detected severity level ("
                + detectedSeverity.name() + "):";

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
                        ChatMessage chatMessage = createEmpathicChatMessage(responseText, userMessage,
                                detectedSeverity);
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
    private ChatMessage createEmpathicChatMessage(String responseText, String originalQuery,
            ChatMessage.Severity severity) {
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
     * Triage symptoms with structured output, retries, rate limiting, and context
     */
    public void triageSymptoms(String userMessage, String patientContext, String recentMessages, TriageCallback callback) {
        long requestId = System.currentTimeMillis();
        Log.i(TAG, "=== TRIAGE REQUEST " + requestId + " ===");
        Log.i(TAG, "Input (redacted): " + (userMessage != null ? userMessage.substring(0, Math.min(50, userMessage.length())) + "..." : "null"));
        
        if (!checkRateLimit()) {
            Log.w(TAG, "Rate limit exceeded for request " + requestId);
            callback.onError("Rate limit exceeded. Please try again later.");
            return;
        }

        // Normalize symptoms first
        String normalizedMessage = SymptomNormalizer.normalize(userMessage);
        Log.d(TAG, "Normalized input: " + (normalizedMessage != null ? normalizedMessage.substring(0, Math.min(50, normalizedMessage.length())) + "..." : "null"));
        
        // Pre-check for red flags - bypass AI for immediate response
        if (RedFlagDetector.hasRedFlags(normalizedMessage)) {
            Log.w(TAG, "Red flag detected - returning immediate emergency response");
            com.mit.bodhiq.data.model.TriageResponse emergencyResponse = createEmergencyResponse(normalizedMessage);
            Log.i(TAG, "Emergency response created, urgency: emergency");
            callback.onSuccess(emergencyResponse);
            return;
        }

        // Create full prompt with context
        String fullMessage = GeminiTriagePrompts.createUserMessage(normalizedMessage, patientContext, recentMessages);
        String prompt = TRIAGE_SYSTEM_PROMPT + "\n" + fullMessage;
        Log.d(TAG, "Prompt length: " + prompt.length() + " characters");
        
        Content content = new Content.Builder().addText(prompt).build();
        Log.i(TAG, "Sending request to Gemini API...");

        generateTriageWithRetry(content, normalizedMessage, MAX_RETRIES, requestId, callback);
    }
    
    /**
     * Overload for backward compatibility
     */
    public void triageSymptoms(String userMessage, TriageCallback callback) {
        triageSymptoms(userMessage, null, null, callback);
    }

    private void generateTriageWithRetry(Content content, String originalInput, int retriesLeft, long requestId, TriageCallback callback) {
        Log.d(TAG, "Attempt " + (MAX_RETRIES - retriesLeft + 1) + "/" + MAX_RETRIES + " for request " + requestId);
        
        long apiCallStart = System.currentTimeMillis();
        ListenableFuture<GenerateContentResponse> response = triageModelFutures.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                long apiDuration = System.currentTimeMillis() - apiCallStart;
                Log.i(TAG, "API response received in " + apiDuration + "ms");
                
                try {
                    String responseText = result.getText();
                    Log.d(TAG, "Response text length: " + (responseText != null ? responseText.length() : 0) + " characters");
                    
                    if (responseText == null || responseText.trim().isEmpty()) {
                        Log.e(TAG, "Empty response from Gemini API");
                        handleError("Empty response from API", originalInput, retriesLeft, requestId, callback, content);
                        return;
                    }
                    
                    // Log first 200 chars of response (for debugging)
                    Log.d(TAG, "Response preview: " + responseText.substring(0, Math.min(200, responseText.length())) + "...");
                    
                    // Clean JSON response
                    String jsonStr = responseText.replace("```json", "").replace("```", "").trim();
                    
                    // Remove any text before first { or after last }
                    int firstBrace = jsonStr.indexOf('{');
                    int lastBrace = jsonStr.lastIndexOf('}');
                    if (firstBrace >= 0 && lastBrace > firstBrace) {
                        jsonStr = jsonStr.substring(firstBrace, lastBrace + 1);
                    } else {
                        Log.e(TAG, "No valid JSON found in response");
                        handleError("No valid JSON in response", originalInput, retriesLeft, requestId, callback, content);
                        return;
                    }
                    
                    Log.d(TAG, "Parsing JSON response...");
                    com.mit.bodhiq.data.model.TriageResponse triageResponse = gson.fromJson(jsonStr,
                            com.mit.bodhiq.data.model.TriageResponse.class);

                    // Validate response structure
                    if (triageResponse == null) {
                        Log.e(TAG, "Parsed response is null");
                        handleError("Failed to parse response", originalInput, retriesLeft, requestId, callback, content);
                        return;
                    }
                    
                    if (triageResponse.getTriage() == null || triageResponse.getTriage().getUrgency() == null) {
                        Log.e(TAG, "Malformed triage response - missing urgency field");
                        handleError("Malformed response - missing urgency", originalInput, retriesLeft, requestId, callback, content);
                        return;
                    }
                    
                    // Check confidence score - if too low, retry with stricter prompt
                    if (triageResponse.getConfidenceScore() < 0.6 && retriesLeft > 0) {
                        Log.w(TAG, "Low confidence score: " + triageResponse.getConfidenceScore() + ", retrying...");
                        String retryPrompt = GeminiTriagePrompts.createRetryPrompt(originalInput);
                        Content retryContent = new Content.Builder().addText(retryPrompt).build();
                        generateTriageWithRetry(retryContent, originalInput, retriesLeft - 1, requestId, callback);
                        return;
                    }

                    // Log metadata only (no PHI)
                    Log.i(TAG, "✅ Triage completed successfully");
                    Log.i(TAG, "Urgency: " + triageResponse.getTriage().getUrgency());
                    Log.i(TAG, "Confidence: " + triageResponse.getConfidenceScore());
                    Log.i(TAG, "Total duration: " + (System.currentTimeMillis() - (requestId)) + "ms");
                    
                    callback.onSuccess(triageResponse);
                } catch (com.google.gson.JsonSyntaxException e) {
                    Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
                    handleError("JSON parsing error: " + e.getMessage(), originalInput, retriesLeft, requestId, callback, content);
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error processing response", e);
                    handleError("Processing error: " + e.getMessage(), originalInput, retriesLeft, requestId, callback, content);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                long apiDuration = System.currentTimeMillis() - apiCallStart;
                Log.e(TAG, "❌ API call failed after " + apiDuration + "ms", t);
                Log.e(TAG, "Error type: " + t.getClass().getSimpleName());
                Log.e(TAG, "Error message: " + t.getMessage());
                
                handleError("API error: " + t.getMessage(), originalInput, retriesLeft, requestId, callback, content);
            }
        }, executor);
    }

    private void handleError(String errorMsg, String originalInput, int retriesLeft, long requestId, TriageCallback callback, Content content) {
        if (retriesLeft > 0) {
            long retryDelay = RETRY_DELAY_MS * (MAX_RETRIES - retriesLeft + 1);
            Log.w(TAG, "Retrying triage... " + retriesLeft + " attempts left. Error: " + errorMsg);
            Log.w(TAG, "Waiting " + retryDelay + "ms before retry...");
            
            try {
                Thread.sleep(retryDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Retry interrupted");
            }
            generateTriageWithRetry(content, originalInput, retriesLeft - 1, requestId, callback);
        } else {
            Log.e(TAG, "❌ Triage failed after all retries: " + errorMsg);
            Log.w(TAG, "Falling back to conservative triage response");
            
            // Fallback to conservative triage
            com.mit.bodhiq.data.model.TriageResponse fallbackResponse = createFallbackResponse(originalInput, errorMsg);
            Log.i(TAG, "Fallback response created with urgency: urgent");
            callback.onSuccess(fallbackResponse);
        }
    }
    
    /**
     * Create conservative fallback response when API fails
     */
    private com.mit.bodhiq.data.model.TriageResponse createFallbackResponse(String userMessage, String errorReason) {
        String json = "{\n" +
                "  \"extracted\": {\n" +
                "    \"presenting_symptoms\": [\"" + userMessage.replace("\"", "'").replace("\n", " ") + "\"],\n" +
                "    \"severity\": \"unknown\"\n" +
                "  },\n" +
                "  \"triage\": {\n" +
                "    \"urgency\": \"urgent\",\n" +
                "    \"reasons\": [\"Unable to complete automated triage due to technical error\", \"Conservative recommendation for safety\"]\n" +
                "  },\n" +
                "  \"suggestions\": {\n" +
                "    \"immediate_actions\": [\"Contact your healthcare provider\", \"Seek medical evaluation if symptoms worsen\"],\n" +
                "    \"red_flag_wording\": \"Automated triage unavailable - please consult a healthcare professional\",\n" +
                "    \"recommended_specialist\": \"Primary Care Physician or Urgent Care\"\n" +
                "  },\n" +
                "  \"confidence_score\": 0.0,\n" +
                "  \"disclaimer\": \"Automated triage failed. Please seek professional medical advice. Error: " + errorReason.replace("\"", "'") + "\"\n" +
                "}";

        return gson.fromJson(json, com.mit.bodhiq.data.model.TriageResponse.class);
    }

    private synchronized boolean checkRateLimit() {
        long now = System.currentTimeMillis();
        if (now - rateLimitWindowStart > RATE_LIMIT_WINDOW_MS) {
            rateLimitWindowStart = now;
            requestCount = 0;
        }

        if (requestCount < MAX_REQUESTS_PER_WINDOW) {
            requestCount++;
            return true;
        }
        return false;
    }

    private com.mit.bodhiq.data.model.TriageResponse createEmergencyResponse(String userMessage) {
        RedFlagDetector.RedFlagType redFlagType = RedFlagDetector.getRedFlagType(userMessage);
        
        String specificActions = getEmergencyActions(redFlagType);
        String specificWarning = getEmergencyWarning(redFlagType);
        
        String json = "{\n" +
                "  \"extracted\": {\n" +
                "    \"presenting_symptoms\": [\"" + userMessage.replace("\"", "'").replace("\n", " ") + "\"],\n" +
                "    \"severity\": \"CRITICAL\"\n" +
                "  },\n" +
                "  \"triage\": {\n" +
                "    \"urgency\": \"emergency\",\n" +
                "    \"reasons\": [\"Red-flag symptoms detected: " + redFlagType.name() + "\", \"Requires immediate emergency evaluation\"]\n" +
                "  },\n" +
                "  \"suggestions\": {\n" +
                "    \"immediate_actions\": " + specificActions + ",\n" +
                "    \"red_flag_wording\": \"" + specificWarning + "\",\n" +
                "    \"recommended_specialist\": \"Emergency Department - Call 911\"\n" +
                "  },\n" +
                "  \"confidence_score\": 1.0,\n" +
                "  \"disclaimer\": \"MEDICAL EMERGENCY - Call 911 immediately.\"\n" +
                "}";

        return gson.fromJson(json, com.mit.bodhiq.data.model.TriageResponse.class);
    }
    
    private String getEmergencyActions(RedFlagDetector.RedFlagType type) {
        switch (type) {
            case CHEST_PAIN:
                return "[\"Call 911 immediately\", \"Sit down and rest\", \"Chew aspirin if not allergic\", \"Do not drive yourself\"]";
            case BREATHING_DIFFICULTY:
                return "[\"Call 911 immediately\", \"Sit upright\", \"Loosen tight clothing\", \"Use inhaler if prescribed\"]";
            case SEVERE_BLEEDING:
                return "[\"Call 911 immediately\", \"Apply direct pressure to wound\", \"Elevate injured area if possible\", \"Do not remove embedded objects\"]";
            case LOSS_OF_CONSCIOUSNESS:
                return "[\"Call 911 immediately\", \"Check breathing and pulse\", \"Place in recovery position if breathing\", \"Start CPR if not breathing\"]";
            case STROKE_SIGNS:
                return "[\"Call 911 immediately\", \"Note time symptoms started\", \"Do not give food or drink\", \"Keep person calm and still\"]";
            case ANAPHYLAXIS:
                return "[\"Call 911 immediately\", \"Use EpiPen if available\", \"Lie down with legs elevated\", \"Monitor breathing\"]";
            default:
                return "[\"Call 911 immediately\", \"Do not drive yourself\", \"Stay calm\", \"Have someone stay with you\"]";
        }
    }
    
    private String getEmergencyWarning(RedFlagDetector.RedFlagType type) {
        switch (type) {
            case CHEST_PAIN:
                return "EMERGENCY: Potential cardiac event - immediate evaluation required";
            case BREATHING_DIFFICULTY:
                return "EMERGENCY: Severe respiratory distress - immediate intervention needed";
            case SEVERE_BLEEDING:
                return "EMERGENCY: Life-threatening hemorrhage - immediate medical attention required";
            case LOSS_OF_CONSCIOUSNESS:
                return "EMERGENCY: Loss of consciousness - critical medical emergency";
            case STROKE_SIGNS:
                return "EMERGENCY: Possible stroke - time-critical emergency";
            case ANAPHYLAXIS:
                return "EMERGENCY: Severe allergic reaction - life-threatening emergency";
            default:
                return "EMERGENCY: Medical emergency detected - immediate care required";
        }
    }

    /**
     * Callback interface for triage responses
     */
    public interface TriageCallback {
        void onSuccess(com.mit.bodhiq.data.model.TriageResponse response);

        void onError(String error);
    }

    /**
     * Callback interface for Gemini API responses
     */
    public interface GeminiCallback {
        void onSuccess(ChatMessage response);

        void onError(String error);
    }
}