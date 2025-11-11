package com.mit.bodhiq.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsed medical report with structured fields
 */
public class ParsedReport {
    private String reportId;
    private String patientName;
    private String reportDate;
    private String imageUri;
    private float overallConfidence;
    
    private PatientDetails patientDetails;
    private List<ParsedField> vitalSigns;
    private List<ParsedField> labResults;
    private List<ParsedField> medications;
    private List<ParsedField> diagnostics;
    private String remarks;
    
    private long parsedAt;
    private String rawText;
    
    public ParsedReport() {
        this.vitalSigns = new ArrayList<>();
        this.labResults = new ArrayList<>();
        this.medications = new ArrayList<>();
        this.diagnostics = new ArrayList<>();
        this.parsedAt = System.currentTimeMillis();
    }
    
    // Getters and setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public String getReportDate() { return reportDate; }
    public void setReportDate(String reportDate) { this.reportDate = reportDate; }
    
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    
    public float getOverallConfidence() { return overallConfidence; }
    public void setOverallConfidence(float overallConfidence) { this.overallConfidence = overallConfidence; }
    
    public PatientDetails getPatientDetails() { return patientDetails; }
    public void setPatientDetails(PatientDetails patientDetails) { this.patientDetails = patientDetails; }
    
    public List<ParsedField> getVitalSigns() { return vitalSigns; }
    public void setVitalSigns(List<ParsedField> vitalSigns) { this.vitalSigns = vitalSigns; }
    
    public List<ParsedField> getLabResults() { return labResults; }
    public void setLabResults(List<ParsedField> labResults) { this.labResults = labResults; }
    
    public List<ParsedField> getMedications() { return medications; }
    public void setMedications(List<ParsedField> medications) { this.medications = medications; }
    
    public List<ParsedField> getDiagnostics() { return diagnostics; }
    public void setDiagnostics(List<ParsedField> diagnostics) { this.diagnostics = diagnostics; }
    
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    public long getParsedAt() { return parsedAt; }
    public void setParsedAt(long parsedAt) { this.parsedAt = parsedAt; }
    
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    
    /**
     * Get all flagged (abnormal) fields
     */
    public List<ParsedField> getFlaggedFields() {
        List<ParsedField> flagged = new ArrayList<>();
        for (ParsedField field : labResults) {
            if (field.getFlag() != ParsedField.Flag.NORMAL && field.getFlag() != ParsedField.Flag.UNKNOWN) {
                flagged.add(field);
            }
        }
        for (ParsedField field : vitalSigns) {
            if (field.getFlag() != ParsedField.Flag.NORMAL && field.getFlag() != ParsedField.Flag.UNKNOWN) {
                flagged.add(field);
            }
        }
        return flagged;
    }
    
    /**
     * Get chat context summary
     */
    public String getChatContextSummary() {
        List<ParsedField> flagged = getFlaggedFields();
        if (flagged.isEmpty()) {
            return "All values are within normal range.";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Flagged items:\n");
        for (ParsedField field : flagged) {
            summary.append("• ").append(field.getName()).append(": ")
                .append(field.getValue()).append(" ").append(field.getUnit())
                .append(" (").append(field.getFlag()).append(")\n");
        }
        return summary.toString();
    }
    
    /**
     * Patient details sub-model
     */
    public static class PatientDetails {
        private String name;
        private String age;
        private String gender;
        private String patientId;
        private String doctorName;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getAge() { return age; }
        public void setAge(String age) { this.age = age; }
        
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        
        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }
        
        public String getDoctorName() { return doctorName; }
        public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    }
}
