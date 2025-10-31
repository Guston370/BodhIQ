package com.mit.bodhiq.data.repository;

import com.mit.bodhiq.agent.MasterAgent;
import com.mit.bodhiq.data.database.dao.AgentResultDao;
import com.mit.bodhiq.data.database.dao.QueryDao;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.database.entity.Query;
import com.mit.bodhiq.data.model.AgentStatus;
import com.mit.bodhiq.data.model.AgentUpdate;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository for query creation and agent execution management.
 * Provides query lifecycle management and real-time agent progress tracking.
 * Implements requirements 3.1, 3.2, 3.4 for query processing and agent orchestration.
 */
@Singleton
public class QueryRepository {
    
    private final QueryDao queryDao;
    private final AgentResultDao agentResultDao;
    private final MasterAgent masterAgent;
    
    // Map to track agent progress for active queries
    private final ConcurrentHashMap<Long, BehaviorSubject<AgentUpdate>> progressSubjects;
    
    // Supported molecules for query processing
    private static final String[] SUPPORTED_MOLECULES = {
        "Montelukast", "Humira", "Metformin", "GLP-1", "Eliquis"
    };
    
    @Inject
    public QueryRepository(QueryDao queryDao, AgentResultDao agentResultDao, MasterAgent masterAgent) {
        this.queryDao = queryDao;
        this.agentResultDao = agentResultDao;
        this.masterAgent = masterAgent;
        this.progressSubjects = new ConcurrentHashMap<>();
    }
    
    /**
     * Create a new pharmaceutical query.
     * Requirement 3.1: Extract molecule name and create Query entity
     * 
     * @param queryText User's query text
     * @param userId ID of the user creating the query
     * @return Single emitting the created query's ID
     */
    public Single<Long> createQuery(String queryText, long userId) {
        return Single.fromCallable(() -> {
            String molecule = extractMoleculeFromQuery(queryText);
            if (molecule == null) {
                throw new IllegalArgumentException("No supported molecule found in query: " + queryText);
            }
            
            Query query = new Query(userId, queryText, molecule, "PENDING", System.currentTimeMillis());
            return query;
        })
        .flatMap(queryDao::insertQuery)
        .subscribeOn(Schedulers.io());
    }
    
    /**
     * Create query with explicit molecule specification.
     * 
     * @param queryText User's query text
     * @param molecule Molecule name
     * @param userId ID of the user creating the query
     * @return Single emitting the created query's ID
     */
    public Single<Long> createQuery(String queryText, String molecule, long userId) {
        if (!isSupportedMolecule(molecule)) {
            return Single.error(new IllegalArgumentException("Unsupported molecule: " + molecule));
        }
        
        Query query = new Query(userId, queryText, molecule, "PENDING", System.currentTimeMillis());
        return queryDao.insertQuery(query)
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Execute agents for a query with real-time progress tracking.
     * Requirement 3.2: Sequential agent execution with progress updates
     * 
     * @param queryId ID of the query to process
     * @return Completable indicating execution completion
     */
    public Completable executeAgents(long queryId) {
        return queryDao.getQueryById(queryId)
                .flatMapCompletable(query -> {
                    // Update query status to PROCESSING
                    return updateQueryStatus(queryId, "PROCESSING")
                            .andThen(executeAgentsInternal(queryId, query.getMolecule()));
                })
                .subscribeOn(Schedulers.io());
    }
    
    /**
     * Get real-time agent progress updates for a query.
     * Requirement 3.4: Real-time progress tracking with Flowable streams
     * 
     * @param queryId ID of the query to monitor
     * @return Flowable emitting AgentUpdate objects with progress information
     */
    public Flowable<AgentUpdate> getAgentProgress(long queryId) {
        return Flowable.defer(() -> {
            BehaviorSubject<AgentUpdate> subject = progressSubjects.computeIfAbsent(
                queryId, 
                k -> BehaviorSubject.create()
            );
            return subject.toFlowable(io.reactivex.rxjava3.core.BackpressureStrategy.BUFFER);
        });
    }
    
    /**
     * Get query by ID.
     * 
     * @param queryId Query's ID
     * @return Single emitting Query entity
     */
    public Single<Query> getQueryById(long queryId) {
        return queryDao.getQueryById(queryId);
    }
    
    /**
     * Get all queries for a user.
     * 
     * @param userId User's ID
     * @return Flowable emitting list of user's queries
     */
    public Flowable<List<Query>> getQueriesByUserId(long userId) {
        return queryDao.getQueriesByUserId(userId);
    }
    
    /**
     * Get recent queries for a user.
     * 
     * @param userId User's ID
     * @param limit Maximum number of queries to return
     * @return Flowable emitting list of recent queries
     */
    public Flowable<List<Query>> getRecentQueries(long userId, int limit) {
        return queryDao.getRecentQueriesByUserId(userId, limit);
    }
    
    /**
     * Get all queries in the system.
     * 
     * @return Flowable emitting list of all queries
     */
    public Flowable<List<Query>> getAllQueries() {
        return queryDao.getAllQueries();
    }
    
    /**
     * Get queries by status.
     * 
     * @param status Query status to filter by
     * @return Flowable emitting list of queries with specified status
     */
    public Flowable<List<Query>> getQueriesByStatus(String status) {
        return queryDao.getQueriesByStatus(status);
    }
    
    /**
     * Get queries by molecule.
     * 
     * @param molecule Molecule name to filter by
     * @return Flowable emitting list of queries for the molecule
     */
    public Flowable<List<Query>> getQueriesByMolecule(String molecule) {
        return queryDao.getQueriesByMolecule(molecule);
    }
    
    /**
     * Search queries by text content.
     * 
     * @param searchTerm Search term to match
     * @return Flowable emitting list of matching queries
     */
    public Flowable<List<Query>> searchQueries(String searchTerm) {
        return queryDao.searchQueries(searchTerm);
    }
    
    /**
     * Get query statistics.
     * 
     * @return Single emitting query count statistics
     */
    public Single<QueryStatistics> getQueryStatistics() {
        return Single.zip(
            queryDao.getQueryCount(),
            queryDao.getQueryCountByStatus("COMPLETED"),
            queryDao.getQueryCountByStatus("FAILED"),
            queryDao.getQueryCountByStatus("PROCESSING"),
            (total, completed, failed, processing) -> 
                new QueryStatistics(total, completed, failed, processing)
        );
    }
    
    /**
     * Get agent results for a query.
     * 
     * @param queryId Query's ID
     * @return Flowable emitting list of agent results
     */
    public Flowable<List<AgentResult>> getAgentResults(long queryId) {
        return agentResultDao.getAgentResultsByQueryId(queryId);
    }
    
    /**
     * Update query status.
     * 
     * @param queryId Query's ID
     * @param status New status
     * @return Completable indicating operation completion
     */
    public Completable updateQueryStatus(long queryId, String status) {
        if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
            return queryDao.updateQueryStatusAndCompletionTime(queryId, status, System.currentTimeMillis());
        } else {
            return queryDao.updateQueryStatus(queryId, status);
        }
    }
    
