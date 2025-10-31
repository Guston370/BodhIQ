package com.mit.bodhiq.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.mit.bodhiq.ui.base.BaseActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.mit.bodhiq.R;
import com.mit.bodhiq.databinding.ActivityForgotPasswordBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * ForgotPasswordActivity handles password reset functionality using Firebase Auth
 * Features:
 * - Email validation
 * - Firebase password reset email
 * - Success/error message display with animations
 * - Material Design 3 UI
 * - Smooth transitions
 */
@AndroidEntryPoint
public class ForgotPasswordActivity extends BaseActivity {

    private static final String TAG = "ForgotPasswordActivity";
    
    // Firebase components
    private FirebaseAuth mAuth;
    
    // UI Components
    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize View Binding
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize Firebase components
        initializeFirebase();
        
        // Setup UI listeners
        setupClickListeners();
        
        // Setup transitions
        setupTransitions();
    }

    /**
     * Initialize Firebase Auth
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "Firebase initialized successfully");
    }   
 /**
     * Setup click listeners for UI components
     */
    private void setupClickListeners() {
        binding.btnSendResetLink.setOnClickListener(v -> attemptPasswordReset());
        binding.tvBackToLogin.setOnClickListener(v -> navigateToLogin());
    }

    /**
     * Setup smooth transitions
     */
    private void setupTransitions() {
        // Enable activity transitions
        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().setAllowReturnTransitionOverlap(true);
    }

    /**
     * Attempt to send password reset email
     */
    private void attemptPasswordReset() {
        // Clear previous errors
        clearErrors();
        
        // Get email input
        String email = binding.etEmail.getText().toString().trim();
        
        // Validate email
        if (!validateEmail(email)) {
            return;
        }
        
        // Show progress and send reset email
        showProgressBar();
        sendPasswordResetEmail(email);
    }

    /**
     * Validate email input
     */
    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_empty_email));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            return false;
        }
        return true;
    }

    /**
     * Send password reset email using Firebase Auth
     */
    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideProgressBar();
                        
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password reset email sent successfully");
                            showSuccessMessage(email);
                        } else {
                            Log.w(TAG, "Failed to send password reset email", task.getException());
                            handleResetError(task.getException());
                        }
                    }
                });
    }    /*
*
     * Show success message with animation
     */
    private void showSuccessMessage(String email) {
        // Show success message card
        binding.cvMessageContainer.setVisibility(View.VISIBLE);
        binding.ivMessageIcon.setImageResource(R.drawable.ic_check_circle);
        binding.ivMessageIcon.setColorFilter(getColor(R.color.success));
        binding.tvMessage.setText(getString(R.string.password_reset_sent));
        binding.cvMessageContainer.setCardBackgroundColor(getColor(R.color.success_container));
        
        // Animate the message card
        binding.cvMessageContainer.setAlpha(0f);
        binding.cvMessageContainer.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
        
        // Also show snackbar
        Snackbar.make(binding.getRoot(), getString(R.string.check_email_for_reset), 
                Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.success))
                .setTextColor(getColor(R.color.white))
                .show();
    }

    /**
     * Handle password reset errors
     */
    private void handleResetError(Exception exception) {
        String errorMessage = getString(R.string.error_network);
        
        if (exception != null) {
            String exceptionMessage = exception.getMessage();
            if (exceptionMessage != null) {
                if (exceptionMessage.contains("no user record")) {
                    errorMessage = "No account found with this email address";
                    binding.tilEmail.setError(errorMessage);
                } else if (exceptionMessage.contains("network error")) {
                    errorMessage = getString(R.string.error_network);
                }
            }
        }
        
        // Show error message card
        binding.cvMessageContainer.setVisibility(View.VISIBLE);
        binding.ivMessageIcon.setImageResource(R.drawable.ic_error);
        binding.ivMessageIcon.setColorFilter(getColor(R.color.error));
        binding.tvMessage.setText(errorMessage);
        binding.cvMessageContainer.setCardBackgroundColor(getColor(R.color.error_container));
        
        // Animate the message card
        binding.cvMessageContainer.setAlpha(0f);
        binding.cvMessageContainer.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
        
        // Also show snackbar
        showErrorSnackbar(errorMessage);
    }   
 /**
     * Show error message with animation
     */
    private void showErrorSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.error))
                .setTextColor(getColor(R.color.white))
                .setAction("DISMISS", v -> {})
                .show();
    }

    /**
     * Navigate back to LoginActivity
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        
        // Add slide transition
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Clear all input errors
     */
    private void clearErrors() {
        binding.tilEmail.setError(null);
        binding.cvMessageContainer.setVisibility(View.GONE);
    }

    /**
     * Show progress indicator
     */
    private void showProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSendResetLink.setEnabled(false);
        binding.btnSendResetLink.setText("Sending...");
    }

    /**
     * Hide progress indicator
     */
    private void hideProgressBar() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSendResetLink.setEnabled(true);
        binding.btnSendResetLink.setText(getString(R.string.send_reset_link_button));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Add slide transition when going back
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}