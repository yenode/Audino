package com.audino.service;

import com.audino.model.Medication;
import com.audino.model.Patient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Data Service Tests")
public class DataServiceTest {

    private static DataService dataService;
    private static Path sqlitePath;

    @BeforeAll
    static void setUp() {
        try {
            sqlitePath = Files.createTempFile("audino-data-service-test", ".db");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test SQLite database", e);
        }
        System.setProperty("audino.sqlite.path", sqlitePath.toString());

        // Initialize config manager before data service
        com.audino.util.ConfigurationManager.getInstance().initialize();
        dataService = new DataService();
        dataService.loadAllData();
    }

    @AfterAll
    static void tearDown() {
        System.clearProperty("audino.sqlite.path");
        if (sqlitePath != null) {
            try {
                Files.deleteIfExists(sqlitePath);
            } catch (Exception ignored) {
            }
        }
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
        List<Patient> results = dataService.searchPatients("Mandal");
        assertEquals(1, results.size());
        assertEquals("Mandal", results.get(0).getLastName());
        assertEquals("Mridankan", results.get(0).getFirstName());
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

    @Test
    @DisplayName("Should persist patient CRUD operations in SQLite")
    void testPatientCrudPersistsInSQLite() {
        Patient patient = new Patient("SQLite", "Patient", LocalDate.of(1990, 1, 1));
        patient.setPatientId("PAT-SQLITE-CRUD");
        patient.addAllergy("Dust");
        patient.addChronicCondition("Hypertension");

        dataService.savePatient(patient);

        DataService reloadedService = new DataService();
        reloadedService.loadAllData();
        List<Patient> inserted = reloadedService.searchPatients("SQLite");
        assertFalse(inserted.isEmpty());
        assertEquals("PAT-SQLITE-CRUD", inserted.get(0).getPatientId());

        patient.setLastName("Updated");
        dataService.updatePatient(patient);

        reloadedService = new DataService();
        reloadedService.loadAllData();
        List<Patient> updated = reloadedService.searchPatients("Updated");
        assertFalse(updated.isEmpty());
        assertEquals("PAT-SQLITE-CRUD", updated.get(0).getPatientId());

        dataService.deletePatient(patient);
        reloadedService = new DataService();
        reloadedService.loadAllData();
        List<Patient> deleted = reloadedService.searchPatients("Updated");
        assertTrue(deleted.isEmpty());
    }
}