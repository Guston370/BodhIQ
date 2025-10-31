package com.mit.bodhiq.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mit.bodhiq.data.model.ScannedReport;
import com.mit.bodhiq.data.repository.ReportsRepository;
import com.mit.bodhiq.databinding.FragmentReportsHistoryBinding;
import com.mit.bodhiq.ui.adapters.ScannedReportsAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Fragment for displaying reports history
 */
@AndroidEntryPoint
public class ReportsHistoryFragment extends Fragment {
    
    private FragmentReportsHistoryBinding binding;
    private ScannedReportsAdapter reportsAdapter;
    private CompositeDisposable disposables;
    private List<ScannedReport> allReports = new ArrayList<>();
    private List<ScannedReport> filteredReports = new ArrayList<>();
    
    @Inject
    ReportsRepository reportsRepository;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposables = new CompositeDisposable();
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReportsHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerView();
        setupSearchAndFilter();
        loadReports();
    }
    
    private void setupRecyclerView() {
        reportsAdapter = new ScannedReportsAdapter(filteredReports, this::onReportClick);
        binding.recyclerReports.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerReports.setAdapter(reportsAdapter);
    }
    
    private void setupSearchAndFilter() {
        // Search functionality
        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReports(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Filter button
        binding.btnFilter.setOnClickListener(v -> showFilterDialog());
        
        // Swipe to refresh
        binding.swipeRefresh.setOnRefreshListener(this::loadReports);
        
        // Empty state button
        binding.btnScanFirstReport.setOnClickListener(v -> {
            if (getParentFragment() instanceof ReportsFragment) {
                ((ReportsFragment) getParentFragment()).switchToTab(0); // Switch to scan tab
            }
        });
    }
    
    private void loadReports() {
        if (reportsRepository == null) {
            showEmptyState();
            return;
        }
        
        showLoading(true);
        
        disposables.add(
            reportsRepository.getUserReports()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    reports -> {
                        showLoading(false);
                        binding.swipeRefresh.setRefreshing(false);
                        
                        allReports.clear();
                        allReports.addAll(reports);
                        
                        filterReports(binding.editSearch.getText().toString());
                        
                        if (reports.isEmpty()) {
                            showEmptyState();
                        } else {
                            showReportsList();
                        }
                    },
                    error -> {
                        showLoading(false);
                        binding.swipeRefresh.setRefreshing(false);
                        showEmptyState();
                        
                        // Only show error if it's not a simple "no reports" case
                        if (!error.getMessage().contains("not found")) {
                            Toast.makeText(getContext(), "Failed to load reports: " + error.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                )
        );
    }
    
    private void filterReports(String searchTerm) {
        filteredReports.clear();
        
        if (searchTerm.trim().isEmpty()) {
            filteredReports.addAll(allReports);
        } else {
            String lowerSearchTerm = searchTerm.toLowerCase();
            for (ScannedReport report : allReports) {
                if (report.getTitle().toLowerCase().contains(lowerSearchTerm) ||
                    (report.getExtractedText() != null && 
                     report.getExtractedText().toLowerCase().contains(lowerSearchTerm))) {
                    filteredReports.add(report);
                }
            }
        }
        
        reportsAdapter.notifyDataSetChanged();
        
        if (filteredReports.isEmpty() && !allReports.isEmpty()) {
            // Show "no search results" state
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerReports.setVisibility(View.GONE);
        } else if (!filteredReports.isEmpty()) {
            showReportsList();
        }
    }
    
    private void showFilterDialog() {
        // TODO: Implement filter dialog with options like:
        // - Date range
        // - Status (completed, processing, failed)
        // - Health parameter types
        Toast.makeText(getContext(), "Filter options coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void onReportClick(ScannedReport report) {
        // Navigate to report details
        Intent intent = new Intent(getContext(), com.mit.bodhiq.ui.ReportDetailsActivity.class);
        intent.putExtra("report_id", report.getId());
        startActivity(intent);
    }
    
    private void showLoading(boolean show) {
        binding.layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.recyclerReports.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.recyclerReports.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
    }
    
    private void showReportsList() {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.recyclerReports.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh reports when returning to this fragment
        loadReports();
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