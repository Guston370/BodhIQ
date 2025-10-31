package com.mit.bodhiq.utils;

import android.content.Context;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Utility class for managing application preferences using DataStore.
 * Provides type-safe access to user preferences and settings.
 */
@Singleton
public class PreferenceManager {
    
    private static final String PREFERENCES_NAME = "bodhiq_preferences";
    
    // Preference keys
    private static final Preferences.Key<String> KEY_USER_EMAIL = PreferencesKeys.stringKey("user_email");
    private static final Preferences.Key<String> KEY_USER_NAME = PreferencesKeys.stringKey("user_name");
    private static final Preferences.Key<String> KEY_USER_ROLE = PreferencesKeys.stringKey("user_role");
    private static final Preferences.Key<Long> KEY_USER_ID = PreferencesKeys.longKey("user_id");
    private static final Preferences.Key<Boolean> KEY_IS_LOGGED_IN = PreferencesKeys.booleanKey("is_logged_in");
    private static final Preferences.Key<Long> KEY_LAST_LOGIN_TIME = PreferencesKeys.longKey("last_login_time");
    private static final Preferences.Key<Boolean> KEY_FIRST_TIME_USER = PreferencesKeys.booleanKey("first_time_user");
    private static final Preferences.Key<String> KEY_THEME_MODE = PreferencesKeys.stringKey("theme_mode");
    private static final Preferences.Key<Boolean> KEY_NOTIFICATIONS_ENABLED = PreferencesKeys.booleanKey("notifications_enabled");
    private static final Preferences.Key<String> KEY_DEFAULT_REPORT_FORMAT = PreferencesKeys.stringKey("default_report_format");
    private static final Preferences.Key<Boolean> KEY_AUTO_GENERATE_REPORTS = PreferencesKeys.booleanKey("auto_generate_reports");
    private static final Preferences.Key<Integer> KEY_QUERY_HISTORY_LIMIT = PreferencesKeys.intKey("query_history_limit");
    
    private final RxDataStore<Preferences> dataStore;
    
    @Inject
    public PreferenceManager(Context context) {
        this.dataStore = new RxPreferenceDataStoreBuilder(context, PREFERENCES_NAME).build();
    }
    
    // User Authentication Preferences
    
