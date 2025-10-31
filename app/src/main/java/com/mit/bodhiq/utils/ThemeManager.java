package com.mit.bodhiq.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * ThemeManager handles dark mode theme switching and persistence
 */
public class ThemeManager {
    
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    // Theme mode constants
    public static final int MODE_SYSTEM = 0;
    public static final int MODE_LIGHT = 1;
    public static final int MODE_DARK = 2;
    
    private final SharedPreferences preferences;
    
    public ThemeManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Apply the saved theme or default to system theme
     */
    public void applyTheme() {
        int themeMode = getSavedThemeMode();
        int nightMode = convertToNightMode(themeMode);
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
    
    /**
     * Set theme mode and apply it immediately
     */
    public void setThemeMode(int themeMode) {
        // Save preference
        preferences.edit()
                .putInt(KEY_THEME_MODE, themeMode)
                .apply();
        
        // Apply theme immediately
        int nightMode = convertToNightMode(themeMode);
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
    
    /**
     * Get the currently saved theme mode
     */
    public int getSavedThemeMode() {
        return preferences.getInt(KEY_THEME_MODE, MODE_SYSTEM);
    }
    
    /**
     * Check if dark mode is currently active
     */
    public boolean isDarkModeActive(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode 
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }
    
    /**
     * Get theme mode description for UI
     */
    public String getThemeModeDescription(Context context, int themeMode) {
        switch (themeMode) {
            case MODE_LIGHT:
                return "Light mode";
            case MODE_DARK:
                return "Dark mode";
            case MODE_SYSTEM:
            default:
                return "Follow system setting";
        }
    }
    
    /**
     * Convert our theme mode constants to AppCompatDelegate night mode constants
     */
    private int convertToNightMode(int themeMode) {
        switch (themeMode) {
            case MODE_LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case MODE_DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            case MODE_SYSTEM:
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
    
    /**
     * Get the next theme mode for cycling through options
     */
    public int getNextThemeMode(int currentMode) {
        switch (currentMode) {
            case MODE_SYSTEM:
                return MODE_LIGHT;
            case MODE_LIGHT:
                return MODE_DARK;
            case MODE_DARK:
            default:
                return MODE_SYSTEM;
        }
    }
    
    /**
     * Check if user has manually set a theme preference
     */
    public boolean hasManualThemePreference() {
        return getSavedThemeMode() != MODE_SYSTEM;
    }
}