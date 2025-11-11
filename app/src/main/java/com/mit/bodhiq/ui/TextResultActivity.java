package com.mit.bodhiq.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.mit.bodhiq.ui.base.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mit.bodhiq.chatbot.DocumentAnalyzer;
import com.mit.bodhiq.data.model.HealthValue;
import com.mit.bodhiq.data.model.ScannedReport;
import com.mit.bodhiq.data.repository.ReportsRepository;
import com.mit.bodhiq.databinding.ActivityTextResultBinding;
import com.mit.bodhiq.ui.adapters.HealthValueAdapter;
import com.mit.bodhiq.ui.reminders.RemindersActivity;
import com.mit.bodhiq.utils.TextRecognitionService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Activity for displaying and managing text recognition results
 */
@AndroidEntryPoint
public class TextResultActivity extends BaseActivity {
    
    private ActivityTextResultBinding binding;
    private TextRecognitionService textRecognitionService;
    private HealthValueAdapter healthValueAdapter;
    private CompositeDisposable disposables;
    
    private String extractedText;
    private Uri imageUri;
    private List<HealthValue> healthValues;
    private DocumentAnalyzer documentAnalyzer;
    private DocumentAnalyzer.ReportAnalysis reportAnalysis;
    
    @Inject
    ReportsRepository reportsRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityTextResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        disposables = new CompositeDisposable();
        textRecognitionService = new TextRecognitionService(this);
        documentAnalyzer = new DocumentAnalyzer();
        
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        
        // Get data from intent
        extractDataFromIntent();
        
