package com.mit.bodhiq.data.repository;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mit.bodhiq.data.model.ScannedReport;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Repository for managing scanned medical reports
 */
@Singleton
public class ReportsRepository {
    
    private static final String COLLECTION_REPORTS = "scanned_reports";
    private static final String STORAGE_PATH_REPORTS = "report_images";
    
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;
    
    @Inject
    public ReportsRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }
    
    /**
     * Upload report image to Firebase Storage
     */
    public Single<String> uploadReportImage(Uri imageUri) {
        return Single.fromCallable(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            
            String fileName = "report_" + userId + "_" + UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storage.getReference()
                .child(STORAGE_PATH_REPORTS)
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
     * Save scanned report to Firestore
     */
    public Single<String> saveScannedReport(ScannedReport report) {
        return Single.fromCallable(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            
            report.setUserId(userId);
            
            return firestore.collection(COLLECTION_REPORTS)
                .add(report.toMap())
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().getId();
                    } else {
                        throw new RuntimeException("Failed to save report", task.getException());
                    }
                }).getResult();
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Get all scanned reports for current user
     */
    public Single<List<ScannedReport>> getUserReports() {
        return Single.fromCallable(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            
            return firestore.collection(COLLECTION_REPORTS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(ScannedReport.class);
                    } else {
                        throw new RuntimeException("Failed to fetch reports", task.getException());
                    }
                }).getResult();
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Get specific report by ID
     */
    public Single<ScannedReport> getReportById(String reportId) {
        return Single.fromCallable(() -> {
            return firestore.collection(COLLECTION_REPORTS)
                .document(reportId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        ScannedReport report = task.getResult().toObject(ScannedReport.class);
                        if (report != null) {
                            report.setId(task.getResult().getId());
                        }
                        return report;
                    } else {
                        throw new RuntimeException("Report not found");
                    }
                }).getResult();
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Delete report
     */
    public Completable deleteReport(String reportId) {
        return Completable.fromAction(() -> {
            firestore.collection(COLLECTION_REPORTS)
                .document(reportId)
                .delete()
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to delete report", e);
                });
        }).subscribeOn(Schedulers.io());
    }
    
    private String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
}