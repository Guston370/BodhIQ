package com.mit.bodhiq;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.mit.bodhiq.ui.login.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Login Flow Tests using actual view IDs from activity_login.xml
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginFlowTests {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    /**
     * Test that LoginActivity launches and displays correctly
     */
    @Test
    public void testLoginActivityDisplays() {
        // Verify Google Sign-In button is displayed
        onView(withId(R.id.btn_google_sign_in))
                .check(matches(isDisplayed()));

        // Verify Email Sign-In toggle button is displayed
        onView(withId(R.id.btn_email_sign_in_toggle))
                .check(matches(isDisplayed()));

        // Verify Sign Up link is displayed
        onView(withId(R.id.tv_sign_up))
                .check(matches(isDisplayed()));
    }

    /**
     * Test email sign-in form appears when toggle is clicked
     */
    @Test
    public void testEmailFormToggle() {
        // Click email sign-in toggle
        onView(withId(R.id.btn_email_sign_in_toggle))
                .perform(click());

        // Wait a moment for animation
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify email form is displayed
        onView(withId(R.id.ll_email_form))
                .check(matches(isDisplayed()));

        // Verify email input is displayed
        onView(withId(R.id.et_email))
                .check(matches(isDisplayed()));

        // Verify password input is displayed
        onView(withId(R.id.et_password))
                .check(matches(isDisplayed()));

        // Verify forgot password link is displayed
        onView(withId(R.id.tv_forgot_password))
                .check(matches(isDisplayed()));
    }

    /**
     * Test email input validation
     */
    @Test
    public void testEmailInputValidation() {
        // Show email form
        onView(withId(R.id.btn_email_sign_in_toggle))
                .perform(click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Enter invalid email
        onView(withId(R.id.et_email))
                .perform(typeText("invalidemail"), closeSoftKeyboard());

        // Enter password
        onView(withId(R.id.et_password))
                .perform(typeText("password123"), closeSoftKeyboard());

        // Click sign in button
        onView(withId(R.id.btn_email_sign_in))
                .perform(click());

        // Email validation should prevent login
        // Form should still be visible
        onView(withId(R.id.ll_email_form))
                .check(matches(isDisplayed()));
    }

    /**
     * Test forgot password navigation
     */
    @Test
    public void testForgotPasswordNavigation() {
        // Show email form
        onView(withId(R.id.btn_email_sign_in_toggle))
                .perform(click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click forgot password
        onView(withId(R.id.tv_forgot_password))
                .perform(click());

        // Should navigate to ForgotPasswordActivity
        // (Activity transition test - would need Intents.intended() for full verification)
    }

    /**
     * Test back to options button
     */
    @Test
    public void testBackToOptions() {
        // Show email form
        onView(withId(R.id.btn_email_sign_in_toggle))
                .perform(click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify form is displayed
        onView(withId(R.id.ll_email_form))
                .check(matches(isDisplayed()));

        // Click back to options
        onView(withId(R.id.btn_back_to_options))
                .perform(click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Email form should be hidden, sign-in options should be visible
        onView(withId(R.id.btn_google_sign_in))
                .check(matches(isDisplayed()));
    }

    /**
     * Test sign up navigation
     */
    @Test
    public void testSignUpNavigation() {
        // Click sign up link
        onView(withId(R.id.tv_sign_up))
                .perform(click());

        // Should navigate to SignUpActivity
        // (Activity transition test)
    }
}
