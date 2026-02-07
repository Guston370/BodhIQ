package com.mit.bodhiq.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TriageResponse {
    @SerializedName("extracted")
    private Extracted extracted;

    @SerializedName("triage")
    private Triage triage;

    @SerializedName("suggestions")
    private Suggestions suggestions;

    @SerializedName("confidence_score")
    private double confidenceScore;

    @SerializedName("disclaimer")
    private String disclaimer;

    public Extracted getExtracted() {
        return extracted;
    }

    public Triage getTriage() {
        return triage;
    }

    public Suggestions getSuggestions() {
        return suggestions;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public static class Extracted {
        @SerializedName("presenting_symptoms")
        private List<String> presentingSymptoms;

        @SerializedName("onset")
        private String onset;

        @SerializedName("duration")
        private String duration;

        @SerializedName("severity")
        private String severity;

        @SerializedName("associated_symptoms")
        private List<String> associatedSymptoms;

        @SerializedName("vitals_if_provided")
        private String vitalsIfProvided;

        @SerializedName("meds")
        private String meds;

        @SerializedName("allergies")
        private String allergies;

        @SerializedName("history")
        private String history;

        public List<String> getPresentingSymptoms() {
            return presentingSymptoms;
        }

        public String getOnset() {
            return onset;
        }

        public String getDuration() {
            return duration;
        }

        public String getSeverity() {
            return severity;
        }

        public List<String> getAssociatedSymptoms() {
            return associatedSymptoms;
        }

        public String getVitalsIfProvided() {
            return vitalsIfProvided;
        }

        public String getMeds() {
            return meds;
        }

        public String getAllergies() {
            return allergies;
        }

        public String getHistory() {
            return history;
        }
    }

    public static class Triage {
        @SerializedName("urgency")
        private String urgency; // emergency, urgent, routine, selfcare

        @SerializedName("reasons")
        private List<String> reasons;

        public String getUrgency() {
            return urgency;
        }

        public List<String> getReasons() {
            return reasons;
        }
    }

    public static class Suggestions {
        @SerializedName("immediate_actions")
        private List<String> immediateActions;

        @SerializedName("red_flag_wording")
        private String redFlagWording;

        @SerializedName("recommended_specialist")
        private String recommendedSpecialist;

        @SerializedName("tests_to_consider")
        private List<String> testsToConsider;

        public List<String> getImmediateActions() {
            return immediateActions;
        }

        public String getRedFlagWording() {
            return redFlagWording;
        }

        public String getRecommendedSpecialist() {
            return recommendedSpecialist;
        }

        public List<String> getTestsToConsider() {
            return testsToConsider;
        }
    }
}
