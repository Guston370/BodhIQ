package com.mit.bodhiq.utils;

import android.content.Context;
import com.mit.bodhiq.R;
import com.mit.bodhiq.data.model.ChatMessage;

/**
 * Helper class for building empathetic chatbot responses using string resources
 */
public class EmpathicResponseBuilder {
    
    private final Context context;
    
    public EmpathicResponseBuilder(Context context) {
        this.context = context;
    }
    
    /**
     * Build a complete empathetic response based on severity and user input
     */
    public String buildEmpathicResponse(String userInput, ChatMessage.Severity severity, String symptom) {
        StringBuilder response = new StringBuilder();
        
        // 1. Empathetic acknowledgment
        response.append(getEmpathicAcknowledgment(symptom, severity));
        response.append("\n\n");
        
        // 2. Reassuring context based on severity
        response.append(getReassuranceContext(symptom, severity));
        response.append("\n\n");
        
        // 3. Practical guidance
        response.append(getPracticalGuidance(severity));
        response.append("\n\n");
        
        // 4. Follow-up questions
        response.append(getFollowUpQuestions(symptom));
        
        return response.toString();
    }
    
    /**
     * Get empathetic acknowledgment based on severity
     */
    private String getEmpathicAcknowledgment(String symptom, ChatMessage.Severity severity) {
        switch (severity) {
            case LOW:
                return String.format(context.getString(R.string.chatbot_understanding_prefix), symptom) + 
                       ". " + context.getString(R.string.chatbot_validation_concern) + ".";
                       
            case MEDIUM:
                return String.format(context.getString(R.string.chatbot_empathy_acknowledgment), "frustrating") + 
                       ". " + context.getString(R.string.chatbot_validation_action) + ".";
                       
            case HIGH:
                return String.format(context.getString(R.string.chatbot_high_severity_intro), symptom) + 
                       ". " + context.getString(R.string.chatbot_validation_action) + ".";
                       
            case CRITICAL:
                return String.format(context.getString(R.string.chatbot_critical_severity_intro), symptom) + 
                       ". I'm here to help guide you through this.";
                       
            default:
                return String.format(context.getString(R.string.chatbot_understanding_prefix), symptom);
        }
    }
    
    /**
     * Get reassuring context based on severity
     */
    private String getReassuranceContext(String symptom, ChatMessage.Severity severity) {
        switch (severity) {
            case LOW:
                return String.format(context.getString(R.string.chatbot_low_severity_intro), symptom);
                
            case MEDIUM:
                return String.format(context.getString(R.string.chatbot_moderate_severity_intro), symptom);
                
            case HIGH:
                return "While there can be various causes, when it comes to " + symptom + 
                       ", it's always best to have it evaluated. " + 
                       context.getString(R.string.chatbot_reassurance_care) + ".";
                       
            case CRITICAL:
                return "I understand this must be frightening. The most important thing right now is " +
                       "getting you the immediate care you need. " + 
                       context.getString(R.string.chatbot_reassurance_care) + ".";
                       
            default:
                return String.format(context.getString(R.string.chatbot_reassurance_common), "manageable");
        }
    }
    
    /**
     * Get practical guidance based on severity
     */
    private String getPracticalGuidance(ChatMessage.Severity severity) {
        StringBuilder guidance = new StringBuilder();
        
        switch (severity) {
            case LOW:
                guidance.append("**").append(context.getString(R.string.chatbot_gentle_suggestion)).append("**\n");
                guidance.append("• ").append(context.getString(R.string.chatbot_selfcare_rest)).append("\n");
                guidance.append("• ").append(context.getString(R.string.chatbot_selfcare_hydration)).append("\n");
                guidance.append("• ").append(context.getString(R.string.chatbot_selfcare_comfort)).append("\n");
                guidance.append("• ").append(context.getString(R.string.chatbot_selfcare_gentle_activity));
                break;
                
            case MEDIUM:
                guidance.append("**It might be helpful to:**\n");
                guidance.append("• ").append(context.getString(R.string.chatbot_selfcare_monitoring)).append("\n");
                guidance.append("• Notice if certain activities or times affect how you feel\n");
                guidance.append("• ").append(String.format(context.getString(R.string.chatbot_professional_guidance), 
                    "this continues for more than a week or two"));
                break;
                
            case HIGH:
                guidance.append("**I'd recommend:**\n");
                guidance.append("• Contacting your doctor or healthcare provider today if possible\n");
                guidance.append("• If symptoms worsen, don't hesitate to seek immediate care\n");
                guidance.append("• Try to stay calm and avoid strenuous activity until you can be seen");
                break;
                
            case CRITICAL:
                guidance.append("**Immediate steps:**\n");
                guidance.append("• Seek emergency medical care right away\n");
                guidance.append("• Call emergency services or go to the nearest emergency room\n");
                guidance.append("• If possible, have someone accompany you\n");
                guidance.append("• Stay as calm as possible - help is available");
                break;
        }
        
        return guidance.toString();
    }
    
    /**
     * Get appropriate follow-up questions
     */
    private String getFollowUpQuestions(String symptom) {
        return context.getString(R.string.chatbot_followup_duration) + "? " +
               context.getString(R.string.chatbot_followup_triggers) + "?";
    }
    
    /**
     * Get medical disclaimer based on severity
     */
    public String getMedicalDisclaimer(ChatMessage.Severity severity) {
        switch (severity) {
            case LOW:
                return context.getString(R.string.chatbot_disclaimer_low);
            case MEDIUM:
                return context.getString(R.string.chatbot_disclaimer_moderate);
            case HIGH:
                return context.getString(R.string.chatbot_disclaimer_high);
            case CRITICAL:
                return context.getString(R.string.chatbot_disclaimer_critical);
            default:
                return context.getString(R.string.chatbot_disclaimer_low);
        }
    }
    
    /**
     * Build a supportive closing message
     */
    public String buildSupportiveClosing() {
        return context.getString(R.string.chatbot_supportive_closing) + ". " +
               "Feel free to share any other details that might be helpful.";
    }
}