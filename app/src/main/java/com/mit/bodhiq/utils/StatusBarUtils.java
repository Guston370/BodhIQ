package com.mit.bodhiq.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.content.ContextCompat;
import com.mit.bodhiq.R;

/**
 * Utility class for managing status bar appearance based on theme
 */
public class StatusBarUtils {
    
    /**
     * Configure status bar for the current theme
     */
    public static void configureStatusBar(Activity activity) {
        if (activity == null) return;
        
        Window window = activity.getWindow();
        if (window == null) return;
        
        // Clear the FLAG_TRANSLUCENT_STATUS flag
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        
        // Add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        // Set status bar color to surface color
        int statusBarColor = ContextCompat.getColor(activity, R.color.surface);
        window.setStatusBarColor(statusBarColor);
        
        // Set navigation bar color to surface color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int navigationBarColor = ContextCompat.getColor(activity, R.color.surface);
            window.setNavigationBarColor(navigationBarColor);
        }
        
        // Configure status bar content color based on theme
        configureStatusBarContent(activity);
    }
    
    /**
     * Configure status bar content (icons and text) color based on theme
     */
    private static void configureStatusBarContent(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = activity.getWindow();
            View decorView = window.getDecorView();
            
            boolean isDarkMode = isDarkModeActive(activity);
            
            int flags = decorView.getSystemUiVisibility();
            
            if (isDarkMode) {
                // Dark mode: use light content (white icons/text)
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            } else {
                // Light mode: use dark content (dark icons/text)
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            }
            
            decorView.setSystemUiVisibility(flags);
        }
    }
    
    /**
     * Check if dark mode is currently active
     */
    private static boolean isDarkModeActive(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }
    
    /**
     * Make status bar transparent
     */
    public static void makeStatusBarTransparent(Activity activity) {
        if (activity == null) return;
        
        Window window = activity.getWindow();
        if (window == null) return;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
            
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }
}