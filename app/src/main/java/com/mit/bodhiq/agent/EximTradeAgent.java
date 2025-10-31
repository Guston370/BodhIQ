package com.mit.bodhiq.agent;

import com.google.gson.Gson;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.model.AgentStatus;
import com.mit.bodhiq.data.model.EximTrade;
import com.mit.bodhiq.data.provider.MockDataProvider;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Agent responsible for processing export-import trade data and analysis
 * for pharmaceutical molecules.
 */
@Singleton
public class EximTradeAgent implements PharmaceuticalAgent {
    
    private static final String AGENT_NAME = "EXIM Trade";
    private static final int EXECUTION_ORDER = 4;
    private static final int ESTIMATED_DURATION_MS = 2500; // 2.5 seconds
    
    private final MockDataProvider mockDataProvider;
    private final Gson gson;
    
    @Inject
    public EximTradeAgent() {
        this.mockDataProvider = MockDataProvider.getInstance();
        this.gson = new Gson();
    }
    
    @Override
    public Single<AgentResult> execute(String molecule, long queryId) {
        return Single.fromCallable(() -> {
            // Simulate processing time
            Thread.sleep(ESTIMATED_DURATION_MS);
            
            // Get EXIM trade data for the molecule
            List<EximTrade> eximTradeData = mockDataProvider.getEximTradeData(molecule);
            
            if (eximTradeData == null || eximTradeData.isEmpty()) {
                throw new RuntimeException("No EXIM trade data available for molecule: " + molecule);
            }
            
            // Create agent result
            AgentResult result = new AgentResult();
            result.setQueryId(queryId);
            result.setAgentName(AGENT_NAME);
            result.setStatus(AgentStatus.COMPLETED.name());
            result.setStartedAt(System.currentTimeMillis() - ESTIMATED_DURATION_MS);
            result.setCompletedAt(System.currentTimeMillis());
            result.setExecutionTimeMs(ESTIMATED_DURATION_MS);
            
            // Convert EXIM trade data to JSON
            String resultJson = gson.toJson(eximTradeData);
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