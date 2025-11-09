package com.audino.service;

import com.audino.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConditionCheckStrategy implements InteractionCheckStrategy {

    @Override
    public String getStrategyName() {
        return "Drug-Condition Contraindication Check";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<InteractionAlert> check(Patient patient, Prescription prescription, Map<String, Object> rules, List<Medication> allMedications) {
        List<InteractionAlert> alerts = new ArrayList<>();
        if (patient == null || patient.getChronicConditions() == null || patient.getChronicConditions().isEmpty() || rules == null) {
            return alerts;
        }

        Map<String, Object> conditionRules = (Map<String, Object>) rules.get("drugConditionInteractions");
        if (conditionRules == null) return alerts;
        
        List<Medication> prescribedMedications = prescription.getPrescribedDrugs().stream()
            .map(drug -> allMedications.stream()
                .filter(m -> m.getMedicationId().equals(drug.getMedicationId()))
                .findFirst().orElse(null))
            .filter(m -> m != null)
            .collect(Collectors.toList());

        for (Medication med : prescribedMedications) {
            for (String condition : patient.getChronicConditions()) {
                String conditionLower = condition.toLowerCase();

                conditionRules.forEach((key, ruleObj) -> {
                    Map<String, Object> rule = (Map<String, Object>) ruleObj;
                    List<String> keywords = (List<String>) rule.get("conditionKeywords");
                    List<String> medClasses = (List<String>) rule.get("medicationClasses");

                    if (keywords != null && medClasses != null) {
                        boolean conditionMatches = keywords.stream().anyMatch(keyword -> 
                            conditionLower.contains(keyword.toLowerCase()));
                        
                        if (conditionMatches) {
                            boolean medicationMatches = medClasses.stream().anyMatch(medClass ->
                                med.getInteractionIdentifiers().stream().anyMatch(id -> 
                                    id.equalsIgnoreCase(medClass)));
                            
                            if (medicationMatches) {
                                alerts.add(createAlert(patient, med, condition, rule));
                            }
                        }
                    }
                });
            }
        }
        return alerts;
    }

    private InteractionAlert createAlert(Patient p, Medication m, String condition, Map<String, Object> rule) {
        String severity = (String) rule.get("severity");
        AlertLevel level = "CRITICAL".equalsIgnoreCase(severity) ? AlertLevel.CRITICAL : AlertLevel.WARNING;
        String message = String.format(
            "Prescribing %s is potentially unsafe for patients with '%s'. %s",
            m.getDisplayName(), condition, rule.get("description")
        );
        
        return new InteractionAlert(
            level,
            AlertType.DRUG_CONDITION,
            "Drug-Condition Contraindication",
            message,
            (String) rule.get("recommendation"),
            m.getDisplayName(),
            condition
        );
    }
}