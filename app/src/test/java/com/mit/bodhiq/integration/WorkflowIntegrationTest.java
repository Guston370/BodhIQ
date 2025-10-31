package com.mit.bodhiq.integration;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mit.bodhiq.data.database.entity.Query;
import com.mit.bodhiq.data.database.entity.Report;
import com.mit.bodhiq.data.database.entity.User;
import com.mit.bodhiq.data.model.AgentStatus;
import com.mit.bodhiq.data.model.AgentUpdate;
import com.mit.bodhiq.data.repository.QueryRepository;
import com.mit.bodhiq.data.repository.ReportRepository;
import com.mit.bodhiq.data.repository.UserRepository;
import com.mit.bodhiq.ui.queryconsole.QueryConsoleViewModel;
import com.mit.bodhiq.ui.dashboard.DashboardViewModel;
import com.mit.bodhiq.ui.reportviewer.ReportViewerViewModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Integration test for the complete BodhIQ workflow.
 * Tests end-to-end functionality from query creation to report generation.
 * Verifies requirements 3.5, 5.5 for complete workflow integration.
 */
@RunWith(RobolectricTestRunner.class)
public class WorkflowIntegrationTest {
    
    @Mock
    private QueryRepository queryRepository;
    
    @Mock
    private ReportRepository reportRepository;
    
    @Mock
    private UserRepository userRepository;
    
    private QueryConsoleViewModel queryConsoleViewModel;
    private DashboardViewModel dashboardViewModel;
    private ReportViewerViewModel reportViewerViewModel;
    
    // Test data
    private User testUser;
    private Query testQuery;
    private Report testReport;
    private List<AgentUpdate> testAgentUpdates;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Initialize test data
        setupTestData();
        
        // Initialize ViewModels
        queryConsoleViewModel = new QueryConsoleViewModel(queryRepository, reportRepository, userRepository);
        dashboardViewModel = new DashboardViewModel(queryRepository, userRepository);
        reportViewerViewModel = new ReportViewerViewModel(reportRepository);
        
