package com.mit.bodhiq.data.model;

import java.util.List;

/**
 * Data model for AI-generated medical insights
 */
public class MedicalInsight {
    public enum InsightType {
        BLOOD_WORK,
        VITAL_SIGNS,
        METABOLIC,
        HORMONAL,
        LIVER_FUNCTION,
        KIDNEY_FUNCTION,
        CARDIAC,
        GENERAL_HEALTH
    }

    public enum RiskLevel {
        NORMAL,
        MILD_CONCERN,
        MODERATE_CONCERN,
        HIGH_CONCERN,
        CRITICAL
    }

    private String id;
    private InsightType type;
    private String title;
    private String description;
    private RiskLevel riskLevel;
    private List<String> affectedParameters;
    private List<String> recommendations;
    private List<String> suggestedTests;
    private String whenToSeeDoctor;
    private boolean emergencyFlag;
    private double confidenceScore;
    private long timestamp;

    public MedicalInsight() {
        this.timestamp = System.currentTimeMillis();
    }

    public MedicalInsight(InsightType type, String title, String description, RiskLevel riskLevel) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.riskLevel = riskLevel;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public InsightType getType() { return type; }
    public void setType(InsightType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public List<String> getAffectedParameters() { return affectedParameters; }
    public void setAffectedParameters(List<String> affectedParameters) { this.affectedParameters = affectedParameters; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public List<String> getSuggestedTests() { return suggestedTests; }
    public void setSuggestedTests(List<String> suggestedTests) { this.suggestedTests = suggestedTests; }

    public String getWhenToSeeDoctor() { return whenToSeeDoctor; }
    public void setWhenToSeeDoctor(String whenToSeeDoctor) { this.whenToSeeDoctor = whenToSeeDoctor; }

    public boolean isEmergencyFlag() { return emergencyFlag; }
    public void setEmergencyFlag(boolean emergencyFlag) { this.emergencyFlag = emergencyFlag; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}