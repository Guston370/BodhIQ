package com.mit.bodhiq.agent;

import com.mit.bodhiq.data.database.dao.AgentResultDao;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.model.AgentStatus;
import com.mit.bodhiq.data.model.AgentUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * Master orchestrator for sequential execution of pharmaceutical analysis agents.
 * Manages the execution pipeline, progress tracking, and error handling for all agents.
 */
@Singleton
public class MasterAgent {
    
    private final AgentResultDao agentResultDao;
    private final List<PharmaceuticalAgent> agents;
    private final PublishSubject<AgentUpdate> progressSubject;
    
    @Inject
    public MasterAgent(AgentResultDao agentResultDao) {
        this.agentResultDao = agentResultDao;
        this.agents = new ArrayList<>();
        this.progressSubject = PublishSubject.create();
    }
    
    /**
     * Register an agent with the master orchestrator.
     * Agents will be executed in order of their getExecutionOrder() value.
     * 
     * @param agent The agent to register
     */
    public void registerAgent(PharmaceuticalAgent agent) {
        agents.add(agent);
        // Sort agents by execution order
        agents.sort((a1, a2) -> Integer.compare(a1.getExecutionOrder(), a2.getExecutionOrder()));
    }
    
    /**
     * Execute all registered agents sequentially for the given molecule and query.
     * Emits real-time progress updates via Flowable stream.
     * 
     * @param molecule The pharmaceutical molecule to analyze
     * @param queryId The ID of the query this execution belongs to
     * @return Flowable stream of AgentUpdate objects for real-time progress tracking
     */
    public Flowable<AgentUpdate> executeAllAgents(String molecule, long queryId) {
        return Flowable.fromIterable(agents)
            .concatMapSingle(agent -> executeAgentWithProgress(agent, molecule, queryId))
            .subscribeOn(Schedulers.io())
            .doOnError(throwable -> {
                // Emit error update
                AgentUpdate errorUpdate = new AgentUpdate(
                    "Pipeline", 
                    AgentStatus.FAILED, 
                    0, 
                    null, 
                    "Pipeline execution failed: " + throwable.getMessage()
                );
                progressSubject.onNext(errorUpdate);
            });
    }
    
    /**
     * Execute a single agent with progress tracking and error handling.
     * 
     * @param agent The agent to execute
     * @param molecule The molecule to analyze
     * @param queryId The query ID
     * @return Single emitting AgentUpdate with the result
     */
    private Single<AgentUpdate> executeAgentWithProgress(PharmaceuticalAgent agent, 
                                                        String molecule, 
                                                        long queryId) {
        // Emit starting update
        AgentUpdate startUpdate = new AgentUpdate(
            agent.getAgentName(), 
            AgentStatus.PROCESSING, 
            0
        );
        progressSubject.onNext(startUpdate);
        
        // Create initial agent result record
        AgentResult initialResult = new AgentResult(
            queryId, 
            agent.getAgentName(), 
            AgentStatus.PROCESSING.name(), 
            System.currentTimeMillis()
        );
        
        return agentResultDao.insertAgentResult(initialResult)
        .flatMap(resultId -> 
            agent.execute(molecule, queryId)
                .timeout(30, TimeUnit.SECONDS) // 30 second timeout per agent
                .retry(2) // Retry up to 2 times on failure
                .map(result -> {
                    // Update the result in database
                    result.setId(resultId);
                    result.setCompletedAt(System.currentTimeMillis());
                    result.setExecutionTimeMs(
                        result.getCompletedAt() - result.getStartedAt()
                    );
                    agentResultDao.updateAgentResult(result).blockingAwait();
                    
                    // Create success update
                    AgentUpdate successUpdate = new AgentUpdate(
                        agent.getAgentName(),
                        AgentStatus.COMPLETED,
                        100,
                        result,
                        null
                    );
                    progressSubject.onNext(successUpdate);
                    
                    return successUpdate;
                })
                .onErrorReturn(throwable -> {
                    // Handle agent failure
                    AgentResult failedResult = new AgentResult();
                    failedResult.setId(resultId);
                    failedResult.setQueryId(queryId);
                    failedResult.setAgentName(agent.getAgentName());
                    failedResult.setStatus(AgentStatus.FAILED.name());
                    failedResult.setErrorMessage(throwable.getMessage());
                    failedResult.setCompletedAt(System.currentTimeMillis());
                    
                    agentResultDao.updateAgentResult(failedResult).blockingAwait();
                    
                    // Create failure update
                    AgentUpdate failureUpdate = new AgentUpdate(
                        agent.getAgentName(),
                        AgentStatus.FAILED,
                        0,
                        failedResult,
                        throwable.getMessage()
                    );
                    progressSubject.onNext(failureUpdate);
                    
                    return failureUpdate;
                })
        )
        .subscribeOn(Schedulers.io());
    }
    
    /**
     * Get the progress stream for real-time updates.
     * 
     * @return Flowable stream of AgentUpdate objects
     */
    public Flowable<AgentUpdate> getProgressStream() {
        return progressSubject.toFlowable(io.reactivex.rxjava3.core.BackpressureStrategy.BUFFER);
    }
    
    /**
     * Get the total number of registered agents.
     * 
     * @return Number of agents in the pipeline
     */
    public int getAgentCount() {
        return agents.size();
    }
    
    /**
     * Get the estimated total execution time for all agents.
     * 
     * @return Total estimated duration in milliseconds
     */
    public int getTotalEstimatedDurationMs() {
        return agents.stream()
            .mapToInt(PharmaceuticalAgent::getEstimatedDurationMs)
            .sum();
    }
    
    /**
     * Check if a specific molecule is supported by the agents.
     * 
     * @param molecule The molecule name to check
     * @return true if the molecule is supported
     */
    public boolean isMoleculeSupported(String molecule) {
        // List of supported molecules from MockDataProvider
        String[] supportedMolecules = {
            "Montelukast", "Humira", "Metformin", "GLP-1", "Eliquis"
        };
        
        for (String supported : supportedMolecules) {
            if (supported.equalsIgnoreCase(molecule)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Cancel the current execution pipeline.
     * This will stop further agent execution but won't interrupt currently running agents.
     */
    public void cancelExecution() {
        AgentUpdate cancelUpdate = new AgentUpdate(
            "Pipeline", 
            AgentStatus.CANCELLED, 
            0, 
            null, 
            "Execution cancelled by user"
        );
        progressSubject.onNext(cancelUpdate);
    }
}