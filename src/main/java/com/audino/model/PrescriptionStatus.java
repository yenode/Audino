package com.audino.model;

/**
 * Enumeration representing the status of a prescription.
 */
public enum PrescriptionStatus {
    DRAFT("Draft", "Prescription is being created."),
    PENDING_REVIEW("Pending Review", "Prescription awaiting review."),
    APPROVED("Approved", "Prescription has been approved."),
    CANCELLED("Cancelled", "Prescription has been cancelled.");

    private final String displayName;
    private final String description;

    PrescriptionStatus(String displayName, String description) {
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