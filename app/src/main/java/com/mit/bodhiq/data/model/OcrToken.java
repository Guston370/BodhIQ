package com.mit.bodhiq.data.model;

import android.graphics.Rect;

/**
 * Individual token (word) recognized by OCR with confidence and bounding box
 */
public class OcrToken {
    private String text;
    private float confidence;  // 0.0-1.0, -1 if unknown
    private Rect boundingBox;
    private boolean isLowConfidence;
    private int position; // Position in original text
    
    private static final float LOW_CONFIDENCE_THRESHOLD = 0.7f;
    
    public OcrToken(String text, float confidence, Rect boundingBox) {
        this.text = text;
        this.confidence = confidence;
        this.boundingBox = boundingBox;
        this.isLowConfidence = (confidence >= 0 && confidence < LOW_CONFIDENCE_THRESHOLD);
    }
    
    public OcrToken(String text, float confidence) {
        this(text, confidence, null);
    }
    
    // Getters and setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { 
        this.confidence = confidence;
        this.isLowConfidence = (confidence >= 0 && confidence < LOW_CONFIDENCE_THRESHOLD);
    }
    
    public Rect getBoundingBox() { return boundingBox; }
    public void setBoundingBox(Rect boundingBox) { this.boundingBox = boundingBox; }
    
    public boolean isLowConfidence() { return isLowConfidence; }
    public void setLowConfidence(boolean lowConfidence) { this.isLowConfidence = lowConfidence; }
    
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    
    /**
     * Check if confidence is available
     */
    public boolean hasConfidence() {
        return confidence >= 0;
    }
    
    /**
     * Get confidence as percentage string
     */
    public String getConfidenceString() {
        if (confidence < 0) {
            return "Unknown";
        }
        return String.format("%.1f%%", confidence * 100);
    }
}
