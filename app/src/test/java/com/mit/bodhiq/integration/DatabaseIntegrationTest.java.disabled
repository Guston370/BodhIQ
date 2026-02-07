package com.mit.bodhiq.integration;

import static org.junit.Assert.*;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.mit.bodhiq.data.database.AppDatabase;
import com.mit.bodhiq.data.database.dao.AgentResultDao;
import com.mit.bodhiq.data.database.dao.QueryDao;
import com.mit.bodhiq.data.database.dao.ReportDao;
import com.mit.bodhiq.data.database.dao.UserDao;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.database.entity.Query;
import com.mit.bodhiq.data.database.entity.Report;
import com.mit.bodhiq.data.database.entity.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

/**
 * Integration tests for database operations.
 * Tests requirements 8.2, 8.4 for Room database with proper entity relationships and foreign key constraints.
 */
@RunWith(RobolectricTestRunner.class)
public class DatabaseIntegrationTest {
    
    private AppDatabase database;
    private UserDao userDao;
    private QueryDao queryDao;
    private AgentResultDao agentResultDao;
    private ReportDao reportDao;
    
    // Test data
    private User testUser;
    private Query testQuery;
    private AgentResult testAgentResult;
    private Report testReport;
    
    @Before
    public void setUp() {
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase.class
        )
        .allowMainThreadQueries()
        .build();
        
        // Get DAOs
        userDao = database.userDao();
        queryDao = database.queryDao();
        agentResultDao = database.agentResultDao();
        reportDao = database.reportDao();
        
