package com.audino.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Patient Model Tests")
public class PatientTest { // This line was changed

    private Patient patient;
    private final LocalDate birthDate = LocalDate.of(1980, 5, 15);

    @BeforeEach
    void setUp() {
        patient = new Patient("John", "Doe", birthDate);
    }

    @Test
    @DisplayName("Should create patient and generate ID")
    void testPatientCreation() {
        assertEquals("John", patient.getFirstName());
        assertEquals("Doe", patient.getLastName());
        assertEquals("John Doe", patient.getFullName());
        assertNotNull(patient.getPatientId());
        assertTrue(patient.getPatientId().startsWith("PAT-"));
    }

    @Test
    @DisplayName("Should calculate age correctly")
    void testAgeCalculation() {
        int expectedAge = LocalDate.now().getYear() - 1980;
        if (LocalDate.now().getDayOfYear() < birthDate.getDayOfYear()) {
            expectedAge--;
        }
        assertEquals(expectedAge, patient.getAge());
    }

    @Test
    @DisplayName("Should add and retrieve allergies correctly")
    void testAllergyManagement() {
        patient.addAllergy("Penicillin");
        patient.addAllergy("  Sulfa  ");
        patient.addAllergy("Penicillin");

        List<String> allergies = patient.getAllergies();
        assertEquals(2, allergies.size());
        assertTrue(allergies.contains("Penicillin"));
        assertTrue(allergies.contains("Sulfa"));
    }

    @Test
    @DisplayName("Should add and retrieve conditions correctly")
    void testConditionManagement() {
        patient.addChronicCondition("Hypertension");
        patient.addChronicCondition("Diabetes");
        patient.addChronicCondition("Hypertension");

        List<String> conditions = patient.getChronicConditions();
        assertEquals(2, conditions.size());
        assertTrue(conditions.contains("Hypertension"));
        assertTrue(conditions.contains("Diabetes"));
    }
    
    @Test
    @DisplayName("Should return defensive copies of lists")
    void testDefensiveCopies() {
        List<String> allergies = patient.getAllergies();
        allergies.add("Aspirin");
        
        assertEquals(0, patient.getAllergies().size());
    }
}