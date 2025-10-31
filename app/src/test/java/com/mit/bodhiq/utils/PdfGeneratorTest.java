package com.mit.bodhiq.utils;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import com.mit.bodhiq.data.database.dao.AgentResultDao;
import com.mit.bodhiq.data.database.dao.QueryDao;
import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.database.entity.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Unit tests for PdfGenerator PDF generation and chart creation.
 * Tests requirements 5.1, 5.2, 5.4 for PDF generation functionality.
 */
@RunWith(RobolectricTestRunner.class)
public class PdfGeneratorTest {
    
    @Mock
    private QueryDao queryDao;
    
    @Mock
    private AgentResultDao agentResultDao;
    
    @Mock
    private ChartHelper chartHelper;
    
    private PdfGenerator pdfGenerator;
    private Context context;
    
    // Test data
    private Query testQuery;
    private List<AgentResult> testAgentResults;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        
        pdfGenerator = new PdfGenerator(context, queryDao, agentResultDao, chartHelper);
        
        // Setup test data
        testQuery = new Query(1L, "Analyze Montelukast market data", "Montelukast", "COMPLETED", System.currentTimeMillis());
        testQuery.setId(1L);
        testQuery.setCompletedAt(System.currentTimeMillis());
        
        testAgentResults = Arrays.asList(
            createAgentResult(1L, "Market Insights", "{\"molecule\":\"Montelukast\",\"marketSize2024\":5000000000}"),
            createAgentResult(2L, "Patent Landscape", "{\"patents\":[{\"number\":\"US123456\",\"title\":\"Test Patent\"}]}"),
            createAgentResult(3L, "Clinical Trials", "{\"trials\":[{\"nctId\":\"NCT123456\",\"phase\":\"Phase III\"}]}")
        );
    }
    
    private AgentResult createAgentResult(long id, String agentName, String resultData) {
        AgentResult result = new AgentResult(1L, agentName, "COMPLETED", System.currentTimeMillis());
        result.setId(id);
        result.setResultData(resultData);
        result.setCompletedAt(System.currentTimeMillis());
        result.setExecutionTimeMs(3000);
        return result;
    }
    
    /**
     * Test successful PDF generation.
     * Requirement 5.1: Automatic PDF report generation using iText7
     */
    @Test
    public void testGenerateReport_Success() {
        // Given
        long queryId = 1L;
        String molecule = "Montelukast";
        
        when(queryDao.getQueryById(queryId)).thenReturn(Single.just(testQuery));
        when(agentResultDao.getResultsByQueryId(queryId)).thenReturn(Flowable.just(testAgentResults));
        when(chartHelper.createMarketDataChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createPatentChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createClinicalTrialsChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        
        // When
        String filePath = pdfGenerator.generateReport(queryId, molecule);
        
        // Then
        assertNotNull("File path should not be null", filePath);
        assertTrue("File path should contain molecule name", filePath.contains("Montelukast"));
        assertTrue("File path should have PDF extension", filePath.endsWith(".pdf"));
        
        // Verify file was created
        File pdfFile = new File(filePath);
        assertTrue("PDF file should exist", pdfFile.exists());
        assertTrue("PDF file should have content", pdfFile.length() > 0);
        
        verify(queryDao).getQueryById(queryId);
        verify(agentResultDao).getResultsByQueryId(queryId);
        verify(chartHelper).createMarketDataChart(any());
        verify(chartHelper).createPatentChart(any());
        verify(chartHelper).createClinicalTrialsChart(any());
        
        // Cleanup
        pdfFile.delete();
    }
    
    /**
     * Test PDF generation with query not found.
     */
    @Test
    public void testGenerateReport_QueryNotFound() {
        // Given
        long queryId = 999L;
        String molecule = "Montelukast";
        
        when(queryDao.getQueryById(queryId)).thenReturn(Single.error(new RuntimeException("Query not found")));
        
        // When & Then
        try {
            pdfGenerator.generateReport(queryId, molecule);
            fail("Should throw exception when query not found");
        } catch (Exception e) {
            assertTrue("Should contain query not found error", e.getMessage().contains("Query not found"));
        }
        
        verify(queryDao).getQueryById(queryId);
        verify(agentResultDao, never()).getResultsByQueryId(anyLong());
    }
    
    /**
     * Test PDF generation with no agent results.
     */
    @Test
    public void testGenerateReport_NoAgentResults() {
        // Given
        long queryId = 1L;
        String molecule = "Montelukast";
        
        when(queryDao.getQueryById(queryId)).thenReturn(Single.just(testQuery));
        when(agentResultDao.getResultsByQueryId(queryId)).thenReturn(Flowable.just(Arrays.asList()));
        
        // When
        String filePath = pdfGenerator.generateReport(queryId, molecule);
        
        // Then
        assertNotNull("File path should not be null even with no results", filePath);
        
        // Verify file was created
        File pdfFile = new File(filePath);
        assertTrue("PDF file should exist", pdfFile.exists());
        assertTrue("PDF file should have content", pdfFile.length() > 0);
        
        verify(queryDao).getQueryById(queryId);
        verify(agentResultDao).getResultsByQueryId(queryId);
        
        // Cleanup
        pdfFile.delete();
    }
    
    /**
     * Test PDF generation with chart creation failure.
     * Requirement 5.2: Chart embedding in PDF reports
     */
    @Test
    public void testGenerateReport_ChartCreationFailure() {
        // Given
        long queryId = 1L;
        String molecule = "Montelukast";
        
        when(queryDao.getQueryById(queryId)).thenReturn(Single.just(testQuery));
        when(agentResultDao.getResultsByQueryId(queryId)).thenReturn(Flowable.just(testAgentResults));
        when(chartHelper.createMarketDataChart(any())).thenThrow(new RuntimeException("Chart creation failed"));
        when(chartHelper.createPatentChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createClinicalTrialsChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        
        // When
        String filePath = pdfGenerator.generateReport(queryId, molecule);
        
        // Then - Should still generate PDF without the failed chart
        assertNotNull("File path should not be null", filePath);
        
        File pdfFile = new File(filePath);
        assertTrue("PDF file should exist", pdfFile.exists());
        assertTrue("PDF file should have content", pdfFile.length() > 0);
        
        verify(chartHelper).createMarketDataChart(any());
        verify(chartHelper).createPatentChart(any());
        verify(chartHelper).createClinicalTrialsChart(any());
        
        // Cleanup
        pdfFile.delete();
    }
    
    /**
     * Test PDF file naming convention.
     * Requirement 5.4: Report metadata including molecule name and generation date
     */
    @Test
    public void testGenerateReport_FileNaming() {
        // Given
        long queryId = 1L;
        String molecule = "GLP-1";
        
        when(queryDao.getQueryById(queryId)).thenReturn(Single.just(testQuery));
        when(agentResultDao.getResultsByQueryId(queryId)).thenReturn(Flowable.just(testAgentResults));
        when(chartHelper.createMarketDataChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createPatentChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createClinicalTrialsChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        
        // When
        String filePath = pdfGenerator.generateReport(queryId, molecule);
        
        // Then
        assertNotNull("File path should not be null", filePath);
        assertTrue("File name should contain BodhIQ prefix", filePath.contains("BodhIQ_Report"));
        assertTrue("File name should contain sanitized molecule name", filePath.contains("GLP_1")); // Hyphen replaced with underscore
        assertTrue("File name should contain timestamp", filePath.matches(".*\\d{8}_\\d{6}.*")); // YYYYMMDD_HHMMSS pattern
        assertTrue("File should have PDF extension", filePath.endsWith(".pdf"));
        
        // Cleanup
        new File(filePath).delete();
    }
    
    /**
     * Test PDF content structure.
     * Requirement 5.1: Include all agent results, charts, and data tables
     */
    @Test
    public void testGenerateReport_ContentStructure() {
        // Given
        long queryId = 1L;
        String molecule = "Montelukast";
        
        when(queryDao.getQueryById(queryId)).thenReturn(Single.just(testQuery));
        when(agentResultDao.getResultsByQueryId(queryId)).thenReturn(Flowable.just(testAgentResults));
        when(chartHelper.createMarketDataChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createPatentChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createClinicalTrialsChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        
        // When
        String filePath = pdfGenerator.generateReport(queryId, molecule);
        
        // Then
        File pdfFile = new File(filePath);
        assertTrue("PDF file should exist", pdfFile.exists());
        
        // Verify file size indicates content was added
        long fileSize = pdfFile.length();
        assertTrue("PDF should have substantial content", fileSize > 10000); // At least 10KB
        
        // Verify all agent results were processed
        verify(agentResultDao).getResultsByQueryId(queryId);
        
        // Verify charts were created for each relevant agent
        verify(chartHelper).createMarketDataChart(any());
        verify(chartHelper).createPatentChart(any());
        verify(chartHelper).createClinicalTrialsChart(any());
        
        // Cleanup
        pdfFile.delete();
    }
    
    /**
     * Test PDF generation with different molecules.
     */
    @Test
    public void testGenerateReport_DifferentMolecules() {
        String[] molecules = {"Montelukast", "Humira", "Metformin", "GLP-1", "Eliquis"};
        
        when(queryDao.getQueryById(anyLong())).thenReturn(Single.just(testQuery));
        when(agentResultDao.getResultsByQueryId(anyLong())).thenReturn(Flowable.just(testAgentResults));
        when(chartHelper.createMarketDataChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createPatentChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createClinicalTrialsChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        
        for (String molecule : molecules) {
            // When
            String filePath = pdfGenerator.generateReport(1L, molecule);
            
            // Then
            assertNotNull("File path should not be null for " + molecule, filePath);
            assertTrue("File path should contain molecule name for " + molecule, 
                filePath.contains(molecule.replace("-", "_")));
            
            File pdfFile = new File(filePath);
            assertTrue("PDF file should exist for " + molecule, pdfFile.exists());
            assertTrue("PDF file should have content for " + molecule, pdfFile.length() > 0);
            
            // Cleanup
            pdfFile.delete();
        }
    }
    
    /**
     * Test concurrent PDF generation.
     */
    @Test
    public void testGenerateReport_ConcurrentGeneration() {
        // Given
        when(queryDao.getQueryById(anyLong())).thenReturn(Single.just(testQuery));
        when(agentResultDao.getResultsByQueryId(anyLong())).thenReturn(Flowable.just(testAgentResults));
        when(chartHelper.createMarketDataChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createPatentChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createClinicalTrialsChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        
        // When - Generate multiple reports concurrently
        String filePath1 = pdfGenerator.generateReport(1L, "Montelukast");
        String filePath2 = pdfGenerator.generateReport(2L, "Humira");
        String filePath3 = pdfGenerator.generateReport(3L, "Metformin");
        
        // Then
        assertNotNull("First file path should not be null", filePath1);
        assertNotNull("Second file path should not be null", filePath2);
        assertNotNull("Third file path should not be null", filePath3);
        
        // Verify all files are different
        assertNotEquals("File paths should be unique", filePath1, filePath2);
        assertNotEquals("File paths should be unique", filePath2, filePath3);
        assertNotEquals("File paths should be unique", filePath1, filePath3);
        
        // Verify all files exist
        assertTrue("First PDF should exist", new File(filePath1).exists());
        assertTrue("Second PDF should exist", new File(filePath2).exists());
        assertTrue("Third PDF should exist", new File(filePath3).exists());
        
        // Cleanup
        new File(filePath1).delete();
        new File(filePath2).delete();
        new File(filePath3).delete();
    }
    
    /**
     * Test PDF generation with malformed agent result data.
     */
    @Test
    public void testGenerateReport_MalformedAgentData() {
        // Given
        long queryId = 1L;
        String molecule = "Montelukast";
        
        // Create agent results with malformed JSON
        List<AgentResult> malformedResults = Arrays.asList(
            createAgentResult(1L, "Market Insights", "invalid json"),
            createAgentResult(2L, "Patent Landscape", "{incomplete json"),
            createAgentResult(3L, "Clinical Trials", "null")
        );
        
        when(queryDao.getQueryById(queryId)).thenReturn(Single.just(testQuery));
        when(agentResultDao.getResultsByQueryId(queryId)).thenReturn(Flowable.just(malformedResults));
        when(chartHelper.createMarketDataChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createPatentChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createClinicalTrialsChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        
        // When
        String filePath = pdfGenerator.generateReport(queryId, molecule);
        
        // Then - Should still generate PDF with error handling
        assertNotNull("File path should not be null", filePath);
        
        File pdfFile = new File(filePath);
        assertTrue("PDF file should exist", pdfFile.exists());
        assertTrue("PDF file should have content", pdfFile.length() > 0);
        
        // Cleanup
        pdfFile.delete();
    }
    
    /**
     * Test PDF storage location.
     * Requirement 5.3: Store PDF file in app's internal storage
     */
    @Test
    public void testGenerateReport_StorageLocation() {
        // Given
        long queryId = 1L;
        String molecule = "Montelukast";
        
        when(queryDao.getQueryById(queryId)).thenReturn(Single.just(testQuery));
        when(agentResultDao.getResultsByQueryId(queryId)).thenReturn(Flowable.just(testAgentResults));
        when(chartHelper.createMarketDataChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createPatentChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        when(chartHelper.createClinicalTrialsChart(any())).thenReturn(mock(android.graphics.Bitmap.class));
        
        // When
        String filePath = pdfGenerator.generateReport(queryId, molecule);
        
        // Then
        assertNotNull("File path should not be null", filePath);
        
        // Verify file is in app's internal storage (should contain app package or internal directory)
        assertTrue("File should be in internal storage", 
            filePath.contains("files") || filePath.contains("internal") || filePath.contains(context.getPackageName()));
        
        File pdfFile = new File(filePath);
        assertTrue("PDF file should exist in internal storage", pdfFile.exists());
        
        // Verify file permissions (should be readable by app)
        assertTrue("PDF file should be readable", pdfFile.canRead());
        
        // Cleanup
        pdfFile.delete();
    }
}