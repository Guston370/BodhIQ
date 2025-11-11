package com.mit.bodhiq.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.mit.bodhiq.data.database.entity.Reminder;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * DAO for Reminder entity
 */
@Dao
public interface ReminderDao {
    
    @Insert
    Single<Long> insert(Reminder reminder);
    
    @Update
    Completable update(Reminder reminder);
    
    @Delete
    Completable delete(Reminder reminder);
    
    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY lastUpdated DESC")
    Flowable<List<Reminder>> getAllByUserId(String userId);
    
    @Query("SELECT * FROM reminders WHERE id = :id")
    Single<Reminder> getById(long id);
    
    @Query("SELECT * FROM reminders WHERE userId = :userId AND enabled = 1")
    Flowable<List<Reminder>> getEnabledByUserId(String userId);
    
    @Query("UPDATE reminders SET enabled = :enabled WHERE id = :id")
    Completable updateEnabled(long id, boolean enabled);
    
    @Query("DELETE FROM reminders WHERE userId = :userId")
    Completable deleteAllByUserId(String userId);
}
