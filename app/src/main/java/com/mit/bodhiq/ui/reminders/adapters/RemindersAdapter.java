package com.mit.bodhiq.ui.reminders.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mit.bodhiq.data.database.entity.Reminder;
import com.mit.bodhiq.databinding.ItemReminderBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Adapter for reminders list
 */
public class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.ViewHolder> {
    
    private List<Reminder> reminders;
    private final ReminderListener listener;
    
    public interface ReminderListener {
        void onToggle(Reminder reminder, boolean enabled);
        void onEdit(Reminder reminder);
        void onDelete(Reminder reminder);
    }
    
    public RemindersAdapter(List<Reminder> reminders, ReminderListener listener) {
        this.reminders = reminders;
        this.listener = listener;
    }
    
    public void updateReminders(List<Reminder> newReminders) {
        this.reminders = newReminders;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReminderBinding binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(reminders.get(position));
    }
    
    @Override
    public int getItemCount() {
        return reminders.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemReminderBinding binding;
        
        ViewHolder(ItemReminderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(Reminder reminder) {
            binding.tvMedicineName.setText(reminder.getMedicineName());
            binding.tvDose.setText(reminder.getDose() + " • " + reminder.getForm());
            
            // Format times
            if (reminder.getTimes() != null && !reminder.getTimes().isEmpty()) {
                String timesStr = reminder.getTimes().stream()
                    .map(this::formatTime)
                    .collect(Collectors.joining(", "));
                binding.tvTimes.setText(timesStr);
            }
            
            // Format frequency
            String frequency = getFrequencyText(reminder);
            binding.tvFrequency.setText(frequency);
            
            // Set switch state
            binding.switchEnabled.setOnCheckedChangeListener(null);
            binding.switchEnabled.setChecked(reminder.isEnabled());
            binding.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                listener.onToggle(reminder, isChecked);
            });
            
            // Set click listeners
            binding.btnEdit.setOnClickListener(v -> listener.onEdit(reminder));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(reminder));
        }
        
        private String formatTime(String time) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                return output.format(input.parse(time));
            } catch (Exception e) {
                return time;
            }
        }
        
        private String getFrequencyText(Reminder reminder) {
            String type = reminder.getFrequencyType();
            if (type == null || "daily".equals(type)) {
                return "Daily";
            } else if ("every_n_days".equals(type)) {
                return "Every " + reminder.getFrequencyValue() + " days";
            } else if ("weekdays".equals(type)) {
                return "Weekdays only";
            }
            return "Custom";
        }
    }
}
