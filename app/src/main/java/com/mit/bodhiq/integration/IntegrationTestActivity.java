package com.mit.bodhiq.integration;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.mit.bodhiq.R;

/**
 * Test activity for verifying complete workflow integration.
 * This activity can be used during development to test the integration
 * without running the full application workflow.
 */
public class IntegrationTestActivity extends AppCompatActivity {
    
    private static final String TAG = "IntegrationTest";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a simple layout programmatically
        TextView textView = new TextView(this);
        textView.setPadding(32, 32, 32, 32);
        textView.setTextSize(14);
        
        // Run integration tests
        runIntegrationTests(textView);
        
        setContentView(textView);
    }
    
    private void runIntegrationTests(TextView resultView) {
        StringBuilder results = new StringBuilder();
        results.append("BodhIQ Integration Test Results\n");
        results.append("================================\n\n");
        
        try {
            // Test 1: Navigation Integration
            results.append("1. Navigation Integration Test\n");
            boolean navigationOk = WorkflowIntegrationManager.verifyNavigationIntegration(this);
            results.append("   Result: ").append(navigationOk ? "✓ PASS" : "✗ FAIL").append("\n\n");
            
            // Test 2: Lifecycle Integration
            results.append("2. Lifecycle Integration Test\n");
            boolean lifecycleOk = WorkflowIntegrationManager.verifyLifecycleIntegration(this);
            results.append("   Result: ").append(lifecycleOk ? "✓ PASS" : "✗ FAIL").append("\n\n");
            
            // Test 3: Deep Link Integration
            results.append("3. Deep Link Integration Test\n");
            boolean deepLinkOk = WorkflowIntegrationManager.verifyDeepLinkIntegration(this);
            results.append("   Result: ").append(deepLinkOk ? "✓ PASS" : "✗ FAIL").append("\n\n");
            
            // Test 4: Complete Workflow Integration
            results.append("4. Complete Workflow Integration Test\n");
            boolean completeOk = WorkflowIntegrationManager.verifyCompleteWorkflowIntegration(this);
            results.append("   Result: ").append(completeOk ? "✓ PASS" : "✗ FAIL").append("\n\n");
            
            // Overall Result
            boolean allTestsPassed = navigationOk && lifecycleOk && deepLinkOk && completeOk;
            results.append("================================\n");
            results.append("Overall Integration Status: ");
            results.append(allTestsPassed ? "✓ ALL TESTS PASSED" : "✗ SOME TESTS FAILED");
            results.append("\n================================\n\n");
            
            // Add detailed summary
            results.append(WorkflowIntegrationManager.getIntegrationStatusSummary(this));
            
            Log.i(TAG, "Integration test completed. Overall status: " + (allTestsPassed ? "PASS" : "FAIL"));
            
        } catch (Exception e) {
            results.append("ERROR: Integration test failed with exception: ");
            results.append(e.getMessage());
            Log.e(TAG, "Integration test failed", e);
        }
        
        resultView.setText(results.toString());
    }
}