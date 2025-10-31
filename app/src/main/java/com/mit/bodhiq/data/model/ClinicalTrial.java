package com.mit.bodhiq.data.model;

/**
 * POJO representing clinical trial information
 */
public class ClinicalTrial {
    private String nctId; // ClinicalTrials.gov identifier
    private String title;
    private String phase; // Phase I, II, III, IV
    private String status; // Recruiting, Completed, Terminated, etc.
    private String sponsor;
    private String molecule;
    private String indication;
    private int enrollmentCount;
    private String startDate;
    private String completionDate;
    private String primaryEndpoint;
    private String secondaryEndpoints;
    private String studyType; // Interventional, Observational
    private String allocation; // Randomized, Non-Randomized
    private String masking; // Open Label, Single Blind, Double Blind
    private String location; // Countries/regions

    public ClinicalTrial() {}

    public ClinicalTrial(String nctId, String title, String phase, String status, String sponsor,
                        String molecule, String indication, int enrollmentCount, String startDate,
                        String completionDate, String primaryEndpoint, String secondaryEndpoints,
                        String studyType, String allocation, String masking, String location) {
        this.nctId = nctId;
        this.title = title;
        this.phase = phase;
        this.status = status;
        this.sponsor = sponsor;
        this.molecule = molecule;
        this.indication = indication;
        this.enrollmentCount = enrollmentCount;
        this.startDate = startDate;
        this.completionDate = completionDate;
        this.primaryEndpoint = primaryEndpoint;
        this.secondaryEndpoints = secondaryEndpoints;
        this.studyType = studyType;
        this.allocation = allocation;
        this.masking = masking;
        this.location = location;
    }

    // Getters and Setters
    public String getNctId() {
        return nctId;
    }

    public void setNctId(String nctId) {
        this.nctId = nctId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getMolecule() {
        return molecule;
    }

    public void setMolecule(String molecule) {
        this.molecule = molecule;
    }

    public String getIndication() {
        return indication;
    }

    public void setIndication(String indication) {
        this.indication = indication;
    }

    public int getEnrollmentCount() {
        return enrollmentCount;
    }

    public void setEnrollmentCount(int enrollmentCount) {
        this.enrollmentCount = enrollmentCount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    public String getPrimaryEndpoint() {
        return primaryEndpoint;
    }

    public void setPrimaryEndpoint(String primaryEndpoint) {
        this.primaryEndpoint = primaryEndpoint;
    }

    public String getSecondaryEndpoints() {
        return secondaryEndpoints;
    }

    public void setSecondaryEndpoints(String secondaryEndpoints) {
        this.secondaryEndpoints = secondaryEndpoints;
    }

    public String getStudyType() {
        return studyType;
    }

    public void setStudyType(String studyType) {
        this.studyType = studyType;
    }

    public String getAllocation() {
        return allocation;
    }

    public void setAllocation(String allocation) {
        this.allocation = allocation;
    }

    public String getMasking() {
        return masking;
    }

    public void setMasking(String masking) {
        this.masking = masking;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isCompleted() {
        return "Completed".equalsIgnoreCase(status);
    }

    public boolean isActive() {
        return "Recruiting".equalsIgnoreCase(status) || "Active, not recruiting".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "ClinicalTrial{" +
                "nctId='" + nctId + '\'' +
                ", title='" + title + '\'' +
                ", phase='" + phase + '\'' +
                ", status='" + status + '\'' +
                ", sponsor='" + sponsor + '\'' +
                ", molecule='" + molecule + '\'' +
                ", indication='" + indication + '\'' +
                ", enrollmentCount=" + enrollmentCount +
                ", startDate='" + startDate + '\'' +
                ", completionDate='" + completionDate + '\'' +
                ", primaryEndpoint='" + primaryEndpoint + '\'' +
                ", secondaryEndpoints='" + secondaryEndpoints + '\'' +
                ", studyType='" + studyType + '\'' +
                ", allocation='" + allocation + '\'' +
                ", masking='" + masking + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}