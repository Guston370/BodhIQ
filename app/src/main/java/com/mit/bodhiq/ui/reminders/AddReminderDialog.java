package com.mit.bodhiq.ui.reminders;

import android.app.TimePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mit.bodhiq.R;
import com.mit.bodhiq.data.database.entity.Reminder;
import com.mit.bodhiq.databinding.DialogAddReminderBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Dialog for adding/editing reminders
 */
public class AddReminderDialog {
    
    private final Context context;
    private final Reminder existingReminder;
    private final OnReminderSavedListener listener;
    private DialogAddReminderBinding binding;
    private List<String> selectedTimes = new ArrayList<>();
    
    public interface OnReminderSavedListener {
        void onSaved(Reminder reminder);
    }
    
    public AddReminderDialog(Context context, Reminder existingReminder, 
                            OnReminderSavedListener listener) {
        this.context = context;
        this.existingReminder = existingReminder;
        this.listener = listener;
    }
    
    public void show() {
        binding = DialogAddReminderBinding.inflate(LayoutInflater.from(context));
        
        setupFormDropdowns();
        setupFrequencyDropdowns();
        setupTimeSelection();
        
        if (existingReminder != null) {
            populateExistingData();
        }
        
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context)
            .setTitle(existingReminder == null ? "Add Reminder" : "Edit Reminder")
            .setView(binding.getRoot())
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validateAndSave()) {
                    dialog.dismiss();
                }
            });
        });
        
        dialog.show();
    }
    
    private void setupFormDropdowns() {
        String[] forms = {"Tablet", "Capsule", "Syrup", "Injection", "Drops", "Inhaler"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
            android.R.layout.simple_dropdown_item_1line, forms);
        binding.etForm.setAdapter(adapter);
    }
    
    private void setupFrequencyDropdowns() {
        String[] frequencies = {"Daily", "Every N Days", "Weekdays Only"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
            android.R.layout.simple_dropdown_item_1line, frequencies);
        binding.etFrequency.setAdapter(adapter);
        
        binding.etFrequency.setOnItemClickListener((parent, view, position, id) -> {
            binding.tilFrequencyValue.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        });
    }
    
    private void setupTimeSelection() {
        binding.btnAddTime.setOnClickListener(v -> showTimePicker());
    }
    
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(context, (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            addTimeChip(time);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }
    
    private void addTimeChip(String time) {
        if (selectedTimes.contains(time)) {
            Toast.makeText(context, "Time already added", Toast.LENGTH_SHORT).show();
            return;
        }
        
        selectedTimes.add(time);
        
        Chip chip = new Chip(context);
        chip.setText(formatTimeDisplay(time));
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            selectedTimes.remove(time);
            binding.chipGroupTimes.removeView(chip);
        });
        
        binding.chipGroupTimes.addView(chip);
    }
    
    private String formatTimeDisplay(String time) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return output.format(input.parse(time));
        } catch (Exception e) {
            return time;
        }
    }
    
    private void populateExistingData() {
        binding.etMedicineName.setText(existingReminder.getMedicineName());
        binding.etDose.setText(existingReminder.getDose());
        binding.etForm.setText(existingReminder.getForm(), false);
        binding.etNotes.setText(existingReminder.getNotes());
        
        if (existingReminder.getTimes() != null) {
            selectedTimes.addAll(existingReminder.getTimes());
            for (String time : selectedTimes) {
                addTimeChip(time);
            }
        }
        
        String frequencyType = existingReminder.getFrequencyType();
        if ("daily".equals(frequencyType)) {
            binding.etFrequency.setText("Daily", false);
        } else if ("every_n_days".equals(frequencyType)) {
            binding.etFrequency.setText("Every N Days", false);
            binding.tilFrequencyValue.setVisibility(View.VISIBLE);
            binding.etFrequencyValue.setText(String.valueOf(existingReminder.getFrequencyValue()));
        } else if ("weekdays".equals(frequencyType)) {
            binding.etFrequency.setText("Weekdays Only", false);
        }
    }
    
    private boolean validateAndSave() {
        String medicineName = binding.etMedicineName.getText().toString().trim();
        String dose = binding.etDose.getText().toString().trim();
        String form = binding.etForm.getText().toString().trim();
        String notes = binding.etNotes.getText().toString().trim();
        
        if (TextUtils.isEmpty(medicineName)) {
            binding.tilMedicineName.setError("Medicine name is required");
            return false;
        }
        
        if (TextUtils.isEmpty(dose)) {
            binding.tilDose.setError("Dose is required");
            return false;
        }
        
        if (selectedTimes.isEmpty()) {
            Toast.makeText(context, "Please add at least one time", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        Reminder reminder = existingReminder != null ? existingReminder : new Reminder();
        reminder.setMedicineName(medicineName);
        reminder.setDose(dose);
        reminder.setForm(form);
        reminder.setNotes(notes);
        reminder.setTimes(new ArrayList<>(selectedTimes));
        reminder.setTimezone(TimeZone.getDefault().getID());
        reminder.setStartDate(new Date());
        
        // Set frequency
        String frequency = binding.etFrequency.getText().toString();
        if ("Daily".equals(frequency)) {
            reminder.setFrequencyType("daily");
        } else if ("Every N Days".equals(frequency)) {
            reminder.setFrequencyType("every_n_days");
            String valueStr = binding.etFrequencyValue.getText().toString();
            reminder.setFrequencyValue(TextUtils.isEmpty(valueStr) ? 1 : Integer.parseInt(valueStr));
        } else if ("Weekdays Only".equals(frequency)) {
            reminder.setFrequencyType("weekdays");
        } else {
            reminder.setFrequencyType("daily");
        }
        
        listener.onSaved(reminder);
        return true;
    }
}
