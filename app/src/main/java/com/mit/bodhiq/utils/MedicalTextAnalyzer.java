package com.mit.bodhiq.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.mit.bodhiq.data.model.MedicalParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Advanced medical text analyzer using ML Kit with intelligent parameter extraction
 */
@Singleton
public class MedicalTextAnalyzer {
    private static final String TAG = "MedicalTextAnalyzer";
    
    private final TextRecognizer textRecognizer;
    
    // Medical parameter patterns
    private static final Map<String, Pattern> PARAMETER_PATTERNS = new HashMap<>();
    private static final Map<String, String[]> PARAMETER_UNITS = new HashMap<>();
    private static final Map<String, String[]> REFERENCE_RANGES = new HashMap<>();
    
    static {
        initializePatterns();
        initializeUnits();
        initializeReferenceRanges();
    }
    
    @Inject
    public MedicalTextAnalyzer() {
        this.textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }
    
    /**
     * Extract text and medical parameters from image
     */
    public CompletableFuture<MedicalAnalysisResult> analyzeImage(Bitmap bitmap) {
        CompletableFuture<MedicalAnalysisResult> future = new CompletableFuture<>();
        
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            
            textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String extractedText = visionText.getText();
                    List<MedicalParameter> parameters = extractMedicalParameters(extractedText);
                    
                    MedicalAnalysisResult result = new MedicalAnalysisResult();
                    result.setExtractedText(extractedText);
                    result.setMedicalParameters(parameters);
                    result.setSuccess(true);
                    
                    future.complete(result);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text recognition failed", e);
                    MedicalAnalysisResult result = new MedicalAnalysisResult();
                    result.setSuccess(false);
                    result.setErrorMessage(e.getMessage());
                    future.complete(result);
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Image processing failed", e);
            MedicalAnalysisResult result = new MedicalAnalysisResult();
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            future.complete(result);
        }
        
