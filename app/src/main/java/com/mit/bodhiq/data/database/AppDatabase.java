package com.mit.bodhiq.data.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.mit.bodhiq.data.database.dao.AgentResultDao;
import com.mit.bodhiq.data.database.dao.QueryDao;
import com.mit.bodhiq.data.database.dao.ReportDao;
import com.mit.bodhiq.data.database.dao.UserDao;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.database.entity.Query;
import com.mit.bodhiq.data.database.entity.Report;
import com.mit.bodhiq.data.database.entity.User;
import java.util.concurrent.Executors;

/**
 * Room database class for the BodhIQ application.
 * Manages all entities and provides access to DAOs with pre-seeding capability.
 */
@Database(
    entities = {
        User.class,
        Query.class,
        AgentResult.class,
        Report.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    // Abstract methods to get DAOs
    public abstract UserDao userDao();
    public abstract QueryDao queryDao();
    public abstract AgentResultDao agentResultDao();
    public abstract ReportDao reportDao();
    
    /**
     * Database callback for pre-seeding initial data.
     * This callback is executed when the database is created for the first time.
     */
    private static final RoomDatabase.Callback DATABASE_CALLBACK = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            
            // Pre-seed the database with initial users on a background thread
            Executors.newSingleThreadExecutor().execute(() -> {
                seedInitialData(db);
            });
        }
    };
    
    /**
     * Seeds the database with initial data including predefined users.
     * 
     * @param db SupportSQLiteDatabase instance for executing raw SQL
     */
    private static void seedInitialData(SupportSQLiteDatabase db) {
        long currentTime = System.currentTimeMillis();
        
        // Insert predefined users as specified in requirements
        // User 1: Admin user
        db.execSQL("INSERT INTO users (email, name, role, created_at) VALUES (?, ?, ?, ?)",
                new Object[]{"pharma.strategist@company.com", "Pharma Strategist", "admin", currentTime});
        
        // User 2: Analyst user  
        db.execSQL("INSERT INTO users (email, name, role, created_at) VALUES (?, ?, ?, ?)",
                new Object[]{"analyst@company.com", "Research Analyst", "analyst", currentTime});
        
        // Note: Additional seed data for queries, agent results, and reports can be added here
        // if needed for testing or demo purposes
    }
    
    /**
     * Returns the database callback for pre-seeding.
     * This method is used by the DatabaseModule to configure the database builder.
     * 
     * @return RoomDatabase.Callback for database initialization
     */
    public static RoomDatabase.Callback getDatabaseCallback() {
        return DATABASE_CALLBACK;
    }
}