package com.mit.bodhiq.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.mit.bodhiq.data.database.entity.Reminder;
import com.mit.bodhiq.receivers.ReminderReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Scheduler for medicine reminders using AlarmManager
 */
public class ReminderScheduler {
    
    private static final String TAG = "ReminderScheduler";
    private final Context context;
    private final AlarmManager alarmManager;
    
    public ReminderScheduler(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
    
    /**
     * Schedule all times for a reminder
     */
    public void scheduleReminder(Reminder reminder) {
        if (reminder.getTimes() == null || reminder.getTimes().isEmpty()) {
            Log.w(TAG, "No times specified for reminder: " + reminder.getId());
            return;
        }
        
        List<String> times = reminder.getTimes();
        for (int i = 0; i < times.size(); i++) {
            scheduleReminderTime(reminder, times.get(i), i);
        }
        
        Log.d(TAG, "Scheduled " + times.size() + " alarms for reminder: " + reminder.getMedicineName());
    }
    
    /**
     * Schedule a specific time for a reminder
     */
    private void scheduleReminderTime(Reminder reminder, String time, int timeIndex) {
        try {
            long triggerTime = calculateNextOccurrence(reminder, time);
            
            if (triggerTime == -1) {
                Log.w(TAG, "Reminder has ended or invalid: " + reminder.getId());
                return;
            }
            
            int requestCode = getRequestCode(reminder.getId(), timeIndex);
            
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("reminder_id", reminder.getId());
            intent.putExtra("medicine_name", reminder.getMedicineName());
            intent.putExtra("dose", reminder.getDose());
            intent.putExtra("time", time);
            intent.putExtra("time_index", timeIndex);
            intent.putExtra("snooze_minutes", reminder.getSnoozeMinutes());
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Use setExactAndAllowWhileIdle for exact timing
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                );
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                );
            }
            
            Log.d(TAG, String.format("Scheduled alarm for %s at %s (requestCode: %d)", 
                reminder.getMedicineName(), new Date(triggerTime), requestCode));
                
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling reminder time: " + time, e);
        }
    }
    
    /**
     * Calculate next occurrence time for a reminder
     */
    private long calculateNextOccurrence(Reminder reminder, String time) {
        try {
            Calendar now = Calendar.getInstance();
            
            // Set timezone if specified
            if (reminder.getTimezone() != null) {
                now.setTimeZone(TimeZone.getTimeZone(reminder.getTimezone()));
            }
            
            // Parse time (HH:mm format)
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date timeDate = timeFormat.parse(time);
            if (timeDate == null) {
                return -1;
            }
            
            Calendar reminderTime = Calendar.getInstance();
            if (reminder.getTimezone() != null) {
                reminderTime.setTimeZone(TimeZone.getTimeZone(reminder.getTimezone()));
            }
            reminderTime.setTime(timeDate);
            
            // Set today's date with reminder time
            Calendar nextOccurrence = (Calendar) now.clone();
            nextOccurrence.set(Calendar.HOUR_OF_DAY, reminderTime.get(Calendar.HOUR_OF_DAY));
            nextOccurrence.set(Calendar.MINUTE, reminderTime.get(Calendar.MINUTE));
            nextOccurrence.set(Calendar.SECOND, 0);
            nextOccurrence.set(Calendar.MILLISECOND, 0);
            
            // If time has passed today, move to next valid day
            if (nextOccurrence.before(now)) {
                nextOccurrence.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            // Apply frequency rules
            nextOccurrence = applyFrequencyRules(reminder, nextOccurrence, now);
            
            // Check if within date range
            if (reminder.getStartDate() != null && nextOccurrence.getTime().before(reminder.getStartDate())) {
                nextOccurrence.setTime(reminder.getStartDate());
            }
            
            if (reminder.getEndDate() != null && nextOccurrence.getTime().after(reminder.getEndDate())) {
                return -1; // Reminder has ended
            }
            
            return nextOccurrence.getTimeInMillis();
            
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing time: " + time, e);
            return -1;
        }
    }
    
    /**
     * Apply frequency rules to find next valid occurrence
     */
    private Calendar applyFrequencyRules(Reminder reminder, Calendar candidate, Calendar now) {
        String frequencyType = reminder.getFrequencyType();
        
        if (frequencyType == null || "daily".equals(frequencyType)) {
            return candidate; // Daily, no adjustment needed
        }
        
        if ("every_n_days".equals(frequencyType)) {
            int n = reminder.getFrequencyValue();
            if (n <= 1) {
                return candidate; // Same as daily
            }
            
            // Calculate days since start date
            if (reminder.getStartDate() != null) {
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(reminder.getStartDate());
                
                long daysSinceStart = (candidate.getTimeInMillis() - startCal.getTimeInMillis()) 
                    / (24 * 60 * 60 * 1000);
                
                long remainder = daysSinceStart % n;
                if (remainder != 0) {
                    candidate.add(Calendar.DAY_OF_MONTH, (int)(n - remainder));
                }
            }
        }
        
        if ("weekdays".equals(frequencyType)) {
            // Monday to Friday only
            while (candidate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
                   candidate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                candidate.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        
        return candidate;
    }
    
    /**
     * Cancel all alarms for a reminder
     */
    public void cancelReminder(long reminderId) {
        // Cancel up to 10 possible time slots (reasonable maximum)
        for (int i = 0; i < 10; i++) {
            int requestCode = getRequestCode(reminderId, i);
            
            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                Log.d(TAG, "Cancelled alarm with requestCode: " + requestCode);
            }
        }
        
        Log.d(TAG, "Cancelled all alarms for reminder: " + reminderId);
    }
    
    /**
     * Generate unique request code for alarm
     */
    private int getRequestCode(long reminderId, int timeIndex) {
        // Combine reminder ID and time index to create unique request code
        // Use reminder ID * 100 + timeIndex to ensure uniqueness
        return (int)(reminderId * 100 + timeIndex);
    }
    
    /**
     * Schedule snooze alarm
     */
    public void scheduleSnooze(long reminderId, String medicineName, String dose, 
                               int snoozeMinutes, int timeIndex) {
        long triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L);
        
        int requestCode = getRequestCode(reminderId, timeIndex);
        
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("reminder_id", reminderId);
        intent.putExtra("medicine_name", medicineName);
        intent.putExtra("dose", dose);
        intent.putExtra("is_snoozed", true);
        intent.putExtra("time_index", timeIndex);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            );
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            );
        }
        
        Log.d(TAG, String.format("Scheduled snooze for %s in %d minutes", 
            medicineName, snoozeMinutes));
    }
}
