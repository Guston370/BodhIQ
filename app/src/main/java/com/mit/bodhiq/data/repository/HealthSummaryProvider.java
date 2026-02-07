package com.mit.bodhiq.data.repository;

import android.util.Log;

import com.mit.bodhiq.data.model.HealthSummary;
import com.mit.bodhiq.data.model.UserProfile;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Health Summary Provider - Aggregates data from Profile and Reports
 * Publishes updates via Observable for live UI refresh
 * Privacy: No PHI logging
 */
@Singleton
public class HealthSummaryProvider {
    private static final String TAG = "HealthSummary";

    private final ProfileRepository profileRepository;
    private final BehaviorSubject<HealthSummary> summarySubject;

    @Inject
    public HealthSummaryProvider(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        this.summarySubject = BehaviorSubject.create();
    }

    /**
     * Get current health summary as Observable (live updates)
     */
    public Observable<HealthSummary> getHealthSummary() {
        return summarySubject;
    }

    /**
     * Refresh health summary from all sources
     * Call after profile updates or report imports
     */
    public void refreshSummary() {
        profileRepository.getUserProfile()
                .subscribe(
                        this::aggregateFromProfile,
                        error -> {
                            Log.w(TAG, "Failed to load profile for summary");
                            publishEmptySummary();
                        });
    }

    /**
     * Aggregate data from UserProfile
     * Privacy: Trim values, redact nothing (used for display), no PHI logging
     */
    private void aggregateFromProfile(UserProfile profile) {
        HealthSummary summary = new HealthSummary();

        // Basic info
        summary.setName(trim(profile.getFullName()));
        summary.setAge(trim(profile.getAge()));
        summary.setDateOfBirth(trim(profile.getDateOfBirth()));
        summary.setGender(trim(profile.getGender()));

        // Health data
        summary.setBloodType(trim(profile.getBloodGroup()));
        summary.setAllergies(trim(profile.getAllergies()));
        summary.setHeight(trim(profile.getHeight()));
        summary.setWeight(trim(profile.getWeight()));

        // Emergency contact
        String emergencyPhone = profileRepository.getCachedEmergencyPhone();
        String emergencyName = profileRepository.getCachedEmergencyName();
        summary.setEmergencyContactPhone(trim(emergencyPhone));
        summary.setEmergencyContactName(trim(emergencyName));

        summary.setLastUpdated(profile.getUpdatedAt());

        // Calculate presence flags
        boolean hasEmergency = hasValue(emergencyPhone);
        boolean hasVitals = hasValue(profile.getHeight()) || hasValue(profile.getWeight())
                || hasValue(profile.getBloodGroup());
        boolean hasAny = hasValue(profile.getFullName()) || hasValue(profile.getAge())
                || hasValue(profile.getBloodGroup()) || hasValue(profile.getAllergies())
                || hasEmergency || hasVitals;

        summary.setHasEmergencyContact(hasEmergency);
        summary.setHasVitalInfo(hasVitals);
        summary.setHasAnyData(hasAny);

        // Privacy: Log status only, no PHI
        Log.d(TAG, "Health summary updated: has data=" + hasAny);

        summarySubject.onNext(summary);
    }

    private void publishEmptySummary() {
        HealthSummary empty = new HealthSummary();
        empty.setHasAnyData(false);
        summarySubject.onNext(empty);
    }

    private String trim(String value) {
        return (value != null) ? value.trim() : null;
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
