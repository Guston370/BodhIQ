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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Log.d(TAG, "Redirected to LoginActivity");
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