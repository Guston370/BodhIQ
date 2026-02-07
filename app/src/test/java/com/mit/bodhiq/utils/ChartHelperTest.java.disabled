package com.mit.bodhiq.utils;

import static org.junit.Assert.*;

import android.content.Context;
import android.graphics.Bitmap;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.mit.bodhiq.data.model.ClinicalTrial;
import com.mit.bodhiq.data.model.Competitor;
import com.mit.bodhiq.data.model.EximTrade;
import com.mit.bodhiq.data.model.MarketData;
import com.mit.bodhiq.data.model.PatentInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for ChartHelper chart creation functionality.
 * Tests requirements 4.2, 4.4, 4.5 for data visualization with MPAndroidChart.
 */
@RunWith(RobolectricTestRunner.class)
public class ChartHelperTest {
    
    private ChartHelper chartHelper;
    private Context context;
    
    // Test data
    private MarketData testMarketData;
    private List<PatentInfo> testPatents;
    private List<ClinicalTrial> testClinicalTrials;
    private List<EximTrade> testEximTrades;
    
    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        chartHelper = new ChartHelper(context);
        
        // Setup test market data
        testMarketData = new MarketData();
        testMarketData.setMolecule("Montelukast");
        testMarketData.setMarketSize2024(5000000000L);
        testMarketData.setCagr(8.5);
        testMarketData.setRegion("Global");
        testMarketData.setTopIndications(Arrays.asList("Asthma", "Allergic Rhinitis"));
        testMarketData.setEmergingIndications(Arrays.asList("COPD", "Chronic Urticaria"));
        
        // Setup competitors
        List<Competitor> competitors = Arrays.asList(
            new Competitor("Singulair", "Merck", 35.0),
            new Competitor("Generic Montelukast", "Various", 45.0),
            new Competitor("Accolate", "AstraZeneca", 20.0)
        );
        testMarketData.setCompetitors(competitors);
        testMarketData.setGrowthDrivers(Arrays.asList("Increasing asthma prevalence", "Generic availability"));
        
        // Setup test patents
        testPatents = Arrays.asList(
            new PatentInfo("US6268533", "Montelukast sodium compositions", "1999-01-15", "2019-01-15", "Merck", "Expired"),
            new PatentInfo("US7235576", "Pharmaceutical formulations", "2003-05-20", "2023-05-20", "Merck", "Expired"),
            new PatentInfo("US8765432", "Extended release formulation", "2010-03-10", "2030-03-10", "Generic Co", "Active")
        );
        
        // Setup test clinical trials
        testClinicalTrials = Arrays.asList(
            new ClinicalTrial("NCT00123456", "Phase III", 500, "Asthma efficacy study", "Completed", "Merck"),
            new ClinicalTrial("NCT00789012", "Phase II", 200, "Allergic rhinitis study", "Active", "Generic Co"),
            new ClinicalTrial("NCT00345678", "Phase I", 50, "Safety study", "Completed", "Pharma Inc")
        );
        
