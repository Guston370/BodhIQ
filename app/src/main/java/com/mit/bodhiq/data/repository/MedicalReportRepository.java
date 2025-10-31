package com.mit.bodhiq.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mit.bodhiq.data.model.MedicalReport;
import com.mit.bodhiq.data.model.MedicalParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Repository for managing medical reports in Firebase Firestore
 */
@Singleton
public class MedicalReportRepository {
    
    private static final String COLLECTION_MEDICAL_REPORTS = "medical_reports";
    private static final String STORAGE_PATH_REPORTS = "medical_reports";
    
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;
    
    @Inject
    public MedicalReportRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }
    
    /**
     * Save medical report to Firestore
     */
    public Single<String> saveMedicalReport(MedicalReport report) {
        return Single.fromCallable(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            
            report.setUserId(userId);
            report.setUpdatedAt(System.currentTimeMillis());
            
            Map<String, Object> reportData = convertToMap(report);
            
            return firestore.collection(COLLECTION_MEDICAL_REPORTS)
                .add(reportData)
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
     * Update existing medical report
     */
    public Completable updateMedicalReport(String reportId, MedicalReport report) {
        return Completable.fromAction(() -> {
            report.setUpdatedAt(System.currentTimeMillis());
            Map<String, Object> reportData = convertToMap(report);
            
            firestore.collection(COLLECTION_MEDICAL_REPORTS)
                .document(reportId)
                .set(reportData)
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to update report", e);
                });
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Get all medical reports for current user
     */
    public Flowable<List<MedicalReport>> getUserMedicalReports() {
        return Flowable.fromCallable(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            
            return firestore.collection(COLLECTION_MEDICAL_REPORTS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(MedicalReport.class);
                    } else {
                        throw new RuntimeException("Failed to fetch reports", task.getException());
                    }
                }).getResult();
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Get medical report by ID
     */
    public Single<MedicalReport> getMedicalReportById(String reportId) {
        return Single.fromCallable(() -> {
            return firestore.collection(COLLECTION_MEDICAL_REPORTS)
                .document(reportId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        return task.getResult().toObject(MedicalReport.class);
                    } else {
                        throw new RuntimeException("Report not found");
                    }
                }).getResult();
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Delete medical report
     */
    public Completable deleteMedicalReport(String reportId) {
        return Completable.fromAction(() -> {
            firestore.collection(COLLECTION_MEDICAL_REPORTS)
                .document(reportId)
                .delete()
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to delete report", e);
                });
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Upload image to Firebase Storage
     */
    public Single<String> uploadReportImage(byte[] imageData, String fileName) {
        return Single.fromCallable(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            
            StorageReference imageRef = storage.getReference()
                .child(STORAGE_PATH_REPORTS)
                .child(userId)
                .child(fileName);
            
            return imageRef.putBytes(imageData)
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
     * Search medical reports by text
     */
    public Flowable<List<MedicalReport>> searchMedicalReports(String searchTerm) {
        return Flowable.fromCallable(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            
            // Note: Firestore doesn't support full-text search natively
            // This is a simplified search that looks for exact matches in summary
            return firestore.collection(COLLECTION_MEDICAL_REPORTS)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("summary", searchTerm)
                .whereLessThanOrEqualTo("summary", searchTerm + "\uf8ff")
                .orderBy("summary")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(MedicalReport.class);
                    } else {
                        throw new RuntimeException("Search failed", task.getException());
                    }
                }).getResult();
        }).subscribeOn(Schedulers.io());
    }
    
    /**
     * Get reports by status
     */
    public Flowable<List<MedicalReport>> getReportsByStatus(String status) {
        return Flowable.fromCallable(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            
            return firestore.collection(COLLECTION_MEDICAL_REPORTS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(MedicalReport.class);
                    } else {
                        throw new RuntimeException("Failed to fetch reports", task.getException());
                    }
                }).getResult();
        }).subscribeOn(Schedulers.io());
    }
    
    // Helper methods
    
    private String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
    
    private Map<String, Object> convertToMap(MedicalReport report) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", report.getUserId());
        data.put("originalImagePath", report.getOriginalImagePath());
        data.put("extractedText", report.getExtractedText());
        data.put("parameters", convertParametersToMap(report.getParameters()));
        data.put("aiInsights", report.getAiInsights());
        data.put("summary", report.getSummary());
        data.put("createdAt", report.getCreatedAt());
        data.put("updatedAt", report.getUpdatedAt());
        data.put("status", report.getStatus());
        return data;
    }
    
    private List<Map<String, Object>> convertParametersToMap(List<MedicalParameter> parameters) {
        if (parameters == null) return null;
        
        return parameters.stream()
            .map(param -> {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("parameter", param.getParameter());
                paramMap.put("value", param.getValue());
                paramMap.put("unit", param.getUnit());
                paramMap.put("status", param.getStatus());
                paramMap.put("referenceRange", param.getReferenceRange());
                paramMap.put("notes", param.getNotes());
                return paramMap;
            })
            .collect(java.util.stream.Collectors.toList());
    }
}