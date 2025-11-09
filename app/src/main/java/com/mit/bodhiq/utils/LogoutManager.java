package com.mit.bodhiq.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mit.bodhiq.ui.login.LoginActivity;

/**
 * LogoutManager - Centralized logout handling
 * Ensures consistent logout behavior across the app
 */
public class LogoutManager {
    
    private static final String TAG = "LogoutManager";
    
    /**
     * Perform logout from any Activity
     */
    public static void performLogout(Activity activity, AuthManager authManager, Runnable onComplete) {
        if (activity == null || authManager == null) {
            Log.e(TAG, "Activity or AuthManager is null");
            return;
        }
        
        Log.d(TAG, "Starting logout from activity: " + activity.getClass().getSimpleName());
        
        // Perform complete logout
        authManager.performCompleteLogout(() -> {
            // Navigate to login and clear stack
            navigateToLoginAndFinish(activity);
            
            // Run completion callback if provided
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
    
    /**
     * Perform logout from any Context (Fragment, Service, etc.)
     */
    public static void performLogout(Context context, AuthManager authManager, Runnable onComplete) {
        if (context == null || authManager == null) {
            Log.e(TAG, "Context or AuthManager is null");
            return;
        }
        
        Log.d(TAG, "Starting logout from context: " + context.getClass().getSimpleName());
        
        // Perform complete logout
        authManager.performCompleteLogout(() -> {
            // Navigate to login with cleared stack
            navigateToLogin(context);
            
            // Run completion callback if provided
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
    
    /**
     * Navigate to LoginActivity and clear the entire activity stack
     */
    private static void navigateToLoginAndFinish(Activity activity) {
        Intent intent = createLoginIntent(activity);
        activity.startActivity(intent);
        
        // Finish current activity
        activity.finish();
        
        // Add smooth transition animation
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        
        Log.d(TAG, "Navigated to LoginActivity and finished current activity");
    }
    
    /**
     * Navigate to LoginActivity from any context
     */
    private static void navigateToLogin(Context context) {
        Intent intent = createLoginIntent(context);
        context.startActivity(intent);
        
        Log.d(TAG, "Navigated to LoginActivity from context");
    }
    
    /**
     * Create properly configured Intent for LoginActivity
     */
    private static Intent createLoginIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        
        // Clear the entire task stack and create a new task
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                       Intent.FLAG_ACTIVITY_CLEAR_TASK | 
                       Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Ensure user cannot navigate back to protected screens
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        
        // Add extra to indicate this is a logout navigation
        intent.putExtra("logout_navigation", true);
        
        return intent;
    }
    
    /**
     * Handle automatic logout (e.g., token expiration)
     */
    public static void handleAutomaticLogout(Context context, String reason) {
        Log.w(TAG, "Automatic logout triggered: " + reason);
        
        AuthManager authManager = new AuthManager(context);
        
        // Perform logout without user interaction
        authManager.performCompleteLogout(() -> {
            // Navigate to login
            navigateToLogin(context);
            
            Log.d(TAG, "Automatic logout completed");
        });
    }
    
    /**
     * Check if the current intent is from a logout navigation
     */
    public static boolean isLogoutNavigation(Intent intent) {
        return intent != null && intent.getBooleanExtra("logout_navigation", false);
    }
}