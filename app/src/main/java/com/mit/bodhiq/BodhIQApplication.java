package com.mit.bodhiq;

import android.app.Application;
import android.util.Log;
import com.mit.bodhiq.utils.TextRecognitionService;
import com.mit.bodhiq.utils.ThemeManager;
import dagger.hilt.android.HiltAndroidApp;

/**
 * BodhIQ Application class that serves as the entry point for dependency injection.
 * This class is annotated with @HiltAndroidApp to enable Hilt dependency injection
 * throughout the application.
 */
@HiltAndroidApp
public class BodhIQApplication extends Application {
    
    private static final String TAG = "BodhIQApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize theme on app startup
        ThemeManager themeManager = new ThemeManager(this);
        themeManager.applyTheme();
        
        // Pre-download ML Kit text recognition model in background
        // This ensures the model is ready when user first scans a document
        new Thread(() -> {
            try {
                Log.d(TAG, "Pre-downloading ML Kit text recognition model...");
                TextRecognitionService textRecognitionService = new TextRecognitionService(this);
                textRecognitionService.downloadModelIfNeeded();
            } catch (Exception e) {
                Log.e(TAG, "Error pre-downloading ML Kit model", e);
            }
        }).start();
    }
}