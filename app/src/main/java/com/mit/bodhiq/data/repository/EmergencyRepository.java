package com.mit.bodhiq.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.mit.bodhiq.data.model.EmergencyQrMeta;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Repository for Emergency QR metadata
 */
@Singleton
public class EmergencyRepository {
    
    private static final String PREFS_NAME = "emergency_qr_prefs";
    private static final String KEY_QR_META = "qr_meta";
    
    private final SharedPreferences prefs;
    private final Gson gson;
    
    @Inject
    public EmergencyRepository(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * Save QR metadata
     */
    public void saveMetadata(EmergencyQrMeta meta) {
        String json = gson.toJson(meta);
        prefs.edit().putString(KEY_QR_META, json).apply();
    }
    
    /**
     * Load QR metadata
     */
    public EmergencyQrMeta loadMetadata() {
        String json = prefs.getString(KEY_QR_META, null);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, EmergencyQrMeta.class);
    }
    
    /**
     * Clear QR metadata
     */
    public void clearMetadata() {
        prefs.edit().remove(KEY_QR_META).apply();
    }
}
