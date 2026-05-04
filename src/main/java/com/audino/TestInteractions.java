package com.audino;

import com.audino.model.*;
import com.audino.service.*;
import java.util.*;

public class TestInteractions {
    public static void main(String[] args) throws Exception {
        DataService dataService = new DataService();
        dataService.loadAllData();
        InteractionEngine engine = new InteractionEngine();
        
        System.out.println("Loaded " + dataService.getAllPatients().size() + " patients.");
        System.out.println("Loaded " + dataService.getAllMedications().size() + " medications.");
        System.out.println("Loaded rules: " + dataService.getInteractionRules().keySet());
        
        for (Patient p : dataService.getAllPatients()) {
            if (p.getPatientId().equals("PAT-0002")) {
                Prescription rx = dataService.getActivePrescriberionForPatient(p.getPatientId());
                if (rx == null) {
                    System.out.println("No prescription for PAT-0002");
                    return;
                }
                System.out.println("Found Rx: " + rx.getPrescriptionId());
                List<InteractionAlert> alerts = engine.checkAllInteractionsAsync(
                    p, rx, dataService.getInteractionRules(), dataService.getAllMedications()
                ).join();
                System.out.println("Generated " + alerts.size() + " alerts!");
                for (InteractionAlert a : alerts) {
                    System.out.println("- " + a.getAlertType() + ": " + a.getMessage());
                }
            }
        }
    }
}
