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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mit.bodhiq.MainActivity;
import com.mit.bodhiq.R;
import com.mit.bodhiq.databinding.ActivitySignupBinding;
import com.mit.bodhiq.utils.UserPreferences;

import java.util.HashMap;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * SignUpActivity handles email/password user registration using Firebase Auth
 * Features:
 * - Email/password registration
 * - Input validation (email format, password strength, password confirmation)
 * - Firebase Authentication integration
 * - User profile storage in Firestore
 * - Smooth transitions and animations
 * - Material Design 3 UI
 */
@AndroidEntryPoint
public class SignUpActivity extends BaseActivity {

    private static final String TAG = "SignUpActivity";
    private static final int MIN_PASSWORD_LENGTH = 6;
    
    // Firebase components
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    // UI Components
    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize View Binding
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize Firebase components
        initializeFirebase();
        
        // Setup UI listeners
        setupClickListeners();
        
        // Setup transitions
        setupTransitions();
    }

    /**
     * Initialize Firebase Auth and Firestore
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Firebase initialized successfully");
    }

    /**
     * Setup click listeners for UI components
     */
    private void setupClickListeners() {
        binding.btnCreateAccount.setOnClickListener(v -> attemptSignUp());
        binding.tvAlreadyHaveAccount.setOnClickListener(v -> navigateToLogin());
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
     * Attempt to create a new user account
     */
    private void attemptSignUp() {
        // Clear previous errors
        clearErrors();
        
        // Get input values
        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();
        
        // Validate inputs
        if (!validateInputs(fullName, email, password, confirmPassword)) {
            return;
        }
        
        // Show progress and create account
        showProgressBar();
        createUserAccount(fullName, email, password);
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs(String fullName, String email, String password, String confirmPassword) {
        boolean isValid = true;
        
        // Validate email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_empty_email));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }
        
        // Validate password
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            binding.tilPassword.setError(getString(R.string.error_weak_password));
            isValid = false;
        }
        
        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            isValid = false;
        }
        
        return isValid;
    }

    /**
     * Create user account with Firebase Auth
     */
    private void createUserAccount(String fullName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressBar();
                        
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User account created successfully");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                updateUserProfile(user, fullName);
                                saveUserToFirestore(user, fullName);
                                saveUserPreferences(user, fullName);
                                showSuccessMessage();
                                redirectToMainActivity();
                            }
                        } else {
                            Log.w(TAG, "Account creation failed", task.getException());
                            handleSignUpError(task.getException());
                        }
                    }
                });
    }

    /**
     * Update user profile with display name
     */
    private void updateUserProfile(FirebaseUser user, String fullName) {
        if (!TextUtils.isEmpty(fullName)) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated successfully");
                        } else {
                            Log.w(TAG, "Failed to update user profile", task.getException());
                        }
                    });
        }
    }

    /**
     * Save user profile to Firestore
     */
    private void saveUserToFirestore(FirebaseUser user, String fullName) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", user.getUid());
        userMap.put("name", !TextUtils.isEmpty(fullName) ? fullName : "");
        userMap.put("email", user.getEmail());
        userMap.put("photoUrl", "");
        userMap.put("createdAt", System.currentTimeMillis());
        userMap.put("lastLogin", System.currentTimeMillis());
        userMap.put("authProvider", "email");

        db.collection("users").document(user.getUid())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile saved to Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error saving user profile", e);
                });
    }

    /**
     * Save user preferences locally
     */
    private void saveUserPreferences(FirebaseUser user, String fullName) {
        UserPreferences.saveUserInfo(this, user.getUid(), 
                !TextUtils.isEmpty(fullName) ? fullName : "", 
                user.getEmail(), "");
    }

    /**
     * Handle sign up errors
     */
    private void handleSignUpError(Exception exception) {
        String errorMessage = getString(R.string.error_authentication_failed);
        
        if (exception != null) {
            String exceptionMessage = exception.getMessage();
            if (exceptionMessage != null) {
                if (exceptionMessage.contains("email address is already in use")) {
                    errorMessage = getString(R.string.error_email_already_exists);
                    binding.tilEmail.setError(errorMessage);
                } else if (exceptionMessage.contains("weak password")) {
                    errorMessage = getString(R.string.error_weak_password_firebase);
                    binding.tilPassword.setError(errorMessage);
                } else if (exceptionMessage.contains("network error")) {
                    errorMessage = getString(R.string.error_network);
                }
            }
        }
        
        showErrorSnackbar(errorMessage);
    }

    /**
     * Show success message
     */
    private void showSuccessMessage() {
        Snackbar.make(binding.getRoot(), getString(R.string.account_created_successfully), 
                Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.success))
                .setTextColor(getColor(R.color.white))
                .show();
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
     * Redirect to MainActivity after successful signup
     */
    private void redirectToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        // Add fade transition
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Clear all input errors
     */
    private void clearErrors() {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    /**
     * Show progress indicator
     */
    private void showProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCreateAccount.setEnabled(false);
        binding.btnCreateAccount.setText(getString(R.string.signing_in));
    }

    /**
     * Hide progress indicator
     */
    private void hideProgressBar() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnCreateAccount.setEnabled(true);
        binding.btnCreateAccount.setText(getString(R.string.create_account_button));
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