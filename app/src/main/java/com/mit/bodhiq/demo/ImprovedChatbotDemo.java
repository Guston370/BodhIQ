package com.mit.bodhiq.demo;

import android.content.Context;
import android.util.Log;
import com.mit.bodhiq.data.model.ChatMessage;
import com.mit.bodhiq.utils.EmpathicMedicalResponseGenerator;

/**
 * Demonstration of the improved conversational chatbot with emoji formatting
 * and interactive follow-up questions
 */
public class ImprovedChatbotDemo {
    
    private static final String TAG = "ImprovedChatbotDemo";
    
    /**
     * Demonstrate the new conversational format with emojis
     */
    public static void demonstrateImprovedFormat() {
        Log.d(TAG, "\n🩺 === IMPROVED CONVERSATIONAL CHATBOT DEMO ===\n");
        
        // Example 1: Low Severity - Common Cold
        demonstrateLowSeverityExample();
        
        // Example 2: Moderate Severity - Persistent Symptoms
        demonstrateModerateSeverityExample();
        
        // Example 3: High Severity - Chest Pain
        demonstrateHighSeverityExample();
        
        // Example 4: Critical Severity - Emergency
        demonstrateCriticalSeverityExample();
        
        Log.d(TAG, "\n✅ IMPROVED CHATBOT DEMONSTRATION COMPLETE\n");
    }
    
    private static void demonstrateLowSeverityExample() {
        Log.d(TAG, "\n🟢 === LOW SEVERITY: Common Cold ===\n");
        
        String userInput = "I have a runny nose and mild headache for 2 days";
        
        Log.d(TAG, "👤 USER: \"" + userInput + "\"");
        Log.d(TAG, "\n🤖 BODHIQ RESPONSE:\n");
        
        Log.d(TAG, "🧩 **Analysis**: I understand that must be uncomfortable. Based on what you're describing — a runny nose and mild headache for 2 days — this is quite common and usually indicates a viral upper respiratory infection (common cold). When viruses infect your nasal passages, they cause inflammation and increased mucus production, which explains the runny nose. The headache often comes from sinus pressure or general body response to the infection.");
        
        Log.d(TAG, "\n💊 **Possible Remedies**:");
        Log.d(TAG, "• Get plenty of rest to help your immune system fight the infection");
        Log.d(TAG, "• Stay well-hydrated with water, herbal teas, or warm broths");
        Log.d(TAG, "• Use a humidifier or breathe steam from a hot shower to ease congestion");
        Log.d(TAG, "• Try over-the-counter pain relievers like acetaminophen for the headache");
        Log.d(TAG, "• Saline nasal rinses can help clear mucus and reduce congestion");
        
        Log.d(TAG, "\n🩺 **When to Consult a Doctor**:");
        Log.d(TAG, "• If symptoms persist beyond 7-10 days without improvement");
        Log.d(TAG, "• If you develop a high fever (over 101.3°F/38.5°C)");
        Log.d(TAG, "• If you experience severe headache, facial pain, or difficulty breathing");
        Log.d(TAG, "• If symptoms worsen significantly after initial improvement");
        
        Log.d(TAG, "\n🔍 **Follow-Up Question**: How long have you been experiencing this? Have you noticed anything that makes it better or worse?");
        
        Log.d(TAG, "\n⚠️ **Disclaimer**: This information is for educational purposes and should not replace professional medical advice.");
        
        Log.d(TAG, "\n✨ KEY IMPROVEMENTS:");
        Log.d(TAG, "• Empathetic opening: 'I understand that must be uncomfortable'");
        Log.d(TAG, "• Natural conversational flow");
        Log.d(TAG, "• Clear emoji-based structure");
        Log.d(TAG, "• Interactive follow-up question");
        Log.d(TAG, "• Specific, actionable remedies");
    }
    
