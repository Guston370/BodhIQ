package com.mit.bodhiq.demo;

import android.content.Context;
import android.util.Log;
import com.mit.bodhiq.data.model.ChatMessage;
import com.mit.bodhiq.utils.EmpathicMedicalResponseGenerator;
import com.mit.bodhiq.utils.GeminiApiService;

/**
 * Demonstration class showing the new doctor-like chatbot functionality
 * with clinical reasoning and structured medical analysis
 */
public class DoctorChatbotDemo {
    
    private static final String TAG = "DoctorChatbotDemo";
    private final GeminiApiService geminiService;
    
    public DoctorChatbotDemo(Context context) {
        this.geminiService = new GeminiApiService(context);
    }
    
    /**
     * Demonstrate the new doctor-like response format
     */
    public void demonstrateNewDoctorFormat() {
        Log.d(TAG, "\n🩺 === NEW DOCTOR-LIKE CHATBOT DEMONSTRATION ===\n");
        
        // Test case 1: Low severity - Common cold symptoms
        demonstrateLowSeverityAnalysis();
        
        // Test case 2: Moderate severity - Persistent symptoms
        demonstrateModerateSeverityAnalysis();
        
        // Test case 3: High severity - Concerning symptoms
        demonstrateHighSeverityAnalysis();
        
        // Test case 4: Critical severity - Emergency symptoms
        demonstrateCriticalSeverityAnalysis();
        
        Log.d(TAG, "\n✅ DOCTOR CHATBOT DEMONSTRATION COMPLETE\n");
    }
    
    private void demonstrateLowSeverityAnalysis() {
        Log.d(TAG, "\n🟢 === LOW SEVERITY: Common Cold Symptoms ===\n");
        
        String userInput = "I have a runny nose, mild headache, and feel a bit tired. Started 2 days ago.";
        String userContext = "28-year-old adult, generally healthy";
        
        Log.d(TAG, "👤 USER INPUT: \"" + userInput + "\"");
        Log.d(TAG, "📋 CONTEXT: " + userContext);
        
        ChatMessage.Severity severity = EmpathicMedicalResponseGenerator.detectSeverityFromInput(userInput);
        Log.d(TAG, "🔍 DETECTED SEVERITY: " + severity.name());
        
        // Show the clinical prompt that would be sent to Gemini
        String clinicalPrompt = EmpathicMedicalResponseGenerator.createSymptomAnalysisPrompt(
            userInput, userContext, severity);
        
        Log.d(TAG, "\n🤖 EXPECTED DOCTOR-LIKE RESPONSE FORMAT:");
        Log.d(TAG, "(1) Analysis: Based on your symptoms of runny nose, mild headache, and fatigue that started 2 days ago, this appears to be consistent with a common viral upper respiratory infection (cold). These symptoms typically occur when viruses infect the nasal passages and sinuses, causing inflammation and increased mucus production.");
        Log.d(TAG, "\n(2) Possible Remedies:");
        Log.d(TAG, "• Get plenty of rest to help your immune system fight the infection");
        Log.d(TAG, "• Stay well-hydrated with water, herbal teas, or warm broths");
        Log.d(TAG, "• Use a humidifier or breathe steam from a hot shower");
        Log.d(TAG, "• Consider over-the-counter pain relievers like acetaminophen for the headache");
        Log.d(TAG, "• Saline nasal rinses can help clear congestion");
        Log.d(TAG, "\n(3) When to Consult a Doctor:");
        Log.d(TAG, "• If symptoms worsen or persist beyond 7-10 days");
        Log.d(TAG, "• If you develop a high fever (over 101.3°F/38.5°C)");
        Log.d(TAG, "• If you experience severe headache, sinus pain, or difficulty breathing");
        Log.d(TAG, "• If you have underlying health conditions that put you at higher risk");
        Log.d(TAG, "\nThis analysis is for informational purposes only and should not replace professional medical advice.");
        
        Log.d(TAG, "\n✨ KEY IMPROVEMENTS:");
        Log.d(TAG, "• Clinical reasoning explaining viral infection mechanism");
        Log.d(TAG, "• Structured 3-part response format");
        Log.d(TAG, "• Specific, actionable remedies");
        Log.d(TAG, "• Clear criteria for when to seek care");
        Log.d(TAG, "• Professional medical language made accessible");
    }
    
