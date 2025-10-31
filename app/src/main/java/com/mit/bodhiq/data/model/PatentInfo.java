package com.mit.bodhiq.data.model;

/**
 * POJO representing pharmaceutical patent information
 */
public class PatentInfo {
    private String patentNumber;
    private String title;
    private String assignee; // Patent holder/company
    private String filingDate;
    private String publicationDate;
    private String expiryDate;
    private String status; // Active, Expired, Pending, etc.
    private String jurisdiction; // US, EU, etc.
    private String molecule;
    private String indication;
    private String patentType; // Composition, Method, Formulation, etc.
    private String priority;
    private String inventorNames;

    public PatentInfo() {}

    public PatentInfo(String patentNumber, String title, String assignee, String filingDate,
                     String publicationDate, String expiryDate, String status, String jurisdiction,
                     String molecule, String indication, String patentType, String priority,
                     String inventorNames) {
        this.patentNumber = patentNumber;
        this.title = title;
        this.assignee = assignee;
        this.filingDate = filingDate;
        this.publicationDate = publicationDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.jurisdiction = jurisdiction;
        this.molecule = molecule;
        this.indication = indication;
        this.patentType = patentType;
        this.priority = priority;
        this.inventorNames = inventorNames;
    }

    // Getters and Setters
    public String getPatentNumber() {
        return patentNumber;
    }

    public void setPatentNumber(String patentNumber) {
        this.patentNumber = patentNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getFilingDate() {
        return filingDate;
    }

    public void setFilingDate(String filingDate) {
        this.filingDate = filingDate;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
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

    public String getPatentType() {
        return patentType;
    }

    public void setPatentType(String patentType) {
        this.patentType = patentType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getInventorNames() {
        return inventorNames;
    }

    public void setInventorNames(String inventorNames) {
        this.inventorNames = inventorNames;
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }

    public boolean isExpired() {
        return "Expired".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "PatentInfo{" +
                "patentNumber='" + patentNumber + '\'' +
                ", title='" + title + '\'' +
                ", assignee='" + assignee + '\'' +
                ", filingDate='" + filingDate + '\'' +
                ", publicationDate='" + publicationDate + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", status='" + status + '\'' +
                ", jurisdiction='" + jurisdiction + '\'' +
                ", molecule='" + molecule + '\'' +
                ", indication='" + indication + '\'' +
                ", patentType='" + patentType + '\'' +
                ", priority='" + priority + '\'' +
                ", inventorNames='" + inventorNames + '\'' +
                '}';
    }
}