    private static void demonstrateModerateSeverityExample() {
        Log.d(TAG, "\n🟠 === MODERATE SEVERITY: Persistent Digestive Issues ===\n");
        
        String userInput = "I've had stomach pain and bloating for a week, especially after eating";
        
        Log.d(TAG, "👤 USER: \"" + userInput + "\"");
        Log.d(TAG, "\n🤖 BODHIQ RESPONSE:\n");
        
        Log.d(TAG, "🧩 **Analysis**: I can see why this would be concerning. Stomach pain and bloating that occurs especially after eating for a week suggests several possibilities. This pattern could indicate gastritis (inflammation of the stomach lining), food intolerance, or functional dyspepsia. The fact that it's triggered by eating is significant — it tells us your digestive system may be reacting to the process of breaking down food. Given the duration of a week, this warrants attention and possibly some dietary adjustments.");
        
        Log.d(TAG, "\n💊 **Possible Remedies**:");
        Log.d(TAG, "• Try eating smaller, more frequent meals instead of large ones");
        Log.d(TAG, "• Temporarily avoid spicy, fatty, or acidic foods that can irritate the stomach");
        Log.d(TAG, "• Keep a food diary to identify potential trigger foods");
        Log.d(TAG, "• Stay upright for 2-3 hours after eating to aid digestion");
        Log.d(TAG, "• Consider over-the-counter antacids for temporary relief");
        Log.d(TAG, "• Ensure adequate hydration between meals (not during)");
        
        Log.d(TAG, "\n🩺 **When to Consult a Doctor**:");
        Log.d(TAG, "• If symptoms persist beyond 2 weeks despite dietary changes");
        Log.d(TAG, "• If you experience severe pain, vomiting, or unintended weight loss");
        Log.d(TAG, "• If you notice blood in vomit or stool");
        Log.d(TAG, "• If symptoms significantly interfere with daily activities");
        Log.d(TAG, "• Consider seeing a doctor within a week if no improvement");
        
        Log.d(TAG, "\n🔍 **Follow-Up Question**: Can you tell me when this started and whether it's been getting worse, staying the same, or improving?");
        
        Log.d(TAG, "\n⚠️ **Disclaimer**: This information is for educational purposes and should not replace professional medical advice.");
        
        Log.d(TAG, "\n✨ KEY IMPROVEMENTS:");
        Log.d(TAG, "• Empathetic acknowledgment: 'I can see why this would be concerning'");
        Log.d(TAG, "• Differential reasoning with multiple possibilities");
        Log.d(TAG, "• Medical context integration");
        Log.d(TAG, "• Specific timeframes for seeking care");
    }
    
    private static void demonstrateHighSeverityExample() {
        Log.d(TAG, "\n🔴 === HIGH SEVERITY: Chest Pain with Dizziness ===\n");
        
        String userInput = "I have chest pain and dizziness";
        
        Log.d(TAG, "👤 USER: \"" + userInput + "\"");
        Log.d(TAG, "\n🤖 BODHIQ RESPONSE:\n");
        
        Log.d(TAG, "🧩 **Analysis**: I'm glad you reached out. These symptoms need attention. Let me explain why. Chest pain combined with dizziness may indicate a possible cardiovascular issue, low blood pressure, or other conditions that affect blood flow. It's important to assess whether this pain occurs during rest or activity, and whether it's accompanied by other symptoms. While there can be less serious causes like anxiety or muscle strain, the combination of chest pain and dizziness warrants professional evaluation to rule out more serious conditions.");
        
        Log.d(TAG, "\n💊 **Possible Remedies**:");
        Log.d(TAG, "• Sit or lie down immediately in a comfortable position");
        Log.d(TAG, "• Rest and avoid any strenuous activity");
        Log.d(TAG, "• Stay hydrated if you haven't been drinking enough fluids");
        Log.d(TAG, "• Do not ignore persistent symptoms — seek medical evaluation");
        
        Log.d(TAG, "\n🩺 **When to Consult a Doctor**:");
        Log.d(TAG, "• You should see a healthcare provider within 24 hours");
        Log.d(TAG, "• If pain radiates to the arm, jaw, or you feel breathless or sweaty — seek emergency care immediately");
        Log.d(TAG, "• If dizziness is severe or you feel like you might faint — call 911");
        Log.d(TAG, "• If symptoms worsen or new symptoms develop — don't wait");
        
        Log.d(TAG, "\n🔍 **Follow-Up Question**: Is the pain sharp, dull, or pressure-like, and when does it occur? Have you noticed any other symptoms along with this?");
        
        Log.d(TAG, "\n⚠️ **Disclaimer**: This information is for educational purposes and should not replace professional medical advice.");
        
        Log.d(TAG, "\n✨ KEY IMPROVEMENTS:");
        Log.d(TAG, "• Calm but clear urgency: 'I'm glad you reached out. These symptoms need attention.'");
        Log.d(TAG, "• Explains why evaluation is needed");
        Log.d(TAG, "• Very specific urgency timeline (24 hours)");
        Log.d(TAG, "• Clear escalation criteria for emergency care");
    }
    
