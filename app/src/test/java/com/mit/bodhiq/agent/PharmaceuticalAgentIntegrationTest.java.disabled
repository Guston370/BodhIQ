package com.mit.bodhiq.agent;

import static org.junit.Assert.*;

import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.model.AgentStatus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.observers.TestObserver;

/**
 * Integration tests for all pharmaceutical agent implementations.
 * Tests requirements 3.2, 7.2, 7.3, 7.4 for comprehensive agent functionality.
 */
@RunWith(RobolectricTestRunner.class)
public class PharmaceuticalAgentIntegrationTest {
    
    private List<PharmaceuticalAgent> allAgents;
    private final String[] supportedMolecules = {"Montelukast", "Humira", "Metformin", "GLP-1", "Eliquis"};
    
    @Before
    public void setUp() {
        // Initialize all agent implementations
        allAgents = Arrays.asList(
            new MarketInsightsAgent(),
            new PatentLandscapeAgent(),
            new ClinicalTrialsAgent(),
            new EximTradeAgent(),
            new WebIntelligenceAgent(),
            new InternalInsightsAgent(),
            new ReportGeneratorAgent()
        );
    }
    
    /**
     * Test that all agents implement the interface correctly.
     * Requirement 3.2: All agents follow PharmaceuticalAgent interface
     */
    @Test
    public void testAllAgents_InterfaceImplementation() {
        for (PharmaceuticalAgent agent : allAgents) {
            // Test metadata methods
            assertNotNull("Agent name should not be null", agent.getAgentName());
            assertFalse("Agent name should not be empty", agent.getAgentName().trim().isEmpty());
            
            assertTrue("Execution order should be positive", agent.getExecutionOrder() > 0);
            assertTrue("Execution order should be reasonable", agent.getExecutionOrder() <= 10);
            
            assertTrue("Estimated duration should be positive", agent.getEstimatedDurationMs() > 0);
            assertTrue("Estimated duration should be reasonable", agent.getEstimatedDurationMs() <= 30000); // Max 30 seconds
        }
    }
    
    /**
     * Test execution order consistency.
     * Requirement 3.2: Sequential agent execution pipeline
     */
    @Test
    public void testAllAgents_ExecutionOrderUniqueness() {
        // Collect all execution orders
        int[] orders = allAgents.stream()
            .mapToInt(PharmaceuticalAgent::getExecutionOrder)
            .sorted()
            .toArray();
        
        // Verify uniqueness
        for (int i = 1; i < orders.length; i++) {
            assertNotEquals("Execution orders should be unique", orders[i-1], orders[i]);
        }
        
        // Verify expected order sequence (1-7)
        assertEquals("Should have 7 agents", 7, orders.length);
        assertEquals("First order should be 1", 1, orders[0]);
        assertEquals("Last order should be 7", 7, orders[6]);
    }
    
    /**
     * Test all agents with all supported molecules.
     * Requirement 7.2, 7.3, 7.4: Data availability for all molecules
     */
    @Test
    public void testAllAgents_AllSupportedMolecules() {
        for (String molecule : supportedMolecules) {
            for (PharmaceuticalAgent agent : allAgents) {
                // Given
                long queryId = 1L;
                
                // When
                TestObserver<AgentResult> testObserver = agent.execute(molecule, queryId)
                    .timeout(10, TimeUnit.SECONDS)
                    .test();
                
                // Then
                testObserver.awaitDone();
                testObserver.assertComplete();
                testObserver.assertNoErrors();
                testObserver.assertValueCount(1);
                
                AgentResult result = testObserver.values().get(0);
                assertNotNull("Result should not be null for " + agent.getAgentName() + " with " + molecule, result);
                assertEquals("Query ID should match", queryId, result.getQueryId());
                assertEquals("Agent name should match", agent.getAgentName(), result.getAgentName());
                assertEquals("Status should be COMPLETED", AgentStatus.COMPLETED.name(), result.getStatus());
                assertNotNull("Result data should not be null", result.getResultData());
                assertFalse("Result data should not be empty", result.getResultData().trim().isEmpty());
            }
        }
    }
    
    /**
     * Test all agents with unsupported molecule.
     */
    @Test
    public void testAllAgents_UnsupportedMolecule() {
        String unsupportedMolecule = "UnsupportedDrug";
        
        for (PharmaceuticalAgent agent : allAgents) {
            // Given
            long queryId = 1L;
            
            // When
            TestObserver<AgentResult> testObserver = agent.execute(unsupportedMolecule, queryId)
                .timeout(10, TimeUnit.SECONDS)
                .test();
            
            // Then
            testObserver.awaitDone();
            testObserver.assertError(RuntimeException.class);
            
            Throwable error = testObserver.errors().get(0);
            assertNotNull("Error should not be null for " + agent.getAgentName(), error);
            assertTrue("Error message should indicate no data available for " + agent.getAgentName(),
                error.getMessage().toLowerCase().contains("no") || 
                error.getMessage().toLowerCase().contains("not found") ||
                error.getMessage().toLowerCase().contains("unavailable"));
        }
    }
    
    /**
     * Test execution timing consistency.
     */
    @Test
    public void testAllAgents_ExecutionTiming() {
        String molecule = "Montelukast";
        long queryId = 1L;
        
        for (PharmaceuticalAgent agent : allAgents) {
            // Given
            long startTime = System.currentTimeMillis();
            
            // When
            AgentResult result = agent.execute(molecule, queryId).blockingGet();
            long endTime = System.currentTimeMillis();
            long actualDuration = endTime - startTime;
            
            // Then
            assertTrue("Execution should take approximately the estimated time for " + agent.getAgentName(), 
                actualDuration >= agent.getEstimatedDurationMs() - 1000); // Allow 1s tolerance
            assertTrue("Execution should not take much longer than estimated for " + agent.getAgentName(), 
                actualDuration <= agent.getEstimatedDurationMs() + 2000); // Allow 2s tolerance
            
            assertEquals("Recorded execution time should match estimated for " + agent.getAgentName(), 
                agent.getEstimatedDurationMs(), result.getExecutionTimeMs());
        }
    }
    
