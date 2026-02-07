package com.mit.bodhiq;

import com.mit.bodhiq.data.model.UserProfile;
import com.mit.bodhiq.utils.EmergencyPayloadBuilder;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit tests to validate plaintext QR payload format
 */
public class PlaintextQrTest {

    @Test
    public void testPlaintextFormat_ContainsAllRequiredFields() {
        UserProfile profile = createTestProfile();
        String payload = EmergencyPayloadBuilder.build(profile);
        
        // Verify all required fields present
        assertTrue("Must contain Name field", payload.contains("Name: "));
        assertTrue("Must contain Age field", payload.contains("Age: "));
        assertTrue("Must contain Blood Type field", payload.contains("Blood Type: "));
        assertTrue("Must contain Allergies field", payload.contains("Allergies: "));
        assertTrue("Must contain Conditions field", payload.contains("Conditions: "));
        assertTrue("Must contain Emergency Contact field", payload.contains("Emergency Contact: "));
        assertTrue("Must contain Note field", payload.contains("Note: "));
    }
    
    @Test
    public void testPlaintextFormat_NoJsonContent() {
        UserProfile profile = createTestProfile();
        String payload = EmergencyPayloadBuilder.build(profile);
        
        // Verify no JSON structure present
        assertFalse("Must not contain JSON schema field", payload.contains("\"schema\""));
        assertFalse("Must not contain JSON braces", payload.contains("{") || payload.contains("}"));
        assertFalse("Must not contain structured data marker", payload.contains("---STRUCTURED-DATA---"));
    }
    
    @Test
    public void testPlaintextFormat_HandlesMissingFields() {
        // Create profile with missing fields
        UserProfile profile = new UserProfile();
        profile.setFullName("Test User");
        // Leave other fields null
        
        String payload = EmergencyPayloadBuilder.build(profile);
        
        // Should have fallback values
        assertTrue("Missing age should show Unknown", payload.contains("Age: Unknown"));
        assertTrue("Missing blood type should show Unknown", payload.contains("Blood Type: Unknown"));
        assertTrue("Missing allergies should show None", payload.contains("Allergies: None"));
        assertTrue("Missing conditions should show None", payload.contains("Conditions: None"));
    }
    
    @Test
    public void testPlaintextFormat_UTF8Encoding() {
        UserProfile profile = createTestProfile();
        String payload = EmergencyPayloadBuilder.build(profile);
        
        // Verify UTF-8 can handle the payload
        byte[] bytes = payload.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String decoded = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        
        assertEquals("UTF-8 encoding should be preserved", payload, decoded);
    }
    
    @Test
    public void testPlaintextFormat_ReasonableSize() {
        UserProfile profile = createTestProfile();
        String payload = EmergencyPayloadBuilder.build(profile);
        
        int payloadSize = payload.getBytes().length;
        
        // Plaintext should be compact (typically under 500 bytes for normal data)
        assertTrue("Payload should be under 500 bytes for typical data, was: " + payloadSize, 
            payloadSize < 500);
    }
    
    @Test
    public void testEmergencyContact_ExtractsPhoneNumber() {
        UserProfile profile = new UserProfile();
        profile.setFullName("Test User");
        profile.setEmergencyContact("+91-98123XXXXX");
        
        String payload = EmergencyPayloadBuilder.build(profile);
        
        // Should include emergency contact
        assertTrue("Should contain emergency contact phone", 
            payload.contains("+91-98123XXXXX"));
    }
    
    @Test
    public void testCompactFormat_SameAsStandard() {
        UserProfile profile = createTestProfile();
        
        String standardPayload = EmergencyPayloadBuilder.build(profile);
        String compactPayload = EmergencyPayloadBuilder.buildCompact(profile);
        
        assertEquals("Compact format should be same as standard plaintext", 
            standardPayload, compactPayload);
    }
    
    /**
     * Helper to create test profile
     */
    private UserProfile createTestProfile() {
        UserProfile profile = new UserProfile();
        profile.setFullName("John Doe");
        profile.setAge("35");
        profile.setBloodGroup("A+");
        profile.setAllergies("Peanuts, Latex");
        profile.setEmergencyContact("Emergency Contact +1-555-0123");
        return profile;
    }
}
