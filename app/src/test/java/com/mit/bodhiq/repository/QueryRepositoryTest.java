package com.mit.bodhiq.repository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mit.bodhiq.agent.MasterAgent;
import com.mit.bodhiq.data.database.dao.AgentResultDao;
import com.mit.bodhiq.data.database.dao.QueryDao;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.database.entity.Query;
import com.mit.bodhiq.data.model.AgentStatus;
import com.mit.bodhiq.data.model.AgentUpdate;
import com.mit.bodhiq.data.repository.QueryRepository;

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
 * Unit tests for QueryRepository agent execution workflow.
 * Tests requirements 3.1, 3.2, 3.4 for query processing and agent orchestration.
 */
@RunWith(RobolectricTestRunner.class)
public class QueryRepositoryTest {
    
    @Mock
    private QueryDao queryDao;
    
    @Mock
    private AgentResultDao agentResultDao;
    
    @Mock
    private MasterAgent masterAgent;
    
    private QueryRepository queryRepository;
    
    // Test data
    private Query testQuery;
    private AgentResult testAgentResult;
    private List<AgentUpdate> testAgentUpdates;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        queryRepository = new QueryRepository(queryDao, agentResultDao, masterAgent);
        
        // Setup test data
        testQuery = new Query(1L, "Analyze Montelukast market data", "Montelukast", "PENDING", System.currentTimeMillis());
        testQuery.setId(1L);
        
        testAgentResult = new AgentResult(1L, "Market Insights", "COMPLETED", System.currentTimeMillis());
        testAgentResult.setId(1L);
        