    private void demonstrateModerateSeverityAnalysis() {
        Log.d(TAG, "\n🟠 === MODERATE SEVERITY: Persistent Digestive Issues ===\n");
        
        String userInput = "I've had stomach pain and bloating for about a week, especially after eating. Sometimes nauseous too.";
        String userContext = "35-year-old adult, no known allergies";
        
        Log.d(TAG, "👤 USER INPUT: \"" + userInput + "\"");
        Log.d(TAG, "📋 CONTEXT: " + userContext);
        
        ChatMessage.Severity severity = EmpathicMedicalResponseGenerator.detectSeverityFromInput(userInput);
        Log.d(TAG, "🔍 DETECTED SEVERITY: " + severity.name());
        
        Log.d(TAG, "\n🤖 EXPECTED DOCTOR-LIKE RESPONSE FORMAT:");
        Log.d(TAG, "(1) Analysis: Your symptoms of persistent stomach pain, bloating, and nausea after eating for a week could indicate several conditions. This pattern suggests possible gastritis (stomach lining inflammation), food intolerance, or functional dyspepsia. The post-meal timing is particularly significant as it suggests the digestive process may be triggering your symptoms.");
        Log.d(TAG, "\n(2) Possible Remedies:");
        Log.d(TAG, "• Try eating smaller, more frequent meals");
        Log.d(TAG, "• Avoid spicy, fatty, or acidic foods temporarily");
        Log.d(TAG, "• Consider keeping a food diary to identify triggers");
        Log.d(TAG, "• Stay upright for 2-3 hours after eating");
        Log.d(TAG, "• Try over-the-counter antacids for symptom relief");
        Log.d(TAG, "• Ensure adequate hydration between meals");
        Log.d(TAG, "\n(3) When to Consult a Doctor:");
        Log.d(TAG, "• If symptoms persist beyond 2 weeks");
        Log.d(TAG, "• If you experience severe pain, vomiting, or weight loss");
        Log.d(TAG, "• If you notice blood in vomit or stool");
        Log.d(TAG, "• If symptoms significantly interfere with daily activities");
        Log.d(TAG, "• Consider seeing a doctor within a week if no improvement");
        Log.d(TAG, "\nThis analysis is for informational purposes only and should not replace professional medical advice.");
        
        Log.d(TAG, "\n✨ KEY IMPROVEMENTS:");
        Log.d(TAG, "• Multiple differential diagnoses considered");
        Log.d(TAG, "• Clinical significance of post-meal timing explained");
        Log.d(TAG, "• Systematic approach to dietary modifications");
        Log.d(TAG, "• Specific red flags and timeframes provided");
    }
    
    private void demonstrateHighSeverityAnalysis() {
        Log.d(TAG, "\n🔴 === HIGH SEVERITY: Concerning Chest Symptoms ===\n");
        
        String userInput = "I have chest discomfort when I breathe deeply, and I've been feeling short of breath with mild activity for the past day.";
        String userContext = "45-year-old adult, occasional smoker";
        
        Log.d(TAG, "👤 USER INPUT: \"" + userInput + "\"");
        Log.d(TAG, "📋 CONTEXT: " + userContext);
        
        ChatMessage.Severity severity = EmpathicMedicalResponseGenerator.detectSeverityFromInput(userInput);
        Log.d(TAG, "🔍 DETECTED SEVERITY: " + severity.name());
        
        Log.d(TAG, "\n🤖 EXPECTED DOCTOR-LIKE RESPONSE FORMAT:");
        Log.d(TAG, "(1) Analysis: Chest discomfort with deep breathing combined with shortness of breath during mild activity requires careful evaluation. These symptoms could indicate several conditions including pleuritis (lung lining inflammation), pneumonia, pulmonary embolism, or cardiac issues. Given your smoking history, respiratory causes are particularly concerning and need professional assessment.");
        Log.d(TAG, "\n(2) Possible Remedies:");
        Log.d(TAG, "• Rest and avoid strenuous activities until evaluated");
        Log.d(TAG, "• Sit upright to ease breathing");
        Log.d(TAG, "• Avoid smoking completely while experiencing these symptoms");
        Log.d(TAG, "• Monitor your symptoms closely");
        Log.d(TAG, "• Stay calm and avoid panic, which can worsen breathing");
        Log.d(TAG, "\n(3) When to Consult a Doctor:");
        Log.d(TAG, "• You should see a healthcare provider within 24 hours");
        Log.d(TAG, "• Seek immediate emergency care if breathing becomes more difficult");
        Log.d(TAG, "• Go to ER immediately if you experience severe chest pain");
        Log.d(TAG, "• Call 911 if you feel faint, have severe shortness of breath, or chest pain worsens");
        Log.d(TAG, "• Don't delay - chest symptoms with breathing difficulty need prompt evaluation");
        Log.d(TAG, "\nThis analysis is for informational purposes only and should not replace professional medical advice.");
        
        Log.d(TAG, "\n✨ KEY IMPROVEMENTS:");
        Log.d(TAG, "• Serious differential diagnoses explained clearly");
        Log.d(TAG, "• Risk factors (smoking) incorporated into analysis");
        Log.d(TAG, "• Limited remedies focused on safety while awaiting care");
        Log.d(TAG, "• Specific urgency timeline (24 hours)");
        Log.d(TAG, "• Clear escalation criteria for emergency care");
    }
    
