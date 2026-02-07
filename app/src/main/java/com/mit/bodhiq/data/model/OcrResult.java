package com.mit.bodhiq.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * OCR recognition result with confidence scores and token-level data
 */
public class OcrResult {
    private String rawText;
    private List<OcrToken> tokens;
    private List<OcrLine> lines;
    private float overallConfidence;
    private long recognitionTimeMs;
    
    public OcrResult() {
        this.tokens = new ArrayList<>();
        this.lines = new ArrayList<>();
        this.overallConfidence = -1.0f; // Unknown confidence
    }
    
    public OcrResult(String rawText, List<OcrToken> tokens, List<OcrLine> lines, float overallConfidence) {
        this.rawText = rawText;
        this.tokens = tokens != null ? tokens : new ArrayList<>();
        this.lines = lines != null ? lines : new ArrayList<>();
        this.overallConfidence = overallConfidence;
    }
    
    // Getters and setters
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    
    public List<OcrToken> getTokens() { return tokens; }
    public void setTokens(List<OcrToken> tokens) { this.tokens = tokens; }
    
    public List<OcrLine> getLines() { return lines; }
    public void setLines(List<OcrLine> lines) { this.lines = lines; }
    
    public float getOverallConfidence() { return overallConfidence; }
    public void setOverallConfidence(float overallConfidence) { this.overallConfidence = overallConfidence; }
    
    public long getRecognitionTimeMs() { return recognitionTimeMs; }
    public void setRecognitionTimeMs(long recognitionTimeMs) { this.recognitionTimeMs = recognitionTimeMs; }
    
    /**
     * Get all low-confidence tokens (confidence < 0.7)
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
    
    /**
     * Get token count
     */
    public int getTokenCount() {
        return tokens != null ? tokens.size() : 0;
    }
    
    /**
     * Get line count
     */
    public int getLineCount() {
        return lines != null ? lines.size() : 0;
    }
}
