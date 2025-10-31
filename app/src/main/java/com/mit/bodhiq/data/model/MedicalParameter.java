package com.mit.bodhiq.data.model;

/**
 * Data model for individual medical parameters extracted from reports
 */
public class MedicalParameter {
    private String parameter;
    private String value;
    private String unit;
    private String status; // HIGH, LOW, NORMAL
    private String referenceRange;
    private String notes;

    public MedicalParameter() {}

    public MedicalParameter(String parameter, String value, String unit, String status) {
        this.parameter = parameter;
        this.value = value;
        this.unit = unit;
        this.status = status;
    }

    // Getters and Setters
    public String getParameter() { return parameter; }
    public void setParameter(String parameter) { this.parameter = parameter; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReferenceRange() { return referenceRange; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}