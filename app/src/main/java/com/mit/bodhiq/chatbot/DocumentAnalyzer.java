package com.mit.bodhiq.chatbot;

import com.mit.bodhiq.data.model.ChatMessage;
import com.mit.bodhiq.data.model.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzes medical documents and extracts key information
 */
public class DocumentAnalyzer {
    
    /**
     * Analyze medical report text
     */
    public ReportAnalysis analyzeReport(String reportText) {
        ReportAnalysis analysis = new ReportAnalysis();
        
        // Extract values
        analysis.setDetectedValues(extractMedicalValues(reportText));
        
        // Extract medications
        analysis.setMedications(extractMedications(reportText));
        
        // Extract dates
        analysis.setDates(extractDates(reportText));
        
        // Generate summary
        analysis.setSummary(generateSummary(analysis));
        
        // Generate key findings
        analysis.setKeyFindings(generateKeyFindings(analysis));
        
        // Generate recommendations
        analysis.setRecommendations(generateRecommendations(analysis));
        
        // Determine severity
        analysis.setSeverity(determineSeverity(analysis));
        
        // Check if follow-up needed
        analysis.setRequiresFollowUp(needsFollowUp(analysis));
        
        return analysis;
    }
    
    /**
     * Extract medical values from text
     */
    private List<DetectedValue> extractMedicalValues(String text) {
        List<DetectedValue> values = new ArrayList<>();
        
        // Hemoglobin
        Pattern hbPattern = Pattern.compile("(?i)(hemoglobin|hgb|hb)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*(g/dl|gm/dl)?");
        Matcher hbMatcher = hbPattern.matcher(text);
        while (hbMatcher.find()) {
            double value = Double.parseDouble(hbMatcher.group(2));
            String status = (value < 12.0) ? "Low" : (value > 16.0) ? "High" : "Normal";
            values.add(new DetectedValue("Hemoglobin", hbMatcher.group(2), "g/dL", status));
        }
        
        // Blood Glucose
        Pattern glucosePattern = Pattern.compile("(?i)(glucose|blood sugar|fbs|rbs)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*(mg/dl)?");
        Matcher glucoseMatcher = glucosePattern.matcher(text);
        while (glucoseMatcher.find()) {
            double value = Double.parseDouble(glucoseMatcher.group(2));
            String status = (value < 70) ? "Low" : (value > 140) ? "High" : "Normal";
            values.add(new DetectedValue("Blood Glucose", glucoseMatcher.group(2), "mg/dL", status));
        }
        
        // Cholesterol
        Pattern cholPattern = Pattern.compile("(?i)(cholesterol|total cholesterol)\\s*:?\\s*([0-9]+\\.?[0-9]*)\\s*(mg/dl)?");
        Matcher cholMatcher = cholPattern.matcher(text);
        while (cholMatcher.find()) {
            double value = Double.parseDouble(cholMatcher.group(2));
            String status = (value > 200) ? "High" : "Normal";
            values.add(new DetectedValue("Cholesterol", cholMatcher.group(2), "mg/dL", status));
        }
        
        // Blood Pressure
        Pattern bpPattern = Pattern.compile("(?i)(blood pressure|bp)\\s*:?\\s*([0-9]+)/([0-9]+)\\s*(mmhg)?");
        Matcher bpMatcher = bpPattern.matcher(text);
        while (bpMatcher.find()) {
            int systolic = Integer.parseInt(bpMatcher.group(2));
            int diastolic = Integer.parseInt(bpMatcher.group(3));
            String status = (systolic > 140 || diastolic > 90) ? "High" : 
                           (systolic < 90 || diastolic < 60) ? "Low" : "Normal";
            values.add(new DetectedValue("Blood Pressure", bpMatcher.group(2) + "/" + bpMatcher.group(3), "mmHg", status));
        }
        
        return values;
    }
    
