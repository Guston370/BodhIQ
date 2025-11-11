package com.mit.bodhiq.chatbot;

import com.mit.bodhiq.data.model.ParsedField;

import java.util.HashMap;
import java.util.Map;

/**
 * Local reference ranges for common medical tests
 */
public class ReferenceRanges {
    
    private static final Map<String, TestRange> ranges = new HashMap<>();
    
    static {
        // Hematology
        ranges.put("hemoglobin", new TestRange("Hemoglobin", "g/dL", 12.0, 16.0, 11.0, 17.0));
        ranges.put("hgb", new TestRange("Hemoglobin", "g/dL", 12.0, 16.0, 11.0, 17.0));
        ranges.put("hb", new TestRange("Hemoglobin", "g/dL", 12.0, 16.0, 11.0, 17.0));
        
        ranges.put("wbc", new TestRange("White Blood Cells", "/μL", 4000, 11000, 3000, 15000));
        ranges.put("white blood cell", new TestRange("White Blood Cells", "/μL", 4000, 11000, 3000, 15000));
        
        ranges.put("rbc", new TestRange("Red Blood Cells", "million/μL", 4.5, 5.5, 4.0, 6.0));
        ranges.put("red blood cell", new TestRange("Red Blood Cells", "million/μL", 4.5, 5.5, 4.0, 6.0));
        
        ranges.put("platelet", new TestRange("Platelets", "x10³/μL", 150, 450, 100, 500));
        ranges.put("plt", new TestRange("Platelets", "x10³/μL", 150, 450, 100, 500));
        
        // Chemistry
        ranges.put("glucose", new TestRange("Blood Glucose", "mg/dL", 70, 140, 60, 180));
        ranges.put("blood sugar", new TestRange("Blood Glucose", "mg/dL", 70, 140, 60, 180));
        ranges.put("fbs", new TestRange("Fasting Blood Sugar", "mg/dL", 70, 100, 60, 126));
        
        ranges.put("cholesterol", new TestRange("Total Cholesterol", "mg/dL", 0, 200, 0, 240));
        ranges.put("total cholesterol", new TestRange("Total Cholesterol", "mg/dL", 0, 200, 0, 240));
        
        ranges.put("hdl", new TestRange("HDL Cholesterol", "mg/dL", 40, 200, 35, 250));
        ranges.put("ldl", new TestRange("LDL Cholesterol", "mg/dL", 0, 100, 0, 130));
        
        ranges.put("triglycerides", new TestRange("Triglycerides", "mg/dL", 0, 150, 0, 200));
        
        ranges.put("creatinine", new TestRange("Creatinine", "mg/dL", 0.6, 1.2, 0.5, 1.5));
        ranges.put("bun", new TestRange("Blood Urea Nitrogen", "mg/dL", 7, 20, 5, 25));
        
        // Liver function
        ranges.put("alt", new TestRange("ALT (SGPT)", "U/L", 0, 40, 0, 55));
        ranges.put("sgpt", new TestRange("ALT (SGPT)", "U/L", 0, 40, 0, 55));
        
        ranges.put("ast", new TestRange("AST (SGOT)", "U/L", 0, 40, 0, 55));
        ranges.put("sgot", new TestRange("AST (SGOT)", "U/L", 0, 40, 0, 55));
        
        ranges.put("bilirubin", new TestRange("Total Bilirubin", "mg/dL", 0.1, 1.2, 0, 1.5));
        
        // Thyroid
        ranges.put("tsh", new TestRange("TSH", "μIU/mL", 0.4, 4.0, 0.3, 5.0));
        ranges.put("t3", new TestRange("T3", "ng/dL", 80, 200, 70, 220));
        ranges.put("t4", new TestRange("T4", "μg/dL", 4.5, 12.0, 4.0, 13.0));
        
        // Electrolytes
        ranges.put("sodium", new TestRange("Sodium", "mEq/L", 136, 145, 135, 148));
        ranges.put("potassium", new TestRange("Potassium", "mEq/L", 3.5, 5.0, 3.0, 5.5));
        ranges.put("calcium", new TestRange("Calcium", "mg/dL", 8.5, 10.5, 8.0, 11.0));
    }
    
    /**
     * Get reference range for a test
     */
    public static TestRange getRange(String testName) {
        if (testName == null) return null;
        return ranges.get(testName.toLowerCase().trim());
    }
    
    /**
     * Evaluate a test value against reference range
     */
    public static ParsedField.Flag evaluateValue(String testName, double value) {
        TestRange range = getRange(testName);
        if (range == null) {
            return ParsedField.Flag.UNKNOWN;
        }
        
        if (value < range.criticalLow || value > range.criticalHigh) {
            return ParsedField.Flag.CRITICAL;
        } else if (value < range.normalLow) {
            return ParsedField.Flag.LOW;
        } else if (value > range.normalHigh) {
            return ParsedField.Flag.HIGH;
        } else if (value < range.normalLow * 1.1 || value > range.normalHigh * 0.9) {
            return ParsedField.Flag.BORDERLINE;
        } else {
            return ParsedField.Flag.NORMAL;
        }
    }
    
    /**
     * Get suggested action for a flagged value
     */
    public static String getSuggestedAction(ParsedField.Flag flag, String testName) {
        switch (flag) {
            case CRITICAL:
                return "Seek immediate medical attention";
            case HIGH:
            case LOW:
                return "Consult your healthcare provider soon";
            case BORDERLINE:
                return "Monitor and retest in 3-6 months";
            case NORMAL:
                return "Continue healthy lifestyle";
            default:
                return "Discuss with your doctor";
        }
    }
    
    /**
     * Get action level for a flag
     */
    public static ParsedField.ActionLevel getActionLevel(ParsedField.Flag flag) {
        switch (flag) {
            case CRITICAL:
                return ParsedField.ActionLevel.URGENT_CARE;
            case HIGH:
            case LOW:
                return ParsedField.ActionLevel.CONSULT_SPECIALIST;
            case BORDERLINE:
                return ParsedField.ActionLevel.SELF_CARE;
            default:
                return ParsedField.ActionLevel.NONE;
        }
    }
    
    /**
     * Test range data class
     */
    public static class TestRange {
        public final String displayName;
        public final String unit;
        public final double normalLow;
        public final double normalHigh;
        public final double criticalLow;
        public final double criticalHigh;
        
        public TestRange(String displayName, String unit, double normalLow, double normalHigh, 
                        double criticalLow, double criticalHigh) {
            this.displayName = displayName;
            this.unit = unit;
            this.normalLow = normalLow;
            this.normalHigh = normalHigh;
            this.criticalLow = criticalLow;
            this.criticalHigh = criticalHigh;
        }
        
        public String getRangeString() {
            return String.format("%.1f - %.1f %s", normalLow, normalHigh, unit);
        }
    }
}
