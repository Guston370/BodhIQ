package com.mit.bodhiq.data.model;

/**
 * Individual parsed field from medical report
 */
public class ParsedField {
    
    public enum Flag {
        NORMAL,
        BORDERLINE,
        LOW,
        HIGH,
        CRITICAL,
        UNKNOWN
    }
    
    public enum ActionLevel {
        NONE,
        SELF_CARE,
        CONSULT_SPECIALIST,
        URGENT_CARE,
        EMERGENCY
    }
    
    private String id;
    private String name;
    private String value;
    private String unit;
    private String referenceRange;
    private Flag flag;
    private float confidence;
    private String notes;
    private ActionLevel actionLevel;
    private String suggestedAction;
    private boolean isEdited;
    private long editedAt;
    
    public ParsedField() {
        this.flag = Flag.UNKNOWN;
        this.actionLevel = ActionLevel.NONE;
        this.confidence = 0.0f;
    }
    
    public ParsedField(String name, String value, String unit) {
        this();
        this.name = name;
        this.value = value;
        this.unit = unit;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public String getReferenceRange() { return referenceRange; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }
    
    public Flag getFlag() { return flag; }
    public void setFlag(Flag flag) { this.flag = flag; }
    
    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public ActionLevel getActionLevel() { return actionLevel; }
    public void setActionLevel(ActionLevel actionLevel) { this.actionLevel = actionLevel; }
    
    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }
    
    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean edited) { isEdited = edited; }
    
    public long getEditedAt() { return editedAt; }
    public void setEditedAt(long editedAt) { this.editedAt = editedAt; }
    
    /**
     * Get numeric value for comparison
     */
    public Double getNumericValue() {
        try {
            // Remove non-numeric characters except decimal point
            String cleaned = value.replaceAll("[^0-9.]", "");
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get flag color resource
     */
    public int getFlagColor() {
        switch (flag) {
            case NORMAL:
                return android.R.color.holo_green_dark;
            case BORDERLINE:
                return android.R.color.holo_orange_light;
            case LOW:
            case HIGH:
                return android.R.color.holo_orange_dark;
            case CRITICAL:
                return android.R.color.holo_red_dark;
            default:
                return android.R.color.darker_gray;
        }
    }
    
    /**
     * Get flag icon
     */
    public String getFlagIcon() {
        switch (flag) {
            case NORMAL:
                return "✓";
            case BORDERLINE:
                return "⚠";
            case LOW:
                return "↓";
            case HIGH:
                return "↑";
            case CRITICAL:
                return "🚨";
            default:
                return "?";
        }
    }
}
