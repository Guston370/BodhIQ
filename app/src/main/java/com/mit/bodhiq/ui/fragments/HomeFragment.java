package com.mit.bodhiq.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mit.bodhiq.databinding.FragmentHomeBinding;

/**
 * HomeFragment - Main dashboard fragment
 * Shows overview of app features and recent activity
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize home screen content
        setupHomeContent();
    }

    private void setupHomeContent() {
        // Setup quick action buttons
        binding.btnViewReports.setOnClickListener(v -> navigateToReports());
        binding.btnStartChat.setOnClickListener(v -> navigateToChat());
    }
    
    private void navigateToReports() {
        // Add subtle animation feedback
        binding.btnViewReports.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    binding.btnViewReports.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start();
                    
                    // Navigate to Reports tab
                    if (getActivity() instanceof com.mit.bodhiq.MainActivity) {
                        com.mit.bodhiq.MainActivity mainActivity = (com.mit.bodhiq.MainActivity) getActivity();
                        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                                mainActivity.findViewById(com.mit.bodhiq.R.id.bottom_navigation);
                        bottomNav.setSelectedItemId(com.mit.bodhiq.R.id.nav_reports);
                    }
                })
                .start();
    }
    
    private void navigateToChat() {
        // Add subtle animation feedback
        binding.btnStartChat.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    binding.btnStartChat.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start();
                    
                    // Navigate to Chat tab
                    if (getActivity() instanceof com.mit.bodhiq.MainActivity) {
                        com.mit.bodhiq.MainActivity mainActivity = (com.mit.bodhiq.MainActivity) getActivity();
                        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                                mainActivity.findViewById(com.mit.bodhiq.R.id.bottom_navigation);
                        bottomNav.setSelectedItemId(com.mit.bodhiq.R.id.nav_chat);
                    }
                })
                .start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}