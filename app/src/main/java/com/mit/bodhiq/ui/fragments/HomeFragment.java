package com.mit.bodhiq.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mit.bodhiq.R;
import com.mit.bodhiq.data.model.UserProfile;
import com.mit.bodhiq.data.repository.ProfileRepository;
import com.mit.bodhiq.databinding.FragmentHomeBinding;
import com.mit.bodhiq.ui.EmergencyQrActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * HomeFragment - Medical-grade dashboard
 * Calm, minimal overview without duplicating existing features
 */
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private CompositeDisposable disposables;
    
    @Inject
    ProfileRepository profileRepository;
    
    private FirebaseAuth firebaseAuth;
    private UserProfile userProfile;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposables = new CompositeDisposable();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupHeader();
        setupGreeting();
        setupHealthTips();
        setupQuickActions();
        setupEmergencyBanner();
        loadUserProfile();
    }
    
    private void setupHeader() {
        // Profile avatar click
        binding.ivProfileAvatar.setOnClickListener(v -> navigateToProfile());
    }
    
    private void setupGreeting() {
        // Time-based greeting
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;
        
        if (hour < 12) {
            greeting = "Good morning";
        } else if (hour < 17) {
            greeting = "Good afternoon";
        } else {
            greeting = "Good evening";
        }
        
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userName = user != null && user.getDisplayName() != null ? 
            user.getDisplayName().split(" ")[0] : "there";
        
        binding.tvGreeting.setText(greeting + ", " + userName);
    }
    
    private void setupHealthTips() {
        // Load random health tip
        String[] tips = getResources().getStringArray(R.array.health_tips);
        int randomIndex = new Random().nextInt(tips.length);
        binding.tvHealthTip.setText(tips[randomIndex]);
    }
    
    private void setupQuickActions() {
        // Emergency QR
        binding.cardEmergencyQr.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EmergencyQrActivity.class);
            startActivity(intent);
        });
        
        // Scan Report
        binding.cardScanReport.setOnClickListener(v -> {
            navigateToTab(R.id.nav_reports);
        });
        
        // Health Tracker (placeholder - navigate to profile)
        binding.cardHealthTracker.setOnClickListener(v -> {
            navigateToProfile();
        });
    }
    
    private void setupEmergencyBanner() {
        // Tap to open Emergency QR
        binding.layoutEmergencyBanner.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EmergencyQrActivity.class);
            startActivity(intent);
        });
        
        // Long press to call emergency contact
        binding.layoutEmergencyBanner.setOnLongClickListener(v -> {
            callEmergencyContact();
            return true;
        });
    }
    
    private void loadUserProfile() {
        disposables.add(
            profileRepository.getUserProfile()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    profile -> {
                        userProfile = profile;
                        updateUIWithProfile(profile);
                    },
                    error -> {
                        // Handle silently
                    }
                )
        );
    }
    
    private void updateUIWithProfile(UserProfile profile) {
        // Update profile avatar
        if (profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                .load(profile.getProfileImageUrl())
                .circleCrop()
                .into(binding.ivProfileAvatar);
        }
        
        // Update status message
        String status = "Your health data is up to date";
        binding.tvStatus.setText(status);
    }
    
    private void callEmergencyContact() {
        if (userProfile != null && userProfile.getEmergencyContact() != null) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + userProfile.getEmergencyContact()));
            startActivity(intent);
        } else {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:911"));
            startActivity(intent);
        }
    }
    
    private void navigateToProfile() {
        navigateToTab(R.id.nav_profile);
    }
    
    private void navigateToTab(int tabId) {
        if (getActivity() instanceof com.mit.bodhiq.MainActivity) {
            com.mit.bodhiq.MainActivity mainActivity = (com.mit.bodhiq.MainActivity) getActivity();
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                    mainActivity.findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(tabId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (disposables != null) {
            disposables.clear();
        }
        binding = null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposables != null) {
            disposables.dispose();
        }
    }
}