        testAgentUpdates = Arrays.asList(
            new AgentUpdate("Market Insights", AgentStatus.COMPLETED, 100),
            new AgentUpdate("Patent Landscape", AgentStatus.COMPLETED, 100)
        );
    }
    
    /**
     * Test query creation with automatic molecule extraction.
     * Requirement 3.1: Extract molecule name and create Query entity
     */
    @Test
    public void testCreateQuery_WithMoleculeExtraction() {
        // Given
        String queryText = "Analyze Montelukast market trends and competitive landscape";
        long userId = 1L;
        when(queryDao.insertQuery(any(Query.class))).thenReturn(Single.just(1L));
        
        // When
        Long queryId = queryRepository.createQuery(queryText, userId).blockingGet();
        
        // Then
        assertNotNull("Query ID should not be null", queryId);
        assertEquals("Should return query ID 1", Long.valueOf(1L), queryId);
        verify(queryDao).insertQuery(argThat(query -> 
            query.getQueryText().equals(queryText) &&
            query.getMolecule().equals("Montelukast") &&
            query.getUserId() == userId &&
            query.getStatus().equals("PENDING")
        ));
    }
    
    /**
     * Test query creation with explicit molecule specification.
     */
    @Test
    public void testCreateQuery_WithExplicitMolecule() {
        // Given
        String queryText = "Market analysis for diabetes treatment";
        String molecule = "Metformin";
        long userId = 1L;
        when(queryDao.insertQuery(any(Query.class))).thenReturn(Single.just(2L));
        
        // When
        Long queryId = queryRepository.createQuery(queryText, molecule, userId).blockingGet();
        
        // Then
        assertNotNull("Query ID should not be null", queryId);
        assertEquals("Should return query ID 2", Long.valueOf(2L), queryId);
        verify(queryDao).insertQuery(argThat(query -> 
            query.getQueryText().equals(queryText) &&
            query.getMolecule().equals(molecule) &&
            query.getUserId() == userId
        ));
    }
    
    /**
     * Test query creation with unsupported molecule.
     */
    @Test
    public void testCreateQuery_UnsupportedMolecule() {
        // Given
        String queryText = "Analyze unknown drug market";
        long userId = 1L;
        
        // When & Then
        try {
            queryRepository.createQuery(queryText, userId).blockingGet();
            fail("Should throw IllegalArgumentException for unsupported molecule");
        } catch (Exception e) {
            assertTrue("Should be IllegalArgumentException", e.getCause() instanceof IllegalArgumentException);
            assertTrue("Error message should mention no supported molecule", 
                e.getCause().getMessage().contains("No supported molecule found"));
        }
        
        verify(queryDao, never()).insertQuery(any(Query.class));
    }
    
    /**
     * Test query creation with explicit unsupported molecule.
     */
    @Test
    public void testCreateQuery_ExplicitUnsupportedMolecule() {
        // Given
        String queryText = "Market analysis";
        String molecule = "UnsupportedDrug";
        long userId = 1L;
        
        // When & Then
        try {
            queryRepository.createQuery(queryText, molecule, userId).blockingGet();
            fail("Should throw IllegalArgumentException for unsupported molecule");
        } catch (Exception e) {
            assertTrue("Should be IllegalArgumentException", e.getCause() instanceof IllegalArgumentException);
            assertTrue("Error message should mention unsupported molecule", 
                e.getCause().getMessage().contains("Unsupported molecule"));
        }
        
        verify(queryDao, never()).insertQuery(any(Query.class));
    }
    
    /**
     * Test agent execution workflow.
     * Requirement 3.2: Sequential agent execution with progress updates
     */
    @Test
    public void testExecuteAgents_Success() {
        // Given
        long queryId = 1L;
        when(queryDao.getQueryById(queryId)).thenReturn(Single.just(testQuery));
        when(queryDao.updateQueryStatus(queryId, "PROCESSING")).thenReturn(Completable.complete());
        when(masterAgent.executeAllAgents("Montelukast", queryId))
            .thenReturn(Flowable.fromIterable(testAgentUpdates));
        
        // When
        queryRepository.executeAgents(queryId).blockingAwait();
        
        // Then
        verify(queryDao).getQueryById(queryId);
        verify(queryDao).updateQueryStatus(queryId, "PROCESSING");
        verify(masterAgent).executeAllAgents("Montelukast", queryId);
    }
    
    /**
     * Test agent execution with query not found.
     */
    @Test
    public void testExecuteAgents_QueryNotFound() {
        // Given
        long queryId = 999L;
        when(queryDao.getQueryById(queryId)).thenReturn(Single.error(new RuntimeException("Query not found")));
        
        // When & Then
        try {
            queryRepository.executeAgents(queryId).blockingAwait();
            fail("Should throw exception when query not found");
        } catch (Exception e) {
            assertTrue("Should contain query not found error", e.getMessage().contains("Query not found"));
        }
        
        verify(queryDao).getQueryById(queryId);
        verify(masterAgent, never()).executeAllAgents(anyString(), anyLong());
    }
    
    /**
     * Test real-time agent progress monitoring.
     * Requirement 3.4: Real-time progress tracking with Flowable streams
     */
    @Test
    public void testGetAgentProgress() {
        // Given
        long queryId = 1L;
        
        // When
        Flowable<AgentUpdate> progressStream = queryRepository.getAgentProgress(queryId);
        
        // Then
        assertNotNull("Progress stream should not be null", progressStream);
        
        // Test that we can subscribe to the stream
        progressStream.test()
            .assertSubscribed()
            .assertNoErrors();
    }
    
    /**
     * Test getting query by ID.
     */
    @Test
    public void testGetQueryById() {
        // Given
        long queryId = 1L;
        when(queryDao.getQueryById(queryId)).thenReturn(Single.just(testQuery));
        
        // When
        Query result = queryRepository.getQueryById(queryId).blockingGet();
        
        // Then
        assertNotNull("Query should not be null", result);
        assertEquals("Query ID should match", queryId, result.getId());
        assertEquals("Molecule should match", "Montelukast", result.getMolecule());
        verify(queryDao).getQueryById(queryId);
    }
    
    /**
     * Test getting queries by user ID.
     */
    @Test
    public void testGetQueriesByUserId() {
        // Given
        long userId = 1L;
        List<Query> queries = Arrays.asList(testQuery);
        when(queryDao.getQueriesByUserId(userId)).thenReturn(Flowable.just(queries));
        
        // When
        List<Query> result = queryRepository.getQueriesByUserId(userId).blockingFirst();
        
        // Then
        assertNotNull("Queries should not be null", result);
        assertEquals("Should return 1 query", 1, result.size());
        assertEquals("Query should match", testQuery.getId(), result.get(0).getId());
        verify(queryDao).getQueriesByUserId(userId);
    }
    
    /**
     * Test getting recent queries.
     */
    @Test
    public void testGetRecentQueries() {
        // Given
        long userId = 1L;
        int limit = 10;
        List<Query> queries = Arrays.asList(testQuery);
        when(queryDao.getRecentQueriesByUserId(userId, limit)).thenReturn(Flowable.just(queries));
        
        // When
        List<Query> result = queryRepository.getRecentQueries(userId, limit).blockingFirst();
        
        // Then
        assertNotNull("Recent queries should not be null", result);
        assertEquals("Should return 1 query", 1, result.size());
        verify(queryDao).getRecentQueriesByUserId(userId, limit);
    }
    
    /**
     * Test getting queries by status.
     */
    @Test
    public void testGetQueriesByStatus() {
        // Given
        String status = "COMPLETED";
        List<Query> queries = Arrays.asList(testQuery);
        when(queryDao.getQueriesByStatus(status)).thenReturn(Flowable.just(queries));
        
        // When
        List<Query> result = queryRepository.getQueriesByStatus(status).blockingFirst();
        
        // Then
        assertNotNull("Queries should not be null", result);
        assertEquals("Should return 1 query", 1, result.size());
        verify(queryDao).getQueriesByStatus(status);
    }
    
    /**
     * Test getting queries by molecule.
     */
    @Test
    public void testGetQueriesByMolecule() {
        // Given
        String molecule = "Montelukast";
        List<Query> queries = Arrays.asList(testQuery);
        when(queryDao.getQueriesByMolecule(molecule)).thenReturn(Flowable.just(queries));
        
        // When
        List<Query> result = queryRepository.getQueriesByMolecule(molecule).blockingFirst();
        
        // Then
        assertNotNull("Queries should not be null", result);
        assertEquals("Should return 1 query", 1, result.size());
        verify(queryDao).getQueriesByMolecule(molecule);
    }
    
    /**
     * Test searching queries by text.
     */
    @Test
    public void testSearchQueries() {
        // Given
        String searchTerm = "Montelukast";
        List<Query> queries = Arrays.asList(testQuery);
        when(queryDao.searchQueries(searchTerm)).thenReturn(Flowable.just(queries));
        
        // When
        List<Query> result = queryRepository.searchQueries(searchTerm).blockingFirst();
        
        // Then
        assertNotNull("Search results should not be null", result);
        assertEquals("Should return 1 query", 1, result.size());
        verify(queryDao).searchQueries(searchTerm);
    }
    
    /**
     * Test getting query statistics.
     */
    @Test
    public void testGetQueryStatistics() {
        // Given
        when(queryDao.getQueryCount()).thenReturn(Single.just(10));
        when(queryDao.getQueryCountByStatus("COMPLETED")).thenReturn(Single.just(8));
        when(queryDao.getQueryCountByStatus("FAILED")).thenReturn(Single.just(1));
        when(queryDao.getQueryCountByStatus("PROCESSING")).thenReturn(Single.just(1));
        
        // When
        QueryRepository.QueryStatistics stats = queryRepository.getQueryStatistics().blockingGet();
        
        // Then
        assertNotNull("Statistics should not be null", stats);
        assertEquals("Total queries should be 10", 10, stats.getTotalQueries());
        assertEquals("Completed queries should be 8", 8, stats.getCompletedQueries());
        assertEquals("Failed queries should be 1", 1, stats.getFailedQueries());
        assertEquals("Processing queries should be 1", 1, stats.getProcessingQueries());
        assertEquals("Success rate should be 80%", 80.0, stats.getSuccessRate(), 0.1);
        
        verify(queryDao).getQueryCount();
        verify(queryDao).getQueryCountByStatus("COMPLETED");
        verify(queryDao).getQueryCountByStatus("FAILED");
        verify(queryDao).getQueryCountByStatus("PROCESSING");
    }
    
    /**
     * Test getting agent results for a query.
     */
    @Test
    public void testGetAgentResults() {
        // Given
        long queryId = 1L;
        List<AgentResult> results = Arrays.asList(testAgentResult);
        when(agentResultDao.getResultsByQueryId(queryId)).thenReturn(Flowable.just(results));
        
        // When
        List<AgentResult> result = queryRepository.getAgentResults(queryId).blockingFirst();
        
        // Then
        assertNotNull("Agent results should not be null", result);
        assertEquals("Should return 1 result", 1, result.size());
        assertEquals("Result should match", testAgentResult.getId(), result.get(0).getId());
        verify(agentResultDao).getResultsByQueryId(queryId);
    }
    
    /**
     * Test updating query status to completed.
     */
    @Test
    public void testUpdateQueryStatus_Completed() {
        // Given
        long queryId = 1L;
        String status = "COMPLETED";
        when(queryDao.updateQueryStatusAndCompletionTime(eq(queryId), eq(status), anyLong()))
            .thenReturn(Completable.complete());
        
        // When
        queryRepository.updateQueryStatus(queryId, status).blockingAwait();
        
        // Then
        verify(queryDao).updateQueryStatusAndCompletionTime(eq(queryId), eq(status), anyLong());
        verify(queryDao, never()).updateQueryStatus(anyLong(), anyString());
    }
    
    /**
     * Test updating query status to processing.
     */
    @Test
    public void testUpdateQueryStatus_Processing() {
        // Given
        long queryId = 1L;
        String status = "PROCESSING";
        when(queryDao.updateQueryStatus(queryId, status)).thenReturn(Completable.complete());
        
        // When
        queryRepository.updateQueryStatus(queryId, status).blockingAwait();
        
        // Then
        verify(queryDao).updateQueryStatus(queryId, status);
        verify(queryDao, never()).updateQueryStatusAndCompletionTime(anyLong(), anyString(), anyLong());
    }
    
    /**
     * Test deleting query.
     */
    @Test
    public void testDeleteQuery() {
        // Given
        long queryId = 1L;
        when(queryDao.deleteQueryById(queryId)).thenReturn(Completable.complete());
        
        // When
        queryRepository.deleteQuery(queryId).blockingAwait();
        
        // Then
        verify(queryDao).deleteQueryById(queryId);
    }
    
    /**
     * Test molecule extraction from various query formats.
     */
    @Test
    public void testMoleculeExtraction_VariousFormats() {
        // Test different query formats that should extract molecules
        testMoleculeExtractionHelper("What is the market size for Montelukast?", "Montelukast");
        testMoleculeExtractionHelper("Analyze HUMIRA competitive landscape", "Humira");
        testMoleculeExtractionHelper("metformin patent analysis", "Metformin");
        testMoleculeExtractionHelper("GLP-1 agonists market trends", "GLP-1");
        testMoleculeExtractionHelper("Eliquis clinical trials data", "Eliquis");
    }
    
    private void testMoleculeExtractionHelper(String queryText, String expectedMolecule) {
        // Given
        long userId = 1L;
        when(queryDao.insertQuery(any(Query.class))).thenReturn(Single.just(1L));
        
        // When
        queryRepository.createQuery(queryText, userId).blockingGet();
        
        // Then
        verify(queryDao).insertQuery(argThat(query -> 
            query.getMolecule().equals(expectedMolecule)
        ));
        
        // Reset mock for next test
        reset(queryDao);
    }
}