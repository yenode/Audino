package com.audino.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Prescription {

    private String prescriptionId;
    private String patientId;
    private List<PrescribedDrug> prescribedDrugs;
    private LocalDateTime createdAt;
    private String prescribedBy;
    private PrescriptionStatus status;
    private List<InteractionAlert> alerts;
    private int version;

    public Prescription() {
        this.prescribedDrugs = new ArrayList<>();
        this.alerts = new ArrayList<>();
    }

    public Prescription(Patient patient, String prescribedBy) {
        this.prescriptionId = "RX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.patientId = patient.getPatientId();
        this.prescribedBy = prescribedBy;
        this.prescribedDrugs = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.status = PrescriptionStatus.DRAFT;
        this.alerts = new ArrayList<>();
    }

    public void addPrescribedDrug(PrescribedDrug drug) {
        if (drug != null && drug.isValid()) {
            this.prescribedDrugs.add(drug);
        }
    }

    public void removePrescribedDrug(PrescribedDrug drug) {
        this.prescribedDrugs.remove(drug);
    }
    
    public boolean isEmpty() {
        return prescribedDrugs.isEmpty();
    }
    
    public Double getTotalBill() {
        return prescribedDrugs.stream()
                .map(drug -> {
                    drug.calculateCost();
                    return drug.getTotalCost() != null ? drug.getTotalCost() : 0.0;
                })
                .reduce(0.0, Double::sum);
    }
    
    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public List<PrescribedDrug> getPrescribedDrugs() { return new ArrayList<>(prescribedDrugs); }
    public void setPrescribedDrugs(List<PrescribedDrug> prescribedDrugs) { this.prescribedDrugs = prescribedDrugs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getPrescribedBy() { return prescribedBy; }
    public void setPrescribedBy(String prescribedBy) { this.prescribedBy = prescribedBy; }
    public PrescriptionStatus getStatus() { return status; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }
    public List<InteractionAlert> getAlerts() { return new ArrayList<>(alerts); }
    public void setAlerts(List<InteractionAlert> alerts) { this.alerts = new ArrayList<>(alerts); }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prescription that = (Prescription) o;
        return Objects.equals(prescriptionId, that.prescriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prescriptionId);
    }
}