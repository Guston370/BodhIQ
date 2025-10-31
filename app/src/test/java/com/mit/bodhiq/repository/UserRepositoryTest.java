package com.mit.bodhiq.repository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mit.bodhiq.data.database.dao.UserDao;
import com.mit.bodhiq.data.database.entity.User;
import com.mit.bodhiq.data.repository.UserRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Unit tests for UserRepository authentication methods.
 * Tests requirements 1.1, 1.2, 1.4 for user authentication and role management.
 */
@RunWith(RobolectricTestRunner.class)
public class UserRepositoryTest {
    
    @Mock
    private UserDao userDao;
    
    private UserRepository userRepository;
    
    // Test data
    private User adminUser;
    private User analystUser;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userRepository = new UserRepository(userDao);
        
        // Setup test users
        adminUser = new User("admin@company.com", "Admin User", "admin", System.currentTimeMillis());
        adminUser.setId(1L);
        
        analystUser = new User("analyst@company.com", "Analyst User", "analyst", System.currentTimeMillis());
        analystUser.setId(2L);
    }
    
    /**
     * Test successful user authentication.
     * Requirement 1.1: User authentication with predefined credentials
     */
    @Test
    public void testAuthenticateUser_Success() {
        // Given
        String email = "admin@company.com";
        when(userDao.getUserByEmail(email)).thenReturn(Single.just(adminUser));
        
        // When
        User result = userRepository.authenticateUser(email).blockingGet();
        
        // Then
        assertNotNull("User should be authenticated", result);
        assertEquals("Email should match", email, result.getEmail());
        assertEquals("Role should be admin", "admin", result.getRole());
        verify(userDao).getUserByEmail(email);
    }
    
    /**
     * Test authentication failure with invalid email.
     * Requirement 1.1: Authentication error handling
     */
    @Test
    public void testAuthenticateUser_InvalidEmail() {
        // Given
        String email = "invalid@company.com";
        when(userDao.getUserByEmail(email)).thenReturn(Single.error(new RuntimeException("User not found")));
        
        // When & Then
        try {
            userRepository.authenticateUser(email).blockingGet();
            fail("Should throw AuthenticationException");
        } catch (Exception e) {
            assertTrue("Should be AuthenticationException", e.getCause() instanceof UserRepository.AuthenticationException);
            assertTrue("Error message should contain email", e.getCause().getMessage().contains(email));
        }
        
        verify(userDao).getUserByEmail(email);
    }
    
    /**
     * Test admin role checking.
     * Requirement 1.2: Support for admin and analyst roles
     */
    @Test
    public void testIsUserAdmin_AdminUser() {
        // Given
        long userId = 1L;
        when(userDao.getUserById(userId)).thenReturn(Single.just(adminUser));
        
        // When
        Boolean isAdmin = userRepository.isUserAdmin(userId).blockingGet();
        
        // Then
        assertTrue("Admin user should return true", isAdmin);
        verify(userDao).getUserById(userId);
    }
    
    /**
     * Test admin role checking for analyst user.
     * Requirement 1.2: Role-based access control
     */
    @Test
    public void testIsUserAdmin_AnalystUser() {
        // Given
        long userId = 2L;
        when(userDao.getUserById(userId)).thenReturn(Single.just(analystUser));
        
        // When
        Boolean isAdmin = userRepository.isUserAdmin(userId).blockingGet();
        
        // Then
        assertFalse("Analyst user should return false for admin check", isAdmin);
        verify(userDao).getUserById(userId);
    }
    
    /**
     * Test analyst role checking.
     * Requirement 1.2: Support for admin and analyst roles
     */
    @Test
    public void testIsUserAnalyst_AnalystUser() {
        // Given
        long userId = 2L;
        when(userDao.getUserById(userId)).thenReturn(Single.just(analystUser));
        
        // When
        Boolean isAnalyst = userRepository.isUserAnalyst(userId).blockingGet();
        
        // Then
        assertTrue("Analyst user should return true", isAnalyst);
        verify(userDao).getUserById(userId);
    }
    
    /**
     * Test analyst role checking for admin user.
     * Requirement 1.2: Role-based access control
     */
    @Test
    public void testIsUserAnalyst_AdminUser() {
        // Given
        long userId = 1L;
        when(userDao.getUserById(userId)).thenReturn(Single.just(adminUser));
        
        // When
        Boolean isAnalyst = userRepository.isUserAnalyst(userId).blockingGet();
        
        // Then
        assertFalse("Admin user should return false for analyst check", isAnalyst);
        verify(userDao).getUserById(userId);
    }
    
    /**
     * Test role checking error handling.
     */
    @Test
    public void testIsUserAdmin_UserNotFound() {
        // Given
        long userId = 999L;
        when(userDao.getUserById(userId)).thenReturn(Single.error(new RuntimeException("User not found")));
        
        // When
        Boolean isAdmin = userRepository.isUserAdmin(userId).blockingGet();
        
        // Then
        assertFalse("Should return false when user not found", isAdmin);
        verify(userDao).getUserById(userId);
    }
    
    /**
     * Test getting all users.
     * Requirement 1.4: User management functionality
     */
    @Test
    public void testGetAllUsers() {
        // Given
        List<User> users = Arrays.asList(adminUser, analystUser);
        when(userDao.getAllUsers()).thenReturn(Flowable.just(users));
        
        // When
        List<User> result = userRepository.getAllUsers().blockingFirst();
        
        // Then
        assertNotNull("Users list should not be null", result);
        assertEquals("Should return 2 users", 2, result.size());
        assertTrue("Should contain admin user", result.contains(adminUser));
        assertTrue("Should contain analyst user", result.contains(analystUser));
        verify(userDao).getAllUsers();
    }
    
    /**
     * Test getting users by role.
     * Requirement 1.2: Role-based user filtering
     */
    @Test
    public void testGetUsersByRole_AdminRole() {
        // Given
        String role = "admin";
        List<User> adminUsers = Arrays.asList(adminUser);
        when(userDao.getUsersByRole(role)).thenReturn(Flowable.just(adminUsers));
        
        // When
        List<User> result = userRepository.getUsersByRole(role).blockingFirst();
        
        // Then
        assertNotNull("Admin users list should not be null", result);
        assertEquals("Should return 1 admin user", 1, result.size());
        assertEquals("User should be admin", "admin", result.get(0).getRole());
        verify(userDao).getUsersByRole(role);
    }
    
    /**
     * Test user count functionality.
     */
    @Test
    public void testGetUserCount() {
        // Given
        when(userDao.getUserCount()).thenReturn(Single.just(2));
        
        // When
        Integer count = userRepository.getUserCount().blockingGet();
        
        // Then
        assertNotNull("Count should not be null", count);
        assertEquals("Should return 2 users", Integer.valueOf(2), count);
        verify(userDao).getUserCount();
    }
    
    /**
     * Test admin count functionality.
     */
    @Test
    public void testGetAdminCount() {
        // Given
        when(userDao.getUserCountByRole("admin")).thenReturn(Single.just(1));
        
        // When
        Integer count = userRepository.getAdminCount().blockingGet();
        
        // Then
        assertNotNull("Admin count should not be null", count);
        assertEquals("Should return 1 admin", Integer.valueOf(1), count);
        verify(userDao).getUserCountByRole("admin");
    }
    
    /**
     * Test analyst count functionality.
     */
    @Test
    public void testGetAnalystCount() {
        // Given
        when(userDao.getUserCountByRole("analyst")).thenReturn(Single.just(1));
        
        // When
        Integer count = userRepository.getAnalystCount().blockingGet();
        
        // Then
        assertNotNull("Analyst count should not be null", count);
        assertEquals("Should return 1 analyst", Integer.valueOf(1), count);
        verify(userDao).getUserCountByRole("analyst");
    }
    
    /**
     * Test user existence check.
     */
    @Test
    public void testUserExists_ExistingUser() {
        // Given
        String email = "admin@company.com";
        when(userDao.userExistsByEmail(email)).thenReturn(Single.just(true));
        
        // When
        Boolean exists = userRepository.userExists(email).blockingGet();
        
        // Then
        assertTrue("User should exist", exists);
        verify(userDao).userExistsByEmail(email);
    }
    
    /**
     * Test user existence check for non-existing user.
     */
    @Test
    public void testUserExists_NonExistingUser() {
        // Given
        String email = "nonexistent@company.com";
        when(userDao.userExistsByEmail(email)).thenReturn(Single.just(false));
        
        // When
        Boolean exists = userRepository.userExists(email).blockingGet();
        
        // Then
        assertFalse("User should not exist", exists);
        verify(userDao).userExistsByEmail(email);
    }
    
    /**
     * Test user creation with valid data.
     * Requirement 1.4: User management functionality
     */
    @Test
    public void testCreateUser_ValidData() {
        // Given
        String email = "newuser@company.com";
        String name = "New User";
        String role = "analyst";
        when(userDao.userExistsByEmail(email)).thenReturn(Single.just(false));
        when(userDao.insertUser(any(User.class))).thenReturn(Single.just(3L));
        
        // When
        Long userId = userRepository.createUser(email, name, role).blockingGet();
        
        // Then
        assertNotNull("User ID should not be null", userId);
        assertEquals("Should return user ID 3", Long.valueOf(3L), userId);
        verify(userDao).userExistsByEmail(email);
        verify(userDao).insertUser(any(User.class));
    }
    
    /**
     * Test user creation with invalid role.
     */
    @Test
    public void testCreateUser_InvalidRole() {
        // Given
        String email = "newuser@company.com";
        String name = "New User";
        String role = "invalid";
        
        // When & Then
        try {
            userRepository.createUser(email, name, role).blockingGet();
            fail("Should throw IllegalArgumentException");
        } catch (Exception e) {
            assertTrue("Should be IllegalArgumentException", e.getCause() instanceof IllegalArgumentException);
            assertTrue("Error message should mention invalid role", e.getCause().getMessage().contains("Invalid role"));
        }
        
        verify(userDao, never()).userExistsByEmail(anyString());
        verify(userDao, never()).insertUser(any(User.class));
    }
    
    /**
     * Test user creation with existing email.
     */
    @Test
    public void testCreateUser_ExistingEmail() {
        // Given
        String email = "admin@company.com";
        String name = "New User";
        String role = "analyst";
        when(userDao.userExistsByEmail(email)).thenReturn(Single.just(true));
        
        // When & Then
        try {
            userRepository.createUser(email, name, role).blockingGet();
            fail("Should throw IllegalArgumentException");
        } catch (Exception e) {
            assertTrue("Should be IllegalArgumentException", e.getCause() instanceof IllegalArgumentException);
            assertTrue("Error message should mention existing user", e.getCause().getMessage().contains("already exists"));
        }
        
        verify(userDao).userExistsByEmail(email);
        verify(userDao, never()).insertUser(any(User.class));
    }
    
    /**
     * Test user update functionality.
     * Requirement 1.4: User management functionality
     */
    @Test
    public void testUpdateUser_ValidData() {
        // Given
        User updatedUser = new User("admin@company.com", "Updated Admin", "admin", System.currentTimeMillis());
        updatedUser.setId(1L);
        when(userDao.updateUser(updatedUser)).thenReturn(Completable.complete());
        
        // When
        userRepository.updateUser(updatedUser).blockingAwait();
        
        // Then
        verify(userDao).updateUser(updatedUser);
    }
    
    /**
     * Test user update with invalid role.
     */
    @Test
    public void testUpdateUser_InvalidRole() {
        // Given
        User updatedUser = new User("admin@company.com", "Updated Admin", "invalid", System.currentTimeMillis());
        updatedUser.setId(1L);
        
        // When & Then
        try {
            userRepository.updateUser(updatedUser).blockingAwait();
            fail("Should throw IllegalArgumentException");
        } catch (Exception e) {
            assertTrue("Should be IllegalArgumentException", e.getCause() instanceof IllegalArgumentException);
            assertTrue("Error message should mention invalid role", e.getCause().getMessage().contains("Invalid role"));
        }
        
        verify(userDao, never()).updateUser(any(User.class));
    }
    
    /**
     * Test user deletion.
     * Requirement 1.4: User management functionality
     */
    @Test
    public void testDeleteUser() {
        // Given
        long userId = 1L;
        when(userDao.deleteUserById(userId)).thenReturn(Completable.complete());
        
        // When
        userRepository.deleteUser(userId).blockingAwait();
        
        // Then
        verify(userDao).deleteUserById(userId);
    }
    
    /**
     * Test email validation.
     */
    @Test
    public void testIsValidEmail_ValidEmails() {
        assertTrue("Valid email should pass", userRepository.isValidEmail("user@company.com"));
        assertTrue("Valid email with subdomain should pass", userRepository.isValidEmail("user@mail.company.com"));
        assertTrue("Valid email with numbers should pass", userRepository.isValidEmail("user123@company.com"));
    }
    
    /**
     * Test email validation with invalid emails.
     */
    @Test
    public void testIsValidEmail_InvalidEmails() {
        assertFalse("Null email should fail", userRepository.isValidEmail(null));
        assertFalse("Empty email should fail", userRepository.isValidEmail(""));
        assertFalse("Email without @ should fail", userRepository.isValidEmail("usercompany.com"));
        assertFalse("Email without . should fail", userRepository.isValidEmail("user@company"));
        assertFalse("Short email should fail", userRepository.isValidEmail("u@c.c"));
    }
}