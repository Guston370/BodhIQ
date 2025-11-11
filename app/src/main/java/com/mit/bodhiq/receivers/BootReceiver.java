package com.mit.bodhiq.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mit.bodhiq.data.database.AppDatabase;
import com.mit.bodhiq.data.database.dao.ReminderDao;
import com.mit.bodhiq.data.database.entity.Reminder;
import com.mit.bodhiq.utils.ReminderScheduler;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * Receiver to reschedule reminders after device reboot
 */
public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device booted, rescheduling reminders");
            rescheduleAllReminders(context);
        }
    }
    
    private void rescheduleAllReminders(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                ReminderDao reminderDao = db.reminderDao();
                ReminderScheduler scheduler = new ReminderScheduler(context);
                
                // Get all enabled reminders for all users
                // Note: This is a simplified approach. In production, you might want to
                // only reschedule for the currently logged-in user
                List<Reminder> reminders = reminderDao.getEnabledByUserId("*").blockingFirst();
                
                for (Reminder reminder : reminders) {
                    scheduler.scheduleReminder(reminder);
                }
                
                Log.d(TAG, "Rescheduled " + reminders.size() + " reminders");
                
            } catch (Exception e) {
                Log.e(TAG, "Error rescheduling reminders after boot", e);
            }
        });
    }
}
