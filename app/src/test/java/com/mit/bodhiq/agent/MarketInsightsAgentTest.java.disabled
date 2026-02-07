package com.mit.bodhiq.agent;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.model.AgentStatus;
import com.mit.bodhiq.data.model.MarketData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import io.reactivex.rxjava3.observers.TestObserver;

/**
 * Unit tests for MarketInsightsAgent implementation.
 * Tests requirements 3.2, 7.2 for market data processing.
 */
@RunWith(RobolectricTestRunner.class)
public class MarketInsightsAgentTest {
    
    private MarketInsightsAgent marketInsightsAgent;
    private Gson gson;
    
    @Before
    public void setUp() {
        marketInsightsAgent = new MarketInsightsAgent();
        gson = new Gson();
    }
    
    /**
     * Test successful execution with supported molecule.
     * Requirement 3.2: Agent execution with market data processing
     */
    @Test
    public void testExecute_SupportedMolecule_Montelukast() {
        // Given
        String molecule = "Montelukast";
        long queryId = 1L;
        
        // When
        TestObserver<AgentResult> testObserver = marketInsightsAgent.execute(molecule, queryId).test();
        
        // Then
        testObserver.awaitDone();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
        
        AgentResult result = testObserver.values().get(0);
        assertNotNull("Result should not be null", result);
        assertEquals("Query ID should match", queryId, result.getQueryId());
        assertEquals("Agent name should be Market Insights", "Market Insights", result.getAgentName());
        assertEquals("Status should be COMPLETED", AgentStatus.COMPLETED.name(), result.getStatus());
        assertNotNull("Result data should not be null", result.getResultData());
        assertTrue("Execution time should be positive", result.getExecutionTimeMs() > 0);
        
        // Verify market data content
        MarketData marketData = gson.fromJson(result.getResultData(), MarketData.class);
        assertNotNull("Market data should not be null", marketData);
        assertEquals("Molecule should match", molecule, marketData.getMolecule());
        assertTrue("Market size should be positive", marketData.getMarketSize2024() > 0);
        assertTrue("CAGR should be positive", marketData.getCagr() > 0);
        assertNotNull("Region should not be null", marketData.getRegion());
        assertNotNull("Top indications should not be null", marketData.getTopIndications());
        assertFalse("Top indications should not be empty", marketData.getTopIndications().isEmpty());
    }
    
    /**
     * Test execution with all supported molecules.
     * Requirement 7.2: Market data availability for all supported molecules
     */
    @Test
    public void testExecute_AllSupportedMolecules() {
        String[] supportedMolecules = {"Montelukast", "Humira", "Metformin", "GLP-1", "Eliquis"};
        
        for (String molecule : supportedMolecules) {
            // Given
            long queryId = 1L;
            
            // When
            TestObserver<AgentResult> testObserver = marketInsightsAgent.execute(molecule, queryId).test();
            
            // Then
            testObserver.awaitDone();
            testObserver.assertComplete();
            testObserver.assertNoErrors();
            testObserver.assertValueCount(1);
            
            AgentResult result = testObserver.values().get(0);
            assertEquals("Status should be COMPLETED for " + molecule, 
                AgentStatus.COMPLETED.name(), result.getStatus());
            assertNotNull("Result data should not be null for " + molecule, result.getResultData());
            
            // Verify market data can be parsed
            MarketData marketData = gson.fromJson(result.getResultData(), MarketData.class);
            assertNotNull("Market data should not be null for " + molecule, marketData);
            assertEquals("Molecule should match for " + molecule, molecule, marketData.getMolecule());
        }
    }
    
    /**
     * Test execution with unsupported molecule.
     */
    @Test
    public void testExecute_UnsupportedMolecule() {
        // Given
        String molecule = "UnsupportedDrug";
        long queryId = 1L;
        
        // When
        TestObserver<AgentResult> testObserver = marketInsightsAgent.execute(molecule, queryId).test();
        
        // Then
        testObserver.awaitDone();
        testObserver.assertError(RuntimeException.class);
        
        Throwable error = testObserver.errors().get(0);
        assertTrue("Error message should mention no market data", 
            error.getMessage().contains("No market data available"));
        assertTrue("Error message should mention molecule name", 
            error.getMessage().contains(molecule));
    }
    
    /**
     * Test agent metadata.
     */
    @Test
    public void testAgentMetadata() {
        assertEquals("Agent name should be Market Insights", 
            "Market Insights", marketInsightsAgent.getAgentName());
        assertEquals("Execution order should be 1", 
            1, marketInsightsAgent.getExecutionOrder());
        assertEquals("Estimated duration should be 3000ms", 
            3000, marketInsightsAgent.getEstimatedDurationMs());
    }
    
    /**
     * Test execution timing.
     */
    @Test
    public void testExecute_TimingAccuracy() {
        // Given
        String molecule = "Montelukast";
        long queryId = 1L;
        long startTime = System.currentTimeMillis();
        
        // When
        AgentResult result = marketInsightsAgent.execute(molecule, queryId).blockingGet();
        long endTime = System.currentTimeMillis();
        long actualDuration = endTime - startTime;
        
        // Then
        assertTrue("Execution should take approximately the estimated time", 
            actualDuration >= marketInsightsAgent.getEstimatedDurationMs() - 500); // Allow 500ms tolerance
        assertTrue("Execution should not take much longer than estimated", 
            actualDuration <= marketInsightsAgent.getEstimatedDurationMs() + 1000); // Allow 1s tolerance
        
        assertEquals("Recorded execution time should match estimated", 
            marketInsightsAgent.getEstimatedDurationMs(), result.getExecutionTimeMs());
    }
    
