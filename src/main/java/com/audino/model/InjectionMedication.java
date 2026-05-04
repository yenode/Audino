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
        try {
            double dose = Double.parseDouble(dosage.trim());
            return dose > 0;
        } catch (NumberFormatException e) {
            return dosage.toLowerCase().matches("^[0-9]+(\\.[0-9]+)?\\s*(ml|units|mg|mcg)$");
        }
    }
    
    public String getConcentration() { return concentration; }
    public void setConcentration(String concentration) { this.concentration = concentration; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
}