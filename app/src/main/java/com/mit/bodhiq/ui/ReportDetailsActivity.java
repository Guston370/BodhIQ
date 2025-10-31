package com.mit.bodhiq.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.mit.bodhiq.ui.base.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.mit.bodhiq.R;
import com.mit.bodhiq.data.model.ScannedReport;
import com.mit.bodhiq.data.repository.ReportsRepository;
import com.mit.bodhiq.databinding.ActivityReportDetailsBinding;
import com.mit.bodhiq.ui.adapters.HealthValueAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Activity for displaying detailed view of a scanned report
 */
@AndroidEntryPoint
public class ReportDetailsActivity extends BaseActivity {
    
    private ActivityReportDetailsBinding binding;
    private HealthValueAdapter healthValueAdapter;
    private CompositeDisposable disposables;
    private ScannedReport currentReport;
    
    @Inject
    ReportsRepository reportsRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityReportDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        disposables = new CompositeDisposable();
        
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        
        // Load report data
        String reportId = getIntent().getStringExtra("report_id");
        if (reportId != null) {
            loadReportDetails(reportId);
        } else {
            Toast.makeText(this, "Invalid report ID", Toast.LENGTH_SHORT).show();
            finish();
        }
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
        binding.btnShareReport.setOnClickListener(v -> shareReport());
        
        // Image click to view full size
        binding.imageReport.setOnClickListener(v -> {
            if (currentReport != null && currentReport.getImageUrl() != null) {
                // TODO: Open full-screen image viewer
                Toast.makeText(this, "Full-screen image viewer coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadReportDetails(String reportId) {
        showLoading(true);
        
        disposables.add(
            reportsRepository.getReportById(reportId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    report -> {
                        showLoading(false);
                        currentReport = report;
                        displayReportDetails(report);
                    },
                    error -> {
                        showLoading(false);
                        Toast.makeText(this, "Failed to load report: " + error.getMessage(), 
                            Toast.LENGTH_LONG).show();
                        finish();
                    }
                )
        );
    }
    
    private void displayReportDetails(ScannedReport report) {
        // Set report title
        binding.textReportTitle.setText(report.getTitle());
        
        // Format and set date
        Date date = new Date(report.getTimestamp());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        binding.textReportDate.setText(dateFormat.format(date));
        
        // Load report image if available
        if (report.getImageUrl() != null && !report.getImageUrl().isEmpty()) {
            binding.cardReportImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                .load(report.getImageUrl())
                .placeholder(R.drawable.ic_document)
                .error(R.drawable.ic_document)
                .centerCrop()
                .into(binding.imageReport);
        } else {
            binding.cardReportImage.setVisibility(View.GONE);
        }
        
        // Display health values if available
        if (report.getHealthValues() != null && !report.getHealthValues().isEmpty()) {
            binding.cardHealthValues.setVisibility(View.VISIBLE);
            healthValueAdapter.updateHealthValues(report.getHealthValues());
        } else {
            binding.cardHealthValues.setVisibility(View.GONE);
        }
        
        // Set extracted text
        String extractedText = report.getExtractedText();
        if (extractedText != null && !extractedText.trim().isEmpty()) {
            binding.textExtracted.setText(extractedText);
        } else {
            binding.textExtracted.setText("No text was extracted from this report.");
            binding.btnCopyText.setEnabled(false);
        }
        
        // Set status
        String status = report.getStatus();
        if (status != null) {
            binding.textStatus.setText("Status: " + status.toUpperCase());
            
            int statusColor;
            switch (status.toLowerCase()) {
                case "completed":
                    statusColor = getColor(R.color.status_normal);
                    break;
                case "processing":
                    statusColor = getColor(android.R.color.holo_orange_light);
                    break;
                case "failed":
                    statusColor = getColor(R.color.status_high);
                    break;
                default:
                    statusColor = getColor(R.color.status_unknown);
                    break;
            }
            binding.textStatus.setTextColor(statusColor);
        }
    }
    
    private void copyTextToClipboard() {
        if (currentReport != null && currentReport.getExtractedText() != null) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Report Text", currentReport.getExtractedText());
            clipboard.setPrimaryClip(clip);
            
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void shareReport() {
        if (currentReport == null) return;
        
        StringBuilder shareText = new StringBuilder();
        shareText.append(currentReport.getTitle()).append("\n\n");
        
        // Add date
        Date date = new Date(currentReport.getTimestamp());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        shareText.append("Date: ").append(dateFormat.format(date)).append("\n\n");
        
        // Add health values if available
        if (currentReport.getHealthValues() != null && !currentReport.getHealthValues().isEmpty()) {
            shareText.append("Health Values:\n");
            currentReport.getHealthValues().forEach(value -> {
                shareText.append("• ").append(value.getParameter())
                    .append(": ").append(value.getValue());
                if (value.getUnit() != null && !value.getUnit().isEmpty()) {
                    shareText.append(" ").append(value.getUnit());
                }
                shareText.append(" (").append(value.getStatus().toUpperCase()).append(")\n");
            });
            shareText.append("\n");
        }
        
        // Add extracted text
        if (currentReport.getExtractedText() != null && !currentReport.getExtractedText().trim().isEmpty()) {
            shareText.append("Extracted Text:\n").append(currentReport.getExtractedText());
        }
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentReport.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        
        startActivity(Intent.createChooser(shareIntent, "Share Report"));
    }
    
    private void showLoading(boolean show) {
        binding.layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.scrollContent.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_report_details, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_delete) {
            // TODO: Implement delete functionality with confirmation dialog
            Toast.makeText(this, "Delete functionality coming soon", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_edit) {
            // TODO: Implement edit functionality
            Toast.makeText(this, "Edit functionality coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposables != null) {
            disposables.dispose();
        }
    }
}