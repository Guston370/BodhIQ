package com.mit.bodhiq.data.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a pharmaceutical query in the system.
 * Each query is linked to a user and tracks the execution status.
 */
@Entity(
    tableName = "queries",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    )
)
public class Query {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "user_id")
    private long userId;
    
    @ColumnInfo(name = "query_text")
    private String queryText;
    
    @ColumnInfo(name = "molecule")
    private String molecule;
    
    @ColumnInfo(name = "status")
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "completed_at")
    private Long completedAt; // Nullable - only set when completed
    
    // Constructors
    public Query() {}
    
    public Query(long userId, String queryText, String molecule, String status, long createdAt) {
        this.userId = userId;
        this.queryText = queryText;
        this.molecule = molecule;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public String getQueryText() {
        return queryText;
    }
    
    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }
    
    public String getMolecule() {
        return molecule;
    }
    
    public void setMolecule(String molecule) {
        this.molecule = molecule;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }
    
    @Override
    public String toString() {
        return "Query{" +
                "id=" + id +
                ", userId=" + userId +
                ", queryText='" + queryText + '\'' +
                ", molecule='" + molecule + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", completedAt=" + completedAt +
                '}';
    }
}