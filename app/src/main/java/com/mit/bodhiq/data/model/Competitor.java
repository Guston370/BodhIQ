package com.mit.bodhiq.data.model;

/**
 * POJO representing competitor information in pharmaceutical market
 */
public class Competitor {
    private String companyName;
    private String productName;
    private double marketShare; // Percentage
    private String region;
    private String launchYear;
    private String indication;
    private String competitiveAdvantage;

    public Competitor() {}

    public Competitor(String companyName, String productName, double marketShare, 
                     String region, String launchYear, String indication, String competitiveAdvantage) {
        this.companyName = companyName;
        this.productName = productName;
        this.marketShare = marketShare;
        this.region = region;
        this.launchYear = launchYear;
        this.indication = indication;
        this.competitiveAdvantage = competitiveAdvantage;
    }

    // Getters and Setters
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getMarketShare() {
        return marketShare;
    }

    public void setMarketShare(double marketShare) {
        this.marketShare = marketShare;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLaunchYear() {
        return launchYear;
    }

    public void setLaunchYear(String launchYear) {
        this.launchYear = launchYear;
    }

    public String getIndication() {
        return indication;
    }

    public void setIndication(String indication) {
        this.indication = indication;
    }

    public String getCompetitiveAdvantage() {
        return competitiveAdvantage;
    }

    public void setCompetitiveAdvantage(String competitiveAdvantage) {
        this.competitiveAdvantage = competitiveAdvantage;
    }

    @Override
    public String toString() {
        return "Competitor{" +
                "companyName='" + companyName + '\'' +
                ", productName='" + productName + '\'' +
                ", marketShare=" + marketShare +
                ", region='" + region + '\'' +
                ", launchYear='" + launchYear + '\'' +
                ", indication='" + indication + '\'' +
                ", competitiveAdvantage='" + competitiveAdvantage + '\'' +
                '}';
    }
}