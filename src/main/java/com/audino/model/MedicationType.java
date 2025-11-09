package com.audino.model;

/**
 * Enumeration representing different types of medication forms.
 * Used for inheritance hierarchy and dosage instruction formatting.
 */
public enum MedicationType {
    TABLET("Tablet"),
    LIQUID("Liquid"),
    INJECTION("Injection");

    private final String displayName;

    MedicationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}