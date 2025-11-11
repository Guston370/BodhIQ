package com.mit.bodhiq.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mit.bodhiq.data.database.AppDatabase;
import com.mit.bodhiq.data.database.dao.ReminderDao;
import com.mit.bodhiq.data.database.dao.ReminderHistoryDao;
import com.mit.bodhiq.data.database.entity.Reminder;
import com.mit.bodhiq.data.database.entity.ReminderHistory;
import com.mit.bodhiq.utils.ReminderScheduler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * Receiver for notification action buttons (Taken, Snooze, Skip)
 */
public class NotificationActionReceiver extends BroadcastReceiver {
    
    private static final String TAG = "NotificationAction";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        
        long reminderId = intent.getLongExtra("reminder_id", -1);
        String medicineName = intent.getStringExtra("medicine_name");
        int timeIndex = intent.getIntExtra("time_index", 0);
        
        if (reminderId == -1) {
            Log.e(TAG, "Invalid reminder ID");
            return;
        }
        
        // Dismiss notification
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = (int)(reminderId * 100 + timeIndex);
        notificationManager.cancel(notificationId);
        
        // Handle action
        switch (action) {
            case "ACTION_TAKEN":
                handleTaken(context, reminderId, medicineName, intent.getStringExtra("time"));
                break;
            case "ACTION_SNOOZE":
                handleSnooze(context, reminderId, medicineName, 
                           intent.getStringExtra("dose"),
                           intent.getIntExtra("snooze_minutes", 10),
                           timeIndex);
                break;
            case "ACTION_SKIP":
                handleSkip(context, reminderId, medicineName, intent.getStringExtra("time"));
                break;
        }
    }
    
    private void handleTaken(Context context, long reminderId, String medicineName, String time) {
        Log.d(TAG, "Medicine taken: " + medicineName);
        
        // Record in history
        recordHistory(context, reminderId, medicineName, "TAKEN", time);
        
        // Reschedule next occurrence
        rescheduleReminder(context, reminderId);
        
        // Show toast
        Toast.makeText(context, "✓ " + medicineName + " marked as taken", 
            Toast.LENGTH_SHORT).show();
    }
    
    private void handleSnooze(Context context, long reminderId, String medicineName, 
                             String dose, int snoozeMinutes, int timeIndex) {
        Log.d(TAG, "Medicine snoozed: " + medicineName);
        
        // Record in history
        recordHistory(context, reminderId, medicineName, "SNOOZED", null);
        
        // Schedule snooze
        ReminderScheduler scheduler = new ReminderScheduler(context);
        scheduler.scheduleSnooze(reminderId, medicineName, dose, snoozeMinutes, timeIndex);
        
        // Show toast
        Toast.makeText(context, "⏰ Snoozed for " + snoozeMinutes + " minutes", 
            Toast.LENGTH_SHORT).show();
    }
    
    private void handleSkip(Context context, long reminderId, String medicineName, String time) {
        Log.d(TAG, "Medicine skipped: " + medicineName);
        
        // Record in history
        recordHistory(context, reminderId, medicineName, "SKIPPED", time);
        
        // Reschedule next occurrence
        rescheduleReminder(context, reminderId);
        
        // Show toast
        Toast.makeText(context, "Skipped: " + medicineName, Toast.LENGTH_SHORT).show();
    }
    
    private void recordHistory(Context context, long reminderId, String medicineName, 
                              String action, String time) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                ReminderHistoryDao historyDao = db.reminderHistoryDao();
                ReminderDao reminderDao = db.reminderDao();
                
                // Get reminder to get userId
                Reminder reminder = reminderDao.getById(reminderId).blockingGet();
                
                Date scheduledTime = null;
                if (time != null) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        scheduledTime = sdf.parse(time);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing time", e);
                    }
                }
                
                ReminderHistory history = new ReminderHistory(
                    reminderId,
                    reminder.getUserId(),
                    medicineName,
                    action,
                    scheduledTime,
                    new Date()
                );
                
                historyDao.insert(history).blockingAwait();
                Log.d(TAG, "History recorded: " + action);
                
            } catch (Exception e) {
                Log.e(TAG, "Error recording history", e);
            }
        });
    }
    
    private void rescheduleReminder(Context context, long reminderId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                ReminderDao reminderDao = db.reminderDao();
                
                Reminder reminder = reminderDao.getById(reminderId).blockingGet();
                
                if (reminder != null && reminder.isEnabled()) {
                    ReminderScheduler scheduler = new ReminderScheduler(context);
                    scheduler.scheduleReminder(reminder);
                    Log.d(TAG, "Reminder rescheduled: " + reminder.getMedicineName());
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error rescheduling reminder", e);
            }
        });
    }
}
