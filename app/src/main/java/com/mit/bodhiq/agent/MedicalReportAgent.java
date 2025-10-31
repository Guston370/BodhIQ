package com.mit.bodhiq.agent;

import com.mit.bodhiq.data.database.entity.AgentResult;
import com.mit.bodhiq.data.model.MedicalParameter;
import com.mit.bodhiq.data.model.MedicalInsight;
import com.mit.bodhiq.data.model.UserProfile;
import com.mit.bodhiq.data.model.ChatMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Agent for generating AI insights from medical report parameters
 */
@Singleton
public class MedicalReportAgent implements PharmaceuticalAgent {
    
    private static final String AGENT_NAME = "Medical Report Analyzer";
    private static final int EXECUTION_ORDER = 100;
    private static final int ESTIMATED_DURATION_MS = 3000;
    
    @Inject
    public MedicalReportAgent() {}
    
    @Override
    public String getAgentName() {
        return AGENT_NAME;
    }
    
    @Override
    public int getExecutionOrder() {
        return EXECUTION_ORDER;
    }
    
    @Override
    public int getEstimatedDurationMs() {
        return ESTIMATED_DURATION_MS;
    }
    
    @Override
    public Single<AgentResult> execute(String molecule, long queryId) {
        // This method is for the existing pharmaceutical pipeline
        // For medical reports, use the generateInsights method directly
        return Single.error(new UnsupportedOperationException(
            "Use generateInsights method for medical report analysis"));
    }
    
