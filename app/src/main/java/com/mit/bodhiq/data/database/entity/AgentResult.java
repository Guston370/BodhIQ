package com.mit.bodhiq.data.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * Room entity representing the result of an individual agent execution.
 * Each agent result is linked to a specific query and contains the processed data.
 */
@Entity(
    tableName = "agent_results",
    foreignKeys = @ForeignKey(
        entity = Query.class,
        parentColumns = "id",
        childColumns = "query_id",
        onDelete = ForeignKey.CASCADE
    )
)
public class AgentResult {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "query_id")
    private long queryId;
    
    @ColumnInfo(name = "agent_name")
    private String agentName;
    
    @ColumnInfo(name = "status")
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    
    @ColumnInfo(name = "result_data")
    private String resultData; // JSON string containing the agent's processed data
    
    @ColumnInfo(name = "error_message")
    private String errorMessage; // Nullable - only set if agent failed
    
    @ColumnInfo(name = "execution_time_ms")
    private long executionTimeMs;
    
    @ColumnInfo(name = "started_at")
    private long startedAt;
    
    @ColumnInfo(name = "completed_at")
    private Long completedAt; // Nullable - only set when completed
    
    // Constructors
    public AgentResult() {}
    
    public AgentResult(long queryId, String agentName, String status, long startedAt) {
        this.queryId = queryId;
        this.agentName = agentName;
        this.status = status;
        this.startedAt = startedAt;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getQueryId() {
        return queryId;
    }
    
    public void setQueryId(long queryId) {
        this.queryId = queryId;
    }
    
    public String getAgentName() {
        return agentName;
    }
    
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getResultData() {
        return resultData;
    }
    
    public void setResultData(String resultData) {
        this.resultData = resultData;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public long getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }
    
    public Long getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }
    
    @Override
    public String toString() {
        return "AgentResult{" +
                "id=" + id +
                ", queryId=" + queryId +
                ", agentName='" + agentName + '\'' +
                ", status='" + status + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                '}';
    }
}