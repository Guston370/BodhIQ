package com.mit.bodhiq.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mit.bodhiq.data.model.UserProfile;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Repository for managing user profile data
 */
@Singleton
public class ProfileRepository {

    private static final String COLLECTION_USERS = "users";
    private static final String STORAGE_PATH_PROFILE_IMAGES = "profile_images";
    private static final String PREFS_NAME = "bodhiq_profile_cache";
    private static final String PREF_EMERGENCY_PHONE = "emergency_contact_phone";
    private static final String PREF_EMERGENCY_NAME = "emergency_contact_name";

    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;
    private final SharedPreferences prefs;

    @Inject
    public ProfileRepository(@ApplicationContext Context context) {
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get current user profile from Firestore
     */
    public Single<UserProfile> getUserProfile() {
        return Single.<UserProfile>create(emitter -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                emitter.onError(new IllegalStateException("User not authenticated"));
                return;
            }

            firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                            if (profile != null) {
                                emitter.onSuccess(profile);
                            } else {
                                emitter.onSuccess(createProfileFromAuth());
                            }
                        } else {
                            // Create profile from Firebase Auth data if doesn't exist
                            UserProfile newProfile = createProfileFromAuth();
                            // Save the new profile to Firestore
                            saveUserProfileSync(newProfile);
                            emitter.onSuccess(newProfile);
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("ProfileRepository", "Failed to get profile", e);
                        // Fallback to auth data
                        emitter.onSuccess(createProfileFromAuth());
                    });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Save or update user profile
     * ONE-SAVE FIX: Writes emergency contact to local cache FIRST (instant), then
     * Firestore (async)
     */
    public Completable saveUserProfile(UserProfile profile) {
        return Completable.create(emitter -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                emitter.onError(new IllegalStateException("User not authenticated"));
                return;
            }

            profile.setUserId(userId);
            profile.setUpdatedAt(System.currentTimeMillis());

            // ONE-SAVE FIX: Cache emergency contact locally FIRST (synchronous, instant)
            cacheEmergencyContact(profile.getEmergencyContactName(), profile.getEmergencyContactPhone());

            // Then save to Firestore (async, network-dependent)
            firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .set(profile)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("ProfileRepository", "Profile saved to Firestore");
                        emitter.onComplete();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("ProfileRepository", "Failed to save to Firestore", e);
                        // Even if Firestore fails, local cache succeeded
                        emitter.onComplete(); // Don't fail - local cache is already saved
                    });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Save user profile synchronously (for internal use)
     */
    private void saveUserProfileSync(UserProfile profile) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return;
        }

        profile.setUserId(userId);
        profile.setUpdatedAt(System.currentTimeMillis());

        firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(profile)
                .addOnSuccessListener(aVoid -> android.util.Log.d("ProfileRepository", "Profile auto-saved"))
                .addOnFailureListener(e -> android.util.Log.e("ProfileRepository", "Failed to auto-save profile", e));
    }

    /**
     * Upload profile image to Firebase Storage
     */
    public Single<String> uploadProfileImage(Uri imageUri) {
        return Single.<String>create(emitter -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                emitter.onError(new IllegalStateException("User not authenticated"));
                return;
            }

            String fileName = "profile_" + userId + "_" + UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storage.getReference()
                    .child(STORAGE_PATH_PROFILE_IMAGES)
                    .child(fileName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    android.util.Log.d("ProfileRepository", "Image uploaded: " + uri.toString());
                                    emitter.onSuccess(uri.toString());
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("ProfileRepository", "Failed to get download URL", e);
                                    emitter.onError(
                                            new RuntimeException("Failed to get download URL: " + e.getMessage(), e));
                                });
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("ProfileRepository", "Upload failed", e);
                        emitter.onError(new RuntimeException("Upload failed: " + e.getMessage(), e));
                    });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Delete old profile image from storage
     */
    public Completable deleteProfileImage(String imageUrl) {
        return Completable.fromAction(() -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
                    imageRef.delete();
                } catch (Exception e) {
                    // Ignore errors when deleting old images
                    android.util.Log.w("ProfileRepository", "Failed to delete old image: " + e.getMessage());
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Get login provider information
     */
    public String getLoginProvider() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && !user.getProviderData().isEmpty()) {
            String providerId = user.getProviderData().get(1).getProviderId(); // Skip firebase provider
            switch (providerId) {
                case "google.com":
                    return "Google Account";
                case "facebook.com":
                    return "Facebook Account";
                case "twitter.com":
                    return "Twitter Account";
                case "password":
                    return "Email Account";
                default:
                    return "Unknown Provider";
            }
        }
        return "Email Account";
    }

    /**
     * Sync profile data from Firebase (call on app start)
     */
    public Completable syncProfileFromCloud() {
        return Completable.create(emitter -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                emitter.onComplete(); // Not logged in, nothing to sync
                return;
            }

            android.util.Log.d("ProfileRepository", "Syncing profile from cloud...");

            firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            android.util.Log.d("ProfileRepository", "Profile synced from cloud");
                            emitter.onComplete();
                        } else {
                            // Create initial profile if doesn't exist
                            UserProfile newProfile = createProfileFromAuth();
                            saveUserProfileSync(newProfile);
                            android.util.Log.d("ProfileRepository", "Created initial profile in cloud");
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("ProfileRepository", "Failed to sync profile", e);
                        emitter.onComplete(); // Don't fail, just log
                    });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Sign out user
     */
    public Completable signOut() {
        return Completable.fromAction(() -> {
            auth.signOut();
        }).subscribeOn(Schedulers.io());
    }

    // Helper methods

    private String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private UserProfile createProfileFromAuth() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            UserProfile profile = new UserProfile(
                    user.getUid(),
                    user.getDisplayName() != null ? user.getDisplayName() : "",
                    user.getEmail() != null ? user.getEmail() : "");

            if (user.getPhotoUrl() != null) {
                profile.setProfileImageUrl(user.getPhotoUrl().toString());
            }

            profile.setLoginProvider(getLoginProvider());
            return profile;
        }
        return new UserProfile();
    }

    /**
     * ONE-SAVE FIX: Cache emergency contact to SharedPreferences (instant, no
     * network)
     * Trim and normalize phone before storing
     */
    private void cacheEmergencyContact(String name, String phone) {
        SharedPreferences.Editor editor = prefs.edit();

        // Trim and normalize
        String normalizedName = (name != null) ? name.trim() : "";
        String normalizedPhone = normalizePhone(phone);

        editor.putString(PREF_EMERGENCY_NAME, normalizedName);
        editor.putString(PREF_EMERGENCY_PHONE, normalizedPhone);

        // CRITICAL: Use commit() for synchronous save (not apply())
        editor.commit();

        android.util.Log.d("ProfileRepository", "Emergency contact cached locally");
    }

    /**
     * ONE-SAVE FIX: Get cached emergency contact phone (instant, no network)
     * Returns normalized phone
     */
    public String getCachedEmergencyPhone() {
        String phone = prefs.getString(PREF_EMERGENCY_PHONE, null);
        return (phone != null && !phone.isEmpty()) ? phone : null;
    }

    /**
     * ONE-SAVE FIX: Get cached emergency contact name (instant, no network)
     */
    public String getCachedEmergencyName() {
        String name = prefs.getString(PREF_EMERGENCY_NAME, null);
        return (name != null && !name.isEmpty()) ? name : null;
    }

    /**
     * Normalize and trim phone number
     * Removes leading/trailing whitespace
     */
    private String normalizePhone(String phone) {
        if (phone == null)
            return "";
        return phone.trim();
    }
}