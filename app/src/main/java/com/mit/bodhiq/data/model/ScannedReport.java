package com.mit.bodhiq.data.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data model for scanned medical reports
 */
public class ScannedReport {
    private String id;
    private String userId;
    private String title;
    private String extractedText;
    private String imageUrl;
    private long timestamp;
    private List<HealthValue> healthValues;
    private String status; // "processing", "completed", "failed"

    // Default constructor for Firestore
    public ScannedReport() {}

    public ScannedReport(String userId, String title, String extractedText, String imageUrl) {
        this.userId = userId;
        this.title = title;
        this.extractedText = extractedText;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
        this.status = "completed";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public List<HealthValue> getHealthValues() { return healthValues; }
    public void setHealthValues(List<HealthValue> healthValues) { this.healthValues = healthValues; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Convert to Map for Firestore
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("title", title);
        result.put("extractedText", extractedText);
        result.put("imageUrl", imageUrl);
        result.put("timestamp", timestamp);
        result.put("status", status);
        return result;
    }
}