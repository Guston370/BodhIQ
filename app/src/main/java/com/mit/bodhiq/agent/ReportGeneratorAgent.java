package com.mit.bodhiq.agent;

import com.google.gson.JsonObject;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.model.AgentStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Agent responsible for triggering PDF report generation process
 * after all other agents have completed their analysis.
 */
@Singleton
public class ReportGeneratorAgent implements PharmaceuticalAgent {
    
    private static final String AGENT_NAME = "Report Generator";
    private static final int EXECUTION_ORDER = 7;
    private static final int ESTIMATED_DURATION_MS = 2000; // 2 seconds
    
    @Inject
    public ReportGeneratorAgent() {
        // Constructor for dependency injection
    }
    
    @Override
    public Single<AgentResult> execute(String molecule, long queryId) {
        return Single.fromCallable(() -> {
            // Simulate report generation processing time
            Thread.sleep(ESTIMATED_DURATION_MS);
            
            // Create report generation metadata
            JsonObject reportMetadata = new JsonObject();
            reportMetadata.addProperty("molecule", molecule);
            reportMetadata.addProperty("queryId", queryId);
            reportMetadata.addProperty("reportGeneratedAt", System.currentTimeMillis());
            reportMetadata.addProperty("reportFormat", "PDF");
            reportMetadata.addProperty("includesCharts", true);
            reportMetadata.addProperty("includesDataTables", true);
            reportMetadata.addProperty("status", "Report generation triggered successfully");
            
            // Create agent result
            AgentResult result = new AgentResult();
            result.setQueryId(queryId);
            result.setAgentName(AGENT_NAME);
            result.setStatus(AgentStatus.COMPLETED.name());
            result.setStartedAt(System.currentTimeMillis() - ESTIMATED_DURATION_MS);
            result.setCompletedAt(System.currentTimeMillis());
            result.setExecutionTimeMs(ESTIMATED_DURATION_MS);
            
            // Set report metadata as result data
            result.setResultData(reportMetadata.toString());
            
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