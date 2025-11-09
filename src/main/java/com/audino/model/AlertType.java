package com.audino.model;

/**
 * Enumeration representing different types of interaction alerts.
 */
public enum AlertType {
    DRUG_ALLERGY("Drug-Allergy Interaction", "Medication contains ingredients the patient is allergic to."),
    DRUG_DRUG("Drug-Drug Interaction", "Medications may interact with each other."),
    DRUG_CONDITION("Drug-Condition Contraindication", "Medication may be harmful given the patient's medical conditions."),
    DUPLICATE_THERAPY("Duplicate Therapy", "Patient is receiving multiple medications with similar effects.");

    private final String displayName;
    private final String description;

    AlertType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}