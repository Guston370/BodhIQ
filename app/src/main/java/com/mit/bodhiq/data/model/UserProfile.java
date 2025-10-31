package com.mit.bodhiq.data.model;

import java.util.HashMap;
import java.util.Map;

/**
 * User Profile data model for Firestore storage
 */
public class UserProfile {
    private String userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String age;
    private String gender;
    private String profileImageUrl;
    private String loginProvider;
    private long createdAt;
    private long updatedAt;

    // Default constructor required for Firestore
    public UserProfile() {}

    public UserProfile(String userId, String fullName, String email) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getLoginProvider() { return loginProvider; }
    public void setLoginProvider(String loginProvider) { this.loginProvider = loginProvider; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Convert to Map for Firestore
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("fullName", fullName);
        result.put("email", email);
        result.put("phoneNumber", phoneNumber);
        result.put("age", age);
        result.put("gender", gender);
        result.put("profileImageUrl", profileImageUrl);
        result.put("loginProvider", loginProvider);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        return result;
    }

    /**
     * Create from Firestore Map
     */
    public static UserProfile fromMap(Map<String, Object> map) {
        UserProfile profile = new UserProfile();
        profile.setUserId((String) map.get("userId"));
        profile.setFullName((String) map.get("fullName"));
        profile.setEmail((String) map.get("email"));
        profile.setPhoneNumber((String) map.get("phoneNumber"));
        profile.setAge((String) map.get("age"));
        profile.setGender((String) map.get("gender"));
        profile.setProfileImageUrl((String) map.get("profileImageUrl"));
        profile.setLoginProvider((String) map.get("loginProvider"));
        
        if (map.get("createdAt") instanceof Long) {
            profile.setCreatedAt((Long) map.get("createdAt"));
        }
        if (map.get("updatedAt") instanceof Long) {
            profile.setUpdatedAt((Long) map.get("updatedAt"));
        }
        
        return profile;
    }
}