package com.mit.bodhiq;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.mit.bodhiq.ui.EmergencyQrActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Emergency QR Code Tests using actual view IDs from activity_emergency_qr.xml
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EmergencyQRTests {

    @Rule
    public ActivityScenarioRule<EmergencyQrActivity> activityRule =
            new ActivityScenarioRule<>(EmergencyQrActivity.class);

    /**
     * Test that EmergencyQrActivity launches
     */
    @Test
    public void testEmergencyQRActivityLaunches() {
        // Wait for loading to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify toolbar is displayed
        onView(withId(R.id.toolbar))
                .check(matches(isDisplayed()));
    }

    /**
     * Test QR code generation button is visible
     */
    @Test
    public void testGenerateQRButtonVisible() {
        // Wait for loading
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify generate button exists
        onView(withId(R.id.btn_generate_qr))
                .check(matches(isDisplayed()));
    }

    /**
     * Test QR code generation flow
     */
    @Test
    public void testQRCodeGeneration() {
        // Wait for loading
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click generate button
        onView(withId(R.id.btn_generate_qr))
                .perform(click());

        // Wait for QR generation
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify QR code image is displayed
        onView(withId(R.id.iv_qr_code))
                .check(matches(isDisplayed()));

        // Verify action buttons are displayed
        onView(withId(R.id.btn_save_qr))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btn_share_qr))
                .check(matches(isDisplayed()));
    }

    /**
     * Test logo toggle switch
     */
    @Test
    public void testLogoToggleSwitch() {
        // Wait for loading
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify logo switch is displayed
        onView(withId(R.id.switch_include_logo))
                .check(matches(isDisplayed()));

        // Toggle the switch
        onView(withId(R.id.switch_include_logo))
                .perform(click());
    }

    /**
     * Test copy payload button
     */
    @Test
    public void testCopyPayloadButton() {
        // Wait for loading
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Generate QR first
        onView(withId(R.id.btn_generate_qr))
                .perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify copy payload button is displayed
        onView(withId(R.id.btn_copy_payload))
                .check(matches(isDisplayed()));

        // Click copy payload
        onView(withId(R.id.btn_copy_payload))
                .perform(click());
    }

    /**
     * Test regenerate QR code
     */
    @Test
    public void testRegenerateQRCode() {
        // Wait for loading
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Generate QR first
        onView(withId(R.id.btn_generate_qr))
                .perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify regenerate button is displayed
        onView(withId(R.id.btn_regenerate))
                .check(matches(isDisplayed()));

        // Click regenerate
        onView(withId(R.id.btn_regenerate))
                .perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // QR code should still be displayed
        onView(withId(R.id.iv_qr_code))
                .check(matches(isDisplayed()));
    }
}
