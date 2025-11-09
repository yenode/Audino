package com.audino.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LiquidMedication extends Medication {

    @JsonProperty("concentration")
    private String concentration;

    public LiquidMedication() {
        super();
        this.medicationType = MedicationType.LIQUID;
    }

    @Override
    public String getDosageInstructions(String dosage, String frequency, String duration) {
        return String.format("Take %s of liquid %s for %s. Shake well before use.", dosage, frequency, duration);
    }
    
    @Override
    public boolean isValidDosage(String dosage) {
        if (dosage == null || dosage.trim().isEmpty()) return false;
        // Simple validation for formats like "5ml", "10 mg", etc.
        return dosage.toLowerCase().matches("\\d+(\\.\\d+)?\\s*(ml|mg)");
    }

    public String getConcentration() { return concentration; }
    public void setConcentration(String concentration) { this.concentration = concentration; }
}