package com.mit.bodhiq.di;

import com.mit.bodhiq.agent.MasterAgent;
import com.mit.bodhiq.agent.ClinicalTrialsAgent;
import com.mit.bodhiq.agent.EximTradeAgent;
import com.mit.bodhiq.agent.InternalInsightsAgent;
import com.mit.bodhiq.agent.MarketInsightsAgent;
import com.mit.bodhiq.agent.PatentLandscapeAgent;
import com.mit.bodhiq.agent.ReportGeneratorAgent;
import com.mit.bodhiq.agent.WebIntelligenceAgent;
import com.mit.bodhiq.data.database.dao.AgentResultDao;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/**
 * Hilt module that provides agent-related dependencies.
 * This module configures the pharmaceutical analysis agents and their orchestration.
 */
@Module
@InstallIn(SingletonComponent.class)
public class AgentModule {

    // Individual agents are provided via @Inject constructors
    // No need for explicit @Provides methods since they use constructor injection

    /**
     * Provides MasterAgent for orchestrating sequential agent execution.
     * Registers all agents with the master agent after creation.
     *
     * @param agentResultDao AgentResultDao for database operations
     * @param marketInsightsAgent MarketInsightsAgent instance
     * @param patentLandscapeAgent PatentLandscapeAgent instance
     * @param clinicalTrialsAgent ClinicalTrialsAgent instance
     * @param eximTradeAgent EximTradeAgent instance
     * @param webIntelligenceAgent WebIntelligenceAgent instance
     * @param internalInsightsAgent InternalInsightsAgent instance
     * @param reportGeneratorAgent ReportGeneratorAgent instance
     * @return MasterAgent instance
     */
    @Provides
    @Singleton
    public MasterAgent provideMasterAgent(
            AgentResultDao agentResultDao,
            MarketInsightsAgent marketInsightsAgent,
            PatentLandscapeAgent patentLandscapeAgent,
            ClinicalTrialsAgent clinicalTrialsAgent,
            EximTradeAgent eximTradeAgent,
            WebIntelligenceAgent webIntelligenceAgent,
            InternalInsightsAgent internalInsightsAgent,
            ReportGeneratorAgent reportGeneratorAgent
    ) {
        MasterAgent masterAgent = new MasterAgent(agentResultDao);
        
        // Register all agents with the master agent
        masterAgent.registerAgent(marketInsightsAgent);
        masterAgent.registerAgent(patentLandscapeAgent);
        masterAgent.registerAgent(clinicalTrialsAgent);
        masterAgent.registerAgent(eximTradeAgent);
        masterAgent.registerAgent(webIntelligenceAgent);
        masterAgent.registerAgent(internalInsightsAgent);
        masterAgent.registerAgent(reportGeneratorAgent);
        
        return masterAgent;
    }
}