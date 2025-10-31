package com.mit.bodhiq.data.model;

import com.mit.bodhiq.data.database.entity.AgentResult;

/**
 * POJO representing real-time agent execution updates
 */
public class AgentUpdate {
    private String agentName;
    private AgentStatus status;
    private int progress; // 0-100 percentage
    private AgentResult result; // nullable - only set when completed
    private String error; // nullable - only set when failed
    private long timestamp;

    public AgentUpdate() {}

    public AgentUpdate(String agentName, AgentStatus status, int progress) {
        this.agentName = agentName;
        this.status = status;
        this.progress = progress;
        this.timestamp = System.currentTimeMillis();
    }

    public AgentUpdate(String agentName, AgentStatus status, int progress, 
                      AgentResult result, String error) {
        this.agentName = agentName;
        this.status = status;
        this.progress = progress;
        this.result = result;
        this.error = error;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress)); // Clamp between 0-100
    }

    public AgentResult getResult() {
        return result;
    }

    public void setResult(AgentResult result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isCompleted() {
        return status == AgentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == AgentStatus.FAILED;
    }

    public boolean isProcessing() {
        return status == AgentStatus.PROCESSING;
    }

    @Override
    public String toString() {
        return "AgentUpdate{" +
                "agentName='" + agentName + '\'' +
                ", status=" + status +
                ", progress=" + progress +
                ", result=" + result +
                ", error='" + error + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}