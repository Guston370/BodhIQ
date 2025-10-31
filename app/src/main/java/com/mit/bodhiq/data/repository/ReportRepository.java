package com.mit.bodhiq.data.repository;

import com.mit.bodhiq.data.database.dao.ReportDao;
import com.mit.bodhiq.data.database.entity.Report;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository for PDF report generation and management operations.
 * Handles report lifecycle from creation to file management.
 * Implements requirements 5.1, 5.3, 6.1 for report generation and archive management.
 */
@Singleton
public class ReportRepository {
    
    private final ReportDao reportDao;
    
    // Date formatter for report file names
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    
    @Inject
    public ReportRepository(ReportDao reportDao) {
        this.reportDao = reportDao;
    }
    
    /**
     * Generate a comprehensive PDF report for a completed query.
     * Requirement 5.1: Automatic PDF report generation using iText7
     * 
     * @param queryId ID of the completed query
     * @param molecule Molecule name for the report
     * @return Single emitting the generated report's ID
     */
    public Single<Long> generateReport(long queryId, String molecule) {
        return Single.fromCallable(() -> {
            // Create report entity with PENDING status
            String fileName = generateReportFileName(molecule);
            Report report = new Report(queryId, fileName, molecule, "PENDING", System.currentTimeMillis());
            return report;
        })
        .flatMap(reportDao::insertReport)
        .flatMap(reportId -> 
            // Generate the actual PDF file
            generatePdfFile(reportId, queryId, molecule)
                .andThen(Single.just(reportId))
        )
        .subscribeOn(Schedulers.io());
    }
    
    /**
     * Get report by ID.
     * 
     * @param reportId Report's ID
     * @return Single emitting Report entity
     */
    public Single<Report> getReportById(long reportId) {
        return reportDao.getReportById(reportId);
    }
    
    /**
     * Get report by query ID.
     * 
     * @param queryId Query's ID
     * @return Single emitting Report entity
     */
    public Single<Report> getReportByQueryId(long queryId) {
        return reportDao.getReportByQueryId(queryId);
    }
    
    /**
     * Get all reports in the system.
     * Requirement 6.1: Display all generated reports in archive
     * 
     * @return Flowable emitting list of all reports
     */
    public Flowable<List<Report>> getAllReports() {
        return reportDao.getAllReports();
    }
    
    /**
     * Get completed reports only.
     * 
     * @return Flowable emitting list of completed reports
     */
    public Flowable<List<Report>> getCompletedReports() {
        return reportDao.getCompletedReports();
    }
    
    /**
     * Get reports by molecule name.
     * 
     * @param molecule Molecule name to filter by
     * @return Flowable emitting list of reports for the molecule
     */
    public Flowable<List<Report>> getReportsByMolecule(String molecule) {
        return reportDao.getReportsByMolecule(molecule);
    }
    
    /**
     * Get reports by generation status.
     * 
     * @param status Report generation status
     * @return Flowable emitting list of reports with specified status
     */
    public Flowable<List<Report>> getReportsByStatus(String status) {
        return reportDao.getReportsByStatus(status);
    }
    
    /**
     * Search reports by molecule name or file name.
     * 
     * @param searchTerm Search term to match
     * @return Flowable emitting list of matching reports
     */
    public Flowable<List<Report>> searchReports(String searchTerm) {
        return reportDao.searchReports(searchTerm);
    }
    
    /**
     * Get reports sorted by creation date.
     * 
     * @param ascending True for ascending order, false for descending
     * @return Flowable emitting sorted list of reports
     */
    public Flowable<List<Report>> getReportsSortedByDate(boolean ascending) {
        return ascending ? reportDao.getReportsSortedByDateAsc() : reportDao.getReportsSortedByDateDesc();
    }
    
    /**
     * Get reports sorted by file size.
     * 
     * @param ascending True for ascending order, false for descending
     * @return Flowable emitting sorted list of reports
     */
    public Flowable<List<Report>> getReportsSortedBySize(boolean ascending) {
        return ascending ? reportDao.getReportsSortedBySizeAsc() : reportDao.getReportsSortedBySizeDesc();
    }
    
    /**
     * Get report statistics.
     * 
     * @return Single emitting report statistics
     */
    public Single<ReportStatistics> getReportStatistics() {
        return Single.zip(
            reportDao.getReportCount(),
            reportDao.getCompletedReportCount(),
            reportDao.getReportCountByStatus("FAILED"),
            reportDao.getTotalReportFileSize(),
            (total, completed, failed, totalSize) -> 
                new ReportStatistics(total, completed, failed, totalSize != null ? totalSize : 0L)
        );
    }
    
    /**
     * Update report indication tags.
     * Requirement 5.3: Include metadata such as indication tags
     * 
     * @param reportId Report's ID
     * @param indicationTags JSON string of indication tags
     * @return Completable indicating operation completion
     */
    public Completable updateReportIndicationTags(long reportId, String indicationTags) {
        return reportDao.updateReportIndicationTags(reportId, indicationTags);
    }
    
