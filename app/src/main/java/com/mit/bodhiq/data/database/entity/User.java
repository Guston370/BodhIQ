package com.mit.bodhiq.data.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a user in the BodhIQ system.
 * Supports two roles: admin and analyst.
 */
@Entity(tableName = "users")
public class User {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "email")
    private String email;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "role")
    private String role; // "admin" or "analyst"
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    // Constructors
    public User() {}
    
    public User(String email, String name, String role, long createdAt) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}