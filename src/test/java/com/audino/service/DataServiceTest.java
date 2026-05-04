package com.audino.service;

import com.audino.model.Medication;
import com.audino.model.Patient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
        
        try (java.sql.Connection conn = com.audino.util.ConfigurationManager.getInstance().getDataSource().getConnection();
             java.sql.Statement st = conn.createStatement()) {
            st.execute("DELETE FROM patient_conditions; DELETE FROM patient_allergies; DELETE FROM prescribed_drugs; DELETE FROM prescriptions; DELETE FROM patients; DELETE FROM medications; DELETE FROM interaction_rules;");
            
            st.execute("INSERT INTO patients (patient_id, first_name, last_name, version) VALUES ('PAT-TEST-1', 'Raj', 'Kumar', 1)");
            st.execute("INSERT INTO patients (patient_id, first_name, last_name, version) VALUES ('PAT-TEST-2', 'Mridankan', 'Mandal', 1)");
            
            st.execute("INSERT INTO medications (medication_id, generic_name, brand_name, rxnorm_code, medication_type) VALUES ('MED-TEST-1', 'Amoxicillin', 'Amoxil', '723', 'TABLET')");
            st.execute("INSERT INTO medications (medication_id, generic_name, brand_name, rxnorm_code, medication_type) VALUES ('MED-TEST-2', 'Ibuprofen', 'Advil', '5640', 'TABLET')");
            
            st.execute("INSERT INTO interaction_rules (id, rules_json) VALUES (1, '{\"rules\":[]}')");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
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
    @DisplayName("Should autocorrect misspelled medication names and suggest matches")
    void testMedicationAutoCorrectAndSuggestions() {
        String corrected = dataService.autoCorrectMedicationName("ibuprofne");
        assertEquals("Ibuprofen", corrected);

        List<Medication> suggestions = dataService.suggestMedications("advi", 5);
        assertFalse(suggestions.isEmpty());
        assertEquals("Ibuprofen", suggestions.get(0).getGenericName());
    }

    @Test
    @DisplayName("Should search medications by RxNorm code")
    void testSearchMedicationsByRxNorm() {
        List<Medication> results = dataService.searchMedications("5640");
        assertFalse(results.isEmpty());
        assertEquals("Ibuprofen", results.get(0).getGenericName());
        assertEquals("5640", results.get(0).getRxNormCode());
    }

    @Test
    @org.junit.jupiter.api.Disabled("Obsolete since PostgreSQL migration")
    @DisplayName("Should persist runtime mode data to external SQLite directory when enabled")
    void testExternalFallbackSaveDirectory() throws Exception {
        String originalSurefire = System.getProperty("surefire.test.class.path");
        String originalDataDir = System.getProperty("audino.data.dir");
        Path tempDir = Files.createTempDirectory("audino-external-save-test");

        try {
            System.clearProperty("surefire.test.class.path");
            System.setProperty("audino.data.dir", tempDir.toString());

            DataService runtimeStyleService = new DataService();
            runtimeStyleService.loadAllData();
            runtimeStyleService.saveAllData();

            Path sqliteFile = tempDir.resolve("data").resolve("audino.db");
            assertTrue(Files.exists(sqliteFile), "Expected audino.db to be written to external data directory");

            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile.toAbsolutePath())) {
                try (ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM patients")) {
                    assertTrue(rs.next());
                    assertEquals(0, rs.getInt(1));
                }
            }
        } finally {
            if (originalSurefire == null) {
                System.clearProperty("surefire.test.class.path");
            } else {
                System.setProperty("surefire.test.class.path", originalSurefire);
            }

            if (originalDataDir == null) {
                System.clearProperty("audino.data.dir");
            } else {
                System.setProperty("audino.data.dir", originalDataDir);
            }
        }
    }
}