    private static void demonstrateCriticalSeverityExample() {
        Log.d(TAG, "\n🚨 === CRITICAL SEVERITY: Severe Chest Pain ===\n");
        
        String userInput = "I have severe chest pain, difficulty breathing, and I'm sweating a lot";
        
        Log.d(TAG, "👤 USER: \"" + userInput + "\"");
        Log.d(TAG, "\n🤖 BODHIQ RESPONSE:\n");
        
        Log.d(TAG, "🧩 **Analysis**: This sounds serious. Please seek immediate medical attention or contact a healthcare provider. The combination of severe chest pain, difficulty breathing, and sweating can be associated with serious conditions like a heart attack, pulmonary embolism, or other cardiovascular emergencies. These symptoms require immediate evaluation because time is critical in treating such conditions.");
        
        Log.d(TAG, "\n💊 **Possible Remedies**:");
        Log.d(TAG, "• Call 911 immediately — do not drive yourself");
        Log.d(TAG, "• Sit down in a comfortable position while waiting for help");
        Log.d(TAG, "• Loosen any tight clothing around your chest and neck");
        Log.d(TAG, "• Stay as calm as possible and focus on breathing");
        Log.d(TAG, "• If you have prescribed nitroglycerin, take as directed");
        Log.d(TAG, "• Have someone stay with you if possible");
        
        Log.d(TAG, "\n🩺 **When to Consult a Doctor**:");
        Log.d(TAG, "• This requires IMMEDIATE emergency medical attention");
        Log.d(TAG, "• Call 911 or go to the nearest emergency room RIGHT NOW");
        Log.d(TAG, "• Do not wait to see if symptoms improve");
        Log.d(TAG, "• Time is critical with these types of symptoms");
        Log.d(TAG, "• Emergency services can provide life-saving treatment en route");
        
        Log.d(TAG, "\n🔍 **Follow-Up Question**: Are you able to get to emergency care right now? Is there someone with you who can help?");
        
        Log.d(TAG, "\n⚠️ **Disclaimer**: This information is for educational purposes and should not replace professional medical advice.");
        
        Log.d(TAG, "\n✨ KEY IMPROVEMENTS:");
        Log.d(TAG, "• Immediate clear message: 'This sounds serious. Please seek immediate medical attention'");
        Log.d(TAG, "• Urgent yet calm and supportive tone");
        Log.d(TAG, "• Specific emergency actions");
        Log.d(TAG, "• Follow-up about ability to get help");
    }
    
