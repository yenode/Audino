package com.audino.util;

import com.audino.model.*;
import com.audino.service.DataService;
import com.audino.service.InteractionEngine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import com.audino.util.ConfigurationManager;

public class AppSeeder {

    public static void main(String[] args) {
        System.out.println("Starting Indian Healthcare Data Seeding...");
        
        try {
            ConfigurationManager.getInstance().initialize();
            DataService dataService = new DataService();
            dataService.loadAllData();
            
            System.out.println("Schema Initialized. Injecting normalized clinical data...");

            Method getConnectionMethod = DataService.class.getDeclaredMethod("getConnection");
            getConnectionMethod.setAccessible(true);
            Connection conn = (Connection) getConnectionMethod.invoke(dataService);

            // Seed Medications
            try (Statement st = conn.createStatement()) {
                st.execute("DELETE FROM prescription_alerts");
                st.execute("DELETE FROM prescribed_drugs");
                st.execute("DELETE FROM prescriptions");
                st.execute("DELETE FROM patient_allergies");
                st.execute("DELETE FROM patient_conditions");
                st.execute("DELETE FROM patients");
                st.execute("DELETE FROM medication_ingredients");
                st.execute("DELETE FROM medication_identifiers");
                st.execute("DELETE FROM medications");
                st.execute("DELETE FROM interaction_rules");
                
                String insertMed = "INSERT INTO medications (medication_id, generic_name, brand_name, rxnorm_code, medication_type, price_per_unit, version) VALUES (?, ?, ?, ?, ?, ?, 1)";
                try (PreparedStatement ps = conn.prepareStatement(insertMed)) {
                    // Paracetamol
                    ps.setString(1, "MED-IND-01"); ps.setString(2, "Paracetamol"); ps.setString(3, "Dolo 650"); ps.setString(4, "161"); ps.setString(5, "TABLET"); ps.setDouble(6, 2.50); ps.addBatch();
                    // Amoxicillin
                    ps.setString(1, "MED-IND-02"); ps.setString(2, "Amoxicillin"); ps.setString(3, "Mox 500"); ps.setString(4, "723"); ps.setString(5, "TABLET"); ps.setDouble(6, 8.00); ps.addBatch();
                    // Ibuprofen
                    ps.setString(1, "MED-IND-03"); ps.setString(2, "Ibuprofen"); ps.setString(3, "Brufen 400"); ps.setString(4, "5640"); ps.setString(5, "TABLET"); ps.setDouble(6, 3.50); ps.addBatch();
                    // Warfarin
                    ps.setString(1, "MED-IND-04"); ps.setString(2, "Warfarin"); ps.setString(3, "Coumadin"); ps.setString(4, "11289"); ps.setString(5, "TABLET"); ps.setDouble(6, 12.00); ps.addBatch();
                    // Azithromycin
                    ps.setString(1, "MED-IND-05"); ps.setString(2, "Azithromycin"); ps.setString(3, "Azee 500"); ps.setString(4, "15202"); ps.setString(5, "TABLET"); ps.setDouble(6, 22.00); ps.addBatch();
                    // Metformin
                    ps.setString(1, "MED-IND-06"); ps.setString(2, "Metformin"); ps.setString(3, "Glycomet 500"); ps.setString(4, "860975"); ps.setString(5, "TABLET"); ps.setDouble(6, 45.00); ps.addBatch();
                    // Atorvastatin
                    ps.setString(1, "MED-IND-07"); ps.setString(2, "Atorvastatin"); ps.setString(3, "Lipikind 10"); ps.setString(4, "259255"); ps.setString(5, "TABLET"); ps.setDouble(6, 85.50); ps.addBatch();
                    // Amlodipine
                    ps.setString(1, "MED-IND-08"); ps.setString(2, "Amlodipine"); ps.setString(3, "Amlokind 5"); ps.setString(4, "197361"); ps.setString(5, "TABLET"); ps.setDouble(6, 30.00); ps.addBatch();
                    // Omeprazole
                    ps.setString(1, "MED-IND-09"); ps.setString(2, "Omeprazole"); ps.setString(3, "Omez 20"); ps.setString(4, "199903"); ps.setString(5, "CAPSULE"); ps.setDouble(6, 55.00); ps.addBatch();
                    // Clopidogrel
                    ps.setString(1, "MED-IND-10"); ps.setString(2, "Clopidogrel"); ps.setString(3, "Plavix 75"); ps.setString(4, "32968"); ps.setString(5, "TABLET"); ps.setDouble(6, 110.00); ps.addBatch();
                    // Celecoxib
                    ps.setString(1, "MED-IND-11"); ps.setString(2, "Celecoxib"); ps.setString(3, "Celebrex 200"); ps.setString(4, "204320"); ps.setString(5, "CAPSULE"); ps.setDouble(6, 130.00); ps.addBatch();
                    
                    ps.executeBatch();
                }
                
                String insertRule = "INSERT INTO interaction_rules (rule_type, keyword1, keyword2, severity, description, recommendation) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertRule)) {
                    ps.setString(1, "DRUG_DRUG"); ps.setString(2, "warfarin"); ps.setString(3, "ibuprofen"); ps.setString(4, "CRITICAL"); ps.setString(5, "Ibuprofen may enhance the anticoagulant effect of Warfarin, increasing bleeding risk."); ps.setString(6, "Avoid concurrent use. Consider Paracetamol for pain."); ps.addBatch();
                    ps.setString(1, "DRUG_ALLERGY"); ps.setString(2, "penicillin"); ps.setString(3, "amoxicillin"); ps.setString(4, "CRITICAL"); ps.setString(5, "Patient is allergic to Penicillin. Amoxicillin is a penicillin-class antibiotic."); ps.setString(6, "Use alternative antibiotic like Azithromycin."); ps.addBatch();
                    ps.setString(1, "DRUG_CONDITION"); ps.setString(2, "peptic ulcer"); ps.setString(3, "ibuprofen"); ps.setString(4, "CRITICAL"); ps.setString(5, "NSAIDs like Ibuprofen can exacerbate peptic ulcers."); ps.setString(6, "Avoid NSAIDs. Use Paracetamol."); ps.addBatch();
                    ps.setString(1, "DRUG_DRUG"); ps.setString(2, "clopidogrel"); ps.setString(3, "omeprazole"); ps.setString(4, "CRITICAL"); ps.setString(5, "Omeprazole may reduce the antiplatelet effect of Clopidogrel."); ps.setString(6, "Consider using an alternative PPI like Pantoprazole."); ps.addBatch();
                    ps.setString(1, "DRUG_CONDITION"); ps.setString(2, "renal failure"); ps.setString(3, "metformin"); ps.setString(4, "CRITICAL"); ps.setString(5, "Metformin is contraindicated in severe renal impairment due to lactic acidosis risk."); ps.setString(6, "Use insulin or alternative oral hypoglycemic agent."); ps.addBatch();
                    ps.setString(1, "DRUG_ALLERGY"); ps.setString(2, "sulfa"); ps.setString(3, "celecoxib"); ps.setString(4, "WARNING"); ps.setString(5, "Patient has a known allergy to Sulfa Drugs. Celecoxib contains a sulfonamide moiety."); ps.setString(6, "Monitor closely for cross-reactivity or use alternative NSAID."); ps.addBatch();
                    
                    ps.executeBatch();
                }
            }

            Patient p1 = new Patient("Rajesh", "Kumar", LocalDate.of(1980, 5, 20));
            p1.setPatientId("PAT-IND-01"); p1.setGender("Male"); p1.setContactNumber("9876543210");
            p1.addAllergy("Penicillin"); p1.addChronicCondition("Hypertension"); dataService.savePatient(p1);

            Patient p2 = new Patient("Priya", "Sharma", LocalDate.of(1992, 8, 15));
            p2.setPatientId("PAT-IND-02"); p2.setGender("Female"); p2.setContactNumber("9123456789");
            p2.addChronicCondition("Peptic Ulcer"); dataService.savePatient(p2);

            Patient p3 = new Patient("Anil", "Desai", LocalDate.of(1965, 3, 10));
            p3.setPatientId("PAT-IND-03"); p3.setGender("Male"); p3.setContactNumber("9988776655");
            p3.addAllergy("Sulfa Drugs"); p3.addChronicCondition("Renal Failure"); dataService.savePatient(p3);

            Patient p4 = new Patient("Sunita", "Singh", LocalDate.of(1958, 11, 22));
            p4.setPatientId("PAT-IND-04"); p4.setGender("Female"); p4.setContactNumber("9876543211");
            p4.addChronicCondition("Heart Disease"); dataService.savePatient(p4);
            
            System.out.println("Generating 100 bulk medicines, 50 rules, and 100 bulk patients...");
            generateBulkData(conn, dataService);

            dataService.loadAllData();
            InteractionEngine engine = new InteractionEngine();
            
            System.out.println("Generating prescriptions to trigger demonstrations...");
            
            Medication amox = dataService.getAllMedications().stream().filter(m -> m.getMedicationId().equals("MED-IND-02")).findFirst().get();
            Prescription rx1 = new Prescription(p1, "Dr. Gupta");
            rx1.addPrescribedDrug(new PrescribedDrug(amox, "500mg", "Twice daily", "5 days", "After meals", "Dr. Gupta"));
            rx1.setAlerts(engine.checkAllInteractionsAsync(p1, rx1, dataService.getInteractionRules(), dataService.getAllMedications()).join());
            if(!rx1.getAlerts().isEmpty()) rx1.setStatus(PrescriptionStatus.CANCELLED);
            dataService.savePrescription(rx1);
            
            Medication ibup = dataService.getAllMedications().stream().filter(m -> m.getMedicationId().equals("MED-IND-03")).findFirst().get();
            Medication warf = dataService.getAllMedications().stream().filter(m -> m.getMedicationId().equals("MED-IND-04")).findFirst().get();
            Prescription rx2 = new Prescription(p2, "Dr. Reddy");
            rx2.addPrescribedDrug(new PrescribedDrug(ibup, "400mg", "SOS", "3 days", "For pain", "Dr. Reddy"));
            rx2.addPrescribedDrug(new PrescribedDrug(warf, "5mg", "Once daily", "30 days", "Take at night", "Dr. Reddy"));
            rx2.setAlerts(engine.checkAllInteractionsAsync(p2, rx2, dataService.getInteractionRules(), dataService.getAllMedications()).join());
            if(!rx2.getAlerts().isEmpty()) rx2.setStatus(PrescriptionStatus.CANCELLED);
            dataService.savePrescription(rx2);
            
            Medication metf = dataService.getAllMedications().stream().filter(m -> m.getMedicationId().equals("MED-IND-06")).findFirst().get();
            Medication cele = dataService.getAllMedications().stream().filter(m -> m.getMedicationId().equals("MED-IND-11")).findFirst().get();
            Prescription rx3 = new Prescription(p3, "Dr. Joshi");
            rx3.addPrescribedDrug(new PrescribedDrug(metf, "500mg", "Twice daily", "30 days", "With meals", "Dr. Joshi"));
            rx3.addPrescribedDrug(new PrescribedDrug(cele, "200mg", "Once daily", "10 days", "For joint pain", "Dr. Joshi"));
            rx3.setAlerts(engine.checkAllInteractionsAsync(p3, rx3, dataService.getInteractionRules(), dataService.getAllMedications()).join());
            if(!rx3.getAlerts().isEmpty()) rx3.setStatus(PrescriptionStatus.CANCELLED);
            dataService.savePrescription(rx3);

            Medication clop = dataService.getAllMedications().stream().filter(m -> m.getMedicationId().equals("MED-IND-10")).findFirst().get();
            Medication omep = dataService.getAllMedications().stream().filter(m -> m.getMedicationId().equals("MED-IND-09")).findFirst().get();
            Prescription rx4 = new Prescription(p4, "Dr. Khan");
            rx4.addPrescribedDrug(new PrescribedDrug(clop, "75mg", "Once daily", "30 days", "Morning", "Dr. Khan"));
            rx4.addPrescribedDrug(new PrescribedDrug(omep, "20mg", "Once daily", "30 days", "Before breakfast", "Dr. Khan"));
            rx4.setAlerts(engine.checkAllInteractionsAsync(p4, rx4, dataService.getInteractionRules(), dataService.getAllMedications()).join());
            if(!rx4.getAlerts().isEmpty()) rx4.setStatus(PrescriptionStatus.CANCELLED);
            dataService.savePrescription(rx4);

            System.out.println("Generating random prescriptions for all patients...");
            List<Medication> allMeds = dataService.getAllMedications();
            Random rand = new Random(42);
            for(Patient p : dataService.getAllPatients()) {
                if(dataService.getActivePrescriberionForPatient(p.getPatientId()) == null) {
                    Prescription rx = new Prescription(p, "Dr. Bulk");
                    int numDrugs = 1 + rand.nextInt(3);
                    for(int k=0; k<numDrugs; k++) {
                        Medication m = allMeds.get(rand.nextInt(allMeds.size()));
                        rx.addPrescribedDrug(new PrescribedDrug(m, "1 tablet", "Once daily", "10 days", "Take with water", "Dr. Bulk"));
                    }
                    rx.setAlerts(engine.checkAllInteractionsAsync(p, rx, dataService.getInteractionRules(), allMeds).join());
                    if(!rx.getAlerts().isEmpty()) {
                        rx.setStatus(PrescriptionStatus.CANCELLED);
                    } else {
                        rx.setStatus(PrescriptionStatus.APPROVED);
                    }
                    dataService.savePrescription(rx);
                }
            }

            // Verify PostgreSQL DBMS Data with a query!
            System.out.println("--------------------------------------------------");
            System.out.println("VERIFYING POSTGRESQL NATIVE TABLES: ");
            try (Statement st = conn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT count(*) FROM patients");
                if (rs.next()) System.out.println("Total Patients in DB: " + rs.getInt(1));
                
                rs = st.executeQuery("SELECT count(*) FROM medications");
                if (rs.next()) System.out.println("Total Medications in DB: " + rs.getInt(1));

                rs = st.executeQuery("SELECT count(*) FROM interaction_rules");
                if (rs.next()) System.out.println("Total Rules in DB: " + rs.getInt(1));
                
                rs = st.executeQuery("SELECT count(*) FROM prescriptions");
                if (rs.next()) System.out.println("Total Prescriptions in DB: " + rs.getInt(1));
                
                rs = st.executeQuery("SELECT count(*) FROM prescribed_drugs");
                if (rs.next()) System.out.println("Total Prescribed Drugs in DB: " + rs.getInt(1));
            }
            System.out.println("--------------------------------------------------");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void generateBulkData(Connection conn, DataService dataService) throws Exception {
        Random rand = new Random(1337);
        String[] firstNamesMale = {"Aarav", "Arjun", "Ramesh", "Rajesh", "Sanjay", "Rahul", "Vikram", "Amit", "Ravi", "Suresh", "Vijay", "Deepak", "Sunil", "Anil", "Manoj", "Pranav", "Karan", "Nikhil", "Gaurav", "Rohit"};
        String[] firstNamesFemale = {"Ananya", "Priya", "Sunita", "Lakshmi", "Sneha", "Kavita", "Meena", "Neha", "Pooja", "Aarti", "Rekha", "Kiran", "Anita", "Geeta", "Seema", "Diya", "Riya", "Nisha", "Swati", "Divya"};
        String[] lastNames = {"Kumar", "Sharma", "Singh", "Patel", "Reddy", "Desai", "Gupta", "Joshi", "Verma", "Iyer", "Nair", "Das", "Bose", "Chawla", "Malhotra", "Mehta", "Bhat", "Shah", "Agarwal", "Menon"};
        
        String[] medPrefixes = {"Cipro", "Amoxi", "Lisi", "Met", "Atorva", "Omepra", "Amlodi", "Clopido", "Celeco", "Azithro", "Panta", "Ofloc", "Doxy", "Loxa", "Leva", "Fexo", "Odan", "Olm"};
        String[] medSuffixes = {"floxacin", "cillin", "pril", "formin", "statin", "zole", "pine", "grel", "xib", "mycin", "prazole", "sartan", "tidine", "vir", "olol", "dine", "setron"};
        String[] brandPrefixes = {"Zeno", "Max", "Nova", "Pro", "Cardio", "Gastro", "Neuro", "Ortho", "Derma", "Vita", "Hepato", "Pulmo", "Renal", "Opti", "Bio", "Cure", "Heal", "Cura", "Pharma"};
        String[] brandSuffixes = {"cip", "mox", "pril", "met", "stat", "mez", "pine", "grel", "xib", "cin", "fort", "plus", "clear", "heal", "care", "fast", "relief", "tab", "med"};
        
        String[] conditions = {"Hypertension", "Diabetes", "Asthma", "Peptic Ulcer", "Renal Failure", "Heart Disease", "Arthritis", "Migraine", "Thyroid Disorder", "Glaucoma", "Anemia", "Osteoporosis"};
        String[] allergies = {"Penicillin", "Sulfa Drugs", "Aspirin", "Ibuprofen", "Tetracycline", "Codeine", "Latex", "Iodine", "Cephalosporins", "Anticonvulsants"};
    
        List<String> generatedGenerics = new ArrayList<>();
        String insertMed = "INSERT INTO medications (medication_id, generic_name, brand_name, rxnorm_code, medication_type, price_per_unit, version) VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(insertMed)) {
            for (int i = 0; i < 100; i++) {
                String generic = medPrefixes[rand.nextInt(medPrefixes.length)] + medSuffixes[rand.nextInt(medSuffixes.length)];
                String brand = brandPrefixes[rand.nextInt(brandPrefixes.length)] + brandSuffixes[rand.nextInt(brandSuffixes.length)];
                generatedGenerics.add(generic.toLowerCase());
                
                ps.setString(1, "MED-BULK-" + String.format("%03d", i));
                ps.setString(2, generic);
                ps.setString(3, brand);
                ps.setString(4, String.valueOf(100000 + rand.nextInt(900000)));
                ps.setString(5, rand.nextBoolean() ? "TABLET" : "CAPSULE");
                ps.setDouble(6, 10.0 + rand.nextInt(500) + (rand.nextInt(100) / 100.0));
                ps.addBatch();
            }
            ps.executeBatch();
        }
        
        String insertRule = "INSERT INTO interaction_rules (rule_type, keyword1, keyword2, severity, description, recommendation) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertRule)) {
            for (int i = 0; i < 50; i++) {
                String drug1 = generatedGenerics.get(rand.nextInt(generatedGenerics.size()));
                String drug2 = generatedGenerics.get(rand.nextInt(generatedGenerics.size()));
                while (drug1.equals(drug2)) {
                    drug2 = generatedGenerics.get(rand.nextInt(generatedGenerics.size()));
                }
                
                ps.setString(1, "DRUG_DRUG");
                ps.setString(2, drug1);
                ps.setString(3, drug2);
                ps.setString(4, rand.nextBoolean() ? "CRITICAL" : "WARNING");
                ps.setString(5, "Concurrent use of " + drug1 + " and " + drug2 + " may result in adverse clinical effects.");
                ps.setString(6, "Monitor patient closely or consider alternative therapy.");
                ps.addBatch();
            }
            ps.executeBatch();
        }
        
        for (int i = 0; i < 100; i++) {
            boolean isMale = rand.nextBoolean();
            String firstName = isMale ? firstNamesMale[rand.nextInt(firstNamesMale.length)] : firstNamesFemale[rand.nextInt(firstNamesFemale.length)];
            String lastName = lastNames[rand.nextInt(lastNames.length)];
            
            int year = 1940 + rand.nextInt(60);
            int month = 1 + rand.nextInt(12);
            int day = 1 + rand.nextInt(28);
            
            Patient p = new Patient(firstName, lastName, LocalDate.of(year, month, day));
            p.setPatientId("PAT-BULK-" + String.format("%03d", i));
            p.setGender(isMale ? "Male" : "Female");
            p.setContactNumber("9" + (100000000 + rand.nextInt(900000000)));
            
            if (rand.nextInt(100) < 30) {
                p.addAllergy(allergies[rand.nextInt(allergies.length)]);
            }
            if (rand.nextInt(100) < 40) {
                p.addChronicCondition(conditions[rand.nextInt(conditions.length)]);
            }
            dataService.savePatient(p);
        }
    }
}
