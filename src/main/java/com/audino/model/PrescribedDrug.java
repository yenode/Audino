package com.audino.model;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import java.util.Objects;

public class PrescribedDrug {

    private String medicationId;
    private String dosage;
    private String frequency;
    private String duration;
    private String specialInstructions;
    private String prescribedBy;

    @BsonIgnore
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
    
    @BsonIgnore
    public Medication getMedication() { return medication; }
    @BsonIgnore
    public void setMedication(Medication medication) { this.medication = medication; }

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