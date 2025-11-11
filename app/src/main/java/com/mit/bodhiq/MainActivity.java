package com.mit.bodhiq;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.mit.bodhiq.ui.base.BaseActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.animation.DecelerateInterpolator;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mit.bodhiq.utils.AuthManager;
import com.mit.bodhiq.utils.LogoutManager;
import com.mit.bodhiq.databinding.ActivityMainBinding;
import com.mit.bodhiq.ui.fragments.ChatAgentFragment;
import com.mit.bodhiq.ui.fragments.HomeFragment;
import com.mit.bodhiq.ui.fragments.ProfileFragment;
import com.mit.bodhiq.ui.fragments.ReportsFragment;
import com.mit.bodhiq.ui.login.LoginActivity;
import com.mit.bodhiq.data.repository.ProfileRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * MainActivity - Main app container with bottom navigation
 * Manages fragment switching and user authentication state
 * Contains: HomeFragment, ReportsFragment, ChatAgentFragment, ProfileFragment
 */
@AndroidEntryPoint
public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private AuthManager authManager;
    private FragmentManager fragmentManager;
    private CompositeDisposable disposables = new CompositeDisposable();
    
    @Inject
    ProfileRepository profileRepository;
    
    // AuthStateListener to monitor authentication changes
    private FirebaseAuth.AuthStateListener authStateListener;
    
    // Fragment instances to maintain state
    private HomeFragment homeFragment;
    private ReportsFragment reportsFragment;
    private ChatAgentFragment chatAgentFragment;
    private ProfileFragment profileFragment;
    
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable window content transitions
        getWindow().requestFeature(android.view.Window.FEATURE_CONTENT_TRANSITIONS);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Apply window insets to handle status bar properly
        // Only apply bottom inset for navigation bar, fragments will handle their own top insets
        applyBottomInset(binding.bottomNavigation);

        // Initialize Firebase Auth and AuthManager
        mAuth = FirebaseAuth.getInstance();
        authManager = new AuthManager(this);
        
        // Setup AuthStateListener
        setupAuthStateListener();
        
        // Check authentication state
        checkAuthenticationState();
        
        // Sync profile from cloud
        syncProfileData();
        
        // Initialize fragments and navigation
        initializeFragments();
        setupBottomNavigation();
        
        // Set initial fragment
        if (savedInstanceState == null) {
            showFragment(homeFragment);
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
            // Animate initial selected icon
            binding.bottomNavigation.post(() -> animateSelectedIcon(R.id.nav_home));
        }
    }

    /**
     * Setup AuthStateListener to monitor authentication changes in real-time
     */
    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // User signed out (could be manual logout or token expiration)
                Log.w("MainActivity", "User authentication lost, handling automatic logout");
                handleAutomaticLogout("Authentication state changed - user is null");
            }
        };
    }
    
    /**
     * Handle automatic logout scenarios (token expiration, etc.)
     */
    private void handleAutomaticLogout(String reason) {
        Log.w("MainActivity", "Handling automatic logout: " + reason);
        
        // Use LogoutManager to handle automatic logout
        LogoutManager.handleAutomaticLogout(this, reason);
        
        // Finish this activity to prevent back navigation
        finish();
    }

    /**
     * Check if user is authenticated, redirect to login if not
     */
    private void checkAuthenticationState() {
        if (!authManager.isUserAuthenticated()) {
            redirectToLogin();
            return;
        }
    }

    /**
     * Initialize all fragments
     */
    private void initializeFragments() {
        fragmentManager = getSupportFragmentManager();
        
        homeFragment = new HomeFragment();
        reportsFragment = new ReportsFragment();
        chatAgentFragment = new ChatAgentFragment();
        profileFragment = new ProfileFragment();
        
        // Add all fragments to the container but hide them initially
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.nav_host_fragment, homeFragment, "HOME");
        transaction.add(R.id.nav_host_fragment, reportsFragment, "REPORTS");
        transaction.add(R.id.nav_host_fragment, chatAgentFragment, "CHAT");
        transaction.add(R.id.nav_host_fragment, profileFragment, "PROFILE");
        
        // Hide all except home initially
        transaction.hide(reportsFragment);
        transaction.hide(chatAgentFragment);
        transaction.hide(profileFragment);
        
        transaction.commit();
        
        activeFragment = homeFragment;
    }

    /**
     * Setup bottom navigation listener
     */
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = homeFragment;
            } else if (itemId == R.id.nav_reports) {
                selectedFragment = reportsFragment;
            } else if (itemId == R.id.nav_chat) {
                selectedFragment = chatAgentFragment;
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = profileFragment;
            }
            
            if (selectedFragment != null && selectedFragment != activeFragment) {
                showFragment(selectedFragment);
                animateSelectedIcon(itemId);
                return true;
            }
            
            return false;
        });
    }

    /**
     * Animate the selected bottom navigation icon
     */
    private void animateSelectedIcon(int selectedItemId) {
        // Find the selected menu item view and animate it
        for (int i = 0; i < binding.bottomNavigation.getMenu().size(); i++) {
            android.view.MenuItem menuItem = binding.bottomNavigation.getMenu().getItem(i);
            android.view.View itemView = binding.bottomNavigation.findViewById(menuItem.getItemId());
            
            if (itemView != null) {
                if (menuItem.getItemId() == selectedItemId) {
                    // Animate selected item - subtle scale up
                    itemView.animate()
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .setDuration(200)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .start();
                } else {
                    // Reset other items to normal scale
                    itemView.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .start();
                }
            }
        }
    }

    /**
     * Show selected fragment with smooth transitions
     */
    private void showFragment(Fragment fragment) {
        if (fragment == activeFragment) {
            return;
        }
        
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Add smooth horizontal slide transitions for main tabs
        transaction.setCustomAnimations(
            R.anim.slide_in_right,  // enter
            R.anim.slide_out_left,  // exit
            R.anim.slide_in_left,   // popEnter
            R.anim.slide_out_right  // popExit
        );
        
        transaction.hide(activeFragment);
        transaction.show(fragment);
        transaction.commit();
        
        activeFragment = fragment;
    }

    /**
     * Redirect to LoginActivity
     */
    private void redirectToLogin() {
        authManager.redirectToLogin();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Add AuthStateListener when activity starts
        if (authStateListener != null) {
            mAuth.addAuthStateListener(authStateListener);
        }
        
        // Double-check authentication state
        checkAuthenticationState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove AuthStateListener when activity stops
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    /**
     * Sync profile data from Firebase cloud
     */
    private void syncProfileData() {
        disposables.add(
            profileRepository.syncProfileFromCloud()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> Log.d("MainActivity", "Profile synced from cloud"),
                    error -> Log.e("MainActivity", "Failed to sync profile", error)
                )
        );
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
        binding = null;
    }
}