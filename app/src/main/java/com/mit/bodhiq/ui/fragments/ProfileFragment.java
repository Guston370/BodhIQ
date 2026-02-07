package com.mit.bodhiq.ui.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.mit.bodhiq.utils.AuthManager;
import com.mit.bodhiq.R;
import com.mit.bodhiq.data.model.UserProfile;
import com.mit.bodhiq.data.repository.ProfileRepository;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

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

    @Inject
    ProfileRepository profileRepository;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private UserProfile currentUserProfile;

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
                });
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
        binding.switchNotifications
                .setOnCheckedChangeListener((buttonView, isChecked) -> toggleNotifications(isChecked));
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
            // Show loading
            binding.layoutLoading.setVisibility(View.VISIBLE);

            // Load profile from Firestore
            Disposable disposable = profileRepository.getUserProfile()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            profile -> {
                                currentUserProfile = profile;
                                displayUserProfile(profile);
                                binding.layoutLoading.setVisibility(View.GONE);
                            },
                            error -> {
                                // Fallback to Firebase Auth data
                                currentUserProfile = createProfileFromFirebaseAuth(currentUser);
                                displayUserProfile(currentUserProfile);
                                binding.layoutLoading.setVisibility(View.GONE);
                            });
            compositeDisposable.add(disposable);
        }
    }

    private UserProfile createProfileFromFirebaseAuth(FirebaseUser user) {
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getUid());
        profile.setFullName(user.getDisplayName() != null ? user.getDisplayName() : "User");
        profile.setEmail(user.getEmail());
        if (user.getPhotoUrl() != null) {
            profile.setProfileImageUrl(user.getPhotoUrl().toString());
        }
        return profile;
    }

    private void displayUserProfile(UserProfile profile) {
        // Basic info
        binding.tvUserName.setText(profile.getFullName() != null ? profile.getFullName() : "User");
        binding.tvUserEmail.setText(profile.getEmail());

        // Profile picture
        if (profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(profile.getProfileImageUrl())
                    .circleCrop()
                    .into(binding.ivProfilePicture);
        }

        // Health data
        if (profile.getAge() != null && !profile.getAge().isEmpty()) {
            binding.tvAge.setText(profile.getAge() + " years");
        } else {
            binding.tvAge.setText("Not set");
        }

        binding.tvGender.setText(profile.getGender() != null ? profile.getGender() : "Not set");
        binding.tvBloodGroup.setText(profile.getBloodGroup() != null ? profile.getBloodGroup() : "Not set");

        // Display emergency contact (name and phone)
        String emergencyDisplay = "Not set";
        if (profile.getEmergencyContactPhone() != null && !profile.getEmergencyContactPhone().isEmpty()) {
            if (profile.getEmergencyContactName() != null && !profile.getEmergencyContactName().isEmpty()) {
                emergencyDisplay = profile.getEmergencyContactName() + ": " + profile.getEmergencyContactPhone();
            } else {
                emergencyDisplay = profile.getEmergencyContactPhone();
            }
        }
        binding.tvEmergencyContact.setText(emergencyDisplay);
        binding.tvHeight.setText(profile.getHeight() != null ? profile.getHeight() + " cm" : "Not set");
        binding.tvWeight.setText(profile.getWeight() != null ? profile.getWeight() + " kg" : "Not set");
        binding.tvAllergies.setText(
                profile.getAllergies() != null && !profile.getAllergies().isEmpty() ? profile.getAllergies() : "None");

        // Calculate BMI if height and weight are available
        if (profile.getHeight() != null && profile.getWeight() != null) {
            try {
                double height = Double.parseDouble(profile.getHeight());
                double weight = Double.parseDouble(profile.getWeight());
                calculateAndDisplayBMI(height, weight);
            } catch (NumberFormatException e) {
                binding.tvBmiValue.setText("--");
                binding.tvBmiCategory.setText("Data incomplete");
            }
        } else {
            binding.tvBmiValue.setText("--");
            binding.tvBmiCategory.setText("Data incomplete");
        }
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
        if (currentUserProfile == null) {
            Toast.makeText(getContext(), "Loading profile data...", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile_full, null);

        // Initialize input fields
        TextInputEditText etFullName = dialogView.findViewById(R.id.et_full_name);
        TextInputEditText etEmail = dialogView.findViewById(R.id.et_email);
        TextInputEditText etPhone = dialogView.findViewById(R.id.et_phone);
        TextInputEditText etAge = dialogView.findViewById(R.id.et_age);
        TextInputEditText etDateOfBirth = dialogView.findViewById(R.id.et_date_of_birth);
        AutoCompleteTextView etGender = dialogView.findViewById(R.id.et_gender);
        AutoCompleteTextView etBloodGroup = dialogView.findViewById(R.id.et_blood_group);
        TextInputEditText etHeight = dialogView.findViewById(R.id.et_height);
        TextInputEditText etWeight = dialogView.findViewById(R.id.et_weight);
        TextInputEditText etEmergencyContactName = dialogView.findViewById(R.id.et_emergency_contact_name);
        TextInputEditText etEmergencyContactPhone = dialogView.findViewById(R.id.et_emergency_contact_phone);
        TextInputEditText etAddress = dialogView.findViewById(R.id.et_address);
        TextInputEditText etAllergies = dialogView.findViewById(R.id.et_allergies);

        // Populate current values
        etFullName.setText(currentUserProfile.getFullName());
        etEmail.setText(currentUserProfile.getEmail());
        etPhone.setText(currentUserProfile.getPhoneNumber());
        etAge.setText(currentUserProfile.getAge());
        etDateOfBirth.setText(currentUserProfile.getDateOfBirth());
        etGender.setText(currentUserProfile.getGender());
        etBloodGroup.setText(currentUserProfile.getBloodGroup());
        etHeight.setText(currentUserProfile.getHeight());
        etWeight.setText(currentUserProfile.getWeight());
        etEmergencyContactName.setText(currentUserProfile.getEmergencyContactName());
        etEmergencyContactPhone.setText(currentUserProfile.getEmergencyContactPhone());
        etAddress.setText(currentUserProfile.getAddress());
        etAllergies.setText(currentUserProfile.getAllergies());

        // Setup dropdowns
        String[] genders = { "Male", "Female", "Other", "Prefer not to say" };
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, genders);
        etGender.setAdapter(genderAdapter);

        String[] bloodGroups = { "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-" };
        ArrayAdapter<String> bloodGroupAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, bloodGroups);
        etBloodGroup.setAdapter(bloodGroupAdapter);

        // Date picker for date of birth
        etDateOfBirth.setOnClickListener(v -> showDatePicker(etDateOfBirth));

        // Create dialog
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getContext())
                .setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();

        // Setup buttons
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            // Validate and save
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String dateOfBirth = etDateOfBirth.getText().toString().trim();
            String gender = etGender.getText().toString().trim();
            String bloodGroup = etBloodGroup.getText().toString().trim();
            String height = etHeight.getText().toString().trim();
            String weight = etWeight.getText().toString().trim();
            String emergencyContactName = etEmergencyContactName.getText().toString().trim();
            String emergencyContactPhone = etEmergencyContactPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String allergies = etAllergies.getText().toString().trim();

            // Validate required fields
            if (TextUtils.isEmpty(fullName)) {
                Toast.makeText(getContext(), "Please enter your full name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate emergency phone number if provided
            if (!TextUtils.isEmpty(emergencyContactPhone)) {
                if (!isValidPhoneNumber(emergencyContactPhone)) {
                    Toast.makeText(getContext(), "Enter a valid phone number", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // Update profile
            currentUserProfile.setFullName(fullName);
            currentUserProfile.setEmail(email);
            currentUserProfile.setPhoneNumber(phone);
            currentUserProfile.setAge(age);
            currentUserProfile.setDateOfBirth(dateOfBirth);
            currentUserProfile.setGender(gender);
            currentUserProfile.setBloodGroup(bloodGroup);
            currentUserProfile.setHeight(height);
            currentUserProfile.setWeight(weight);
            currentUserProfile.setEmergencyContactName(emergencyContactName);
            currentUserProfile.setEmergencyContactPhone(emergencyContactPhone);
            currentUserProfile.setAddress(address);
            currentUserProfile.setAllergies(allergies);

            dialog.dismiss();
            saveUserProfile(currentUserProfile);
        });

        dialog.show();
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();

        // Parse existing date if available
        String currentDate = editText.getText().toString();
        if (!TextUtils.isEmpty(currentDate)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = sdf.parse(currentDate);
                if (date != null) {
                    calendar.setTime(date);
                }
            } catch (Exception e) {
                // Use current date
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                            dayOfMonth, month + 1, year);
                    editText.setText(selectedDate);

                    // Calculate age
                    Calendar dob = Calendar.getInstance();
                    dob.set(year, month, dayOfMonth);
                    int age = Calendar.getInstance().get(Calendar.YEAR) - year;
                    if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                        age--;
                    }

                    // Update age field if available
                    View parent = (View) editText.getParent().getParent().getParent();
                    TextInputEditText etAge = parent.findViewById(R.id.et_age);
                    if (etAge != null) {
                        etAge.setText(String.valueOf(age));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void saveUserProfile(UserProfile profile) {
        // Show loading
        binding.layoutLoading.setVisibility(View.VISIBLE);

        Disposable disposable = profileRepository.saveUserProfile(profile)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            // Update Firebase Auth email if changed
                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                            if (currentUser != null && !profile.getEmail().equals(currentUser.getEmail())) {
                                updateFirebaseEmail(profile.getEmail());
                            }

                            // Update Firebase Auth display name if changed
                            if (currentUser != null && !profile.getFullName().equals(currentUser.getDisplayName())) {
                                updateFirebaseDisplayName(profile.getFullName());
                            }

                            binding.layoutLoading.setVisibility(View.GONE);
                            displayUserProfile(profile);

                            Snackbar.make(binding.getRoot(), "Profile updated successfully", Snackbar.LENGTH_LONG)
                                    .setAction("OK", v -> {
                                    })
                                    .show();
                        },
                        error -> {
                            binding.layoutLoading.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Failed to update profile: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });
        compositeDisposable.add(disposable);
    }

    private void updateFirebaseEmail(String newEmail) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.updateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Email synced with Firebase", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Email sync failed. Please re-authenticate.",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateFirebaseDisplayName(String newName) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnSuccessListener(aVoid -> {
                        // Name updated successfully
                    })
                    .addOnFailureListener(e -> {
                        // Handle error silently
                    });
        }
    }

    private void generateEmergencyQRCode() {
        Intent intent = new Intent(getContext(), com.mit.bodhiq.ui.EmergencyQrActivity.class);
        startActivity(intent);
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
        Intent intent = new Intent(getContext(), com.mit.bodhiq.ui.reminders.RemindersActivity.class);
        startActivity(intent);
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
     * Validate phone number - basic check for 10+ digits
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null)
            return false;
        // Remove spaces, dashes, parentheses, plus signs
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\+]", "");
        // Check if it's 10 or more digits
        return cleaned.matches("\\d{10,}");
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
        compositeDisposable.clear();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}