    /**
     * Generate comprehensive medical insights with user profile context
     */
    public Single<ChatMessage> generateMedicalInsights(List<MedicalParameter> parameters, UserProfile userProfile, String reportId) {
        return Single.fromCallable(() -> {
            ChatMessage response = new ChatMessage();
            response.setType(ChatMessage.MessageType.AI_MEDICAL_CARD);
            response.setFromUser(false);
            response.setMedicalParameters(parameters);
            response.setReportId(reportId);
            
            if (parameters == null || parameters.isEmpty()) {
                response.setContent(generateNoParametersInsight());
                response.setSeverity(ChatMessage.Severity.LOW);
                return response;
            }
            
            // Generate comprehensive analysis
            List<MedicalInsight> insights = generateDetailedInsights(parameters, userProfile);
            String analysisText = formatInsightsAsText(insights);
            
            response.setContent(analysisText);
            response.setSeverity(calculateOverallSeverity(insights));
            response.setRequiresFollowUp(shouldRequireFollowUp(insights));
            response.setRecommendations(generateRecommendations(insights, userProfile));
            response.setMedicalDisclaimer(getStandardDisclaimer());
            
            return response;
            
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Generate AI insights from medical parameters (legacy method)
     */
    public Single<String> generateInsights(List<MedicalParameter> parameters) {
        return generateMedicalInsights(parameters, null, null)
                .map(ChatMessage::getContent);
    }

    /**
     * Generate detailed medical insights with risk assessment
     */
    private List<MedicalInsight> generateDetailedInsights(List<MedicalParameter> parameters, UserProfile userProfile) {
        List<MedicalInsight> insights = new ArrayList<>();
        
        // Analyze by medical categories
        insights.addAll(analyzeBloodWorkAdvanced(parameters, userProfile));
        insights.addAll(analyzeVitalSignsAdvanced(parameters, userProfile));
        insights.addAll(analyzeMetabolicMarkersAdvanced(parameters, userProfile));
        insights.addAll(analyzeLiverFunction(parameters, userProfile));
        insights.addAll(analyzeKidneyFunction(parameters, userProfile));
        insights.addAll(analyzeThyroidFunction(parameters, userProfile));
        
        return insights;
    }

    /**
     * Advanced blood work analysis with risk stratification
     */
    private List<MedicalInsight> analyzeBloodWorkAdvanced(List<MedicalParameter> parameters, UserProfile userProfile) {
        List<MedicalInsight> insights = new ArrayList<>();
        
        for (MedicalParameter param : parameters) {
            String paramName = param.getParameter().toLowerCase();
            
            if (paramName.contains("hemoglobin") || paramName.contains("hgb") || paramName.contains("hb")) {
                MedicalInsight insight = new MedicalInsight();
                insight.setType(MedicalInsight.InsightType.BLOOD_WORK);
                insight.setTitle("Hemoglobin Analysis");
                insight.setAffectedParameters(Arrays.asList("Hemoglobin"));
                
                if ("LOW".equals(param.getStatus())) {
                    insight.setRiskLevel(MedicalInsight.RiskLevel.MODERATE_CONCERN);
                    insight.setDescription("Your hemoglobin level (" + param.getValue() + " " + param.getUnit() + 
                        ") is below the normal range. This may indicate anemia, which can cause fatigue, weakness, and shortness of breath.");
                    insight.setRecommendations(Arrays.asList(
                        "Increase iron-rich foods (spinach, red meat, beans)",
                        "Consider vitamin C with iron-rich meals for better absorption",
                        "Avoid tea and coffee with meals as they can inhibit iron absorption",
                        "Get adequate sleep and manage stress"
                    ));
                    insight.setSuggestedTests(Arrays.asList("Iron studies", "Vitamin B12", "Folate", "Reticulocyte count"));
                    insight.setWhenToSeeDoctor("Schedule an appointment within 1-2 weeks to investigate the cause of anemia");
                } else if ("HIGH".equals(param.getStatus())) {
                    insight.setRiskLevel(MedicalInsight.RiskLevel.MILD_CONCERN);
                    insight.setDescription("Your hemoglobin level (" + param.getValue() + " " + param.getUnit() + 
                        ") is above the normal range. This could indicate dehydration, lung disease, or other conditions.");
                    insight.setRecommendations(Arrays.asList(
                        "Ensure adequate hydration",
                        "Monitor for symptoms like headaches or dizziness",
                        "Avoid smoking and excessive alcohol"
                    ));
                    insight.setWhenToSeeDoctor("Consult your healthcare provider within 2-4 weeks for evaluation");
                } else {
                    insight.setRiskLevel(MedicalInsight.RiskLevel.NORMAL);
                    insight.setDescription("Your hemoglobin level (" + param.getValue() + " " + param.getUnit() + 
                        ") is within the normal range, indicating good oxygen-carrying capacity.");
                    insight.setRecommendations(Arrays.asList(
                        "Maintain a balanced diet rich in iron",
                        "Continue regular physical activity",
                        "Stay hydrated"
                    ));
                }
                
                insight.setConfidenceScore(0.85);
                insights.add(insight);
            }
            
            // White Blood Cell analysis
            if (paramName.contains("wbc") || paramName.contains("white blood cell")) {
                MedicalInsight insight = new MedicalInsight();
                insight.setType(MedicalInsight.InsightType.BLOOD_WORK);
                insight.setTitle("White Blood Cell Analysis");
                insight.setAffectedParameters(Arrays.asList("White Blood Cells"));
                
                if ("HIGH".equals(param.getStatus())) {
                    insight.setRiskLevel(MedicalInsight.RiskLevel.HIGH_CONCERN);
                    insight.setDescription("Your white blood cell count (" + param.getValue() + " " + param.getUnit() + 
                        ") is elevated. This may indicate an infection, inflammation, or immune system response.");
                    insight.setRecommendations(Arrays.asList(
                        "Monitor for fever, chills, or other signs of infection",
                        "Get adequate rest and stay hydrated",
                        "Avoid contact with sick individuals",
                        "Practice good hygiene"
                    ));
                    insight.setSuggestedTests(Arrays.asList("Blood culture", "Complete blood count with differential"));
                    insight.setWhenToSeeDoctor("Contact your healthcare provider within 24-48 hours");
                    insight.setEmergencyFlag(true);
                } else if ("LOW".equals(param.getStatus())) {
                    insight.setRiskLevel(MedicalInsight.RiskLevel.MODERATE_CONCERN);
                    insight.setDescription("Your white blood cell count (" + param.getValue() + " " + param.getUnit() + 
                        ") is below normal. This could indicate a weakened immune system.");
                    insight.setRecommendations(Arrays.asList(
                        "Avoid crowded places and sick individuals",
                        "Practice excellent hygiene",
                        "Eat a nutrient-rich diet",
                        "Get adequate sleep and manage stress"
                    ));
                    insight.setWhenToSeeDoctor("Schedule an appointment within 1 week");
                }
                
                insight.setConfidenceScore(0.90);
                insights.add(insight);
            }
        }
        
        return insights;
    }

    /**
     * Advanced vital signs analysis
     */
    private List<MedicalInsight> analyzeVitalSignsAdvanced(List<MedicalParameter> parameters, UserProfile userProfile) {
        List<MedicalInsight> insights = new ArrayList<>();
        
        for (MedicalParameter param : parameters) {
            String paramName = param.getParameter().toLowerCase();
            
            if (paramName.contains("blood pressure") || paramName.contains("systolic") || paramName.contains("diastolic")) {
                MedicalInsight insight = new MedicalInsight();
                insight.setType(MedicalInsight.InsightType.VITAL_SIGNS);
                insight.setTitle("Blood Pressure Analysis");
                insight.setAffectedParameters(Arrays.asList("Blood Pressure"));
                
                if ("HIGH".equals(param.getStatus())) {
                    insight.setRiskLevel(MedicalInsight.RiskLevel.HIGH_CONCERN);
                    insight.setDescription("Your blood pressure (" + param.getValue() + " " + param.getUnit() + 
                        ") is elevated. High blood pressure increases your risk of heart disease, stroke, and kidney problems.");
                    insight.setRecommendations(Arrays.asList(
                        "Reduce sodium intake to less than 2,300mg per day",
                        "Engage in regular aerobic exercise (30 minutes, 5 days/week)",
                        "Maintain a healthy weight",
                        "Limit alcohol consumption",
                        "Manage stress through relaxation techniques",
                        "Quit smoking if applicable"
                    ));
                    insight.setSuggestedTests(Arrays.asList("24-hour blood pressure monitoring", "Echocardiogram", "Kidney function tests"));
                    insight.setWhenToSeeDoctor("Schedule an appointment within 1-2 weeks for blood pressure management");
                } else if ("LOW".equals(param.getStatus())) {
                    insight.setRiskLevel(MedicalInsight.RiskLevel.MILD_CONCERN);
                    insight.setDescription("Your blood pressure (" + param.getValue() + " " + param.getUnit() + 
                        ") is below normal. While often not concerning, very low blood pressure can cause symptoms.");
                    insight.setRecommendations(Arrays.asList(
                        "Stay well hydrated",
                        "Rise slowly from sitting or lying positions",
                        "Wear compression stockings if recommended",
                        "Monitor for dizziness or fainting"
                    ));
                    insight.setWhenToSeeDoctor("Consult if you experience dizziness, fainting, or other symptoms");
                }
                
                insight.setConfidenceScore(0.88);
                insights.add(insight);
            }
        }
        
        return insights;
    }

    /**
     * Advanced metabolic markers analysis
     */
    private List<MedicalInsight> analyzeMetabolicMarkersAdvanced(List<MedicalParameter> parameters, UserProfile userProfile) {
        List<MedicalInsight> insights = new ArrayList<>();
        
        for (MedicalParameter param : parameters) {
            String paramName = param.getParameter().toLowerCase();
            
            if (paramName.contains("glucose") || paramName.contains("sugar")) {
                MedicalInsight insight = new MedicalInsight();
                insight.setType(MedicalInsight.InsightType.METABOLIC);
                insight.setTitle("Blood Glucose Analysis");
                insight.setAffectedParameters(Arrays.asList("Glucose"));
                
                if ("HIGH".equals(param.getStatus())) {
                    double glucoseValue = Double.parseDouble(param.getValue());
                    if (glucoseValue > 200) {
                        insight.setRiskLevel(MedicalInsight.RiskLevel.CRITICAL);
                        insight.setEmergencyFlag(true);
                        insight.setWhenToSeeDoctor("Seek immediate medical attention - blood glucose is critically high");
                    } else if (glucoseValue > 140) {
                        insight.setRiskLevel(MedicalInsight.RiskLevel.HIGH_CONCERN);
                        insight.setWhenToSeeDoctor("Schedule an appointment within 1-2 days");
                    } else {
                        insight.setRiskLevel(MedicalInsight.RiskLevel.MODERATE_CONCERN);
                        insight.setWhenToSeeDoctor("Schedule an appointment within 1 week");
                    }
                    
                    insight.setDescription("Your blood glucose (" + param.getValue() + " " + param.getUnit() + 
                        ") is elevated. This may indicate diabetes or prediabetes.");
                    insight.setRecommendations(Arrays.asList(
                        "Follow a low-carbohydrate, high-fiber diet",
                        "Engage in regular physical activity",
                        "Monitor blood glucose if you have a meter",
                        "Stay hydrated with water",
                        "Avoid sugary drinks and processed foods"
                    ));
                    insight.setSuggestedTests(Arrays.asList("HbA1c", "Fasting glucose", "Oral glucose tolerance test"));
                }
                
                insight.setConfidenceScore(0.92);
                insights.add(insight);
            }
        }
        
        return insights;
    }

    /**
     * Liver function analysis
     */
    private List<MedicalInsight> analyzeLiverFunction(List<MedicalParameter> parameters, UserProfile userProfile) {
        List<MedicalInsight> insights = new ArrayList<>();
        
        boolean hasElevatedLiverEnzymes = false;
        List<String> elevatedEnzymes = new ArrayList<>();
        
        for (MedicalParameter param : parameters) {
            String paramName = param.getParameter().toLowerCase();
            
            if ((paramName.contains("alt") || paramName.contains("ast") || paramName.contains("bilirubin")) 
                && "HIGH".equals(param.getStatus())) {
                hasElevatedLiverEnzymes = true;
                elevatedEnzymes.add(param.getParameter());
            }
        }
        
        if (hasElevatedLiverEnzymes) {
            MedicalInsight insight = new MedicalInsight();
            insight.setType(MedicalInsight.InsightType.LIVER_FUNCTION);
            insight.setTitle("Liver Function Analysis");
            insight.setRiskLevel(MedicalInsight.RiskLevel.MODERATE_CONCERN);
            insight.setAffectedParameters(elevatedEnzymes);
            insight.setDescription("Your liver enzymes are elevated, which may indicate liver inflammation or damage.");
            insight.setRecommendations(Arrays.asList(
                "Avoid alcohol completely",
                "Limit acetaminophen use",
                "Maintain a healthy weight",
                "Eat a liver-friendly diet (low fat, high antioxidants)",
                "Stay hydrated"
            ));
            insight.setSuggestedTests(Arrays.asList("Hepatitis panel", "Ultrasound of liver", "Additional liver function tests"));
            insight.setWhenToSeeDoctor("Schedule an appointment within 1-2 weeks for liver evaluation");
            insight.setConfidenceScore(0.85);
            insights.add(insight);
        }
        
        return insights;
    }

    /**
     * Kidney function analysis
     */
    private List<MedicalInsight> analyzeKidneyFunction(List<MedicalParameter> parameters, UserProfile userProfile) {
        List<MedicalInsight> insights = new ArrayList<>();
        
        for (MedicalParameter param : parameters) {
            String paramName = param.getParameter().toLowerCase();
            
            if ((paramName.contains("creatinine") || paramName.contains("bun")) && "HIGH".equals(param.getStatus())) {
                MedicalInsight insight = new MedicalInsight();
                insight.setType(MedicalInsight.InsightType.KIDNEY_FUNCTION);
                insight.setTitle("Kidney Function Analysis");
                insight.setRiskLevel(MedicalInsight.RiskLevel.MODERATE_CONCERN);
                insight.setAffectedParameters(Arrays.asList(param.getParameter()));
                insight.setDescription("Your kidney function markers are elevated, which may indicate reduced kidney function.");
                insight.setRecommendations(Arrays.asList(
                    "Stay well hydrated",
                    "Limit protein intake if recommended by your doctor",
                    "Monitor blood pressure closely",
                    "Avoid NSAIDs (ibuprofen, naproxen)",
                    "Control diabetes if present"
                ));
                insight.setSuggestedTests(Arrays.asList("Estimated GFR", "Urine analysis", "24-hour urine collection"));
                insight.setWhenToSeeDoctor("Schedule an appointment within 1 week for kidney function evaluation");
                insight.setConfidenceScore(0.87);
                insights.add(insight);
                break; // Only add one kidney insight
            }
        }
        
        return insights;
    }

    /**
     * Thyroid function analysis
     */
    private List<MedicalInsight> analyzeThyroidFunction(List<MedicalParameter> parameters, UserProfile userProfile) {
        List<MedicalInsight> insights = new ArrayList<>();
        
        for (MedicalParameter param : parameters) {
            String paramName = param.getParameter().toLowerCase();
            
            if (paramName.contains("tsh")) {
                MedicalInsight insight = new MedicalInsight();
                insight.setType(MedicalInsight.InsightType.HORMONAL);
                insight.setTitle("Thyroid Function Analysis");
                insight.setAffectedParameters(Arrays.asList("TSH"));
                
                if ("HIGH".equals(param.getStatus())) {
                    insight.setRiskLevel(MedicalInsight.RiskLevel.MODERATE_CONCERN);
                    insight.setDescription("Your TSH level (" + param.getValue() + " " + param.getUnit() + 
                        ") is elevated, which may indicate an underactive thyroid (hypothyroidism).");
                    insight.setRecommendations(Arrays.asList(
                        "Monitor for symptoms like fatigue, weight gain, cold intolerance",
                        "Ensure adequate iodine intake",
                        "Consider selenium-rich foods",
                        "Manage stress levels"
                    ));
                    insight.setSuggestedTests(Arrays.asList("Free T4", "Free T3", "Thyroid antibodies"));
                } else if ("LOW".equals(param.getStatus())) {
                    insight.setRiskLevel(MedicalInsight.RiskLevel.MODERATE_CONCERN);
                    insight.setDescription("Your TSH level (" + param.getValue() + " " + param.getUnit() + 
                        ") is low, which may indicate an overactive thyroid (hyperthyroidism).");
                    insight.setRecommendations(Arrays.asList(
                        "Monitor for symptoms like rapid heartbeat, weight loss, heat intolerance",
                        "Avoid excessive iodine intake",
                        "Limit caffeine consumption",
                        "Practice stress management"
                    ));
                    insight.setSuggestedTests(Arrays.asList("Free T4", "Free T3", "Thyroid antibodies", "Thyroid ultrasound"));
                }
                
                insight.setWhenToSeeDoctor("Schedule an appointment within 2-4 weeks for thyroid evaluation");
                insight.setConfidenceScore(0.83);
                insights.add(insight);
            }
        }
        
        return insights;
    }
    
    private String generateNoParametersInsight() {
        return "📋 Medical Report Analysis\n\n" +
               "No specific medical parameters were detected in this report. " +
               "This could be due to:\n" +
               "• Image quality issues\n" +
               "• Non-standard report format\n" +
               "• Text recognition limitations\n\n" +
               "💡 Recommendations:\n" +
               "• Ensure the image is clear and well-lit\n" +
               "• Try scanning individual sections of the report\n" +
               "• Consult with your healthcare provider for proper interpretation\n\n" +
               getStandardDisclaimer();
    }
    
    private void analyzeBloodWork(List<MedicalParameter> parameters, StringBuilder insights) {
        boolean hasBloodWork = false;
        
        for (MedicalParameter param : parameters) {
            String paramName = param.getParameter().toLowerCase();
            
            if (paramName.contains("hemoglobin") || paramName.contains("hgb") || paramName.contains("hb")) {
                hasBloodWork = true;
                insights.append("🩸 Blood Analysis:\n");
                
                if ("LOW".equals(param.getStatus())) {
                    insights.append("• Hemoglobin is below normal range (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). This may indicate anemia, which could be due to iron deficiency, ")
                           .append("chronic disease, or other factors. Consider discussing iron-rich foods ")
                           .append("and further testing with your healthcare provider.\n\n");
                } else if ("HIGH".equals(param.getStatus())) {
                    insights.append("• Hemoglobin is above normal range (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). This could indicate dehydration, lung disease, or other conditions. ")
                           .append("Follow up with your healthcare provider for evaluation.\n\n");
                } else {
                    insights.append("• Hemoglobin levels are within normal range (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). This indicates good oxygen-carrying capacity of your blood.\n\n");
                }
            }
            
            if (paramName.contains("wbc") || paramName.contains("white blood cell")) {
                if (!hasBloodWork) {
                    hasBloodWork = true;
                    insights.append("🩸 Blood Analysis:\n");
                }
                
                if ("HIGH".equals(param.getStatus())) {
                    insights.append("• White blood cell count is elevated (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). This may indicate an infection, inflammation, or immune response. ")
                           .append("Consult your healthcare provider for proper evaluation.\n\n");
                } else if ("LOW".equals(param.getStatus())) {
                    insights.append("• White blood cell count is below normal (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). This could indicate a weakened immune system or certain medications' effects. ")
                           .append("Discuss with your healthcare provider.\n\n");
                }
            }
        }
    }
    
    private void analyzeVitalSigns(List<MedicalParameter> parameters, StringBuilder insights) {
        for (MedicalParameter param : parameters) {
            String paramName = param.getParameter().toLowerCase();
            
            if (paramName.contains("blood pressure") || paramName.contains("bp")) {
                insights.append("💓 Cardiovascular Health:\n");
                
                if ("HIGH".equals(param.getStatus())) {
                    insights.append("• Blood pressure is elevated (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). High blood pressure increases risk of heart disease and stroke. ")
                           .append("Consider lifestyle modifications like reducing sodium intake, ")
                           .append("regular exercise, and stress management. Consult your healthcare provider ")
                           .append("about treatment options.\n\n");
                } else if ("LOW".equals(param.getStatus())) {
                    insights.append("• Blood pressure is below normal range (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). While often not concerning, very low blood pressure can cause ")
                           .append("dizziness or fainting. Discuss with your healthcare provider if you ")
                           .append("experience symptoms.\n\n");
                } else {
                    insights.append("• Blood pressure is within healthy range (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). Continue maintaining a healthy lifestyle to keep it optimal.\n\n");
                }
                break;
            }
        }
    }
    
    private void analyzeMetabolicMarkers(List<MedicalParameter> parameters, StringBuilder insights) {
        boolean hasMetabolic = false;
        
        for (MedicalParameter param : parameters) {
            String paramName = param.getParameter().toLowerCase();
            
            if (paramName.contains("glucose") || paramName.contains("sugar")) {
                if (!hasMetabolic) {
                    hasMetabolic = true;
                    insights.append("🍯 Metabolic Health:\n");
                }
                
                if ("HIGH".equals(param.getStatus())) {
                    insights.append("• Blood glucose is elevated (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). This may indicate diabetes or prediabetes. Consider dietary ")
                           .append("modifications, regular exercise, and follow up with your healthcare ")
                           .append("provider for proper diabetes management.\n\n");
                } else if ("LOW".equals(param.getStatus())) {
                    insights.append("• Blood glucose is below normal (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). Low blood sugar can cause symptoms like dizziness, sweating, ")
                           .append("or confusion. If you experience these symptoms, consult your healthcare provider.\n\n");
                } else {
                    insights.append("• Blood glucose is within normal range (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). Continue maintaining a balanced diet and active lifestyle.\n\n");
                }
            }
            
            if (paramName.contains("cholesterol")) {
                if (!hasMetabolic) {
                    hasMetabolic = true;
                    insights.append("🍯 Metabolic Health:\n");
                }
                
                if ("HIGH".equals(param.getStatus())) {
                    insights.append("• Cholesterol levels are elevated (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). High cholesterol increases cardiovascular risk. Consider a ")
                           .append("heart-healthy diet low in saturated fats, regular exercise, and ")
                           .append("discuss treatment options with your healthcare provider.\n\n");
                } else {
                    insights.append("• Cholesterol levels are within acceptable range (")
                           .append(param.getValue()).append(" ").append(param.getUnit())
                           .append("). Continue heart-healthy lifestyle habits.\n\n");
                }
            }
        }
    }
    
    private void addGeneralRecommendations(List<MedicalParameter> parameters, StringBuilder insights) {
        insights.append("💡 General Health Recommendations:\n");
        
        // Count abnormal parameters
        long abnormalCount = parameters.stream()
            .filter(p -> "HIGH".equals(p.getStatus()) || "LOW".equals(p.getStatus()))
            .count();
        
        if (abnormalCount == 0) {
            insights.append("• Your test results show values within normal ranges. ")
                   .append("Continue maintaining your current healthy lifestyle.\n");
            insights.append("• Regular health check-ups help maintain optimal health.\n");
            insights.append("• Keep up with a balanced diet, regular exercise, and adequate sleep.\n\n");
        } else {
            insights.append("• Schedule a follow-up appointment with your healthcare provider ")
                   .append("to discuss the abnormal values and create an appropriate treatment plan.\n");
            insights.append("• Maintain a healthy lifestyle with balanced nutrition and regular physical activity.\n");
            insights.append("• Monitor your symptoms and keep track of any changes.\n");
            insights.append("• Take medications as prescribed and follow your healthcare provider's recommendations.\n\n");
        }
    }
    
    private void addMedicalDisclaimer(StringBuilder insights) {
        insights.append(getStandardDisclaimer());
    }
    
    /**
     * Format insights as readable text
     */
    private String formatInsightsAsText(List<MedicalInsight> insights) {
        if (insights.isEmpty()) {
            return generateNoParametersInsight();
        }
        
        StringBuilder text = new StringBuilder();
        text.append("🔬 **Comprehensive Medical Analysis**\n\n");
        
        // Group insights by risk level
        List<MedicalInsight> criticalInsights = insights.stream()
            .filter(i -> i.getRiskLevel() == MedicalInsight.RiskLevel.CRITICAL)
            .collect(Collectors.toList());
        
        List<MedicalInsight> highConcernInsights = insights.stream()
            .filter(i -> i.getRiskLevel() == MedicalInsight.RiskLevel.HIGH_CONCERN)
            .collect(Collectors.toList());
        
        List<MedicalInsight> moderateConcernInsights = insights.stream()
            .filter(i -> i.getRiskLevel() == MedicalInsight.RiskLevel.MODERATE_CONCERN)
            .collect(Collectors.toList());
        
        List<MedicalInsight> normalInsights = insights.stream()
            .filter(i -> i.getRiskLevel() == MedicalInsight.RiskLevel.NORMAL)
            .collect(Collectors.toList());
        
        // Critical findings first
        if (!criticalInsights.isEmpty()) {
            text.append("🚨 **CRITICAL FINDINGS - IMMEDIATE ATTENTION REQUIRED**\n");
            for (MedicalInsight insight : criticalInsights) {
                text.append("• ").append(insight.getTitle()).append(": ").append(insight.getDescription()).append("\n");
                text.append("  **Action:** ").append(insight.getWhenToSeeDoctor()).append("\n\n");
            }
        }
        
        // High concern findings
        if (!highConcernInsights.isEmpty()) {
            text.append("⚠️ **HIGH PRIORITY FINDINGS**\n");
            for (MedicalInsight insight : highConcernInsights) {
                text.append("• ").append(insight.getTitle()).append(": ").append(insight.getDescription()).append("\n");
                if (insight.getRecommendations() != null && !insight.getRecommendations().isEmpty()) {
                    text.append("  **Recommendations:** ").append(String.join(", ", insight.getRecommendations())).append("\n");
                }
                text.append("  **Follow-up:** ").append(insight.getWhenToSeeDoctor()).append("\n\n");
            }
        }
        
        // Moderate concern findings
        if (!moderateConcernInsights.isEmpty()) {
            text.append("💡 **AREAS FOR ATTENTION**\n");
            for (MedicalInsight insight : moderateConcernInsights) {
                text.append("• ").append(insight.getTitle()).append(": ").append(insight.getDescription()).append("\n");
                if (insight.getRecommendations() != null && !insight.getRecommendations().isEmpty()) {
                    text.append("  **Suggestions:** ").append(String.join(", ", insight.getRecommendations())).append("\n");
                }
                text.append("\n");
            }
        }
        
        // Normal findings
        if (!normalInsights.isEmpty()) {
            text.append("✅ **NORMAL FINDINGS**\n");
            for (MedicalInsight insight : normalInsights) {
                text.append("• ").append(insight.getTitle()).append(": ").append(insight.getDescription()).append("\n");
            }
            text.append("\n");
        }
        
        return text.toString();
    }

    /**
     * Calculate overall severity from insights
     */
    private ChatMessage.Severity calculateOverallSeverity(List<MedicalInsight> insights) {
        boolean hasCritical = insights.stream().anyMatch(i -> i.getRiskLevel() == MedicalInsight.RiskLevel.CRITICAL);
        boolean hasHigh = insights.stream().anyMatch(i -> i.getRiskLevel() == MedicalInsight.RiskLevel.HIGH_CONCERN);
        boolean hasModerate = insights.stream().anyMatch(i -> i.getRiskLevel() == MedicalInsight.RiskLevel.MODERATE_CONCERN);
        
        if (hasCritical) return ChatMessage.Severity.CRITICAL;
        if (hasHigh) return ChatMessage.Severity.HIGH;
        if (hasModerate) return ChatMessage.Severity.MEDIUM;
        return ChatMessage.Severity.LOW;
    }

    /**
     * Determine if follow-up is required
     */
    private boolean shouldRequireFollowUp(List<MedicalInsight> insights) {
        return insights.stream().anyMatch(i -> 
            i.getRiskLevel() == MedicalInsight.RiskLevel.CRITICAL ||
            i.getRiskLevel() == MedicalInsight.RiskLevel.HIGH_CONCERN ||
            i.isEmergencyFlag()
        );
    }

    /**
     * Generate personalized recommendations
     */
    private List<String> generateRecommendations(List<MedicalInsight> insights, UserProfile userProfile) {
        List<String> recommendations = new ArrayList<>();
        
        // Collect all recommendations from insights
        for (MedicalInsight insight : insights) {
            if (insight.getRecommendations() != null) {
                recommendations.addAll(insight.getRecommendations());
            }
        }
        
        // Add general recommendations based on user profile
        if (userProfile != null) {
            try {
                int age = Integer.parseInt(userProfile.getAge());
                if (age > 50) {
                    recommendations.add("Consider regular cardiovascular screening given your age");
                    recommendations.add("Ensure adequate calcium and vitamin D intake");
                }
                if (age > 65) {
                    recommendations.add("Discuss fall prevention strategies with your healthcare provider");
                }
            } catch (NumberFormatException e) {
                // Age not available or invalid
            }
            
            if ("female".equalsIgnoreCase(userProfile.getGender())) {
                recommendations.add("Ensure adequate iron intake, especially if pre-menopausal");
            }
        }
        
        // Remove duplicates and limit to top recommendations
        return recommendations.stream()
            .distinct()
            .limit(8)
            .collect(Collectors.toList());
    }

    /**
     * Generate response for symptom-based queries
     */
    public Single<ChatMessage> analyzeSymptoms(String symptoms, UserProfile userProfile) {
        return Single.fromCallable(() -> {
            ChatMessage response = new ChatMessage();
            response.setType(ChatMessage.MessageType.AI_RESPONSE);
            response.setFromUser(false);
            response.setSeverity(ChatMessage.Severity.LOW);
            
            String analysisText = analyzeSymptomText(symptoms, userProfile);
            response.setContent(analysisText);
            response.setMedicalDisclaimer(getStandardDisclaimer());
            
            // Determine if symptoms suggest urgent care
            if (containsUrgentSymptoms(symptoms)) {
                response.setSeverity(ChatMessage.Severity.HIGH);
                response.setRequiresFollowUp(true);
            }
            
            return response;
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Analyze symptom text and provide guidance
     */
    private String analyzeSymptomText(String symptoms, UserProfile userProfile) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("🩺 **Symptom Analysis**\n\n");
        
        String lowerSymptoms = symptoms.toLowerCase();
        
        // Check for emergency symptoms
        if (containsUrgentSymptoms(symptoms)) {
            analysis.append("🚨 **URGENT SYMPTOMS DETECTED**\n");
            analysis.append("Based on your symptoms, you should seek immediate medical attention. ");
            analysis.append("Consider visiting an emergency room or calling emergency services.\n\n");
        }
        
        // Analyze common symptom patterns
        if (lowerSymptoms.contains("chest pain") || lowerSymptoms.contains("chest pressure")) {
            analysis.append("⚠️ **Chest Pain Analysis:**\n");
            analysis.append("Chest pain can have various causes ranging from muscle strain to serious cardiac conditions. ");
            analysis.append("Seek immediate medical attention if accompanied by shortness of breath, sweating, or nausea.\n\n");
        }
        
        if (lowerSymptoms.contains("shortness of breath") || lowerSymptoms.contains("difficulty breathing")) {
            analysis.append("⚠️ **Breathing Difficulty:**\n");
            analysis.append("Shortness of breath can indicate respiratory or cardiac issues. ");
            analysis.append("Monitor your symptoms closely and seek medical attention if they worsen.\n\n");
        }
        
        if (lowerSymptoms.contains("fever") || lowerSymptoms.contains("temperature")) {
            analysis.append("🌡️ **Fever Analysis:**\n");
            analysis.append("Fever is often a sign of infection. Stay hydrated, rest, and monitor your temperature. ");
            analysis.append("Seek medical attention if fever exceeds 103°F (39.4°C) or persists for more than 3 days.\n\n");
        }
        
        if (lowerSymptoms.contains("headache")) {
            analysis.append("🧠 **Headache Assessment:**\n");
            analysis.append("Headaches can have many causes. Stay hydrated, rest in a quiet environment, ");
            analysis.append("and consider over-the-counter pain relief. Seek medical attention for severe, ");
            analysis.append("sudden-onset, or persistent headaches.\n\n");
        }
        
        // General recommendations
        analysis.append("💡 **General Recommendations:**\n");
        analysis.append("• Monitor your symptoms and note any changes\n");
        analysis.append("• Stay hydrated and get adequate rest\n");
        analysis.append("• Keep a symptom diary with dates and severity\n");
        analysis.append("• Contact your healthcare provider if symptoms persist or worsen\n\n");
        
        return analysis.toString();
    }

    /**
     * Check if symptoms suggest urgent medical attention
     */
    private boolean containsUrgentSymptoms(String symptoms) {
        String lowerSymptoms = symptoms.toLowerCase();
        
        String[] urgentKeywords = {
            "chest pain", "difficulty breathing", "severe headache", "confusion",
            "loss of consciousness", "severe abdominal pain", "blood in stool",
            "blood in urine", "severe dizziness", "fainting", "seizure",
            "severe allergic reaction", "difficulty swallowing", "severe bleeding"
        };
        
        for (String keyword : urgentKeywords) {
            if (lowerSymptoms.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }

    private String getStandardDisclaimer() {
        return "⚠️ **IMPORTANT MEDICAL DISCLAIMER:**\n" +
               "This analysis is for informational purposes only and should not replace professional " +
               "medical advice, diagnosis, or treatment. Always consult with qualified healthcare " +
               "professionals regarding your medical condition and treatment options. In case of " +
               "medical emergencies, seek immediate professional medical attention.\n\n" +
               "The AI-generated insights are based on general medical knowledge and may not account " +
               "for your individual medical history, current medications, or other relevant factors " +
               "that only your healthcare provider can properly evaluate.";
    }
}