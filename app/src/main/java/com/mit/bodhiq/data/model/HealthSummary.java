package com.mit.bodhiq.data.model;

/**
 * Aggregated health summary combining data from Profile and Reports
 * Privacy: PHI contained but not logged
 */
public class HealthSummary {
    private String name;
    private String age;
    private String dateOfBirth;
    private String bloodType;
    private String allergies;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String height;
    private String weight;
    private String gender;
    private long lastUpdated;

    // Presence flags
    private boolean hasAnyData;
    private boolean hasEmergencyContact;
    private boolean hasVitalInfo;

    public HealthSummary() {
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getBloodType() {
        return bloodType;
    }

    public String getAllergies() {
        return allergies;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public String getHeight() {
        return height;
    }

    public String getWeight() {
        return weight;
    }

    public String getGender() {
        return gender;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public boolean hasAnyData() {
        return hasAnyData;
    }

    public boolean hasEmergencyContact() {
        return hasEmergencyContact;
    }

    public boolean hasVitalInfo() {
        return hasVitalInfo;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setHasAnyData(boolean hasAnyData) {
        this.hasAnyData = hasAnyData;
    }

    public void setHasEmergencyContact(boolean hasEmergencyContact) {
        this.hasEmergencyContact = hasEmergencyContact;
    }

    public void setHasVitalInfo(boolean hasVitalInfo) {
        this.hasVitalInfo = hasVitalInfo;
    }

    /**
     * Get a concise summary line (for card previews)
     * MAX 5 most important fields
     */
    public String getConciseSummary() {
        StringBuilder sb = new StringBuilder();
        int count = 0;

        if (hasValue(bloodType) && count < 5) {
            sb.append("Blood: ").append(bloodType).append(" • ");
            count++;
        }
        if (hasValue(age) && count < 5) {
            sb.append("Age: ").append(age).append(" • ");
            count++;
        }
        if (hasValue(allergies) && count < 5) {
            sb.append("Allergies: ").append(allergies).append(" • ");
            count++;
        }
        if (hasEmergencyContact && count < 5) {
            sb.append("Emergency: ").append(emergencyContactPhone).append(" • ");
            count++;
        }
        if (hasValue(height) && hasValue(weight) && count < 5) {
            sb.append(height).append("cm / ").append(weight).append("kg");
            count++;
        }

        String result = sb.toString();
        if (result.endsWith(" • ")) {
            result = result.substring(0, result.length() - 3);
        }
        return result.isEmpty() ? "No health data available" : result;
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
