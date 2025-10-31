package com.mit.bodhiq;

import android.app.Application;
import com.mit.bodhiq.utils.ThemeManager;
import dagger.hilt.android.HiltAndroidApp;

/**
 * BodhIQ Application class that serves as the entry point for dependency injection.
 * This class is annotated with @HiltAndroidApp to enable Hilt dependency injection
 * throughout the application.
 */
@HiltAndroidApp
public class BodhIQApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize theme on app startup
        ThemeManager themeManager = new ThemeManager(this);
        themeManager.applyTheme();
    }
}