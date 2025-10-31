package com.mit.bodhiq.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mit.bodhiq.R;
import com.mit.bodhiq.data.model.ScannedReport;
import com.mit.bodhiq.databinding.ItemScannedReportBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying scanned reports in RecyclerView
 */
public class ScannedReportsAdapter extends RecyclerView.Adapter<ScannedReportsAdapter.ReportViewHolder> {
    
    private final List<ScannedReport> reports;
    private final OnReportClickListener clickListener;
    
    public interface OnReportClickListener {
        void onReportClick(ScannedReport report);
    }
    
    public ScannedReportsAdapter(List<ScannedReport> reports, OnReportClickListener clickListener) {
        this.reports = reports;
        this.clickListener = clickListener;
    }
    
    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScannedReportBinding binding = ItemScannedReportBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ReportViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        holder.bind(reports.get(position));
    }
    
    @Override
    public int getItemCount() {
        return reports.size();
    }
    
    class ReportViewHolder extends RecyclerView.ViewHolder {
        private final ItemScannedReportBinding binding;
        
        public ReportViewHolder(ItemScannedReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(ScannedReport report) {
            // Set report title
            binding.textReportTitle.setText(report.getTitle());
            
            // Format and set date
            Date date = new Date(report.getTimestamp());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault());
            binding.textReportDate.setText(dateFormat.format(date));
            
            // Set preview text (first 100 characters of extracted text)
            String previewText = report.getExtractedText();
            if (previewText != null && previewText.length() > 100) {
                previewText = previewText.substring(0, 100) + "...";
            }
            binding.textReportPreview.setText(previewText != null ? previewText : "No text extracted");
            
            // Load report image if available
            if (report.getImageUrl() != null && !report.getImageUrl().isEmpty()) {
                binding.imageReport.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                    .load(report.getImageUrl())
                    .placeholder(R.drawable.ic_document)
                    .error(R.drawable.ic_document)
                    .centerCrop()
                    .into(binding.imageReport);
            } else {
                binding.imageReport.setVisibility(View.GONE);
            }
            
            // Show health values count if available
            if (report.getHealthValues() != null && !report.getHealthValues().isEmpty()) {
                int healthValueCount = report.getHealthValues().size();
                binding.textHealthValuesCount.setText(healthValueCount + " health parameter" + 
                    (healthValueCount > 1 ? "s" : "") + " detected");
                binding.textHealthValuesCount.setVisibility(View.VISIBLE);
                binding.iconHealthValues.setVisibility(View.VISIBLE);
            } else {
                binding.textHealthValuesCount.setVisibility(View.GONE);
                binding.iconHealthValues.setVisibility(View.GONE);
            }
            
            // Set status indicator
            String status = report.getStatus();
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "completed":
                        binding.viewStatusIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.status_normal));
                        break;
                    case "processing":
                        binding.viewStatusIndicator.setBackgroundColor(
                            itemView.getContext().getColor(android.R.color.holo_orange_light));
                        break;
                    case "failed":
                        binding.viewStatusIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.status_high));
                        break;
                    default:
                        binding.viewStatusIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.status_unknown));
                        break;
                }
            }
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onReportClick(report);
                }
            });
            
            // Add ripple effect
            itemView.setBackground(itemView.getContext().getDrawable(R.drawable.ripple_background));
        }
    }
}