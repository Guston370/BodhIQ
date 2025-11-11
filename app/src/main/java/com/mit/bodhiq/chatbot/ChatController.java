package com.mit.bodhiq.chatbot;

import android.content.Context;
import android.util.Log;

import com.mit.bodhiq.data.model.ChatMessage;
import com.mit.bodhiq.data.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Main controller for chatbot logic and conversation flow
 */
public class ChatController {
    
    private static final String TAG = "ChatController";
    
    private final Context context;
    private final DocumentAnalyzer documentAnalyzer;
    private final ActionHandler actionHandler;
    private final EmergencyDetector emergencyDetector;
    private final ConversationContext conversationContext;
    
    private UserProfile userProfile;
    private String lastReportText;
    
    public ChatController(Context context) {
        this.context = context;
        this.documentAnalyzer = new DocumentAnalyzer();
        this.actionHandler = new ActionHandler(context);
        this.emergencyDetector = new EmergencyDetector();
        this.conversationContext = new ConversationContext();
    }
    
    /**
     * Set user profile for personalized responses
     */
    public void setUserProfile(UserProfile profile) {
        this.userProfile = profile;
        conversationContext.setUserProfile(profile);
    }
    
    /**
     * Set extracted report text for analysis
     */
    public void setReportText(String reportText) {
        this.lastReportText = reportText;
        conversationContext.setLastReport(reportText);
    }
    
    /**
     * Process user message and generate response
     */
    public ChatMessage processMessage(String userMessage) {
        Log.d(TAG, "Processing message: " + userMessage);
        
        // Update conversation context
        conversationContext.addUserMessage(userMessage);
        
        // Check for emergency keywords first
        if (emergencyDetector.detectEmergency(userMessage)) {
            return handleEmergency(userMessage);
        }
        
        // Detect intent
        Intent intent = detectIntent(userMessage);
        
        // Route to appropriate handler
        ChatMessage response;
        switch (intent) {
            case ANALYZE_REPORT:
                response = handleReportAnalysis(userMessage);
                break;
            case CREATE_REMINDER:
                response = handleReminderCreation(userMessage);
                break;
            case LIST_REMINDERS:
                response = handleReminderList();
                break;
            case EMERGENCY_QR:
                response = handleEmergencyQR();
                break;
            case SYMPTOM_CHECK:
                response = handleSymptomCheck(userMessage);
                break;
            case MEDICATION_INFO:
                response = handleMedicationInfo(userMessage);
                break;
            case HEALTH_ADVICE:
                response = handleHealthAdvice(userMessage);
                break;
            default:
                response = handleGeneralQuery(userMessage);
                break;
        }
        
        // Add context to response
        conversationContext.addBotMessage(response.getContent());
        
        return response;
    }
    
    /**
     * Detect user intent from message
     */
    private Intent detectIntent(String message) {
        String lower = message.toLowerCase();
        
        // Report analysis
        if (lower.contains("analyze") || lower.contains("report") || 
            lower.contains("test result") || lower.contains("lab result")) {
            return Intent.ANALYZE_REPORT;
        }
        
        // Reminder management
        if (lower.contains("remind") || lower.contains("reminder") || 
            lower.contains("medicine") || lower.contains("medication")) {
            if (lower.contains("create") || lower.contains("add") || lower.contains("set")) {
                return Intent.CREATE_REMINDER;
            } else if (lower.contains("list") || lower.contains("show") || lower.contains("my")) {
                return Intent.LIST_REMINDERS;
            }
        }
        
        // Emergency QR
        if (lower.contains("emergency") || lower.contains("qr code") || 
            lower.contains("emergency qr")) {
            return Intent.EMERGENCY_QR;
        }
        
        // Symptom check
        if (lower.contains("symptom") || lower.contains("feel") || 
            lower.contains("pain") || lower.contains("ache") || 
            lower.contains("sick") || lower.contains("hurt")) {
            return Intent.SYMPTOM_CHECK;
        }
        
        // Medication info
        if (lower.contains("drug") || lower.contains("pill") || 
            lower.contains("tablet") || lower.contains("dose")) {
            return Intent.MEDICATION_INFO;
        }
        
        // Health advice
        if (lower.contains("advice") || lower.contains("suggest") || 
            lower.contains("recommend") || lower.contains("should i")) {
            return Intent.HEALTH_ADVICE;
        }
        
        return Intent.GENERAL_QUERY;
    }
    
