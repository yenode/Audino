package com.audino.service;

import com.audino.model.Medication;
import com.audino.model.Patient;
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