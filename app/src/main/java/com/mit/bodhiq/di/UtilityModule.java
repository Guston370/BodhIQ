package com.mit.bodhiq.di;

import android.content.Context;
import com.mit.bodhiq.utils.DateUtils;
import com.mit.bodhiq.utils.GeminiApiService;
import com.mit.bodhiq.utils.PermissionHelper;
import com.mit.bodhiq.utils.PreferenceManager;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/**
 * Hilt module that provides utility classes and helper dependencies.
 * This module contains application-level utilities that are used across
 * multiple components in the application.
 */
@Module
@InstallIn(SingletonComponent.class)
public class UtilityModule {



    /**
     * Provides DateUtils for date formatting and operations.
     *
     * @return DateUtils instance
     */
    @Provides
    @Singleton
    public DateUtils provideDateUtils() {
        return new DateUtils();
    }

    /**
     * Provides PreferenceManager for DataStore integration.
     *
     * @param context Application context
     * @return PreferenceManager instance
     */
    @Provides
    @Singleton
    public PreferenceManager providePreferenceManager(@ApplicationContext Context context) {
        return new PreferenceManager(context);
    }

    /**
     * Provides PermissionHelper for file access permissions.
     *
     * @return PermissionHelper instance
     */
    @Provides
    @Singleton
    public PermissionHelper providePermissionHelper() {
        return new PermissionHelper();
    }

    /**
     * Provides GeminiApiService for AI-powered medical chatbot.
     *
     * @param context Application context
     * @return GeminiApiService instance
     */
    @Provides
    @Singleton
    public GeminiApiService provideGeminiApiService(@ApplicationContext Context context) {
        return new GeminiApiService(context);
    }
}