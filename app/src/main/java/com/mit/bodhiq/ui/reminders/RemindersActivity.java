package com.mit.bodhiq.ui.reminders;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mit.bodhiq.R;
import com.mit.bodhiq.ui.base.BaseActivity;
import com.mit.bodhiq.data.database.entity.Reminder;
import com.mit.bodhiq.data.repository.ReminderRepository;
import com.mit.bodhiq.databinding.ActivityRemindersBinding;
import com.mit.bodhiq.ui.reminders.adapters.RemindersAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Activity for managing medicine reminders
 */
@AndroidEntryPoint
public class RemindersActivity extends BaseActivity {
    
    private ActivityRemindersBinding binding;
    private RemindersAdapter adapter;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    
    @Inject
    ReminderRepository reminderRepository;
    
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRemindersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Apply window insets for proper edge-to-edge display
        applyTopInset(binding.getRoot());
        
        setupToolbar();
        setupPermissionLauncher();
        setupRecyclerView();
        setupClickListeners();
        
        checkNotificationPermission();
        loadReminders();
        loadAdherenceStats();
        
        // Reschedule reminders on app start
        rescheduleReminders();
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupPermissionLauncher() {
        notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission is required for reminders", 
                        Toast.LENGTH_LONG).show();
                }
            }
        );
    }
    
    private void setupRecyclerView() {
        adapter = new RemindersAdapter(new ArrayList<>(), new RemindersAdapter.ReminderListener() {
            @Override
            public void onToggle(Reminder reminder, boolean enabled) {
                toggleReminder(reminder.getId(), enabled);
            }
            
            @Override
            public void onEdit(Reminder reminder) {
                editReminder(reminder);
            }
            
            @Override
            public void onDelete(Reminder reminder) {
                confirmDelete(reminder);
            }
        });
        
        binding.recyclerReminders.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerReminders.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        binding.fabAddReminder.setOnClickListener(v -> addReminder());
    }
    
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    
    private void loadReminders() {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        
        Disposable disposable = reminderRepository.getAllReminders()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                reminders -> {
                    binding.layoutLoading.setVisibility(View.GONE);
                    adapter.updateReminders(reminders);
                    
                    if (reminders.isEmpty()) {
                        binding.layoutEmpty.setVisibility(View.VISIBLE);
                        binding.recyclerReminders.setVisibility(View.GONE);
                    } else {
                        binding.layoutEmpty.setVisibility(View.GONE);
                        binding.recyclerReminders.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    binding.layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading reminders: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            );
        
        compositeDisposable.add(disposable);
    }
    
    private void loadAdherenceStats() {
        Disposable disposable = reminderRepository.getAdherenceStats(7)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                stats -> {
                    binding.tvAdherencePercentage.setText(stats.percentage + "%");
                    binding.tvTakenCount.setText(stats.takenCount + "/" + stats.totalCount);
                },
                error -> {
                    // Silently fail
                }
            );
        
        compositeDisposable.add(disposable);
    }
    
    private void addReminder() {
        AddReminderDialog dialog = new AddReminderDialog(this, null, reminder -> {
            saveReminder(reminder);
        });
        dialog.show();
    }
    
    private void editReminder(Reminder reminder) {
        AddReminderDialog dialog = new AddReminderDialog(this, reminder, updatedReminder -> {
            saveReminder(updatedReminder);
        });
        dialog.show();
    }
    
    private void saveReminder(Reminder reminder) {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        
        Disposable disposable = reminderRepository.saveReminder(reminder)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                id -> {
                    binding.layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Reminder saved", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    binding.layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Error saving reminder: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            );
        
        compositeDisposable.add(disposable);
    }
    
    private void toggleReminder(long reminderId, boolean enabled) {
        Disposable disposable = reminderRepository.toggleReminder(reminderId, enabled)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> {
                    String message = enabled ? "Reminder enabled" : "Reminder disabled";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(this, "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            );
        
        compositeDisposable.add(disposable);
    }
    
    private void confirmDelete(Reminder reminder) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete this reminder for " + 
                reminder.getMedicineName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> deleteReminder(reminder.getId()))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteReminder(long reminderId) {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        
        Disposable disposable = reminderRepository.deleteReminder(reminderId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> {
                    binding.layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    binding.layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Error deleting reminder: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            );
        
        compositeDisposable.add(disposable);
    }
    
    private void rescheduleReminders() {
        Disposable disposable = reminderRepository.rescheduleAllReminders()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> {
                    // Silently succeed
                },
                error -> {
                    // Silently fail
                }
            );
        
        compositeDisposable.add(disposable);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
