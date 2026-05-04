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

                Set<String> ids1 = new HashSet<>();
                if (med1.getInteractionIdentifiers() != null) ids1.addAll(med1.getInteractionIdentifiers());
                if (med1.getGenericName() != null) ids1.add(med1.getGenericName());
                if (med1.getBrandName() != null) ids1.add(med1.getBrandName());
                
                Set<String> ids2 = new HashSet<>();
                if (med2.getInteractionIdentifiers() != null) ids2.addAll(med2.getInteractionIdentifiers());
                if (med2.getGenericName() != null) ids2.add(med2.getGenericName());
                if (med2.getBrandName() != null) ids2.add(med2.getBrandName());

                drugDrugRules.forEach((key, ruleObj) -> {
                    Map<String, Object> rule = (Map<String, Object>) ruleObj;
                    Object drug1Obj = rule.get("drug1");
                    Object drug2Obj = rule.get("drug2");
                    
                    // Handle both String and List<String> for drug classes
                    List<String> drug1Classes = new ArrayList<>();
                    List<String> drug2Classes = new ArrayList<>();
                    
                    if (drug1Obj instanceof String) {
                        drug1Classes.add((String) drug1Obj);
                    } else if (drug1Obj instanceof List) {
                        drug1Classes.addAll((List<String>) drug1Obj);
                    }
                    
                    if (drug2Obj instanceof String) {
                        drug2Classes.add((String) drug2Obj);
                    } else if (drug2Obj instanceof List) {
                        drug2Classes.addAll((List<String>) drug2Obj);
                    }
                    
                    // Check if any combination of drug1 and drug2 classes match
                    boolean matchFound = false;
                    for (String drug1Class : drug1Classes) {
                        for (String drug2Class : drug2Classes) {
                            boolean pair1Match = ids1.stream().anyMatch(id -> id.equalsIgnoreCase(drug1Class)) &&
                                                 ids2.stream().anyMatch(id -> id.equalsIgnoreCase(drug2Class));
                            
                            boolean pair2Match = ids1.stream().anyMatch(id -> id.equalsIgnoreCase(drug2Class)) &&
                                                 ids2.stream().anyMatch(id -> id.equalsIgnoreCase(drug1Class));
                            
                            if (pair1Match || pair2Match) {
                                matchFound = true;
                                break;
                            }
                        }
                        if (matchFound) break;
                    }
                    
                    if (matchFound) {
                        alerts.add(createAlert(med1, med2, rule));
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