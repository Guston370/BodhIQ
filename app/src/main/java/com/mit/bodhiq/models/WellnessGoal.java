package com.mit.bodhiq.models;

import java.util.Date;

/**
 * Model class for wellness goals
 */
public class WellnessGoal {
    private String id;
    private String title;
    private String description;
    private String type; // "steps", "weight", "exercise", "water", "sleep"
    private double targetValue;
    private double currentValue;
    private String unit;
    private Date createdDate;
    private Date targetDate;
    private boolean isActive;
    
    public WellnessGoal() {}
    
    public WellnessGoal(String id, String title, String description, String type, 
                       double targetValue, double currentValue, String unit) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.targetValue = targetValue;
        this.currentValue = currentValue;
        this.unit = unit;
        this.createdDate = new Date();
        this.isActive = true;
    }
    
    public double getProgressPercentage() {
        if (targetValue == 0) return 0;
        return Math.min(100, (currentValue / targetValue) * 100);
    }
    
    public String getProgressText() {
        return String.format("%.0f / %.0f %s", currentValue, targetValue, unit);
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { this.targetValue = targetValue; }
    
    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    
    public Date getTargetDate() { return targetDate; }
    public void setTargetDate(Date targetDate) { this.targetDate = targetDate; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}