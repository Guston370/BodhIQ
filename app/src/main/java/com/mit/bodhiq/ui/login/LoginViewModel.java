package com.mit.bodhiq.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mit.bodhiq.data.database.entity.User;
import com.mit.bodhiq.data.repository.UserRepository;
import com.mit.bodhiq.utils.PreferenceManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * LoginViewModel handles authentication logic and UI state management for LoginActivity.
 * Implements requirements 1.1, 1.2, and 8.5 for user authentication and state management.
 * Uses RxJava3 for asynchronous operations and LiveData for UI updates.
 */
@HiltViewModel
public class LoginViewModel extends ViewModel {
    
    private final UserRepository userRepository;
    private final PreferenceManager preferenceManager;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    // LiveData for UI state management
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<AuthenticationResult> authenticationResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    @Inject
    public LoginViewModel(UserRepository userRepository, PreferenceManager preferenceManager) {
        this.userRepository = userRepository;
        this.preferenceManager = preferenceManager;
    }
    
    /**
     * Get loading state LiveData.
     * 
     * @return LiveData indicating if authentication is in progress
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Get authentication result LiveData.
     * 
     * @return LiveData containing authentication result
     */
    public LiveData<AuthenticationResult> getAuthenticationResult() {
        return authenticationResult;
    }
    
    /**
     * Get error message LiveData.
     * 
     * @return LiveData containing error messages
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Authenticate user with email address.
     * Requirement 1.1: User authentication with predefined credentials
     * 
     * @param email User's email address
     */
    public void authenticateUser(String email) {
        // Validate email format first
        if (!userRepository.isValidEmail(email)) {
            errorMessage.setValue("Please enter a valid email address");
            return;
        }
        
        // Set loading state
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        // Perform authentication
        disposables.add(
            userRepository.authenticateUser(email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::onAuthenticationSuccess,
                    this::onAuthenticationError
                )
        );
    }
    
    /**
     * Handle successful authentication.
     * Requirement 1.3: Store authentication state using DataStore Preferences
     * 
     * @param user Authenticated user
     */
    private void onAuthenticationSuccess(User user) {
        isLoading.setValue(false);
        
        // Store user session information
        storeUserSession(user);
        
        // Notify UI of successful authentication
        authenticationResult.setValue(AuthenticationResult.success(user));
    }
    
    /**
     * Handle authentication error.
     * 
     * @param throwable Error that occurred during authentication
     */
    private void onAuthenticationError(Throwable throwable) {
        isLoading.setValue(false);
        
        String errorMsg;
        if (throwable instanceof UserRepository.AuthenticationException) {
            errorMsg = "Authentication failed. Please check your credentials.";
        } else if (throwable instanceof java.net.UnknownHostException || 
                   throwable instanceof java.net.SocketTimeoutException) {
            errorMsg = "Network error. Please check your connection and try again.";
        } else {
            errorMsg = "An unexpected error occurred. Please try again.";
        }
        
        // Notify UI of authentication failure
        authenticationResult.setValue(AuthenticationResult.failure(errorMsg));
        errorMessage.setValue(errorMsg);
    }
    
    /**
     * Store user session information in preferences.
     * Requirement 1.3: Store User authentication state using DataStore Preferences
     * 
     * @param user Authenticated user to store
     */
    private void storeUserSession(User user) {
        disposables.add(
            preferenceManager.saveUserLogin(user.getId(), user.getEmail(), user.getName(), user.getRole())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    preferences -> {
                        // Session stored successfully
                    },
                    throwable -> {
                        // Log error but don't fail authentication
                        errorMessage.setValue("Warning: Session storage failed. You may need to login again.");
                    }
                )
        );
    }
    
    /**
     * Check if user is currently authenticated.
     * 
     * @return LiveData indicating if user is authenticated
     */
    public LiveData<Boolean> isUserAuthenticated() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        disposables.add(
            preferenceManager.isLoggedIn()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result::setValue,
                    throwable -> result.setValue(false)
                )
        );
        
        return result;
    }
    
    /**
     * Get current user information from stored session.
     * 
     * @return LiveData containing current user or null if not authenticated
     */
    public LiveData<User> getCurrentUser() {
        MutableLiveData<User> result = new MutableLiveData<>();
        
        // Combine user data from preferences to create User object
        disposables.add(
            preferenceManager.getUserId()
                .take(1)
                .flatMap(userId -> {
                    if (userId == -1L) {
                        return io.reactivex.rxjava3.core.Flowable.just((User) null);
                    }
                    
                    return io.reactivex.rxjava3.core.Flowable.combineLatest(
                        preferenceManager.getUserEmail(),
                        preferenceManager.getUserName(),
                        preferenceManager.getUserRole(),
                        (email, name, role) -> {
                            User user = new User(email, name, role, System.currentTimeMillis());
                            user.setId(userId);
                            return user;
                        }
                    );
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result::setValue,
                    throwable -> result.setValue(null)
                )
        );
        
        return result;
    }
    
    /**
     * Logout current user by clearing stored session.
     */
    public void logout() {
        disposables.add(
            preferenceManager.clearUserLogin()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    preferences -> {
                        // Session cleared successfully
                        authenticationResult.setValue(AuthenticationResult.logout());
                    },
                    throwable -> {
                        errorMessage.setValue("Error during logout. Please try again.");
                    }
                )
        );
    }
    
    /**
     * Clear error messages.
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
    
    /**
     * Validate email format.
     * 
     * @param email Email to validate
     * @return true if email format is valid, false otherwise
     */
    public boolean isValidEmail(String email) {
        return userRepository.isValidEmail(email);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
    
    /**
     * Authentication result wrapper class.
     */
    public static class AuthenticationResult {
        private final boolean success;
        private final User user;
        private final String errorMessage;
        private final boolean isLogout;
        
        private AuthenticationResult(boolean success, User user, String errorMessage, boolean isLogout) {
            this.success = success;
            this.user = user;
            this.errorMessage = errorMessage;
            this.isLogout = isLogout;
        }
        
        public static AuthenticationResult success(User user) {
            return new AuthenticationResult(true, user, null, false);
        }
        
        public static AuthenticationResult failure(String errorMessage) {
            return new AuthenticationResult(false, null, errorMessage, false);
        }
        
        public static AuthenticationResult logout() {
            return new AuthenticationResult(false, null, null, true);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public User getUser() {
            return user;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public boolean isLogout() {
            return isLogout;
        }
    }
}