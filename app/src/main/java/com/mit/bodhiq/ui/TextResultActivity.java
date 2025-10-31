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
import com.mit.bodhiq.data.model.HealthValue;
import com.mit.bodhiq.data.model.ScannedReport;
import com.mit.bodhiq.data.repository.ReportsRepository;
import com.mit.bodhiq.databinding.ActivityTextResultBinding;
import com.mit.bodhiq.ui.adapters.HealthValueAdapter;
import com.mit.bodhiq.utils.TextRecognitionService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    
    @Inject
    ReportsRepository reportsRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityTextResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        disposables = new CompositeDisposable();
        textRecognitionService = new TextRecognitionService(this);
        
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        
        // Get data from intent
        extractDataFromIntent();
        
        // Process health values
        processHealthValues();
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
        binding.btnCopyText.setOnClickListener(v -> copyTextToClipboard());
        binding.btnSaveReport.setOnClickListener(v -> saveReport());
        binding.btnShareResults.setOnClickListener(v -> shareResults());
    }
    
    private void extractDataFromIntent() {
        Intent intent = getIntent();
        extractedText = intent.getStringExtra("extracted_text");
        String imageUriString = intent.getStringExtra("image_uri");
        
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString);
        }
        
        if (extractedText != null) {
            binding.textExtracted.setText(extractedText);
            
            // Generate default title based on current date
            String defaultTitle = "Medical Report - " + 
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
            binding.editReportTitle.setText(defaultTitle);
        } else {
            binding.textExtracted.setText("No text was extracted from the image.");
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