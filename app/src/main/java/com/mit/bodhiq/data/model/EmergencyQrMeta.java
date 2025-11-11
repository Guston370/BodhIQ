package com.mit.bodhiq.data.model;

/**
 * Metadata for Emergency QR code generation
 */
public class EmergencyQrMeta {
    private long lastGeneratedAt;
    private String mode; // "json" or "shorturl"
    private String payloadHash;
    private boolean includesLogo;
    private int qrSize;

    public EmergencyQrMeta() {}

    public EmergencyQrMeta(long lastGeneratedAt, String mode, String payloadHash, boolean includesLogo, int qrSize) {
        this.lastGeneratedAt = lastGeneratedAt;
        this.mode = mode;
        this.payloadHash = payloadHash;
        this.includesLogo = includesLogo;
        this.qrSize = qrSize;
    }

    public long getLastGeneratedAt() { return lastGeneratedAt; }
    public void setLastGeneratedAt(long lastGeneratedAt) { this.lastGeneratedAt = lastGeneratedAt; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }

    public boolean isIncludesLogo() { return includesLogo; }
    public void setIncludesLogo(boolean includesLogo) { this.includesLogo = includesLogo; }

    public int getQrSize() { return qrSize; }
    public void setQrSize(int qrSize) { this.qrSize = qrSize; }
}
