package com.mit.bodhiq.di;

import android.content.Context;
import androidx.room.Room;
import com.mit.bodhiq.data.database.AppDatabase;
import com.mit.bodhiq.data.database.dao.AgentResultDao;
import com.mit.bodhiq.data.database.dao.QueryDao;
import com.mit.bodhiq.data.database.dao.ReportDao;
import com.mit.bodhiq.data.database.dao.UserDao;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/**
 * Hilt module that provides database-related dependencies.
 * This module is installed in the SingletonComponent to ensure
 * database instances are application-scoped singletons.
 */
@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    /**
     * Provides the main Room database instance.
     * The database is created with pre-seeding capability through a callback.
     *
     * @param context Application context
     * @return AppDatabase instance
     */
    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                "bodhiq_database"
        )
        .addCallback(AppDatabase.getDatabaseCallback())
        .build();
    }

    /**
     * Provides UserDao for user-related database operations.
     *
     * @param database AppDatabase instance
     * @return UserDao instance
     */
    @Provides
    public UserDao provideUserDao(AppDatabase database) {
        return database.userDao();
    }

    /**
     * Provides QueryDao for query-related database operations.
     *
     * @param database AppDatabase instance
     * @return QueryDao instance
     */
    @Provides
    public QueryDao provideQueryDao(AppDatabase database) {
        return database.queryDao();
    }

    /**
     * Provides AgentResultDao for agent result database operations.
     *
     * @param database AppDatabase instance
     * @return AgentResultDao instance
     */
    @Provides
    public AgentResultDao provideAgentResultDao(AppDatabase database) {
        return database.agentResultDao();
    }

    /**
     * Provides ReportDao for report-related database operations.
     *
     * @param database AppDatabase instance
     * @return ReportDao instance
     */
    @Provides
    public ReportDao provideReportDao(AppDatabase database) {
        return database.reportDao();
    }
}