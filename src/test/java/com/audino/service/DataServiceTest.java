package com.audino.service;

import com.audino.model.Medication;
import com.audino.model.Patient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Data Service Tests")
public class DataServiceTest {

    private static DataService dataService;

    @BeforeAll
    static void setUp() {
        // Initialize config manager before data service
        com.audino.util.ConfigurationManager.getInstance().initialize();
        dataService = new DataService();
        dataService.loadAllData();
    }

    @Test
    @DisplayName("Should load all data files without errors")
    void testLoadAllData() {
        assertNotNull(dataService.getAllPatients());
        assertNotNull(dataService.getAllMedications());
        assertNotNull(dataService.getInteractionRules());
        assertFalse(dataService.getAllPatients().isEmpty());
        assertFalse(dataService.getAllMedications().isEmpty());
        assertFalse(dataService.getInteractionRules().isEmpty());
    }

    @Test
    @DisplayName("Should search for patients correctly")
    void testSearchPatients() {
        List<Patient> results = dataService.searchPatients("Smith");
        assertEquals(1, results.size());
        assertEquals("Smith", results.get(0).getLastName());
    }
    
    @Test
    @DisplayName("Should return all patients for empty search")
    void testEmptyPatientSearch() {
        List<Patient> allPatients = dataService.getAllPatients();
        List<Patient> searchResults = dataService.searchPatients("");
        assertEquals(allPatients.size(), searchResults.size());
    }

    @Test
    @DisplayName("Should search for medications correctly")
    void testSearchMedications() {
        List<Medication> results = dataService.searchMedications("Advil");
        assertEquals(1, results.size());
        assertEquals("Ibuprofen", results.get(0).getGenericName());
    }

    @Test
    @DisplayName("Should return all medications for empty search")
    void testEmptyMedicationSearch() {
        List<Medication> allMedications = dataService.getAllMedications();
        List<Medication> searchResults = dataService.searchMedications(null);
        assertEquals(allMedications.size(), searchResults.size());
    }
}