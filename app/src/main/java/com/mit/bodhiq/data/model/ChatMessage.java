package com.mit.bodhiq.data.model;

import java.util.List;

/**
 * Data model for chat messages in the medical chatbot
 */
public class ChatMessage {
    public enum MessageType {
        USER_TEXT,
        USER_REPORT,
        AI_RESPONSE,
        AI_MEDICAL_CARD,
        AI_RECOMMENDATION,
        SYSTEM_INFO
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    private String id;
    private String userId;
    private MessageType type;
    private String content;
    private long timestamp;
    private boolean isFromUser;
    
    // Medical-specific fields
    private List<MedicalParameter> medicalParameters;
    private List<String> recommendations;
    private Severity severity;
    private String reportId;
    private boolean requiresFollowUp;
    private String medicalDisclaimer;

    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(String userId, MessageType type, String content, boolean isFromUser) {
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.isFromUser = isFromUser;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isFromUser() { return isFromUser; }
    public void setFromUser(boolean fromUser) { isFromUser = fromUser; }

    public List<MedicalParameter> getMedicalParameters() { return medicalParameters; }
    public void setMedicalParameters(List<MedicalParameter> medicalParameters) { this.medicalParameters = medicalParameters; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public boolean isRequiresFollowUp() { return requiresFollowUp; }
    public void setRequiresFollowUp(boolean requiresFollowUp) { this.requiresFollowUp = requiresFollowUp; }

    public String getMedicalDisclaimer() { return medicalDisclaimer; }
    public void setMedicalDisclaimer(String medicalDisclaimer) { this.medicalDisclaimer = medicalDisclaimer; }
}