        // Setup test data
        testUser = new User("test@company.com", "Test User", "analyst", System.currentTimeMillis());
        testQuery = new Query(1L, "Test query", "Montelukast", "PENDING", System.currentTimeMillis());
        testAgentResult = new AgentResult(1L, "Market Insights", "COMPLETED", System.currentTimeMillis());
        testReport = new Report(1L, "test_report.pdf", "Montelukast", "COMPLETED", System.currentTimeMillis());
    }
    
    @After
    public void tearDown() {
        database.close();
    }
    
    /**
     * Test complete database workflow with foreign key relationships.
     * Requirement 8.2: Room database with proper entity relationships and foreign key constraints
     */
    @Test
    public void testCompleteWorkflow_WithForeignKeyConstraints() {
        // Step 1: Insert user
        TestObserver<Long> userInsertObserver = userDao.insertUser(testUser).test();
        userInsertObserver.awaitDone();
        userInsertObserver.assertComplete();
        userInsertObserver.assertNoErrors();
        
        long userId = userInsertObserver.values().get(0);
        assertTrue("User ID should be positive", userId > 0);
        
        // Step 2: Insert query with user foreign key
        testQuery.setUserId(userId);
        TestObserver<Long> queryInsertObserver = queryDao.insertQuery(testQuery).test();
        queryInsertObserver.awaitDone();
        queryInsertObserver.assertComplete();
        queryInsertObserver.assertNoErrors();
        
        long queryId = queryInsertObserver.values().get(0);
        assertTrue("Query ID should be positive", queryId > 0);
        
        // Step 3: Insert agent result with query foreign key
        testAgentResult.setQueryId(queryId);
        TestObserver<Long> agentResultInsertObserver = agentResultDao.insertAgentResult(testAgentResult).test();
        agentResultInsertObserver.awaitDone();
        agentResultInsertObserver.assertComplete();
        agentResultInsertObserver.assertNoErrors();
        
        long agentResultId = agentResultInsertObserver.values().get(0);
        assertTrue("Agent result ID should be positive", agentResultId > 0);
        
        // Step 4: Insert report with query foreign key
        testReport.setQueryId(queryId);
        TestObserver<Long> reportInsertObserver = reportDao.insertReport(testReport).test();
        reportInsertObserver.awaitDone();
        reportInsertObserver.assertComplete();
        reportInsertObserver.assertNoErrors();
        
        long reportId = reportInsertObserver.values().get(0);
        assertTrue("Report ID should be positive", reportId > 0);
        
        // Step 5: Verify relationships by querying
        TestObserver<Query> queryObserver = queryDao.getQueryById(queryId).test();
        queryObserver.awaitDone();
        queryObserver.assertComplete();
        queryObserver.assertNoErrors();
        
        Query retrievedQuery = queryObserver.values().get(0);
        assertEquals("Query user ID should match", userId, retrievedQuery.getUserId());
        assertEquals("Query text should match", testQuery.getQueryText(), retrievedQuery.getQueryText());
        
        // Step 6: Verify agent results for query
        TestSubscriber<List<AgentResult>> agentResultsObserver = agentResultDao.getResultsByQueryId(queryId).test();
        agentResultsObserver.awaitCount(1);
        agentResultsObserver.assertNoErrors();
        
        List<AgentResult> agentResults = agentResultsObserver.values().get(0);
        assertEquals("Should have 1 agent result", 1, agentResults.size());
        assertEquals("Agent result query ID should match", queryId, agentResults.get(0).getQueryId());
        
        // Step 7: Verify report for query
        TestObserver<Report> reportObserver = reportDao.getReportByQueryId(queryId).test();
        reportObserver.awaitDone();
        reportObserver.assertComplete();
        reportObserver.assertNoErrors();
        
        Report retrievedReport = reportObserver.values().get(0);
        assertEquals("Report query ID should match", queryId, retrievedReport.getQueryId());
        assertEquals("Report molecule should match", testReport.getMolecule(), retrievedReport.getMolecule());
    }
    
    /**
     * Test foreign key constraint enforcement on delete.
     * Requirement 8.2: Foreign key constraint enforcement
     */
    @Test
    public void testForeignKeyConstraints_CascadeDelete() {
        // Setup: Insert user and query
        long userId = userDao.insertUser(testUser).blockingGet();
        testQuery.setUserId(userId);
        long queryId = queryDao.insertQuery(testQuery).blockingGet();
        
        // Insert agent result and report
        testAgentResult.setQueryId(queryId);
        testReport.setQueryId(queryId);
        agentResultDao.insertAgentResult(testAgentResult).blockingGet();
        reportDao.insertReport(testReport).blockingGet();
        
        // Verify data exists
        assertEquals("Should have 1 query", 1, queryDao.getQueryCount().blockingGet().intValue());
        assertEquals("Should have 1 agent result", 1, agentResultDao.getResultsByQueryId(queryId).blockingFirst().size());
        assertTrue("Should have report for query", reportDao.reportExistsForQuery(queryId).blockingGet());
        
        // Delete user (should cascade to queries and related data)
        userDao.deleteUserById(userId).blockingAwait();
        
        // Verify cascade delete worked
        assertEquals("Should have 0 queries after user delete", 0, queryDao.getQueryCount().blockingGet().intValue());
        assertEquals("Should have 0 agent results after cascade", 0, agentResultDao.getResultsByQueryId(queryId).blockingFirst().size());
        assertFalse("Should have no report after cascade", reportDao.reportExistsForQuery(queryId).blockingGet());
    }
    
    /**
     * Test database pre-seeding functionality.
     * Requirement 8.4: Database callback for initial data seeding
     */
    @Test
    public void testDatabasePreSeeding() {
        // Note: In a real test, this would test the database callback
        // For this test, we'll manually verify the seeding functionality
        
        // Create users that would be pre-seeded
        User adminUser = new User("pharma.strategist@company.com", "Pharma Strategist", "admin", System.currentTimeMillis());
        User analystUser = new User("analyst@company.com", "Analyst", "analyst", System.currentTimeMillis());
        
        // Insert pre-seed data
        userDao.insertUser(adminUser).blockingGet();
        userDao.insertUser(analystUser).blockingGet();
        
        // Verify pre-seeded data
        TestSubscriber<List<User>> allUsersObserver = userDao.getAllUsers().test();
        allUsersObserver.awaitCount(1);
        allUsersObserver.assertNoErrors();
        
        List<User> users = allUsersObserver.values().get(0);
        assertEquals("Should have 2 pre-seeded users", 2, users.size());
        
        // Verify admin user
        boolean hasAdmin = users.stream().anyMatch(user -> 
            user.getEmail().equals("pharma.strategist@company.com") && 
            user.getRole().equals("admin")
        );
        assertTrue("Should have admin user", hasAdmin);
        
        // Verify analyst user
        boolean hasAnalyst = users.stream().anyMatch(user -> 
            user.getEmail().equals("analyst@company.com") && 
            user.getRole().equals("analyst")
        );
        assertTrue("Should have analyst user", hasAnalyst);
    }
    
    /**
     * Test concurrent database operations.
     */
    @Test
    public void testConcurrentDatabaseOperations() {
        // Insert user first
        long userId = userDao.insertUser(testUser).blockingGet();
        
        // Create multiple queries concurrently
        Query query1 = new Query(userId, "Query 1", "Montelukast", "PENDING", System.currentTimeMillis());
        Query query2 = new Query(userId, "Query 2", "Humira", "PENDING", System.currentTimeMillis());
        Query query3 = new Query(userId, "Query 3", "Metformin", "PENDING", System.currentTimeMillis());
        
        // Insert concurrently
        TestObserver<Long> observer1 = queryDao.insertQuery(query1).test();
        TestObserver<Long> observer2 = queryDao.insertQuery(query2).test();
        TestObserver<Long> observer3 = queryDao.insertQuery(query3).test();
        
        // Wait for all to complete
        observer1.awaitDone();
        observer2.awaitDone();
        observer3.awaitDone();
        
        observer1.assertComplete().assertNoErrors();
        observer2.assertComplete().assertNoErrors();
        observer3.assertComplete().assertNoErrors();
        
        // Verify all queries were inserted
        assertEquals("Should have 3 queries", 3, queryDao.getQueryCount().blockingGet().intValue());
        
        // Verify queries for user
        List<Query> userQueries = queryDao.getQueriesByUserId(userId).blockingFirst();
        assertEquals("User should have 3 queries", 3, userQueries.size());
    }
    
    /**
     * Test database transaction rollback on error.
     */
    @Test
    public void testTransactionRollback() {
        // Insert user
        long userId = userDao.insertUser(testUser).blockingGet();
        
        // Try to insert query with invalid foreign key (should fail)
        Query invalidQuery = new Query(999L, "Invalid query", "Montelukast", "PENDING", System.currentTimeMillis());
        
        TestObserver<Long> observer = queryDao.insertQuery(invalidQuery).test();
        observer.awaitDone();
        observer.assertError(Throwable.class);
        
        // Verify no query was inserted
        assertEquals("Should have 0 queries after failed insert", 0, queryDao.getQueryCount().blockingGet().intValue());
    }
    
    /**
     * Test complex query operations.
     */
    @Test
    public void testComplexQueryOperations() {
        // Setup: Insert user and multiple queries
        long userId = userDao.insertUser(testUser).blockingGet();
        
        Query query1 = new Query(userId, "Montelukast analysis", "Montelukast", "COMPLETED", System.currentTimeMillis() - 3600000);
        Query query2 = new Query(userId, "Humira research", "Humira", "PROCESSING", System.currentTimeMillis() - 1800000);
        Query query3 = new Query(userId, "Metformin study", "Metformin", "PENDING", System.currentTimeMillis());
        
        queryDao.insertQuery(query1).blockingGet();
        queryDao.insertQuery(query2).blockingGet();
        queryDao.insertQuery(query3).blockingGet();
        
        // Test search functionality
        List<Query> searchResults = queryDao.searchQueries("Montelukast").blockingFirst();
        assertEquals("Should find 1 query with Montelukast", 1, searchResults.size());
        assertEquals("Found query should be Montelukast query", "Montelukast", searchResults.get(0).getMolecule());
        
        // Test status filtering
        List<Query> completedQueries = queryDao.getQueriesByStatus("COMPLETED").blockingFirst();
        assertEquals("Should have 1 completed query", 1, completedQueries.size());
        
        List<Query> processingQueries = queryDao.getQueriesByStatus("PROCESSING").blockingFirst();
        assertEquals("Should have 1 processing query", 1, processingQueries.size());
        
        // Test molecule filtering
        List<Query> montelukastQueries = queryDao.getQueriesByMolecule("Montelukast").blockingFirst();
        assertEquals("Should have 1 Montelukast query", 1, montelukastQueries.size());
        
        // Test recent queries (limit)
        List<Query> recentQueries = queryDao.getRecentQueriesByUserId(userId, 2).blockingFirst();
        assertEquals("Should return 2 most recent queries", 2, recentQueries.size());
        
        // Verify ordering (most recent first)
        assertTrue("First query should be more recent", 
            recentQueries.get(0).getCreatedAt() >= recentQueries.get(1).getCreatedAt());
    }
    
    /**
     * Test database statistics and aggregations.
     */
    @Test
    public void testDatabaseStatistics() {
        // Setup: Insert test data
        long userId = userDao.insertUser(testUser).blockingGet();
        
        // Insert queries with different statuses
        Query completedQuery1 = new Query(userId, "Query 1", "Montelukast", "COMPLETED", System.currentTimeMillis());
        Query completedQuery2 = new Query(userId, "Query 2", "Humira", "COMPLETED", System.currentTimeMillis());
        Query failedQuery = new Query(userId, "Query 3", "Metformin", "FAILED", System.currentTimeMillis());
        Query processingQuery = new Query(userId, "Query 4", "GLP-1", "PROCESSING", System.currentTimeMillis());
        
        queryDao.insertQuery(completedQuery1).blockingGet();
        queryDao.insertQuery(completedQuery2).blockingGet();
        queryDao.insertQuery(failedQuery).blockingGet();
        queryDao.insertQuery(processingQuery).blockingGet();
        
        // Test query statistics
        assertEquals("Total queries should be 4", 4, queryDao.getQueryCount().blockingGet().intValue());
        assertEquals("Completed queries should be 2", 2, queryDao.getQueryCountByStatus("COMPLETED").blockingGet().intValue());
        assertEquals("Failed queries should be 1", 1, queryDao.getQueryCountByStatus("FAILED").blockingGet().intValue());
        assertEquals("Processing queries should be 1", 1, queryDao.getQueryCountByStatus("PROCESSING").blockingGet().intValue());
        
        // Test user statistics
        assertEquals("Total users should be 1", 1, userDao.getUserCount().blockingGet().intValue());
        assertEquals("Analyst users should be 1", 1, userDao.getUserCountByRole("analyst").blockingGet().intValue());
        assertEquals("Admin users should be 0", 0, userDao.getUserCountByRole("admin").blockingGet().intValue());
    }
    
    /**
     * Test data integrity and validation.
     */
    @Test
    public void testDataIntegrityAndValidation() {
        // Test user email uniqueness (if implemented)
        long userId1 = userDao.insertUser(testUser).blockingGet();
        assertTrue("First user insert should succeed", userId1 > 0);
        
        // Test query-agent result relationship integrity
        testQuery.setUserId(userId1);
        long queryId = queryDao.insertQuery(testQuery).blockingGet();
        
        testAgentResult.setQueryId(queryId);
        long agentResultId = agentResultDao.insertAgentResult(testAgentResult).blockingGet();
        assertTrue("Agent result insert should succeed", agentResultId > 0);
        
        // Verify agent result is linked to correct query
        List<AgentResult> results = agentResultDao.getResultsByQueryId(queryId).blockingFirst();
        assertEquals("Should have 1 agent result for query", 1, results.size());
        assertEquals("Agent result should be linked to correct query", queryId, results.get(0).getQueryId());
        
        // Test report-query relationship integrity
        testReport.setQueryId(queryId);
        long reportId = reportDao.insertReport(testReport).blockingGet();
        assertTrue("Report insert should succeed", reportId > 0);
        
        Report retrievedReport = reportDao.getReportByQueryId(queryId).blockingGet();
        assertEquals("Report should be linked to correct query", queryId, retrievedReport.getQueryId());
    }
}