    /**
     * Test result data structure for Humira.
     */
    @Test
    public void testExecute_HumiraMarketData() {
        // Given
        String molecule = "Humira";
        long queryId = 1L;
        
        // When
        AgentResult result = marketInsightsAgent.execute(molecule, queryId).blockingGet();
        
        // Then
        MarketData marketData = gson.fromJson(result.getResultData(), MarketData.class);
        assertNotNull("Market data should not be null", marketData);
        assertEquals("Molecule should be Humira", "Humira", marketData.getMolecule());
        
        // Verify Humira-specific data
        assertNotNull("Competitors should not be null", marketData.getCompetitors());
        assertFalse("Competitors should not be empty", marketData.getCompetitors().isEmpty());
        assertNotNull("Growth drivers should not be null", marketData.getGrowthDrivers());
        assertFalse("Growth drivers should not be empty", marketData.getGrowthDrivers().isEmpty());
        assertNotNull("Emerging indications should not be null", marketData.getEmergingIndications());
    }
    
    /**
     * Test result data structure for GLP-1.
     */
    @Test
    public void testExecute_GLP1MarketData() {
        // Given
        String molecule = "GLP-1";
        long queryId = 1L;
        
        // When
        AgentResult result = marketInsightsAgent.execute(molecule, queryId).blockingGet();
        
        // Then
        MarketData marketData = gson.fromJson(result.getResultData(), MarketData.class);
        assertNotNull("Market data should not be null", marketData);
        assertEquals("Molecule should be GLP-1", "GLP-1", marketData.getMolecule());
        
        // Verify GLP-1 specific characteristics
        assertTrue("GLP-1 should have significant market size", marketData.getMarketSize2024() > 1000000000L); // > $1B
        assertTrue("GLP-1 should have high CAGR", marketData.getCagr() > 10.0); // > 10%
    }
    
    /**
     * Test concurrent execution safety.
     */
    @Test
    public void testExecute_ConcurrentExecution() {
        // Given
        String molecule = "Montelukast";
        long queryId1 = 1L;
        long queryId2 = 2L;
        
        // When - Execute concurrently
        TestObserver<AgentResult> testObserver1 = marketInsightsAgent.execute(molecule, queryId1).test();
        TestObserver<AgentResult> testObserver2 = marketInsightsAgent.execute(molecule, queryId2).test();
        
        // Then
        testObserver1.awaitDone();
        testObserver2.awaitDone();
        
        testObserver1.assertComplete();
        testObserver1.assertNoErrors();
        testObserver2.assertComplete();
        testObserver2.assertNoErrors();
        
        AgentResult result1 = testObserver1.values().get(0);
        AgentResult result2 = testObserver2.values().get(0);
        
        assertEquals("First result should have query ID 1", queryId1, result1.getQueryId());
        assertEquals("Second result should have query ID 2", queryId2, result2.getQueryId());
        
        // Both should have same market data content
        assertEquals("Both results should have same data", result1.getResultData(), result2.getResultData());
    }
    
    /**
     * Test null molecule handling.
     */
    @Test
    public void testExecute_NullMolecule() {
        // Given
        String molecule = null;
        long queryId = 1L;
        
        // When
        TestObserver<AgentResult> testObserver = marketInsightsAgent.execute(molecule, queryId).test();
        
        // Then
        testObserver.awaitDone();
        testObserver.assertError(RuntimeException.class);
        
        Throwable error = testObserver.errors().get(0);
        assertTrue("Error message should mention no market data", 
            error.getMessage().contains("No market data available"));
    }
    
    /**
     * Test empty molecule handling.
     */
    @Test
    public void testExecute_EmptyMolecule() {
        // Given
        String molecule = "";
        long queryId = 1L;
        
        // When
        TestObserver<AgentResult> testObserver = marketInsightsAgent.execute(molecule, queryId).test();
        
        // Then
        testObserver.awaitDone();
        testObserver.assertError(RuntimeException.class);
        
        Throwable error = testObserver.errors().get(0);
        assertTrue("Error message should mention no market data", 
            error.getMessage().contains("No market data available"));
    }
    
    /**
     * Test case sensitivity in molecule names.
     */
    @Test
    public void testExecute_CaseSensitivity() {
        // Given - Test different cases
        String[] moleculeVariants = {"montelukast", "MONTELUKAST", "Montelukast", "MontelukasT"};
        long queryId = 1L;
        
        for (String molecule : moleculeVariants) {
            // When
            TestObserver<AgentResult> testObserver = marketInsightsAgent.execute(molecule, queryId).test();
            
            // Then
            testObserver.awaitDone();
            
            // Only exact case match should work based on MockDataProvider implementation
            if ("Montelukast".equals(molecule)) {
                testObserver.assertComplete();
                testObserver.assertNoErrors();
            } else {
                testObserver.assertError(RuntimeException.class);
            }
        }
    }
}