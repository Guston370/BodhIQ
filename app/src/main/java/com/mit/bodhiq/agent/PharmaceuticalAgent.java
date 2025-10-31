package com.mit.bodhiq.agent;

import com.mit.bodhiq.data.database.entity.AgentResult;

import io.reactivex.rxjava3.core.Single;

/**
 * Base interface for all pharmaceutical analysis agents.
 * Each agent processes specific aspects of pharmaceutical data for a given molecule.
 */
public interface PharmaceuticalAgent {
    
    /**
     * Execute the agent's analysis for the specified molecule and query.
     * 
     * @param molecule The pharmaceutical molecule to analyze
     * @param queryId The ID of the query this execution belongs to
     * @return Single emitting the AgentResult containing processed data
     */
    Single<AgentResult> execute(String molecule, long queryId);
    
    /**
     * Get the display name of this agent.
     * 
     * @return The agent's display name for UI purposes
     */
    String getAgentName();
    
    /**
     * Get the estimated execution duration for this agent.
     * Used for progress tracking and UI feedback.
     * 
     * @return Estimated duration in milliseconds
     */
    int getEstimatedDurationMs();
    
    /**
     * Get the order/priority of this agent in the execution pipeline.
     * Lower numbers execute first.
     * 
     * @return Execution order (1-7 for the 7 agents)
     */
    int getExecutionOrder();
}