package com.mit.bodhiq;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * MainActivity Tests using actual view IDs from activity_main.xml
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Test that MainActivity launches and displays correctly
     */
    @Test
    public void testMainActivityDisplays() {
        // Verify fragment container is displayed
        onView(withId(R.id.nav_host_fragment))
                .check(matches(isDisplayed()));

        // Verify bottom navigation is displayed
        onView(withId(R.id.bottom_navigation))
                .check(matches(isDisplayed()));
    }

    /**
     * Test bottom navigation is functional
     */
    @Test
    public void testBottomNavigationExists() {
        // Verify bottom navigation exists and is displayed
        onView(withId(R.id.bottom_navigation))
                .check(matches(isDisplayed()));
    }

    /**
     * Test fragment container exists
     */
    @Test
    public void testFragmentContainerExists() {
        // Verify fragment container exists
        onView(withId(R.id.nav_host_fragment))
                .check(matches(isDisplayed()));
    }
}
