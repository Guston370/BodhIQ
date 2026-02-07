package com.mit.bodhiq.ui;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.mit.bodhiq.R;
import com.mit.bodhiq.data.model.UserProfile;
import com.mit.bodhiq.data.repository.ProfileRepository;
import com.mit.bodhiq.databinding.ActivityEmergencyQrBinding;
import com.mit.bodhiq.utils.EmergencyPayloadBuilder;
import com.mit.bodhiq.utils.QrUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Activity for generating and managing Emergency QR codes
 */
@AndroidEntryPoint
public class EmergencyQrActivity extends AppCompatActivity {
    
    private ActivityEmergencyQrBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    @Inject
    ProfileRepository profileRepository;
    
    @Inject
    com.mit.bodhiq.data.repository.EmergencyRepository emergencyRepository;
    
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private UserProfile currentUserProfile;
    private Bitmap currentQrBitmap;
    private String currentPayload;
    private boolean consentGiven = false;
    private boolean includeLogo = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupToolbar();
        setupClickListeners();
        loadUserProfile();
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Emergency QR Code");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupClickListeners() {
        binding.btnGenerateQr.setOnClickListener(v -> showConsentDialog());
        binding.btnSaveQr.setOnClickListener(v -> saveQrToGallery());
        binding.btnShareQr.setOnClickListener(v -> shareQr());
        binding.btnCopyPayload.setOnClickListener(v -> copyPayloadToClipboard());
        binding.btnCopyText.setOnClickListener(v -> copyAsText());
        binding.btnRegenerate.setOnClickListener(v -> regenerateQr());
        binding.switchIncludeLogo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            includeLogo = isChecked;
            if (consentGiven && currentUserProfile != null) {
                generateQrCode();
            }
        });
    }
    
    private void loadUserProfile() {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.layoutContent.setVisibility(View.GONE);
        
        Disposable disposable = profileRepository.getUserProfile()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                profile -> {
                    currentUserProfile = profile;
                    binding.layoutLoading.setVisibility(View.GONE);
                    binding.layoutContent.setVisibility(View.VISIBLE);
                    displayProfileSummary(profile);
                },
                error -> {
                    binding.layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load profile: " + error.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    finish();
                }
            );
        compositeDisposable.add(disposable);
    }
    
    private void displayProfileSummary(UserProfile profile) {
        StringBuilder summary = new StringBuilder();
        summary.append("Name: ").append(profile.getFullName()).append("\n");
        
        if (profile.getBloodGroup() != null) {
            summary.append("Blood Group: ").append(profile.getBloodGroup()).append("\n");
        }
        
        if (profile.getAllergies() != null && !profile.getAllergies().isEmpty()) {
            summary.append("Allergies: ").append(profile.getAllergies()).append("\n");
        }
        
        if (profile.getEmergencyContact() != null) {
            summary.append("Emergency Contact: ").append(profile.getEmergencyContact()).append("\n");
        }
        
        binding.tvProfileSummary.setText(summary.toString());
    }
    
    private void showConsentDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Privacy Consent")
            .setMessage("This QR code will contain your personal health information (PHI) including:\n\n" +
                "• Full name\n" +
                "• Blood group\n" +
                "• Allergies\n" +
                "• Emergency contacts\n" +
                "• Date of birth\n\n" +
                "This information will be embedded directly in the QR code and can be read by anyone who scans it. " +
                "Only share this QR code with trusted individuals or in emergency situations.\n\n" +
                "Do you consent to generate this QR code?")
            .setPositiveButton("I Consent", (dialog, which) -> {
                consentGiven = true;
                generateQrCode();
            })
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Use Short URL", (dialog, which) -> {
                Toast.makeText(this, "Short URL mode not yet implemented", Toast.LENGTH_SHORT).show();
            })
            .show();
    }
    
    private void generateQrCode() {
        if (currentUserProfile == null) {
            Toast.makeText(this, "Profile not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.layoutQrPreview.setVisibility(View.GONE);
        
        // Generate payload in background
        new Thread(() -> {
            // Generate plaintext payload
            currentPayload = EmergencyPayloadBuilder.build(currentUserProfile);
            final int payloadSize = EmergencyPayloadBuilder.getPayloadSize(currentPayload);
            
            // Generate QR code
            final int qrSize = QrUtil.getRecommendedSize(currentPayload);
            currentQrBitmap = QrUtil.generate(currentPayload, qrSize, includeLogo, this);
            
            runOnUiThread(() -> {
                binding.layoutLoading.setVisibility(View.GONE);
                if (currentQrBitmap != null) {
                    displayQrCode(payloadSize);
                    saveQrMetadata(payloadSize, qrSize);
                } else {
                    Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    
    private void displayQrCode(int payloadSize) {
        binding.ivQrCode.setImageBitmap(currentQrBitmap);
        binding.layoutQrPreview.setVisibility(View.VISIBLE);
        binding.btnGenerateQr.setVisibility(View.GONE);
        binding.layoutActions.setVisibility(View.VISIBLE);
        
        // Show payload info
        String sizeText = String.format("Payload: %d bytes (%.1f KB)", 
            payloadSize, payloadSize / 1024.0);
        binding.tvPayloadSize.setText(sizeText);
        binding.tvPayloadSize.setVisibility(View.VISIBLE);
        
        // Show accessibility description
        binding.tvAccessibilityDesc.setText("QR code contains emergency information for " + 
            currentUserProfile.getFullName() + ". Readable by any QR scanner.");
        binding.tvAccessibilityDesc.setVisibility(View.VISIBLE);
    }
    
    private void showSizeLimitDialog(int payloadSize) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Payload Too Large")
            .setMessage(String.format("Your emergency information is too large (%d bytes, limit is 3 KB). " +
                "Please use Short URL mode or reduce the amount of information in your profile.", payloadSize))
            .setPositiveButton("Use Short URL", (dialog, which) -> {
                Toast.makeText(this, "Short URL mode not yet implemented", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("OK", null)
            .show();
    }
    
    private void saveQrToGallery() {
        if (currentQrBitmap == null) {
            Toast.makeText(this, "No QR code to save", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check permissions for older Android versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
                return;
            }
        }
        
        performSaveToGallery();
    }
    
    private void performSaveToGallery() {
        try {
            String fileName = "Emergency_QR_" + System.currentTimeMillis() + ".png";
            OutputStream fos;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10+
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BodhIQ");
                
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    throw new Exception("Failed to create MediaStore entry");
                }
                
                fos = getContentResolver().openOutputStream(uri);
            } else {
                // Use legacy storage for older versions
                File imagesDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "BodhIQ");
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs();
                }
                
                File imageFile = new File(imagesDir, fileName);
                fos = new FileOutputStream(imageFile);
                
                // Notify media scanner
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(imageFile));
                sendBroadcast(mediaScanIntent);
            }
            
            currentQrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            
            Snackbar.make(binding.getRoot(), "QR code saved to gallery", Snackbar.LENGTH_LONG)
                .setAction("OK", v -> {})
                .show();
                
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save QR code: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void shareQr() {
        if (currentQrBitmap == null) {
            Toast.makeText(this, "No QR code to share", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Save to cache
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "emergency_qr.png");
            FileOutputStream stream = new FileOutputStream(file);
            currentQrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            
            // Share using FileProvider
            Uri contentUri = FileProvider.getUriForFile(this, 
                getPackageName() + ".fileprovider", file);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Emergency QR Code for " + currentUserProfile.getFullName());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Share Emergency QR Code"));
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to share QR code: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void copyPayloadToClipboard() {
        if (currentPayload == null) {
            Toast.makeText(this, "No payload to copy", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Emergency QR Payload", currentPayload);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "Payload copied to clipboard", Toast.LENGTH_SHORT).show();
    }
    
    private void copyAsText() {
        if (currentUserProfile == null) {
            Toast.makeText(this, "No profile data to copy", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder text = new StringBuilder();
        text.append("EMERGENCY INFORMATION\n\n");
        text.append("Name: ").append(currentUserProfile.getFullName()).append("\n");
        
        if (currentUserProfile.getDateOfBirth() != null) {
            text.append("Date of Birth: ").append(currentUserProfile.getDateOfBirth()).append("\n");
        }
        
        if (currentUserProfile.getBloodGroup() != null) {
            text.append("Blood Group: ").append(currentUserProfile.getBloodGroup()).append("\n");
        }
        
        if (currentUserProfile.getAllergies() != null && !currentUserProfile.getAllergies().isEmpty()) {
            text.append("Allergies: ").append(currentUserProfile.getAllergies()).append("\n");
        }
        
        if (currentUserProfile.getEmergencyContact() != null) {
            text.append("Emergency Contact: ").append(currentUserProfile.getEmergencyContact()).append("\n");
        }
        
        if (currentUserProfile.getPhoneNumber() != null) {
            text.append("Phone: ").append(currentUserProfile.getPhoneNumber()).append("\n");
        }
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Emergency Information", text.toString());
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "Emergency information copied as text", Toast.LENGTH_SHORT).show();
    }
    
    private void regenerateQr() {
        consentGiven = true;
        generateQrCode();
    }
    
    private void saveQrMetadata(int payloadSize, int qrSize) {
        try {
            String payloadHash = String.valueOf(currentPayload.hashCode());
            com.mit.bodhiq.data.model.EmergencyQrMeta meta = new com.mit.bodhiq.data.model.EmergencyQrMeta(
                System.currentTimeMillis(),
                "plaintext",
                payloadHash,
                includeLogo,
                qrSize
            );
            emergencyRepository.saveMetadata(meta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performSaveToGallery();
            } else {
                Toast.makeText(this, "Permission denied. Cannot save to gallery.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
