package com.mit.bodhiq.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.mit.bodhiq.R;
import com.mit.bodhiq.MainActivity;

/**
 * Receiver for medicine reminder alarms
 */
public class ReminderReceiver extends BroadcastReceiver {
    
    private static final String TAG = "ReminderReceiver";
    private static final String CHANNEL_ID = "medicine_reminders";
    private static final String CHANNEL_NAME = "Medicine Reminders";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Reminder alarm received");
        
        long reminderId = intent.getLongExtra("reminder_id", -1);
        String medicineName = intent.getStringExtra("medicine_name");
        String dose = intent.getStringExtra("dose");
        String time = intent.getStringExtra("time");
        int timeIndex = intent.getIntExtra("time_index", 0);
        int snoozeMinutes = intent.getIntExtra("snooze_minutes", 10);
        boolean isSnoozed = intent.getBooleanExtra("is_snoozed", false);
        
        if (reminderId == -1 || medicineName == null) {
            Log.e(TAG, "Invalid reminder data");
            return;
        }
        
        // Create notification channel
        createNotificationChannel(context);
        
        // Show notification
        showNotification(context, reminderId, medicineName, dose, time, 
                        timeIndex, snoozeMinutes, isSnoozed);
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for medicine reminders");
            channel.enableVibration(true);
            channel.enableLights(true);
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private void showNotification(Context context, long reminderId, String medicineName,
                                 String dose, String time, int timeIndex, 
                                 int snoozeMinutes, boolean isSnoozed) {
        
        // Main intent - open app
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
            context, 
            (int)reminderId, 
            mainIntent, 
            PendingIntent.FLAG_IMMUTABLE
        );
        
        // Action: Taken
        Intent takenIntent = new Intent(context, NotificationActionReceiver.class);
        takenIntent.setAction("ACTION_TAKEN");
        takenIntent.putExtra("reminder_id", reminderId);
        takenIntent.putExtra("medicine_name", medicineName);
        takenIntent.putExtra("time", time);
        takenIntent.putExtra("time_index", timeIndex);
        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (int)(reminderId * 1000 + timeIndex * 10 + 1),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Action: Snooze
        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction("ACTION_SNOOZE");
        snoozeIntent.putExtra("reminder_id", reminderId);
        snoozeIntent.putExtra("medicine_name", medicineName);
        snoozeIntent.putExtra("dose", dose);
        snoozeIntent.putExtra("time_index", timeIndex);
        snoozeIntent.putExtra("snooze_minutes", snoozeMinutes);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (int)(reminderId * 1000 + timeIndex * 10 + 2),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Action: Skip
        Intent skipIntent = new Intent(context, NotificationActionReceiver.class);
        skipIntent.setAction("ACTION_SKIP");
        skipIntent.putExtra("reminder_id", reminderId);
        skipIntent.putExtra("medicine_name", medicineName);
        skipIntent.putExtra("time", time);
        skipIntent.putExtra("time_index", timeIndex);
        PendingIntent skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (int)(reminderId * 1000 + timeIndex * 10 + 3),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build notification
        String title = isSnoozed ? "Reminder (Snoozed): " + medicineName : "Time to take: " + medicineName;
        String text = dose != null ? "Dose: " + dose : "Take your medicine";
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)
            .addAction(R.drawable.ic_check_circle, "Taken", takenPendingIntent)
            .addAction(R.drawable.ic_bedtime, "Snooze", snoozePendingIntent)
            .addAction(R.drawable.ic_close, "Skip", skipPendingIntent);
        
        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = (int)(reminderId * 100 + timeIndex);
        
        try {
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification shown for: " + medicineName);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for notification", e);
        }
    }
}
