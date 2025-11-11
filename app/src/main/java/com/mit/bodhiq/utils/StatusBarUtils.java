package com.mit.bodhiq.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.mit.bodhiq.R;

/**
 * Utility class for managing status bar appearance based on theme
 */
public class StatusBarUtils {
    
    /**
     * Configure status bar for the current theme with enhanced contrast
     */
    public static void configureStatusBar(Activity activity) {
        if (activity == null) return;
        
        Window window = activity.getWindow();
        if (window == null) return;
        
        // Clear translucent flags and enable system bar backgrounds
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        boolean isDarkMode = isDarkModeActive(activity);
        
        // Set status bar and navigation bar colors with proper contrast
        if (isDarkMode) {
            // Dark mode: Use slightly lighter colors for contrast
            int statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_dark);
            int navigationBarColor = ContextCompat.getColor(activity, R.color.navigation_bar_dark);
            window.setStatusBarColor(statusBarColor);
            window.setNavigationBarColor(navigationBarColor);
        } else {
            // Light mode: Use slightly darker colors for contrast
            int statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_light);
            int navigationBarColor = ContextCompat.getColor(activity, R.color.navigation_bar_light);
            window.setStatusBarColor(statusBarColor);
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
                // Dark mode: use light content (white/light gray icons and text)
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            } else {
                // Light mode: use dark content (dark icons and text)
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            }
            
            decorView.setSystemUiVisibility(flags);
        }
    }
    
    /**
     * Configure status bar for edge-to-edge display with proper insets
     * This is the main method to call for proper edge-to-edge setup
     */
    public static void configureEdgeToEdge(Activity activity) {
        if (activity == null) return;
        
        Window window = activity.getWindow();
        if (window == null) return;
        
        // Enable edge-to-edge using WindowCompat (works on all API levels)
        WindowCompat.setDecorFitsSystemWindows(window, false);
        
        // Configure status bar colors and appearance
        configureStatusBar(activity);
        
        // Apply window insets to root view
        View rootView = window.getDecorView().findViewById(android.R.id.content);
        if (rootView != null) {
            applyWindowInsets(rootView);
        }
    }
    
    /**
     * Apply window insets to a view to handle system bars properly
     * This ensures content doesn't overlap with status bar or navigation bar
     */
    public static void applyWindowInsets(View view) {
        if (view == null) return;
        
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Apply padding to avoid overlap with system bars
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            if (params != null) {
                params.topMargin = insets.top;
                params.bottomMargin = insets.bottom;
                params.leftMargin = insets.left;
                params.rightMargin = insets.right;
                v.setLayoutParams(params);
            } else {
                // Fallback to padding if no layout params
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            }
            
            return WindowInsetsCompat.CONSUMED;
        });
        
        // Request insets to be applied
        ViewCompat.requestApplyInsets(view);
    }
    
    /**
     * Apply window insets with custom handling for specific edges
     * @param view The view to apply insets to
     * @param applyTop Whether to apply top inset (status bar)
     * @param applyBottom Whether to apply bottom inset (navigation bar)
     * @param applyLeft Whether to apply left inset
     * @param applyRight Whether to apply right inset
     */
    public static void applyWindowInsets(View view, boolean applyTop, boolean applyBottom, 
                                        boolean applyLeft, boolean applyRight) {
        if (view == null) return;
        
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            int top = applyTop ? insets.top : 0;
            int bottom = applyBottom ? insets.bottom : 0;
            int left = applyLeft ? insets.left : 0;
            int right = applyRight ? insets.right : 0;
            
            // Try to apply as margin first (preferred for most layouts)
            if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                params.topMargin = top;
                params.bottomMargin = bottom;
                params.leftMargin = left;
                params.rightMargin = right;
                v.setLayoutParams(params);
            } else {
                // Fallback to padding
                v.setPadding(left, top, right, bottom);
            }
            
            return WindowInsetsCompat.CONSUMED;
        });
        
        ViewCompat.requestApplyInsets(view);
    }
    
    /**
     * Apply top inset only (for status bar)
     * Useful for screens with toolbars or headers
     */
    public static void applyTopInset(View view) {
        applyWindowInsets(view, true, false, false, false);
    }
    
    /**
     * Apply bottom inset only (for navigation bar)
     * Useful for screens with bottom navigation
     */
    public static void applyBottomInset(View view) {
        applyWindowInsets(view, false, true, false, false);
    }
    
    /**
     * Apply status bar configuration with gesture navigation support
     */
    public static void configureForGestureNavigation(Activity activity) {
        if (activity == null) return;
        
        configureStatusBar(activity);
        
        // Additional configuration for gesture navigation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Window window = activity.getWindow();
            if (window != null) {
                // Ensure proper contrast in gesture navigation mode
                boolean isDarkMode = isDarkModeActive(activity);
                if (isDarkMode) {
                    window.setNavigationBarContrastEnforced(true);
                }
            }
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