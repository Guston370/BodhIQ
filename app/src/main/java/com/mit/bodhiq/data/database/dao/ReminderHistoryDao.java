package com.mit.bodhiq.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.mit.bodhiq.data.database.entity.ReminderHistory;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * DAO for ReminderHistory entity
 */
@Dao
public interface ReminderHistoryDao {
    
    @Insert
    Completable insert(ReminderHistory history);
    
    @Query("SELECT * FROM reminder_history WHERE userId = :userId ORDER BY actionTime DESC LIMIT :limit")
    Flowable<List<ReminderHistory>> getRecentByUserId(String userId, int limit);
    
    @Query("SELECT * FROM reminder_history WHERE userId = :userId AND actionTime >= :startDate AND actionTime <= :endDate ORDER BY actionTime DESC")
    Flowable<List<ReminderHistory>> getByUserIdAndDateRange(String userId, long startDate, long endDate);
    
    @Query("SELECT COUNT(*) FROM reminder_history WHERE userId = :userId AND action = 'TAKEN' AND actionTime >= :startDate")
    Single<Integer> getTakenCountSince(String userId, long startDate);
    
    @Query("SELECT COUNT(*) FROM reminder_history WHERE userId = :userId AND actionTime >= :startDate")
    Single<Integer> getTotalCountSince(String userId, long startDate);
    
    @Query("SELECT * FROM reminder_history WHERE reminderId = :reminderId ORDER BY actionTime DESC")
    Flowable<List<ReminderHistory>> getByReminderId(long reminderId);
    
    @Query("DELETE FROM reminder_history WHERE userId = :userId")
    Completable deleteAllByUserId(String userId);
}
