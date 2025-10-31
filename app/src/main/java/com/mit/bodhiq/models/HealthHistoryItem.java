package com.mit.bodhiq.models;

import java.util.Date;

/**
 * Model class for health history items
 */
public class HealthHistoryItem {
    private String id;
    private String title;
    private String summary;
    private Date date;
    private String type; // "report", "diagnosis", "recommendation"
    private String status; // "normal", "high", "low", "critical"
    
    public HealthHistoryItem() {}
    
    public HealthHistoryItem(String id, String title, String summary, Date date, String type, String status) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.date = date;
        this.type = type;
        this.status = status;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}