package com.mit.bodhiq.utils;

/**
 * Utility class for BMI calculations
 */
public class BMICalculator {
    
    /**
     * Calculate BMI from height and weight
     * @param heightCm Height in centimeters
     * @param weightKg Weight in kilograms
     * @return BMI value
     */
    public static double calculateBMI(double heightCm, double weightKg) {
        if (heightCm <= 0 || weightKg <= 0) {
            return 0;
        }
        
        double heightM = heightCm / 100.0; // Convert cm to meters
        return weightKg / (heightM * heightM);
    }
    
    /**
     * Get BMI category based on BMI value
     * @param bmi BMI value
     * @return BMI category string
     */
    public static String getBMICategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi < 25.0) {
            return "Normal Weight";
        } else if (bmi < 30.0) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }
    
    /**
     * Get BMI health advice based on category
     * @param bmi BMI value
     * @return Health advice string
     */
    public static String getBMIAdvice(double bmi) {
        String category = getBMICategory(bmi);
        
        switch (category) {
            case "Underweight":
                return "Consider consulting a healthcare provider about healthy weight gain strategies.";
            case "Normal Weight":
                return "Great! Maintain your current healthy lifestyle.";
            case "Overweight":
                return "Consider a balanced diet and regular exercise to reach a healthier weight.";
            case "Obese":
                return "Consult with a healthcare provider about a weight management plan.";
            default:
                return "Please ensure accurate height and weight measurements.";
        }
    }
    
    /**
     * Calculate ideal weight range for given height
     * @param heightCm Height in centimeters
     * @return Array with [minWeight, maxWeight] in kg
     */
    public static double[] getIdealWeightRange(double heightCm) {
        double heightM = heightCm / 100.0;
        double minWeight = 18.5 * heightM * heightM;
        double maxWeight = 24.9 * heightM * heightM;
        return new double[]{minWeight, maxWeight};
    }
}