        return future;
    }
    
    /**
     * Extract medical parameters from text using intelligent pattern matching
     */
    public List<MedicalParameter> extractMedicalParameters(String text) {
        List<MedicalParameter> parameters = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return parameters;
        }
        
        String normalizedText = text.toLowerCase().replaceAll("\\s+", " ");
        
        // Extract parameters using patterns
        for (Map.Entry<String, Pattern> entry : PARAMETER_PATTERNS.entrySet()) {
            String parameterName = entry.getKey();
            Pattern pattern = entry.getValue();
            
            Matcher matcher = pattern.matcher(normalizedText);
            while (matcher.find()) {
                try {
                    String value = matcher.group(1);
                    String unit = matcher.groupCount() > 1 ? matcher.group(2) : "";
                    
                    // Clean and validate the value
                    value = cleanValue(value);
                    if (isValidValue(value)) {
                        MedicalParameter parameter = new MedicalParameter();
                        parameter.setParameter(parameterName);
                        parameter.setValue(value);
                        parameter.setUnit(determineUnit(parameterName, unit));
                        parameter.setStatus(determineStatus(parameterName, value));
                        parameter.setReferenceRange(getReferenceRange(parameterName));
                        
                        parameters.add(parameter);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing parameter: " + parameterName, e);
                }
            }
        }
        
        return parameters;
    }
    
    private static void initializePatterns() {
        // Blood work patterns
        PARAMETER_PATTERNS.put("Hemoglobin", Pattern.compile("(?:hemoglobin|hgb|hb)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("Hematocrit", Pattern.compile("(?:hematocrit|hct)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([%a-z/]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("White Blood Cells", Pattern.compile("(?:white blood cells?|wbc|leucocytes?)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/μ³]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("Red Blood Cells", Pattern.compile("(?:red blood cells?|rbc|erythrocytes?)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/μ³]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("Platelets", Pattern.compile("(?:platelets?|plt)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/μ³]+)?", Pattern.CASE_INSENSITIVE));
        
        // Metabolic panel
        PARAMETER_PATTERNS.put("Glucose", Pattern.compile("(?:glucose|blood sugar|fasting glucose)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("Cholesterol Total", Pattern.compile("(?:total cholesterol|cholesterol)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("HDL Cholesterol", Pattern.compile("(?:hdl cholesterol|hdl)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("LDL Cholesterol", Pattern.compile("(?:ldl cholesterol|ldl)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("Triglycerides", Pattern.compile("(?:triglycerides?|tg)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        
        // Liver function
        PARAMETER_PATTERNS.put("ALT", Pattern.compile("(?:alt|alanine aminotransferase)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("AST", Pattern.compile("(?:ast|aspartate aminotransferase)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("Bilirubin Total", Pattern.compile("(?:total bilirubin|bilirubin)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        
        // Kidney function
        PARAMETER_PATTERNS.put("Creatinine", Pattern.compile("(?:creatinine|creat)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("BUN", Pattern.compile("(?:bun|blood urea nitrogen|urea)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        
        // Thyroid function
        PARAMETER_PATTERNS.put("TSH", Pattern.compile("(?:tsh|thyroid stimulating hormone)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/μ]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("T3", Pattern.compile("(?:t3|triiodothyronine)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("T4", Pattern.compile("(?:t4|thyroxine)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*([a-z/]+)?", Pattern.CASE_INSENSITIVE));
        
        // Vital signs
        PARAMETER_PATTERNS.put("Blood Pressure Systolic", Pattern.compile("(?:systolic|blood pressure)\\s*:?\\s*([0-9]+)\\s*(?:/[0-9]+)?\\s*(mmhg)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("Blood Pressure Diastolic", Pattern.compile("(?:diastolic|[0-9]+/)\\s*([0-9]+)\\s*(mmhg)?", Pattern.CASE_INSENSITIVE));
        PARAMETER_PATTERNS.put("Heart Rate", Pattern.compile("(?:heart rate|pulse|hr)\\s*:?\\s*([0-9]+)\\s*(bpm)?", Pattern.CASE_INSENSITIVE));
    }
    
    private static void initializeUnits() {
        PARAMETER_UNITS.put("Hemoglobin", new String[]{"g/dL", "g/L"});
        PARAMETER_UNITS.put("Hematocrit", new String[]{"%"});
        PARAMETER_UNITS.put("White Blood Cells", new String[]{"×10³/μL", "K/μL"});
        PARAMETER_UNITS.put("Red Blood Cells", new String[]{"×10⁶/μL", "M/μL"});
        PARAMETER_UNITS.put("Platelets", new String[]{"×10³/μL", "K/μL"});
        PARAMETER_UNITS.put("Glucose", new String[]{"mg/dL", "mmol/L"});
        PARAMETER_UNITS.put("Cholesterol Total", new String[]{"mg/dL", "mmol/L"});
        PARAMETER_UNITS.put("HDL Cholesterol", new String[]{"mg/dL", "mmol/L"});
        PARAMETER_UNITS.put("LDL Cholesterol", new String[]{"mg/dL", "mmol/L"});
        PARAMETER_UNITS.put("Triglycerides", new String[]{"mg/dL", "mmol/L"});
        PARAMETER_UNITS.put("ALT", new String[]{"U/L", "IU/L"});
        PARAMETER_UNITS.put("AST", new String[]{"U/L", "IU/L"});
        PARAMETER_UNITS.put("Bilirubin Total", new String[]{"mg/dL", "μmol/L"});
        PARAMETER_UNITS.put("Creatinine", new String[]{"mg/dL", "μmol/L"});
        PARAMETER_UNITS.put("BUN", new String[]{"mg/dL", "mmol/L"});
        PARAMETER_UNITS.put("TSH", new String[]{"mIU/L", "μIU/mL"});
        PARAMETER_UNITS.put("T3", new String[]{"ng/dL", "nmol/L"});
        PARAMETER_UNITS.put("T4", new String[]{"μg/dL", "nmol/L"});
        PARAMETER_UNITS.put("Blood Pressure Systolic", new String[]{"mmHg"});
        PARAMETER_UNITS.put("Blood Pressure Diastolic", new String[]{"mmHg"});
        PARAMETER_UNITS.put("Heart Rate", new String[]{"bpm"});
    }
    
    private static void initializeReferenceRanges() {
        REFERENCE_RANGES.put("Hemoglobin", new String[]{"12.0-15.5 g/dL (F)", "13.5-17.5 g/dL (M)"});
        REFERENCE_RANGES.put("Hematocrit", new String[]{"36-46% (F)", "41-53% (M)"});
        REFERENCE_RANGES.put("White Blood Cells", new String[]{"4.0-11.0 ×10³/μL"});
        REFERENCE_RANGES.put("Red Blood Cells", new String[]{"4.0-5.2 ×10⁶/μL (F)", "4.5-5.9 ×10⁶/μL (M)"});
        REFERENCE_RANGES.put("Platelets", new String[]{"150-450 ×10³/μL"});
        REFERENCE_RANGES.put("Glucose", new String[]{"70-100 mg/dL (fasting)"});
        REFERENCE_RANGES.put("Cholesterol Total", new String[]{"<200 mg/dL"});
        REFERENCE_RANGES.put("HDL Cholesterol", new String[]{">40 mg/dL (M)", ">50 mg/dL (F)"});
        REFERENCE_RANGES.put("LDL Cholesterol", new String[]{"<100 mg/dL"});
        REFERENCE_RANGES.put("Triglycerides", new String[]{"<150 mg/dL"});
        REFERENCE_RANGES.put("ALT", new String[]{"7-56 U/L"});
        REFERENCE_RANGES.put("AST", new String[]{"10-40 U/L"});
        REFERENCE_RANGES.put("Bilirubin Total", new String[]{"0.2-1.2 mg/dL"});
        REFERENCE_RANGES.put("Creatinine", new String[]{"0.6-1.2 mg/dL"});
        REFERENCE_RANGES.put("BUN", new String[]{"7-20 mg/dL"});
        REFERENCE_RANGES.put("TSH", new String[]{"0.4-4.0 mIU/L"});
        REFERENCE_RANGES.put("T3", new String[]{"80-200 ng/dL"});
        REFERENCE_RANGES.put("T4", new String[]{"5.0-12.0 μg/dL"});
        REFERENCE_RANGES.put("Blood Pressure Systolic", new String[]{"<120 mmHg"});
        REFERENCE_RANGES.put("Blood Pressure Diastolic", new String[]{"<80 mmHg"});
        REFERENCE_RANGES.put("Heart Rate", new String[]{"60-100 bpm"});
    }
    
    private String cleanValue(String value) {
        if (value == null) return "";
        return value.trim().replaceAll("[^0-9.]", "");
    }
    
    private boolean isValidValue(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private String determineUnit(String parameterName, String extractedUnit) {
        String[] possibleUnits = PARAMETER_UNITS.get(parameterName);
        if (possibleUnits != null && possibleUnits.length > 0) {
            // If we extracted a unit, try to match it
            if (extractedUnit != null && !extractedUnit.trim().isEmpty()) {
                for (String unit : possibleUnits) {
                    if (unit.toLowerCase().contains(extractedUnit.toLowerCase()) ||
                        extractedUnit.toLowerCase().contains(unit.toLowerCase())) {
                        return unit;
                    }
                }
            }
            // Return the most common unit
            return possibleUnits[0];
        }
        return extractedUnit != null ? extractedUnit : "";
    }
    
    private String determineStatus(String parameterName, String value) {
        try {
            double numValue = Double.parseDouble(value);
            
            // Simple status determination based on common ranges
            // This is a simplified version - in practice, you'd want more sophisticated logic
            switch (parameterName) {
                case "Glucose":
                    if (numValue < 70) return "LOW";
                    if (numValue > 100) return "HIGH";
                    return "NORMAL";
                    
                case "Cholesterol Total":
                    if (numValue > 200) return "HIGH";
                    return "NORMAL";
                    
                case "Blood Pressure Systolic":
                    if (numValue < 90) return "LOW";
                    if (numValue > 120) return "HIGH";
                    return "NORMAL";
                    
                case "Blood Pressure Diastolic":
                    if (numValue < 60) return "LOW";
                    if (numValue > 80) return "HIGH";
                    return "NORMAL";
                    
                case "Heart Rate":
                    if (numValue < 60) return "LOW";
                    if (numValue > 100) return "HIGH";
                    return "NORMAL";
                    
                default:
                    return "NORMAL"; // Default to normal if we can't determine
            }
        } catch (NumberFormatException e) {
            return "UNKNOWN";
        }
    }
    
    private String getReferenceRange(String parameterName) {
        String[] ranges = REFERENCE_RANGES.get(parameterName);
        if (ranges != null && ranges.length > 0) {
            return String.join(", ", ranges);
        }
        return "";
    }
    
    /**
     * Result class for medical analysis
     */
    public static class MedicalAnalysisResult {
        private boolean success;
        private String extractedText;
        private List<MedicalParameter> medicalParameters;
        private String errorMessage;
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getExtractedText() { return extractedText; }
        public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
        
        public List<MedicalParameter> getMedicalParameters() { return medicalParameters; }
        public void setMedicalParameters(List<MedicalParameter> medicalParameters) { this.medicalParameters = medicalParameters; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}