package com.mit.bodhiq.di;

import com.mit.bodhiq.data.database.dao.AgentResultDao;
import com.mit.bodhiq.data.database.dao.QueryDao;
import com.mit.bodhiq.data.database.dao.ReportDao;
import com.mit.bodhiq.data.database.dao.UserDao;
import com.mit.bodhiq.data.repository.QueryRepository;
import com.mit.bodhiq.data.repository.ReportRepository;
import com.mit.bodhiq.data.repository.UserRepository;
import com.mit.bodhiq.agent.MasterAgent;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/**
 * Hilt module that provides repository layer dependencies.
 * Repositories serve as the single source of truth for data operations
 * and coordinate between local database and business logic.
 */
@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

    /**
     * Provides UserRepository for user authentication and management operations.
     *
     * @param userDao UserDao for database operations
     * @return UserRepository instance
     */
    @Provides
    @Singleton
    public UserRepository provideUserRepository(UserDao userDao) {
        return new UserRepository(userDao);
    }

    /**
     * Provides QueryRepository for query creation and agent execution coordination.
     *
     * @param queryDao QueryDao for query database operations
     * @param agentResultDao AgentResultDao for storing agent results
     * @param masterAgent MasterAgent for orchestrating agent execution
     * @return QueryRepository instance
     */
    @Provides
    @Singleton
    public QueryRepository provideQueryRepository(
            QueryDao queryDao,
            AgentResultDao agentResultDao,
            MasterAgent masterAgent
    ) {
        return new QueryRepository(queryDao, agentResultDao, masterAgent);
    }

    /**
     * Provides ReportRepository for report management.
     *
     * @param reportDao ReportDao for report database operations
     * @return ReportRepository instance
     */
    @Provides
    @Singleton
    public ReportRepository provideReportRepository(ReportDao reportDao) {
        return new ReportRepository(reportDao);
    }
}