    /**
     * Extract medication names from text
     */
    private List<String> extractMedications(String text) {
        List<String> medications = new ArrayList<>();
        
        // Common medication patterns
        String[] commonMeds = {
            "aspirin", "ibuprofen", "paracetamol", "acetaminophen", "metformin",
            "lisinopril", "amlodipine", "atorvastatin", "simvastatin", "omeprazole",
            "levothyroxine", "albuterol", "metoprolol", "losartan", "gabapentin"
        };
        
        String lowerText = text.toLowerCase();
        for (String med : commonMeds) {
            if (lowerText.contains(med)) {
                medications.add(capitalize(med));
            }
        }
        
        // Generic pattern for medications (word ending in common suffixes)
        Pattern medPattern = Pattern.compile("\\b([A-Z][a-z]+(pril|olol|statin|cillin|mycin|azole|pine))\\b");
        Matcher medMatcher = medPattern.matcher(text);
        while (medMatcher.find()) {
            String med = medMatcher.group(1);
            if (!medications.contains(med)) {
                medications.add(med);
            }
        }
        
        return medications;
    }
    
    /**
     * Extract dates from text
     */
    private List<String> extractDates(String text) {
        List<String> dates = new ArrayList<>();
        
        // Date patterns: DD/MM/YYYY, DD-MM-YYYY, Month DD, YYYY
        Pattern datePattern = Pattern.compile("\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]* \\d{1,2},? \\d{4})\\b");
        Matcher dateMatcher = datePattern.matcher(text);
        while (dateMatcher.find()) {
            dates.add(dateMatcher.group());
        }
        
        return dates;
    }
    
    private String generateSummary(ReportAnalysis analysis) {
        int normalCount = 0;
        int abnormalCount = 0;
        
        for (DetectedValue value : analysis.getDetectedValues()) {
            if ("Normal".equals(value.getStatus())) {
                normalCount++;
            } else {
                abnormalCount++;
            }
        }
        
        if (analysis.getDetectedValues().isEmpty()) {
            return "Report analyzed. No specific medical values detected in the text.";
        }
        
        return String.format("Found %d medical parameter(s): %d normal, %d requiring attention.",
            analysis.getDetectedValues().size(), normalCount, abnormalCount);
    }
    
    private List<String> generateKeyFindings(ReportAnalysis analysis) {
        List<String> findings = new ArrayList<>();
        
        for (DetectedValue value : analysis.getDetectedValues()) {
            if (!"Normal".equals(value.getStatus())) {
                findings.add(value.getParameter() + " is " + value.getStatus() + 
                    " (" + value.getValue() + " " + value.getUnit() + ")");
            }
        }
        
        if (findings.isEmpty()) {
            findings.add("All detected parameters are within normal range");
        }
        
        return findings;
    }
    
    private List<String> generateRecommendations(ReportAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();
        
        boolean hasAbnormal = false;
        for (DetectedValue value : analysis.getDetectedValues()) {
            if (!"Normal".equals(value.getStatus())) {
                hasAbnormal = true;
                break;
            }
        }
        
        if (hasAbnormal) {
            recommendations.add("Consult your healthcare provider about abnormal values");
            recommendations.add("Keep a record of these results for your next appointment");
            recommendations.add("Follow any prescribed treatment plans");
        } else {
            recommendations.add("Continue maintaining healthy lifestyle habits");
            recommendations.add("Schedule regular check-ups as recommended");
        }
        
        if (!analysis.getMedications().isEmpty()) {
            recommendations.add("Take medications as prescribed");
            recommendations.add("Report any side effects to your doctor");
        }
        
        return recommendations;
    }
    
    private ChatMessage.Severity determineSeverity(ReportAnalysis analysis) {
        int highCount = 0;
        
        for (DetectedValue value : analysis.getDetectedValues()) {
            if ("High".equals(value.getStatus()) || "Low".equals(value.getStatus())) {
                highCount++;
            }
        }
        
        if (highCount == 0) {
            return ChatMessage.Severity.LOW;
        } else if (highCount <= 2) {
            return ChatMessage.Severity.MEDIUM;
        } else {
            return ChatMessage.Severity.HIGH;
        }
    }
    
