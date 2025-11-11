package com.mit.bodhiq.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.content.Context;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.mit.bodhiq.data.model.HealthValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Enhanced service for text recognition using ML Kit Text Recognition V2
 * Supports both printed and handwritten text commonly found in medical reports
 */
public class TextRecognitionService {
    
    private static final String TAG = "TextRecognitionService";
    
    private final TextRecognizer textRecognizer;
    private final Context context;
    
    public TextRecognitionService(Context context) {
        this.context = context;
        // Using ML Kit Text Recognition V2 with enhanced Latin script support
        // This model works offline and supports both printed and handwritten text
        this.textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Log.d(TAG, "TextRecognitionService initialized with ML Kit v2");
    }
    
    /**
     * Extract text from image URI using ML Kit Text Recognition V2
     * Automatically processes the image and extracts all readable text
     */
    public Single<String> extractTextFromImage(Uri imageUri) {
        return Single.fromCallable(() -> {
            try {
                Log.d(TAG, "Starting text extraction from URI: " + imageUri);
                InputImage image = InputImage.fromFilePath(context, imageUri);
                return processImage(image);
            } catch (IOException e) {
                Log.e(TAG, "Failed to load image from URI", e);
                throw new RuntimeException("Failed to process image: " + e.getMessage(), e);
            }
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Extract text from bitmap using ML Kit Text Recognition V2
     */
    public Single<String> extractTextFromBitmap(Bitmap bitmap) {
        return Single.fromCallable(() -> {
            Log.d(TAG, "Starting text extraction from bitmap");
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            return processImage(image);
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Process image and extract text using ML Kit Text Recognition V2
     * Supports both printed and handwritten text
     */
    private String processImage(InputImage image) throws Exception {
        Log.d(TAG, "Processing image with ML Kit Text Recognition V2");
        
        return textRecognizer.process(image)
            .continueWith(task -> {
                if (task.isSuccessful()) {
                    Text visionText = task.getResult();
                    String extractedText = visionText.getText();
                    
                    if (extractedText == null || extractedText.trim().isEmpty()) {
                        Log.w(TAG, "No text detected in image");
                        throw new RuntimeException("No text found. Please retake the photo.");
                    }
                    
                    // Log extraction details
                    Log.d(TAG, "Text extraction successful");
                    Log.d(TAG, "Detected blocks: " + visionText.getTextBlocks().size());
                    Log.d(TAG, "Extracted text length: " + extractedText.length() + " characters");
                    
                    // Enhanced text with block structure for better parsing
                    StringBuilder enhancedText = new StringBuilder();
                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                        enhancedText.append(block.getText()).append("\n");
                    }
                    
                    String finalText = enhancedText.toString().trim();
                    if (finalText.isEmpty()) {
                        throw new RuntimeException("No text found. Please retake the photo.");
                    }
                    
                    return finalText;
                } else {
                    Exception exception = task.getException();
                    Log.e(TAG, "Text recognition failed", exception);
                    throw new RuntimeException("Text recognition failed: " + 
                        (exception != null ? exception.getMessage() : "Unknown error"));
                }
            }).getResult();
    }
    
    /**
     * Parse health values from extracted text
     */
    public List<HealthValue> parseHealthValues(String extractedText) {
        List<HealthValue> healthValues = new ArrayList<>();
        
        // Health parameter patterns with regex
        String[][] patterns = {
            // Hemoglobin patterns
            {"Hemoglobin|Hgb|HB", "([0-9]+\\.?[0-9]*)", "(g/dl|g/dL|gm/dl)", "12.0-16.0"},
            // Blood pressure patterns  
            {"Blood Pressure|BP", "([0-9]+)/([0-9]+)", "(mmHg|mm Hg)", "90-140/60-90"},
            // Cholesterol patterns
            {"Cholesterol|Total Cholesterol", "([0-9]+\\.?[0-9]*)", "(mg/dl|mg/dL)", "<200"},
            // Blood sugar patterns
            {"Glucose|Blood Sugar|FBS|RBS", "([0-9]+\\.?[0-9]*)", "(mg/dl|mg/dL)", "70-140"},
            // White blood cell patterns
            {"WBC|White Blood Cell", "([0-9]+\\.?[0-9]*)", "(cells/μl|/μl|x10³/μl)", "4000-11000"},
            // Red blood cell patterns
            {"RBC|Red Blood Cell", "([0-9]+\\.?[0-9]*)", "(million/μl|x10⁶/μl)", "4.5-5.5"},
            // Platelet patterns
            {"Platelet|PLT", "([0-9]+\\.?[0-9]*)", "(x10³/μl|/μl|lakh/μl)", "150000-450000"}
        };
        
        for (String[] patternData : patterns) {
            String paramPattern = patternData[0];
            String valuePattern = patternData[1];
            String unitPattern = patternData[2];
            String normalRange = patternData[3];
            
            // Create regex pattern
            String fullPattern = "(?i)(" + paramPattern + ")\\s*:?\\s*" + valuePattern + "\\s*(" + unitPattern + ")?";
            Pattern pattern = Pattern.compile(fullPattern);
            Matcher matcher = pattern.matcher(extractedText);
            
            while (matcher.find()) {
                String parameter = matcher.group(1).trim();
                String value = matcher.group(2).trim();
                String unit = matcher.groupCount() > 3 && matcher.group(4) != null ? 
                    matcher.group(4).trim() : "";
                
                // Handle blood pressure specially (has two values)
                if (parameter.toLowerCase().contains("blood pressure") || 
                    parameter.toLowerCase().contains("bp")) {
                    if (matcher.groupCount() > 2 && matcher.group(3) != null) {
                        value = value + "/" + matcher.group(3);
                    }
                }
                
                HealthValue healthValue = new HealthValue(parameter, value, unit, 
                    determineStatus(parameter, value, normalRange));
                healthValue.setNormalRange(normalRange);
                healthValues.add(healthValue);
            }
        }
        
        return healthValues;
    }
    
    /**
     * Determine if health value is normal, high, or low
     */
    private String determineStatus(String parameter, String value, String normalRange) {
        try {
            String paramLower = parameter.toLowerCase();
            
            // Handle blood pressure
            if (paramLower.contains("blood pressure") || paramLower.contains("bp")) {
                String[] parts = value.split("/");
                if (parts.length == 2) {
                    int systolic = Integer.parseInt(parts[0].trim());
                    int diastolic = Integer.parseInt(parts[1].trim());
                    if (systolic > 140 || diastolic > 90) return "high";
                    if (systolic < 90 || diastolic < 60) return "low";
                    return "normal";
                }
                return "unknown";
            }
            
            // Handle numeric values
            double numValue = Double.parseDouble(value.replaceAll("[^0-9.]", ""));
            
            if (paramLower.contains("hemoglobin") || paramLower.contains("hgb") || paramLower.contains("hb")) {
                return (numValue < 12.0) ? "low" : (numValue > 16.0) ? "high" : "normal";
            } else if (paramLower.contains("glucose") || paramLower.contains("sugar")) {
                return (numValue < 70) ? "low" : (numValue > 140) ? "high" : "normal";
            } else if (paramLower.contains("cholesterol")) {
                return (numValue > 200) ? "high" : "normal";
            } else if (paramLower.contains("wbc")) {
                return (numValue < 4000) ? "low" : (numValue > 11000) ? "high" : "normal";
            } else if (paramLower.contains("rbc")) {
                return (numValue < 4.5) ? "low" : (numValue > 5.5) ? "high" : "normal";
            } else if (paramLower.contains("platelet")) {
                return (numValue < 150000) ? "low" : (numValue > 450000) ? "high" : "normal";
            }
            
        } catch (NumberFormatException e) {
            return "unknown";
        }
        
        return "unknown";
    }
    
    /**
     * Generate smart suggestions for diagnosis (placeholder)
     */
    public String generateHealthSuggestions(List<HealthValue> healthValues) {
        if (healthValues.isEmpty()) {
            return "No health parameters detected. Please ensure the image contains clear medical report data.";
        }
        
        StringBuilder suggestions = new StringBuilder();
        suggestions.append("Health Analysis Summary:\n\n");
        
        int normalCount = 0;
        int abnormalCount = 0;
        
        for (HealthValue value : healthValues) {
            if ("normal".equals(value.getStatus())) {
                normalCount++;
            } else if ("high".equals(value.getStatus()) || "low".equals(value.getStatus())) {
                abnormalCount++;
            }
        }
        
        suggestions.append(String.format("Parameters analyzed: %d\n", healthValues.size()));
        suggestions.append(String.format("Normal values: %d\n", normalCount));
        suggestions.append(String.format("Values requiring attention: %d\n\n", abnormalCount));
        
        if (abnormalCount > 0) {
            suggestions.append("Parameters that may need attention:\n");
            for (HealthValue value : healthValues) {
                if ("high".equals(value.getStatus()) || "low".equals(value.getStatus())) {
                    suggestions.append(String.format("• %s: %s %s (%s)\n", 
                        value.getParameter(), value.getValue(), 
                        value.getUnit() != null ? value.getUnit() : "", 
                        value.getStatus().toUpperCase()));
                }
            }
            suggestions.append("\n");
        }
        
        suggestions.append("Note: This is an automated analysis. Please consult with your healthcare provider for proper medical interpretation.");
        
        return suggestions.toString();
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        if (textRecognizer != null) {
            textRecognizer.close();
        }
    }
}