    /**
     * Saves user login information.
     */
    public Single<Preferences> saveUserLogin(long userId, String email, String name, String role) {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_USER_ID, userId);
            mutablePreferences.set(KEY_USER_EMAIL, email);
            mutablePreferences.set(KEY_USER_NAME, name);
            mutablePreferences.set(KEY_USER_ROLE, role);
            mutablePreferences.set(KEY_IS_LOGGED_IN, true);
            mutablePreferences.set(KEY_LAST_LOGIN_TIME, System.currentTimeMillis());
            mutablePreferences.set(KEY_FIRST_TIME_USER, false);
            return Single.just(mutablePreferences);
        });
    }
    
    /**
     * Clears user login information (logout).
     */
    public Single<Preferences> clearUserLogin() {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.remove(KEY_USER_ID);
            mutablePreferences.remove(KEY_USER_EMAIL);
            mutablePreferences.remove(KEY_USER_NAME);
            mutablePreferences.remove(KEY_USER_ROLE);
            mutablePreferences.set(KEY_IS_LOGGED_IN, false);
            return Single.just(mutablePreferences);
        });
    }
    
    /**
     * Checks if user is logged in.
     */
    public Flowable<Boolean> isLoggedIn() {
        return dataStore.data().map(preferences -> preferences.get(KEY_IS_LOGGED_IN) != null && 
                                                  preferences.get(KEY_IS_LOGGED_IN));
    }
    
    /**
     * Gets current user ID.
     */
    public Flowable<Long> getUserId() {
        return dataStore.data().map(preferences -> preferences.get(KEY_USER_ID) != null ? 
                                                  preferences.get(KEY_USER_ID) : -1L);
    }
    
    /**
     * Gets current user email.
     */
    public Flowable<String> getUserEmail() {
        return dataStore.data().map(preferences -> preferences.get(KEY_USER_EMAIL) != null ? 
                                                  preferences.get(KEY_USER_EMAIL) : "");
    }
    
    /**
     * Gets current user name.
     */
    public Flowable<String> getUserName() {
        return dataStore.data().map(preferences -> preferences.get(KEY_USER_NAME) != null ? 
                                                  preferences.get(KEY_USER_NAME) : "");
    }
    
    /**
     * Gets current user role.
     */
    public Flowable<String> getUserRole() {
        return dataStore.data().map(preferences -> preferences.get(KEY_USER_ROLE) != null ? 
                                                  preferences.get(KEY_USER_ROLE) : "");
    }  
  
    /**
     * Gets last login time.
     */
    public Flowable<Long> getLastLoginTime() {
        return dataStore.data().map(preferences -> preferences.get(KEY_LAST_LOGIN_TIME) != null ? 
                                                  preferences.get(KEY_LAST_LOGIN_TIME) : 0L);
    }
    
    // Application Settings
    
    /**
     * Checks if this is a first-time user.
     */
    public Flowable<Boolean> isFirstTimeUser() {
        return dataStore.data().map(preferences -> preferences.get(KEY_FIRST_TIME_USER) == null || 
                                                  preferences.get(KEY_FIRST_TIME_USER));
    }
    
    /**
     * Sets theme mode preference.
     */
    public Single<Preferences> setThemeMode(String themeMode) {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_THEME_MODE, themeMode);
            return Single.just(mutablePreferences);
        });
    }
    
    /**
     * Gets theme mode preference.
     */
    public Flowable<String> getThemeMode() {
        return dataStore.data().map(preferences -> preferences.get(KEY_THEME_MODE) != null ? 
                                                  preferences.get(KEY_THEME_MODE) : "system");
    }
    
    /**
     * Sets notifications enabled preference.
     */
    public Single<Preferences> setNotificationsEnabled(boolean enabled) {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_NOTIFICATIONS_ENABLED, enabled);
            return Single.just(mutablePreferences);
        });
    }
    
    /**
     * Gets notifications enabled preference.
     */
    public Flowable<Boolean> areNotificationsEnabled() {
        return dataStore.data().map(preferences -> preferences.get(KEY_NOTIFICATIONS_ENABLED) == null || 
                                                  preferences.get(KEY_NOTIFICATIONS_ENABLED));
    }
    
    /**
     * Sets default report format preference.
     */
    public Single<Preferences> setDefaultReportFormat(String format) {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_DEFAULT_REPORT_FORMAT, format);
            return Single.just(mutablePreferences);
        });
    }
    
    /**
     * Gets default report format preference.
     */
    public Flowable<String> getDefaultReportFormat() {
        return dataStore.data().map(preferences -> preferences.get(KEY_DEFAULT_REPORT_FORMAT) != null ? 
                                                  preferences.get(KEY_DEFAULT_REPORT_FORMAT) : "PDF");
    }
    
    /**
     * Sets auto-generate reports preference.
     */
    public Single<Preferences> setAutoGenerateReports(boolean autoGenerate) {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_AUTO_GENERATE_REPORTS, autoGenerate);
            return Single.just(mutablePreferences);
        });
    }
    
    /**
     * Gets auto-generate reports preference.
     */
    public Flowable<Boolean> isAutoGenerateReports() {
        return dataStore.data().map(preferences -> preferences.get(KEY_AUTO_GENERATE_REPORTS) != null && 
                                                  preferences.get(KEY_AUTO_GENERATE_REPORTS));
    }
    
    /**
     * Sets query history limit preference.
     */
    public Single<Preferences> setQueryHistoryLimit(int limit) {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(KEY_QUERY_HISTORY_LIMIT, limit);
            return Single.just(mutablePreferences);
        });
    }
    
    /**
     * Gets query history limit preference.
     */
    public Flowable<Integer> getQueryHistoryLimit() {
        return dataStore.data().map(preferences -> preferences.get(KEY_QUERY_HISTORY_LIMIT) != null ? 
                                                  preferences.get(KEY_QUERY_HISTORY_LIMIT) : 50);
    }    

    // Utility Methods
    
    /**
     * Clears all preferences (factory reset).
     */
    public Single<Preferences> clearAllPreferences() {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.clear();
            return Single.just(mutablePreferences);
        });
    }
    
    /**
     * Gets a single preference value synchronously (for immediate access).
     * Note: This should be used sparingly as it blocks the thread.
     */
    public String getStringPreferenceSync(Preferences.Key<String> key, String defaultValue) {
        try {
            return dataStore.data().blockingFirst().get(key) != null ? 
                   dataStore.data().blockingFirst().get(key) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Gets a boolean preference value synchronously.
     */
    public boolean getBooleanPreferenceSync(Preferences.Key<Boolean> key, boolean defaultValue) {
        try {
            return dataStore.data().blockingFirst().get(key) != null ? 
                   dataStore.data().blockingFirst().get(key) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Gets a long preference value synchronously.
     */
    public long getLongPreferenceSync(Preferences.Key<Long> key, long defaultValue) {
        try {
            return dataStore.data().blockingFirst().get(key) != null ? 
                   dataStore.data().blockingFirst().get(key) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Gets an integer preference value synchronously.
     */
    public int getIntPreferenceSync(Preferences.Key<Integer> key, int defaultValue) {
        try {
            return dataStore.data().blockingFirst().get(key) != null ? 
                   dataStore.data().blockingFirst().get(key) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    // Constants for theme modes
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";
    
    // Constants for report formats
    public static final String REPORT_FORMAT_PDF = "PDF";
    public static final String REPORT_FORMAT_HTML = "HTML";
    
    // Default values
    public static final int DEFAULT_QUERY_HISTORY_LIMIT = 50;
    public static final boolean DEFAULT_NOTIFICATIONS_ENABLED = true;
    public static final boolean DEFAULT_AUTO_GENERATE_REPORTS = false;
    
    // Convenience methods for synchronous access (used in ViewModels)
    
    /**
     * Gets current user email synchronously.
     */
    public String getCurrentUserEmail() {
        return getStringPreferenceSync(KEY_USER_EMAIL, "");
    }
    
    /**
     * Gets current user ID synchronously.
     */
    public long getCurrentUserId() {
        return getLongPreferenceSync(KEY_USER_ID, -1L);
    }
    
    /**
     * Checks if user is logged in synchronously.
     */
    public boolean isCurrentlyLoggedIn() {
        return getBooleanPreferenceSync(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Clears user session (convenience method for logout).
     */
    public void clearUserSession() {
        clearUserLogin().subscribe();
    }
}