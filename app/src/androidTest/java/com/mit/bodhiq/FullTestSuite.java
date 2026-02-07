package com.mit.bodhiq;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Full Test Suite for BodhIQ Application
 * 
 * Runs all available tests including:
 * - Basic smoke tests
 * - Login flow tests
 * - Emergency QR tests
 * - MainActivity tests
 * - Utility class tests
 * 
 * To run:
 * ./gradlew connectedAndroidTest
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BasicSmokeTest.class,
    LoginFlowTests.class,
    EmergencyQRTests.class,
    MainActivityTests.class,
    UtilityClassTests.class
})
public class FullTestSuite {
    // This class remains empty, used only as a holder for the above annotations
}