    /**
     * Delete query by ID.
     * 
     * @param queryId ID of query to delete
     * @return Completable indicating operation completion
     */
    public Completable deleteQuery(long queryId) {
        return queryDao.deleteQueryById(queryId)
                .doOnComplete(() -> {
                    // Clean up progress subject
                    BehaviorSubject<AgentUpdate> subject = progressSubjects.remove(queryId);
                    if (subject != null) {
                        subject.onComplete();
                    }
                });
    }
    
    // Private helper methods
    
    /**
     * Internal method to execute agents with progress tracking.
     */
    private Completable executeAgentsInternal(long queryId, String molecule) {
        BehaviorSubject<AgentUpdate> progressSubject = progressSubjects.computeIfAbsent(
            queryId, 
            k -> BehaviorSubject.create()
        );
        
        return masterAgent.executeAllAgents(molecule, queryId)
                .doOnNext(progressSubject::onNext)
                .doOnComplete(() -> {
                    // Update query status to COMPLETED
                    updateQueryStatus(queryId, "COMPLETED").subscribe();
                    progressSubject.onComplete();
                    progressSubjects.remove(queryId);
                })
                .doOnError(throwable -> {
                    // Update query status to FAILED
                    updateQueryStatus(queryId, "FAILED").subscribe();
                    
                    // Send error update
                    AgentUpdate errorUpdate = new AgentUpdate("System", AgentStatus.FAILED, 0);
                    errorUpdate.setError(throwable.getMessage());
                    progressSubject.onNext(errorUpdate);
                    progressSubject.onError(throwable);
                    progressSubjects.remove(queryId);
                })
                .ignoreElements();
    }
    
    /**
     * Extract molecule name from query text using pattern matching.
     */
    private String extractMoleculeFromQuery(String queryText) {
        if (queryText == null || queryText.trim().isEmpty()) {
            return null;
        }
        
        String lowerQuery = queryText.toLowerCase();
        
        // Check for each supported molecule
        for (String molecule : SUPPORTED_MOLECULES) {
            String lowerMolecule = molecule.toLowerCase();
            
            // Direct match
            if (lowerQuery.contains(lowerMolecule)) {
                return molecule;
            }
            
            // Handle special cases
            if ("GLP-1".equals(molecule) && (lowerQuery.contains("glp-1") || lowerQuery.contains("glp1") || lowerQuery.contains("glucagon"))) {
                return molecule;
            }
        }
        
        return null;
    }
    
    /**
     * Check if molecule is supported.
     */
    private boolean isSupportedMolecule(String molecule) {
        if (molecule == null) return false;
        
        for (String supported : SUPPORTED_MOLECULES) {
            if (supported.equalsIgnoreCase(molecule)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * POJO for query statistics.
     */
    public static class QueryStatistics {
        private final int totalQueries;
        private final int completedQueries;
        private final int failedQueries;
        private final int processingQueries;
        
        public QueryStatistics(int totalQueries, int completedQueries, int failedQueries, int processingQueries) {
            this.totalQueries = totalQueries;
            this.completedQueries = completedQueries;
            this.failedQueries = failedQueries;
            this.processingQueries = processingQueries;
        }
        
        public int getTotalQueries() { return totalQueries; }
        public int getCompletedQueries() { return completedQueries; }
        public int getFailedQueries() { return failedQueries; }
        public int getProcessingQueries() { return processingQueries; }
        
        public double getSuccessRate() {
            return totalQueries > 0 ? (double) completedQueries / totalQueries * 100 : 0.0;
        }
    }
}