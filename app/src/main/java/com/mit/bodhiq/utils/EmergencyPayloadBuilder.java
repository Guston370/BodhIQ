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
 * Format: Human-readable summary followed by structured JSON
 */
public class EmergencyPayloadBuilder {
    
    private static final int MAX_PAYLOAD_SIZE = 3 * 1024; // 3 KB limit
    private static final String SCHEMA_VERSION = "medical-agent.v1";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Build emergency payload with plaintext format for generic QR scanners
     */
    public static String build(UserProfile profile) {
        return buildPlaintextPayload(profile);
    }
    
    /**
     * Build plaintext payload for generic QR scanners
     * Format: Labeled plaintext with all essential emergency information
     */
    private static String buildPlaintextPayload(UserProfile profile) {
        StringBuilder payload = new StringBuilder();
        
        // Name (required)
        String fullName = !TextUtils.isEmpty(profile.getFullName()) ? 
            profile.getFullName() : "Unknown";
        payload.append("Name: ").append(fullName).append("\n");
        
        // Age
        String age = !TextUtils.isEmpty(profile.getAge()) ? 
            profile.getAge() : "Unknown";
        payload.append("Age: ").append(age).append("\n");
        
        // Blood Type
        String bloodType = !TextUtils.isEmpty(profile.getBloodGroup()) ? 
            profile.getBloodGroup() : "Unknown";
        payload.append("Blood Type: ").append(bloodType).append("\n");
        
        // Allergies
        String allergies = !TextUtils.isEmpty(profile.getAllergies()) ? 
            profile.getAllergies() : "None";
        payload.append("Allergies: ").append(allergies).append("\n");
        
        // Conditions (not currently in UserProfile, defaulting to "None")
        // Future: Could be added as a profile field
        payload.append("Conditions: None\n");
        
        // Emergency Contact
        String emergencyContact = extractEmergencyContactInfo(profile);
        payload.append("Emergency Contact: ").append(emergencyContact).append("\n");
        
        // Note (generic medical note)
        // Future: Could be customizable per user
        String note = generateDefaultNote(profile);
        payload.append("Note: ").append(note);
        
        return payload.toString();
    }
    
    /**
     * Extract emergency contact information (name + phone)
     */
    private static String extractEmergencyContactInfo(UserProfile profile) {
        if (!TextUtils.isEmpty(profile.getEmergencyContact())) {
            // Currently only stores phone number, parse if format is "Name Phone"
            String contact = profile.getEmergencyContact();
            // If it looks like just a phone number, add generic label
            if (contact.matches("^[+]?[0-9\\-\\s]+$")) {
                return "Emergency Contact " + contact;
            }
            return contact;
        }
        return "Unknown";
    }
    
    /**
     * Generate default medical note (max 120 chars)
     */
    private static String generateDefaultNote(UserProfile profile) {
        StringBuilder note = new StringBuilder();
        
        // Add relevant alerts based on profile
        if (!TextUtils.isEmpty(profile.getAllergies())) {
            note.append("Allergies noted. ");
        }
        
        if (!TextUtils.isEmpty(profile.getEmergencyContact())) {
            note.append("Call emergency contact for details.");
        } else {
            note.append("Check patient records for full medical history.");
        }
        
        // Truncate to 120 characters max
        String result = note.toString();
        if (result.length() > 120) {
            return result.substring(0, 117) + "...";
        }
        return result;
    }
    

    
    /**
     * Build compact emergency payload (same as standard plaintext format)
     */
    public static String buildCompact(UserProfile profile) {
        // Use same plaintext format - no compact variant needed
        return buildPlaintextPayload(profile);
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
     * Get schema version
     */
    public static String getSchemaVersion() {
        return SCHEMA_VERSION;
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
