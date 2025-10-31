package com.mit.bodhiq.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;

/**
 * Data Access Object for Query entity operations.
 * Provides CRUD operations and query filtering with RxJava3 return types.
 */
@Dao
public interface QueryDao {
    
    /**
     * Insert a new query into the database.
     * 
     * @param query Query entity to insert
     * @return Single emitting the inserted query's ID
     */
    @Insert
    Single<Long> insertQuery(com.mit.bodhiq.data.database.entity.Query query);
    
    /**
     * Update an existing query in the database.
     * 
     * @param query Query entity with updated information
     * @return Completable indicating operation completion
     */
    @Update
    Completable updateQuery(com.mit.bodhiq.data.database.entity.Query query);
    
    /**
     * Get a query by ID.
     * 
     * @param queryId Query's ID
     * @return Single emitting the Query if found, error if not found
     */
    @androidx.room.Query("SELECT * FROM queries WHERE id = :queryId")
    Single<com.mit.bodhiq.data.database.entity.Query> getQueryById(long queryId);
    
    /**
     * Get all queries for a specific user.
     * 
     * @param userId User's ID
     * @return Flowable emitting list of queries for the user, ordered by creation date
     */
    @androidx.room.Query("SELECT * FROM queries WHERE user_id = :userId ORDER BY created_at DESC")
    Flowable<List<com.mit.bodhiq.data.database.entity.Query>> getQueriesByUserId(long userId);
    
    /**
     * Get recent queries for a user (limited to specified count).
     * 
     * @param userId User's ID
     * @param limit Maximum number of queries to return
     * @return Flowable emitting list of recent queries
     */
    @androidx.room.Query("SELECT * FROM queries WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    Flowable<List<com.mit.bodhiq.data.database.entity.Query>> getRecentQueriesByUserId(long userId, int limit);
    
    /**
     * Get all queries in the system.
     * 
     * @return Flowable emitting list of all queries, ordered by creation date
     */
    @androidx.room.Query("SELECT * FROM queries ORDER BY created_at DESC")
    Flowable<List<com.mit.bodhiq.data.database.entity.Query>> getAllQueries();
    
    /**
     * Get queries by status.
     * 
     * @param status Query status to filter by (PENDING, PROCESSING, COMPLETED, FAILED)
     * @return Flowable emitting list of queries with specified status
     */
    @androidx.room.Query("SELECT * FROM queries WHERE status = :status ORDER BY created_at DESC")
    Flowable<List<com.mit.bodhiq.data.database.entity.Query>> getQueriesByStatus(String status);
    
    /**
     * Get queries by molecule name.
     * 
     * @param molecule Molecule name to filter by
     * @return Flowable emitting list of queries for the specified molecule
     */
    @androidx.room.Query("SELECT * FROM queries WHERE molecule = :molecule ORDER BY created_at DESC")
    Flowable<List<com.mit.bodhiq.data.database.entity.Query>> getQueriesByMolecule(String molecule);
    
    /**
     * Get queries by user and status.
     * 
     * @param userId User's ID
     * @param status Query status
     * @return Flowable emitting list of queries matching both criteria
     */
    @androidx.room.Query("SELECT * FROM queries WHERE user_id = :userId AND status = :status ORDER BY created_at DESC")
    Flowable<List<com.mit.bodhiq.data.database.entity.Query>> getQueriesByUserIdAndStatus(long userId, String status);
    
    /**
     * Get total count of queries in the system.
     * 
     * @return Single emitting the total query count
     */
    @androidx.room.Query("SELECT COUNT(*) FROM queries")
    Single<Integer> getQueryCount();
    
    /**
     * Get count of queries by status.
     * 
     * @param status Query status to count
     * @return Single emitting count of queries with specified status
     */
    @androidx.room.Query("SELECT COUNT(*) FROM queries WHERE status = :status")
    Single<Integer> getQueryCountByStatus(String status);
    
    /**
     * Get count of queries for a specific user.
     * 
     * @param userId User's ID
     * @return Single emitting count of queries for the user
     */
    @androidx.room.Query("SELECT COUNT(*) FROM queries WHERE user_id = :userId")
    Single<Integer> getQueryCountByUserId(long userId);
    
    /**
     * Update query status by ID.
     * 
     * @param queryId Query's ID
     * @param status New status
     * @return Completable indicating operation completion
     */
    @androidx.room.Query("UPDATE queries SET status = :status WHERE id = :queryId")
    Completable updateQueryStatus(long queryId, String status);
    
    /**
     * Update query completion time.
     * 
     * @param queryId Query's ID
     * @param completedAt Completion timestamp
     * @return Completable indicating operation completion
     */
    @androidx.room.Query("UPDATE queries SET completed_at = :completedAt WHERE id = :queryId")
    Completable updateQueryCompletionTime(long queryId, long completedAt);
    
    /**
     * Update query status and completion time.
     * 
     * @param queryId Query's ID
     * @param status New status
     * @param completedAt Completion timestamp
     * @return Completable indicating operation completion
     */
    @androidx.room.Query("UPDATE queries SET status = :status, completed_at = :completedAt WHERE id = :queryId")
    Completable updateQueryStatusAndCompletionTime(long queryId, String status, long completedAt);
    
    /**
     * Delete a query by ID.
     * 
     * @param queryId ID of query to delete
     * @return Completable indicating operation completion
     */
    @androidx.room.Query("DELETE FROM queries WHERE id = :queryId")
    Completable deleteQueryById(long queryId);
    
    /**
     * Search queries by text content (query_text or molecule).
     * 
     * @param searchTerm Search term to match against query text or molecule
     * @return Flowable emitting list of matching queries
     */
    @androidx.room.Query("SELECT * FROM queries WHERE query_text LIKE '%' || :searchTerm || '%' OR molecule LIKE '%' || :searchTerm || '%' ORDER BY created_at DESC")
    Flowable<List<com.mit.bodhiq.data.database.entity.Query>> searchQueries(String searchTerm);
}