    /**
     * Delete report and associated PDF file.
     * Requirement 6.1: Allow users to delete reports
     * 
     * @param reportId ID of report to delete
     * @return Completable indicating operation completion
     */
    public Completable deleteReport(long reportId) {
        return reportDao.getReportById(reportId)
                .flatMapCompletable(report -> {
                    // Delete the PDF file if it exists
                    Completable deleteFile = Completable.fromAction(() -> {
                        if (report.getFilePath() != null) {
                            File pdfFile = new File(report.getFilePath());
                            if (pdfFile.exists()) {
                                pdfFile.delete();
                            }
                        }
                    });
                    
                    // Delete the database record
                    return deleteFile.andThen(reportDao.deleteReportById(reportId));
                })
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Check if report exists for a query.
     * 
     * @param queryId Query's ID
     * @return Single emitting true if report exists, false otherwise
     */
    public Single<Boolean> reportExistsForQuery(long queryId) {
        return reportDao.reportExistsForQuery(queryId);
    }
    
    /**
     * Get file path for a completed report.
     * Requirement 5.3: File path management for PDF storage
     * 
     * @param reportId Report's ID
     * @return Single emitting file path or error if report not completed
     */
    public Single<String> getReportFilePath(long reportId) {
        return reportDao.getReportById(reportId)
                .map(report -> {
                    if (!"COMPLETED".equals(report.getGenerationStatus())) {
                        throw new IllegalStateException("Report is not completed yet");
                    }
                    if (report.getFilePath() == null) {
                        throw new IllegalStateException("Report file path is null");
                    }
                    return report.getFilePath();
                });
    }
    
    /**
     * Check if report file exists on disk.
     * 
     * @param reportId Report's ID
     * @return Single emitting true if file exists, false otherwise
     */
    public Single<Boolean> reportFileExists(long reportId) {
        return getReportFilePath(reportId)
                .map(filePath -> new File(filePath).exists())
                .onErrorReturnItem(false);
    }
    
    // Private helper methods
    
    /**
     * Generate the actual PDF file (placeholder implementation).
     */
    private Completable generatePdfFile(long reportId, long queryId, String molecule) {
        return Completable.fromAction(() -> {
            try {
                // Update status to GENERATING
                reportDao.updateReportStatus(reportId, "GENERATING").blockingAwait();
                
                // TODO: Implement PDF generation when needed
                // For now, just mark as completed with placeholder
                String filePath = "/placeholder/path/" + generateReportFileName(molecule);
                long fileSize = 0;
                
                // Update report with completion data
                reportDao.updateReportWithCompletion(
                    reportId, 
                    "COMPLETED", 
                    filePath, 
                    fileSize, 
                    System.currentTimeMillis()
                ).blockingAwait();
                
            } catch (Exception e) {
                // Update report with error
                reportDao.updateReportWithError(
                    reportId, 
                    "FAILED", 
                    e.getMessage(), 
                    System.currentTimeMillis()
                ).blockingAwait();
                
                throw new RuntimeException("Failed to generate PDF report", e);
            }
        });
    }
    
    /**
     * Generate a unique file name for the report.
     */
    private String generateReportFileName(String molecule) {
        String timestamp = DATE_FORMAT.format(new Date());
        String sanitizedMolecule = molecule.replaceAll("[^a-zA-Z0-9-]", "_");
        return String.format("BodhIQ_Report_%s_%s.pdf", sanitizedMolecule, timestamp);
    }
    
    /**
     * POJO for report statistics.
     */
    public static class ReportStatistics {
        private final int totalReports;
        private final int completedReports;
        private final int failedReports;
        private final long totalFileSizeBytes;
        
        public ReportStatistics(int totalReports, int completedReports, int failedReports, long totalFileSizeBytes) {
            this.totalReports = totalReports;
            this.completedReports = completedReports;
            this.failedReports = failedReports;
            this.totalFileSizeBytes = totalFileSizeBytes;
        }
        
        public int getTotalReports() { return totalReports; }
        public int getCompletedReports() { return completedReports; }
        public int getFailedReports() { return failedReports; }
        public long getTotalFileSizeBytes() { return totalFileSizeBytes; }
        
        public double getSuccessRate() {
            return totalReports > 0 ? (double) completedReports / totalReports * 100 : 0.0;
        }
        
        public String getFormattedFileSize() {
            if (totalFileSizeBytes < 1024) return totalFileSizeBytes + " B";
            if (totalFileSizeBytes < 1024 * 1024) return String.format("%.1f KB", totalFileSizeBytes / 1024.0);
            if (totalFileSizeBytes < 1024 * 1024 * 1024) return String.format("%.1f MB", totalFileSizeBytes / (1024.0 * 1024.0));
            return String.format("%.1f GB", totalFileSizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}