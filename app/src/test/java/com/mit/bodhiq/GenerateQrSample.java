package com.mit.bodhiq;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.mit.bodhiq.data.model.UserProfile;
import com.mit.bodhiq.utils.EmergencyPayloadBuilder;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Test utility to generate sample QR code with Aditya Joshi test data
 * This creates PNG and TXT files for manual verification
 */
public class GenerateQrSample {

    private static final String OUTPUT_DIR = "app/src/test/resources/qr_samples";
    private static final String QR_PNG_FILE = OUTPUT_DIR + "/sample_aditya_qr.png";
    private static final String TXT_FILE = OUTPUT_DIR + "/sample_aditya_text.txt";
    
    @Test
    public void generateSampleQrCode() throws Exception {
        System.out.println("=== Generating Sample QR Code ===");
        
        // Create sample patient (Aditya Joshi)
        UserProfile samplePatient = createAdityaJoshiProfile();
        
        // Build plaintext payload
        String plaintextPayload = buildSamplePayload(samplePatient);
        System.out.println("Generated Plaintext Payload:");
        System.out.println(plaintextPayload);
        System.out.println("\nPayload size: " + plaintextPayload.getBytes().length + " bytes");
        
        // Save plaintext to file
        savePlaintextToFile(plaintextPayload);
        System.out.println("Saved plaintext to: " + TXT_FILE);
        
        // Generate QR code bitmap
        Bitmap qrBitmap = generateQrBitmap(plaintextPayload, 800);
        
        // Save QR code as PNG
        saveQrCodeToPng(qrBitmap);
        System.out.println("Saved QR code to: " + QR_PNG_FILE);
        
        System.out.println("\n=== Sample Generation Complete ===");
        System.out.println("Next steps:");
        System.out.println("1. Scan " + QR_PNG_FILE + " with phone camera");
        System.out.println("2. Verify output matches " + TXT_FILE);
    }
    
    /**
     * Create sample UserProfile with Aditya Joshi data
     */
    private UserProfile createAdityaJoshiProfile() {
        UserProfile profile = new UserProfile();
        profile.setFullName("Aditya Joshi");
        profile.setAge("29");
        profile.setBloodGroup("O+");
        profile.setAllergies("Penicillin");
        // Note: "conditions" field doesn't exist in UserProfile
        // Will use custom note in payload
        profile.setEmergencyContact("Dilip Panchal +91-98123XXXXX");
        return profile;
    }
    
    /**
     * Build sample plaintext payload with exact format
     * Using hardcoded sample data to match requirements exactly
     */
    private String buildSamplePayload(UserProfile profile) {
        StringBuilder payload = new StringBuilder();
        
        payload.append("Name: Aditya Joshi\n");
        payload.append("Age: 29\n");
        payload.append("Blood Type: O+\n");
        payload.append("Allergies: Penicillin\n");
        payload.append("Conditions: Type 2 Diabetes\n");
        payload.append("Emergency Contact: Dilip Panchal +91-98123XXXXX\n");
        payload.append("Note: Carries insulin. Call emergency contact first.");
        
        return payload.toString();
    }
    
    /**
     * Generate QR code bitmap using ZXing
     */
    private Bitmap generateQrBitmap(String payload, int size) throws WriterException {
        // Configure QR code hints
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        
        // Generate QR code
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(payload, BarcodeFormat.QR_CODE, size, size, hints);
        
        // Create bitmap
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        
        return bitmap;
    }
    
    /**
     * Save plaintext payload to TXT file
     */
    private void savePlaintextToFile(String plaintext) throws Exception {
        File file = new File(TXT_FILE);
        file.getParentFile().mkdirs();
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(plaintext);
        }
    }
    
    /**
     * Save QR code bitmap to PNG file
     */
    private void saveQrCodeToPng(Bitmap bitmap) throws Exception {
        File file = new File(QR_PNG_FILE);
        file.getParentFile().mkdirs();
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }
    }
}
