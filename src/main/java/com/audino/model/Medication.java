package com.audino.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "medicationType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TabletMedication.class, name = "TABLET"),
    @JsonSubTypes.Type(value = LiquidMedication.class, name = "LIQUID"),
    @JsonSubTypes.Type(value = InjectionMedication.class, name = "INJECTION")
})
public abstract class Medication {

    @JsonProperty("medicationId")
    protected String medicationId;

    @JsonProperty("genericName")
    protected String genericName;

    @JsonProperty("brandName")
    protected String brandName;

    @JsonProperty("activeIngredients")
    protected List<String> activeIngredients;

    @JsonProperty("interactionIdentifiers")
    protected List<String> interactionIdentifiers;

    @JsonProperty("medicationType")
    protected MedicationType medicationType;

    public Medication() {
        this.activeIngredients = new ArrayList<>();
        this.interactionIdentifiers = new ArrayList<>();
    }

    public Medication(String medicationId, String genericName, String brandName, MedicationType medicationType) {
        this();
        this.medicationId = medicationId;
        this.genericName = genericName;
        this.brandName = brandName;
        this.medicationType = medicationType;
    }

    public abstract String getDosageInstructions(String dosage, String frequency, String duration);
    
    public abstract boolean isValidDosage(String dosage);

    public String getDisplayName() {
        return (brandName != null && !brandName.trim().isEmpty()) ? brandName : genericName;
    }

    // Getters and Setters
    public String getMedicationId() { return medicationId; }
    public void setMedicationId(String medicationId) { this.medicationId = medicationId; }
    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public List<String> getActiveIngredients() { return new ArrayList<>(activeIngredients); }
    public void setActiveIngredients(List<String> ingredients) { this.activeIngredients = new ArrayList<>(ingredients); }
    public List<String> getInteractionIdentifiers() { return new ArrayList<>(interactionIdentifiers); }
    public void setInteractionIdentifiers(List<String> identifiers) { this.interactionIdentifiers = new ArrayList<>(identifiers); }
    public MedicationType getMedicationType() { return medicationType; }
    public void setMedicationType(MedicationType type) { this.medicationType = type; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medication that = (Medication) o;
        return Objects.equals(medicationId, that.medicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicationId);
    }
}