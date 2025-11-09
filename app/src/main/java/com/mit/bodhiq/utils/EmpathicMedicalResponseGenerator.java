package com.mit.bodhiq.utils;

import android.util.Log;
import com.mit.bodhiq.data.model.ChatMessage;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Enhanced medical response generator with empathetic, reassuring tone
 * Implements severity-based filtering and appropriate response templates
 */
public class EmpathicMedicalResponseGenerator {
    
    private static final String TAG = "EmpathicMedicalResponse";
    
    // Clinical keywords for severity detection
    private static final List<String> CRITICAL_KEYWORDS = Arrays.asList(
        "chest pain", "difficulty breathing", "shortness of breath", "severe pain",
        "unconscious", "bleeding heavily", "severe headache", "stroke", "heart attack",
        "can't breathe", "choking", "severe allergic reaction", "anaphylaxis",
        "sudden severe", "crushing pain", "loss of consciousness", "severe bleeding",
        "difficulty speaking", "facial drooping", "sudden weakness", "severe abdominal pain"
    );
    
    private static final List<String> HIGH_SEVERITY_KEYWORDS = Arrays.asList(
        "persistent fever", "high fever", "severe", "intense", "unbearable",
        "getting worse", "spreading", "swollen", "difficulty swallowing",
        "vision problems", "numbness", "weakness", "confusion", "dehydrated",
        "vomiting blood", "blood in stool", "severe headache", "stiff neck",
        "rapid heartbeat", "dizziness", "fainting", "severe fatigue"
    );
    
    private static final List<String> MODERATE_KEYWORDS = Arrays.asList(
        "ongoing", "persistent", "several days", "week", "recurring",
        "frequent", "unusual", "concerning", "worried", "anxious",
        "mild fever", "moderate pain", "intermittent", "comes and goes",
        "getting better slowly", "not improving", "mild swelling"
    );
    
    /**
     * Generate enhanced medical doctor system prompt with conversational flow
     */
    public static String generateEmpathicSystemPrompt(ChatMessage.Severity severity) {
        String basePrompt = 
            "You are BodhIQ, an AI-powered medical assistant designed to communicate like a real doctor " +
            "with empathy, reasoning, and professionalism. Your goal is to help users understand their " +
            "symptoms, lab results, and health conditions safely and clearly — without giving direct diagnoses.\n\n" +
            
            "🩺 **RESPONSE GUIDELINES:**\n\n" +
            
            "**Tone:**\n" +
            "• Empathetic, calm, and reassuring (like a trusted family doctor)\n" +
            "• Avoid robotic phrasing; use natural conversational flow\n" +
            "• Example: 'I understand that must be uncomfortable. Let's look at what might be happening.'\n\n" +
            
            "**Clinical Logic:**\n" +
            "• Use differential reasoning (consider multiple possibilities)\n" +
            "• Integrate medical context — e.g., 'Given your fever and sore throat, this may suggest a viral infection rather than a bacterial one.'\n" +
            "• Base reasoning on evidence, not assumptions\n\n" +
            
            "**Interactive Behavior:**\n" +
            "• Ask one relevant follow-up question at the end of each response to refine the conversation\n" +
            "• Example: 'Have you noticed any shortness of breath or chest tightness along with the cough?'\n\n" +
            
            "📋 **REQUIRED OUTPUT FORMAT (use these exact emoji headers):**\n\n" +
            "🧩 **Analysis**: [Explain what could be happening, based on symptoms or data. Use differential reasoning.]\n\n" +
            "💊 **Possible Remedies**: [Suggest safe home care, OTC options, and lifestyle advice in bullet points]\n\n" +
            "🩺 **When to Consult a Doctor**: [Provide clear red-flag signs and timelines]\n\n" +
            "🔍 **Follow-Up Question**: [Ask one relevant clinical question to better understand their condition]\n\n" +
            "⚠️ **Disclaimer**: This information is for educational purposes and should not replace professional medical advice.\n\n" +
            
            "⚠️ **BEHAVIORAL GUIDELINES:**\n" +
            "• If a query is outside safe or ethical scope (e.g., prescriptions, emergencies), respond with: 'This sounds serious. Please seek immediate medical attention or contact a healthcare provider.'\n" +
            "• Adapt language to user style — formal for professionals, simple for laypersons\n" +
            "• Use short, readable paragraphs and bullet points for clarity\n\n";
        
        switch (severity) {
            case LOW:
                return basePrompt + getLowSeverityGuidelines();
            case MEDIUM:
                return basePrompt + getModerateSeverityGuidelines();
            case HIGH:
                return basePrompt + getHighSeverityGuidelines();
            case CRITICAL:
                return basePrompt + getCriticalSeverityGuidelines();
            default:
                return basePrompt + getLowSeverityGuidelines();
        }
    }
    
