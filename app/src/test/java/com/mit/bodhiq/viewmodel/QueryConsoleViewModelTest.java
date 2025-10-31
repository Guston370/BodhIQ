package com.mit.bodhiq.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.database.entity.Query;
import com.mit.bodhiq.data.database.entity.Report;
import com.mit.bodhiq.data.model.AgentStatus;
import com.mit.bodhiq.data.model.AgentUpdate;
import com.mit.bodhiq.data.repository.QueryRepository;
import com.mit.bodhiq.data.repository.ReportRepository;
import com.mit.bodhiq.data.repository.UserRepository;
import com.mit.bodhiq.ui.queryconsole.QueryConsoleViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Unit tests for QueryConsoleViewModel LiveData updates and error handling.
 * Tests requirements 3.2, 3.4, 4.1, 4.2, 8.5 for query execution and result visualization.
 */
@RunWith(RobolectricTestRunner.class)
public class QueryConsoleViewModelTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    @Mock
    private QueryRepository queryRepository;
    
    @Mock
    private ReportRepository reportRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private Observer<Boolean> loadingObserver;
    
    @Mock
    private Observer<String> loadingTextObserver;
    
    @Mock
    private Observer<List<AgentUpdate>> agentUpdatesObserver;
    
    @Mock
    private Observer<Boolean> queryCompletedObserver;
    
    @Mock
    private Observer<String> errorObserver;
    
    @Mock
    private Observer<Query> currentQueryObserver;
    
    @Mock
    private Observer<Long> generatedReportIdObserver;
    
    private QueryConsoleViewModel viewModel;
    
    // Test data
    private Query testQuery;
    private Report testReport;
    private List<AgentUpdate> testAgentUpdates;
    private List<AgentResult> testAgentResults;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up RxJava to use synchronous schedulers for testing
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
        
        viewModel = new QueryConsoleViewModel(queryRepository, reportRepository, userRepository);
        
        // Setup test data
        testQuery = new Query(1L, "Analyze Montelukast market data", "Montelukast", "PENDING", System.currentTimeMillis());
        testQuery.setId(1L);
        
        testReport = new Report(1L, "BodhIQ_Report_Montelukast_20241028.pdf", "Montelukast", "COMPLETED", System.currentTimeMillis());
        testReport.setId(1L);
        
        testAgentUpdates = Arrays.asList(
            new AgentUpdate("Market Insights", AgentStatus.COMPLETED, 100),
            new AgentUpdate("Patent Landscape", AgentStatus.PROCESSING, 50),
            new AgentUpdate("Clinical Trials", AgentStatus.PENDING, 0)
        );
        
        testAgentResults = Arrays.asList(
            new AgentResult(1L, "Market Insights", "COMPLETED", System.currentTimeMillis()),
            new AgentResult(1L, "Patent Landscape", "COMPLETED", System.currentTimeMillis())
        );
        
        // Observe LiveData
        viewModel.getIsLoading().observeForever(loadingObserver);
        viewModel.getLoadingText().observeForever(loadingTextObserver);
        viewModel.getAgentUpdates().observeForever(agentUpdatesObserver);
        viewModel.getQueryCompleted().observeForever(queryCompletedObserver);
        viewModel.getErrorMessage().observeForever(errorObserver);
        viewModel.getCurrentQuery().observeForever(currentQueryObserver);
        viewModel.getGeneratedReportId().observeForever(generatedReportIdObserver);
    }
    
    /**
     * Test successful query execution workflow.
     * Requirement 3.2: Query execution workflow using QueryRepository
     */
    @Test
    public void testExecuteQuery_Success() {
        // Given
        String queryText = "Analyze Montelukast market trends";
        when(queryRepository.createQuery(queryText, 1L)).thenReturn(Single.just(1L));
        when(queryRepository.getQueryById(1L)).thenReturn(Single.just(testQuery));
        when(queryRepository.getAgentProgress(1L)).thenReturn(Flowable.fromIterable(testAgentUpdates));
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.complete());
        when(reportRepository.getReportByQueryId(1L)).thenReturn(Single.just(testReport));
        
        // When
        viewModel.executeQuery(queryText);
        
        // Then
        verify(loadingObserver).onChanged(true);  // Loading started
        verify(loadingTextObserver).onChanged("Creating query...");
        verify(loadingObserver).onChanged(false); // Loading finished
        verify(queryCompletedObserver).onChanged(true);
        verify(generatedReportIdObserver).onChanged(1L);
        
        verify(queryRepository).createQuery(queryText, 1L);
        verify(queryRepository).getQueryById(1L);
        verify(queryRepository).executeAgents(1L);
        verify(queryRepository).getAgentProgress(1L);
        verify(reportRepository).getReportByQueryId(1L);
    }
    
    /**
     * Test query creation failure.
     */
    @Test
    public void testExecuteQuery_CreationFailure() {
        // Given
        String queryText = "Analyze unknown drug";
        String errorMessage = "No supported molecule found";
        when(queryRepository.createQuery(queryText, 1L))
            .thenReturn(Single.error(new RuntimeException(errorMessage)));
        
        // When
        viewModel.executeQuery(queryText);
        
        // Then
        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(errorObserver).onChanged(argThat(error -> 
            error != null && error.contains("Failed to create query") && error.contains(errorMessage)
        ));
        
        verify(queryRepository).createQuery(queryText, 1L);
        verify(queryRepository, never()).executeAgents(anyLong());
    }
    
    /**
     * Test agent execution failure.
     */
    @Test
    public void testExecuteQuery_AgentExecutionFailure() {
        // Given
        String queryText = "Analyze Montelukast market trends";
        String errorMessage = "Agent execution failed";
        when(queryRepository.createQuery(queryText, 1L)).thenReturn(Single.just(1L));
        when(queryRepository.getQueryById(1L)).thenReturn(Single.just(testQuery));
        when(queryRepository.getAgentProgress(1L)).thenReturn(Flowable.fromIterable(testAgentUpdates));
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.error(new RuntimeException(errorMessage)));
        
        // When
        viewModel.executeQuery(queryText);
        
        // Then
        verify(loadingObserver).onChanged(true);
        verify(loadingObserver).onChanged(false);
        verify(errorObserver).onChanged(argThat(error -> 
            error != null && error.contains("Agent execution failed") && error.contains(errorMessage)
        ));
        
        verify(queryRepository).executeAgents(1L);
    }
    
    /**
     * Test loading existing query.
     */
    @Test
    public void testLoadQuery_CompletedQuery() {
        // Given
        long queryId = 1L;
        Query completedQuery = new Query(1L, "Test query", "Montelukast", "COMPLETED", System.currentTimeMillis());
        completedQuery.setId(queryId);
        
        when(queryRepository.getQueryById(queryId)).thenReturn(Single.just(completedQuery));
        when(queryRepository.getAgentResults(queryId)).thenReturn(Flowable.just(testAgentResults));
        
        // When
        viewModel.loadQuery(queryId);
        
        // Then
        verify(currentQueryObserver).onChanged(completedQuery);
        verify(agentUpdatesObserver).onChanged(argThat(updates -> 
            updates != null && updates.size() == 2 && 
            updates.stream().allMatch(update -> update.getStatus() == AgentStatus.COMPLETED)
        ));
        
        verify(queryRepository).getQueryById(queryId);
        verify(queryRepository).getAgentResults(queryId);
    }
    
    /**
     * Test loading processing query.
     */
    @Test
    public void testLoadQuery_ProcessingQuery() {
        // Given
        long queryId = 1L;
        Query processingQuery = new Query(1L, "Test query", "Montelukast", "PROCESSING", System.currentTimeMillis());
        processingQuery.setId(queryId);
        
        when(queryRepository.getQueryById(queryId)).thenReturn(Single.just(processingQuery));
        when(queryRepository.getAgentProgress(queryId)).thenReturn(Flowable.fromIterable(testAgentUpdates));
        
        // When
        viewModel.loadQuery(queryId);
        
        // Then
        verify(currentQueryObserver).onChanged(processingQuery);
        verify(queryRepository).getQueryById(queryId);
        verify(queryRepository).getAgentProgress(queryId);
    }
    
    /**
     * Test real-time agent progress updates.
     * Requirement 3.4: Real-time progress tracking with agent updates
     */
    @Test
    public void testAgentProgressUpdates() {
        // Given
        String queryText = "Analyze Montelukast market trends";
        when(queryRepository.createQuery(queryText, 1L)).thenReturn(Single.just(1L));
        when(queryRepository.getQueryById(1L)).thenReturn(Single.just(testQuery));
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.complete());
        when(queryRepository.getAgentProgress(1L)).thenReturn(Flowable.fromIterable(testAgentUpdates));
        when(reportRepository.getReportByQueryId(1L)).thenReturn(Single.just(testReport));
        
        // When
        viewModel.executeQuery(queryText);
        
        // Then
        verify(agentUpdatesObserver, atLeastOnce()).onChanged(argThat(updates -> 
            updates != null && !updates.isEmpty()
        ));
        verify(loadingTextObserver).onChanged(argThat(text -> 
            text != null && text.contains("Market Insights")
        ));
    }
    
    /**
     * Test progress monitoring failure.
     */
    @Test
    public void testAgentProgressUpdates_MonitoringFailure() {
        // Given
        String queryText = "Analyze Montelukast market trends";
        String errorMessage = "Progress monitoring failed";
        when(queryRepository.createQuery(queryText, 1L)).thenReturn(Single.just(1L));
        when(queryRepository.getQueryById(1L)).thenReturn(Single.just(testQuery));
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.complete());
        when(queryRepository.getAgentProgress(1L))
            .thenReturn(Flowable.error(new RuntimeException(errorMessage)));
        
        // When
        viewModel.executeQuery(queryText);
        
        // Then
        verify(loadingObserver).onChanged(false);
        verify(errorObserver).onChanged(argThat(error -> 
            error != null && error.contains("Progress monitoring failed") && error.contains(errorMessage)
        ));
    }
    
    /**
     * Test retry execution functionality.
     */
    @Test
    public void testRetryExecution() {
        // Given - First execute a query
        String queryText = "Analyze Montelukast market trends";
        when(queryRepository.createQuery(queryText, 1L)).thenReturn(Single.just(1L));
        when(queryRepository.getQueryById(1L)).thenReturn(Single.just(testQuery));
        when(queryRepository.getAgentProgress(1L)).thenReturn(Flowable.fromIterable(testAgentUpdates));
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.complete());
        when(reportRepository.getReportByQueryId(1L)).thenReturn(Single.just(testReport));
        
        viewModel.executeQuery(queryText);
        
        // Reset mocks to track retry
        reset(queryRepository);
        when(queryRepository.getAgentProgress(1L)).thenReturn(Flowable.fromIterable(testAgentUpdates));
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.complete());
        when(reportRepository.getReportByQueryId(1L)).thenReturn(Single.just(testReport));
        
        // When
        viewModel.retryExecution();
        
        // Then
        verify(queryRepository).executeAgents(1L);
        verify(queryRepository).getAgentProgress(1L);
    }
    
    /**
     * Test cancel execution functionality.
     */
    @Test
    public void testCancelExecution() {
        // Given - Start a query execution
        String queryText = "Analyze Montelukast market trends";
        when(queryRepository.createQuery(queryText, 1L)).thenReturn(Single.just(1L));
        when(queryRepository.getQueryById(1L)).thenReturn(Single.just(testQuery));
        when(queryRepository.getAgentProgress(1L)).thenReturn(Flowable.never()); // Never completes
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.never()); // Never completes
        when(queryRepository.updateQueryStatus(1L, "FAILED")).thenReturn(Completable.complete());
        
        viewModel.executeQuery(queryText);
        
        // When
        viewModel.cancelExecution();
        
        // Then
        verify(loadingObserver).onChanged(false);
        verify(queryRepository).updateQueryStatus(1L, "FAILED");
    }
    
    /**
     * Test concurrent query execution prevention.
     */
    @Test
    public void testExecuteQuery_PreventConcurrentExecution() {
        // Given
        String queryText = "Analyze Montelukast market trends";
        when(queryRepository.createQuery(queryText, 1L)).thenReturn(Single.just(1L));
        when(queryRepository.getQueryById(1L)).thenReturn(Single.just(testQuery));
        when(queryRepository.getAgentProgress(1L)).thenReturn(Flowable.never()); // Never completes
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.never()); // Never completes
        
        // When - First execution
        viewModel.executeQuery(queryText);
        
        // Reset mocks to track second call
        reset(queryRepository);
        
        // Attempt second execution while first is in progress
        viewModel.executeQuery(queryText);
        
        // Then - Second execution should be ignored
        verify(queryRepository, never()).createQuery(anyString(), anyLong());
    }
    
    /**
     * Test loading text updates based on agent progress.
     */
    @Test
    public void testLoadingTextUpdates() {
        // Given
        AgentUpdate processingUpdate = new AgentUpdate("Market Insights", AgentStatus.PROCESSING, 50);
        AgentUpdate completedUpdate = new AgentUpdate("Market Insights", AgentStatus.COMPLETED, 100);
        AgentUpdate failedUpdate = new AgentUpdate("Patent Landscape", AgentStatus.FAILED, 0);
        
        String queryText = "Analyze Montelukast market trends";
        when(queryRepository.createQuery(queryText, 1L)).thenReturn(Single.just(1L));
        when(queryRepository.getQueryById(1L)).thenReturn(Single.just(testQuery));
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.complete());
        when(queryRepository.getAgentProgress(1L))
            .thenReturn(Flowable.fromIterable(Arrays.asList(processingUpdate, completedUpdate, failedUpdate)));
        when(reportRepository.getReportByQueryId(1L)).thenReturn(Single.just(testReport));
        
        // When
        viewModel.executeQuery(queryText);
        
        // Then
        verify(loadingTextObserver).onChanged("Executing Market Insights...");
        verify(loadingTextObserver).onChanged("Market Insights completed");
        verify(loadingTextObserver).onChanged("Patent Landscape failed");
    }
    
    /**
     * Test current query ID and molecule tracking.
     */
    @Test
    public void testCurrentQueryTracking() {
        // Given
        long queryId = 1L;
        when(queryRepository.getQueryById(queryId)).thenReturn(Single.just(testQuery));
        when(queryRepository.getAgentResults(queryId)).thenReturn(Flowable.just(testAgentResults));
        
        // When
        viewModel.loadQuery(queryId);
        
        // Then
        assertEquals("Current query ID should match", queryId, viewModel.getCurrentQueryId());
        assertEquals("Current molecule should match", "Montelukast", viewModel.getCurrentMolecule());
    }
    
    /**
     * Test agent update accumulation and replacement.
     */
    @Test
    public void testAgentUpdateAccumulation() {
        // Given
        AgentUpdate initialUpdate = new AgentUpdate("Market Insights", AgentStatus.PROCESSING, 25);
        AgentUpdate updatedUpdate = new AgentUpdate("Market Insights", AgentStatus.PROCESSING, 75);
        AgentUpdate completedUpdate = new AgentUpdate("Market Insights", AgentStatus.COMPLETED, 100);
        AgentUpdate newAgentUpdate = new AgentUpdate("Patent Landscape", AgentStatus.PROCESSING, 50);
        
        String queryText = "Analyze Montelukast market trends";
        when(queryRepository.createQuery(queryText, 1L)).thenReturn(Single.just(1L));
        when(queryRepository.getQueryById(1L)).thenReturn(Single.just(testQuery));
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.complete());
        when(queryRepository.getAgentProgress(1L))
            .thenReturn(Flowable.fromIterable(Arrays.asList(initialUpdate, updatedUpdate, completedUpdate, newAgentUpdate)));
        when(reportRepository.getReportByQueryId(1L)).thenReturn(Single.just(testReport));
        
        // When
        viewModel.executeQuery(queryText);
        
        // Then - Should have 2 agents with latest updates
        verify(agentUpdatesObserver, atLeastOnce()).onChanged(argThat(updates -> 
            updates != null && updates.size() == 2 &&
            updates.stream().anyMatch(update -> 
                update.getAgentName().equals("Market Insights") && 
                update.getStatus() == AgentStatus.COMPLETED) &&
            updates.stream().anyMatch(update -> 
                update.getAgentName().equals("Patent Landscape") && 
                update.getStatus() == AgentStatus.PROCESSING)
        ));
    }
    
    /**
     * Test report ID loading after query completion.
     */
    @Test
    public void testGeneratedReportIdLoading() {
        // Given
        String queryText = "Analyze Montelukast market trends";
        when(queryRepository.createQuery(queryText, 1L)).thenReturn(Single.just(1L));
        when(queryRepository.getQueryById(1L)).thenReturn(Single.just(testQuery));
        when(queryRepository.getAgentProgress(1L)).thenReturn(Flowable.fromIterable(testAgentUpdates));
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.complete());
        when(reportRepository.getReportByQueryId(1L)).thenReturn(Single.just(testReport));
        
        // When
        viewModel.executeQuery(queryText);
        
        // Then
        verify(generatedReportIdObserver).onChanged(1L);
        verify(reportRepository).getReportByQueryId(1L);
    }
    
    /**
     * Test report loading failure handling.
     */
    @Test
    public void testGeneratedReportIdLoading_Failure() {
        // Given
        String queryText = "Analyze Montelukast market trends";
        when(queryRepository.createQuery(queryText, 1L)).thenReturn(Single.just(1L));
        when(queryRepository.getQueryById(1L)).thenReturn(Single.just(testQuery));
        when(queryRepository.getAgentProgress(1L)).thenReturn(Flowable.fromIterable(testAgentUpdates));
        when(queryRepository.executeAgents(1L)).thenReturn(Completable.complete());
        when(reportRepository.getReportByQueryId(1L))
            .thenReturn(Single.error(new RuntimeException("Report not found")));
        
        // When
        viewModel.executeQuery(queryText);
        
        // Then - Should complete successfully even if report loading fails
        verify(queryCompletedObserver).onChanged(true);
        verify(generatedReportIdObserver, never()).onChanged(anyLong());
    }
}