    private boolean needsFollowUp(ReportAnalysis analysis) {
        for (DetectedValue value : analysis.getDetectedValues()) {
            if (!"Normal".equals(value.getStatus())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Extract reminder details from user message
     */
    public ReminderDetails extractReminderDetails(String message) {
        ReminderDetails details = new ReminderDetails();
        
        // Extract medicine name (capitalize first word after "take" or "remind")
        Pattern medPattern = Pattern.compile("(?i)(?:take|remind.*take)\\s+([A-Za-z]+)");
        Matcher medMatcher = medPattern.matcher(message);
        if (medMatcher.find()) {
            details.setMedicineName(capitalize(medMatcher.group(1)));
        }
        
        // Extract times
        Pattern timePattern = Pattern.compile("(?i)(\\d{1,2})\\s*(?::|\\s)?(\\d{2})?\\s*(am|pm|AM|PM)");
        Matcher timeMatcher = timePattern.matcher(message);
        List<String> times = new ArrayList<>();
        while (timeMatcher.find()) {
            String hour = timeMatcher.group(1);
            String minute = timeMatcher.group(2) != null ? timeMatcher.group(2) : "00";
            String period = timeMatcher.group(3).toUpperCase();
            times.add(hour + ":" + minute + " " + period);
        }
        details.setTimes(times);
        
        // Extract dose
        Pattern dosePattern = Pattern.compile("(?i)(\\d+)\\s*(mg|ml|tablet|pill|capsule)");
        Matcher doseMatcher = dosePattern.matcher(message);
        if (doseMatcher.find()) {
            details.setDose(doseMatcher.group(1) + " " + doseMatcher.group(2));
        }
        
        return details.getMedicineName() != null ? details : null;
    }
    
    /**
     * Analyze symptoms from user message
     */
    public SymptomAnalysis analyzeSymptoms(String message, UserProfile profile) {
        SymptomAnalysis analysis = new SymptomAnalysis();
        
        // Extract symptoms
        List<String> symptoms = extractSymptoms(message);
        analysis.setSymptoms(symptoms);
        
        // Generate assessment
        analysis.setAssessment(generateSymptomAssessment(symptoms, profile));
        
        // Generate recommendations
        analysis.setRecommendations(generateSymptomRecommendations(symptoms));
        
        // Determine severity
        analysis.setSeverity(determineSymptomSeverity(symptoms));
        
        // Check if follow-up needed
        analysis.setRequiresFollowUp(analysis.getSeverity() != ChatMessage.Severity.LOW);
        
        return analysis;
    }
    
    private List<String> extractSymptoms(String message) {
        List<String> symptoms = new ArrayList<>();
        String lower = message.toLowerCase();
        
        String[] symptomKeywords = {
            "headache", "fever", "cough", "sore throat", "runny nose", "congestion",
            "nausea", "vomiting", "diarrhea", "stomach pain", "abdominal pain",
            "chest pain", "shortness of breath", "dizziness", "fatigue", "weakness",
            "rash", "itching", "swelling", "joint pain", "muscle pain", "back pain"
        };
        
        for (String symptom : symptomKeywords) {
            if (lower.contains(symptom)) {
                symptoms.add(capitalize(symptom));
            }
        }
        
        return symptoms;
    }
    
    private String generateSymptomAssessment(List<String> symptoms, UserProfile profile) {
        if (symptoms.isEmpty()) {
            return "Please describe your symptoms in more detail.";
        }
        
        StringBuilder assessment = new StringBuilder();
        assessment.append("Based on your symptoms");
        
        if (profile != null && profile.getAge() != null) {
            assessment.append(" and age (").append(profile.getAge()).append(" years)");
        }
        
        assessment.append(", you may be experiencing a common condition. ");
        assessment.append("However, proper diagnosis requires medical evaluation.");
        
        return assessment.toString();
    }
    
    private List<String> generateSymptomRecommendations(List<String> symptoms) {
        List<String> recommendations = new ArrayList<>();
        
        recommendations.add("Rest and stay hydrated");
        recommendations.add("Monitor your symptoms");
        recommendations.add("Take over-the-counter medications if appropriate");
        recommendations.add("Seek medical attention if symptoms worsen");
        recommendations.add("Contact your doctor if symptoms persist beyond 3-5 days");
        
        return recommendations;
    }
    
    private ChatMessage.Severity determineSymptomSeverity(List<String> symptoms) {
        String symptomsStr = symptoms.toString().toLowerCase();
        
        // Critical symptoms
        if (symptomsStr.contains("chest pain") || symptomsStr.contains("shortness of breath")) {
            return ChatMessage.Severity.CRITICAL;
        }
        
        // High severity
        if (symptomsStr.contains("severe") || symptoms.size() > 4) {
            return ChatMessage.Severity.HIGH;
        }
        
        // Medium severity
        if (symptoms.size() > 2) {
            return ChatMessage.Severity.MEDIUM;
        }
        
        return ChatMessage.Severity.LOW;
    }
    
    /**
     * Extract medication name from message
     */
    public String extractMedicationName(String message) {
        // Look for medication name after "about" or "for"
        Pattern pattern = Pattern.compile("(?i)(?:about|for|regarding)\\s+([A-Za-z]+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return capitalize(matcher.group(1));
        }
        
        // Look for capitalized words (likely medication names)
        Pattern capPattern = Pattern.compile("\\b([A-Z][a-z]+)\\b");
        Matcher capMatcher = capPattern.matcher(message);
        if (capMatcher.find()) {
            return capMatcher.group(1);
        }
        
        return null;
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    // Data classes
    
    public static class ReportAnalysis {
        private String summary;
        private List<String> keyFindings = new ArrayList<>();
        private List<DetectedValue> detectedValues = new ArrayList<>();
        private List<String> medications = new ArrayList<>();
        private List<String> dates = new ArrayList<>();
        private List<String> recommendations = new ArrayList<>();
        private ChatMessage.Severity severity;
        private boolean requiresFollowUp;
        
        // Getters and setters
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        
        public List<String> getKeyFindings() { return keyFindings; }
        public void setKeyFindings(List<String> keyFindings) { this.keyFindings = keyFindings; }
        
        public List<DetectedValue> getDetectedValues() { return detectedValues; }
        public void setDetectedValues(List<DetectedValue> detectedValues) { this.detectedValues = detectedValues; }
        
        public List<String> getMedications() { return medications; }
        public void setMedications(List<String> medications) { this.medications = medications; }
        
        public List<String> getDates() { return dates; }
        public void setDates(List<String> dates) { this.dates = dates; }
        
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
        
        public ChatMessage.Severity getSeverity() { return severity; }
        public void setSeverity(ChatMessage.Severity severity) { this.severity = severity; }
        
        public boolean requiresFollowUp() { return requiresFollowUp; }
        public void setRequiresFollowUp(boolean requiresFollowUp) { this.requiresFollowUp = requiresFollowUp; }
    }
    
    public static class DetectedValue {
        private String parameter;
        private String value;
        private String unit;
        private String status;
        
        public DetectedValue(String parameter, String value, String unit, String status) {
            this.parameter = parameter;
            this.value = value;
            this.unit = unit;
            this.status = status;
        }
        
        public String getParameter() { return parameter; }
        public String getValue() { return value; }
        public String getUnit() { return unit; }
        public String getStatus() { return status; }
    }
    
    public static class ReminderDetails {
        private String medicineName;
        private String dose;
        private List<String> times = new ArrayList<>();
        
        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
        
        public String getDose() { return dose; }
        public void setDose(String dose) { this.dose = dose; }
        
        public List<String> getTimes() { return times; }
        public void setTimes(List<String> times) { this.times = times; }
    }
    
    public static class SymptomAnalysis {
        private List<String> symptoms = new ArrayList<>();
        private String assessment;
        private List<String> recommendations = new ArrayList<>();
        private ChatMessage.Severity severity;
        private boolean requiresFollowUp;
        
        public List<String> getSymptoms() { return symptoms; }
        public void setSymptoms(List<String> symptoms) { this.symptoms = symptoms; }
        
        public String getAssessment() { return assessment; }
        public void setAssessment(String assessment) { this.assessment = assessment; }
        
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
        
        public ChatMessage.Severity getSeverity() { return severity; }
        public void setSeverity(ChatMessage.Severity severity) { this.severity = severity; }
        
        public boolean requiresFollowUp() { return requiresFollowUp; }
        public void setRequiresFollowUp(boolean requiresFollowUp) { this.requiresFollowUp = requiresFollowUp; }
    }
}
