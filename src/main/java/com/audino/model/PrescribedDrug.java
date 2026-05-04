package com.audino.model;

import java.util.Objects;

public class PrescribedDrug {

    private String medicationId;
    private String dosage;
    private String frequency;
    private String duration;
    private String specialInstructions;
    private String prescribedBy;
    private Double totalCost;

    private Medication medication;

    public PrescribedDrug() {
    }

    public PrescribedDrug(Medication medication, String dosage, String frequency, String duration, String specialInstructions, String prescribedBy) {
        this.medication = medication;
        this.medicationId = medication.getMedicationId();
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.specialInstructions = specialInstructions;
        this.prescribedBy = prescribedBy;
    }
    
    public boolean isValid() {
        return medication != null && dosage != null && !dosage.isEmpty() &&
               frequency != null && !frequency.isEmpty() && medication.isValidDosage(dosage);
    }
    
    public void calculateCost() {
        if (medication != null && medication.getPricePerUnit() != null) {
            try {
                // Naive calculation for demo purposes. 
                // Parses the first number it finds in dosage and duration to calculate a multiplier.
                double doseUnits = parseFirstNumber(dosage, 1.0);
                double durationDays = parseFirstNumber(duration, 1.0);
                double freqMultiplier = parseFrequency(frequency);
                
                this.totalCost = doseUnits * durationDays * freqMultiplier * medication.getPricePerUnit();
            } catch (Exception e) {
                this.totalCost = 0.0;
            }
        } else {
            this.totalCost = null;
        }
    }
    
    private double parseFirstNumber(String text, double defaultVal) {
        if (text == null || text.trim().isEmpty()) return defaultVal;
        String num = text.replaceAll("[^0-9.]", "");
        if (num.isEmpty() || num.equals(".")) return defaultVal;
        try {
            return Double.parseDouble(num);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
    
    private double parseFrequency(String freq) {
        if (freq == null) return 1.0;
        String lower = freq.toLowerCase();
        if (lower.contains("twice") || lower.contains("bid") || lower.contains("2 times")) return 2.0;
        if (lower.contains("three") || lower.contains("tid") || lower.contains("3 times")) return 3.0;
        if (lower.contains("four") || lower.contains("qid") || lower.contains("4 times")) return 4.0;
        return 1.0;
    }

    public String getMedicationId() { return medicationId; }
    public void setMedicationId(String medicationId) { this.medicationId = medicationId; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    public String getPrescribedBy() { return prescribedBy; }
    public void setPrescribedBy(String prescribedBy) { this.prescribedBy = prescribedBy; }
    
    public Medication getMedication() { return medication; }
    public void setMedication(Medication medication) { this.medication = medication; }
    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrescribedDrug that = (PrescribedDrug) o;
        return Objects.equals(medicationId, that.medicationId) && Objects.equals(dosage, that.dosage) && Objects.equals(frequency, that.frequency) && Objects.equals(duration, that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicationId, dosage, frequency, duration);
    }
}