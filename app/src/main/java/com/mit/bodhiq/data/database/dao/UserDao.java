package com.mit.bodhiq.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.mit.bodhiq.data.database.entity.User;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

/**
 * Data Access Object for User entity operations.
 * Provides authentication and user management methods with RxJava3 return types.
 */
@Dao
public interface UserDao {
    
    /**
     * Insert a new user into the database.
     * 
     * @param user User entity to insert
     * @return Single emitting the inserted user's ID
     */
    @Insert
    Single<Long> insertUser(User user);
    
    /**
     * Insert multiple users into the database.
     * 
     * @param users List of User entities to insert
     * @return Completable indicating operation completion
     */
    @Insert
    Completable insertUsers(List<User> users);
    
    /**
     * Update an existing user in the database.
     * 
     * @param user User entity with updated information
     * @return Completable indicating operation completion
     */
    @Update
    Completable updateUser(User user);
    
    /**
     * Get a user by email address for authentication.
     * 
     * @param email User's email address
     * @return Single emitting the User if found, error if not found
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    Single<User> getUserByEmail(String email);
    
    /**
     * Get a user by ID.
     * 
     * @param userId User's ID
     * @return Single emitting the User if found, error if not found
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    Single<User> getUserById(long userId);
    
    /**
     * Get all users in the system.
     * 
     * @return Flowable emitting list of all users
     */
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    Flowable<List<User>> getAllUsers();
    
    /**
     * Get users by role (admin or analyst).
     * 
     * @param role User role to filter by
     * @return Flowable emitting list of users with specified role
     */
    @Query("SELECT * FROM users WHERE role = :role ORDER BY created_at DESC")
    Flowable<List<User>> getUsersByRole(String role);
    
    /**
     * Get total count of users in the system.
     * 
     * @return Single emitting the total user count
     */
    @Query("SELECT COUNT(*) FROM users")
    Single<Integer> getUserCount();
    
    /**
     * Check if a user exists with the given email.
     * 
     * @param email Email address to check
     * @return Single emitting true if user exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    Single<Boolean> userExistsByEmail(String email);
    
    /**
     * Get count of users by role.
     * 
     * @param role User role to count
     * @return Single emitting count of users with specified role
     */
    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    Single<Integer> getUserCountByRole(String role);
    
    /**
     * Delete a user by ID.
     * 
     * @param userId ID of user to delete
     * @return Completable indicating operation completion
     */
    @Query("DELETE FROM users WHERE id = :userId")
    Completable deleteUserById(long userId);
}