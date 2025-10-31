package com.mit.bodhiq.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.mit.bodhiq.data.database.entity.User;
import com.mit.bodhiq.data.repository.UserRepository;
import com.mit.bodhiq.ui.login.LoginViewModel;
import com.mit.bodhiq.utils.PreferenceManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Unit tests for LoginViewModel LiveData updates and error handling.
 * Tests requirements 1.1, 1.2, 8.5 for authentication and state management.
 */
@RunWith(RobolectricTestRunner.class)
public class LoginViewModelTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PreferenceManager preferenceManager;
    
    @Mock
    private Observer<Boolean> loadingObserver;
    
    @Mock
    private Observer<LoginViewModel.AuthenticationResult> authResultObserver;
    
    @Mock
    private Observer<String> errorObserver;
    
    private LoginViewModel loginViewModel;
    
    // Test data
    private User testUser;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up RxJava to use synchronous schedulers for testing
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
        
        loginViewModel = new LoginViewModel(userRepository, preferenceManager);
        
        // Setup test user
        testUser = new User("admin@company.com", "Admin User", "admin", System.currentTimeMillis());
        testUser.setId(1L);
        
        // Observe LiveData
        loginViewModel.getIsLoading().observeForever(loadingObserver);
        loginViewModel.getAuthenticationResult().observeForever(authResultObserver);
        loginViewModel.getErrorMessage().observeForever(errorObserver);
    }
    
    /**
     * Test successful user authentication.
     * Requirement 1.1: User authentication with predefined credentials
     */
    @Test
    public void testAuthenticateUser_Success() {
        // Given
        String email = "admin@company.com";
        when(userRepository.isValidEmail(email)).thenReturn(true);
        when(userRepository.authenticateUser(email)).thenReturn(Single.just(testUser));
        when(preferenceManager.saveUserLogin(anyLong(), anyString(), anyString(), anyString()))
            .thenReturn(Single.just(mock(androidx.datastore.preferences.core.Preferences.class)));
        
        // When
        loginViewModel.authenticateUser(email);
        
        // Then
        verify(loadingObserver).onChanged(true);  // Loading started
        verify(loadingObserver).onChanged(false); // Loading finished
        verify(authResultObserver).onChanged(argThat(result -> 
            result != null && result.isSuccess() && result.getUser().equals(testUser)
        ));
        verify(errorObserver).onChanged(null); // Clear error
        
        verify(userRepository).authenticateUser(email);
        verify(preferenceManager).saveUserLogin(testUser.getId(), testUser.getEmail(), 
            testUser.getName(), testUser.getRole());
    }
    
    /**
     * Test authentication failure with invalid credentials.
     * Requirement 1.1: Authentication error handling
     */
    @Test
    public void testAuthenticateUser_InvalidCredentials() {
        // Given
        String email = "invalid@company.com";
        when(userRepository.isValidEmail(email)).thenReturn(true);
        when(userRepository.authenticateUser(email))
            .thenReturn(Single.error(new UserRepository.AuthenticationException("Invalid credentials")));
        
        // When
        loginViewModel.authenticateUser(email);
        
        // Then
        verify(loadingObserver).onChanged(true);  // Loading started
        verify(loadingObserver).onChanged(false); // Loading finished
        verify(authResultObserver).onChanged(argThat(result -> 
            result != null && !result.isSuccess() && 
            result.getErrorMessage().contains("Authentication failed")
        ));
        verify(errorObserver).onChanged(argThat(error -> 
            error != null && error.contains("Authentication failed")
        ));
        
        verify(userRepository).authenticateUser(email);
        verify(preferenceManager, never()).saveUserLogin(anyLong(), anyString(), anyString(), anyString());
    }
    
    /**
     * Test authentication with invalid email format.
     */
    @Test
    public void testAuthenticateUser_InvalidEmailFormat() {
        // Given
        String email = "invalid-email";
        when(userRepository.isValidEmail(email)).thenReturn(false);
        
        // When
        loginViewModel.authenticateUser(email);
        
        // Then
        verify(loadingObserver, never()).onChanged(true); // Should not start loading
        verify(errorObserver).onChanged("Please enter a valid email address");
        verify(userRepository, never()).authenticateUser(anyString());
    }
    
    /**
     * Test network error handling.
     */
    @Test
    public void testAuthenticateUser_NetworkError() {
        // Given
        String email = "admin@company.com";
        when(userRepository.isValidEmail(email)).thenReturn(true);
        when(userRepository.authenticateUser(email))
            .thenReturn(Single.error(new java.net.UnknownHostException("Network error")));
        
        // When
        loginViewModel.authenticateUser(email);
        
        // Then
        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(authResultObserver).onChanged(argThat(result -> 
            result != null && !result.isSuccess() && 
            result.getErrorMessage().contains("Network error")
        ));
        verify(errorObserver).onChanged(argThat(error -> 
            error != null && error.contains("Network error")
        ));
    }
    
    /**
     * Test unexpected error handling.
     */
    @Test
    public void testAuthenticateUser_UnexpectedError() {
        // Given
        String email = "admin@company.com";
        when(userRepository.isValidEmail(email)).thenReturn(true);
        when(userRepository.authenticateUser(email))
            .thenReturn(Single.error(new RuntimeException("Unexpected error")));
        
        // When
        loginViewModel.authenticateUser(email);
        
        // Then
        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(authResultObserver).onChanged(argThat(result -> 
            result != null && !result.isSuccess() && 
            result.getErrorMessage().contains("unexpected error")
        ));
        verify(errorObserver).onChanged(argThat(error -> 
            error != null && error.contains("unexpected error")
        ));
    }
    
    /**
     * Test session storage failure handling.
     * Requirement 1.3: Store authentication state using DataStore Preferences
     */
    @Test
    public void testAuthenticateUser_SessionStorageFailure() {
        // Given
        String email = "admin@company.com";
        when(userRepository.isValidEmail(email)).thenReturn(true);
        when(userRepository.authenticateUser(email)).thenReturn(Single.just(testUser));
        when(preferenceManager.saveUserLogin(anyLong(), anyString(), anyString(), anyString()))
            .thenReturn(Single.error(new RuntimeException("Storage error")));
        
        // When
        loginViewModel.authenticateUser(email);
        
        // Then
        verify(authResultObserver).onChanged(argThat(result -> 
            result != null && result.isSuccess() && result.getUser().equals(testUser)
        ));
        verify(errorObserver).onChanged(argThat(error -> 
            error != null && error.contains("Session storage failed")
        ));
    }
    
    /**
     * Test checking if user is authenticated.
     */
    @Test
    public void testIsUserAuthenticated_True() {
        // Given
        when(preferenceManager.isLoggedIn()).thenReturn(Flowable.just(true));
        
        // When
        Observer<Boolean> observer = mock(Observer.class);
        loginViewModel.isUserAuthenticated().observeForever(observer);
        
        // Then
        verify(observer).onChanged(true);
        verify(preferenceManager).isLoggedIn();
    }
    
    /**
     * Test checking if user is not authenticated.
     */
    @Test
    public void testIsUserAuthenticated_False() {
        // Given
        when(preferenceManager.isLoggedIn()).thenReturn(Flowable.just(false));
        
        // When
        Observer<Boolean> observer = mock(Observer.class);
        loginViewModel.isUserAuthenticated().observeForever(observer);
        
        // Then
        verify(observer).onChanged(false);
        verify(preferenceManager).isLoggedIn();
    }
    
    /**
     * Test getting current user from session.
     */
    @Test
    public void testGetCurrentUser_Success() {
        // Given
        when(preferenceManager.getUserId()).thenReturn(Flowable.just(1L));
        when(preferenceManager.getUserEmail()).thenReturn(Flowable.just("admin@company.com"));
        when(preferenceManager.getUserName()).thenReturn(Flowable.just("Admin User"));
        when(preferenceManager.getUserRole()).thenReturn(Flowable.just("admin"));
        
        // When
        Observer<User> observer = mock(Observer.class);
        loginViewModel.getCurrentUser().observeForever(observer);
        
        // Then
        verify(observer).onChanged(argThat(user -> 
            user != null && 
            user.getId() == 1L &&
            user.getEmail().equals("admin@company.com") &&
            user.getName().equals("Admin User") &&
            user.getRole().equals("admin")
        ));
    }
    
    /**
     * Test getting current user when not logged in.
     */
    @Test
    public void testGetCurrentUser_NotLoggedIn() {
        // Given
        when(preferenceManager.getUserId()).thenReturn(Flowable.just(-1L));
        
        // When
        Observer<User> observer = mock(Observer.class);
        loginViewModel.getCurrentUser().observeForever(observer);
        
        // Then
        verify(observer).onChanged(null);
    }
    
    /**
     * Test user logout functionality.
     */
    @Test
    public void testLogout_Success() {
        // Given
        when(preferenceManager.clearUserLogin())
            .thenReturn(Single.just(mock(androidx.datastore.preferences.core.Preferences.class)));
        
        // When
        loginViewModel.logout();
        
        // Then
        verify(authResultObserver).onChanged(argThat(result -> 
            result != null && result.isLogout()
        ));
        verify(preferenceManager).clearUserLogin();
    }
    
    /**
     * Test logout failure handling.
     */
    @Test
    public void testLogout_Failure() {
        // Given
        when(preferenceManager.clearUserLogin())
            .thenReturn(Single.error(new RuntimeException("Logout error")));
        
        // When
        loginViewModel.logout();
        
        // Then
        verify(errorObserver).onChanged(argThat(error -> 
            error != null && error.contains("Error during logout")
        ));
        verify(preferenceManager).clearUserLogin();
    }
    
    /**
     * Test clearing error messages.
     */
    @Test
    public void testClearError() {
        // When
        loginViewModel.clearError();
        
        // Then
        verify(errorObserver).onChanged(null);
    }
    
    /**
     * Test email validation.
     */
    @Test
    public void testIsValidEmail() {
        // Given
        when(userRepository.isValidEmail("valid@email.com")).thenReturn(true);
        when(userRepository.isValidEmail("invalid")).thenReturn(false);
        
        // When & Then
        assertTrue("Valid email should return true", 
            loginViewModel.isValidEmail("valid@email.com"));
        assertFalse("Invalid email should return false", 
            loginViewModel.isValidEmail("invalid"));
        
        verify(userRepository).isValidEmail("valid@email.com");
        verify(userRepository).isValidEmail("invalid");
    }
    
    /**
     * Test authentication result success factory method.
     */
    @Test
    public void testAuthenticationResult_Success() {
        // When
        LoginViewModel.AuthenticationResult result = LoginViewModel.AuthenticationResult.success(testUser);
        
        // Then
        assertTrue("Should be successful", result.isSuccess());
        assertEquals("User should match", testUser, result.getUser());
        assertNull("Error message should be null", result.getErrorMessage());
        assertFalse("Should not be logout", result.isLogout());
    }
    
    /**
     * Test authentication result failure factory method.
     */
    @Test
    public void testAuthenticationResult_Failure() {
        // Given
        String errorMessage = "Authentication failed";
        
        // When
        LoginViewModel.AuthenticationResult result = LoginViewModel.AuthenticationResult.failure(errorMessage);
        
        // Then
        assertFalse("Should not be successful", result.isSuccess());
        assertNull("User should be null", result.getUser());
        assertEquals("Error message should match", errorMessage, result.getErrorMessage());
        assertFalse("Should not be logout", result.isLogout());
    }
    
    /**
     * Test authentication result logout factory method.
     */
    @Test
    public void testAuthenticationResult_Logout() {
        // When
        LoginViewModel.AuthenticationResult result = LoginViewModel.AuthenticationResult.logout();
        
        // Then
        assertFalse("Should not be successful", result.isSuccess());
        assertNull("User should be null", result.getUser());
        assertNull("Error message should be null", result.getErrorMessage());
        assertTrue("Should be logout", result.isLogout());
    }
    
    /**
     * Test concurrent authentication attempts.
     */
    @Test
    public void testAuthenticateUser_ConcurrentAttempts() {
        // Given
        String email = "admin@company.com";
        when(userRepository.isValidEmail(email)).thenReturn(true);
        when(userRepository.authenticateUser(email)).thenReturn(Single.just(testUser));
        when(preferenceManager.saveUserLogin(anyLong(), anyString(), anyString(), anyString()))
            .thenReturn(Single.just(mock(androidx.datastore.preferences.core.Preferences.class)));
        
        // When - First authentication is in progress
        loginViewModel.authenticateUser(email);
        
        // Reset loading observer to track second call
        reset(loadingObserver);
        loginViewModel.getIsLoading().observeForever(loadingObserver);
        
        // Attempt second authentication while first is in progress
        loginViewModel.authenticateUser(email);
        
        // Then - Second authentication should be ignored
        verify(loadingObserver, never()).onChanged(true);
        verify(userRepository, times(1)).authenticateUser(email); // Only called once
    }
}