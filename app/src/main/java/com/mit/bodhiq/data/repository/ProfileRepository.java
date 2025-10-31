package com.mit.bodhiq.data.repository;

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
    
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;
    
    @Inject
    public ProfileRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }
    
    /**
     * Get current user profile from Firestore
     */
    public Single<UserProfile> getUserProfile() {
        return Single.fromCallable(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            
            return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        return task.getResult().toObject(UserProfile.class);
                    } else {
                        // Create profile from Firebase Auth data if doesn't exist
                        return createProfileFromAuth();
                    }
                }).getResult();
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Save or update user profile
     */
    public Completable saveUserProfile(UserProfile profile) {
        return Completable.fromAction(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            
            profile.setUserId(userId);
            profile.setUpdatedAt(System.currentTimeMillis());
            
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(profile)
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to save profile", e);
                });
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Upload profile image to Firebase Storage
     */
    public Single<String> uploadProfileImage(Uri imageUri) {
        return Single.fromCallable(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            
            String fileName = "profile_" + userId + "_" + UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storage.getReference()
                .child(STORAGE_PATH_PROFILE_IMAGES)
                .child(fileName);
            
            return imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw new RuntimeException("Upload failed", task.getException());
                    }
                    return imageRef.getDownloadUrl();
                })
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toString();
                    } else {
                        throw new RuntimeException("Failed to get download URL", task.getException());
                    }
                }).getResult();
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
                user.getEmail() != null ? user.getEmail() : ""
            );
            
            if (user.getPhotoUrl() != null) {
                profile.setProfileImageUrl(user.getPhotoUrl().toString());
            }
            
            profile.setLoginProvider(getLoginProvider());
            return profile;
        }
        return new UserProfile();
    }
}