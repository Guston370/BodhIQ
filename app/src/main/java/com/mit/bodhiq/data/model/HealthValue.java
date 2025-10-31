package com.mit.bodhiq.data.model;

/**
 * Data model for parsed health values from medical reports
 */
public class HealthValue {
    private String parameter;
    private String value;
    private String unit;
    private String normalRange;
    private String status; // "normal", "high", "low", "unknown"

    public HealthValue() {}

    public HealthValue(String parameter, String value, String unit, String status) {
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

    public String getNormalRange() { return normalRange; }
    public void setNormalRange(String normalRange) { this.normalRange = normalRange; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}