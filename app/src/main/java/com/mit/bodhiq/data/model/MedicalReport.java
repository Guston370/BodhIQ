package com.mit.bodhiq.data.model;

import java.util.List;

/**
 * Data model for medical report analysis results
 */
public class MedicalReport {
    private String id;
    private String userId;
    private String originalImagePath;
    private String extractedText;
    private List<MedicalParameter> parameters;
    private String aiInsights;
    private String summary;
    private long createdAt;
    private long updatedAt;
    private String status; // PROCESSING, COMPLETED, FAILED

    public MedicalReport() {}

    public MedicalReport(String userId, String originalImagePath, String extractedText) {
        this.userId = userId;
        this.originalImagePath = originalImagePath;
        this.extractedText = extractedText;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.status = "PROCESSING";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOriginalImagePath() { return originalImagePath; }
    public void setOriginalImagePath(String originalImagePath) { this.originalImagePath = originalImagePath; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public List<MedicalParameter> getParameters() { return parameters; }
    public void setParameters(List<MedicalParameter> parameters) { this.parameters = parameters; }

    public String getAiInsights() { return aiInsights; }
    public void setAiInsights(String aiInsights) { this.aiInsights = aiInsights; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}