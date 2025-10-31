package com.mit.bodhiq.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import com.mit.bodhiq.ui.base.BaseActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mit.bodhiq.utils.AuthManager;
import com.mit.bodhiq.databinding.ActivitySplashBinding;
import com.mit.bodhiq.MainActivity;
import com.mit.bodhiq.ui.login.LoginActivity;

/**
 * SplashActivity - Entry point of the app
 * Shows app logo and checks authentication status
 * Redirects to appropriate activity based on login state
 */
public class SplashActivity extends BaseActivity {

    private ActivitySplashBinding binding;
    private FirebaseAuth mAuth;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize Firebase Auth and AuthManager
        mAuth = FirebaseAuth.getInstance();
        authManager = new AuthManager(this);
        
        // Check authentication immediately without delay
        checkAuthenticationAndRedirect();
    }

    /**
     * Check if user is authenticated and redirect accordingly
     */
    private void checkAuthenticationAndRedirect() {
        Intent intent;
        if (authManager.isUserAuthenticated()) {
            // User is signed in, go to MainActivity
            intent = new Intent(this, MainActivity.class);
        } else {
            // User is not signed in, go to Login
            intent = new Intent(this, LoginActivity.class);
        }
        
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}