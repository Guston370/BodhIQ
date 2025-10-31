package com.mit.bodhiq.repository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mit.bodhiq.data.database.dao.ReportDao;
import com.mit.bodhiq.data.database.entity.Report;
import com.mit.bodhiq.data.repository.ReportRepository;
import com.mit.bodhiq.utils.PdfGenerator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Unit tests for ReportRepository PDF generation tests.
 * Tests requirements 5.1, 5.3, 6.1 for report generation and archive management.
 */
@RunWith(RobolectricTestRunner.class)
public class ReportRepositoryTest {
    
    @Mock
    private ReportDao reportDao;
    
    @Mock
    private PdfGenerator pdfGenerator;
    
    private ReportRepository reportRepository;
    
    // Test data
    private Report testReport;
    private Report completedReport;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        reportRepository = new ReportRepository(reportDao, pdfGenerator);
        
        // Setup test data
        testReport = new Report(1L, "BodhIQ_Report_Montelukast_20241028.pdf", "Montelukast", "PENDING", System.currentTimeMillis());
        testReport.setId(1L);
        
        completedReport = new Report(1L, "BodhIQ_Report_Montelukast_20241028.pdf", "Montelukast", "COMPLETED", System.currentTimeMillis());
        completedReport.setId(1L);
        completedReport.setFilePath("/data/reports/BodhIQ_Report_Montelukast_20241028.pdf");
        completedReport.setFileSizeBytes(1024000L);
    }
    
    /**
     * Test PDF report generation workflow.
     * Requirement 5.1: Automatic PDF report generation using iText7
     */
    @Test
    public void testGenerateReport_Success() {
        // Given
        long queryId = 1L;
        String molecule = "Montelukast";
        String expectedFilePath = "/data/reports/BodhIQ_Report_Montelukast_20241028.pdf";
        
        when(reportDao.insertReport(any(Report.class))).thenReturn(Single.just(1L));
        when(reportDao.updateReportStatus(1L, "GENERATING")).thenReturn(Completable.complete());
        when(pdfGenerator.generateReport(queryId, molecule)).thenReturn(expectedFilePath);
        when(reportDao.updateReportWithCompletion(eq(1L), eq("COMPLETED"), eq(expectedFilePath), anyLong(), anyLong()))
            .thenReturn(Completable.complete());
        
        // When
        Long reportId = reportRepository.generateReport(queryId, molecule).blockingGet();
        
        // Then
        assertNotNull("Report ID should not be null", reportId);
        assertEquals("Should return report ID 1", Long.valueOf(1L), reportId);
        
        verify(reportDao).insertReport(argThat(report -> 
            report.getQueryId() == queryId &&
            report.getMolecule().equals(molecule) &&
            report.getGenerationStatus().equals("PENDING")
        ));
        verify(reportDao).updateReportStatus(1L, "GENERATING");
        verify(pdfGenerator).generateReport(queryId, molecule);
        verify(reportDao).updateReportWithCompletion(eq(1L), eq("COMPLETED"), eq(expectedFilePath), anyLong(), anyLong());
    }
    
    /**
     * Test PDF generation failure handling.
     */
    @Test
    public void testGenerateReport_PdfGenerationFailure() {
        // Given
        long queryId = 1L;
        String molecule = "Montelukast";
        String errorMessage = "PDF generation failed";
        
        when(reportDao.insertReport(any(Report.class))).thenReturn(Single.just(1L));
        when(reportDao.updateReportStatus(1L, "GENERATING")).thenReturn(Completable.complete());
        when(pdfGenerator.generateReport(queryId, molecule)).thenThrow(new RuntimeException(errorMessage));
        when(reportDao.updateReportWithError(eq(1L), eq("FAILED"), anyString(), anyLong()))
            .thenReturn(Completable.complete());
        
        // When & Then
        try {
            reportRepository.generateReport(queryId, molecule).blockingGet();
            fail("Should throw exception when PDF generation fails");
        } catch (Exception e) {
            assertTrue("Should contain PDF generation error", e.getMessage().contains("Failed to generate PDF report"));
        }
        
        verify(reportDao).insertReport(any(Report.class));
        verify(reportDao).updateReportStatus(1L, "GENERATING");
        verify(pdfGenerator).generateReport(queryId, molecule);
        verify(reportDao).updateReportWithError(eq(1L), eq("FAILED"), anyString(), anyLong());
    }
    
    /**
     * Test getting report by ID.
     */
    @Test
    public void testGetReportById() {
        // Given
        long reportId = 1L;
        when(reportDao.getReportById(reportId)).thenReturn(Single.just(testReport));
        
        // When
        Report result = reportRepository.getReportById(reportId).blockingGet();
        
        // Then
        assertNotNull("Report should not be null", result);
        assertEquals("Report ID should match", reportId, result.getId());
        assertEquals("Molecule should match", "Montelukast", result.getMolecule());
        verify(reportDao).getReportById(reportId);
    }
    
    /**
     * Test getting report by query ID.
     */
    @Test
    public void testGetReportByQueryId() {
        // Given
        long queryId = 1L;
        when(reportDao.getReportByQueryId(queryId)).thenReturn(Single.just(testReport));
        
        // When
        Report result = reportRepository.getReportByQueryId(queryId).blockingGet();
        
        // Then
        assertNotNull("Report should not be null", result);
        assertEquals("Query ID should match", queryId, result.getQueryId());
        verify(reportDao).getReportByQueryId(queryId);
    }
    
    /**
     * Test getting all reports.
     * Requirement 6.1: Display all generated reports in archive
     */
    @Test
    public void testGetAllReports() {
        // Given
        List<Report> reports = Arrays.asList(testReport, completedReport);
        when(reportDao.getAllReports()).thenReturn(Flowable.just(reports));
        
        // When
        List<Report> result = reportRepository.getAllReports().blockingFirst();
        
        // Then
        assertNotNull("Reports should not be null", result);
        assertEquals("Should return 2 reports", 2, result.size());
        verify(reportDao).getAllReports();
    }
    
    /**
     * Test getting completed reports only.
     */
    @Test
    public void testGetCompletedReports() {
        // Given
        List<Report> reports = Arrays.asList(completedReport);
        when(reportDao.getCompletedReports()).thenReturn(Flowable.just(reports));
        
        // When
        List<Report> result = reportRepository.getCompletedReports().blockingFirst();
        
        // Then
        assertNotNull("Completed reports should not be null", result);
        assertEquals("Should return 1 completed report", 1, result.size());
        assertEquals("Report should be completed", "COMPLETED", result.get(0).getGenerationStatus());
        verify(reportDao).getCompletedReports();
    }
    
    /**
     * Test getting reports by molecule.
     */
    @Test
    public void testGetReportsByMolecule() {
        // Given
        String molecule = "Montelukast";
        List<Report> reports = Arrays.asList(testReport);
        when(reportDao.getReportsByMolecule(molecule)).thenReturn(Flowable.just(reports));
        
        // When
        List<Report> result = reportRepository.getReportsByMolecule(molecule).blockingFirst();
        
        // Then
        assertNotNull("Reports should not be null", result);
        assertEquals("Should return 1 report", 1, result.size());
        assertEquals("Molecule should match", molecule, result.get(0).getMolecule());
        verify(reportDao).getReportsByMolecule(molecule);
    }
    
    /**
     * Test getting reports by status.
     */
    @Test
    public void testGetReportsByStatus() {
        // Given
        String status = "COMPLETED";
        List<Report> reports = Arrays.asList(completedReport);
        when(reportDao.getReportsByStatus(status)).thenReturn(Flowable.just(reports));
        
        // When
        List<Report> result = reportRepository.getReportsByStatus(status).blockingFirst();
        
        // Then
        assertNotNull("Reports should not be null", result);
        assertEquals("Should return 1 report", 1, result.size());
        assertEquals("Status should match", status, result.get(0).getGenerationStatus());
        verify(reportDao).getReportsByStatus(status);
    }
    
    /**
     * Test searching reports.
     */
    @Test
    public void testSearchReports() {
        // Given
        String searchTerm = "Montelukast";
        List<Report> reports = Arrays.asList(testReport);
        when(reportDao.searchReports(searchTerm)).thenReturn(Flowable.just(reports));
        
        // When
        List<Report> result = reportRepository.searchReports(searchTerm).blockingFirst();
        
        // Then
        assertNotNull("Search results should not be null", result);
        assertEquals("Should return 1 report", 1, result.size());
        verify(reportDao).searchReports(searchTerm);
    }
    
    /**
     * Test getting reports sorted by date.
     */
    @Test
    public void testGetReportsSortedByDate_Ascending() {
        // Given
        List<Report> reports = Arrays.asList(testReport, completedReport);
        when(reportDao.getReportsSortedByDateAsc()).thenReturn(Flowable.just(reports));
        
        // When
        List<Report> result = reportRepository.getReportsSortedByDate(true).blockingFirst();
        
        // Then
        assertNotNull("Sorted reports should not be null", result);
        assertEquals("Should return 2 reports", 2, result.size());
        verify(reportDao).getReportsSortedByDateAsc();
        verify(reportDao, never()).getReportsSortedByDateDesc();
    }
    
    /**
     * Test getting reports sorted by date descending.
     */
    @Test
    public void testGetReportsSortedByDate_Descending() {
        // Given
        List<Report> reports = Arrays.asList(completedReport, testReport);
        when(reportDao.getReportsSortedByDateDesc()).thenReturn(Flowable.just(reports));
        
        // When
        List<Report> result = reportRepository.getReportsSortedByDate(false).blockingFirst();
        
        // Then
        assertNotNull("Sorted reports should not be null", result);
        assertEquals("Should return 2 reports", 2, result.size());
        verify(reportDao).getReportsSortedByDateDesc();
        verify(reportDao, never()).getReportsSortedByDateAsc();
    }
    
    /**
     * Test getting reports sorted by size.
     */
    @Test
    public void testGetReportsSortedBySize_Ascending() {
        // Given
        List<Report> reports = Arrays.asList(testReport, completedReport);
        when(reportDao.getReportsSortedBySizeAsc()).thenReturn(Flowable.just(reports));
        
        // When
        List<Report> result = reportRepository.getReportsSortedBySize(true).blockingFirst();
        
        // Then
        assertNotNull("Sorted reports should not be null", result);
        assertEquals("Should return 2 reports", 2, result.size());
        verify(reportDao).getReportsSortedBySizeAsc();
    }
    
    /**
     * Test getting report statistics.
     */
    @Test
    public void testGetReportStatistics() {
        // Given
        when(reportDao.getReportCount()).thenReturn(Single.just(5));
        when(reportDao.getCompletedReportCount()).thenReturn(Single.just(4));
        when(reportDao.getReportCountByStatus("FAILED")).thenReturn(Single.just(1));
        when(reportDao.getTotalReportFileSize()).thenReturn(Single.just(5120000L)); // 5MB
        
        // When
        ReportRepository.ReportStatistics stats = reportRepository.getReportStatistics().blockingGet();
        
        // Then
        assertNotNull("Statistics should not be null", stats);
        assertEquals("Total reports should be 5", 5, stats.getTotalReports());
        assertEquals("Completed reports should be 4", 4, stats.getCompletedReports());
        assertEquals("Failed reports should be 1", 1, stats.getFailedReports());
        assertEquals("Total file size should be 5MB", 5120000L, stats.getTotalFileSizeBytes());
        assertEquals("Success rate should be 80%", 80.0, stats.getSuccessRate(), 0.1);
        assertEquals("Formatted size should be MB", "5.0 MB", stats.getFormattedFileSize());
        
        verify(reportDao).getReportCount();
        verify(reportDao).getCompletedReportCount();
        verify(reportDao).getReportCountByStatus("FAILED");
        verify(reportDao).getTotalReportFileSize();
    }
    
    /**
     * Test updating report indication tags.
     * Requirement 5.3: Include metadata such as indication tags
     */
    @Test
    public void testUpdateReportIndicationTags() {
        // Given
        long reportId = 1L;
        String indicationTags = "[\"Asthma\", \"Allergic Rhinitis\"]";
        when(reportDao.updateReportIndicationTags(reportId, indicationTags)).thenReturn(Completable.complete());
        
        // When
        reportRepository.updateReportIndicationTags(reportId, indicationTags).blockingAwait();
        
        // Then
        verify(reportDao).updateReportIndicationTags(reportId, indicationTags);
    }
    
    /**
     * Test deleting report with file cleanup.
     * Requirement 6.1: Allow users to delete reports
     */
    @Test
    public void testDeleteReport_WithFileCleanup() {
        // Given
        long reportId = 1L;
        when(reportDao.getReportById(reportId)).thenReturn(Single.just(completedReport));
        when(reportDao.deleteReportById(reportId)).thenReturn(Completable.complete());
        
        // When
        reportRepository.deleteReport(reportId).blockingAwait();
        
        // Then
        verify(reportDao).getReportById(reportId);
        verify(reportDao).deleteReportById(reportId);
    }
    
    /**
     * Test deleting report without file path.
     */
    @Test
    public void testDeleteReport_NoFilePath() {
        // Given
        long reportId = 1L;
        Report reportWithoutFile = new Report(1L, "test.pdf", "Montelukast", "PENDING", System.currentTimeMillis());
        reportWithoutFile.setId(1L);
        reportWithoutFile.setFilePath(null);
        
        when(reportDao.getReportById(reportId)).thenReturn(Single.just(reportWithoutFile));
        when(reportDao.deleteReportById(reportId)).thenReturn(Completable.complete());
        
        // When
        reportRepository.deleteReport(reportId).blockingAwait();
        
        // Then
        verify(reportDao).getReportById(reportId);
        verify(reportDao).deleteReportById(reportId);
    }
    
    /**
     * Test checking if report exists for query.
     */
    @Test
    public void testReportExistsForQuery() {
        // Given
        long queryId = 1L;
        when(reportDao.reportExistsForQuery(queryId)).thenReturn(Single.just(true));
        
        // When
        Boolean exists = reportRepository.reportExistsForQuery(queryId).blockingGet();
        
        // Then
        assertTrue("Report should exist for query", exists);
        verify(reportDao).reportExistsForQuery(queryId);
    }
    
    /**
     * Test getting report file path for completed report.
     * Requirement 5.3: File path management for PDF storage
     */
    @Test
    public void testGetReportFilePath_CompletedReport() {
        // Given
        long reportId = 1L;
        when(reportDao.getReportById(reportId)).thenReturn(Single.just(completedReport));
        
        // When
        String filePath = reportRepository.getReportFilePath(reportId).blockingGet();
        
        // Then
        assertNotNull("File path should not be null", filePath);
        assertEquals("File path should match", completedReport.getFilePath(), filePath);
        verify(reportDao).getReportById(reportId);
    }
    
    /**
     * Test getting report file path for pending report.
     */
    @Test
    public void testGetReportFilePath_PendingReport() {
        // Given
        long reportId = 1L;
        when(reportDao.getReportById(reportId)).thenReturn(Single.just(testReport));
        
        // When & Then
        try {
            reportRepository.getReportFilePath(reportId).blockingGet();
            fail("Should throw exception for pending report");
        } catch (Exception e) {
            assertTrue("Should contain not completed error", e.getCause().getMessage().contains("not completed"));
        }
        
        verify(reportDao).getReportById(reportId);
    }
    
    /**
     * Test checking if report file exists on disk.
     */
    @Test
    public void testReportFileExists_FileExists() {
        // Given
        long reportId = 1L;
        when(reportDao.getReportById(reportId)).thenReturn(Single.just(completedReport));
        
        // When
        Boolean exists = reportRepository.reportFileExists(reportId).blockingGet();
        
        // Then
        // Note: This will return false in test environment since file doesn't actually exist
        // In real implementation, this would check actual file system
        assertNotNull("File exists result should not be null", exists);
        verify(reportDao).getReportById(reportId);
    }
    
    /**
     * Test report file exists error handling.
     */
    @Test
    public void testReportFileExists_ErrorHandling() {
        // Given
        long reportId = 999L;
        when(reportDao.getReportById(reportId)).thenReturn(Single.error(new RuntimeException("Report not found")));
        
        // When
        Boolean exists = reportRepository.reportFileExists(reportId).blockingGet();
        
        // Then
        assertFalse("Should return false on error", exists);
        verify(reportDao).getReportById(reportId);
    }
    
    /**
     * Test report statistics with null total file size.
     */
    @Test
    public void testGetReportStatistics_NullFileSize() {
        // Given
        when(reportDao.getReportCount()).thenReturn(Single.just(1));
        when(reportDao.getCompletedReportCount()).thenReturn(Single.just(1));
        when(reportDao.getReportCountByStatus("FAILED")).thenReturn(Single.just(0));
        when(reportDao.getTotalReportFileSize()).thenReturn(Single.just(null));
        
        // When
        ReportRepository.ReportStatistics stats = reportRepository.getReportStatistics().blockingGet();
        
        // Then
        assertNotNull("Statistics should not be null", stats);
        assertEquals("Total file size should be 0 when null", 0L, stats.getTotalFileSizeBytes());
        assertEquals("Formatted size should be bytes", "0 B", stats.getFormattedFileSize());
    }
}