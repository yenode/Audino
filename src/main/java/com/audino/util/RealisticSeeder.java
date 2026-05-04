package com.audino.util;

import com.audino.model.*;
import com.audino.service.DataService;
import java.sql.*;
import java.util.List;

public class RealisticSeeder {
    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println("  Audino Realistic Data Seeder");
        System.out.println("=================================================\n");

        ConfigurationManager.getInstance().initialize();
        DataService ds = new DataService();
        ds.loadAllData();
        User admin = ds.authenticate("admin", "1234567890");
        SessionManager.getInstance().login(admin);

        try (Connection conn = ConfigurationManager.getInstance().getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            // Clear
            for (String t : new String[]{"prescribed_drugs","prescriptions",
                    "patient_allergies","patient_conditions","patients","medications",
                    "interaction_rules"}) {
                conn.createStatement().execute("DELETE FROM " + t);
            }
            conn.commit();
            System.out.println("Cleared existing data.");

            // MEDICATIONS
            PreparedStatement pm = conn.prepareStatement("INSERT INTO medications (medication_id,generic_name,brand_name,rxnorm_code,medication_type,strength,price_per_unit,version) VALUES (?,?,?,?,?,?,?,1) ON CONFLICT DO NOTHING");
            pm.setString(1,"MED-0001"); pm.setString(2,"Amoxicillin"); pm.setString(3,"Amoxil"); pm.setString(4,"723"); pm.setString(5,"TABLET"); pm.setString(6,"500mg"); pm.setDouble(7,5.0); pm.addBatch();
            pm.setString(1,"MED-0002"); pm.setString(2,"Lisinopril"); pm.setString(3,"Prinivil"); pm.setString(4,"29046"); pm.setString(5,"TABLET"); pm.setString(6,"10mg"); pm.setDouble(7,3.5); pm.addBatch();
            pm.setString(1,"MED-0003"); pm.setString(2,"Atorvastatin"); pm.setString(3,"Lipitor"); pm.setString(4,"83367"); pm.setString(5,"TABLET"); pm.setString(6,"20mg"); pm.setDouble(7,8.75); pm.addBatch();
            pm.setString(1,"MED-0004"); pm.setString(2,"Metformin"); pm.setString(3,"Glycomet"); pm.setString(4,"6809"); pm.setString(5,"TABLET"); pm.setString(6,"500mg"); pm.setDouble(7,2.25); pm.addBatch();
            pm.setString(1,"MED-0005"); pm.setString(2,"Omeprazole"); pm.setString(3,"Omez"); pm.setString(4,"7646"); pm.setString(5,"TABLET"); pm.setString(6,"20mg"); pm.setDouble(7,4.0); pm.addBatch();
            pm.setString(1,"MED-0006"); pm.setString(2,"Salbutamol"); pm.setString(3,"Asthalin"); pm.setString(4,"435"); pm.setString(5,"TABLET"); pm.setString(6,"4mg"); pm.setDouble(7,12.5); pm.addBatch();
            pm.setString(1,"MED-0007"); pm.setString(2,"Azithromycin"); pm.setString(3,"Zithromax"); pm.setString(4,"308460"); pm.setString(5,"TABLET"); pm.setString(6,"250mg"); pm.setDouble(7,9.0); pm.addBatch();
            pm.setString(1,"MED-0008"); pm.setString(2,"Paracetamol"); pm.setString(3,"Dolo-650"); pm.setString(4,"161"); pm.setString(5,"TABLET"); pm.setString(6,"650mg"); pm.setDouble(7,1.5); pm.addBatch();
            pm.setString(1,"MED-0009"); pm.setString(2,"Ibuprofen"); pm.setString(3,"Brufen"); pm.setString(4,"5640"); pm.setString(5,"TABLET"); pm.setString(6,"400mg"); pm.setDouble(7,2.0); pm.addBatch();
            pm.setString(1,"MED-0010"); pm.setString(2,"Insulin Glargine"); pm.setString(3,"Lantus"); pm.setString(4,"274783"); pm.setString(5,"INJECTION"); pm.setString(6,"100U/ml"); pm.setDouble(7,45.0); pm.addBatch();
            pm.setString(1,"MED-0011"); pm.setString(2,"Cetirizine"); pm.setString(3,"Cetzine"); pm.setString(4,"2100"); pm.setString(5,"TABLET"); pm.setString(6,"10mg"); pm.setDouble(7,3.25); pm.addBatch();
            pm.setString(1,"MED-0012"); pm.setString(2,"Amlodipine"); pm.setString(3,"Norvasc"); pm.setString(4,"17767"); pm.setString(5,"TABLET"); pm.setString(6,"5mg"); pm.setDouble(7,4.5); pm.addBatch();
            pm.setString(1,"MED-0013"); pm.setString(2,"Sertraline"); pm.setString(3,"Zoloft"); pm.setString(4,"36437"); pm.setString(5,"TABLET"); pm.setString(6,"50mg"); pm.setDouble(7,6.75); pm.addBatch();
            pm.setString(1,"MED-0014"); pm.setString(2,"Levothyroxine"); pm.setString(3,"Thyronorm"); pm.setString(4,"10582"); pm.setString(5,"TABLET"); pm.setString(6,"50mcg"); pm.setDouble(7,5.5); pm.addBatch();
            pm.setString(1,"MED-0015"); pm.setString(2,"Amoxicillin Susp"); pm.setString(3,"Amoxil Syrup"); pm.setString(4,"723"); pm.setString(5,"LIQUID"); pm.setString(6,"125mg/5ml"); pm.setDouble(7,7.0); pm.addBatch();
            pm.setString(1,"MED-0016"); pm.setString(2,"Ciprofloxacin"); pm.setString(3,"Ciplox"); pm.setString(4,"2551"); pm.setString(5,"TABLET"); pm.setString(6,"500mg"); pm.setDouble(7,6.0); pm.addBatch();
            pm.setString(1,"MED-0017"); pm.setString(2,"Metronidazole"); pm.setString(3,"Flagyl"); pm.setString(4,"6922"); pm.setString(5,"TABLET"); pm.setString(6,"400mg"); pm.setDouble(7,3.0); pm.addBatch();
            pm.setString(1,"MED-0018"); pm.setString(2,"Ranitidine"); pm.setString(3,"Zantac"); pm.setString(4,"9054"); pm.setString(5,"TABLET"); pm.setString(6,"150mg"); pm.setDouble(7,2.5); pm.addBatch();
            pm.setString(1,"MED-0019"); pm.setString(2,"Losartan"); pm.setString(3,"Cozaar"); pm.setString(4,"203160"); pm.setString(5,"TABLET"); pm.setString(6,"50mg"); pm.setDouble(7,5.0); pm.addBatch();
            pm.setString(1,"MED-0020"); pm.setString(2,"Pantoprazole"); pm.setString(3,"Pan-D"); pm.setString(4,"7833"); pm.setString(5,"TABLET"); pm.setString(6,"40mg"); pm.setDouble(7,4.5); pm.addBatch();
            pm.setString(1,"MED-0021"); pm.setString(2,"Clopidogrel"); pm.setString(3,"Plavix"); pm.setString(4,"174742"); pm.setString(5,"TABLET"); pm.setString(6,"75mg"); pm.setDouble(7,11.0); pm.addBatch();
            pm.setString(1,"MED-0022"); pm.setString(2,"Warfarin"); pm.setString(3,"Warf"); pm.setString(4,"11289"); pm.setString(5,"TABLET"); pm.setString(6,"5mg"); pm.setDouble(7,4.0); pm.addBatch();
            pm.setString(1,"MED-0023"); pm.setString(2,"Prednisolone"); pm.setString(3,"Wysolone"); pm.setString(4,"8638"); pm.setString(5,"TABLET"); pm.setString(6,"10mg"); pm.setDouble(7,3.75); pm.addBatch();
            pm.setString(1,"MED-0024"); pm.setString(2,"Doxycycline"); pm.setString(3,"Doxy-1"); pm.setString(4,"3640"); pm.setString(5,"TABLET"); pm.setString(6,"100mg"); pm.setDouble(7,5.25); pm.addBatch();
            pm.setString(1,"MED-0025"); pm.setString(2,"Glimepiride"); pm.setString(3,"Amaryl"); pm.setString(4,"25789"); pm.setString(5,"TABLET"); pm.setString(6,"2mg"); pm.setDouble(7,6.5); pm.addBatch();
            pm.setString(1,"MED-0026"); pm.setString(2,"Aspirin"); pm.setString(3,"Ecosprin"); pm.setString(4,"1191"); pm.setString(5,"TABLET"); pm.setString(6,"75mg"); pm.setDouble(7,1.75); pm.addBatch();
            pm.setString(1,"MED-0027"); pm.setString(2,"Atenolol"); pm.setString(3,"Tenormin"); pm.setString(4,"10600"); pm.setString(5,"TABLET"); pm.setString(6,"50mg"); pm.setDouble(7,4.25); pm.addBatch();
            pm.setString(1,"MED-0028"); pm.setString(2,"Furosemide"); pm.setString(3,"Lasix"); pm.setString(4,"4603"); pm.setString(5,"TABLET"); pm.setString(6,"40mg"); pm.setDouble(7,3.0); pm.addBatch();
            pm.setString(1,"MED-0029"); pm.setString(2,"Spironolactone"); pm.setString(3,"Aldactone"); pm.setString(4,"9997"); pm.setString(5,"TABLET"); pm.setString(6,"25mg"); pm.setDouble(7,5.75); pm.addBatch();
            pm.setString(1,"MED-0030"); pm.setString(2,"Montelukast"); pm.setString(3,"Montair"); pm.setString(4,"41493"); pm.setString(5,"TABLET"); pm.setString(6,"10mg"); pm.setDouble(7,8.0); pm.addBatch();
            pm.setString(1,"MED-0031"); pm.setString(2,"Vitamin D3"); pm.setString(3,"Calcirol"); pm.setString(4,"42878"); pm.setString(5,"TABLET"); pm.setString(6,"60000IU"); pm.setDouble(7,12.0); pm.addBatch();
            pm.setString(1,"MED-0032"); pm.setString(2,"Rabeprazole"); pm.setString(3,"Razo"); pm.setString(4,"58964"); pm.setString(5,"TABLET"); pm.setString(6,"20mg"); pm.setDouble(7,5.0); pm.addBatch();
            pm.setString(1,"MED-0033"); pm.setString(2,"Insulin Regular"); pm.setString(3,"Actrapid"); pm.setString(4,"86009"); pm.setString(5,"INJECTION"); pm.setString(6,"40U/ml"); pm.setDouble(7,38.0); pm.addBatch();
            pm.setString(1,"MED-0034"); pm.setString(2,"Ondansetron"); pm.setString(3,"Emeset"); pm.setString(4,"26225"); pm.setString(5,"TABLET"); pm.setString(6,"4mg"); pm.setDouble(7,4.75); pm.addBatch();
            pm.setString(1,"MED-0035"); pm.setString(2,"Telmisartan"); pm.setString(3,"Telma"); pm.setString(4,"73494"); pm.setString(5,"TABLET"); pm.setString(6,"40mg"); pm.setDouble(7,6.25); pm.addBatch();
            pm.executeBatch(); conn.commit();
            System.out.println("\u2713 35 medications seeded.");
            // PATIENTS
            PreparedStatement pp = conn.prepareStatement("INSERT INTO patients (patient_id,first_name,last_name,date_of_birth,gender,contact_number,version) VALUES (?,?,?,?,?,?,1) ON CONFLICT DO NOTHING");
            PreparedStatement pa = conn.prepareStatement("INSERT INTO patient_allergies (patient_id,allergy_name) VALUES (?,?) ON CONFLICT DO NOTHING");
            PreparedStatement pc = conn.prepareStatement("INSERT INTO patient_conditions (patient_id,condition_name) VALUES (?,?) ON CONFLICT DO NOTHING");
            pp.setString(1,"PAT-0001"); pp.setString(2,"Aditya"); pp.setString(3,"Pachauri"); pp.setString(4,"1985-03-14"); pp.setString(5,"Male"); pp.setString(6,"9876543210"); pp.addBatch();
            pa.setString(1,"PAT-0001"); pa.setString(2,"Penicillin"); pa.addBatch();
            pc.setString(1,"PAT-0001"); pc.setString(2,"Hypertension"); pc.addBatch();
            pc.setString(1,"PAT-0001"); pc.setString(2,"Chronic Kidney Disease"); pc.addBatch();
            pp.setString(1,"PAT-0002"); pp.setString(2,"Mridankan"); pp.setString(3,"Mandal"); pp.setString(4,"1972-07-22"); pp.setString(5,"Male"); pp.setString(6,"9876543211"); pp.addBatch();
            pa.setString(1,"PAT-0002"); pa.setString(2,"Sulfa Drugs"); pa.addBatch();
            pa.setString(1,"PAT-0002"); pa.setString(2,"Aspirin"); pa.addBatch();
            pc.setString(1,"PAT-0002"); pc.setString(2,"Type 2 Diabetes"); pc.addBatch();
            pc.setString(1,"PAT-0002"); pc.setString(2,"Hyperlipidemia"); pc.addBatch();
            pp.setString(1,"PAT-0003"); pp.setString(2,"Aditya"); pp.setString(3,"Bansod"); pp.setString(4,"1990-11-05"); pp.setString(5,"Male"); pp.setString(6,"9876543212"); pp.addBatch();
            pc.setString(1,"PAT-0003"); pc.setString(2,"Asthma"); pc.addBatch();
            pp.setString(1,"PAT-0004"); pp.setString(2,"Dhannu"); pp.setString(3,"Meena"); pp.setString(4,"1968-01-30"); pp.setString(5,"Female"); pp.setString(6,"9876543213"); pp.addBatch();
            pa.setString(1,"PAT-0004"); pa.setString(2,"Latex"); pa.addBatch();
            pa.setString(1,"PAT-0004"); pa.setString(2,"Iodine"); pa.addBatch();
            pc.setString(1,"PAT-0004"); pc.setString(2,"Hypothyroidism"); pc.addBatch();
            pc.setString(1,"PAT-0004"); pc.setString(2,"Hypertension"); pc.addBatch();
            pp.setString(1,"PAT-0005"); pp.setString(2,"Ankit"); pp.setString(3,"Ekka"); pp.setString(4,"1955-09-18"); pp.setString(5,"Male"); pp.setString(6,"9876543214"); pp.addBatch();
            pa.setString(1,"PAT-0005"); pa.setString(2,"Penicillin"); pa.addBatch();
            pa.setString(1,"PAT-0005"); pa.setString(2,"NSAIDs"); pa.addBatch();
            pc.setString(1,"PAT-0005"); pc.setString(2,"COPD"); pc.addBatch();
            pc.setString(1,"PAT-0005"); pc.setString(2,"Type 2 Diabetes"); pc.addBatch();
            pc.setString(1,"PAT-0005"); pc.setString(2,"Hypertension"); pc.addBatch();
            pp.setString(1,"PAT-0006"); pp.setString(2,"Sourabh"); pp.setString(3,"Rawat"); pp.setString(4,"1992-04-12"); pp.setString(5,"Male"); pp.setString(6,"9876543215"); pp.addBatch();
            pc.setString(1,"PAT-0006"); pc.setString(2,"Anxiety Disorder"); pc.addBatch();
            pc.setString(1,"PAT-0006"); pc.setString(2,"Seasonal Allergies"); pc.addBatch();
            pp.setString(1,"PAT-0007"); pp.setString(2,"Sayan"); pp.setString(3,"Samajpati"); pp.setString(4,"1948-12-03"); pp.setString(5,"Male"); pp.setString(6,"9876543216"); pp.addBatch();
            pa.setString(1,"PAT-0007"); pa.setString(2,"Amoxicillin"); pa.addBatch();
            pa.setString(1,"PAT-0007"); pa.setString(2,"Codeine"); pa.addBatch();
            pc.setString(1,"PAT-0007"); pc.setString(2,"Coronary Artery Disease"); pc.addBatch();
            pc.setString(1,"PAT-0007"); pc.setString(2,"Atrial Fibrillation"); pc.addBatch();
            pc.setString(1,"PAT-0007"); pc.setString(2,"Type 2 Diabetes"); pc.addBatch();
            pp.setString(1,"PAT-0008"); pp.setString(2,"Priya"); pp.setString(3,"Sharma"); pp.setString(4,"1980-06-25"); pp.setString(5,"Female"); pp.setString(6,"9876543217"); pp.addBatch();
            pc.setString(1,"PAT-0008"); pc.setString(2,"Hyperlipidemia"); pc.addBatch();
            pp.setString(1,"PAT-0009"); pp.setString(2,"Kavitha"); pp.setString(3,"Reddy"); pp.setString(4,"1975-02-08"); pp.setString(5,"Female"); pp.setString(6,"9876543218"); pp.addBatch();
            pa.setString(1,"PAT-0009"); pa.setString(2,"Aspirin"); pa.addBatch();
            pc.setString(1,"PAT-0009"); pc.setString(2,"GERD"); pc.addBatch();
            pc.setString(1,"PAT-0009"); pc.setString(2,"Iron Deficiency Anaemia"); pc.addBatch();
            pp.setString(1,"PAT-0010"); pp.setString(2,"Ramesh"); pp.setString(3,"Gupta"); pp.setString(4,"1962-10-15"); pp.setString(5,"Male"); pp.setString(6,"9876543219"); pp.addBatch();
            pa.setString(1,"PAT-0010"); pa.setString(2,"Penicillin"); pa.addBatch();
            pc.setString(1,"PAT-0010"); pc.setString(2,"Hypertension"); pc.addBatch();
            pc.setString(1,"PAT-0010"); pc.setString(2,"Benign Prostatic Hyperplasia"); pc.addBatch();
            pp.setString(1,"PAT-0011"); pp.setString(2,"Sunita"); pp.setString(3,"Yadav"); pp.setString(4,"1988-08-20"); pp.setString(5,"Female"); pp.setString(6,"9876543220"); pp.addBatch();
            pc.setString(1,"PAT-0011"); pc.setString(2,"Depression"); pc.addBatch();
            pc.setString(1,"PAT-0011"); pc.setString(2,"Migraine"); pc.addBatch();
            pp.setString(1,"PAT-0012"); pp.setString(2,"Vikram"); pp.setString(3,"Nair"); pp.setString(4,"1945-05-11"); pp.setString(5,"Male"); pp.setString(6,"9876543221"); pp.addBatch();
            pa.setString(1,"PAT-0012"); pa.setString(2,"Warfarin"); pa.addBatch();
            pa.setString(1,"PAT-0012"); pa.setString(2,"NSAIDs"); pa.addBatch();
            pc.setString(1,"PAT-0012"); pc.setString(2,"Heart Failure"); pc.addBatch();
            pc.setString(1,"PAT-0012"); pc.setString(2,"Atrial Fibrillation"); pc.addBatch();
            pc.setString(1,"PAT-0012"); pc.setString(2,"Hypertension"); pc.addBatch();
            pp.setString(1,"PAT-0013"); pp.setString(2,"Neha"); pp.setString(3,"Joshi"); pp.setString(4,"1995-12-01"); pp.setString(5,"Female"); pp.setString(6,"9876543222"); pp.addBatch();
            pc.setString(1,"PAT-0013"); pc.setString(2,"Acne Vulgaris"); pc.addBatch();
            pc.setString(1,"PAT-0013"); pc.setString(2,"Seasonal Allergies"); pc.addBatch();
            pp.setString(1,"PAT-0014"); pp.setString(2,"Manoj"); pp.setString(3,"Tiwari"); pp.setString(4,"1970-03-28"); pp.setString(5,"Male"); pp.setString(6,"9876543223"); pp.addBatch();
            pa.setString(1,"PAT-0014"); pa.setString(2,"Sulfa Drugs"); pa.addBatch();
            pc.setString(1,"PAT-0014"); pc.setString(2,"Type 2 Diabetes"); pc.addBatch();
            pc.setString(1,"PAT-0014"); pc.setString(2,"Peripheral Neuropathy"); pc.addBatch();
            pp.setString(1,"PAT-0015"); pp.setString(2,"Anjali"); pp.setString(3,"Pillai"); pp.setString(4,"1983-09-14"); pp.setString(5,"Female"); pp.setString(6,"9876543224"); pp.addBatch();
            pa.setString(1,"PAT-0015"); pa.setString(2,"Codeine"); pa.addBatch();
            pc.setString(1,"PAT-0015"); pc.setString(2,"Polycystic Ovary Syndrome"); pc.addBatch();
            pc.setString(1,"PAT-0015"); pc.setString(2,"Hypothyroidism"); pc.addBatch();
            pp.executeBatch(); pa.executeBatch(); pc.executeBatch(); conn.commit();
            System.out.println("\u2713 15 patients seeded.");
            // Interaction rules (PreparedStatement)
            try (java.sql.PreparedStatement pir = conn.prepareStatement(
                    "INSERT INTO interaction_rules (id, rules_json) VALUES (1, ?) ON CONFLICT (id) DO UPDATE SET rules_json = EXCLUDED.rules_json")) {
                pir.setString(1, "{\"drugDrugInteractions\":{\"rule1\":{\"drug1\":[\"Warfarin\"],\"drug2\":[\"Aspirin\"],\"severity\":\"CRITICAL\",\"description\":\"Increased bleeding risk.\",\"recommendation\":\"Monitor INR.\"},\"rule2\":{\"drug1\":[\"Warfarin\",\"Warf\"],\"drug2\":[\"Ibuprofen\",\"Brufen\",\"NSAIDs\"],\"severity\":\"CRITICAL\",\"description\":\"NSAIDs potentiate anticoagulant effect.\",\"recommendation\":\"Avoid combination.\"},\"rule3\":{\"drug1\":[\"Metformin\",\"Glycomet\"],\"drug2\":[\"Prednisolone\",\"Wysolone\"],\"severity\":\"WARNING\",\"description\":\"Steroids antagonize metformin.\",\"recommendation\":\"Monitor blood glucose.\"}},\"drugConditionInteractions\":{\"rule1\":{\"conditionKeywords\":[\"heart failure\"],\"medicationClasses\":[\"Ibuprofen\",\"Brufen\",\"NSAIDs\"],\"severity\":\"CRITICAL\",\"description\":\"NSAIDs cause sodium retention and worsen heart failure.\",\"recommendation\":\"Use alternative analgesic.\"},\"rule2\":{\"conditionKeywords\":[\"asthma\",\"bronchospasm\"],\"medicationClasses\":[\"Atenolol\",\"Tenormin\",\"Beta Blockers\"],\"severity\":\"CRITICAL\",\"description\":\"Beta blockers trigger bronchospasm.\",\"recommendation\":\"Use cardio-selective beta blocker with caution.\"},\"rule3\":{\"conditionKeywords\":[\"chronic kidney disease\",\"ckd\"],\"medicationClasses\":[\"Metformin\",\"Glycomet\"],\"severity\":\"CRITICAL\",\"description\":\"Lactic acidosis risk. Contraindicated if eGFR below 30.\",\"recommendation\":\"Assess renal function.\"},\"rule4\":{\"conditionKeywords\":[\"bipolar\"],\"medicationClasses\":[\"Sertraline\",\"Zoloft\"],\"severity\":\"CRITICAL\",\"description\":\"May precipitate mania.\",\"recommendation\":\"Use mood stabilizer.\"}},\"drugAllergyInteractions\":{\"rule1\":{\"allergyKeywords\":[\"penicillin\"],\"medicationClasses\":[\"Amoxicillin\",\"Amoxil\",\"Amoxicillin Susp\",\"Amoxil Syrup\",\"Penicillin\"],\"severity\":\"CRITICAL\",\"description\":\"Amoxicillin is a penicillin.\",\"recommendation\":\"Use macrolide or cephalosporin.\"},\"rule2\":{\"allergyKeywords\":[\"sulfa\"],\"medicationClasses\":[\"Ciprofloxacin\",\"Ciplox\"],\"severity\":\"WARNING\",\"description\":\"Low cross-reactivity.\",\"recommendation\":\"Monitor.\"},\"rule3\":{\"allergyKeywords\":[\"aspirin\"],\"medicationClasses\":[\"Aspirin\",\"Ecosprin\"],\"severity\":\"CRITICAL\",\"description\":\"Patient is allergic to Aspirin.\",\"recommendation\":\"Use Paracetamol.\"},\"rule4\":{\"allergyKeywords\":[\"nsaids\",\"nsaid\",\"ibuprofen\"],\"medicationClasses\":[\"Ibuprofen\",\"Brufen\",\"Aspirin\",\"Ecosprin\",\"NSAIDs\"],\"severity\":\"CRITICAL\",\"description\":\"Patient is allergic to NSAIDs.\",\"recommendation\":\"Use Paracetamol.\"}}}");
                pir.executeUpdate();
            }
            conn.commit();
            System.out.println("\u2713 Interaction rules seeded.");
            conn.commit();
            System.out.println("\u2713 Interaction rules seeded (drug-drug, drug-condition, drug-allergy).");
            // PRESCRIPTIONS (with Interactions)
            PreparedStatement prx = conn.prepareStatement("INSERT INTO prescriptions (prescription_id,patient_id,created_at,prescribed_by,status,total_bill,alerts_json,version) VALUES (?,?,?,?,?,?,?,1) ON CONFLICT DO NOTHING");
            PreparedStatement prd = conn.prepareStatement("INSERT INTO prescribed_drugs (prescription_id,medication_id,dosage,frequency,duration,special_instructions,prescribed_by,total_cost) VALUES (?,?,?,?,?,?,?,?)");

            prx.setString(1,"RX-0001"); prx.setString(2,"PAT-0001"); prx.setString(3,"2026-04-01T09:00"); prx.setString(4,"Dr. Rajan Verma"); prx.setString(5,"FLAGGED"); prx.setDouble(6,350.0); prx.setString(7,"[{\"alertLevel\": \"CRITICAL\", \"alertType\": \"DRUG_ALLERGY\", \"title\": \"Potential Allergic Reaction\", \"message\": \"Patient has a known allergy to 'Penicillin'. Amoxicillin is a penicillin.\", \"recommendation\": \"Use macrolide or cephalosporin.\", \"involvedMedications\": \"Amoxicillin\", \"patientFactor\": \"Penicillin\"}, {\"alertLevel\": \"CRITICAL\", \"alertType\": \"DRUG_CONDITION\", \"title\": \"Drug-Condition Contraindication\", \"message\": \"Prescribing Metformin is potentially unsafe for patients with 'Chronic Kidney Disease'. Lactic acidosis risk.\", \"recommendation\": \"Assess renal function.\", \"involvedMedications\": \"Metformin\", \"patientFactor\": \"Chronic Kidney Disease\"}]"); prx.addBatch();
            prd.setString(1,"RX-0001"); prd.setString(2,"MED-0001"); prd.setString(3,"500mg"); prd.setString(4,"Twice daily"); prd.setString(5,"7 days"); prd.setString(6,"Finish course"); prd.setString(7,"Dr. Rajan Verma"); prd.setDouble(8,35.0); prd.addBatch();
            prd.setString(1,"RX-0001"); prd.setString(2,"MED-0004"); prd.setString(3,"500mg"); prd.setString(4,"Twice daily"); prd.setString(5,"30 days"); prd.setString(6,"With meals"); prd.setString(7,"Dr. Rajan Verma"); prd.setDouble(8,67.5); prd.addBatch();
            prx.setString(1,"RX-0002"); prx.setString(2,"PAT-0002"); prx.setString(3,"2026-04-02T10:15"); prx.setString(4,"Dr. Meena Iyer"); prx.setString(5,"REJECTED"); prx.setDouble(6,310.0); prx.setString(7,"[{\"alertLevel\": \"WARNING\", \"alertType\": \"DRUG_ALLERGY\", \"title\": \"Potential Allergic Reaction\", \"message\": \"Patient has a known allergy to 'Sulfa Drugs'. Low cross-reactivity with Ciprofloxacin.\", \"recommendation\": \"Monitor.\", \"involvedMedications\": \"Ciprofloxacin\", \"patientFactor\": \"Sulfa Drugs\"}, {\"alertLevel\": \"CRITICAL\", \"alertType\": \"DRUG_ALLERGY\", \"title\": \"Potential Allergic Reaction\", \"message\": \"Patient has a known allergy to 'Aspirin'. Aspirin is contraindicated.\", \"recommendation\": \"Use Paracetamol.\", \"involvedMedications\": \"Aspirin\", \"patientFactor\": \"Aspirin\"}]"); prx.addBatch();
            prd.setString(1,"RX-0002"); prd.setString(2,"MED-0016"); prd.setString(3,"500mg"); prd.setString(4,"Twice daily"); prd.setString(5,"5 days"); prd.setString(6,"After food"); prd.setString(7,"Dr. Meena Iyer"); prd.setDouble(8,60.0); prd.addBatch();
            prd.setString(1,"RX-0002"); prd.setString(2,"MED-0026"); prd.setString(3,"75mg"); prd.setString(4,"Once daily"); prd.setString(5,"30 days"); prd.setString(6,"After meals"); prd.setString(7,"Dr. Meena Iyer"); prd.setDouble(8,52.5); prd.addBatch();
            prx.setString(1,"RX-0003"); prx.setString(2,"PAT-0003"); prx.setString(3,"2026-04-03T11:30"); prx.setString(4,"Dr. Ramesh Nair"); prx.setString(5,"FLAGGED"); prx.setDouble(6,127.5); prx.setString(7,"[{\"alertLevel\": \"CRITICAL\", \"alertType\": \"DRUG_CONDITION\", \"title\": \"Drug-Condition Contraindication\", \"message\": \"Prescribing Atenolol is potentially unsafe for patients with 'Asthma'. Beta blockers trigger bronchospasm.\", \"recommendation\": \"Use cardio-selective beta blocker with caution.\", \"involvedMedications\": \"Atenolol\", \"patientFactor\": \"Asthma\"}]"); prx.addBatch();
            prd.setString(1,"RX-0003"); prd.setString(2,"MED-0027"); prd.setString(3,"50mg"); prd.setString(4,"Once daily"); prd.setString(5,"30 days"); prd.setString(6,"Morning"); prd.setString(7,"Dr. Ramesh Nair"); prd.setDouble(8,127.5); prd.addBatch();
            prx.setString(1,"RX-0004"); prx.setString(2,"PAT-0004"); prx.setString(3,"2026-04-04T12:00"); prx.setString(4,"Dr. Sameer Patel"); prx.setString(5,"APPROVED"); prx.setDouble(6,24.0); prx.setString(7,"[]"); prx.addBatch();
            prd.setString(1,"RX-0004"); prd.setString(2,"MED-0008"); prd.setString(3,"650mg"); prd.setString(4,"SOS"); prd.setString(5,"3 days"); prd.setString(6,"For fever"); prd.setString(7,"Dr. Sameer Patel"); prd.setDouble(8,24.0); prd.addBatch();
            prx.setString(1,"RX-0005"); prx.setString(2,"PAT-0005"); prx.setString(3,"2026-04-05T14:00"); prx.setString(4,"Dr. Rajan Verma"); prx.setString(5,"APPROVED"); prx.setDouble(6,135.0); prx.setString(7,"[]"); prx.addBatch();
            prd.setString(1,"RX-0005"); prd.setString(2,"MED-0012"); prd.setString(3,"5mg"); prd.setString(4,"Once daily"); prd.setString(5,"30 days"); prd.setString(6,"Morning"); prd.setString(7,"Dr. Rajan Verma"); prd.setDouble(8,135.0); prd.addBatch();
            prx.setString(1,"RX-0006"); prx.setString(2,"PAT-0006"); prx.setString(3,"2026-04-06T15:15"); prx.setString(4,"Dr. Meena Iyer"); prx.setString(5,"REJECTED"); prx.setDouble(6,180.0); prx.setString(7,"[{\"alertLevel\": \"CRITICAL\", \"alertType\": \"DRUG_CONDITION\", \"title\": \"Drug-Condition Contraindication\", \"message\": \"Prescribing Ibuprofen is potentially unsafe for patients with 'Heart Failure'. NSAIDs cause sodium retention.\", \"recommendation\": \"Use alternative analgesic.\", \"involvedMedications\": \"Ibuprofen\", \"patientFactor\": \"Heart Failure\"}, {\"alertLevel\": \"CRITICAL\", \"alertType\": \"DRUG_DRUG\", \"title\": \"Drug-Drug Interaction\", \"message\": \"Ibuprofen and Warfarin may interact. NSAIDs potentiate anticoagulant effect.\", \"recommendation\": \"Avoid combination.\", \"involvedMedications\": \"Ibuprofen & Warfarin\"}]"); prx.addBatch();
            prd.setString(1,"RX-0006"); prd.setString(2,"MED-0009"); prd.setString(3,"400mg"); prd.setString(4,"SOS"); prd.setString(5,"5 days"); prd.setString(6,"After food"); prd.setString(7,"Dr. Meena Iyer"); prd.setDouble(8,60.0); prd.addBatch();
            prd.setString(1,"RX-0006"); prd.setString(2,"MED-0022"); prd.setString(3,"5mg"); prd.setString(4,"Once daily"); prd.setString(5,"30 days"); prd.setString(6,"Take with water"); prd.setString(7,"Dr. Meena Iyer"); prd.setDouble(8,120.0); prd.addBatch();
            prx.setString(1,"RX-0007"); prx.setString(2,"PAT-0007"); prx.setString(3,"2026-04-07T09:00"); prx.setString(4,"Dr. Ramesh Nair"); prx.setString(5,"FLAGGED"); prx.setDouble(6,202.5); prx.setString(7,"[{\"alertLevel\": \"CRITICAL\", \"alertType\": \"DRUG_CONDITION\", \"title\": \"Drug-Condition Contraindication\", \"message\": \"Prescribing Sertraline is potentially unsafe for patients with 'Bipolar Disorder'. May precipitate mania.\", \"recommendation\": \"Use mood stabilizer.\", \"involvedMedications\": \"Sertraline\", \"patientFactor\": \"Bipolar Disorder\"}]"); prx.addBatch();
            prd.setString(1,"RX-0007"); prd.setString(2,"MED-0013"); prd.setString(3,"50mg"); prd.setString(4,"Once daily"); prd.setString(5,"30 days"); prd.setString(6,"Morning"); prd.setString(7,"Dr. Ramesh Nair"); prd.setDouble(8,202.5); prd.addBatch();
            prx.setString(1,"RX-0008"); prx.setString(2,"PAT-0008"); prx.setString(3,"2026-04-08T10:00"); prx.setString(4,"Dr. Sameer Patel"); prx.setString(5,"APPROVED"); prx.setDouble(6,135.0); prx.setString(7,"[]"); prx.addBatch();
            prd.setString(1,"RX-0008"); prd.setString(2,"MED-0012"); prd.setString(3,"5mg"); prd.setString(4,"Once daily"); prd.setString(5,"30 days"); prd.setString(6,"Morning"); prd.setString(7,"Dr. Sameer Patel"); prd.setDouble(8,135.0); prd.addBatch();
            prx.setString(1,"RX-0009"); prx.setString(2,"PAT-0009"); prx.setString(3,"2026-04-09T11:00"); prx.setString(4,"Dr. Rajan Verma"); prx.setString(5,"REJECTED"); prx.setDouble(6,52.5); prx.setString(7,"[{\"alertLevel\": \"CRITICAL\", \"alertType\": \"DRUG_ALLERGY\", \"title\": \"Potential Allergic Reaction\", \"message\": \"Patient has a known allergy to 'Aspirin'. Aspirin is contraindicated.\", \"recommendation\": \"Use Paracetamol.\", \"involvedMedications\": \"Aspirin\", \"patientFactor\": \"Aspirin\"}]"); prx.addBatch();
            prd.setString(1,"RX-0009"); prd.setString(2,"MED-0026"); prd.setString(3,"75mg"); prd.setString(4,"Once daily"); prd.setString(5,"30 days"); prd.setString(6,"After meals"); prd.setString(7,"Dr. Rajan Verma"); prd.setDouble(8,52.5); prd.addBatch();
            prx.setString(1,"RX-0010"); prx.setString(2,"PAT-0010"); prx.setString(3,"2026-04-10T12:00"); prx.setString(4,"Dr. Rajan Verma"); prx.setString(5,"FLAGGED"); prx.setDouble(6,70.0); prx.setString(7,"[{\"alertLevel\": \"CRITICAL\", \"alertType\": \"DRUG_ALLERGY\", \"title\": \"Potential Allergic Reaction\", \"message\": \"Patient has a known allergy to 'Penicillin'. Amoxicillin is a penicillin.\", \"recommendation\": \"Use macrolide or cephalosporin.\", \"involvedMedications\": \"Amoxicillin\", \"patientFactor\": \"Penicillin\"}]"); prx.addBatch();
            prd.setString(1,"RX-0010"); prd.setString(2,"MED-0001"); prd.setString(3,"500mg"); prd.setString(4,"Twice daily"); prd.setString(5,"7 days"); prd.setString(6,"Finish course"); prd.setString(7,"Dr. Rajan Verma"); prd.setDouble(8,70.0); prd.addBatch();
            prx.setString(1,"RX-0011"); prx.setString(2,"PAT-0011"); prx.setString(3,"2026-04-11T13:00"); prx.setString(4,"Dr. Meena Iyer"); prx.setString(5,"APPROVED"); prx.setDouble(6,202.5); prx.setString(7,"[]"); prx.addBatch();
            prd.setString(1,"RX-0011"); prd.setString(2,"MED-0013"); prd.setString(3,"50mg"); prd.setString(4,"Once daily"); prd.setString(5,"30 days"); prd.setString(6,"Morning"); prd.setString(7,"Dr. Meena Iyer"); prd.setDouble(8,202.5); prd.addBatch();
            prx.setString(1,"RX-0012"); prx.setString(2,"PAT-0012"); prx.setString(3,"2026-04-12T09:00"); prx.setString(4,"Dr. Ramesh Nair"); prx.setString(5,"REJECTED"); prx.setDouble(6,300.0); prx.setString(7,"[{\"alertLevel\": \"CRITICAL\", \"alertType\": \"DRUG_ALLERGY\", \"title\": \"Potential Allergic Reaction\", \"message\": \"Patient has a known allergy to 'NSAIDs'. Ibuprofen is an NSAID.\", \"recommendation\": \"Use Paracetamol.\", \"involvedMedications\": \"Ibuprofen\", \"patientFactor\": \"NSAIDs\"}, {\"alertLevel\": \"CRITICAL\", \"alertType\": \"DRUG_CONDITION\", \"title\": \"Drug-Condition Contraindication\", \"message\": \"Prescribing Ibuprofen is potentially unsafe for patients with 'Heart Failure'. NSAIDs cause sodium retention.\", \"recommendation\": \"Use alternative analgesic.\", \"involvedMedications\": \"Ibuprofen\", \"patientFactor\": \"Heart Failure\"}, {\"alertLevel\": \"CRITICAL\", \"alertType\": \"DRUG_DRUG\", \"title\": \"Drug-Drug Interaction\", \"message\": \"Ibuprofen and Warfarin may interact. NSAIDs potentiate anticoagulant effect.\", \"recommendation\": \"Avoid combination.\", \"involvedMedications\": \"Ibuprofen & Warfarin\"}]"); prx.addBatch();
            prd.setString(1,"RX-0012"); prd.setString(2,"MED-0009"); prd.setString(3,"400mg"); prd.setString(4,"SOS"); prd.setString(5,"5 days"); prd.setString(6,"After food"); prd.setString(7,"Dr. Ramesh Nair"); prd.setDouble(8,60.0); prd.addBatch();
            prd.setString(1,"RX-0012"); prd.setString(2,"MED-0022"); prd.setString(3,"5mg"); prd.setString(4,"Once daily"); prd.setString(5,"30 days"); prd.setString(6,"Take with water"); prd.setString(7,"Dr. Ramesh Nair"); prd.setDouble(8,120.0); prd.addBatch();
            prd.setString(1,"RX-0012"); prd.setString(2,"MED-0012"); prd.setString(3,"5mg"); prd.setString(4,"Once daily"); prd.setString(5,"30 days"); prd.setString(6,"Morning"); prd.setString(7,"Dr. Ramesh Nair"); prd.setDouble(8,120.0); prd.addBatch();
            prx.setString(1,"RX-0013"); prx.setString(2,"PAT-0013"); prx.setString(3,"2026-04-13T10:00"); prx.setString(4,"Dr. Sameer Patel"); prx.setString(5,"APPROVED"); prx.setDouble(6,90.0); prx.setString(7,"[]"); prx.addBatch();
            prd.setString(1,"RX-0013"); prd.setString(2,"MED-0016"); prd.setString(3,"500mg"); prd.setString(4,"Twice daily"); prd.setString(5,"5 days"); prd.setString(6,"After food"); prd.setString(7,"Dr. Sameer Patel"); prd.setDouble(8,90.0); prd.addBatch();
            prx.setString(1,"RX-0014"); prx.setString(2,"PAT-0014"); prx.setString(3,"2026-04-14T11:00"); prx.setString(4,"Dr. Rajan Verma"); prx.setString(5,"FLAGGED"); prx.setDouble(6,90.0); prx.setString(7,"[{\"alertLevel\": \"WARNING\", \"alertType\": \"DRUG_ALLERGY\", \"title\": \"Potential Allergic Reaction\", \"message\": \"Patient has a known allergy to 'Sulfa Drugs'. Low cross-reactivity with Ciprofloxacin.\", \"recommendation\": \"Monitor.\", \"involvedMedications\": \"Ciprofloxacin\", \"patientFactor\": \"Sulfa Drugs\"}]"); prx.addBatch();
            prd.setString(1,"RX-0014"); prd.setString(2,"MED-0016"); prd.setString(3,"500mg"); prd.setString(4,"Twice daily"); prd.setString(5,"5 days"); prd.setString(6,"After food"); prd.setString(7,"Dr. Rajan Verma"); prd.setDouble(8,90.0); prd.addBatch();
            prx.setString(1,"RX-0015"); prx.setString(2,"PAT-0015"); prx.setString(3,"2026-04-15T12:00"); prx.setString(4,"Dr. Meena Iyer"); prx.setString(5,"APPROVED"); prx.setDouble(6,165.0); prx.setString(7,"[]"); prx.addBatch();
            prd.setString(1,"RX-0015"); prd.setString(2,"MED-0014"); prd.setString(3,"50mcg"); prd.setString(4,"Once daily"); prd.setString(5,"30 days"); prd.setString(6,"Empty stomach"); prd.setString(7,"Dr. Meena Iyer"); prd.setDouble(8,165.0); prd.addBatch();
            prx.executeBatch();
            prd.executeBatch();
            conn.commit();
            System.out.println("\u2713 15 prescriptions seeded.");

            // Summary
            System.out.println("\n─────────── Database Summary ───────────");
            for (String[] q : new String[][]{
                {"patients","Patients"},{"medications","Medications"},
                {"prescriptions","Prescriptions"},{"prescribed_drugs","Prescribed Drugs"},
                {"audit_logs","Audit Log Entries"},{"interaction_rules","Interaction Rule Sets"}}) {
                ResultSet r = conn.createStatement().executeQuery("SELECT count(*) FROM " + q[0]);
                r.next();
                System.out.printf("  %-24s %d%n", q[1]+":", r.getInt(1));
            }
            System.out.println("────────────────────────────────────────");
            System.out.println("\nSeeding complete. Launch app now.");
        }
    }
}
