package com.audino.util;

import com.audino.model.*;
import com.audino.service.DataService;

import java.time.LocalDate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.lang.reflect.Method;

public class DatabaseSeeder {

    public static void main(String[] args) {
        System.out.println("Starting Database Seeding and Testing...");
        
        try {
            DataService dataService = new DataService();
            dataService.loadAllData();
            
            System.out.println("Schema Initialized. Injecting fake data...");

            // Access getConnection via reflection since it's private
            Method getConnectionMethod = DataService.class.getDeclaredMethod("getConnection");
            getConnectionMethod.setAccessible(true);
            Connection conn = (Connection) getConnectionMethod.invoke(dataService);

            // Seed Medications
            try (Statement st = conn.createStatement()) {
                st.execute("DELETE FROM prescribed_drugs");
                st.execute("DELETE FROM prescriptions");
                st.execute("DELETE FROM patients");
                st.execute("DELETE FROM medications");
                
                String insertMed = "INSERT INTO medications (medication_id, generic_name, brand_name, medication_type, price_per_unit, version) VALUES (?, ?, ?, ?, ?, 1)";
                try (PreparedStatement ps = conn.prepareStatement(insertMed)) {
                    ps.setString(1, "MED-SEED-01");
                    ps.setString(2, "Amoxicillin");
                    ps.setString(3, "Amoxil");
                    ps.setString(4, "TABLET");
                    ps.setDouble(5, 5.00);
                    ps.addBatch();
                    
                    ps.setString(1, "MED-SEED-02");
                    ps.setString(2, "Lisinopril");
                    ps.setString(3, "Prinivil");
                    ps.setString(4, "TABLET");
                    ps.setDouble(5, 0); // Trigger PricingPrompt testing
                    ps.addBatch();
                    
                    ps.executeBatch();
                }
            }

            // Create Fake Patients
            Patient p1 = new Patient("John", "Doe", LocalDate.of(1985, 5, 20));
            p1.setPatientId("PAT-SEED-01");
            p1.setGender("Male");
            p1.setContactNumber("555-0100");
            p1.getAllergies().add("Penicillin");
            p1.getChronicConditions().add("Hypertension");

            Patient p2 = new Patient("Jane", "Smith", LocalDate.of(1992, 8, 15));
            p2.setPatientId("PAT-SEED-02");
            p2.setGender("Female");
            p2.setContactNumber("555-0200");
            p2.getChronicConditions().add("Asthma");

            dataService.savePatient(p1);
            dataService.savePatient(p2);
            System.out.println("Patients seeded successfully.");
            
            // Reload to pick up medications
            dataService.loadAllData();
            
            // Create a fake prescription
            Medication m1 = dataService.getAllMedications().stream().filter(m -> m.getMedicationId().equals("MED-SEED-01")).findFirst().get();
            Prescription rx = new Prescription(p1, "Dr. Robot");
            PrescribedDrug drug = new PrescribedDrug(m1, "2", "Daily", "10 days", "", "Dr. Robot");
            drug.calculateCost(); // 2 * 10 * 5.00 = 100.0
            rx.addPrescribedDrug(drug);
            
            dataService.savePrescription(rx);
            
            System.out.println("Prescription saved successfully! Total Bill: $" + rx.getTotalBill());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
