package com.audino.service;

import com.audino.model.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DrugDrugCheckStrategy implements InteractionCheckStrategy {
    @Override
    public String getStrategyName() {
        return "Drug-Drug Interaction Check";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<InteractionAlert> check(Patient patient, Prescription prescription, Map<String, Object> rules, List<Medication> allMedications) {
        List<InteractionAlert> alerts = new ArrayList<>();
        
        List<Medication> meds = prescription.getPrescribedDrugs().stream()
            .map(drug -> allMedications.stream()
                .filter(m -> m.getMedicationId().equals(drug.getMedicationId()))
                .findFirst().orElse(null))
            .filter(m -> m != null)
            .collect(Collectors.toList());

        if (meds.size() < 2 || rules == null) {
            return alerts;
        }

        Map<String, Object> drugDrugRules = (Map<String, Object>) rules.get("drugDrugInteractions");
        if (drugDrugRules == null) return alerts;

        for (int i = 0; i < meds.size(); i++) {
            for (int j = i + 1; j < meds.size(); j++) {
                Medication med1 = meds.get(i);
                Medication med2 = meds.get(j);

                Set<String> ids1 = new HashSet<>(med1.getInteractionIdentifiers());
                Set<String> ids2 = new HashSet<>(med2.getInteractionIdentifiers());

                drugDrugRules.forEach((key, ruleObj) -> {
                    Map<String, Object> rule = (Map<String, Object>) ruleObj;
                    String drug1Class = (String) rule.get("drug1");
                    String drug2Class = (String) rule.get("drug2");
                    
                    if (drug1Class != null && drug2Class != null) {
                        boolean pair1Match = ids1.stream().anyMatch(id -> id.equalsIgnoreCase(drug1Class)) &&
                                             ids2.stream().anyMatch(id -> id.equalsIgnoreCase(drug2Class));
                        
                        boolean pair2Match = ids1.stream().anyMatch(id -> id.equalsIgnoreCase(drug2Class)) &&
                                             ids2.stream().anyMatch(id -> id.equalsIgnoreCase(drug1Class));
                        
                        if (pair1Match || pair2Match) {
                            alerts.add(createAlert(med1, med2, rule));
                        }
                    }
                });
            }
        }
        return alerts;
    }

    private InteractionAlert createAlert(Medication m1, Medication m2, Map<String, Object> rule) {
        String severity = (String) rule.get("severity");
        AlertLevel level = "CRITICAL".equalsIgnoreCase(severity) ? AlertLevel.CRITICAL : AlertLevel.WARNING;
        String message = String.format(
            "%s and %s may interact. %s",
            m1.getDisplayName(), m2.getDisplayName(), rule.get("description")
        );
        String involved = m1.getDisplayName() + " & " + m2.getDisplayName();
        
        return new InteractionAlert(
            level,
            AlertType.DRUG_DRUG,
            "Drug-Drug Interaction",
            message,
            (String) rule.get("recommendation"),
            involved,
            null
        );
    }
}