package com.audino.service;

import com.audino.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Interaction Engine Tests")
public class InteractionEngineTest {
    private static InteractionEngine engine;
    private static DataService dataService;
    private static Patient patientWithAllergyAndCondition;
    private static Medication penicillinMed;
    private static Medication nsaidMed;
    private static Medication warfarinMed;
    private static List<Medication> allMedications;

    @BeforeAll
    static void setUp() {
        com.audino.util.ConfigurationManager.getInstance().initialize();
        engine = new InteractionEngine();
        dataService = new DataService();
        dataService.loadAllData();
        
        allMedications = dataService.getAllMedications();
        
        List<Patient> smiths = dataService.searchPatients("Smith");
        assertFalse(smiths.isEmpty(), "Test patient 'Smith' not found in patients.json");
        patientWithAllergyAndCondition = smiths.get(0);

        penicillinMed = allMedications.stream().filter(m -> "Amoxicillin".equals(m.getGenericName())).findFirst().get();
        nsaidMed = allMedications.stream().filter(m -> "Ibuprofen".equals(m.getGenericName())).findFirst().get();
        warfarinMed = allMedications.stream().filter(m -> "Warfarin".equals(m.getGenericName())).findFirst().get();
    }
    
    @AfterAll
    static void tearDown() {
        engine.shutdown();
    }

    @Test
    @DisplayName("Should detect Drug-Allergy interaction")
    void testDrugAllergyInteraction() throws ExecutionException, InterruptedException {
        Prescription prescription = new Prescription(patientWithAllergyAndCondition, "Dr. Test");
        prescription.addPrescribedDrug(new PrescribedDrug(penicillinMed, "5ml", "tid", "10d", "", ""));

        List<InteractionAlert> alerts = engine.checkAllInteractionsAsync(patientWithAllergyAndCondition, prescription, dataService.getInteractionRules(), allMedications).get();
        
        assertTrue(alerts.stream().anyMatch(a -> a.getAlertType() == AlertType.DRUG_ALLERGY));
    }

    @Test
    @DisplayName("Should detect Drug-Condition interaction")
    void testDrugConditionInteraction() throws ExecutionException, InterruptedException {
        Prescription prescription = new Prescription(patientWithAllergyAndCondition, "Dr. Test");
        prescription.addPrescribedDrug(new PrescribedDrug(nsaidMed, "1", "bid", "5d", "", ""));
        
        List<InteractionAlert> alerts = engine.checkAllInteractionsAsync(patientWithAllergyAndCondition, prescription, dataService.getInteractionRules(), allMedications).get();
        
        assertTrue(alerts.stream().anyMatch(a -> a.getAlertType() == AlertType.DRUG_CONDITION));
    }

    @Test
    @DisplayName("Should detect Drug-Drug interaction")
    void testDrugDrugInteraction() throws ExecutionException, InterruptedException {
        Prescription prescription = new Prescription(patientWithAllergyAndCondition, "Dr. Test");
        prescription.addPrescribedDrug(new PrescribedDrug(nsaidMed, "1", "bid", "5d", "", ""));
        prescription.addPrescribedDrug(new PrescribedDrug(warfarinMed, "1", "daily", "30d", "", ""));
        
        List<InteractionAlert> alerts = engine.checkAllInteractionsAsync(patientWithAllergyAndCondition, prescription, dataService.getInteractionRules(), allMedications).get();
        
        assertTrue(alerts.stream().anyMatch(a -> a.getAlertType() == AlertType.DRUG_DRUG));
    }
    
    @Test
    @DisplayName("Should produce no alerts for safe prescription")
    void testSafePrescription() throws ExecutionException, InterruptedException {
        Patient safePatient = dataService.searchPatients("Doe").get(0);
        Medication safeMed = allMedications.stream().filter(m -> "Lisinopril".equals(m.getGenericName())).findFirst().get();
        
        Prescription prescription = new Prescription(safePatient, "Dr. Test");
        prescription.addPrescribedDrug(new PrescribedDrug(safeMed, "1", "daily", "30d", "", ""));
        
        List<InteractionAlert> alerts = engine.checkAllInteractionsAsync(safePatient, prescription, dataService.getInteractionRules(), allMedications).get();
        
        assertTrue(alerts.isEmpty());
    }
}