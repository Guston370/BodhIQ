package com.mit.bodhiq.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mit.bodhiq.data.model.EmergencyContact;
import com.mit.bodhiq.data.model.UserProfile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builder for Emergency QR code payload
 */
public class EmergencyPayloadBuilder {
    
    private static final int MAX_PAYLOAD_SIZE = 3 * 1024; // 3 KB limit
    private static final Gson gson = new GsonBuilder().create();
    
    /**
     * Build emergency payload from user profile
     */
    public static String build(UserProfile profile) {
        Map<String, Object> payload = new HashMap<>();
        
        payload.put("type", "emergency");
        payload.put("version", 1);
        payload.put("userId", profile.getUserId());
        payload.put("fullName", profile.getFullName());
        
        // Optional fields
        if (!TextUtils.isEmpty(profile.getDateOfBirth())) {
            payload.put("dob", profile.getDateOfBirth());
        }
        
        if (!TextUtils.isEmpty(profile.getBloodGroup())) {
            payload.put("bloodGroup", profile.getBloodGroup());
        }
        
        // Parse allergies
        if (!TextUtils.isEmpty(profile.getAllergies())) {
            List<String> allergies = parseCommaSeparated(profile.getAllergies());
            if (!allergies.isEmpty()) {
                payload.put("allergies", allergies);
            }
        }
        
        // Medical conditions (placeholder - can be extended)
        List<String> medicalConditions = new ArrayList<>();
        payload.put("medicalConditions", medicalConditions);
        
        // Emergency contacts
        List<EmergencyContact> emergencyContacts = new ArrayList<>();
        if (!TextUtils.isEmpty(profile.getEmergencyContact())) {
            EmergencyContact contact = new EmergencyContact(
                "Emergency Contact",
                "Primary",
                profile.getEmergencyContact()
            );
            emergencyContacts.add(contact);
        }
        payload.put("emergencyContacts", emergencyContacts);
        
        // Primary phone
        if (!TextUtils.isEmpty(profile.getPhoneNumber())) {
            payload.put("primaryPhone", profile.getPhoneNumber());
        }
        
        // Notes (optional)
        payload.put("notes", "");
        
        // Timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        payload.put("issuedAt", sdf.format(new Date()));
        
        return gson.toJson(payload);
    }
    
    /**
     * Check if payload exceeds size limit
     */
    public static boolean exceedsSizeLimit(String payload) {
        return payload.getBytes().length > MAX_PAYLOAD_SIZE;
    }
    
    /**
     * Get payload size in bytes
     */
    public static int getPayloadSize(String payload) {
        return payload.getBytes().length;
    }
    
    /**
     * Parse comma-separated string into list
     */
    private static List<String> parseCommaSeparated(String input) {
        List<String> result = new ArrayList<>();
        if (TextUtils.isEmpty(input)) {
            return result;
        }
        
        String[] parts = input.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
    
    /**
     * Build vCard format as alternative
     */
    public static String buildVCard(UserProfile profile) {
        StringBuilder vcard = new StringBuilder();
        vcard.append("BEGIN:VCARD\n");
        vcard.append("VERSION:3.0\n");
        vcard.append("FN:").append(profile.getFullName()).append("\n");
        
        if (!TextUtils.isEmpty(profile.getEmail())) {
            vcard.append("EMAIL:").append(profile.getEmail()).append("\n");
        }
        
        if (!TextUtils.isEmpty(profile.getPhoneNumber())) {
            vcard.append("TEL:").append(profile.getPhoneNumber()).append("\n");
        }
        
        if (!TextUtils.isEmpty(profile.getEmergencyContact())) {
            vcard.append("TEL;TYPE=EMERGENCY:").append(profile.getEmergencyContact()).append("\n");
        }
        
        if (!TextUtils.isEmpty(profile.getBloodGroup())) {
            vcard.append("NOTE:Blood Group: ").append(profile.getBloodGroup());
            if (!TextUtils.isEmpty(profile.getAllergies())) {
                vcard.append(" | Allergies: ").append(profile.getAllergies());
            }
            vcard.append("\n");
        }
        
        vcard.append("END:VCARD");
        return vcard.toString();
    }
}
