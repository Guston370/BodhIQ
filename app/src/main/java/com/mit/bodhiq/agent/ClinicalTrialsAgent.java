package com.mit.bodhiq.agent;

import com.google.gson.Gson;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.model.AgentStatus;
import com.mit.bodhiq.data.model.ClinicalTrial;
import com.mit.bodhiq.data.provider.MockDataProvider;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Agent responsible for processing clinical trials data and pipeline analysis
 * for pharmaceutical molecules.
 */
@Singleton
public class ClinicalTrialsAgent implements PharmaceuticalAgent {
    
    private static final String AGENT_NAME = "Clinical Trials";
    private static final int EXECUTION_ORDER = 3;
    private static final int ESTIMATED_DURATION_MS = 3500; // 3.5 seconds
    
    private final MockDataProvider mockDataProvider;
    private final Gson gson;
    
    @Inject
    public ClinicalTrialsAgent() {
        this.mockDataProvider = MockDataProvider.getInstance();
        this.gson = new Gson();
    }
    
    @Override
    public Single<AgentResult> execute(String molecule, long queryId) {
        return Single.fromCallable(() -> {
            // Simulate processing time
            Thread.sleep(ESTIMATED_DURATION_MS);
            
            // Get clinical trial data for the molecule
            List<ClinicalTrial> clinicalTrialData = mockDataProvider.getClinicalTrialData(molecule);
            
            if (clinicalTrialData == null || clinicalTrialData.isEmpty()) {
                throw new RuntimeException("No clinical trial data available for molecule: " + molecule);
            }
            
            // Create agent result
            AgentResult result = new AgentResult();
            result.setQueryId(queryId);
            result.setAgentName(AGENT_NAME);
            result.setStatus(AgentStatus.COMPLETED.name());
            result.setStartedAt(System.currentTimeMillis() - ESTIMATED_DURATION_MS);
            result.setCompletedAt(System.currentTimeMillis());
            result.setExecutionTimeMs(ESTIMATED_DURATION_MS);
            
            // Convert clinical trial data to JSON
            String resultJson = gson.toJson(clinicalTrialData);
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