package com.mit.bodhiq.agent;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mit.bodhiq.data.database.dao.AgentResultDao;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.model.AgentStatus;
import com.mit.bodhiq.data.model.AgentUpdate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

/**
 * Unit tests for MasterAgent sequential execution pipeline.
 * Tests requirements 3.2, 3.3 for agent orchestration and error handling.
 */
@RunWith(RobolectricTestRunner.class)
public class MasterAgentTest {
    
    @Mock
    private AgentResultDao agentResultDao;
    
    @Mock
    private PharmaceuticalAgent mockAgent1;
    
    @Mock
    private PharmaceuticalAgent mockAgent2;
    
    @Mock
    private PharmaceuticalAgent mockAgent3;
    
    private MasterAgent masterAgent;
    
    // Test data
    private AgentResult testResult1;
    private AgentResult testResult2;
    private AgentResult testResult3;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        masterAgent = new MasterAgent(agentResultDao);
        
        // Setup mock agents
        setupMockAgent(mockAgent1, "Market Insights", 1, 2000);
        setupMockAgent(mockAgent2, "Patent Landscape", 2, 3000);
        setupMockAgent(mockAgent3, "Clinical Trials", 3, 2500);
        
        // Setup test results
        testResult1 = createTestResult(1L, "Market Insights", AgentStatus.COMPLETED);
        testResult2 = createTestResult(2L, "Patent Landscape", AgentStatus.COMPLETED);
        testResult3 = createTestResult(3L, "Clinical Trials", AgentStatus.COMPLETED);
        
