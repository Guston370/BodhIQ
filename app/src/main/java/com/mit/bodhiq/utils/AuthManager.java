package com.mit.bodhiq.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.mit.bodhiq.R;
import com.mit.bodhiq.ui.login.LoginActivity;

/**
 * AuthManager - Centralized authentication management
 * Handles Firebase Auth and Google Sign-In operations
 */
public class AuthManager {
    
    private static final String TAG = "AuthManager";
    
    private final Context context;
    private final FirebaseAuth firebaseAuth;
    private final GoogleSignInClient googleSignInClient;
    
    public AuthManager(Context context) {
        this.context = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
        
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        
        this.googleSignInClient = GoogleSignIn.getClient(context, gso);
    }
    
    /**
     * Check if user is currently authenticated
     */
    public boolean isUserAuthenticated() {
        return firebaseAuth.getCurrentUser() != null;
    }
    
    /**
     * Get current Firebase user
     */
    public com.google.firebase.auth.FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    /**
     * Complete logout process - signs out from both Firebase and Google
     */
    public void logout(LogoutCallback callback) {
        Log.d(TAG, "Starting logout process...");
        
        // Sign out from Google first
        googleSignInClient.signOut()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Google sign-out successful");
                    } else {
                        Log.w(TAG, "Google sign-out failed", task.getException());
                    }
                    
                    // Sign out from Firebase
                    firebaseAuth.signOut();
                    Log.d(TAG, "Firebase sign-out completed");
                    
                    // Clear local user preferences
                    UserPreferences.clearUserData(context);
                    Log.d(TAG, "User preferences cleared");
                    
                    // Callback to notify completion
                    if (callback != null) {
                        callback.onLogoutComplete();
                    }
                });
    }
    
    /**
     * Navigate to LoginActivity and clear activity stack
     */
    public void redirectToLogin() {
        Intent intent = new Intent(context, LoginActivity.class);
        // Clear the entire task stack and create a new task
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                       Intent.FLAG_ACTIVITY_CLEAR_TASK | 
                       Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Ensure user cannot navigate back to protected screens
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        
        context.startActivity(intent);
        Log.d(TAG, "Redirected to LoginActivity with cleared task stack");
    }
    
    /**
     * Enhanced logout method that ensures complete session cleanup
     */
    public void performCompleteLogout(LogoutCallback callback) {
        Log.d(TAG, "Starting complete logout process...");
        
        // Sign out from Google first
        googleSignInClient.signOut()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Google sign-out successful");
                    } else {
                        Log.w(TAG, "Google sign-out failed", task.getException());
                    }
                    
                    // Revoke Google access to ensure complete logout
                    googleSignInClient.revokeAccess()
                            .addOnCompleteListener(revokeTask -> {
                                Log.d(TAG, "Google access revoked");
                                
                                // Sign out from Firebase
                                firebaseAuth.signOut();
                                Log.d(TAG, "Firebase sign-out completed");
                                
                                // Clear all local user data
                                clearAllUserData();
                                
                                // Callback to notify completion
                                if (callback != null) {
                                    callback.onLogoutComplete();
                                }
                            });
                });
    }
    
    /**
     * Clear all user-related data from local storage
     */
    private void clearAllUserData() {
        try {
            // Clear user preferences
            UserPreferences.clearUserData(context);
            
            // Clear any cached data (you can add more clearing logic here)
            // For example: clear database, clear image cache, etc.
            
            Log.d(TAG, "All user data cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing user data", e);
        }
    }
    
    /**
     * Complete logout and redirect to login
     */
    public void logoutAndRedirect() {
        logout(() -> redirectToLogin());
    }
    
    /**
     * Callback interface for logout completion
     */
    public interface LogoutCallback {
        void onLogoutComplete();
    }
}