    private ChatMessage handleReportAnalysis(String userMessage) {
        if (lastReportText == null || lastReportText.isEmpty()) {
            return createResponse(
                "I don't have any report to analyze. Please scan or upload a medical report first from the Reports tab.",
                ChatMessage.MessageType.AI_RESPONSE,
                ChatMessage.Severity.LOW
            );
        }
        
        // Analyze the report
        DocumentAnalyzer.ReportAnalysis analysis = documentAnalyzer.analyzeReport(lastReportText);
        
        StringBuilder response = new StringBuilder();
        response.append("📋 **Report Analysis**\n\n");
        
        // Summary
        response.append("**Summary:**\n");
        response.append(analysis.getSummary()).append("\n\n");
        
        // Key findings
        if (!analysis.getKeyFindings().isEmpty()) {
            response.append("**Key Findings:**\n");
            for (String finding : analysis.getKeyFindings()) {
                response.append("• ").append(finding).append("\n");
            }
            response.append("\n");
        }
        
        // Detected values
        if (!analysis.getDetectedValues().isEmpty()) {
            response.append("**Detected Values:**\n");
            for (DocumentAnalyzer.DetectedValue value : analysis.getDetectedValues()) {
                response.append("• ").append(value.getParameter()).append(": ")
                    .append(value.getValue()).append(" ")
                    .append(value.getUnit()).append(" (")
                    .append(value.getStatus()).append(")\n");
            }
            response.append("\n");
        }
        
        // Medications
        if (!analysis.getMedications().isEmpty()) {
            response.append("**Medications Found:**\n");
            for (String med : analysis.getMedications()) {
                response.append("• ").append(med).append("\n");
            }
            response.append("\n");
        }
        
        // Dates
        if (!analysis.getDates().isEmpty()) {
            response.append("**Important Dates:**\n");
            for (String date : analysis.getDates()) {
                response.append("• ").append(date).append("\n");
            }
            response.append("\n");
        }
        
        // Recommendations
        if (!analysis.getRecommendations().isEmpty()) {
            response.append("**Recommendations:**\n");
            for (String rec : analysis.getRecommendations()) {
                response.append("• ").append(rec).append("\n");
            }
        }
        
        ChatMessage message = createResponse(
            response.toString(),
            ChatMessage.MessageType.AI_RESPONSE,
            analysis.getSeverity()
        );
        
        message.setRequiresFollowUp(analysis.requiresFollowUp());
        
        return message;
    }
    
    private ChatMessage handleReminderCreation(String userMessage) {
        // Extract reminder details from message
        DocumentAnalyzer.ReminderDetails details = documentAnalyzer.extractReminderDetails(userMessage);
        
        if (details == null || details.getMedicineName() == null) {
            return createResponse(
                "I'd be happy to help you create a reminder! Please provide:\n" +
                "• Medicine name\n" +
                "• Time(s) to take it\n" +
                "• Dosage (optional)\n\n" +
                "Example: \"Remind me to take Aspirin at 9 AM and 9 PM\"",
                ChatMessage.MessageType.AI_RESPONSE,
                ChatMessage.Severity.LOW
            );
        }
        
        // Create confirmation message
        StringBuilder response = new StringBuilder();
        response.append("💊 **Create Reminder**\n\n");
        response.append("I'll create a reminder for:\n");
        response.append("• Medicine: ").append(details.getMedicineName()).append("\n");
        
        if (details.getDose() != null) {
            response.append("• Dose: ").append(details.getDose()).append("\n");
        }
        
        if (!details.getTimes().isEmpty()) {
            response.append("• Times: ");
            response.append(String.join(", ", details.getTimes())).append("\n");
        }
        
        response.append("\n**Action Required:**\n");
        response.append("Tap 'Create Reminder' below to confirm, or reply with changes.");
        
        ChatMessage message = createResponse(
            response.toString(),
            ChatMessage.MessageType.AI_RECOMMENDATION,
            ChatMessage.Severity.LOW
        );
        
        // Store pending action
        actionHandler.setPendingReminderAction(details);
        
        return message;
    }
    
    private ChatMessage handleReminderList() {
        List<String> reminders = actionHandler.getActiveReminders();
        
        if (reminders.isEmpty()) {
            return createResponse(
                "You don't have any active reminders. Would you like to create one?",
                ChatMessage.MessageType.AI_RESPONSE,
                ChatMessage.Severity.LOW
            );
        }
        
        StringBuilder response = new StringBuilder();
        response.append("💊 **Your Active Reminders**\n\n");
        
        for (int i = 0; i < reminders.size(); i++) {
            response.append((i + 1)).append(". ").append(reminders.get(i)).append("\n");
        }
        
        response.append("\nWould you like to edit or delete any reminder?");
        
        return createResponse(
            response.toString(),
            ChatMessage.MessageType.AI_RESPONSE,
            ChatMessage.Severity.LOW
        );
    }
    
