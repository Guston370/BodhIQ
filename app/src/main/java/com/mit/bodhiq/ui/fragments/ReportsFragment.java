package com.mit.bodhiq.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.mit.bodhiq.databinding.FragmentReportsBinding;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Enhanced ReportsFragment - Medical report scanning with three tabs
 * Features: Scan Report, Upload Report, History
 */
@AndroidEntryPoint
public class ReportsFragment extends Fragment {

    private FragmentReportsBinding binding;
    private ReportsTabAdapter tabAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupTabLayout();
    }

    private void setupTabLayout() {
        tabAdapter = new ReportsTabAdapter(this);
        binding.viewPager.setAdapter(tabAdapter);
        
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
            (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText("Scan Report");
                        tab.setIcon(com.mit.bodhiq.R.drawable.ic_camera);
                        break;
                    case 1:
                        tab.setText("Upload Report");
                        tab.setIcon(com.mit.bodhiq.R.drawable.ic_upload);
                        break;
                    case 2:
                        tab.setText("History");
                        tab.setIcon(com.mit.bodhiq.R.drawable.ic_history);
                        break;
                }
            }
        ).attach();
    }
    
    /**
     * Show processing overlay
     */
    public void showProcessing(String message) {
        binding.textProcessingStatus.setText(message);
        binding.layoutProcessing.setVisibility(View.VISIBLE);
    }
    
    /**
     * Hide processing overlay
     */
    public void hideProcessing() {
        binding.layoutProcessing.setVisibility(View.GONE);
    }
    
    /**
     * Switch to specific tab
     */
    public void switchToTab(int position) {
        binding.viewPager.setCurrentItem(position, true);
    }
    
    /**
     * ViewPager2 adapter for tabs
     */
    private static class ReportsTabAdapter extends FragmentStateAdapter {
        
        public ReportsTabAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }
        
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ScanReportFragment();
                case 1:
                    return new UploadReportFragment();
                case 2:
                    return new ReportsHistoryFragment();
                default:
                    return new ScanReportFragment();
            }
        }
        
        @Override
        public int getItemCount() {
            return 3;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}