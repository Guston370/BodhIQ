package com.mit.bodhiq.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.mit.bodhiq.ui.base.BaseActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mit.bodhiq.R;
import com.mit.bodhiq.databinding.ActivityLoginBinding;
import com.mit.bodhiq.MainActivity;
import com.mit.bodhiq.ui.login.SignUpActivity;
import com.mit.bodhiq.ui.login.ForgotPasswordActivity;

import com.mit.bodhiq.utils.UserPreferences;
import com.mit.bodhiq.utils.GoogleSignInDiagnostics;
import com.mit.bodhiq.utils.LogoutManager;

import java.util.HashMap;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * LoginActivity handles Google Sign-In authentication using Firebase Auth
 * Features:
 * - Google Sign-In integration
 * - Firebase Authentication
 * - User profile storage in Firestore
 * - Auto-redirect for logged-in users
 * - Comprehensive error handling
 */
@AndroidEntryPoint
public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";
    
    // Firebase & Google Sign-In
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;
    
    // AuthStateListener for real-time authentication monitoring
    private FirebaseAuth.AuthStateListener authStateListener;
    
    // UI Components
    private ActivityLoginBinding binding;
    
    // Activity Result Launcher for Google Sign-In
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize View Binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Apply window insets for edge-to-edge display
        applyTopInset(binding.getRoot());
        
        // Initialize Firebase components
        initializeFirebase();
        
        // Print diagnostics for troubleshooting
        GoogleSignInDiagnostics.printDiagnostics(this);
        
        // Configure Google Sign-In
        configureGoogleSignIn();
        
        // Setup Activity Result Launcher
        setupGoogleSignInLauncher();
        
        // Setup AuthStateListener
        setupAuthStateListener();
        
        // Check if user is already signed in
        checkCurrentUser();
        
        // Setup UI listeners
        setupClickListeners();
        
        // Handle logout navigation or show initial state
        handleNavigationIntent();
        
        // Initially show only Google Sign-In
        showGoogleSignInOnly();
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
     * Configure Google Sign-In options
     * Note: Replace "your_web_client_id" with your actual Web Client ID from Firebase Console
     */
    private void configureGoogleSignIn() {
        String webClientId = getString(R.string.default_web_client_id);
        Log.d(TAG, "Configuring Google Sign-In with Web Client ID: " + webClientId);
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.d(TAG, "Google Sign-In configured successfully");
    }

    /**
     * Setup Activity Result Launcher for Google Sign-In
     */
    private void setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        hideProgressBar();
                        Toast.makeText(this, "Sign-in cancelled", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Google Sign-In cancelled by user");
                    }
                }
        );
    }

    /**
     * Setup AuthStateListener to monitor authentication changes
     */
    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in, redirect to MainActivity
                Log.d(TAG, "AuthStateListener: User signed in: " + user.getEmail());
                redirectToMainActivity();
            } else {
                Log.d(TAG, "AuthStateListener: User signed out");
            }
        };
    }

    /**
     * Check if user is already signed in and redirect to MainActivity
     */
    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already signed in: " + currentUser.getEmail());
            redirectToMainActivity();
        }
    }
    
    /**
     * Handle navigation intent to determine if this is a logout navigation
     */
    private void handleNavigationIntent() {
        Intent intent = getIntent();
        if (LogoutManager.isLogoutNavigation(intent)) {
            Log.d(TAG, "LoginActivity opened after logout - ensuring complete login interface");
            
            // Ensure we show the complete login interface
            // This prevents any cached state from showing incomplete UI
            showCompleteLoginInterface();
            
            // Clear any cached authentication state
            clearAuthenticationCache();
            
            // Show a subtle message that logout was successful (optional)
            Toast.makeText(this, "You have been logged out", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show the complete login interface with all options
     */
    private void showCompleteLoginInterface() {
        // Ensure all login options are visible and properly configured
        binding.btnGoogleSignIn.setVisibility(View.VISIBLE);
        binding.btnEmailSignInToggle.setVisibility(View.VISIBLE);
        binding.tvSignUp.setVisibility(View.VISIBLE);
        
        // Reset to default state (Google Sign-In only initially)
        showGoogleSignInOnly();
        
        Log.d(TAG, "Complete login interface displayed");
    }
    
    /**
     * Clear any cached authentication state
     */
    private void clearAuthenticationCache() {
        try {
            // Clear any form data
            if (binding.etEmail != null) {
                binding.etEmail.setText("");
            }
            if (binding.etPassword != null) {
                binding.etPassword.setText("");
            }
            
            // Clear any error states
            clearErrors();
            
            // Hide progress bar if showing
            hideProgressBar();
            
            Log.d(TAG, "Authentication cache cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing authentication cache", e);
        }
    }

    /**
     * Setup click listeners for UI components
     */
    private void setupClickListeners() {
        binding.btnGoogleSignIn.setOnClickListener(v -> initiateGoogleSignIn());
        binding.btnEmailSignIn.setOnClickListener(v -> attemptEmailSignIn());
        binding.tvSignUp.setOnClickListener(v -> navigateToSignUp());
        binding.tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
        binding.btnEmailSignInToggle.setOnClickListener(v -> toggleEmailSignIn());
        binding.btnBackToOptions.setOnClickListener(v -> showGoogleSignInOnly());
    }

    /**
     * Start Google Sign-In process
     */
    private void initiateGoogleSignIn() {
        showProgressBar();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
        Log.d(TAG, "Google Sign-In intent launched");
    }

    /**
     * Handle Google Sign-In result
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Google Sign-In successful: " + account.getEmail());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            hideProgressBar();
            Log.w(TAG, "Google Sign-In failed", e);
            handleSignInError(e.getStatusCode());
        }
    }

    /**
     * Authenticate with Firebase using Google credentials
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressBar();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Firebase authentication successful");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserToFirestore(user);
                                saveUserPreferences(user);
                                redirectToMainActivity();
                            }
                        } else {
                            Log.w(TAG, "Firebase authentication failed", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Save user profile to Firestore
     */
    private void saveUserToFirestore(FirebaseUser user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", user.getUid());
        userMap.put("name", user.getDisplayName());
        userMap.put("email", user.getEmail());
        userMap.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        userMap.put("lastLogin", System.currentTimeMillis());

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
    private void saveUserPreferences(FirebaseUser user) {
        UserPreferences.saveUserInfo(this, user.getUid(), user.getDisplayName(), 
                user.getEmail(), user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
    }

    /**
     * Handle different types of sign-in errors
     */
    private void handleSignInError(int statusCode) {
        String errorMessage;
        String debugInfo = "";
        
        switch (statusCode) {
            case 12501: // User cancelled
                errorMessage = "Sign-in cancelled";
                debugInfo = "User cancelled the sign-in flow";
                break;
            case 7: // Network error
                errorMessage = "No internet connection";
                debugInfo = "Network error occurred";
                break;
            case 10: // Developer error
                errorMessage = "Configuration error. Please check setup.";
                debugInfo = "Developer error - likely SHA-1 or Web Client ID issue";
                break;
            case 12500: // Sign in failed
                errorMessage = "Sign-in failed. Please try again.";
                debugInfo = "Sign-in failed - check Firebase configuration";
                break;
            default:
                errorMessage = "Sign-in failed. Please try again.";
                debugInfo = "Unknown error occurred";
                break;
        }
        
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Sign-in error - Code: " + statusCode + ", Info: " + debugInfo);
        
        // Additional debug info
        if (statusCode == 12501) {
            Log.e(TAG, "TROUBLESHOOTING: Check if SHA-1 fingerprint is added to Firebase Console");
            Log.e(TAG, "TROUBLESHOOTING: Verify Web Client ID: " + getString(R.string.default_web_client_id));
        }
    }

    /**
     * Redirect to MainActivity after successful login
     */
    private void redirectToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Show progress indicator
     */
    private void showProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGoogleSignIn.setEnabled(false);
        binding.btnEmailSignIn.setEnabled(false);
    }

    /**
     * Hide progress indicator
     */
    private void hideProgressBar() {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnGoogleSignIn.setEnabled(true);
        binding.btnEmailSignIn.setEnabled(true);
    }

    /**
     * Attempt email/password sign in
     */
    private void attemptEmailSignIn() {
        // Clear previous errors
        clearErrors();
        
        // Get input values
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        
        // Validate inputs
        if (!validateEmailSignInInputs(email, password)) {
            return;
        }
        
        // Show progress and sign in
        showProgressBar();
        signInWithEmail(email, password);
    }

    /**
     * Validate email sign in inputs
     */
    private boolean validateEmailSignInInputs(String email, String password) {
        boolean isValid = true;
        
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_empty_email));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }
        
        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        }
        
        return isValid;
    }

    /**
     * Sign in with email and password
     */
    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    hideProgressBar();
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email sign-in successful");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserPreferences(user);
                            redirectToMainActivity();
                        }
                    } else {
                        Log.w(TAG, "Email sign-in failed", task.getException());
                        handleEmailSignInError(task.getException());
                    }
                });
    }

    /**
     * Handle email sign in errors
     */
    private void handleEmailSignInError(Exception exception) {
        String errorMessage = getString(R.string.error_authentication_failed);
        
        if (exception != null) {
            String exceptionMessage = exception.getMessage();
            if (exceptionMessage != null) {
                if (exceptionMessage.contains("no user record") || 
                    exceptionMessage.contains("invalid-email")) {
                    errorMessage = "No account found with this email";
                    binding.tilEmail.setError(errorMessage);
                } else if (exceptionMessage.contains("wrong-password") || 
                          exceptionMessage.contains("invalid-credential")) {
                    errorMessage = "Incorrect password";
                    binding.tilPassword.setError(errorMessage);
                } else if (exceptionMessage.contains("network error")) {
                    errorMessage = getString(R.string.error_network);
                }
            }
        }
        
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Navigate to Sign Up Activity
     */
    private void navigateToSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Navigate to Forgot Password Activity
     */
    private void navigateToForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Clear all input errors
     */
    private void clearErrors() {
        if (binding.tilEmail != null) binding.tilEmail.setError(null);
        if (binding.tilPassword != null) binding.tilPassword.setError(null);
    }

    /**
     * Show only Google Sign-In option (default state)
     */
    private void showGoogleSignInOnly() {
        binding.llSigninOptions.setVisibility(View.VISIBLE);
        binding.llEmailForm.setVisibility(View.GONE);
    }

    /**
     * Show email/password fields along with Google Sign-In
     */
    private void showEmailPasswordFields() {
        binding.llSigninOptions.setVisibility(View.GONE);
        binding.llEmailForm.setVisibility(View.VISIBLE);
    }

    /**
     * Toggle between Google-only and Email+Google sign in modes
     */
    private void toggleEmailSignIn() {
        if (binding.llEmailForm.getVisibility() == View.GONE) {
            showEmailPasswordFields();
        } else {
            showGoogleSignInOnly();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Add AuthStateListener when activity starts
        if (authStateListener != null) {
            mAuth.addAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove AuthStateListener when activity stops
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}