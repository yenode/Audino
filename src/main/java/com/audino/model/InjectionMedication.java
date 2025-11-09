package com.audino.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InjectionMedication extends Medication {
    
    @JsonProperty("concentration")
    private String concentration;
    
    @JsonProperty("route")
    private String route;

    public InjectionMedication() {
        super();
        this.medicationType = MedicationType.INJECTION;
    }

    @Override
    public String getDosageInstructions(String dosage, String frequency, String duration) {
        return String.format("Administer %s via %s route %s for %s.", dosage, route, frequency, duration);
    }

    @Override
    public boolean isValidDosage(String dosage) {
        if (dosage == null || dosage.trim().isEmpty()) return false;
        // Simple validation for formats like "1ml", "10 units", etc.
        return dosage.toLowerCase().matches("\\d+(\\.\\d+)?\\s*(ml|units)");
    }
    
    public String getConcentration() { return concentration; }
    public void setConcentration(String concentration) { this.concentration = concentration; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
}