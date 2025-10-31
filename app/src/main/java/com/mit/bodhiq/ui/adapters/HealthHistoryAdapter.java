package com.mit.bodhiq.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mit.bodhiq.R;
import com.mit.bodhiq.models.HealthHistoryItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for health history items
 */
public class HealthHistoryAdapter extends RecyclerView.Adapter<HealthHistoryAdapter.ViewHolder> {
    
    private List<HealthHistoryItem> items;
    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    public interface OnItemClickListener {
        void onItemClick(HealthHistoryItem item);
    }
    
    public HealthHistoryAdapter(List<HealthHistoryItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_health_history, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthHistoryItem item = items.get(position);
        
        holder.textReportTitle.setText(item.getTitle());
        holder.textReportDate.setText(dateFormat.format(item.getDate()));
        holder.textReportSummary.setText(item.getSummary());
        
        // Set icon based on type
        int iconRes = getIconForType(item.getType());
        holder.iconReportType.setImageResource(iconRes);
        
        // Set status indicator color
        int statusColor = getStatusColor(holder.itemView.getContext(), item.getStatus());
        holder.viewStatusIndicator.setBackgroundColor(statusColor);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void updateItems(List<HealthHistoryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
    
    private int getIconForType(String type) {
        switch (type.toLowerCase()) {
            case "report":
                return R.drawable.ic_document;
            case "diagnosis":
                return R.drawable.ic_health;
            case "recommendation":
                return R.drawable.ic_analytics;
            default:
                return R.drawable.ic_document;
        }
    }
    
    private int getStatusColor(android.content.Context context, String status) {
        switch (status.toLowerCase()) {
            case "normal":
                return context.getResources().getColor(R.color.status_normal, null);
            case "high":
                return context.getResources().getColor(R.color.status_high, null);
            case "low":
                return context.getResources().getColor(R.color.status_low, null);
            case "critical":
                return context.getResources().getColor(R.color.error, null);
            default:
                return context.getResources().getColor(R.color.status_unknown, null);
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewStatusIndicator;
        ImageView iconReportType;
        TextView textReportTitle;
        TextView textReportDate;
        TextView textReportSummary;
        
        ViewHolder(View itemView) {
            super(itemView);
            viewStatusIndicator = itemView.findViewById(R.id.view_status_indicator);
            iconReportType = itemView.findViewById(R.id.icon_report_type);
            textReportTitle = itemView.findViewById(R.id.text_report_title);
            textReportDate = itemView.findViewById(R.id.text_report_date);
            textReportSummary = itemView.findViewById(R.id.text_report_summary);
        }
    }
}