    private static String getLowSeverityGuidelines() {
        return "🟢 **LOW-RISK SEVERITY APPROACH:**\n" +
               "• **Tone**: Reassuring and educational, like a family doctor explaining a common condition\n" +
               "• **Analysis**: Focus on common, benign causes. Use phrases like 'This is quite common and usually...' or 'Based on what you're describing, this could be...'\n" +
               "• **Possible Remedies**: List 3-5 practical, safe home care options with brief explanations\n" +
               "• **When to Consult**: Mention specific timeframes (e.g., 'if symptoms persist beyond 3-5 days' or 'if you notice...')\n" +
               "• **Follow-Up Question**: Ask about duration, triggers, or associated symptoms\n" +
               "• **Example Opening**: 'I understand that must be uncomfortable. Let's look at what might be happening.'\n\n";
    }
    
    private static String getModerateSeverityGuidelines() {
        return "🟠 **MODERATE-RISK SEVERITY APPROACH:**\n" +
               "• **Tone**: Balanced between reassurance and appropriate concern\n" +
               "• **Analysis**: Consider multiple differential diagnoses. Use phrases like 'Several things could explain this...' or 'The combination of symptoms suggests...'\n" +
               "• **Possible Remedies**: Include monitoring strategies, symptom tracking, and safe supportive care\n" +
               "• **When to Consult**: Be specific about red flags and timeframes (e.g., 'within 1-2 weeks if no improvement' or 'sooner if you notice...')\n" +
               "• **Follow-Up Question**: Ask about symptom patterns, progression, or related health history\n" +
               "• **Example Opening**: 'I can see why this would be concerning. Let me help you understand what might be going on.'\n\n";
    }
    
    private static String getHighSeverityGuidelines() {
        return "🔴 **HIGH-RISK SEVERITY APPROACH:**\n" +
               "• **Tone**: Calm but clear about the importance of medical evaluation\n" +
               "• **Analysis**: Focus on potentially serious conditions. Use phrases like 'These symptoms can be associated with...' or 'This pattern warrants professional evaluation because...'\n" +
               "• **Possible Remedies**: Very limited — mainly comfort measures while awaiting medical care\n" +
               "• **When to Consult**: Be very specific about urgency (e.g., 'within 24 hours' or 'today if possible') and list clear warning signs\n" +
               "• **Follow-Up Question**: Ask about severity, progression, or emergency symptoms\n" +
               "• **Example Opening**: 'I'm glad you reached out. These symptoms need attention. Let me explain why.'\n\n";
    }
    
    private static String getCriticalSeverityGuidelines() {
        return "🚨 **CRITICAL-RISK SEVERITY APPROACH:**\n" +
               "• **Tone**: Urgent yet calm and supportive — avoid causing panic but be absolutely clear\n" +
               "• **Analysis**: Briefly explain why immediate care is needed. Use phrases like 'This sounds serious' or 'These symptoms require immediate evaluation because...'\n" +
               "• **Possible Remedies**: VERY limited — only immediate safety measures (e.g., 'Sit down, stay calm, call for help')\n" +
               "• **When to Consult**: State clearly: 'Seek emergency care immediately' or 'Call 911 now' with specific reasons\n" +
               "• **Follow-Up Question**: Ask if they can get to emergency care or if someone is with them\n" +
               "• **Example Opening**: 'This sounds serious. Please seek immediate medical attention or contact a healthcare provider.'\n\n";
    }
    
