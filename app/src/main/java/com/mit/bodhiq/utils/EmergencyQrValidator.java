package com.mit.bodhiq.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Validator for Emergency QR code payloads
 * Ensures format compliance and readability
 */
public class EmergencyQrValidator {
    
    private static final String SEPARATOR = "---STRUCTURED-DATA---";
    private static final String EXPECTED_SCHEMA = "medical-agent.v1";
    
    /**
     * Validate QR payload format
     */
    public static ValidationResult validate(String payload) {
        ValidationResult result = new ValidationResult();
        
        if (payload == null || payload.isEmpty()) {
            result.isValid = false;
            result.error = "Payload is empty";
            return result;
        }
        
        // Check for human-readable summary
        if (!payload.contains("EMERGENCY")) {
            result.isValid = false;
            result.error = "Missing human-readable summary";
            return result;
        }
        
        // Check for structured data separator
        if (!payload.contains(SEPARATOR)) {
            result.isValid = false;
            result.error = "Missing structured data separator";
            return result;
        }
        
        // Extract and validate JSON
        String[] parts = payload.split(SEPARATOR);
        if (parts.length < 2) {
            result.isValid = false;
            result.error = "Invalid payload structure";
            return result;
        }
        
        result.humanReadable = parts[0].trim();
        String jsonPart = parts[1].trim();
        
        try {
            JsonObject json = JsonParser.parseString(jsonPart).getAsJsonObject();
            
            // Validate schema
            if (!json.has("schema")) {
                result.isValid = false;
                result.error = "Missing schema field";
                return result;
            }
            
            String schema = json.get("schema").getAsString();
            if (!schema.equals(EXPECTED_SCHEMA)) {
                result.warning = "Schema version mismatch: " + schema;
            }
            
            // Validate required fields
            if (!json.has("patient")) {
                result.isValid = false;
                result.error = "Missing patient information";
                return result;
            }
            
            JsonObject patient = json.getAsJsonObject("patient");
            if (!patient.has("fullName")) {
                result.isValid = false;
                result.error = "Missing patient name";
                return result;
            }
            
            result.structuredData = jsonPart;
            result.isValid = true;
            result.schema = schema;
            
        } catch (Exception e) {
            result.isValid = false;
            result.error = "Invalid JSON structure: " + e.getMessage();
            return result;
        }
        
        return result;
    }
    
    /**
     * Extract human-readable summary from payload
     */
    public static String extractSummary(String payload) {
        if (payload == null || !payload.contains(SEPARATOR)) {
            return payload;
        }
        return payload.split(SEPARATOR)[0].trim();
    }
    
    /**
     * Extract structured JSON from payload
     */
    public static String extractJson(String payload) {
        if (payload == null || !payload.contains(SEPARATOR)) {
            return null;
        }
        String[] parts = payload.split(SEPARATOR);
        return parts.length > 1 ? parts[1].trim() : null;
    }
    
    /**
     * Validation result
     */
    public static class ValidationResult {
        public boolean isValid = false;
        public String error = null;
        public String warning = null;
        public String humanReadable = null;
        public String structuredData = null;
        public String schema = null;
        
        public String getMessage() {
            if (!isValid) {
                return "Invalid: " + error;
            }
            if (warning != null) {
                return "Valid (Warning: " + warning + ")";
            }
            return "Valid";
        }
    }
}