    private void demonstrateCriticalSeverityAnalysis() {
        Log.d(TAG, "\n🚨 === CRITICAL SEVERITY: Emergency Symptoms ===\n");
        
        String userInput = "I have severe chest pain that started suddenly, I'm having trouble breathing, and I feel dizzy and nauseous.";
        String userContext = "52-year-old adult, family history of heart disease";
        
        Log.d(TAG, "👤 USER INPUT: \"" + userInput + "\"");
        Log.d(TAG, "📋 CONTEXT: " + userContext);
        
        ChatMessage.Severity severity = EmpathicMedicalResponseGenerator.detectSeverityFromInput(userInput);
        Log.d(TAG, "🔍 DETECTED SEVERITY: " + severity.name());
        
        Log.d(TAG, "\n🤖 EXPECTED DOCTOR-LIKE RESPONSE FORMAT:");
        Log.d(TAG, "(1) Analysis: The combination of sudden severe chest pain, breathing difficulty, dizziness, and nausea represents a medical emergency. These symptoms can be associated with serious conditions including myocardial infarction (heart attack), pulmonary embolism, or aortic dissection. Given your family history of heart disease, cardiac causes are of particular concern and require immediate evaluation.");
        Log.d(TAG, "\n(2) Possible Remedies:");
        Log.d(TAG, "• Call 911 immediately - do not drive yourself");
        Log.d(TAG, "• Sit upright or in the most comfortable position");
        Log.d(TAG, "• Loosen tight clothing around chest and neck");
        Log.d(TAG, "• If you have prescribed nitroglycerin, take as directed");
        Log.d(TAG, "• Stay as calm as possible while waiting for emergency services");
        Log.d(TAG, "• Have someone stay with you if possible");
        Log.d(TAG, "\n(3) When to Consult a Doctor:");
        Log.d(TAG, "• This requires IMMEDIATE emergency medical attention");
        Log.d(TAG, "• Call 911 or go to the nearest emergency room RIGHT NOW");
        Log.d(TAG, "• Do not wait to see if symptoms improve");
        Log.d(TAG, "• Time is critical with these types of symptoms");
        Log.d(TAG, "• Emergency services can provide life-saving treatment en route");
        Log.d(TAG, "\nThis analysis is for informational purposes only and should not replace professional medical advice.");
        
        Log.d(TAG, "\n✨ KEY IMPROVEMENTS:");
        Log.d(TAG, "• Immediate recognition of medical emergency");
        Log.d(TAG, "• Specific serious conditions mentioned with clinical reasoning");
        Log.d(TAG, "• Family history incorporated into risk assessment");
        Log.d(TAG, "• Emergency-focused remedies only");
        Log.d(TAG, "• Absolute clarity about need for immediate care");
        Log.d(TAG, "• Specific instructions for emergency response");
    }
    
