package com.mit.bodhiq;

import org.junit.Test;
import static org.junit.Assert.*;

import com.mit.bodhiq.data.model.HealthSummary;

public class HealthSummaryTest {

    @Test
    public void testConciseSummary_Empty() {
        HealthSummary summary = new HealthSummary();
        assertEquals("No health data available", summary.getConciseSummary());
        assertFalse(summary.hasAnyData());
    }

    @Test
    public void testConciseSummary_WithData() {
        HealthSummary summary = new HealthSummary();
        summary.setBloodType("O+");
        summary.setAge("30");
        summary.setAllergies("Peanuts");
        summary.setHasAnyData(true);

        String result = summary.getConciseSummary();
        assertTrue(result.contains("Blood: O+"));
        assertTrue(result.contains("Age: 30"));
        assertTrue(result.contains("Allergies: Peanuts"));
    }

    @Test
    public void testConciseSummary_LimitFields() {
        HealthSummary summary = new HealthSummary();
        summary.setBloodType("A+");
        summary.setAge("25");
        summary.setAllergies("None");
        summary.setEmergencyContactPhone("1234567890");
        summary.setHasEmergencyContact(true);
        summary.setHeight("180");
        summary.setWeight("75");

        // Should contain max 5 items
        // Blood, Age, Allergies, Emergency, Height/Weight (combined)
        String result = summary.getConciseSummary();

        // Check formatting
        assertTrue(result.contains("Blood: A+"));
        assertTrue(result.contains("Age: 25"));
        assertTrue(result.contains("180cm / 75kg"));
    }

    @Test
    public void testHasAnyData() {
        HealthSummary summary = new HealthSummary();
        assertFalse(summary.hasAnyData());

        summary.setHasAnyData(true);
        assertTrue(summary.hasAnyData());
    }
}
