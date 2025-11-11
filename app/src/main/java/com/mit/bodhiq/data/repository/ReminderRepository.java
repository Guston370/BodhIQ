package com.mit.bodhiq.data.repository;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import dagger.hilt.android.qualifiers.ApplicationContext;
import com.google.firebase.auth.FirebaseUser;
import com.mit.bodhiq.data.database.dao.ReminderDao;
import com.mit.bodhiq.data.database.dao.ReminderHistoryDao;
import com.mit.bodhiq.data.database.entity.Reminder;
import com.mit.bodhiq.data.database.entity.ReminderHistory;
import com.mit.bodhiq.utils.ReminderScheduler;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Repository for managing reminders
 */
@Singleton
public class ReminderRepository {
    
    private final ReminderDao reminderDao;
    private final ReminderHistoryDao historyDao;
    private final ReminderScheduler scheduler;
    private final FirebaseAuth firebaseAuth;
    
    @Inject
    public ReminderRepository(@ApplicationContext Context context, ReminderDao reminderDao, 
                            ReminderHistoryDao historyDao) {
        this.reminderDao = reminderDao;
        this.historyDao = historyDao;
        this.scheduler = new ReminderScheduler(context);
        this.firebaseAuth = FirebaseAuth.getInstance();
    }
    
    /**
     * Save a reminder and schedule notifications
     */
    public Single<Long> saveReminder(Reminder reminder) {
        return Single.fromCallable(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            
            reminder.setUserId(userId);
            reminder.setLastUpdated(new Date());
            
            return reminder;
        })
        .flatMap(r -> {
            if (r.getId() > 0) {
                // Update existing
                return reminderDao.update(r)
                    .andThen(Single.just(r.getId()));
            } else {
                // Insert new
                return reminderDao.insert(r);
            }
        })
        .doOnSuccess(id -> {
            // Cancel existing alarms before rescheduling
            if (reminder.getId() > 0) {
                scheduler.cancelReminder(reminder.getId());
            }
            
            // Schedule if enabled
            if (reminder.isEnabled()) {
                reminder.setId(id);
                scheduler.scheduleReminder(reminder);
            }
        })
        .subscribeOn(Schedulers.io());
    }
    
    /**
     * Delete a reminder and cancel its notifications
     */
    public Completable deleteReminder(long reminderId) {
        return reminderDao.getById(reminderId)
            .flatMapCompletable(reminder -> {
                // Cancel scheduled alarms
                scheduler.cancelReminder(reminderId);
                
                // Delete from database
                return reminderDao.delete(reminder);
            })
            .subscribeOn(Schedulers.io());
    }
    
    /**
     * Toggle reminder enabled state
     */
    public Completable toggleReminder(long reminderId, boolean enabled) {
        return reminderDao.getById(reminderId)
            .flatMapCompletable(reminder -> {
                if (enabled) {
                    // Schedule
                    scheduler.scheduleReminder(reminder);
                } else {
                    // Cancel
                    scheduler.cancelReminder(reminderId);
                }
                
                return reminderDao.updateEnabled(reminderId, enabled);
            })
            .subscribeOn(Schedulers.io());
    }
    
    /**
     * Get all reminders for current user
     */
    public Flowable<List<Reminder>> getAllReminders() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Flowable.just(List.of());
        }
        return reminderDao.getAllByUserId(userId);
    }
    
    /**
     * Get enabled reminders for current user
     */
    public Flowable<List<Reminder>> getEnabledReminders() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Flowable.just(List.of());
        }
        return reminderDao.getEnabledByUserId(userId);
    }
    
    /**
     * Get reminder by ID
     */
    public Single<Reminder> getReminderById(long id) {
        return reminderDao.getById(id);
    }
    
    /**
     * Record reminder action in history
     */
    public Completable recordAction(long reminderId, String action, Date scheduledTime) {
        return Single.fromCallable(() -> {
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new IllegalStateException("User not authenticated");
            }
            return userId;
        })
        .flatMap(userId -> reminderDao.getById(reminderId)
            .map(reminder -> {
                ReminderHistory history = new ReminderHistory(
                    reminderId,
                    userId,
                    reminder.getMedicineName(),
                    action,
                    scheduledTime,
                    new Date()
                );
                return history;
            }))
        .flatMapCompletable(historyDao::insert)
        .subscribeOn(Schedulers.io());
    }
    
    /**
     * Get recent history
     */
    public Flowable<List<ReminderHistory>> getRecentHistory(int limit) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Flowable.just(List.of());
        }
        return historyDao.getRecentByUserId(userId, limit);
    }
    
    /**
     * Get adherence statistics
     */
    public Single<AdherenceStats> getAdherenceStats(int days) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Single.just(new AdherenceStats(0, 0, 0));
        }
        
        long startDate = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000);
        
        return Single.zip(
            historyDao.getTakenCountSince(userId, startDate),
            historyDao.getTotalCountSince(userId, startDate),
            (taken, total) -> {
                int percentage = total > 0 ? (taken * 100 / total) : 0;
                return new AdherenceStats(taken, total, percentage);
            }
        );
    }
    
    /**
     * Reschedule all enabled reminders (for boot or app start)
     */
    public Completable rescheduleAllReminders() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Completable.complete();
        }
        
        return reminderDao.getEnabledByUserId(userId)
            .firstOrError()
            .flatMapCompletable(reminders -> {
                for (Reminder reminder : reminders) {
                    scheduler.scheduleReminder(reminder);
                }
                return Completable.complete();
            })
            .subscribeOn(Schedulers.io());
    }
    
    private String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    /**
     * Adherence statistics data class
     */
    public static class AdherenceStats {
        public final int takenCount;
        public final int totalCount;
        public final int percentage;
        
        public AdherenceStats(int takenCount, int totalCount, int percentage) {
            this.takenCount = takenCount;
            this.totalCount = totalCount;
            this.percentage = percentage;
        }
    }
}
