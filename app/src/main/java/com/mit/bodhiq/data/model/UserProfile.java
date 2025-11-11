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
    private String dateOfBirth;
    private String gender;
    private String address;
    private String bloodGroup;
    private String height;
    private String weight;
    private String allergies;
    private String emergencyContact;
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

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = height; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

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
        result.put("dateOfBirth", dateOfBirth);
        result.put("gender", gender);
        result.put("address", address);
        result.put("bloodGroup", bloodGroup);
        result.put("height", height);
        result.put("weight", weight);
        result.put("allergies", allergies);
        result.put("emergencyContact", emergencyContact);
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
        profile.setDateOfBirth((String) map.get("dateOfBirth"));
        profile.setGender((String) map.get("gender"));
        profile.setAddress((String) map.get("address"));
        profile.setBloodGroup((String) map.get("bloodGroup"));
        profile.setHeight((String) map.get("height"));
        profile.setWeight((String) map.get("weight"));
        profile.setAllergies((String) map.get("allergies"));
        profile.setEmergencyContact((String) map.get("emergencyContact"));
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