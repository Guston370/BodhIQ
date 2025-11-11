package com.mit.bodhiq.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.mit.bodhiq.data.database.converters.DateConverter;
import com.mit.bodhiq.data.database.converters.StringListConverter;

import java.util.Date;
import java.util.List;

/**
 * Entity representing a medicine reminder
 */
@Entity(tableName = "reminders")
@TypeConverters({DateConverter.class, StringListConverter.class})
public class Reminder {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String userId;
    private String medicineName;
    private String dose;
    private String form; // tablet, capsule, syrup, injection, etc.
    private List<String> times; // List of times in HH:mm format
    private String frequencyType; // daily, every_n_days, weekdays, custom
    private int frequencyValue; // for every_n_days
    private Date startDate;
    private Date endDate;
    private boolean enabled;
    private String timezone;
    private int snoozeMinutes;
    private Date lastUpdated;
    private String notes;
    
    public Reminder() {
        this.enabled = true;
        this.snoozeMinutes = 10; // default 10 minutes
        this.lastUpdated = new Date();
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    
    public String getDose() { return dose; }
    public void setDose(String dose) { this.dose = dose; }
    
    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }
    
    public List<String> getTimes() { return times; }
    public void setTimes(List<String> times) { this.times = times; }
    
    public String getFrequencyType() { return frequencyType; }
    public void setFrequencyType(String frequencyType) { this.frequencyType = frequencyType; }
    
    public int getFrequencyValue() { return frequencyValue; }
    public void setFrequencyValue(int frequencyValue) { this.frequencyValue = frequencyValue; }
    
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public int getSnoozeMinutes() { return snoozeMinutes; }
    public void setSnoozeMinutes(int snoozeMinutes) { this.snoozeMinutes = snoozeMinutes; }
    
    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
