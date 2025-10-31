package com.mit.bodhiq.ui.base;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mit.bodhiq.utils.ThemeManager;
import com.mit.bodhiq.utils.StatusBarUtils;

/**
 * Base activity that handles theme changes and provides common functionality
 */
public abstract class BaseActivity extends AppCompatActivity {
    
    protected ThemeManager themeManager;
    private int currentNightMode;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Initialize theme manager before calling super.onCreate()
        themeManager = new ThemeManager(this);
        
        // Store current night mode
        currentNightMode = getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK;
        
        super.onCreate(savedInstanceState);
        
        // Configure status bar for current theme
        StatusBarUtils.configureStatusBar(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if theme has changed while activity was paused
        int newNightMode = getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK;
        
        if (currentNightMode != newNightMode) {
            // Theme has changed, recreate activity to apply new theme
            recreate();
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // Update current night mode
        currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        
        // Handle theme change if needed
        onThemeChanged();
    }
    
    /**
     * Called when theme changes. Override in subclasses to handle theme-specific updates
     */
    protected void onThemeChanged() {
        // Update status bar for new theme
        StatusBarUtils.configureStatusBar(this);
        
        // Subclasses can override to handle additional theme changes
    }
    
    /**
     * Get the current theme manager instance
     */
    protected ThemeManager getThemeManager() {
        return themeManager;
    }
}