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

        System.out.println("[ALLERGY-DEBUG] prescribedMedications resolved: " + prescribedMedications.size() + " of " + prescription.getPrescribedDrugs().size() + " drugs. AllMeds size=" + allMedications.size());
        prescription.getPrescribedDrugs().forEach(d -> System.out.println("[ALLERGY-DEBUG]   drug.medicationId=" + d.getMedicationId()));
        prescribedMedications.forEach(m -> System.out.println("[ALLERGY-DEBUG]   resolved med=" + m.getDisplayName() + " id=" + m.getMedicationId()));

        for (Medication med : prescribedMedications) {
            List<String> medIdentifiers = new ArrayList<>();
            if (med.getInteractionIdentifiers() != null) {
                medIdentifiers.addAll(med.getInteractionIdentifiers().stream().map(String::toLowerCase).collect(Collectors.toList()));
            }
            if (med.getGenericName() != null) medIdentifiers.add(med.getGenericName().toLowerCase());
            if (med.getBrandName() != null) medIdentifiers.add(med.getBrandName().toLowerCase());
            System.out.println("[ALLERGY-DEBUG] med=" + med.getDisplayName() + " identifiers=" + medIdentifiers + " allergies=" + patient.getAllergies());
                                             
            for (String allergy : patient.getAllergies()) {
                String allergyLower = allergy.toLowerCase();

                allergyRules.forEach((key, ruleObj) -> {
                    Map<String, Object> rule = (Map<String, Object>) ruleObj;
                    List<String> keywords = (List<String>) rule.get("allergyKeywords");
                    List<String> medicationClasses = (List<String>) rule.get("medicationClasses");

                    // Check if patient allergy matches the rule's allergy keywords
                    if (keywords != null && keywords.stream().anyMatch(keyword -> allergyLower.contains(keyword.toLowerCase()))) {
                        System.out.println("[ALLERGY-DEBUG] allergy=" + allergy + " matches rule keywords=" + keywords + " medClasses=" + medicationClasses + " medIds=" + medIdentifiers);
                        // Check if prescribed medication's class matches the rule's medication classes
                        if (medicationClasses != null && medicationClasses.stream().anyMatch(cls ->
                                medIdentifiers.stream().anyMatch(medId -> medId.equalsIgnoreCase(cls)))) {
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