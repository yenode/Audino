package com.audino.service;

import com.audino.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AllergyCheckStrategy implements InteractionCheckStrategy {

    @Override
    public String getStrategyName() {
        return "Drug-Allergy Interaction Check";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<InteractionAlert> check(Patient patient, Prescription prescription, Map<String, Object> rules, List<Medication> allMedications) {
        List<InteractionAlert> alerts = new ArrayList<>();
        if (patient == null || patient.getAllergies().isEmpty() || rules == null) {
            return alerts;
        }

        Map<String, Object> allergyRules = (Map<String, Object>) rules.get("drugAllergyInteractions");
        if (allergyRules == null) return alerts;

        List<Medication> prescribedMedications = prescription.getPrescribedDrugs().stream()
            .map(drug -> allMedications.stream()
                .filter(m -> m.getMedicationId().equals(drug.getMedicationId()))
                .findFirst().orElse(null))
            .filter(m -> m != null)
            .collect(Collectors.toList());

        for (Medication med : prescribedMedications) {
            List<String> medIdentifiers = med.getInteractionIdentifiers().stream()
                                             .map(String::toLowerCase)
                                             .collect(Collectors.toList());
                                             
            for (String allergy : patient.getAllergies()) {
                String allergyLower = allergy.toLowerCase();

                allergyRules.forEach((key, ruleObj) -> {
                    Map<String, Object> rule = (Map<String, Object>) ruleObj;
                    List<String> keywords = (List<String>) rule.get("allergyKeywords");

                    if (keywords.stream().anyMatch(allergyLower::contains)) {
                        if (medIdentifiers.stream().anyMatch(keywords::contains)) {
                            alerts.add(createAlert(patient, med, allergy, rule));
                        }
                    }
                });
            }
        }
        return alerts;
    }

    private InteractionAlert createAlert(Patient p, Medication m, String allergy, Map<String, Object> rule) {
        String message = String.format(
            "Patient has a known allergy to '%s'. The prescribed medication, %s, is in a class of drugs related to this allergy.",
            allergy, m.getDisplayName()
        );
        return new InteractionAlert(
            AlertLevel.CRITICAL,
            AlertType.DRUG_ALLERGY,
            "Potential Allergic Reaction",
            message,
            (String) rule.get("recommendation"),
            m.getDisplayName(),
            allergy
        );
    }
}