    private ChatMessage handleEmergencyQR() {
        StringBuilder response = new StringBuilder();
        response.append("🆘 **Emergency QR Code**\n\n");
        response.append("Your Emergency QR code contains:\n");
        
        if (userProfile != null) {
            response.append("• Name: ").append(userProfile.getFullName()).append("\n");
            if (userProfile.getBloodGroup() != null) {
                response.append("• Blood Group: ").append(userProfile.getBloodGroup()).append("\n");
            }
            if (userProfile.getAllergies() != null && !userProfile.getAllergies().isEmpty()) {
                response.append("• Allergies: ").append(userProfile.getAllergies()).append("\n");
            }
            if (userProfile.getEmergencyContact() != null) {
                response.append("• Emergency Contact: ").append(userProfile.getEmergencyContact()).append("\n");
            }
        }
        
        response.append("\n**Action Required:**\n");
        response.append("Tap 'Open Emergency QR' below to view or share your QR code.");
        
        ChatMessage message = createResponse(
            response.toString(),
            ChatMessage.MessageType.AI_RECOMMENDATION,
            ChatMessage.Severity.MEDIUM
        );
        
        actionHandler.setPendingEmergencyQRAction();
        
        return message;
    }
    
    private ChatMessage handleSymptomCheck(String userMessage) {
        // Analyze symptoms
        DocumentAnalyzer.SymptomAnalysis analysis = documentAnalyzer.analyzeSymptoms(userMessage, userProfile);
        
        StringBuilder response = new StringBuilder();
        response.append("🩺 **Symptom Assessment**\n\n");
        
        response.append("**Your Symptoms:**\n");
        for (String symptom : analysis.getSymptoms()) {
            response.append("• ").append(symptom).append("\n");
        }
        response.append("\n");
        
        response.append("**Assessment:**\n");
        response.append(analysis.getAssessment()).append("\n\n");
        
        response.append("**Recommendations:**\n");
        for (String rec : analysis.getRecommendations()) {
            response.append("• ").append(rec).append("\n");
        }
        
        ChatMessage message = createResponse(
            response.toString(),
            ChatMessage.MessageType.AI_RESPONSE,
            analysis.getSeverity()
        );
        
        message.setRequiresFollowUp(analysis.requiresFollowUp());
        
        return message;
    }
    
    private ChatMessage handleMedicationInfo(String userMessage) {
        String medicationName = documentAnalyzer.extractMedicationName(userMessage);
        
        if (medicationName == null) {
            return createResponse(
                "Please specify which medication you'd like information about.",
                ChatMessage.MessageType.AI_RESPONSE,
                ChatMessage.Severity.LOW
            );
        }
        
        StringBuilder response = new StringBuilder();
        response.append("💊 **Medication Information**\n\n");
        response.append("**Medication:** ").append(medicationName).append("\n\n");
        response.append("**Important:**\n");
        response.append("• Always take medications as prescribed by your doctor\n");
        response.append("• Don't stop or change dosage without consulting your doctor\n");
        response.append("• Report any side effects to your healthcare provider\n");
        response.append("• Keep medications in their original containers\n");
        response.append("• Check expiration dates regularly\n\n");
        response.append("For specific information about this medication, please consult your pharmacist or healthcare provider.");
        
        return createResponse(
            response.toString(),
            ChatMessage.MessageType.AI_RESPONSE,
            ChatMessage.Severity.LOW
        );
    }
    
    private ChatMessage handleHealthAdvice(String userMessage) {
        StringBuilder response = new StringBuilder();
        response.append("🏥 **Health Advice**\n\n");
        
        // Personalize based on profile
        if (userProfile != null) {
            response.append("Based on your profile:\n");
            if (userProfile.getAge() != null) {
                response.append("• Age: ").append(userProfile.getAge()).append(" years\n");
            }
            if (userProfile.getGender() != null) {
                response.append("• Gender: ").append(userProfile.getGender()).append("\n");
            }
            response.append("\n");
        }
        
        response.append("**General Health Tips:**\n");
        response.append("• Maintain a balanced diet with fruits and vegetables\n");
        response.append("• Exercise regularly (150 minutes per week)\n");
        response.append("• Get 7-9 hours of sleep nightly\n");
        response.append("• Stay hydrated (8-10 glasses of water daily)\n");
        response.append("• Manage stress through relaxation techniques\n");
        response.append("• Schedule regular health check-ups\n\n");
        response.append("For personalized advice, please consult your healthcare provider.");
        
        return createResponse(
            response.toString(),
            ChatMessage.MessageType.AI_RESPONSE,
            ChatMessage.Severity.LOW
        );
    }
    