    /**
     * Compare old vs new format
     */
    public static void demonstrateBeforeAfterComparison() {
        Log.d(TAG, "\n📊 === BEFORE vs AFTER COMPARISON ===\n");
        
        String userInput = "I have a persistent cough and mild fever for 3 days";
        
        Log.d(TAG, "👤 USER: \"" + userInput + "\"");
        
        // OLD FORMAT
        Log.d(TAG, "\n❌ OLD FORMAT (Clinical but less conversational):");
        Log.d(TAG, "(1) Analysis: Your persistent cough and mild fever over 3 days suggests a likely viral upper respiratory infection.");
        Log.d(TAG, "\n(2) Possible Remedies:");
        Log.d(TAG, "• Rest to support immune function");
        Log.d(TAG, "• Increase fluid intake");
        Log.d(TAG, "\n(3) When to Consult a Doctor:");
        Log.d(TAG, "• If fever exceeds 101.3°F");
        Log.d(TAG, "\nThis analysis is for informational purposes only and should not replace professional medical advice.");
        
        // NEW FORMAT
        Log.d(TAG, "\n✅ NEW FORMAT (Conversational with emojis):");
        Log.d(TAG, "🧩 **Analysis**: I understand that must be uncomfortable. Based on what you're describing — a persistent cough and mild fever for 3 days — this is quite common and usually indicates a viral upper respiratory infection. Your immune system is responding to a pathogen, and the cough is actually a protective mechanism to clear irritants from your airways.");
        Log.d(TAG, "\n💊 **Possible Remedies**:");
        Log.d(TAG, "• Get plenty of rest to help your immune system fight the infection");
        Log.d(TAG, "• Increase fluid intake to thin mucus secretions and stay hydrated");
        Log.d(TAG, "• Use honey or throat lozenges for cough suppression (honey works well!)");
        Log.d(TAG, "• Consider over-the-counter fever reducers if you're uncomfortable");
        Log.d(TAG, "• Humidified air can help soothe irritated airways");
        Log.d(TAG, "\n🩺 **When to Consult a Doctor**:");
        Log.d(TAG, "• If fever exceeds 101.3°F (38.5°C) or persists beyond 5 days");
        Log.d(TAG, "• If cough produces blood or becomes severely painful");
        Log.d(TAG, "• If you develop shortness of breath or chest pain");
        Log.d(TAG, "• If symptoms worsen after initial improvement");
        Log.d(TAG, "\n🔍 **Follow-Up Question**: How long have you been experiencing this? Have you noticed anything that makes it better or worse?");
        Log.d(TAG, "\n⚠️ **Disclaimer**: This information is for educational purposes and should not replace professional medical advice.");
        
        Log.d(TAG, "\n🔍 KEY DIFFERENCES:");
        Log.d(TAG, "• OLD: Clinical and formal → NEW: Conversational and empathetic");
        Log.d(TAG, "• OLD: Plain text → NEW: Emoji-based structure for clarity");
        Log.d(TAG, "• OLD: Brief explanations → NEW: Detailed, natural explanations");
        Log.d(TAG, "• OLD: No follow-up → NEW: Interactive follow-up question");
        Log.d(TAG, "• OLD: Generic remedies → NEW: Specific, actionable advice with context");
    }
    
    /**
     * Run complete demonstration
     */
    public static void runCompleteDemo() {
        Log.d(TAG, "\n🚀 STARTING IMPROVED CHATBOT DEMO 🚀");
        
        demonstrateImprovedFormat();
        demonstrateBeforeAfterComparison();
        
        Log.d(TAG, "\n🎉 === DEMO SUMMARY ===\n");
        Log.d(TAG, "✅ Emoji-based structured format implemented");
        Log.d(TAG, "✅ Natural conversational flow");
        Log.d(TAG, "✅ Empathetic openings for each response");
        Log.d(TAG, "✅ Interactive follow-up questions");
        Log.d(TAG, "✅ Differential reasoning with medical context");
        Log.d(TAG, "✅ Specific, actionable remedies");
        Log.d(TAG, "✅ Clear urgency levels and timeframes");
        Log.d(TAG, "✅ Standardized disclaimer");
        
        Log.d(TAG, "\n🌟 The BodhIQ chatbot now communicates like a trusted");
        Log.d(TAG, "family doctor with empathy, clarity, and professionalism! 🌟\n");
    }
}
