package com.audino.model;

/**
 * Enumeration representing different levels of drug interaction alerts.
 * Used for color-coding and prioritizing warnings in the user interface.
 */
public enum AlertLevel {
    /**
     * Critical alert requiring immediate attention (Red).
     * Indicates potentially life-threatening interactions.
     */
    CRITICAL("Critical", "#EF4444", "This interaction may cause serious harm and should be avoided."),

    /**
     * Warning alert requiring careful consideration (Orange).
     * Indicates interactions that may cause adverse effects but are manageable.
     */
    WARNING("Warning", "#F59E0B", "This interaction requires monitoring and may need dosage adjustment."),

    /**
     * Informational alert for awareness (Yellow).
     * Indicates minor interactions that should be noted.
     */
    INFO("Information", "#EAB308", "This interaction is generally minor but should be monitored.");

    private final String displayName;
    private final String colorCode;
    private final String description;

    AlertLevel(String displayName, String colorCode, String description) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}