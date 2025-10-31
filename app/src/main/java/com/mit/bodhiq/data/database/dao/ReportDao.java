package com.mit.bodhiq.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.mit.bodhiq.data.database.entity.Report;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

/**
 * Data Access Object for Report entity operations.
 * Provides report management operations with RxJava3 return types.
 */
@Dao
public interface ReportDao {
    
    /**
     * Insert a new report into the database.
     * 
     * @param report Report entity to insert
     * @return Single emitting the inserted report's ID
     */
    @Insert
    Single<Long> insertReport(Report report);
    
    /**
     * Update an existing report in the database.
     * 
     * @param report Report entity with updated information
     * @return Completable indicating operation completion
     */
    @Update
    Completable updateReport(Report report);
    
    /**
     * Get a report by ID.
     * 
     * @param reportId Report's ID
     * @return Single emitting the Report if found, error if not found
     */
    @Query("SELECT * FROM reports WHERE id = :reportId")
    Single<Report> getReportById(long reportId);
    
    /**
     * Get report by query ID.
     * 
     * @param queryId Query's ID
     * @return Single emitting the Report if found, error if not found
     */
    @Query("SELECT * FROM reports WHERE query_id = :queryId LIMIT 1")
    Single<Report> getReportByQueryId(long queryId);
    
    /**
     * Get all reports in the system.
     * 
     * @return Flowable emitting list of all reports, ordered by creation date
     */
    @Query("SELECT * FROM reports ORDER BY created_at DESC")
    Flowable<List<Report>> getAllReports();
    
    /**
     * Get reports by generation status.
     * 
     * @param status Report generation status (PENDING, GENERATING, COMPLETED, FAILED)
     * @return Flowable emitting list of reports with specified status
     */
    @Query("SELECT * FROM reports WHERE generation_status = :status ORDER BY created_at DESC")
    Flowable<List<Report>> getReportsByStatus(String status);
    
    /**
     * Get reports by molecule name.
     * 
     * @param molecule Molecule name to filter by
     * @return Flowable emitting list of reports for the specified molecule
     */
    @Query("SELECT * FROM reports WHERE molecule = :molecule ORDER BY created_at DESC")
    Flowable<List<Report>> getReportsByMolecule(String molecule);
    
    /**
     * Get completed reports only.
     * 
     * @return Flowable emitting list of completed reports
     */
    @Query("SELECT * FROM reports WHERE generation_status = 'COMPLETED' ORDER BY created_at DESC")
    Flowable<List<Report>> getCompletedReports();
    
    /**
     * Get reports sorted by file size.
     * 
     * @param ascending True for ascending order, false for descending
     * @return Flowable emitting list of reports sorted by file size
     */
    @Query("SELECT * FROM reports WHERE generation_status = 'COMPLETED' ORDER BY file_size_bytes ASC")
    Flowable<List<Report>> getReportsSortedBySizeAsc();
    
    @Query("SELECT * FROM reports WHERE generation_status = 'COMPLETED' ORDER BY file_size_bytes DESC")
    Flowable<List<Report>> getReportsSortedBySizeDesc();
    
    /**
     * Get reports sorted by creation date.
     * 
     * @return Flowable emitting list of reports sorted by creation date (newest first)
     */
    @Query("SELECT * FROM reports ORDER BY created_at DESC")
    Flowable<List<Report>> getReportsSortedByDateDesc();
    
    @Query("SELECT * FROM reports ORDER BY created_at ASC")
    Flowable<List<Report>> getReportsSortedByDateAsc();
    
    /**
     * Search reports by molecule name or file name.
     * 
     * @param searchTerm Search term to match against molecule or file name
     * @return Flowable emitting list of matching reports
     */
    @Query("SELECT * FROM reports WHERE molecule LIKE '%' || :searchTerm || '%' OR file_name LIKE '%' || :searchTerm || '%' ORDER BY created_at DESC")
    Flowable<List<Report>> searchReports(String searchTerm);
    
    /**
     * Get total count of reports in the system.
     * 
     * @return Single emitting the total report count
     */
    @Query("SELECT COUNT(*) FROM reports")
    Single<Integer> getReportCount();
    
    /**
     * Get count of completed reports.
     * 
     * @return Single emitting count of completed reports
     */
    @Query("SELECT COUNT(*) FROM reports WHERE generation_status = 'COMPLETED'")
    Single<Integer> getCompletedReportCount();
    
    /**
     * Get count of reports by status.
     * 
     * @param status Report generation status
     * @return Single emitting count of reports with specified status
     */
    @Query("SELECT COUNT(*) FROM reports WHERE generation_status = :status")
    Single<Integer> getReportCountByStatus(String status);
    
    /**
     * Get total file size of all completed reports.
     * 
     * @return Single emitting total file size in bytes
     */
    @Query("SELECT SUM(file_size_bytes) FROM reports WHERE generation_status = 'COMPLETED'")
    Single<Long> getTotalReportFileSize();
    
    /**
     * Update report generation status.
     * 
     * @param reportId Report's ID
     * @param status New generation status
     * @return Completable indicating operation completion
     */
    @Query("UPDATE reports SET generation_status = :status WHERE id = :reportId")
    Completable updateReportStatus(long reportId, String status);
    
    /**
     * Update report with completion data.
     * 
     * @param reportId Report's ID
     * @param status New status
     * @param filePath File path of generated PDF
     * @param fileSizeBytes File size in bytes
     * @param completedAt Completion timestamp
     * @return Completable indicating operation completion
     */
    @Query("UPDATE reports SET generation_status = :status, file_path = :filePath, file_size_bytes = :fileSizeBytes, completed_at = :completedAt WHERE id = :reportId")
    Completable updateReportWithCompletion(long reportId, String status, String filePath, long fileSizeBytes, long completedAt);
    
    /**
     * Update report with error information.
     * 
     * @param reportId Report's ID
     * @param status New status (typically 'FAILED')
     * @param errorMessage Error message
     * @param completedAt Completion timestamp
     * @return Completable indicating operation completion
     */
    @Query("UPDATE reports SET generation_status = :status, error_message = :errorMessage, completed_at = :completedAt WHERE id = :reportId")
    Completable updateReportWithError(long reportId, String status, String errorMessage, long completedAt);
    
    /**
     * Update report indication tags.
     * 
     * @param reportId Report's ID
     * @param indicationTags JSON string of indication tags
     * @return Completable indicating operation completion
     */
    @Query("UPDATE reports SET indication_tags = :indicationTags WHERE id = :reportId")
    Completable updateReportIndicationTags(long reportId, String indicationTags);
    
    /**
     * Delete a report by ID.
     * 
     * @param reportId ID of report to delete
     * @return Completable indicating operation completion
     */
    @Query("DELETE FROM reports WHERE id = :reportId")
    Completable deleteReportById(long reportId);
    
    /**
     * Delete report by query ID.
     * 
     * @param queryId Query's ID
     * @return Completable indicating operation completion
     */
    @Query("DELETE FROM reports WHERE query_id = :queryId")
    Completable deleteReportByQueryId(long queryId);
    
    /**
     * Check if a report exists for a query.
     * 
     * @param queryId Query's ID
     * @return Single emitting true if report exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM reports WHERE query_id = :queryId)")
    Single<Boolean> reportExistsForQuery(long queryId);
}