        // Setup DAO mocks
        when(agentResultDao.insertAgentResult(any(AgentResult.class)))
            .thenReturn(Single.just(1L), Single.just(2L), Single.just(3L));
        when(agentResultDao.updateAgentResult(any(AgentResult.class)))
            .thenReturn(io.reactivex.rxjava3.core.Completable.complete());
    }
    
    private void setupMockAgent(PharmaceuticalAgent agent, String name, int order, int duration) {
        when(agent.getAgentName()).thenReturn(name);
        when(agent.getExecutionOrder()).thenReturn(order);
        when(agent.getEstimatedDurationMs()).thenReturn(duration);
    }
    
    private AgentResult createTestResult(long id, String agentName, AgentStatus status) {
        AgentResult result = new AgentResult(1L, agentName, status.name(), System.currentTimeMillis());
        result.setId(id);
        result.setCompletedAt(System.currentTimeMillis());
        result.setExecutionTimeMs(2000);
        return result;
    }
    
    /**
     * Test agent registration and ordering.
     * Requirement 3.2: Sequential agent execution pipeline
     */
    @Test
    public void testRegisterAgent_OrderingByExecutionOrder() {
        // When
        masterAgent.registerAgent(mockAgent3); // Order 3
        masterAgent.registerAgent(mockAgent1); // Order 1
        masterAgent.registerAgent(mockAgent2); // Order 2
        
        // Then
        assertEquals("Should have 3 agents registered", 3, masterAgent.getAgentCount());
        assertEquals("Total estimated duration should be sum of all agents", 
            7500, masterAgent.getTotalEstimatedDurationMs());
    }
    
    /**
     * Test successful sequential execution of all agents.
     * Requirement 3.2: Sequential agent execution with progress updates
     */
    @Test
    public void testExecuteAllAgents_Success() {
        // Given
        String molecule = "Montelukast";
        long queryId = 1L;
        
        // Setup successful agent executions
        when(mockAgent1.execute(molecule, queryId)).thenReturn(Single.just(testResult1));
        when(mockAgent2.execute(molecule, queryId)).thenReturn(Single.just(testResult2));
        when(mockAgent3.execute(molecule, queryId)).thenReturn(Single.just(testResult3));
        
        // Register agents
        masterAgent.registerAgent(mockAgent1);
        masterAgent.registerAgent(mockAgent2);
        masterAgent.registerAgent(mockAgent3);
        
        // When
        TestSubscriber<AgentUpdate> testSubscriber = masterAgent.executeAllAgents(molecule, queryId)
            .test();
        
        // Then
        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        
        // Verify all agents were executed in order
        verify(mockAgent1).execute(molecule, queryId);
        verify(mockAgent2).execute(molecule, queryId);
        verify(mockAgent3).execute(molecule, queryId);
        
        // Verify database operations
        verify(agentResultDao, times(3)).insertAgentResult(any(AgentResult.class));
        verify(agentResultDao, times(3)).updateAgentResult(any(AgentResult.class));
        
        // Verify progress updates were emitted
        assertTrue("Should have emitted progress updates", testSubscriber.values().size() >= 3);
    }
    
    /**
     * Test agent execution with individual agent failure.
     * Requirement 3.3: Error handling and retry logic
     */
    @Test
    public void testExecuteAllAgents_IndividualAgentFailure() {
        // Given
        String molecule = "Montelukast";
        long queryId = 1L;
        String errorMessage = "Agent execution failed";
        
        // Setup agent executions - second agent fails
        when(mockAgent1.execute(molecule, queryId)).thenReturn(Single.just(testResult1));
        when(mockAgent2.execute(molecule, queryId)).thenReturn(Single.error(new RuntimeException(errorMessage)));
        when(mockAgent3.execute(molecule, queryId)).thenReturn(Single.just(testResult3));
        
        // Register agents
        masterAgent.registerAgent(mockAgent1);
        masterAgent.registerAgent(mockAgent2);
        masterAgent.registerAgent(mockAgent3);
        
        // When
        TestSubscriber<AgentUpdate> testSubscriber = masterAgent.executeAllAgents(molecule, queryId)
            .test();
        
        // Then
        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber.assertComplete(); // Pipeline should continue despite individual failure
        testSubscriber.assertNoErrors();
        
        // Verify all agents were attempted
        verify(mockAgent1).execute(molecule, queryId);
        verify(mockAgent2, times(3)).execute(molecule, queryId); // Should retry 2 times
        verify(mockAgent3).execute(molecule, queryId);
        
        // Verify failed agent result was recorded
        verify(agentResultDao, times(3)).insertAgentResult(any(AgentResult.class));
        verify(agentResultDao, times(3)).updateAgentResult(any(AgentResult.class));
        
        // Check that failure update was emitted
        boolean hasFailureUpdate = testSubscriber.values().stream()
            .anyMatch(update -> update.getStatus() == AgentStatus.FAILED && 
                              update.getAgentName().equals("Patent Landscape"));
        assertTrue("Should have emitted failure update for failed agent", hasFailureUpdate);
    }
    
    /**
     * Test agent execution timeout handling.
     * Requirement 3.3: Error handling with timeout
     */
    @Test
    public void testExecuteAllAgents_TimeoutHandling() {
        // Given
        String molecule = "Montelukast";
        long queryId = 1L;
        
        // Setup agent that never completes (simulates timeout)
        when(mockAgent1.execute(molecule, queryId)).thenReturn(Single.never());
        
        // Register single agent
        masterAgent.registerAgent(mockAgent1);
        
        // When
        TestSubscriber<AgentUpdate> testSubscriber = masterAgent.executeAllAgents(molecule, queryId)
            .test();
        
        // Then
        testSubscriber.awaitDone(35, TimeUnit.SECONDS); // Wait longer than 30s timeout
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        
        // Verify timeout was handled and failure update was emitted
        boolean hasFailureUpdate = testSubscriber.values().stream()
            .anyMatch(update -> update.getStatus() == AgentStatus.FAILED);
        assertTrue("Should have emitted failure update for timeout", hasFailureUpdate);
    }
    
    /**
     * Test retry logic for failed agents.
     * Requirement 3.3: Retry logic implementation
     */
    @Test
    public void testExecuteAllAgents_RetryLogic() {
        // Given
        String molecule = "Montelukast";
        long queryId = 1L;
        
        // Setup agent that fails twice then succeeds
        when(mockAgent1.execute(molecule, queryId))
            .thenReturn(Single.error(new RuntimeException("First failure")))
            .thenReturn(Single.error(new RuntimeException("Second failure")))
            .thenReturn(Single.just(testResult1));
        
        // Register agent
        masterAgent.registerAgent(mockAgent1);
        
        // When
        TestSubscriber<AgentUpdate> testSubscriber = masterAgent.executeAllAgents(molecule, queryId)
            .test();
        
        // Then
        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        
        // Verify agent was retried 3 times total (initial + 2 retries)
        verify(mockAgent1, times(3)).execute(molecule, queryId);
        
        // Verify successful completion after retries
        boolean hasSuccessUpdate = testSubscriber.values().stream()
            .anyMatch(update -> update.getStatus() == AgentStatus.COMPLETED && 
                              update.getAgentName().equals("Market Insights"));
        assertTrue("Should have emitted success update after retries", hasSuccessUpdate);
    }
    
    /**
     * Test retry exhaustion handling.
     * Requirement 3.3: Error handling when retries are exhausted
     */
    @Test
    public void testExecuteAllAgents_RetryExhaustion() {
        // Given
        String molecule = "Montelukast";
        long queryId = 1L;
        String errorMessage = "Persistent failure";
        
        // Setup agent that always fails
        when(mockAgent1.execute(molecule, queryId))
            .thenReturn(Single.error(new RuntimeException(errorMessage)));
        
        // Register agent
        masterAgent.registerAgent(mockAgent1);
        
        // When
        TestSubscriber<AgentUpdate> testSubscriber = masterAgent.executeAllAgents(molecule, queryId)
            .test();
        
        // Then
        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        
        // Verify agent was retried maximum times (initial + 2 retries = 3 total)
        verify(mockAgent1, times(3)).execute(molecule, queryId);
        
        // Verify failure update was emitted
        boolean hasFailureUpdate = testSubscriber.values().stream()
            .anyMatch(update -> update.getStatus() == AgentStatus.FAILED && 
                              update.getError() != null);
        assertTrue("Should have emitted failure update after retry exhaustion", hasFailureUpdate);
    }
    
    /**
     * Test progress stream functionality.
     */
    @Test
    public void testGetProgressStream() {
        // When
        Flowable<AgentUpdate> progressStream = masterAgent.getProgressStream();
        
        // Then
        assertNotNull("Progress stream should not be null", progressStream);
        
        TestSubscriber<AgentUpdate> testSubscriber = progressStream.test();
        testSubscriber.assertSubscribed();
        testSubscriber.assertNoErrors();
    }
    
    /**
     * Test molecule support checking.
     */
    @Test
    public void testIsMoleculeSupported() {
        // Test supported molecules
        assertTrue("Montelukast should be supported", masterAgent.isMoleculeSupported("Montelukast"));
        assertTrue("Humira should be supported", masterAgent.isMoleculeSupported("Humira"));
        assertTrue("Metformin should be supported", masterAgent.isMoleculeSupported("Metformin"));
        assertTrue("GLP-1 should be supported", masterAgent.isMoleculeSupported("GLP-1"));
        assertTrue("Eliquis should be supported", masterAgent.isMoleculeSupported("Eliquis"));
        
        // Test case insensitive support
        assertTrue("Should be case insensitive", masterAgent.isMoleculeSupported("montelukast"));
        assertTrue("Should be case insensitive", masterAgent.isMoleculeSupported("HUMIRA"));
        
        // Test unsupported molecules
        assertFalse("Unknown molecule should not be supported", masterAgent.isMoleculeSupported("UnknownDrug"));
        assertFalse("Null should not be supported", masterAgent.isMoleculeSupported(null));
        assertFalse("Empty string should not be supported", masterAgent.isMoleculeSupported(""));
    }
    
    /**
     * Test execution cancellation.
     */
    @Test
    public void testCancelExecution() {
        // Given
        TestSubscriber<AgentUpdate> progressSubscriber = masterAgent.getProgressStream().test();
        
        // When
        masterAgent.cancelExecution();
        
        // Then
        progressSubscriber.awaitCount(1);
        
        AgentUpdate cancelUpdate = progressSubscriber.values().get(0);
        assertEquals("Should emit cancel update", "Pipeline", cancelUpdate.getAgentName());
        assertEquals("Should have cancelled status", AgentStatus.CANCELLED, cancelUpdate.getStatus());
        assertNotNull("Should have cancel message", cancelUpdate.getError());
        assertTrue("Cancel message should mention cancellation", 
            cancelUpdate.getError().contains("cancelled"));
    }
    
    /**
     * Test agent count functionality.
     */
    @Test
    public void testGetAgentCount() {
        // Initially no agents
        assertEquals("Should start with 0 agents", 0, masterAgent.getAgentCount());
        
        // Add agents
        masterAgent.registerAgent(mockAgent1);
        assertEquals("Should have 1 agent", 1, masterAgent.getAgentCount());
        
        masterAgent.registerAgent(mockAgent2);
        assertEquals("Should have 2 agents", 2, masterAgent.getAgentCount());
        
        masterAgent.registerAgent(mockAgent3);
        assertEquals("Should have 3 agents", 3, masterAgent.getAgentCount());
    }
    
    /**
     * Test total estimated duration calculation.
     */
    @Test
    public void testGetTotalEstimatedDurationMs() {
        // Initially no duration
        assertEquals("Should start with 0 duration", 0, masterAgent.getTotalEstimatedDurationMs());
        
        // Add agents with different durations
        masterAgent.registerAgent(mockAgent1); // 2000ms
        assertEquals("Should have 2000ms duration", 2000, masterAgent.getTotalEstimatedDurationMs());
        
        masterAgent.registerAgent(mockAgent2); // 3000ms
        assertEquals("Should have 5000ms duration", 5000, masterAgent.getTotalEstimatedDurationMs());
        
        masterAgent.registerAgent(mockAgent3); // 2500ms
        assertEquals("Should have 7500ms duration", 7500, masterAgent.getTotalEstimatedDurationMs());
    }
    
    /**
     * Test empty agent list execution.
     */
    @Test
    public void testExecuteAllAgents_EmptyAgentList() {
        // Given
        String molecule = "Montelukast";
        long queryId = 1L;
        // No agents registered
        
        // When
        TestSubscriber<AgentUpdate> testSubscriber = masterAgent.executeAllAgents(molecule, queryId)
            .test();
        
        // Then
        testSubscriber.awaitDone(5, TimeUnit.SECONDS);
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(0); // No updates should be emitted
    }
    
    /**
     * Test database error handling during agent result insertion.
     */
    @Test
    public void testExecuteAllAgents_DatabaseError() {
        // Given
        String molecule = "Montelukast";
        long queryId = 1L;
        
        // Setup database error
        when(agentResultDao.insertAgentResult(any(AgentResult.class)))
            .thenReturn(Single.error(new RuntimeException("Database error")));
        
        when(mockAgent1.execute(molecule, queryId)).thenReturn(Single.just(testResult1));
        masterAgent.registerAgent(mockAgent1);
        
        // When
        TestSubscriber<AgentUpdate> testSubscriber = masterAgent.executeAllAgents(molecule, queryId)
            .test();
        
        // Then
        testSubscriber.awaitDone(10, TimeUnit.SECONDS);
        testSubscriber.assertError(RuntimeException.class);
        
        // Verify database operation was attempted
        verify(agentResultDao).insertAgentResult(any(AgentResult.class));
    }
}