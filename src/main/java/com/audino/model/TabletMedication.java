package com.audino.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TabletMedication extends Medication {

    @JsonProperty("strength")
    private String strength;

    public TabletMedication() {
        super();
        this.medicationType = MedicationType.TABLET;
    }

    public TabletMedication(String medicationId, String genericName, String brandName, String strength) {
        super(medicationId, genericName, brandName, MedicationType.TABLET);
        this.strength = strength;
    }

    @Override
    public String getDosageInstructions(String dosage, String frequency, String duration) {
        return String.format("Take %s tablet(s) %s for %s.", dosage, frequency, duration);
    }

    @Override
    public boolean isValidDosage(String dosage) {
        if (dosage == null || dosage.trim().isEmpty()) return false;
        try {
            double dose = Double.parseDouble(dosage.trim());
            return dose > 0 && dose < 10; // Simple validation
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }
}