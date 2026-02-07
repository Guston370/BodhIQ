package com.mit.bodhiq.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes symptom inputs to ensure consistent processing
 * Handles common misspellings, unit conversions, and standardization
 */
public class SymptomNormalizer {
    
    private static final Map<String, String> COMMON_MISSPELLINGS = new HashMap<>();
    private static final Map<String, String> SYMPTOM_SYNONYMS = new HashMap<>();
    
    static {
        // Common misspellings
        COMMON_MISSPELLINGS.put("feaver", "fever");
        COMMON_MISSPELLINGS.put("feaver", "fever");
        COMMON_MISSPELLINGS.put("hedache", "headache");
        COMMON_MISSPELLINGS.put("headake", "headache");
        COMMON_MISSPELLINGS.put("stomache", "stomach");
        COMMON_MISSPELLINGS.put("stomachache", "stomach ache");
        COMMON_MISSPELLINGS.put("diarhea", "diarrhea");
        COMMON_MISSPELLINGS.put("diarrhoea", "diarrhea");
        COMMON_MISSPELLINGS.put("nausia", "nausea");
        COMMON_MISSPELLINGS.put("nausious", "nauseous");
        COMMON_MISSPELLINGS.put("dizzy", "dizziness");
        COMMON_MISSPELLINGS.put("coff", "cough");
        COMMON_MISSPELLINGS.put("coughing", "cough");
        
        // Symptom synonyms
        SYMPTOM_SYNONYMS.put("can't breathe", "difficulty breathing");
        SYMPTOM_SYNONYMS.put("cannot breathe", "difficulty breathing");
        SYMPTOM_SYNONYMS.put("hard to breathe", "difficulty breathing");
        SYMPTOM_SYNONYMS.put("short of breath", "shortness of breath");
        SYMPTOM_SYNONYMS.put("sob", "shortness of breath");
        SYMPTOM_SYNONYMS.put("throwing up", "vomiting");
        SYMPTOM_SYNONYMS.put("puking", "vomiting");
        SYMPTOM_SYNONYMS.put("feeling sick", "nausea");
        SYMPTOM_SYNONYMS.put("tummy ache", "stomach ache");
        SYMPTOM_SYNONYMS.put("belly pain", "abdominal pain");
    }
    
    /**
     * Normalize symptom text for consistent processing
     */
    public static String normalize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        
        String normalized = input.toLowerCase().trim();
        
        // Fix common misspellings
        for (Map.Entry<String, String> entry : COMMON_MISSPELLINGS.entrySet()) {
            normalized = normalized.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        
        // Replace synonyms
        for (Map.Entry<String, String> entry : SYMPTOM_SYNONYMS.entrySet()) {
            normalized = normalized.replace(entry.getKey(), entry.getValue());
        }
        
        // Normalize temperature units
        normalized = normalizeTemperature(normalized);
        
        // Normalize duration expressions
        normalized = normalizeDuration(normalized);
        
        // Remove extra whitespace
        normalized = normalized.replaceAll("\\s+", " ").trim();
        
        return normalized;
    }
    
    /**
     * Normalize temperature expressions to Celsius
     */
    private static String normalizeTemperature(String text) {
        // Pattern for Fahrenheit (e.g., "102F", "102 F", "102°F")
        Pattern fahrenheitPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[°]?\\s*f\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = fahrenheitPattern.matcher(text);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            double fahrenheit = Double.parseDouble(matcher.group(1));
            double celsius = (fahrenheit - 32) * 5.0 / 9.0;
            matcher.appendReplacement(result, String.format("%.1f°C", celsius));
        }
        matcher.appendTail(result);
        
        // Ensure Celsius has proper format
        text = result.toString();
        text = text.replaceAll("(\\d+(?:\\.\\d+)?)\\s*[°]?\\s*c\\b", "$1°C");
        
        return text;
    }
    
    /**
     * Normalize duration expressions
     */
    private static String normalizeDuration(String text) {
        // Normalize "2 days" vs "2days" vs "two days"
        text = text.replaceAll("(\\d+)\\s*days?", "$1 days");
        text = text.replaceAll("(\\d+)\\s*hours?", "$1 hours");
        text = text.replaceAll("(\\d+)\\s*weeks?", "$1 weeks");
        text = text.replaceAll("(\\d+)\\s*months?", "$1 months");
        
        // Convert word numbers to digits for common cases
        text = text.replaceAll("\\bone\\s+day", "1 day");
        text = text.replaceAll("\\btwo\\s+days", "2 days");
        text = text.replaceAll("\\bthree\\s+days", "3 days");
        text = text.replaceAll("\\ba\\s+week", "1 week");
        text = text.replaceAll("\\ba\\s+few\\s+days", "2-3 days");
        
        return text;
    }
    
    /**
     * Extract key symptoms from normalized text
     */
    public static String[] extractKeySymptoms(String normalizedText) {
        // Simple extraction - can be enhanced with NLP
        String[] commonSymptoms = {
            "fever", "headache", "cough", "sore throat", "chest pain",
            "difficulty breathing", "shortness of breath", "nausea", "vomiting",
            "diarrhea", "abdominal pain", "dizziness", "fatigue", "weakness",
            "rash", "swelling", "bleeding", "pain"
        };
        
        java.util.List<String> found = new java.util.ArrayList<>();
        for (String symptom : commonSymptoms) {
            if (normalizedText.contains(symptom)) {
                found.add(symptom);
            }
        }
        
        return found.toArray(new String[0]);
    }
}
