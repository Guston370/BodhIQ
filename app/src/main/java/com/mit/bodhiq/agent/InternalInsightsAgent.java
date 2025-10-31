package com.mit.bodhiq.agent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.model.AgentStatus;
import com.mit.bodhiq.data.model.MarketData;
import com.mit.bodhiq.data.provider.MockDataProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Agent responsible for aggregating internal data sources and generating
 * comprehensive insights for pharmaceutical molecules.
 */
@Singleton
public class InternalInsightsAgent implements PharmaceuticalAgent {
    
    private static final String AGENT_NAME = "Internal Insights";
    private static final int EXECUTION_ORDER = 6;
    private static final int ESTIMATED_DURATION_MS = 3000; // 3 seconds
    
    private final MockDataProvider mockDataProvider;
    private final Gson gson;
    
    @Inject
    public InternalInsightsAgent() {
        this.mockDataProvider = MockDataProvider.getInstance();
        this.gson = new Gson();
    }
    
    @Override
    public Single<AgentResult> execute(String molecule, long queryId) {
        return Single.fromCallable(() -> {
            // Simulate processing time
            Thread.sleep(ESTIMATED_DURATION_MS);
            
            // Get market data for internal analysis
            MarketData marketData = mockDataProvider.getMarketData(molecule);
            
            if (marketData == null) {
                throw new RuntimeException("No internal data available for molecule: " + molecule);
            }
            
            // Create internal insights summary
            JsonObject internalInsights = new JsonObject();
            internalInsights.addProperty("molecule", molecule);
            internalInsights.addProperty("analysisDate", System.currentTimeMillis());
            internalInsights.addProperty("marketSize2024", marketData.getMarketSize2024());
            internalInsights.addProperty("projectedSize2030", marketData.getForecastedMarketSize2030());
            internalInsights.addProperty("cagr", marketData.getCagr());
            internalInsights.addProperty("competitorCount", marketData.getCompetitors().size());
            internalInsights.addProperty("primaryIndications", marketData.getTopIndications().size());
            internalInsights.addProperty("emergingIndications", marketData.getEmergingIndications().size());
            
            // Add strategic recommendations
            JsonObject recommendations = new JsonObject();
            recommendations.addProperty("marketPosition", getMarketPositionRecommendation(marketData));
            recommendations.addProperty("growthStrategy", getGrowthStrategyRecommendation(marketData));
            recommendations.addProperty("riskAssessment", getRiskAssessment(marketData));
            internalInsights.add("recommendations", recommendations);
            
            // Create agent result
            AgentResult result = new AgentResult();
            result.setQueryId(queryId);
            result.setAgentName(AGENT_NAME);
            result.setStatus(AgentStatus.COMPLETED.name());
            result.setStartedAt(System.currentTimeMillis() - ESTIMATED_DURATION_MS);
            result.setCompletedAt(System.currentTimeMillis());
            result.setExecutionTimeMs(ESTIMATED_DURATION_MS);
            
            // Convert internal insights to JSON
            String resultJson = gson.toJson(internalInsights);
            result.setResultData(resultJson);
            
            return result;
            
        }).subscribeOn(Schedulers.io());
    }
    
    private String getMarketPositionRecommendation(MarketData marketData) {
        if (marketData.getCagr() > 10.0) {
            return "High-growth market - Consider aggressive expansion strategy";
        } else if (marketData.getCagr() > 5.0) {
            return "Moderate growth - Focus on market share capture";
        } else {
            return "Mature market - Emphasize cost optimization and differentiation";
        }
    }
    
    private String getGrowthStrategyRecommendation(MarketData marketData) {
        if (marketData.getEmergingIndications().size() > 2) {
            return "Pursue emerging indication development for market expansion";
        } else if (marketData.getCompetitors().size() < 3) {
            return "Limited competition - Accelerate market penetration";
        } else {
            return "Competitive market - Focus on product differentiation";
        }
    }
    
    private String getRiskAssessment(MarketData marketData) {
        if (marketData.getMarketChallenges().contains("Generic competition")) {
            return "High risk - Generic competition threat requires patent strategy";
        } else if (marketData.getMarketChallenges().contains("High cost")) {
            return "Medium risk - Cost pressures may impact market access";
        } else {
            return "Low risk - Favorable market conditions for growth";
        }
    }
    
    @Override
    public String getAgentName() {
        return AGENT_NAME;
    }
    
    @Override
    public int getEstimatedDurationMs() {
        return ESTIMATED_DURATION_MS;
    }
    
    @Override
    public int getExecutionOrder() {
        return EXECUTION_ORDER;
    }
}