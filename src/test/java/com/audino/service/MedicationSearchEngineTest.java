package com.audino.service;

import com.audino.model.Medication;
import com.audino.model.TabletMedication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Medication Search Engine Tests")
class MedicationSearchEngineTest {

    @Test
    @DisplayName("Should suggest brand and generic names while typing")
    void shouldSuggestAsUserTypes() {
        List<Medication> medications = List.of(
                new TabletMedication("M001", "Ibuprofen", "Advil", "200mg"),
                new TabletMedication("M002", "Paracetamol", "Tylenol", "500mg"),
                new TabletMedication("M003", "Amoxicillin", "Amoxil", "250mg")
        );
        medications.get(0).setRxNormCode("5640");
        medications.get(1).setRxNormCode("32968");
        medications.get(2).setRxNormCode("723");

        MedicationSearchEngine engine = new MedicationSearchEngine(medications);
        List<Medication> suggestions = engine.suggest("adv", 5);

        assertFalse(suggestions.isEmpty());
        assertEquals("Ibuprofen", suggestions.get(0).getGenericName());

        List<Medication> rxNormSuggestions = engine.suggest("5640", 5);
        assertFalse(rxNormSuggestions.isEmpty());
        assertEquals("Ibuprofen", rxNormSuggestions.get(0).getGenericName());
    }

    @Test
    @DisplayName("Should autocorrect close misspellings")
    void shouldAutoCorrectMisspellings() {
        List<Medication> medications = List.of(
                new TabletMedication("M001", "Ibuprofen", "Advil", "200mg"),
                new TabletMedication("M002", "Paracetamol", "Tylenol", "500mg")
        );
        medications.get(0).setRxNormCode("5640");
        medications.get(1).setRxNormCode("32968");

        MedicationSearchEngine engine = new MedicationSearchEngine(medications);
        Optional<String> corrected = engine.autoCorrect("ibuprofne");

        assertTrue(corrected.isPresent());
        assertEquals("Ibuprofen", corrected.get());

        Optional<String> correctedFromRxNorm = engine.autoCorrect("5640");
        assertTrue(correctedFromRxNorm.isPresent());
        assertEquals("Ibuprofen", correctedFromRxNorm.get());
    }
}