    private ChatMessage handleGeneralQuery(String userMessage) {
        StringBuilder response = new StringBuilder();
        response.append("🤖 **How Can I Help?**\n\n");
        response.append("I can assist you with:\n\n");
        response.append("📋 **Report Analysis**\n");
        response.append("• \"Analyze my report\"\n");
        response.append("• \"What do my test results mean?\"\n\n");
        response.append("💊 **Medicine Reminders**\n");
        response.append("• \"Remind me to take Aspirin at 9 AM\"\n");
        response.append("• \"Show my reminders\"\n\n");
        response.append("🆘 **Emergency QR**\n");
        response.append("• \"Show my emergency QR\"\n");
        response.append("• \"Generate emergency QR code\"\n\n");
        response.append("🩺 **Symptom Check**\n");
        response.append("• \"I have a headache and fever\"\n");
        response.append("• \"Check my symptoms\"\n\n");
        response.append("What would you like help with?");
        
        return createResponse(
            response.toString(),
            ChatMessage.MessageType.AI_RESPONSE,
            ChatMessage.Severity.LOW
        );
    }
    
    private ChatMessage handleEmergency(String userMessage) {
        EmergencyDetector.EmergencyType type = emergencyDetector.getEmergencyType(userMessage);
        
        StringBuilder response = new StringBuilder();
        response.append("🚨 **EMERGENCY DETECTED**\n\n");
        
        switch (type) {
            case CHEST_PAIN:
                response.append("**Chest pain can be serious!**\n\n");
                response.append("**Immediate Actions:**\n");
                response.append("1. Call emergency services (911) immediately\n");
                response.append("2. Sit down and rest\n");
                response.append("3. If you have aspirin, chew one (unless allergic)\n");
                response.append("4. Don't drive yourself to the hospital\n");
                break;
            case BREATHING_DIFFICULTY:
                response.append("**Difficulty breathing requires immediate attention!**\n\n");
                response.append("**Immediate Actions:**\n");
                response.append("1. Call emergency services (911) immediately\n");
                response.append("2. Sit upright\n");
                response.append("3. Loosen tight clothing\n");
                response.append("4. Use inhaler if prescribed\n");
                break;
            case SEVERE_BLEEDING:
                response.append("**Severe bleeding is a medical emergency!**\n\n");
                response.append("**Immediate Actions:**\n");
                response.append("1. Call emergency services (911) immediately\n");
                response.append("2. Apply direct pressure to wound\n");
                response.append("3. Elevate injured area if possible\n");
                response.append("4. Don't remove embedded objects\n");
                break;
            case LOSS_OF_CONSCIOUSNESS:
                response.append("**Loss of consciousness is serious!**\n\n");
                response.append("**Immediate Actions:**\n");
                response.append("1. Call emergency services (911) immediately\n");
                response.append("2. Check breathing and pulse\n");
                response.append("3. Place in recovery position if breathing\n");
                response.append("4. Start CPR if not breathing\n");
                break;
            default:
                response.append("**Your symptoms may require immediate medical attention.**\n\n");
                response.append("**Consider:**\n");
                response.append("1. Calling emergency services (911) if severe\n");
                response.append("2. Visiting the nearest emergency room\n");
                response.append("3. Contacting your doctor immediately\n");
                break;
        }
        
        response.append("\n**Actions Available:**\n");
        response.append("• Tap 'Call Emergency' to dial 911\n");
        response.append("• Tap 'Show Emergency QR' for your medical info\n");
        
        ChatMessage message = createResponse(
            response.toString(),
            ChatMessage.MessageType.SYSTEM_INFO,
            ChatMessage.Severity.CRITICAL
        );
        
        actionHandler.setPendingEmergencyAction(type);
        
        return message;
    }
    
    private ChatMessage createResponse(String content, ChatMessage.MessageType type, ChatMessage.Severity severity) {
        ChatMessage message = new ChatMessage();
        message.setType(type);
        message.setFromUser(false);
        message.setContent(content);
        message.setSeverity(severity);
        message.setTimestamp(System.currentTimeMillis());
        
        // Add disclaimer for medical content
        if (type == ChatMessage.MessageType.AI_RESPONSE || 
            type == ChatMessage.MessageType.AI_RECOMMENDATION) {
            message.setMedicalDisclaimer(
                "⚠️ This information is for educational purposes only. " +
                "Always consult your healthcare provider for medical advice."
            );
        }
        
        return message;
    }
    
    /**
     * Get action handler for executing actions
     */
    public ActionHandler getActionHandler() {
        return actionHandler;
    }
    
    /**
     * Intent types for message routing
     */
    private enum Intent {
        ANALYZE_REPORT,
        CREATE_REMINDER,
        LIST_REMINDERS,
        EMERGENCY_QR,
        SYMPTOM_CHECK,
        MEDICATION_INFO,
        HEALTH_ADVICE,
        GENERAL_QUERY
    }
}
