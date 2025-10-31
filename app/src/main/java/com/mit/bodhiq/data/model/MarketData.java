package com.mit.bodhiq.data.model;

import java.util.List;

/**
 * POJO representing pharmaceutical market data
 */
public class MarketData {
    private String molecule;
    private long marketSize2024; // Market size in USD millions
    private double cagr; // Compound Annual Growth Rate percentage
    private String region;
    private List<String> topIndications;
    private List<String> emergingIndications;
    private List<Competitor> competitors;
    private List<String> growthDrivers;
    private List<String> marketChallenges;
    private String marketOutlook;
    private double forecastedMarketSize2030; // Forecasted market size in USD millions

    public MarketData() {}

    public MarketData(String molecule, long marketSize2024, double cagr, String region,
                     List<String> topIndications, List<String> emergingIndications,
                     List<Competitor> competitors, List<String> growthDrivers,
                     List<String> marketChallenges, String marketOutlook,
                     double forecastedMarketSize2030) {
        this.molecule = molecule;
        this.marketSize2024 = marketSize2024;
        this.cagr = cagr;
        this.region = region;
        this.topIndications = topIndications;
        this.emergingIndications = emergingIndications;
        this.competitors = competitors;
        this.growthDrivers = growthDrivers;
        this.marketChallenges = marketChallenges;
        this.marketOutlook = marketOutlook;
        this.forecastedMarketSize2030 = forecastedMarketSize2030;
    }

    // Getters and Setters
    public String getMolecule() {
        return molecule;
    }

    public void setMolecule(String molecule) {
        this.molecule = molecule;
    }

    public long getMarketSize2024() {
        return marketSize2024;
    }

    public void setMarketSize2024(long marketSize2024) {
        this.marketSize2024 = marketSize2024;
    }

    public double getCagr() {
        return cagr;
    }

    public void setCagr(double cagr) {
        this.cagr = cagr;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<String> getTopIndications() {
        return topIndications;
    }

    public void setTopIndications(List<String> topIndications) {
        this.topIndications = topIndications;
    }

    public List<String> getEmergingIndications() {
        return emergingIndications;
    }

    public void setEmergingIndications(List<String> emergingIndications) {
        this.emergingIndications = emergingIndications;
    }

    public List<Competitor> getCompetitors() {
        return competitors;
    }

    public void setCompetitors(List<Competitor> competitors) {
        this.competitors = competitors;
    }

    public List<String> getGrowthDrivers() {
        return growthDrivers;
    }

    public void setGrowthDrivers(List<String> growthDrivers) {
        this.growthDrivers = growthDrivers;
    }

    public List<String> getMarketChallenges() {
        return marketChallenges;
    }

    public void setMarketChallenges(List<String> marketChallenges) {
        this.marketChallenges = marketChallenges;
    }

    public String getMarketOutlook() {
        return marketOutlook;
    }

    public void setMarketOutlook(String marketOutlook) {
        this.marketOutlook = marketOutlook;
    }

    public double getForecastedMarketSize2030() {
        return forecastedMarketSize2030;
    }

    public void setForecastedMarketSize2030(double forecastedMarketSize2030) {
        this.forecastedMarketSize2030 = forecastedMarketSize2030;
    }

    @Override
    public String toString() {
        return "MarketData{" +
                "molecule='" + molecule + '\'' +
                ", marketSize2024=" + marketSize2024 +
                ", cagr=" + cagr +
                ", region='" + region + '\'' +
                ", topIndications=" + topIndications +
                ", emergingIndications=" + emergingIndications +
                ", competitors=" + competitors +
                ", growthDrivers=" + growthDrivers +
                ", marketChallenges=" + marketChallenges +
                ", marketOutlook='" + marketOutlook + '\'' +
                ", forecastedMarketSize2030=" + forecastedMarketSize2030 +
                '}';
    }
}