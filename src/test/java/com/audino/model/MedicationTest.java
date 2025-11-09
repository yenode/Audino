package com.audino.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Medication Model Tests")
public class MedicationTest { // This line was changed
    private TabletMedication tablet;
    private LiquidMedication liquid;
    private InjectionMedication injection;

    @BeforeEach
    void setUp() {
        tablet = new TabletMedication("MED-T01", "Ibuprofen", "Advil", "200mg");
        liquid = new LiquidMedication();
        liquid.setMedicationId("MED-L01");
        injection = new InjectionMedication();
        injection.setMedicationId("MED-I01");
    }

    @Test
    @DisplayName("Should return correct display name")
    void testDisplayName() {
        assertEquals("Advil", tablet.getDisplayName());
        Medication genericOnly = new TabletMedication("MED-T02", "Generic Drug", " ", "10mg");
        assertEquals("Generic Drug", genericOnly.getDisplayName());
    }
    
    @Test
    @DisplayName("Should validate tablet dosages")
    void testTabletDosageValidation() {
        assertTrue(tablet.isValidDosage("1"));
        assertTrue(tablet.isValidDosage("2.5"));
        assertFalse(tablet.isValidDosage("15"));
        assertFalse(tablet.isValidDosage("abc"));
        assertFalse(tablet.isValidDosage(null));
    }
    
    @Test
    @DisplayName("Should validate liquid dosages")
    void testLiquidDosageValidation() {
        assertTrue(liquid.isValidDosage("5ml"));
        assertTrue(liquid.isValidDosage("10.5 mg"));
        assertFalse(liquid.isValidDosage("5 L"));
        assertFalse(liquid.isValidDosage("ten ml"));
    }

    @Test
    @DisplayName("Should validate injection dosages")
    void testInjectionDosageValidation() {
        assertTrue(injection.isValidDosage("1ml"));
        assertTrue(injection.isValidDosage("50 units"));
        assertFalse(injection.isValidDosage("1 g"));
        assertFalse(injection.isValidDosage("one unit"));
    }
}