        // Process and analyze report
        processHealthValues();
        analyzeReport();
    }
    
    private void setupToolbar() {
        // Toolbar removed - no action bar needed
    }
    
    private void setupRecyclerView() {
        healthValueAdapter = new HealthValueAdapter();
        binding.recyclerHealthValues.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerHealthValues.setAdapter(healthValueAdapter);
    }
    
    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCopyText.setOnClickListener(v -> copyTextToClipboard());
        binding.btnSaveReport.setOnClickListener(v -> saveReport());
        binding.btnShareResults.setOnClickListener(v -> shareResults());
        binding.btnRescan.setOnClickListener(v -> rescan());
        binding.btnCreateReminder.setOnClickListener(v -> createReminder());
        binding.btnEmergencyQr.setOnClickListener(v -> openEmergencyQR());
        binding.btnCopySummary.setOnClickListener(v -> copySummary());
        binding.btnRetry.setOnClickListener(v -> rescan());
    }
    
    private void extractDataFromIntent() {
        Intent intent = getIntent();
        extractedText = intent.getStringExtra("extracted_text");
        String imageUriString = intent.getStringExtra("image_uri");
        
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString);
            // Load image preview
            Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(binding.imagePreview);
            binding.imagePreview.setVisibility(View.VISIBLE);
        }
        
        if (extractedText != null && !extractedText.trim().isEmpty()) {
            binding.textExtracted.setText(extractedText);
            
            // Generate default title based on current date
            String defaultTitle = "Medical Report - " + 
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
            binding.editReportTitle.setText(defaultTitle);
            
            // Show content
            binding.layoutContent.setVisibility(View.VISIBLE);
            binding.layoutNoData.setVisibility(View.GONE);
        } else {
            // Show no data message
            binding.layoutContent.setVisibility(View.GONE);
            binding.layoutNoData.setVisibility(View.VISIBLE);
            binding.btnSaveReport.setEnabled(false);
        }
    }
    
    private void processHealthValues() {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            return;
        }
        
        // Parse health values from extracted text
        healthValues = textRecognitionService.parseHealthValues(extractedText);
        
        if (healthValues != null && !healthValues.isEmpty()) {
            // Show health values section
            binding.cardHealthValues.setVisibility(View.VISIBLE);
            healthValueAdapter.updateHealthValues(healthValues);
            
            // Generate and show health suggestions
            String suggestions = textRecognitionService.generateHealthSuggestions(healthValues);
            binding.textSuggestions.setText(suggestions);
            binding.cardSuggestions.setVisibility(View.VISIBLE);
        } else {
            // Hide health values section if no values found
            binding.cardHealthValues.setVisibility(View.GONE);
            binding.cardSuggestions.setVisibility(View.GONE);
        }
    }
    
    private void copyTextToClipboard() {
        if (extractedText != null && !extractedText.trim().isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Extracted Text", extractedText);
            clipboard.setPrimaryClip(clip);
            
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveReport() {
        String title = binding.editReportTitle.getText().toString().trim();
        
        if (title.isEmpty()) {
            binding.tilReportTitle.setError("Please enter a report title");
            return;
        }
        
        if (extractedText == null || extractedText.trim().isEmpty()) {
            Toast.makeText(this, "No text to save", Toast.LENGTH_SHORT).show();
            return;
        }
        
        binding.tilReportTitle.setError(null);
        showSaving(true);
        
        // First upload image if available
        if (imageUri != null) {
            disposables.add(
                reportsRepository.uploadReportImage(imageUri)
                    .flatMap(imageUrl -> {
                        // Create scanned report with image URL
                        ScannedReport report = new ScannedReport(null, title, extractedText, imageUrl);
                        report.setHealthValues(healthValues);
                        return reportsRepository.saveScannedReport(report);
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        reportId -> {
                            showSaving(false);
                            Toast.makeText(this, "Report saved successfully", Toast.LENGTH_SHORT).show();
                            
                            // Navigate back or to report details
                            setResult(RESULT_OK);
                            finish();
                        },
                        error -> {
                            showSaving(false);
                            Toast.makeText(this, "Failed to save report: " + error.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        }
                    )
            );
        } else {
            // Save without image
            ScannedReport report = new ScannedReport(null, title, extractedText, null);
            report.setHealthValues(healthValues);
            
            disposables.add(
                reportsRepository.saveScannedReport(report)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        reportId -> {
                            showSaving(false);
                            Toast.makeText(this, "Report saved successfully", Toast.LENGTH_SHORT).show();
                            
                            setResult(RESULT_OK);
                            finish();
                        },
                        error -> {
                            showSaving(false);
                            Toast.makeText(this, "Failed to save report: " + error.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        }
                    )
            );
        }
    }
    
    private void shareResults() {
        String title = binding.editReportTitle.getText().toString().trim();
        if (title.isEmpty()) {
            title = "Medical Report Results";
        }
        
        StringBuilder shareText = new StringBuilder();
        shareText.append(title).append("\n\n");
        
        if (healthValues != null && !healthValues.isEmpty()) {
            shareText.append("Health Values:\n");
            for (HealthValue value : healthValues) {
                shareText.append("• ").append(value.getParameter())
                    .append(": ").append(value.getValue());
                if (value.getUnit() != null && !value.getUnit().isEmpty()) {
                    shareText.append(" ").append(value.getUnit());
                }
                shareText.append(" (").append(value.getStatus().toUpperCase()).append(")\n");
            }
            shareText.append("\n");
        }
        
        shareText.append("Extracted Text:\n").append(extractedText);
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        
        startActivity(Intent.createChooser(shareIntent, "Share Report Results"));
    }
    
    private void showSaving(boolean show) {
        binding.layoutSaving.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSaveReport.setEnabled(!show);
        binding.btnShareResults.setEnabled(!show);
    }
    
    /**
     * Analyze report using DocumentAnalyzer
     */
    private void analyzeReport() {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            return;
        }
        
        // Analyze report
        reportAnalysis = documentAnalyzer.analyzeReport(extractedText);
        
        // Display structured summary
        displayStructuredSummary();
        
        // Check for emergency indicators
        checkForEmergency();
    }
    
    /**
     * Display structured summary from analysis
     */
    private void displayStructuredSummary() {
        if (reportAnalysis == null) {
            return;
        }
        
        StringBuilder summary = new StringBuilder();
        
        // Patient Details (extracted from text)
        String patientInfo = extractPatientInfo();
        if (!patientInfo.isEmpty()) {
            summary.append("**Patient Details:**\n").append(patientInfo).append("\n\n");
        }
        
        // Test Results
        if (!reportAnalysis.getDetectedValues().isEmpty()) {
            summary.append("**Test Results:**\n");
            for (DocumentAnalyzer.DetectedValue value : reportAnalysis.getDetectedValues()) {
                summary.append("• ").append(value.getParameter()).append(": ")
                    .append(value.getValue()).append(" ").append(value.getUnit())
                    .append(" (").append(value.getStatus()).append(")\n");
            }
            summary.append("\n");
        }
        
        // Medications
        if (!reportAnalysis.getMedications().isEmpty()) {
            summary.append("**Medications:**\n");
            for (String med : reportAnalysis.getMedications()) {
                summary.append("• ").append(med).append("\n");
            }
            summary.append("\n");
        }
        
        // Important Dates
        if (!reportAnalysis.getDates().isEmpty()) {
            summary.append("**Important Dates:**\n");
            for (String date : reportAnalysis.getDates()) {
                summary.append("• ").append(date).append("\n");
            }
            summary.append("\n");
        }
        
        // Remarks/Recommendations
        if (!reportAnalysis.getRecommendations().isEmpty()) {
            summary.append("**Remarks:**\n");
            for (String rec : reportAnalysis.getRecommendations()) {
                summary.append("• ").append(rec).append("\n");
            }
        }
        
        if (summary.length() > 0) {
            binding.textStructuredSummary.setText(summary.toString());
            binding.cardStructuredSummary.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Extract patient information from text
     */
    private String extractPatientInfo() {
        StringBuilder info = new StringBuilder();
        
        // Extract patient name
        Pattern namePattern = Pattern.compile("(?i)(?:patient|name)\\s*:?\\s*([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)+)");
        Matcher nameMatcher = namePattern.matcher(extractedText);
        if (nameMatcher.find()) {
            info.append("Name: ").append(nameMatcher.group(1)).append("\n");
        }
        
        // Extract age
        Pattern agePattern = Pattern.compile("(?i)age\\s*:?\\s*(\\d+)\\s*(?:years?|yrs?)?");
        Matcher ageMatcher = agePattern.matcher(extractedText);
        if (ageMatcher.find()) {
            info.append("Age: ").append(ageMatcher.group(1)).append(" years\n");
        }
        
        // Extract gender
        Pattern genderPattern = Pattern.compile("(?i)(?:gender|sex)\\s*:?\\s*(male|female|m|f)");
        Matcher genderMatcher = genderPattern.matcher(extractedText);
        if (genderMatcher.find()) {
            String gender = genderMatcher.group(1).toLowerCase();
            if (gender.equals("m")) gender = "Male";
            else if (gender.equals("f")) gender = "Female";
            else gender = gender.substring(0, 1).toUpperCase() + gender.substring(1);
            info.append("Gender: ").append(gender).append("\n");
        }
        
        // Extract doctor name
        Pattern doctorPattern = Pattern.compile("(?i)(?:dr\\.?|doctor)\\s+([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*)");
        Matcher doctorMatcher = doctorPattern.matcher(extractedText);
        if (doctorMatcher.find()) {
            info.append("Doctor: Dr. ").append(doctorMatcher.group(1)).append("\n");
        }
        
        return info.toString();
    }
    
    /**
     * Check for emergency indicators
     */
    private void checkForEmergency() {
        if (reportAnalysis == null) {
            return;
        }
        
        // Check severity
        if (reportAnalysis.getSeverity() == com.mit.bodhiq.data.model.ChatMessage.Severity.HIGH ||
            reportAnalysis.getSeverity() == com.mit.bodhiq.data.model.ChatMessage.Severity.CRITICAL) {
            binding.cardEmergencyAlert.setVisibility(View.VISIBLE);
            binding.btnEmergencyQr.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Copy structured summary to clipboard
     */
    private void copySummary() {
        if (binding.textStructuredSummary.getText() != null) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Report Summary", binding.textStructuredSummary.getText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Summary copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Rescan the document
     */
    private void rescan() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Rescan Document")
            .setMessage("This will discard the current results. Do you want to continue?")
            .setPositiveButton("Rescan", (dialog, which) -> {
                setResult(RESULT_CANCELED);
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Create medicine reminder from detected medications
     */
    private void createReminder() {
        if (reportAnalysis == null || reportAnalysis.getMedications().isEmpty()) {
            Toast.makeText(this, "No medications detected in the report", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show medication selection dialog
        String[] medications = reportAnalysis.getMedications().toArray(new String[0]);
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Create Reminder")
            .setItems(medications, (dialog, which) -> {
                String selectedMed = medications[which];
                
                // Open RemindersActivity with pre-filled medication
                Intent intent = new Intent(this, RemindersActivity.class);
                intent.putExtra("medicine_name", selectedMed);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Open Emergency QR code
     */
    private void openEmergencyQR() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Emergency QR Code")
            .setMessage("Your Emergency QR code contains your medical information for emergency situations. Do you want to open it?")
            .setPositiveButton("Open", (dialog, which) -> {
                Intent intent = new Intent(this, EmergencyQrActivity.class);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposables != null) {
            disposables.dispose();
        }
        if (textRecognitionService != null) {
            textRecognitionService.cleanup();
        }
    }
}