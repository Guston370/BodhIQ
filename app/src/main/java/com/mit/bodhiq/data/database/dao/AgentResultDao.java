package com.mit.bodhiq.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.mit.bodhiq.data.database.entity.AgentResult;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

/**
 * Data Access Object for AgentResult entity operations.
 * Provides result storage and retrieval methods with RxJava3 return types.
 */
@Dao
public interface AgentResultDao {
    
    /**
     * Insert a new agent result into the database.
     * 
     * @param agentResult AgentResult entity to insert
     * @return Single emitting the inserted agent result's ID
     */
    @Insert
    Single<Long> insertAgentResult(AgentResult agentResult);
    
    /**
     * Insert multiple agent results into the database.
     * 
     * @param agentResults List of AgentResult entities to insert
     * @return Completable indicating operation completion
     */
    @Insert
    Completable insertAgentResults(List<AgentResult> agentResults);
    
    /**
     * Update an existing agent result in the database.
     * 
     * @param agentResult AgentResult entity with updated information
     * @return Completable indicating operation completion
     */
    @Update
    Completable updateAgentResult(AgentResult agentResult);
    
    /**
     * Get an agent result by ID.
     * 
     * @param resultId Agent result's ID
     * @return Single emitting the AgentResult if found, error if not found
     */
    @Query("SELECT * FROM agent_results WHERE id = :resultId")
    Single<AgentResult> getAgentResultById(long resultId);
    
    /**
     * Get all agent results for a specific query.
     * 
     * @param queryId Query's ID
     * @return Flowable emitting list of agent results for the query, ordered by start time
     */
    @Query("SELECT * FROM agent_results WHERE query_id = :queryId ORDER BY started_at ASC")
    Flowable<List<AgentResult>> getAgentResultsByQueryId(long queryId);
    
    /**
     * Get agent result by query ID and agent name.
     * 
     * @param queryId Query's ID
     * @param agentName Name of the agent
     * @return Single emitting the AgentResult if found, error if not found
     */
    @Query("SELECT * FROM agent_results WHERE query_id = :queryId AND agent_name = :agentName LIMIT 1")
    Single<AgentResult> getAgentResultByQueryIdAndAgentName(long queryId, String agentName);
    
    /**
     * Get agent results by status.
     * 
     * @param status Agent result status to filter by (PENDING, PROCESSING, COMPLETED, FAILED)
     * @return Flowable emitting list of agent results with specified status
     */
    @Query("SELECT * FROM agent_results WHERE status = :status ORDER BY started_at DESC")
    Flowable<List<AgentResult>> getAgentResultsByStatus(String status);
    
    /**
     * Get agent results by agent name across all queries.
     * 
     * @param agentName Name of the agent
     * @return Flowable emitting list of results for the specified agent
     */
    @Query("SELECT * FROM agent_results WHERE agent_name = :agentName ORDER BY started_at DESC")
    Flowable<List<AgentResult>> getAgentResultsByAgentName(String agentName);
    
    /**
     * Get completed agent results for a query.
     * 
     * @param queryId Query's ID
     * @return Flowable emitting list of completed agent results
     */
    @Query("SELECT * FROM agent_results WHERE query_id = :queryId AND status = 'COMPLETED' ORDER BY started_at ASC")
    Flowable<List<AgentResult>> getCompletedAgentResultsByQueryId(long queryId);
    
    /**
     * Get failed agent results for a query.
     * 
     * @param queryId Query's ID
     * @return Flowable emitting list of failed agent results
     */
    @Query("SELECT * FROM agent_results WHERE query_id = :queryId AND status = 'FAILED' ORDER BY started_at ASC")
    Flowable<List<AgentResult>> getFailedAgentResultsByQueryId(long queryId);
    
    /**
     * Get count of agent results for a query.
     * 
     * @param queryId Query's ID
     * @return Single emitting count of agent results for the query
     */
    @Query("SELECT COUNT(*) FROM agent_results WHERE query_id = :queryId")
    Single<Integer> getAgentResultCountByQueryId(long queryId);
    
    /**
     * Get count of completed agent results for a query.
     * 
     * @param queryId Query's ID
     * @return Single emitting count of completed agent results
     */
    @Query("SELECT COUNT(*) FROM agent_results WHERE query_id = :queryId AND status = 'COMPLETED'")
    Single<Integer> getCompletedAgentResultCountByQueryId(long queryId);
    
    /**
     * Get count of failed agent results for a query.
     * 
     * @param queryId Query's ID
     * @return Single emitting count of failed agent results
     */
    @Query("SELECT COUNT(*) FROM agent_results WHERE query_id = :queryId AND status = 'FAILED'")
    Single<Integer> getFailedAgentResultCountByQueryId(long queryId);
    
    /**
     * Update agent result status by ID.
     * 
     * @param resultId Agent result's ID
     * @param status New status
     * @return Completable indicating operation completion
     */
    @Query("UPDATE agent_results SET status = :status WHERE id = :resultId")
    Completable updateAgentResultStatus(long resultId, String status);
    
    /**
     * Update agent result with completion data.
     * 
     * @param resultId Agent result's ID
     * @param status New status
     * @param resultData Result data JSON
     * @param executionTimeMs Execution time in milliseconds
     * @param completedAt Completion timestamp
     * @return Completable indicating operation completion
     */
    @Query("UPDATE agent_results SET status = :status, result_data = :resultData, execution_time_ms = :executionTimeMs, completed_at = :completedAt WHERE id = :resultId")
    Completable updateAgentResultWithCompletion(long resultId, String status, String resultData, long executionTimeMs, long completedAt);
    
    /**
     * Update agent result with error information.
     * 
     * @param resultId Agent result's ID
     * @param status New status (typically 'FAILED')
     * @param errorMessage Error message
     * @param completedAt Completion timestamp
     * @return Completable indicating operation completion
     */
    @Query("UPDATE agent_results SET status = :status, error_message = :errorMessage, completed_at = :completedAt WHERE id = :resultId")
    Completable updateAgentResultWithError(long resultId, String status, String errorMessage, long completedAt);
    
    /**
     * Delete agent results by query ID.
     * 
     * @param queryId Query's ID
     * @return Completable indicating operation completion
     */
    @Query("DELETE FROM agent_results WHERE query_id = :queryId")
    Completable deleteAgentResultsByQueryId(long queryId);
    
    /**
     * Delete an agent result by ID.
     * 
     * @param resultId ID of agent result to delete
     * @return Completable indicating operation completion
     */
    @Query("DELETE FROM agent_results WHERE id = :resultId")
    Completable deleteAgentResultById(long resultId);
    
    /**
     * Get average execution time for an agent across all queries.
     * 
     * @param agentName Name of the agent
     * @return Single emitting average execution time in milliseconds
     */
    @Query("SELECT AVG(execution_time_ms) FROM agent_results WHERE agent_name = :agentName AND status = 'COMPLETED'")
    Single<Double> getAverageExecutionTimeByAgentName(String agentName);
}