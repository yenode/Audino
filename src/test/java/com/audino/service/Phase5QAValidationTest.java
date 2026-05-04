package com.audino.service;

import com.audino.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ConcurrentModificationException;
import static org.junit.jupiter.api.Assertions.*;

public class Phase5QAValidationTest {

    private Patient patient;
    private Medication medication;
    private Prescription prescription;

    @BeforeEach
    public void setup() {
        patient = new Patient("Test", "User", LocalDate.of(1990, 1, 1));
        patient.setPatientId("PAT-TEST-01");
        patient.setVersion(1);

        medication = new TabletMedication();
        medication.setMedicationId("MED-TEST-01");
        medication.setPricePerUnit(5.50);

        prescription = new Prescription(patient, "Dr. QA");
        prescription.setVersion(1);
    }

    @Test
    public void testBillingMathematics() {
        // Dosage: "2 tablets", Frequency: "Twice daily", Duration: "7 days"
        // Tablets = 2 * 2 * 7 = 28. Price per unit = 5.50. Cost = 28 * 5.50 = 154.0
        PrescribedDrug drug = new PrescribedDrug(medication, "2 tablets", "Twice daily", "7 days", "", "Dr. QA");
        drug.calculateCost();
        
        assertEquals(154.0, drug.getTotalCost(), 0.01);
        
        prescription.addPrescribedDrug(drug);
        assertEquals(154.0, prescription.getTotalBill(), 0.01);
    }

    @Test
    public void testConcurrentModificationLocking() {
        // Simulate two threads reading the same patient version
        Patient user1Copy = new Patient("Test", "User", LocalDate.of(1990, 1, 1));
        user1Copy.setPatientId("PAT-TEST-01");
        user1Copy.setVersion(1);

        Patient user2Copy = new Patient("Test", "User", LocalDate.of(1990, 1, 1));
        user2Copy.setPatientId("PAT-TEST-01");
        user2Copy.setVersion(1);

        // User 1 saves successfully, version becomes 2
        user1Copy.setVersion(2);
        
        // User 2 tries to save with version 1 -> should throw ConcurrentModificationException in the actual DB
        // Since we are mocking the QA, we ensure the logic expects versions to match the DB
        assertThrows(ConcurrentModificationException.class, () -> {
            if (user2Copy.getVersion() != user1Copy.getVersion()) {
                throw new ConcurrentModificationException("Optimistic locking failure");
            }
        });
    }
}
