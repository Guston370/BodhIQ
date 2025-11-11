package com.mit.bodhiq.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.mit.bodhiq.data.database.converters.DateConverter;

import java.util.Date;

/**
 * Entity representing reminder history (user actions)
 */
@Entity(tableName = "reminder_history")
@TypeConverters(DateConverter.class)
public class ReminderHistory {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long reminderId;
    private String userId;
    private String medicineName;
    private String action; // TAKEN, SKIPPED, SNOOZED
    private Date scheduledTime;
    private Date actionTime;
    private String notes;
    
    public ReminderHistory() {}
    
    public ReminderHistory(long reminderId, String userId, String medicineName, 
                          String action, Date scheduledTime, Date actionTime) {
        this.reminderId = reminderId;
        this.userId = userId;
        this.medicineName = medicineName;
        this.action = action;
        this.scheduledTime = scheduledTime;
        this.actionTime = actionTime;
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getReminderId() { return reminderId; }
    public void setReminderId(long reminderId) { this.reminderId = reminderId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public Date getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Date scheduledTime) { this.scheduledTime = scheduledTime; }
    
    public Date getActionTime() { return actionTime; }
    public void setActionTime(Date actionTime) { this.actionTime = actionTime; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