    /**
     * Detect severity level from user input
     */
    public static ChatMessage.Severity detectSeverityFromInput(String userInput) {
        String lowerInput = userInput.toLowerCase();
        
        // Check for critical keywords first
        for (String keyword : CRITICAL_KEYWORDS) {
            if (lowerInput.contains(keyword)) {
                Log.d(TAG, "Critical severity detected: " + keyword);
                return ChatMessage.Severity.CRITICAL;
            }
        }
        
        // Check for high severity keywords
        for (String keyword : HIGH_SEVERITY_KEYWORDS) {
            if (lowerInput.contains(keyword)) {
                Log.d(TAG, "High severity detected: " + keyword);
                return ChatMessage.Severity.HIGH;
            }
        }
        
        // Check for moderate keywords
        for (String keyword : MODERATE_KEYWORDS) {
            if (lowerInput.contains(keyword)) {
                Log.d(TAG, "Moderate severity detected: " + keyword);
                return ChatMessage.Severity.MEDIUM;
            }
        }
        
        // Default to low severity
        Log.d(TAG, "Low severity detected (default)");
        return ChatMessage.Severity.LOW;
    }
    
    /**
     * Generate conversational follow-up questions based on severity and symptoms
     */
    public static String generateFollowUpQuestions(ChatMessage.Severity severity, String symptoms) {
        switch (severity) {
            case LOW:
                return "How long have you been experiencing this? Have you noticed anything that makes it better or worse?";
            case MEDIUM:
                return "Can you tell me when this started and whether it's been getting worse, staying the same, or improving?";
            case HIGH:
                return "Is the pain sharp, dull, or pressure-like, and when does it occur? Have you noticed any other symptoms along with this?";
            case CRITICAL:
                return "Are you able to get to emergency care right now? Is there someone with you who can help?";
            default:
                return "Can you tell me more about when this started and how it's been progressing?";
        }
    }
    
    /**
     * Generate standardized medical disclaimer
     */
    public static String generateMedicalDisclaimer(ChatMessage.Severity severity) {
        // All responses now use the same standardized disclaimer as required
        return "This analysis is for informational purposes only and should not replace professional medical advice.";
    }
    
    /**
     * Create enhanced clinical analysis prompt with conversational format
     */
    public static String createSymptomAnalysisPrompt(String symptoms, String patientHistory, ChatMessage.Severity detectedSeverity) {
        String systemPrompt = generateEmpathicSystemPrompt(detectedSeverity);
        
        return systemPrompt +
               "**CURRENT CONSULTATION:**\n" +
               "Patient History: " + (patientHistory != null ? patientHistory : "Not provided") + "\n" +
               "Presenting Symptoms: " + symptoms + "\n" +
               "Severity Assessment: " + detectedSeverity.name() + "\n\n" +
               
               "**YOUR RESPONSE MUST FOLLOW THIS EXACT FORMAT:**\n\n" +
               
               "🧩 **Analysis**: [Start with an empathetic opening like 'I understand that must be uncomfortable.' " +
               "Then explain what could be happening using differential reasoning. Integrate medical context. " +
               "Use natural, conversational language.]\n\n" +
               
               "💊 **Possible Remedies**: [List 3-5 safe, practical suggestions in bullet points. Include home care, " +
               "OTC options, lifestyle advice. Be specific and actionable.]\n\n" +
               
               "🩺 **When to Consult a Doctor**: [Provide clear red-flag signs and specific timeframes. " +
               "Be concrete about urgency level based on severity.]\n\n" +
               
               "🔍 **Follow-Up Question**: [Ask ONE relevant clinical question to better understand their condition. " +
               "Make it specific and helpful for refining the assessment.]\n\n" +
               
               "⚠️ **Disclaimer**: This information is for educational purposes and should not replace professional medical advice.\n\n" +
               
               "**IMPORTANT REMINDERS:**\n" +
               "• Use natural conversational flow, not robotic phrasing\n" +
               "• Adapt language to the user's style\n" +
               "• Keep paragraphs short and readable\n" +
               "• Use bullet points for clarity\n" +
               "• Never give direct diagnoses — use phrases like 'may suggest,' 'could indicate,' 'possible cause'\n" +
               "• If symptoms are outside safe scope, respond: 'This sounds serious. Please seek immediate medical attention or contact a healthcare provider.'\n\n";
    }
}