package com.mit.bodhiq.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * UserPreferences utility class for managing user session data
 * Handles local storage of user information for quick access
 */
public class UserPreferences {
    
    private static final String PREF_NAME = "BodhIQUserPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHOTO_URL = "user_photo_url";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    /**
     * Save user information to SharedPreferences
     */
    public static void saveUserInfo(Context context, String userId, String userName, 
                                  String userEmail, String photoUrl) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_USER_PHOTO_URL, photoUrl);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        
        editor.apply();
    }

    /**
     * Get user ID
     */
    public static String getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, "");
    }

    /**
     * Get user name
     */
    public static String getUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_NAME, "");
    }

    /**
     * Get user email
     */
    public static String getUserEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Get user photo URL
     */
    public static String getUserPhotoUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_PHOTO_URL, "");
    }

    /**
     * Check if user is logged in
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Clear all user data (for logout)
     */
    public static void clearUserData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}