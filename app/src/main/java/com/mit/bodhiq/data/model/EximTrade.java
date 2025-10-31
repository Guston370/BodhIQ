package com.mit.bodhiq.data.model;

/**
 * POJO representing export-import trade data for pharmaceutical products
 */
public class EximTrade {
    private String molecule;
    private String country;
    private String tradeType; // Import or Export
    private double volumeKg; // Volume in kilograms
    private double valueUsd; // Value in USD
    private String year;
    private String month;
    private String hsCode; // Harmonized System Code
    private String productDescription;
    private String partnerCountry; // Trading partner country
    private double unitPrice; // USD per kg
    private double marketShare; // Percentage of total trade
    private String tradeGrowth; // YoY growth percentage

    public EximTrade() {}

    public EximTrade(String molecule, String country, String tradeType, double volumeKg,
                    double valueUsd, String year, String month, String hsCode,
                    String productDescription, String partnerCountry, double unitPrice,
                    double marketShare, String tradeGrowth) {
        this.molecule = molecule;
        this.country = country;
        this.tradeType = tradeType;
        this.volumeKg = volumeKg;
        this.valueUsd = valueUsd;
        this.year = year;
        this.month = month;
        this.hsCode = hsCode;
        this.productDescription = productDescription;
        this.partnerCountry = partnerCountry;
        this.unitPrice = unitPrice;
        this.marketShare = marketShare;
        this.tradeGrowth = tradeGrowth;
    }

    // Getters and Setters
    public String getMolecule() {
        return molecule;
    }

    public void setMolecule(String molecule) {
        this.molecule = molecule;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public double getVolumeKg() {
        return volumeKg;
    }

    public void setVolumeKg(double volumeKg) {
        this.volumeKg = volumeKg;
    }

    public double getValueUsd() {
        return valueUsd;
    }

    public void setValueUsd(double valueUsd) {
        this.valueUsd = valueUsd;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getHsCode() {
        return hsCode;
    }

    public void setHsCode(String hsCode) {
        this.hsCode = hsCode;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getPartnerCountry() {
        return partnerCountry;
    }

    public void setPartnerCountry(String partnerCountry) {
        this.partnerCountry = partnerCountry;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getMarketShare() {
        return marketShare;
    }

    public void setMarketShare(double marketShare) {
        this.marketShare = marketShare;
    }

    public String getTradeGrowth() {
        return tradeGrowth;
    }

    public void setTradeGrowth(String tradeGrowth) {
        this.tradeGrowth = tradeGrowth;
    }

    public boolean isImport() {
        return "Import".equalsIgnoreCase(tradeType);
    }

    public boolean isExport() {
        return "Export".equalsIgnoreCase(tradeType);
    }

    @Override
    public String toString() {
        return "EximTrade{" +
                "molecule='" + molecule + '\'' +
                ", country='" + country + '\'' +
                ", tradeType='" + tradeType + '\'' +
                ", volumeKg=" + volumeKg +
                ", valueUsd=" + valueUsd +
                ", year='" + year + '\'' +
                ", month='" + month + '\'' +
                ", hsCode='" + hsCode + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", partnerCountry='" + partnerCountry + '\'' +
                ", unitPrice=" + unitPrice +
                ", marketShare=" + marketShare +
                ", tradeGrowth='" + tradeGrowth + '\'' +
                '}';
    }
}