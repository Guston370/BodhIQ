package com.mit.bodhiq.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mit.bodhiq.utils.AuthManager;
import com.mit.bodhiq.R;
import com.mit.bodhiq.databinding.FragmentProfileBinding;
import com.mit.bodhiq.models.HealthHistoryItem;
import com.mit.bodhiq.models.WellnessGoal;
import com.mit.bodhiq.ui.adapters.HealthHistoryAdapter;
import com.mit.bodhiq.ui.adapters.WellnessGoalsAdapter;
import com.mit.bodhiq.ui.login.LoginActivity;
import com.mit.bodhiq.utils.BMICalculator;
import com.mit.bodhiq.utils.LogoutManager;
import com.mit.bodhiq.utils.ThemeManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Enhanced ProfileFragment with comprehensive health management features
 */
@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private AuthManager authManager;
    private HealthHistoryAdapter healthHistoryAdapter;
    private WellnessGoalsAdapter wellnessGoalsAdapter;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ThemeManager themeManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        authManager = new AuthManager(requireContext());
        themeManager = new ThemeManager(requireContext());
        setupImagePickerLauncher();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerViews();
        setupClickListeners();
        setupDarkModeToggle();
        loadUserProfile();
        loadSampleData();
    }

    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        updateProfilePicture(imageUri);
                    }
                }
            }
        );
    }

    private void setupRecyclerViews() {
        // Health History RecyclerView
        healthHistoryAdapter = new HealthHistoryAdapter(new ArrayList<>(), this::onHealthHistoryItemClick);
        binding.recyclerHealthHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerHealthHistory.setAdapter(healthHistoryAdapter);

        // Wellness Goals RecyclerView
        WellnessGoalsAdapter.OnGoalActionListener goalActionListener = new WellnessGoalsAdapter.OnGoalActionListener() {
            @Override
            public void onEditGoal(WellnessGoal goal) {
                onEditGoalClick(goal);
            }

            @Override
            public void onDeleteGoal(WellnessGoal goal) {
                onDeleteGoalClick(goal);
            }
        };
        wellnessGoalsAdapter = new WellnessGoalsAdapter(new ArrayList<>(), goalActionListener);
        binding.recyclerWellnessGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerWellnessGoals.setAdapter(wellnessGoalsAdapter);
    }

    private void setupClickListeners() {
        // Profile picture editing
        binding.fabEditPhoto.setOnClickListener(v -> openImagePicker());
        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        
        // QR Code generation
        binding.btnGenerateQr.setOnClickListener(v -> generateEmergencyQRCode());
        
        // Health History
        binding.tvViewAllHistory.setOnClickListener(v -> viewAllHealthHistory());
        
        // Smart Reminders
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> 
            toggleNotifications(isChecked));
        binding.layoutMedicineReminders.setOnClickListener(v -> manageMedicineReminders());
        binding.layoutAppointmentReminders.setOnClickListener(v -> manageAppointmentReminders());
        
        // AI Insights
        binding.btnViewDetailedInsights.setOnClickListener(v -> viewDetailedInsights());
        
        // Wellness Goals
        binding.tvAddGoal.setOnClickListener(v -> addNewGoal());
        
        // Account & Privacy
        binding.layoutUpdateProfile.setOnClickListener(v -> showEditProfileDialog());
        binding.layoutPrivacySettings.setOnClickListener(v -> showPrivacySettings());
        
        // Logout
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Load basic user info
            binding.tvUserName.setText(currentUser.getDisplayName() != null ? 
                currentUser.getDisplayName() : "User");
            binding.tvUserEmail.setText(currentUser.getEmail());
            
            // Load profile picture
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .circleCrop()
                    .into(binding.ivProfilePicture);
            }
            
            // Load health data
            loadHealthData();
        }
    }

    private void loadHealthData() {
        // Sample data - replace with actual data loading
        binding.tvAge.setText("25 years");
        binding.tvGender.setText("Male");
        binding.tvBloodGroup.setText("O+");
        binding.tvEmergencyContact.setText("+1 234 567 8900");
        binding.tvHeight.setText("175 cm");
        binding.tvWeight.setText("70 kg");
        binding.tvAllergies.setText("Peanuts, Shellfish");
        
        // Calculate and display BMI
        calculateAndDisplayBMI(175, 70); // height in cm, weight in kg
    }

    private void calculateAndDisplayBMI(double heightCm, double weightKg) {
        double bmi = BMICalculator.calculateBMI(heightCm, weightKg);
        String bmiCategory = BMICalculator.getBMICategory(bmi);
        
        DecimalFormat df = new DecimalFormat("#.#");
        binding.tvBmiValue.setText(df.format(bmi));
        binding.tvBmiCategory.setText(bmiCategory);
        
        // Set color based on BMI category
        int color = getBMIColor(bmiCategory);
        binding.tvBmiCategory.setTextColor(color);
    }

    private int getBMIColor(String category) {
        switch (category.toLowerCase()) {
            case "normal weight":
                return getResources().getColor(R.color.status_normal, null);
            case "overweight":
                return getResources().getColor(R.color.status_high, null);
            case "underweight":
            case "obese":
                return getResources().getColor(R.color.error, null);
            default:
                return getResources().getColor(R.color.secondary_text, null);
        }
    }

    private void loadSampleData() {
        // Load sample health history
        List<HealthHistoryItem> historyItems = new ArrayList<>();
        historyItems.add(new HealthHistoryItem("1", "Blood Test Report", 
            "All parameters within normal range", new Date(), "report", "normal"));
        historyItems.add(new HealthHistoryItem("2", "AI Health Recommendation", 
            "Consider increasing daily water intake", new Date(), "recommendation", "normal"));
        historyItems.add(new HealthHistoryItem("3", "Cholesterol Check", 
            "Slightly elevated levels detected", new Date(), "report", "high"));
        healthHistoryAdapter.updateItems(historyItems);

        // Load sample wellness goals
        List<WellnessGoal> goals = new ArrayList<>();
        goals.add(new WellnessGoal("1", "Daily Steps", "Walk 10,000 steps daily", 
            "steps", 10000, 7500, "steps"));
        goals.add(new WellnessGoal("2", "Water Intake", "Drink 8 glasses of water", 
            "water", 8, 6, "glasses"));
        wellnessGoalsAdapter.updateGoals(goals);

        // Load AI insights
        binding.tvAiInsights.setText("Based on your recent activity, you're maintaining good health habits. " +
            "Your BMI is in the normal range and your recent blood work shows improvement. " +
            "Consider maintaining your current exercise routine.");
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Profile Picture"));
    }

    private void updateProfilePicture(Uri imageUri) {
        Glide.with(this)
            .load(imageUri)
            .circleCrop()
            .into(binding.ivProfilePicture);
        
        Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
    }

    private void showEditProfileDialog() {
        Toast.makeText(getContext(), "Edit profile feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void generateEmergencyQRCode() {
        Toast.makeText(getContext(), "QR Code generation feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void onHealthHistoryItemClick(HealthHistoryItem item) {
        Toast.makeText(getContext(), "Viewing: " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void viewAllHealthHistory() {
        Toast.makeText(getContext(), "View all health history feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void toggleNotifications(boolean enabled) {
        Toast.makeText(getContext(), "Notifications " + (enabled ? "enabled" : "disabled"), 
            Toast.LENGTH_SHORT).show();
    }

    private void manageMedicineReminders() {
        Toast.makeText(getContext(), "Medicine reminders feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void manageAppointmentReminders() {
        Toast.makeText(getContext(), "Appointment reminders feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void viewDetailedInsights() {
        Toast.makeText(getContext(), "Detailed insights feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void addNewGoal() {
        Toast.makeText(getContext(), "Add new goal feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void onEditGoalClick(WellnessGoal goal) {
        Toast.makeText(getContext(), "Edit goal: " + goal.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void onDeleteGoalClick(WellnessGoal goal) {
        new MaterialAlertDialogBuilder(getContext())
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete this goal?")
            .setPositiveButton("Delete", (dialog, which) -> {
                Toast.makeText(getContext(), "Goal deleted: " + goal.getTitle(), Toast.LENGTH_SHORT).show();
                // Remove from adapter
                loadSampleData(); // Reload sample data
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showPrivacySettings() {
        Toast.makeText(getContext(), "Privacy settings feature coming soon", Toast.LENGTH_SHORT).show();
    }

    /**
     * Setup dark mode toggle with current state
     */
    private void setupDarkModeToggle() {
        updateDarkModeUI();
        
        // Set up click listener for the entire dark mode layout
        View.OnClickListener darkModeClickListener = v -> showThemeSelectionDialog();
        
        // Find the dark mode layout and set click listener
        View darkModeLayout = binding.getRoot().findViewById(R.id.layout_dark_mode);
        if (darkModeLayout != null) {
            darkModeLayout.setOnClickListener(darkModeClickListener);
        }
        
        // Also set click listener on the switch itself
        binding.switchDarkMode.setOnClickListener(darkModeClickListener);
    }

    /**
     * Update dark mode UI based on current theme
     */
    private void updateDarkModeUI() {
        int currentThemeMode = themeManager.getSavedThemeMode();
        boolean isDarkModeActive = themeManager.isDarkModeActive(requireContext());
        
        // Update switch state based on current theme
        binding.switchDarkMode.setOnCheckedChangeListener(null); // Remove listener temporarily
        
        switch (currentThemeMode) {
            case ThemeManager.MODE_SYSTEM:
                binding.switchDarkMode.setChecked(isDarkModeActive);
                binding.tvDarkModeStatus.setText(getString(R.string.dark_mode_follow_system));
                break;
            case ThemeManager.MODE_LIGHT:
                binding.switchDarkMode.setChecked(false);
                binding.tvDarkModeStatus.setText("Light mode");
                break;
            case ThemeManager.MODE_DARK:
                binding.switchDarkMode.setChecked(true);
                binding.tvDarkModeStatus.setText("Dark mode");
                break;
        }
    }

    /**
     * Show theme selection dialog
     */
    private void showThemeSelectionDialog() {
        String[] themeOptions = {
            "Follow system setting",
            "Light mode", 
            "Dark mode"
        };
        
        int currentSelection = themeManager.getSavedThemeMode();
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose theme")
            .setSingleChoiceItems(themeOptions, currentSelection, (dialog, which) -> {
                // Apply the selected theme
                themeManager.setThemeMode(which);
                
                // Update UI immediately
                updateDarkModeUI();
                
                // Show feedback
                String message = themeManager.getThemeModeDescription(requireContext(), which);
                Toast.makeText(requireContext(), message + " applied", Toast.LENGTH_SHORT).show();
                
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(getContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> performLogout())
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Perform complete logout process with proper navigation
     */
    private void performLogout() {
        // Show loading state
        binding.btnLogout.setEnabled(false);
        binding.btnLogout.setText("Logging out...");
        
        // Use LogoutManager for consistent logout behavior
        LogoutManager.performLogout(getActivity(), authManager, () -> {
            // This callback runs after logout is complete
            if (getContext() != null) {
                Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update dark mode UI when fragment resumes (in case system theme changed)
        if (binding != null) {
            updateDarkModeUI();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}