        // Setup mock behaviors
        setupMockBehaviors();
    }
    
    private void setupTestData() {
        // Test user
        testUser = new User("test@company.com", "Test User", "analyst", System.currentTimeMillis());
        testUser.setId(1L);
        
        // Test query
        testQuery = new Query(1L, "Analyze Montelukast market data", "Montelukast", "PENDING", System.currentTimeMillis());
        testQuery.setId(1L);
        
        // Test report
        testReport = new Report(1L, "BodhIQ_Report_Montelukast_20241028.pdf", "Montelukast", "COMPLETED", System.currentTimeMillis());
        testReport.setId(1L);
        testReport.setFilePath("/data/reports/BodhIQ_Report_Montelukast_20241028.pdf");
        testReport.setFileSizeBytes(1024000L); // 1MB
        
        // Test agent updates
        testAgentUpdates = Arrays.asList(
            createAgentUpdate("Market Insights", AgentStatus.COMPLETED, 100),
            createAgentUpdate("Patent Landscape", AgentStatus.COMPLETED, 100),
            createAgentUpdate("Clinical Trials", AgentStatus.COMPLETED, 100),
            createAgentUpdate("EXIM Trade", AgentStatus.COMPLETED, 100),
            createAgentUpdate("Web Intelligence", AgentStatus.COMPLETED, 100),
            createAgentUpdate("Internal Insights", AgentStatus.COMPLETED, 100),
            createAgentUpdate("Report Generator", AgentStatus.COMPLETED, 100)
        );
    }
    
    private AgentUpdate createAgentUpdate(String agentName, AgentStatus status, int progress) {
        return new AgentUpdate(agentName, status, progress);
    }
    
    private void setupMockBehaviors() {
        // User repository mocks
        when(userRepository.authenticateUser(anyString())).thenReturn(Single.just(testUser));
        when(userRepository.getUserCount()).thenReturn(Single.just(1));
        
        // Query repository mocks
        when(queryRepository.createQuery(anyString(), anyLong())).thenReturn(Single.just(1L));
        when(queryRepository.getQueryById(1L)).thenReturn(Single.just(testQuery));
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.complete());
        when(queryRepository.getAgentProgress(1L)).thenReturn(Flowable.fromIterable(testAgentUpdates));
        when(queryRepository.getRecentQueries(anyLong(), anyInt())).thenReturn(Flowable.just(Arrays.asList(testQuery)));
        when(queryRepository.getQueryStatistics()).thenReturn(Single.just(new QueryRepository.QueryStatistics(1, 1, 0, 0)));
        
        // Report repository mocks
        when(reportRepository.getReportByQueryId(1L)).thenReturn(Single.just(testReport));
        when(reportRepository.getReportById(1L)).thenReturn(Single.just(testReport));
        when(reportRepository.getAllReports()).thenReturn(Flowable.just(Arrays.asList(testReport)));
        when(reportRepository.getReportStatistics()).thenReturn(Single.just(new ReportRepository.ReportStatistics(1, 1, 0, 1024000L)));
    }
    
    /**
     * Test complete workflow: Query creation -> Agent execution -> Report generation -> Report viewing
     * Requirement 3.5: End-to-end query execution and report generation
     */
    @Test
    public void testCompleteWorkflow() {
        // Step 1: Create and execute query
        queryConsoleViewModel.executeQuery("Analyze Montelukast market data and competitive landscape");
        
        // Verify query creation was called
        verify(queryRepository).createQuery(eq("Analyze Montelukast market data and competitive landscape"), eq(1L));
        
        // Verify agent execution was initiated
        verify(queryRepository).executeAgents(1L);
        
        // Verify progress monitoring was started
        verify(queryRepository).getAgentProgress(1L);
        
        // Step 2: Verify query completion triggers report ID loading
        assertNotNull("Query completed should be observable", queryConsoleViewModel.getQueryCompleted().getValue());
        
        // Step 3: Verify report can be loaded and viewed
        reportViewerViewModel.loadReport(1L);
        verify(reportRepository).getReportById(1L);
        
        // Verify report data is available
        assertNotNull("Report should be loaded", reportViewerViewModel.getReport().getValue());
    }
    
    /**
     * Test dashboard integration with recent queries and statistics.
     * Requirement 2.4, 2.5: Dashboard navigation and data display
     */
    @Test
    public void testDashboardIntegration() {
        // Load dashboard data
        dashboardViewModel.loadDashboardData();
        
        // Verify statistics are loaded
        verify(queryRepository).getQueryStatistics();
        
        // Verify recent queries are loaded
        verify(queryRepository).getRecentQueries(anyLong(), eq(10));
        
        // Verify data is available in ViewModels
        assertNotNull("Statistics should be loaded", dashboardViewModel.getStatistics().getValue());
        assertNotNull("Recent queries should be loaded", dashboardViewModel.getRecentQueries().getValue());
    }
    
    /**
     * Test data persistence across app lifecycle.
     * Requirement 5.5: Verify data persistence and retrieval
     */
    @Test
    public void testDataPersistence() {
        // Simulate app restart by creating new ViewModel instances
        QueryConsoleViewModel newQueryViewModel = new QueryConsoleViewModel(queryRepository, reportRepository, userRepository);
        
        // Load existing query
        newQueryViewModel.loadQuery(1L);
        
        // Verify query data is retrieved from persistence
        verify(queryRepository).getQueryById(1L);
        
        // Verify query data is available
        assertNotNull("Query should be loaded from persistence", newQueryViewModel.getCurrentQuery().getValue());
        
        // Test report persistence
        ReportViewerViewModel newReportViewModel = new ReportViewerViewModel(reportRepository);
        newReportViewModel.loadReport(1L);
        
        // Verify report data is retrieved from persistence
        verify(reportRepository, times(2)).getReportById(1L); // Called twice now
        
        // Verify report data is available
        assertNotNull("Report should be loaded from persistence", newReportViewModel.getReport().getValue());
    }
    
    /**
     * Test error handling in the complete workflow.
     */
    @Test
    public void testErrorHandling() {
        // Setup error scenario
        when(queryRepository.createQuery(anyString(), anyLong())).thenReturn(Single.error(new RuntimeException("Database error")));
        
        // Attempt to execute query
        queryConsoleViewModel.executeQuery("Test query");
        
        // Verify error is handled gracefully
        assertNotNull("Error message should be set", queryConsoleViewModel.getErrorMessage().getValue());
        assertTrue("Loading should be stopped on error", !queryConsoleViewModel.getIsLoading().getValue());
    }
    
    /**
     * Test agent progress tracking functionality.
     * Requirement 3.4: Real-time progress tracking
     */
    @Test
    public void testAgentProgressTracking() {
        // Setup progress tracking
        when(queryRepository.getAgentProgress(1L)).thenReturn(Flowable.fromIterable(testAgentUpdates));
        
        // Load query and monitor progress
        queryConsoleViewModel.loadQuery(1L);
        
        // Verify progress updates are received
        verify(queryRepository).getAgentProgress(1L);
        
        // Verify agent updates are available
        assertNotNull("Agent updates should be available", queryConsoleViewModel.getAgentUpdates().getValue());
        assertFalse("Agent updates should not be empty", queryConsoleViewModel.getAgentUpdates().getValue().isEmpty());
    }
    
    /**
     * Test report generation integration.
     * Requirement 5.1: Automatic PDF report generation
     */
    @Test
    public void testReportGeneration() {
        // Execute query that should trigger report generation
        queryConsoleViewModel.executeQuery("Analyze Montelukast market data");
        
        // Verify query execution completes
        verify(queryRepository).executeAgents(1L);
        
        // Verify report ID is loaded after completion
        verify(reportRepository).getReportByQueryId(1L);
        
        // Verify generated report ID is available
        assertNotNull("Generated report ID should be available", queryConsoleViewModel.getGeneratedReportId().getValue());
    }
    
    /**
     * Test navigation integration between components.
     * Requirement 2.4: Navigation between activities with proper data passing
     */
    @Test
    public void testNavigationIntegration() {
        // Test query navigation from dashboard
        dashboardViewModel.loadDashboardData();
        
        // Verify recent queries are loaded for navigation
        verify(queryRepository).getRecentQueries(anyLong(), eq(10));
        
        // Simulate navigation to query console with existing query
        queryConsoleViewModel.loadQuery(1L);
        
        // Verify query is loaded for editing/viewing
        verify(queryRepository).getQueryById(1L);
        
        // Simulate navigation to report viewer
        reportViewerViewModel.loadReport(1L);
        
        // Verify report is loaded for viewing
        verify(reportRepository).getReportById(1L);
    }
}