    /**
     * Test result data format consistency.
     */
    @Test
    public void testAllAgents_ResultDataFormat() {
        String molecule = "Montelukast";
        long queryId = 1L;
        
        for (PharmaceuticalAgent agent : allAgents) {
            // When
            AgentResult result = agent.execute(molecule, queryId).blockingGet();
            
            // Then
            assertNotNull("Result data should not be null for " + agent.getAgentName(), result.getResultData());
            
            // Verify JSON format (should be valid JSON)
            String resultData = result.getResultData();
            assertTrue("Result data should start with { or [ for " + agent.getAgentName(),
                resultData.trim().startsWith("{") || resultData.trim().startsWith("["));
            assertTrue("Result data should end with } or ] for " + agent.getAgentName(),
                resultData.trim().endsWith("}") || resultData.trim().endsWith("]"));
            
            // Verify minimum data length (should contain meaningful data)
            assertTrue("Result data should have reasonable length for " + agent.getAgentName(),
                resultData.length() > 50);
        }
    }
    
    /**
     * Test concurrent execution of all agents.
     */
    @Test
    public void testAllAgents_ConcurrentExecution() {
        String molecule = "Montelukast";
        long queryId = 1L;
        
        // Start all agents concurrently
        TestObserver<AgentResult>[] observers = new TestObserver[allAgents.size()];
        for (int i = 0; i < allAgents.size(); i++) {
            observers[i] = allAgents.get(i).execute(molecule, queryId)
                .timeout(15, TimeUnit.SECONDS)
                .test();
        }
        
        // Wait for all to complete
        for (TestObserver<AgentResult> observer : observers) {
            observer.awaitDone();
            observer.assertComplete();
            observer.assertNoErrors();
            observer.assertValueCount(1);
        }
        
        // Verify all results
        for (int i = 0; i < allAgents.size(); i++) {
            AgentResult result = observers[i].values().get(0);
            assertEquals("Agent name should match for concurrent execution",
                allAgents.get(i).getAgentName(), result.getAgentName());
            assertEquals("Query ID should match for concurrent execution", queryId, result.getQueryId());
        }
    }
    
    /**
     * Test agent name uniqueness.
     */
    @Test
    public void testAllAgents_NameUniqueness() {
        String[] agentNames = allAgents.stream()
            .map(PharmaceuticalAgent::getAgentName)
            .toArray(String[]::new);
        
        // Check for duplicates
        for (int i = 0; i < agentNames.length; i++) {
            for (int j = i + 1; j < agentNames.length; j++) {
                assertNotEquals("Agent names should be unique", agentNames[i], agentNames[j]);
            }
        }
        
        // Verify expected agent names
        List<String> expectedNames = Arrays.asList(
            "Market Insights", "Patent Landscape", "Clinical Trials", 
            "EXIM Trade", "Web Intelligence", "Internal Insights", "Report Generator"
        );
        
        for (String expectedName : expectedNames) {
            boolean found = Arrays.stream(agentNames).anyMatch(name -> name.equals(expectedName));
            assertTrue("Should have agent named: " + expectedName, found);
        }
    }
    
    /**
     * Test error handling consistency across agents.
     */
    @Test
    public void testAllAgents_ErrorHandlingConsistency() {
        String[] invalidInputs = {null, "", "   ", "InvalidMolecule123"};
        long queryId = 1L;
        
        for (String invalidInput : invalidInputs) {
            for (PharmaceuticalAgent agent : allAgents) {
                // When
                TestObserver<AgentResult> testObserver = agent.execute(invalidInput, queryId)
                    .timeout(10, TimeUnit.SECONDS)
                    .test();
                
                // Then
                testObserver.awaitDone();
                testObserver.assertError(RuntimeException.class);
                
                Throwable error = testObserver.errors().get(0);
                assertNotNull("Error should not be null for " + agent.getAgentName() + 
                    " with input: '" + invalidInput + "'", error);
                assertNotNull("Error message should not be null for " + agent.getAgentName(), 
                    error.getMessage());
                assertFalse("Error message should not be empty for " + agent.getAgentName(), 
                    error.getMessage().trim().isEmpty());
            }
        }
    }
    
    /**
     * Test result completeness for different molecules.
     */
    @Test
    public void testAllAgents_ResultCompleteness() {
        for (String molecule : supportedMolecules) {
            for (PharmaceuticalAgent agent : allAgents) {
                // When
                AgentResult result = agent.execute(molecule, 1L).blockingGet();
                
                // Then - Verify all required fields are populated
                assertNotNull("Query ID should be set", result.getQueryId());
                assertNotNull("Agent name should be set", result.getAgentName());
                assertNotNull("Status should be set", result.getStatus());
                assertNotNull("Started at should be set", result.getStartedAt());
                assertNotNull("Completed at should be set", result.getCompletedAt());
                assertNotNull("Execution time should be set", result.getExecutionTimeMs());
                assertNotNull("Result data should be set", result.getResultData());
                
                assertTrue("Started at should be reasonable", result.getStartedAt() > 0);
                assertTrue("Completed at should be reasonable", result.getCompletedAt() > 0);
                assertTrue("Completed at should be after started at", 
                    result.getCompletedAt() >= result.getStartedAt());
                assertTrue("Execution time should be positive", result.getExecutionTimeMs() > 0);
            }
        }
    }
}