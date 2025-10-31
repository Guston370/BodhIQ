package com.mit.bodhiq.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mit.bodhiq.R;
import com.mit.bodhiq.data.model.HealthValue;
import com.mit.bodhiq.databinding.ItemHealthValueBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying health values in RecyclerView
 */
public class HealthValueAdapter extends RecyclerView.Adapter<HealthValueAdapter.HealthValueViewHolder> {
    
    private List<HealthValue> healthValues = new ArrayList<>();
    
    public void updateHealthValues(List<HealthValue> newHealthValues) {
        this.healthValues.clear();
        if (newHealthValues != null) {
            this.healthValues.addAll(newHealthValues);
        }
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public HealthValueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHealthValueBinding binding = ItemHealthValueBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new HealthValueViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HealthValueViewHolder holder, int position) {
        holder.bind(healthValues.get(position));
    }
    
    @Override
    public int getItemCount() {
        return healthValues.size();
    }
    
    static class HealthValueViewHolder extends RecyclerView.ViewHolder {
        private final ItemHealthValueBinding binding;
        
        public HealthValueViewHolder(ItemHealthValueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(HealthValue healthValue) {
            binding.textParameter.setText(healthValue.getParameter());
            
            // Format value with unit
            String valueText = healthValue.getValue();
            if (healthValue.getUnit() != null && !healthValue.getUnit().isEmpty()) {
                valueText += " " + healthValue.getUnit();
            }
            binding.textValue.setText(valueText);
            
            // Set status with appropriate color and icon
            String status = healthValue.getStatus();
            binding.textStatus.setText(status.toUpperCase());
            
            int statusColor;
            int statusIcon;
            
            switch (status.toLowerCase()) {
                case "normal":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_normal);
                    statusIcon = R.drawable.ic_check_circle;
                    break;
                case "high":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_high);
                    statusIcon = R.drawable.ic_arrow_upward;
                    break;
                case "low":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_low);
                    statusIcon = R.drawable.ic_arrow_downward;
                    break;
                default:
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_unknown);
                    statusIcon = R.drawable.ic_help;
                    break;
            }
            
            binding.textStatus.setTextColor(statusColor);
            binding.iconStatus.setImageResource(statusIcon);
            binding.iconStatus.setColorFilter(statusColor);
            
            // Show normal range if available
            if (healthValue.getNormalRange() != null && !healthValue.getNormalRange().isEmpty()) {
                binding.textNormalRange.setText("Normal: " + healthValue.getNormalRange());
                binding.textNormalRange.setVisibility(View.VISIBLE);
            } else {
                binding.textNormalRange.setVisibility(View.GONE);
            }
        }
    }
}