package com.mit.bodhiq.data.model;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * A line of text from OCR with constituent tokens
 */
public class OcrLine {
    private String text;
    private List<OcrToken> tokens;
    private Rect boundingBox;
    private float lineConfidence;
    
    public OcrLine(String text, List<OcrToken> tokens, Rect boundingBox) {
        this.text = text;
        this.tokens = tokens != null ? tokens : new ArrayList<>();
        this.boundingBox = boundingBox;
        this.lineConfidence = calculateLineConfidence();
    }
    
    public OcrLine(String text) {
        this(text, new ArrayList<>(), null);
    }
    
    // Getters and setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public List<OcrToken> getTokens() { return tokens; }
    public void setTokens(List<OcrToken> tokens) { 
        this.tokens = tokens;
        this.lineConfidence = calculateLineConfidence();
    }
    
    public Rect getBoundingBox() { return boundingBox; }
    public void setBoundingBox(Rect boundingBox) { this.boundingBox = boundingBox; }
    
    public float getLineConfidence() { return lineConfidence; }
    
    /**
     * Calculate average confidence from all tokens
     */
    private float calculateLineConfidence() {
        if (tokens == null || tokens.isEmpty()) {
            return -1.0f;
        }
        
        float sum = 0;
        int count = 0;
        
        for (OcrToken token : tokens) {
            if (token.hasConfidence()) {
                sum += token.getConfidence();
                count++;
            }
        }
        
        return count > 0 ? sum / count : -1.0f;
    }
    
    /**
     * Get low-confidence tokens in this line
     */
    public List<OcrToken> getLowConfidenceTokens() {
        List<OcrToken> lowConf = new ArrayList<>();
        for (OcrToken token : tokens) {
            if (token.isLowConfidence()) {
                lowConf.add(token);
            }
        }
        return lowConf;
    }
}