        // Setup test EXIM trade data
        testEximTrades = Arrays.asList(
            new EximTrade("India", "Export", 150000000L, 25000),
            new EximTrade("China", "Import", 80000000L, 15000),
            new EximTrade("USA", "Export", 200000000L, 30000),
            new EximTrade("Germany", "Import", 120000000L, 20000)
        );
    }
    
    /**
     * Test market data chart creation.
     * Requirement 4.2: Market data visualization with bar charts and pie charts
     */
    @Test
    public void testCreateMarketDataChart() {
        // When
        Bitmap chartBitmap = chartHelper.createMarketDataChart(testMarketData);
        
        // Then
        assertNotNull("Chart bitmap should not be null", chartBitmap);
        assertTrue("Chart bitmap should have reasonable width", chartBitmap.getWidth() > 100);
        assertTrue("Chart bitmap should have reasonable height", chartBitmap.getHeight() > 100);
        assertFalse("Chart bitmap should not be recycled", chartBitmap.isRecycled());
    }
    
    /**
     * Test market data chart with null data.
     */
    @Test
    public void testCreateMarketDataChart_NullData() {
        // When
        Bitmap chartBitmap = chartHelper.createMarketDataChart(null);
        
        // Then
        assertNull("Chart bitmap should be null for null data", chartBitmap);
    }
    
    /**
     * Test market data chart with empty competitors.
     */
    @Test
    public void testCreateMarketDataChart_EmptyCompetitors() {
        // Given
        testMarketData.setCompetitors(Arrays.asList());
        
        // When
        Bitmap chartBitmap = chartHelper.createMarketDataChart(testMarketData);
        
        // Then
        assertNotNull("Chart bitmap should not be null even with empty competitors", chartBitmap);
        assertTrue("Chart should still have reasonable dimensions", chartBitmap.getWidth() > 100);
    }
    
    /**
     * Test patent chart creation.
     * Requirement 4.4: Patent information visualization
     */
    @Test
    public void testCreatePatentChart() {
        // When
        Bitmap chartBitmap = chartHelper.createPatentChart(testPatents);
        
        // Then
        assertNotNull("Patent chart bitmap should not be null", chartBitmap);
        assertTrue("Patent chart should have reasonable width", chartBitmap.getWidth() > 100);
        assertTrue("Patent chart should have reasonable height", chartBitmap.getHeight() > 100);
        assertFalse("Patent chart bitmap should not be recycled", chartBitmap.isRecycled());
    }
    
    /**
     * Test patent chart with null data.
     */
    @Test
    public void testCreatePatentChart_NullData() {
        // When
        Bitmap chartBitmap = chartHelper.createPatentChart(null);
        
        // Then
        assertNull("Patent chart bitmap should be null for null data", chartBitmap);
    }
    
    /**
     * Test patent chart with empty list.
     */
    @Test
    public void testCreatePatentChart_EmptyList() {
        // When
        Bitmap chartBitmap = chartHelper.createPatentChart(Arrays.asList());
        
        // Then
        assertNull("Patent chart bitmap should be null for empty list", chartBitmap);
    }
    
    /**
     * Test clinical trials chart creation.
     * Requirement 4.5: Clinical trials phase distribution charts
     */
    @Test
    public void testCreateClinicalTrialsChart() {
        // When
        Bitmap chartBitmap = chartHelper.createClinicalTrialsChart(testClinicalTrials);
        
        // Then
        assertNotNull("Clinical trials chart bitmap should not be null", chartBitmap);
        assertTrue("Clinical trials chart should have reasonable width", chartBitmap.getWidth() > 100);
        assertTrue("Clinical trials chart should have reasonable height", chartBitmap.getHeight() > 100);
        assertFalse("Clinical trials chart bitmap should not be recycled", chartBitmap.isRecycled());
    }
    
    /**
     * Test clinical trials chart with null data.
     */
    @Test
    public void testCreateClinicalTrialsChart_NullData() {
        // When
        Bitmap chartBitmap = chartHelper.createClinicalTrialsChart(null);
        
        // Then
        assertNull("Clinical trials chart bitmap should be null for null data", chartBitmap);
    }
    
    /**
     * Test clinical trials chart with empty list.
     */
    @Test
    public void testCreateClinicalTrialsChart_EmptyList() {
        // When
        Bitmap chartBitmap = chartHelper.createClinicalTrialsChart(Arrays.asList());
        
        // Then
        assertNull("Clinical trials chart bitmap should be null for empty list", chartBitmap);
    }
    
    /**
     * Test EXIM trade chart creation.
     * Requirement 4.5: EXIM trade data visualization
     */
    @Test
    public void testCreateEximTradeChart() {
        // When
        Bitmap chartBitmap = chartHelper.createEximTradeChart(testEximTrades);
        
        // Then
        assertNotNull("EXIM trade chart bitmap should not be null", chartBitmap);
        assertTrue("EXIM trade chart should have reasonable width", chartBitmap.getWidth() > 100);
        assertTrue("EXIM trade chart should have reasonable height", chartBitmap.getHeight() > 100);
        assertFalse("EXIM trade chart bitmap should not be recycled", chartBitmap.isRecycled());
    }
    
    /**
     * Test EXIM trade chart with null data.
     */
    @Test
    public void testCreateEximTradeChart_NullData() {
        // When
        Bitmap chartBitmap = chartHelper.createEximTradeChart(null);
        
        // Then
        assertNull("EXIM trade chart bitmap should be null for null data", chartBitmap);
    }
    
    /**
     * Test EXIM trade chart with empty list.
     */
    @Test
    public void testCreateEximTradeChart_EmptyList() {
        // When
        Bitmap chartBitmap = chartHelper.createEximTradeChart(Arrays.asList());
        
        // Then
        assertNull("EXIM trade chart bitmap should be null for empty list", chartBitmap);
    }
    
    /**
     * Test bar chart creation utility.
     */
    @Test
    public void testCreateBarChart() {
        // Given
        String[] labels = {"Q1", "Q2", "Q3", "Q4"};
        float[] values = {100f, 150f, 120f, 180f};
        String title = "Quarterly Sales";
        
        // When
        BarChart barChart = chartHelper.createBarChart(labels, values, title);
        
        // Then
        assertNotNull("Bar chart should not be null", barChart);
        assertEquals("Chart description should match title", title, barChart.getDescription().getText());
        assertNotNull("Chart should have data", barChart.getData());
        assertEquals("Chart should have correct number of entries", 4, barChart.getData().getDataSetCount() > 0 ? 
            barChart.getData().getDataSetByIndex(0).getEntryCount() : 0);
    }
    
    /**
     * Test pie chart creation utility.
     */
    @Test
    public void testCreatePieChart() {
        // Given
        String[] labels = {"Segment A", "Segment B", "Segment C"};
        float[] values = {40f, 35f, 25f};
        String title = "Market Share";
        
        // When
        PieChart pieChart = chartHelper.createPieChart(labels, values, title);
        
        // Then
        assertNotNull("Pie chart should not be null", pieChart);
        assertEquals("Chart description should match title", title, pieChart.getDescription().getText());
        assertNotNull("Chart should have data", pieChart.getData());
        assertEquals("Chart should have correct number of entries", 3, pieChart.getData().getDataSet().getEntryCount());
    }
    
    /**
     * Test chart bitmap generation with different sizes.
     */
    @Test
    public void testChartBitmapGeneration_DifferentSizes() {
        // Test different chart sizes
        int[] widths = {400, 600, 800};
        int[] heights = {300, 400, 500};
        
        for (int i = 0; i < widths.length; i++) {
            // When
            Bitmap chartBitmap = chartHelper.createMarketDataChartWithSize(testMarketData, widths[i], heights[i]);
            
            // Then
            assertNotNull("Chart bitmap should not be null for size " + widths[i] + "x" + heights[i], chartBitmap);
            assertEquals("Chart width should match requested width", widths[i], chartBitmap.getWidth());
            assertEquals("Chart height should match requested height", heights[i], chartBitmap.getHeight());
        }
    }
    
    /**
     * Test chart color schemes.
     */
    @Test
    public void testChartColorSchemes() {
        // When
        Bitmap marketChart = chartHelper.createMarketDataChart(testMarketData);
        Bitmap patentChart = chartHelper.createPatentChart(testPatents);
        Bitmap clinicalChart = chartHelper.createClinicalTrialsChart(testClinicalTrials);
        
        // Then - All charts should be generated successfully
        assertNotNull("Market chart should be generated", marketChart);
        assertNotNull("Patent chart should be generated", patentChart);
        assertNotNull("Clinical chart should be generated", clinicalChart);
        
        // Verify charts have different content (different bitmaps)
        assertFalse("Market and patent charts should be different", marketChart.sameAs(patentChart));
        assertFalse("Patent and clinical charts should be different", patentChart.sameAs(clinicalChart));
        assertFalse("Market and clinical charts should be different", marketChart.sameAs(clinicalChart));
    }
    
    /**
     * Test concurrent chart generation.
     */
    @Test
    public void testConcurrentChartGeneration() {
        // When - Generate multiple charts concurrently
        Bitmap chart1 = chartHelper.createMarketDataChart(testMarketData);
        Bitmap chart2 = chartHelper.createPatentChart(testPatents);
        Bitmap chart3 = chartHelper.createClinicalTrialsChart(testClinicalTrials);
        Bitmap chart4 = chartHelper.createEximTradeChart(testEximTrades);
        
        // Then
        assertNotNull("First chart should be generated", chart1);
        assertNotNull("Second chart should be generated", chart2);
        assertNotNull("Third chart should be generated", chart3);
        assertNotNull("Fourth chart should be generated", chart4);
        
        // Verify all charts are different
        assertFalse("Charts should be different", chart1.sameAs(chart2));
        assertFalse("Charts should be different", chart2.sameAs(chart3));
        assertFalse("Charts should be different", chart3.sameAs(chart4));
    }
    
    /**
     * Test chart generation with extreme values.
     */
    @Test
    public void testChartGeneration_ExtremeValues() {
        // Given - Market data with extreme values
        MarketData extremeData = new MarketData();
        extremeData.setMolecule("TestMolecule");
        extremeData.setMarketSize2024(0L); // Zero market size
        extremeData.setCagr(-50.0); // Negative growth
        extremeData.setCompetitors(Arrays.asList(
            new Competitor("Competitor1", "Company1", 100.0), // 100% market share
            new Competitor("Competitor2", "Company2", 0.0)    // 0% market share
        ));
        
        // When
        Bitmap chartBitmap = chartHelper.createMarketDataChart(extremeData);
        
        // Then
        assertNotNull("Chart should handle extreme values", chartBitmap);
        assertTrue("Chart should have reasonable dimensions", chartBitmap.getWidth() > 100);
    }
    
    /**
     * Test chart generation performance.
     */
    @Test
    public void testChartGenerationPerformance() {
        // Given
        long startTime = System.currentTimeMillis();
        
        // When - Generate multiple charts
        for (int i = 0; i < 10; i++) {
            Bitmap chart = chartHelper.createMarketDataChart(testMarketData);
            assertNotNull("Chart " + i + " should be generated", chart);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then
        assertTrue("Chart generation should be reasonably fast", duration < 10000); // Less than 10 seconds for 10 charts
    }
    
    /**
     * Test memory management of chart bitmaps.
     */
    @Test
    public void testChartBitmapMemoryManagement() {
        // When
        Bitmap chart1 = chartHelper.createMarketDataChart(testMarketData);
        Bitmap chart2 = chartHelper.createMarketDataChart(testMarketData);
        
        // Then
        assertNotNull("First chart should be created", chart1);
        assertNotNull("Second chart should be created", chart2);
        
        // Verify bitmaps are not the same instance (new bitmap created each time)
        assertNotSame("Should create new bitmap instances", chart1, chart2);
        
        // Verify bitmaps are not recycled
        assertFalse("First chart should not be recycled", chart1.isRecycled());
        assertFalse("Second chart should not be recycled", chart2.isRecycled());
    }
}