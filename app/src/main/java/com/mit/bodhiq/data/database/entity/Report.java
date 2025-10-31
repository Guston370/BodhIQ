package com.mit.bodhiq.data.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a generated PDF report.
 * Each report is linked to a specific query and contains metadata about the generated PDF.
 */
@Entity(
    tableName = "reports",
    foreignKeys = @ForeignKey(
        entity = Query.class,
        parentColumns = "id",
        childColumns = "query_id",
        onDelete = ForeignKey.CASCADE
    )
)
public class Report {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "query_id")
    private long queryId;
    
    @ColumnInfo(name = "file_path")
    private String filePath;
    
    @ColumnInfo(name = "file_name")
    private String fileName;
    
    @ColumnInfo(name = "molecule")
    private String molecule;
    
    @ColumnInfo(name = "file_size_bytes")
    private long fileSizeBytes;
    
    @ColumnInfo(name = "generation_status")
    private String generationStatus; // PENDING, GENERATING, COMPLETED, FAILED
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "completed_at")
    private Long completedAt; // Nullable - only set when generation completed
    
    @ColumnInfo(name = "indication_tags")
    private String indicationTags; // JSON array of indication strings
    
    @ColumnInfo(name = "error_message")
    private String errorMessage; // Nullable - only set if generation failed
    
    // Constructors
    public Report() {}
    
    public Report(long queryId, String fileName, String molecule, String generationStatus, long createdAt) {
        this.queryId = queryId;
        this.fileName = fileName;
        this.molecule = molecule;
        this.generationStatus = generationStatus;
        this.createdAt = createdAt;
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
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getMolecule() {
        return molecule;
    }
    
    public void setMolecule(String molecule) {
        this.molecule = molecule;
    }
    
    public long getFileSizeBytes() {
        return fileSizeBytes;
    }
    
    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }
    
    public String getGenerationStatus() {
        return generationStatus;
    }
    
    public void setGenerationStatus(String generationStatus) {
        this.generationStatus = generationStatus;
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
    
    public String getIndicationTags() {
        return indicationTags;
    }
    
    public void setIndicationTags(String indicationTags) {
        this.indicationTags = indicationTags;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", queryId=" + queryId +
                ", fileName='" + fileName + '\'' +
                ", molecule='" + molecule + '\'' +
                ", fileSizeBytes=" + fileSizeBytes +
                ", generationStatus='" + generationStatus + '\'' +
                ", createdAt=" + createdAt +
                ", completedAt=" + completedAt +
                '}';
    }
}