package com.audino.model;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Patient {

    @BsonId
    private ObjectId id;
    private String patientId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String contactNumber;
    private List<String> allergies;
    private List<String> chronicConditions;

    public Patient() {
        this.allergies = new ArrayList<>();
        this.chronicConditions = new ArrayList<>();
    }

    public Patient(String firstName, String lastName, LocalDate dateOfBirth) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
    public List<String> getAllergies() { return new ArrayList<>(allergies); }
    public void setAllergies(List<String> allergies) { this.allergies = new ArrayList<>(allergies != null ? allergies : new ArrayList<>()); }
    public void addAllergy(String allergy) {
        if (allergy != null && !allergy.trim().isEmpty() && !allergies.contains(allergy.trim())) {
            allergies.add(allergy.trim());
        }
    }
    public List<String> getChronicConditions() { return new ArrayList<>(chronicConditions); }
    public void setChronicConditions(List<String> chronicConditions) { this.chronicConditions = new ArrayList<>(chronicConditions != null ? chronicConditions : new ArrayList<>()); }
    public void addChronicCondition(String condition) {
        if (condition != null && !condition.trim().isEmpty() && !chronicConditions.contains(condition.trim())) {
            chronicConditions.add(condition.trim());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(id, patient.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Patient{id='%s', name='%s', age=%d}",
                           id, getFullName(), getAge());
    }
}