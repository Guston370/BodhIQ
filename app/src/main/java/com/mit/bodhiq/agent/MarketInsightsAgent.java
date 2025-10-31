package com.mit.bodhiq.agent;

import com.google.gson.Gson;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.model.AgentStatus;
import com.mit.bodhiq.data.model.MarketData;
import com.mit.bodhiq.data.provider.MockDataProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Agent responsible for processing market insights and competitive analysis
 * for pharmaceutical molecules.
 */
@Singleton
public class MarketInsightsAgent implements PharmaceuticalAgent {
    
    private static final String AGENT_NAME = "Market Insights";
    private static final int EXECUTION_ORDER = 1;
    private static final int ESTIMATED_DURATION_MS = 3000; // 3 seconds
    
    private final MockDataProvider mockDataProvider;
    private final Gson gson;
    
    @Inject
    public MarketInsightsAgent() {
        this.mockDataProvider = MockDataProvider.getInstance();
        this.gson = new Gson();
    }
    
    @Override
    public Single<AgentResult> execute(String molecule, long queryId) {
        return Single.fromCallable(() -> {
            // Simulate processing time
            Thread.sleep(ESTIMATED_DURATION_MS);
            
            // Get market data for the molecule
            MarketData marketData = mockDataProvider.getMarketData(molecule);
            
            if (marketData == null) {
                throw new RuntimeException("No market data available for molecule: " + molecule);
            }
            
            // Create agent result
            AgentResult result = new AgentResult();
            result.setQueryId(queryId);
            result.setAgentName(AGENT_NAME);
            result.setStatus(AgentStatus.COMPLETED.name());
            result.setStartedAt(System.currentTimeMillis() - ESTIMATED_DURATION_MS);
            result.setCompletedAt(System.currentTimeMillis());
            result.setExecutionTimeMs(ESTIMATED_DURATION_MS);
            
            // Convert market data to JSON
            String resultJson = gson.toJson(marketData);
            result.setResultData(resultJson);
            
            return result;
            
        }).subscribeOn(Schedulers.io());
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