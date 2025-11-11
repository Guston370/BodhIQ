package com.mit.bodhiq.chatbot;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mit.bodhiq.ui.EmergencyQrActivity;
import com.mit.bodhiq.ui.reminders.RemindersActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles actions triggered by chatbot (reminders, emergency QR, etc.)
 */
public class ActionHandler {
    
    private static final String TAG = "ActionHandler";
    
    private final Context context;
    private DocumentAnalyzer.ReminderDetails pendingReminder;
    private boolean pendingEmergencyQR;
    private EmergencyDetector.EmergencyType pendingEmergencyType;
    
    public ActionHandler(Context context) {
        this.context = context;
    }
    
    /**
     * Set pending reminder action
     */
    public void setPendingReminderAction(DocumentAnalyzer.ReminderDetails details) {
        this.pendingReminder = details;
        Log.d(TAG, "Pending reminder set: " + details.getMedicineName());
    }
    
    /**
     * Execute pending reminder creation
     */
    public boolean executePendingReminder() {
        if (pendingReminder == null) {
            return false;
        }
        
        try {
            // Open RemindersActivity with pre-filled data
            Intent intent = new Intent(context, RemindersActivity.class);
            intent.putExtra("medicine_name", pendingReminder.getMedicineName());
            intent.putExtra("dose", pendingReminder.getDose());
            intent.putStringArrayListExtra("times", new ArrayList<>(pendingReminder.getTimes()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
            pendingReminder = null;
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to execute reminder action", e);
            return false;
        }
    }
    
    /**
     * Get active reminders (placeholder - would query ReminderRepository)
     */
    public List<String> getActiveReminders() {
        // TODO: Query ReminderRepository for actual reminders
        List<String> reminders = new ArrayList<>();
        // Placeholder data
        return reminders;
    }
    
    /**
     * Set pending emergency QR action
     */
    public void setPendingEmergencyQRAction() {
        this.pendingEmergencyQR = true;
        Log.d(TAG, "Pending emergency QR action set");
    }
    
    /**
     * Execute pending emergency QR action
     */
    public boolean executePendingEmergencyQR() {
        if (!pendingEmergencyQR) {
            return false;
        }
        
        try {
            Intent intent = new Intent(context, EmergencyQrActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
            pendingEmergencyQR = false;
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to execute emergency QR action", e);
            return false;
        }
    }
    
    /**
     * Set pending emergency action
     */
    public void setPendingEmergencyAction(EmergencyDetector.EmergencyType type) {
        this.pendingEmergencyType = type;
        Log.d(TAG, "Pending emergency action set: " + type);
    }
    
    /**
     * Execute emergency call
     */
    public boolean executeEmergencyCall() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:911"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to execute emergency call", e);
            return false;
        }
    }
    
    /**
     * Check if there's a pending action
     */
    public boolean hasPendingAction() {
        return pendingReminder != null || pendingEmergencyQR || pendingEmergencyType != null;
    }
    
    /**
     * Get pending action type
     */
    public String getPendingActionType() {
        if (pendingReminder != null) {
            return "CREATE_REMINDER";
        } else if (pendingEmergencyQR) {
            return "EMERGENCY_QR";
        } else if (pendingEmergencyType != null) {
            return "EMERGENCY_CALL";
        }
        return null;
    }
    
    /**
     * Clear all pending actions
     */
    public void clearPendingActions() {
        pendingReminder = null;
        pendingEmergencyQR = false;
        pendingEmergencyType = null;
    }
}
