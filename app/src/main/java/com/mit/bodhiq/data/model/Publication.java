package com.mit.bodhiq.data.model;

/**
 * POJO representing scientific publication information
 */
public class Publication {
    private String title;
    private String authors;
    private String journal;
    private String publicationDate;
    private String doi; // Digital Object Identifier
    private String pmid; // PubMed ID
    private String abstractText;
    private String molecule;
    private String indication;
    private String studyType; // Clinical, Preclinical, Review, etc.
    private String keywords;
    private int citationCount;
    private String impactFactor;
    private String url;
    private String relevanceScore; // High, Medium, Low

    public Publication() {}

    public Publication(String title, String authors, String journal, String publicationDate,
                      String doi, String pmid, String abstractText, String molecule,
                      String indication, String studyType, String keywords, int citationCount,
                      String impactFactor, String url, String relevanceScore) {
        this.title = title;
        this.authors = authors;
        this.journal = journal;
        this.publicationDate = publicationDate;
        this.doi = doi;
        this.pmid = pmid;
        this.abstractText = abstractText;
        this.molecule = molecule;
        this.indication = indication;
        this.studyType = studyType;
        this.keywords = keywords;
        this.citationCount = citationCount;
        this.impactFactor = impactFactor;
        this.url = url;
        this.relevanceScore = relevanceScore;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
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

    public String getStudyType() {
        return studyType;
    }

    public void setStudyType(String studyType) {
        this.studyType = studyType;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public int getCitationCount() {
        return citationCount;
    }

    public void setCitationCount(int citationCount) {
        this.citationCount = citationCount;
    }

    public String getImpactFactor() {
        return impactFactor;
    }

    public void setImpactFactor(String impactFactor) {
        this.impactFactor = impactFactor;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(String relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public boolean isHighRelevance() {
        return "High".equalsIgnoreCase(relevanceScore);
    }

    public boolean isClinicalStudy() {
        return "Clinical".equalsIgnoreCase(studyType);
    }

    @Override
    public String toString() {
        return "Publication{" +
                "title='" + title + '\'' +
                ", authors='" + authors + '\'' +
                ", journal='" + journal + '\'' +
                ", publicationDate='" + publicationDate + '\'' +
                ", doi='" + doi + '\'' +
                ", pmid='" + pmid + '\'' +
                ", abstractText='" + abstractText + '\'' +
                ", molecule='" + molecule + '\'' +
                ", indication='" + indication + '\'' +
                ", studyType='" + studyType + '\'' +
                ", keywords='" + keywords + '\'' +
                ", citationCount=" + citationCount +
                ", impactFactor='" + impactFactor + '\'' +
                ", url='" + url + '\'' +
                ", relevanceScore='" + relevanceScore + '\'' +
                '}';
    }
}