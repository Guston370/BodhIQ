package com.mit.bodhiq.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Diagnostic utility for Google Sign-In setup
 * Helps identify configuration issues
 */
public class GoogleSignInDiagnostics {
    
    private static final String TAG = "GoogleSignInDiagnostics";
    
    /**
     * Print diagnostic information to help troubleshoot Google Sign-In issues
     */
    public static void printDiagnostics(Context context) {
        Log.d(TAG, "=== Google Sign-In Diagnostics ===");
        
        // Print package name
        String packageName = context.getPackageName();
        Log.d(TAG, "Package Name: " + packageName);
        
        // Print SHA-1 fingerprint
        printSHA1Fingerprint(context);
        
        // Print Web Client ID
        try {
            int webClientIdRes = context.getResources().getIdentifier("default_web_client_id", "string", packageName);
            if (webClientIdRes != 0) {
                String webClientId = context.getString(webClientIdRes);
                Log.d(TAG, "Web Client ID: " + webClientId);
                
                if (webClientId.contains("YOUR_WEB_CLIENT_ID_HERE")) {
                    Log.e(TAG, "ERROR: Web Client ID not configured! Please update strings.xml");
                }
            } else {
                Log.e(TAG, "ERROR: default_web_client_id not found in strings.xml");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading Web Client ID: " + e.getMessage());
        }
        
        Log.d(TAG, "=== End Diagnostics ===");
    }
    
    /**
     * Print SHA-1 fingerprint for the current app
     */
    private static void printSHA1Fingerprint(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                md.update(signature.toByteArray());
                
                String sha1 = bytesToHex(md.digest());
                Log.d(TAG, "SHA-1 Fingerprint: " + sha1);
                
                // Also print in the format Firebase expects
                String formattedSha1 = sha1.replaceAll("(.{2})", "$1:").replaceAll(":$", "");
                Log.d(TAG, "SHA-1 (Firebase format): " + formattedSha1);
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            Log.e(TAG, "Error getting SHA-1 fingerprint: " + e.getMessage());
        }
    }
    
    /**
     * Convert bytes to hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
}