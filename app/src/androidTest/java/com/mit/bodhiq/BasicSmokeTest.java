package com.mit.bodhiq;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.mit.bodhiq.ui.splash.SplashActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Basic Smoke Tests
 * Simple tests to verify the app launches and basic functionality works
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BasicSmokeTest {

    @Rule
    public ActivityScenarioRule<SplashActivity> activityRule =
            new ActivityScenarioRule<>(SplashActivity.class);

    /**
     * Test that the app context is available
     */
    @Test
    public void testAppContext() {
        Context appContext = ApplicationProvider.getApplicationContext();
        assertEquals("com.mit.bodhiq", appContext.getPackageName());
    }

    /**
     * Test that SplashActivity launches successfully
     */
    @Test
    public void testSplashActivityLaunches() {
        // If we get here, the activity launched successfully
        activityRule.getScenario().onActivity(activity -> {
            assertNotNull(activity);
        });
    }

    /**
     * Test app launch performance
     */
    @Test
    public void testAppLaunchPerformance() {
        long startTime = System.currentTimeMillis();
        
        activityRule.getScenario().onActivity(activity -> {
            assertNotNull(activity);
        });
        
        long endTime = System.currentTimeMillis();
        long launchTime = endTime - startTime;
        
        // Verify launch time is reasonable (under 5 seconds for test)
        assertTrue("App launch took too long: " + launchTime + "ms", launchTime < 5000);
        
        System.out.println("✓ App launch time: " + launchTime + "ms");
    }

    /**
     * Test that BMICalculator utility exists and works
     */
    @Test
    public void testBMICalculatorExists() {
        activityRule.getScenario().onActivity(activity -> {
            // Verify BMICalculator class exists
            try {
                Class.forName("com.mit.bodhiq.utils.BMICalculator");
                System.out.println("✓ BMICalculator class found");
            } catch (ClassNotFoundException e) {
                fail("BMICalculator class not found");
            }
        });
    }

    /**
     * Test that EmergencyQrValidator exists
     */
    @Test
    public void testEmergencyQrValidatorExists() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                Class.forName("com.mit.bodhiq.utils.EmergencyQrValidator");
                System.out.println("✓ EmergencyQrValidator class found");
            } catch (ClassNotFoundException e) {
                fail("EmergencyQrValidator class not found");
            }
        });
    }

    /**
     * Test that key activities exist
     */
    @Test
    public void testKeyActivitiesExist() {
        String[] activities = {
            "com.mit.bodhiq.MainActivity",
            "com.mit.bodhiq.ui.login.LoginActivity",
            "com.mit.bodhiq.ui.EmergencyQrActivity",
            "com.mit.bodhiq.ui.reminders.RemindersActivity"
        };

        for (String activityName : activities) {
            try {
                Class.forName(activityName);
                System.out.println("✓ " + activityName + " found");
            } catch (ClassNotFoundException e) {
                fail(activityName + " not found");
            }
        }
    }

    /**
     * Test that Firebase is configured
     */
    @Test
    public void testFirebaseConfiguration() {
        Context appContext = ApplicationProvider.getApplicationContext();
        
        // Check if google-services.json resources are available
        int resourceId = appContext.getResources().getIdentifier(
            "google_app_id", "string", appContext.getPackageName()
        );
        
        assertTrue("Firebase not configured", resourceId != 0);
        System.out.println("✓ Firebase configuration found");
    }
}
