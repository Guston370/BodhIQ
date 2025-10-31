package com.mit.bodhiq.data.repository;

import com.mit.bodhiq.data.database.dao.UserDao;
import com.mit.bodhiq.data.database.entity.User;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository for user authentication and management operations.
 * Provides a clean API for user-related data operations using RxJava3.
 * Implements requirements 1.1, 1.2, 1.4 for user authentication and role management.
 */
@Singleton
public class UserRepository {
    
    private final UserDao userDao;
    
    @Inject
    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
    }
    
    /**
     * Authenticate user by email address.
     * Requirement 1.1: User authentication with predefined credentials
     * 
     * @param email User's email address
     * @return Single emitting authenticated User or error if not found
     */
    public Single<User> authenticateUser(String email) {
        return userDao.getUserByEmail(email)
                .onErrorResumeNext(throwable -> 
                    Single.error(new AuthenticationException("Invalid email credentials: " + email))
                );
    }
    
    /**
     * Check if user has admin role.
     * Requirement 1.2: Support for admin and analyst roles
     * 
     * @param userId User's ID
     * @return Single emitting true if user is admin, false otherwise
     */
    public Single<Boolean> isUserAdmin(long userId) {
        return userDao.getUserById(userId)
                .map(user -> "admin".equals(user.getRole()))
                .onErrorReturnItem(false);
    }
    
    /**
     * Check if user has analyst role.
     * Requirement 1.2: Support for admin and analyst roles
     * 
     * @param userId User's ID
     * @return Single emitting true if user is analyst, false otherwise
     */
    public Single<Boolean> isUserAnalyst(long userId) {
        return userDao.getUserById(userId)
                .map(user -> "analyst".equals(user.getRole()))
                .onErrorReturnItem(false);
    }
    
    /**
     * Get user by ID.
     * 
     * @param userId User's ID
     * @return Single emitting User or error if not found
     */
    public Single<User> getUserById(long userId) {
        return userDao.getUserById(userId);
    }
    
    /**
     * Get user by email address.
     * 
     * @param email User's email address
     * @return Single emitting User or error if not found
     */
    public Single<User> getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }
    
    /**
     * Get all users in the system.
     * Requirement 1.4: User management functionality
     * 
     * @return Flowable emitting list of all users
     */
    public Flowable<List<User>> getAllUsers() {
        return userDao.getAllUsers();
    }
    
    /**
     * Get users by role (admin or analyst).
     * Requirement 1.2: Role-based user filtering
     * 
     * @param role User role to filter by
     * @return Flowable emitting list of users with specified role
     */
    public Flowable<List<User>> getUsersByRole(String role) {
        return userDao.getUsersByRole(role);
    }
    
    /**
     * Get total count of users in the system.
     * 
     * @return Single emitting total user count
     */
    public Single<Integer> getUserCount() {
        return userDao.getUserCount();
    }
    
    /**
     * Get count of admin users.
     * 
     * @return Single emitting count of admin users
     */
    public Single<Integer> getAdminCount() {
        return userDao.getUserCountByRole("admin");
    }
    
    /**
     * Get count of analyst users.
     * 
     * @return Single emitting count of analyst users
     */
    public Single<Integer> getAnalystCount() {
        return userDao.getUserCountByRole("analyst");
    }
    
    /**
     * Check if user exists by email.
     * 
     * @param email Email address to check
     * @return Single emitting true if user exists, false otherwise
     */
    public Single<Boolean> userExists(String email) {
        return userDao.userExistsByEmail(email);
    }
    
    /**
     * Create a new user.
     * Requirement 1.4: User management functionality
     * 
     * @param email User's email address
     * @param name User's display name
     * @param role User's role (admin or analyst)
     * @return Single emitting the created user's ID
     */
    public Single<Long> createUser(String email, String name, String role) {
        // Validate role
        if (!"admin".equals(role) && !"analyst".equals(role)) {
            return Single.error(new IllegalArgumentException("Invalid role: " + role + ". Must be 'admin' or 'analyst'"));
        }
        
        // Check if user already exists
        return userDao.userExistsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        return Single.error(new IllegalArgumentException("User already exists with email: " + email));
                    }
                    
                    User user = new User(email, name, role, System.currentTimeMillis());
                    return userDao.insertUser(user);
                });
    }
    
    /**
     * Update user information.
     * Requirement 1.4: User management functionality
     * 
     * @param user User entity with updated information
     * @return Completable indicating operation completion
     */
    public Completable updateUser(User user) {
        // Validate role if being updated
        if (user.getRole() != null && !"admin".equals(user.getRole()) && !"analyst".equals(user.getRole())) {
            return Completable.error(new IllegalArgumentException("Invalid role: " + user.getRole()));
        }
        
        return userDao.updateUser(user);
    }
    
    /**
     * Delete user by ID.
     * Requirement 1.4: User management functionality
     * 
     * @param userId ID of user to delete
     * @return Completable indicating operation completion
     */
    public Completable deleteUser(long userId) {
        return userDao.deleteUserById(userId);
    }
    
    /**
     * Validate user credentials format.
     * 
     * @param email Email address to validate
     * @return true if email format is valid, false otherwise
     */
    public boolean isValidEmail(String email) {
        return email != null && 
               email.contains("@") && 
               email.contains(".") && 
               email.length() > 5;
    }
    
    /**
     * Custom exception for authentication failures.
     */
    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
        
        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}