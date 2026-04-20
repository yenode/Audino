package com.audino.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConfigurationManager Tests")
public class ConfigurationManagerTest {

    @Test
    @DisplayName("Should return the same singleton instance")
    void testSingletonInstance() {
        ConfigurationManager first = ConfigurationManager.getInstance();
        ConfigurationManager second = ConfigurationManager.getInstance();

        assertSame(first, second);
    }

    @Test
    @DisplayName("Should initialize object mapper with expected configuration")
    void testInitializeObjectMapper() {
        ConfigurationManager config = ConfigurationManager.getInstance();

        config.initialize();

        assertNotNull(config.getObjectMapper());
        assertFalse(config.getObjectMapper().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    @DisplayName("Should return configured data file properties")
    void testConfiguredDataFileProperties() {
        ConfigurationManager config = ConfigurationManager.getInstance();

        config.initialize();

        assertEquals("/data/patients.json", config.getPatientsDataFile());
        assertEquals("/data/medications.json", config.getMedicationsDataFile());
        assertEquals("/data/interaction-rules.json", config.getInteractionRulesDataFile());
    }

    @Test
    @DisplayName("Should return default prescriptions data file when property is missing")
    void testDefaultPrescriptionsDataFile() {
        ConfigurationManager config = ConfigurationManager.getInstance();

        config.initialize();

        assertEquals("/data/prescriptions.json", config.getPrescriptionsDataFile());
    }
}