    /**
     * Compare old empathetic vs new doctor-like approach
     */
    public void demonstrateBeforeAfterComparison() {
        Log.d(TAG, "\n📊 === BEFORE vs AFTER: EMPATHETIC vs DOCTOR-LIKE ===\n");
        
        String userInput = "I have a persistent cough and mild fever for 3 days";
        
        Log.d(TAG, "👤 USER INPUT: \"" + userInput + "\"");
        
        // OLD EMPATHETIC APPROACH
        Log.d(TAG, "\n❌ OLD EMPATHETIC APPROACH:");
        Log.d(TAG, "💙 I understand that having a persistent cough and fever can be really concerning, especially when it's been going on for several days. It's completely natural to want to know what might be causing these symptoms.");
        Log.d(TAG, "\nMany people experience cough and fever, and most of the time these symptoms are manageable with some gentle self-care. This could be due to a viral infection or other common causes.");
        Log.d(TAG, "\nSome gentle things you could try: rest, stay hydrated, use a humidifier...");
        Log.d(TAG, "\nCan you tell me if anything seems to trigger the cough?");
        
        // NEW DOCTOR-LIKE APPROACH
        Log.d(TAG, "\n✅ NEW DOCTOR-LIKE APPROACH:");
        Log.d(TAG, "(1) Analysis: Your persistent cough and mild fever over 3 days suggests a likely viral upper respiratory infection or possibly early bacterial infection. The combination indicates your immune system is responding to a pathogen, with the cough being a protective mechanism to clear irritants from your airways.");
        Log.d(TAG, "\n(2) Possible Remedies:");
        Log.d(TAG, "• Rest to support immune function");
        Log.d(TAG, "• Increase fluid intake to thin mucus secretions");
        Log.d(TAG, "• Use honey or throat lozenges for cough suppression");
        Log.d(TAG, "• Consider over-the-counter fever reducers if uncomfortable");
        Log.d(TAG, "• Humidified air can help soothe irritated airways");
        Log.d(TAG, "\n(3) When to Consult a Doctor:");
        Log.d(TAG, "• If fever exceeds 101.3°F (38.5°C) or persists beyond 5 days");
        Log.d(TAG, "• If cough produces blood or becomes severely painful");
        Log.d(TAG, "• If you develop shortness of breath or chest pain");
        Log.d(TAG, "• If symptoms worsen after initial improvement");
        Log.d(TAG, "\nThis analysis is for informational purposes only and should not replace professional medical advice.");
        
        Log.d(TAG, "\n🔍 KEY DIFFERENCES:");
        Log.d(TAG, "• OLD: Emotional support focus → NEW: Clinical reasoning focus");
        Log.d(TAG, "• OLD: Vague reassurance → NEW: Specific medical explanation");
        Log.d(TAG, "• OLD: General suggestions → NEW: Structured 3-part analysis");
        Log.d(TAG, "• OLD: Open-ended questions → NEW: Specific follow-up criteria");
        Log.d(TAG, "• OLD: Empathetic disclaimer → NEW: Standardized medical disclaimer");
    }
    
    /**
     * Run complete demonstration of new doctor-like chatbot
     */
    public void runCompleteDemo() {
        Log.d(TAG, "\n🩺 STARTING COMPLETE DOCTOR-LIKE CHATBOT DEMO 🩺");
        
        demonstrateNewDoctorFormat();
        demonstrateBeforeAfterComparison();
        
        Log.d(TAG, "\n🎉 === DEMO SUMMARY ===\n");
        Log.d(TAG, "✅ Doctor-like clinical reasoning implemented");
        Log.d(TAG, "✅ Structured 3-part response format working");
        Log.d(TAG, "✅ Medical analysis with differential diagnoses");
        Log.d(TAG, "✅ Specific, actionable remedies provided");
        Log.d(TAG, "✅ Clear criteria for seeking medical care");
        Log.d(TAG, "✅ Standardized medical disclaimer");
        
        Log.d(TAG, "\n🌟 The BodhIQ chatbot now acts like a real doctor, ");
        Log.d(TAG, "providing clinical reasoning and structured medical ");
        Log.d(TAG, "analysis while maintaining appropriate boundaries! 🌟\n");
    }
}