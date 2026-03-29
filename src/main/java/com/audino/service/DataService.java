package com.audino.service;

import com.audino.model.InjectionMedication;
import com.audino.model.InteractionAlert;
import com.audino.model.Medication;
import com.audino.model.MedicationType;
import com.audino.model.Patient;
import com.audino.model.PrescribedDrug;
import com.audino.model.Prescription;
import com.audino.model.PrescriptionStatus;
import com.audino.model.TabletMedication;
import com.audino.model.LiquidMedication;
import com.audino.util.ConfigurationManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> RULES_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<InteractionAlert>> ALERT_LIST_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final ConfigurationManager config;
    private final String jdbcUrl;
    private List<Patient> patients = new ArrayList<>();
    private List<Medication> medications = new ArrayList<>();
    private List<Prescription> prescriptions = new ArrayList<>();
    private Map<String, Object> interactionRules = new HashMap<>();

    public DataService() {
        this.config = ConfigurationManager.getInstance();
        this.objectMapper = config.getObjectMapper();
        this.jdbcUrl = "jdbc:sqlite:" + resolveDatabasePath(config.getSqliteDatabasePath());
    }

    public synchronized void loadAllData() {
        try (Connection connection = openConnection()) {
            ensureSchema(connection);
            ensureBaselineData(connection);

            patients = loadPatientsFromDb(connection);
            medications = loadMedicationsFromDb(connection);
            interactionRules = loadInteractionRulesFromDb(connection);
            prescriptions = loadPrescriptionsFromDb(connection, buildMedicationIndex(medications));

            System.out.println("All data loaded from SQLite.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load data from SQLite", e);
        }
    }

    private String resolveDatabasePath(String configuredPath) {
        Path primaryPath = toAbsolutePath(configuredPath);
        if (isWritableDatabasePath(primaryPath)) {
            return primaryPath.toString();
        }

        Path fallbackPath = defaultFallbackDatabasePath();
        if (isWritableDatabasePath(fallbackPath)) {
            System.err.println("Configured SQLite path is not writable: " + primaryPath);
            System.err.println("Falling back to writable SQLite path: " + fallbackPath);
            return fallbackPath.toString();
        }

        throw new RuntimeException(
            "No writable SQLite database path available. Tried: " + primaryPath + " and " + fallbackPath
        );
    }

    private Path toAbsolutePath(String configuredPath) {
        Path dbPath = Paths.get(configuredPath);
        if (!dbPath.isAbsolute()) {
            dbPath = Paths.get(System.getProperty("user.dir")).resolve(dbPath);
        }
        return dbPath.toAbsolutePath().normalize();
    }

    private Path defaultFallbackDatabasePath() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path base = (localAppData == null || localAppData.isBlank())
            ? Paths.get(System.getProperty("user.home"), "AppData", "Local")
            : Paths.get(localAppData);
        return base.resolve("Audino").resolve("data").resolve("audino.db");
    }

    private boolean isWritableDatabasePath(Path dbPath) {
        try {
            Path parent = dbPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
                Path probeFile = parent.resolve(".audino-write-probe-" + UUID.randomUUID());
                Files.writeString(probeFile, "probe");
                Files.deleteIfExists(probeFile);
            }

            if (!Files.exists(dbPath)) {
                Files.createFile(dbPath);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private void ensureSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS patients (
                    patient_id TEXT PRIMARY KEY,
                    first_name TEXT,
                    last_name TEXT,
                    date_of_birth TEXT,
                    gender TEXT,
                    contact_number TEXT,
                    allergies_json TEXT,
                    chronic_conditions_json TEXT
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS medications (
                    medication_id TEXT PRIMARY KEY,
                    generic_name TEXT,
                    brand_name TEXT,
                    medication_type TEXT,
                    strength TEXT,
                    concentration TEXT,
                    route TEXT,
                    active_ingredients_json TEXT,
                    interaction_identifiers_json TEXT
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS interaction_rules (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    rules_json TEXT NOT NULL
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS prescriptions (
                    prescription_id TEXT PRIMARY KEY,
                    patient_id TEXT NOT NULL UNIQUE,
                    created_at TEXT,
                    prescribed_by TEXT,
                    status TEXT,
                    alerts_json TEXT,
                    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS prescribed_drugs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    prescription_id TEXT NOT NULL,
                    medication_id TEXT NOT NULL,
                    dosage TEXT,
                    frequency TEXT,
                    duration TEXT,
                    special_instructions TEXT,
                    prescribed_by TEXT,
                    FOREIGN KEY (prescription_id) REFERENCES prescriptions(prescription_id) ON DELETE CASCADE,
                    FOREIGN KEY (medication_id) REFERENCES medications(medication_id)
                )
                """);
        }
    }

    private void ensureBaselineData(Connection connection) throws Exception {
        boolean shouldSeedBaseline = isTableEmpty(connection, "patients")
            && isTableEmpty(connection, "medications")
            && isTableEmpty(connection, "interaction_rules")
            && isTableEmpty(connection, "prescriptions")
            && isTableEmpty(connection, "prescribed_drugs");

        if (!shouldSeedBaseline) {
            return;
        }

        connection.setAutoCommit(false);
        try {
            insertPatients(connection, createBaselinePatients());
            insertMedications(connection, createBaselineMedications());
            upsertInteractionRules(connection, createBaselineInteractionRules());
            connection.commit();
            System.out.println("Seeded baseline data directly into SQLite.");
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private List<Patient> createBaselinePatients() {
        List<Patient> baselinePatients = new ArrayList<>();

        Patient mandal = new Patient("Mridankan", "Mandal", LocalDate.of(1998, 4, 12));
        mandal.setPatientId("PAT-00000001");
        mandal.setGender("Male");
        mandal.setContactNumber("+91-9000000001");
        mandal.addAllergy("Pollen");
        mandal.addChronicCondition("Mild Asthma");
        baselinePatients.add(mandal);

        Patient kumar = new Patient("Ravi", "Kumar", LocalDate.of(1985, 9, 21));
        kumar.setPatientId("PAT-00000002");
        kumar.setGender("Male");
        kumar.setContactNumber("+91-9000000002");
        kumar.addAllergy("Penicillin");
        kumar.addChronicCondition("Hypertension");
        kumar.addChronicCondition("Chronic Kidney Disease");
        baselinePatients.add(kumar);

        Patient patel = new Patient("Neha", "Patel", LocalDate.of(1991, 2, 3));
        patel.setPatientId("PAT-00000003");
        patel.setGender("Female");
        patel.setContactNumber("+91-9000000003");
        baselinePatients.add(patel);

        return baselinePatients;
    }

    private List<Medication> createBaselineMedications() {
        List<Medication> baselineMedications = new ArrayList<>();

        TabletMedication amoxicillin = new TabletMedication("MED-00000001", "Amoxicillin", "Amoxil", "500mg");
        amoxicillin.setActiveIngredients(List.of("Amoxicillin"));
        amoxicillin.setInteractionIdentifiers(List.of("PENICILLIN", "ANTIBIOTIC"));
        baselineMedications.add(amoxicillin);

        TabletMedication ibuprofen = new TabletMedication("MED-00000002", "Ibuprofen", "Advil", "400mg");
        ibuprofen.setActiveIngredients(List.of("Ibuprofen"));
        ibuprofen.setInteractionIdentifiers(List.of("NSAID", "ANALGESIC"));
        baselineMedications.add(ibuprofen);

        TabletMedication warfarin = new TabletMedication("MED-00000003", "Warfarin", "Coumadin", "5mg");
        warfarin.setActiveIngredients(List.of("Warfarin"));
        warfarin.setInteractionIdentifiers(List.of("ANTICOAGULANT"));
        baselineMedications.add(warfarin);

        TabletMedication lisinopril = new TabletMedication("MED-00000004", "Lisinopril", "Prinivil", "10mg");
        lisinopril.setActiveIngredients(List.of("Lisinopril"));
        lisinopril.setInteractionIdentifiers(List.of("ACE_INHIBITOR", "ANTIHYPERTENSIVE"));
        baselineMedications.add(lisinopril);

        return baselineMedications;
    }

    private Map<String, Object> createBaselineInteractionRules() {
        Map<String, Object> rules = new HashMap<>();

        Map<String, Object> allergyInteractions = new HashMap<>();
        Map<String, Object> penicillinRule = new HashMap<>();
        penicillinRule.put("allergyKeywords", List.of("penicillin"));
        penicillinRule.put("medicationClasses", List.of("PENICILLIN"));
        penicillinRule.put("severity", "CRITICAL");
        penicillinRule.put("description", "Penicillin-related drugs can trigger severe allergic reactions.");
        penicillinRule.put("recommendation", "Avoid penicillin-class medications and choose an alternative.");
        allergyInteractions.put("PENICILLIN_ALLERGY", penicillinRule);
        rules.put("drugAllergyInteractions", allergyInteractions);

        Map<String, Object> conditionInteractions = new HashMap<>();
        Map<String, Object> kidneyRule = new HashMap<>();
        kidneyRule.put("conditionKeywords", List.of("kidney", "renal"));
        kidneyRule.put("medicationClasses", List.of("NSAID"));
        kidneyRule.put("severity", "WARNING");
        kidneyRule.put("description", "NSAIDs may worsen kidney function in vulnerable patients.");
        kidneyRule.put("recommendation", "Use caution and monitor renal function during therapy.");
        conditionInteractions.put("NSAID_KIDNEY", kidneyRule);

        Map<String, Object> hypertensionRule = new HashMap<>();
        hypertensionRule.put("conditionKeywords", List.of("hypertension", "blood pressure"));
        hypertensionRule.put("medicationClasses", List.of("NSAID"));
        hypertensionRule.put("severity", "WARNING");
        hypertensionRule.put("description", "NSAIDs may reduce antihypertensive efficacy and increase blood pressure.");
        hypertensionRule.put("recommendation", "Consider an alternative analgesic and monitor blood pressure.");
        conditionInteractions.put("NSAID_HYPERTENSION", hypertensionRule);
        rules.put("drugConditionInteractions", conditionInteractions);

        Map<String, Object> drugDrugInteractions = new HashMap<>();
        Map<String, Object> warfarinNsaidRule = new HashMap<>();
        warfarinNsaidRule.put("drug1", "NSAID");
        warfarinNsaidRule.put("drug2", "ANTICOAGULANT");
        warfarinNsaidRule.put("severity", "CRITICAL");
        warfarinNsaidRule.put("description", "Concurrent use increases the risk of bleeding.");
        warfarinNsaidRule.put("recommendation", "Avoid combination when possible or monitor INR and bleeding signs closely.");
        drugDrugInteractions.put("NSAID_ANTICOAGULANT", warfarinNsaidRule);
        rules.put("drugDrugInteractions", drugDrugInteractions);

        return rules;
    }

    private boolean isTableEmpty(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() && rs.getLong(1) == 0;
        }
    }

    private List<Patient> loadPatientsFromDb(Connection connection) throws Exception {
        List<Patient> results = new ArrayList<>();
        String sql = """
            SELECT patient_id, first_name, last_name, date_of_birth, gender, contact_number,
                   allergies_json, chronic_conditions_json
            FROM patients
            ORDER BY first_name, last_name
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Patient patient = new Patient();
                patient.setPatientId(rs.getString("patient_id"));
                patient.setFirstName(rs.getString("first_name"));
                patient.setLastName(rs.getString("last_name"));
                patient.setDateOfBirth(parseLocalDate(rs.getString("date_of_birth")));
                patient.setGender(rs.getString("gender"));
                patient.setContactNumber(rs.getString("contact_number"));
                patient.setAllergies(fromJsonList(rs.getString("allergies_json")));
                patient.setChronicConditions(fromJsonList(rs.getString("chronic_conditions_json")));
                results.add(patient);
            }
        }
        return results;
    }

    private List<Medication> loadMedicationsFromDb(Connection connection) throws Exception {
        List<Medication> results = new ArrayList<>();
        String sql = """
            SELECT medication_id, generic_name, brand_name, medication_type,
                   strength, concentration, route,
                   active_ingredients_json, interaction_identifiers_json
            FROM medications
            ORDER BY generic_name
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Medication medication = createMedicationInstance(rs.getString("medication_type"));
                medication.setMedicationId(rs.getString("medication_id"));
                medication.setGenericName(rs.getString("generic_name"));
                medication.setBrandName(rs.getString("brand_name"));
                medication.setActiveIngredients(fromJsonList(rs.getString("active_ingredients_json")));
                medication.setInteractionIdentifiers(fromJsonList(rs.getString("interaction_identifiers_json")));

                if (medication instanceof TabletMedication tabletMedication) {
                    tabletMedication.setStrength(rs.getString("strength"));
                } else if (medication instanceof LiquidMedication liquidMedication) {
                    liquidMedication.setConcentration(rs.getString("concentration"));
                } else if (medication instanceof InjectionMedication injectionMedication) {
                    injectionMedication.setConcentration(rs.getString("concentration"));
                    injectionMedication.setRoute(rs.getString("route"));
                }
                results.add(medication);
            }
        }
        return results;
    }

    private Medication createMedicationInstance(String medicationTypeValue) {
        MedicationType type;
        try {
            type = MedicationType.valueOf(medicationTypeValue);
        } catch (Exception e) {
            type = MedicationType.TABLET;
        }

        return switch (type) {
            case TABLET -> new TabletMedication();
            case LIQUID -> new LiquidMedication();
            case INJECTION -> new InjectionMedication();
        };
    }

    private Map<String, Object> loadInteractionRulesFromDb(Connection connection) throws Exception {
        String sql = "SELECT rules_json FROM interaction_rules WHERE id = 1";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (!rs.next()) {
                return new HashMap<>();
            }
            String rulesJson = rs.getString("rules_json");
            if (rulesJson == null || rulesJson.isBlank()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(rulesJson, RULES_TYPE);
        }
    }

    private List<Prescription> loadPrescriptionsFromDb(Connection connection, Map<String, Medication> medicationsById) throws Exception {
        List<Prescription> results = new ArrayList<>();
        String sql = """
            SELECT prescription_id, patient_id, created_at, prescribed_by, status, alerts_json
            FROM prescriptions
            ORDER BY created_at
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Prescription prescription = new Prescription();
                prescription.setPrescriptionId(rs.getString("prescription_id"));
                prescription.setPatientId(rs.getString("patient_id"));
                prescription.setCreatedAt(parseLocalDateTime(rs.getString("created_at")));
                prescription.setPrescribedBy(rs.getString("prescribed_by"));
                prescription.setStatus(parsePrescriptionStatus(rs.getString("status")));
                prescription.setAlerts(fromJsonAlerts(rs.getString("alerts_json")));
                prescription.setPrescribedDrugs(loadPrescribedDrugs(connection, prescription.getPrescriptionId(), medicationsById));
                results.add(prescription);
            }
        }

        return results;
    }

    private List<PrescribedDrug> loadPrescribedDrugs(Connection connection, String prescriptionId, Map<String, Medication> medicationsById) throws SQLException {
        List<PrescribedDrug> drugs = new ArrayList<>();
        String sql = """
            SELECT medication_id, dosage, frequency, duration, special_instructions, prescribed_by
            FROM prescribed_drugs
            WHERE prescription_id = ?
            ORDER BY id
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prescriptionId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    PrescribedDrug prescribedDrug = new PrescribedDrug();
                    prescribedDrug.setMedicationId(rs.getString("medication_id"));
                    prescribedDrug.setDosage(rs.getString("dosage"));
                    prescribedDrug.setFrequency(rs.getString("frequency"));
                    prescribedDrug.setDuration(rs.getString("duration"));
                    prescribedDrug.setSpecialInstructions(rs.getString("special_instructions"));
                    prescribedDrug.setPrescribedBy(rs.getString("prescribed_by"));
                    prescribedDrug.setMedication(medicationsById.get(prescribedDrug.getMedicationId()));
                    drugs.add(prescribedDrug);
                }
            }
        }
        return drugs;
    }

    private Map<String, Medication> buildMedicationIndex(List<Medication> medicationList) {
        return medicationList.stream().collect(Collectors.toMap(Medication::getMedicationId, Function.identity(), (a, b) -> a));
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private LocalDateTime parseLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private PrescriptionStatus parsePrescriptionStatus(String value) {
        if (value == null || value.isBlank()) {
            return PrescriptionStatus.DRAFT;
        }
        try {
            return PrescriptionStatus.valueOf(value);
        } catch (Exception e) {
            return PrescriptionStatus.DRAFT;
        }
    }

    private List<String> fromJsonList(String jsonValue) {
        try {
            if (jsonValue == null || jsonValue.isBlank()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(jsonValue, STRING_LIST_TYPE);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<InteractionAlert> fromJsonAlerts(String jsonValue) {
        try {
            if (jsonValue == null || jsonValue.isBlank()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(jsonValue, ALERT_LIST_TYPE);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String toJson(Object value) throws Exception {
        if (value == null) {
            return "null";
        }
        return objectMapper.writeValueAsString(value);
    }

    private void insertPatients(Connection connection, List<Patient> patientList) throws Exception {
        for (Patient patient : patientList) {
            upsertPatient(connection, patient);
        }
    }

    private void upsertPatient(Connection connection, Patient patient) throws Exception {
        String sql = """
            INSERT INTO patients (
                patient_id, first_name, last_name, date_of_birth, gender, contact_number,
                allergies_json, chronic_conditions_json
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(patient_id) DO UPDATE SET
                first_name = excluded.first_name,
                last_name = excluded.last_name,
                date_of_birth = excluded.date_of_birth,
                gender = excluded.gender,
                contact_number = excluded.contact_number,
                allergies_json = excluded.allergies_json,
                chronic_conditions_json = excluded.chronic_conditions_json
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patient.getPatientId());
            statement.setString(2, patient.getFirstName());
            statement.setString(3, patient.getLastName());
            statement.setString(4, patient.getDateOfBirth() == null ? null : patient.getDateOfBirth().toString());
            statement.setString(5, patient.getGender());
            statement.setString(6, patient.getContactNumber());
            statement.setString(7, toJson(patient.getAllergies()));
            statement.setString(8, toJson(patient.getChronicConditions()));
            statement.executeUpdate();
        }
    }

    private void insertMedications(Connection connection, List<Medication> medicationList) throws Exception {
        String sql = """
            INSERT INTO medications (
                medication_id, generic_name, brand_name, medication_type,
                strength, concentration, route,
                active_ingredients_json, interaction_identifiers_json
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(medication_id) DO UPDATE SET
                generic_name = excluded.generic_name,
                brand_name = excluded.brand_name,
                medication_type = excluded.medication_type,
                strength = excluded.strength,
                concentration = excluded.concentration,
                route = excluded.route,
                active_ingredients_json = excluded.active_ingredients_json,
                interaction_identifiers_json = excluded.interaction_identifiers_json
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Medication medication : medicationList) {
                statement.setString(1, medication.getMedicationId());
                statement.setString(2, medication.getGenericName());
                statement.setString(3, medication.getBrandName());
                statement.setString(4, medication.getMedicationType().name());
                statement.setString(5, medication instanceof TabletMedication tabletMedication ? tabletMedication.getStrength() : null);
                statement.setString(6,
                    medication instanceof LiquidMedication liquidMedication ? liquidMedication.getConcentration() :
                    medication instanceof InjectionMedication injectionMedication ? injectionMedication.getConcentration() : null
                );
                statement.setString(7, medication instanceof InjectionMedication injectionMedication ? injectionMedication.getRoute() : null);
                statement.setString(8, toJson(medication.getActiveIngredients()));
                statement.setString(9, toJson(medication.getInteractionIdentifiers()));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void upsertInteractionRules(Connection connection, Map<String, Object> rules) throws Exception {
        String sql = """
            INSERT INTO interaction_rules (id, rules_json)
            VALUES (1, ?)
            ON CONFLICT(id) DO UPDATE SET rules_json = excluded.rules_json
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, toJson(rules));
            statement.executeUpdate();
        }
    }

    private void replacePrescriptions(Connection connection, List<Prescription> prescriptionList) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM prescribed_drugs");
            statement.executeUpdate("DELETE FROM prescriptions");
        }

        for (Prescription prescription : prescriptionList) {
            upsertPrescription(connection, prescription);
            insertPrescribedDrugs(connection, prescription);
        }
    }

    private void upsertPrescription(Connection connection, Prescription prescription) throws Exception {
        if (prescription.getPrescriptionId() == null || prescription.getPrescriptionId().isBlank()) {
            prescription.setPrescriptionId(generatePrescriptionId());
        }

        String sql = """
            INSERT INTO prescriptions (
                prescription_id, patient_id, created_at, prescribed_by, status, alerts_json
            ) VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(prescription_id) DO UPDATE SET
                patient_id = excluded.patient_id,
                created_at = excluded.created_at,
                prescribed_by = excluded.prescribed_by,
                status = excluded.status,
                alerts_json = excluded.alerts_json
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prescription.getPrescriptionId());
            statement.setString(2, prescription.getPatientId());
            statement.setString(3, prescription.getCreatedAt() == null ? null : prescription.getCreatedAt().toString());
            statement.setString(4, prescription.getPrescribedBy());
            statement.setString(5, prescription.getStatus() == null ? PrescriptionStatus.DRAFT.name() : prescription.getStatus().name());
            statement.setString(6, toJson(prescription.getAlerts()));
            statement.executeUpdate();
        }
    }

    private void insertPrescribedDrugs(Connection connection, Prescription prescription) throws Exception {
        String sql = """
            INSERT INTO prescribed_drugs (
                prescription_id, medication_id, dosage, frequency, duration, special_instructions, prescribed_by
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (PrescribedDrug drug : prescription.getPrescribedDrugs()) {
                String medicationId = drug.getMedicationId();
                if ((medicationId == null || medicationId.isBlank()) && drug.getMedication() != null) {
                    medicationId = drug.getMedication().getMedicationId();
                }
                if (medicationId == null || medicationId.isBlank()) {
                    continue;
                }

                statement.setString(1, prescription.getPrescriptionId());
                statement.setString(2, medicationId);
                statement.setString(3, drug.getDosage());
                statement.setString(4, drug.getFrequency());
                statement.setString(5, drug.getDuration());
                statement.setString(6, drug.getSpecialInstructions());
                statement.setString(7, drug.getPrescribedBy());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>(patients);
    }

    public List<Medication> getAllMedications() {
        return new ArrayList<>(medications);
    }

    public List<Prescription> getAllPrescriptions() {
        return new ArrayList<>(prescriptions);
    }

    public Map<String, Object> getInteractionRules() {
        return interactionRules;
    }
    
    public List<Patient> searchPatients(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllPatients();
        }
        String lowerCaseTerm = searchTerm.toLowerCase();
        return patients.stream()
                .filter(p -> p.getFullName().toLowerCase().contains(lowerCaseTerm))
                .collect(Collectors.toList());
    }

    public List<Medication> searchMedications(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllMedications();
        }
        String lowerCaseTerm = searchTerm.toLowerCase();
        return medications.stream()
                .filter(m -> m.getGenericName().toLowerCase().contains(lowerCaseTerm) ||
                             (m.getBrandName() != null && m.getBrandName().toLowerCase().contains(lowerCaseTerm)))
                .collect(Collectors.toList());
    }

    public synchronized void savePatient(Patient patient) {
        if (patient == null) {
            return;
        }
        if (patient.getPatientId() == null || patient.getPatientId().isBlank()) {
            patient.setPatientId(generatePatientId());
        }

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            upsertPatient(connection, patient);
            connection.commit();
            connection.setAutoCommit(true);

            patients.removeIf(p -> p.getPatientId().equals(patient.getPatientId()));
            patients.add(patient);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save patient", e);
        }
    }

    public synchronized void updatePatient(Patient patient) {
        savePatient(patient);
    }

    public synchronized void deletePatient(Patient patient) {
        if (patient == null || patient.getPatientId() == null || patient.getPatientId().isBlank()) {
            return;
        }

        String patientId = patient.getPatientId();
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM patients WHERE patient_id = ?")) {
                statement.setString(1, patientId);
                statement.executeUpdate();
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete patient", e);
        }

        patients.removeIf(p -> patientId.equals(p.getPatientId()));
        prescriptions.removeIf(p -> patientId.equals(p.getPatientId()));
    }

    public synchronized void savePrescription(Prescription prescription) {
        if (prescription == null || prescription.getPatientId() == null || prescription.getPatientId().isBlank()) {
            throw new IllegalArgumentException("Prescription must reference a valid patient.");
        }

        if (prescription.getPrescriptionId() == null || prescription.getPrescriptionId().isBlank()) {
            prescription.setPrescriptionId(generatePrescriptionId());
        }
        if (prescription.getStatus() == null) {
            prescription.setStatus(PrescriptionStatus.DRAFT);
        }

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM prescriptions WHERE patient_id = ? AND prescription_id <> ?")) {
                statement.setString(1, prescription.getPatientId());
                statement.setString(2, prescription.getPrescriptionId());
                statement.executeUpdate();
            }

            upsertPrescription(connection, prescription);

            try (PreparedStatement deleteDrugs = connection.prepareStatement(
                "DELETE FROM prescribed_drugs WHERE prescription_id = ?")) {
                deleteDrugs.setString(1, prescription.getPrescriptionId());
                deleteDrugs.executeUpdate();
            }

            insertPrescribedDrugs(connection, prescription);
            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save prescription", e);
        }

        hydratePrescription(prescription, buildMedicationIndex(medications));
        prescriptions.removeIf(p -> p.getPatientId().equals(prescription.getPatientId()));
        prescriptions.add(prescription);
    }

    public boolean addMedicationToExistingPrescription(String patientId, Medication medication, String dosage, String frequency, String duration, String prescribingPhysician) {
        Prescription existingPrescription = prescriptions.stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .findFirst()
                .orElse(null);

        if (existingPrescription != null) {
            existingPrescription.addPrescribedDrug(new PrescribedDrug(
                medication, dosage, frequency, duration, "", prescribingPhysician));
            savePrescription(existingPrescription);
            return true;
        }
        return false;
    }

    public List<Prescription> getPrescriptionsForPatient(Patient patient) {
        return prescriptions.stream()
                .filter(p -> p.getPatientId().equals(patient.getPatientId()))
                .collect(Collectors.toList());
    }
    
    public Prescription getActivePrescriberionForPatient(String patientId) {
        return prescriptions.stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .findFirst()
                .orElse(null);
    }
    
    public synchronized void saveAllData() {
        saveAllData(new ArrayList<>(patients), new ArrayList<>(prescriptions));
    }

    public synchronized void saveAllData(List<Patient> currentPatients, List<Prescription> currentPrescriptions) {
        List<Patient> safePatients = currentPatients == null ? new ArrayList<>() : new ArrayList<>(currentPatients);
        List<Prescription> safePrescriptions = currentPrescriptions == null ? new ArrayList<>() : new ArrayList<>(currentPrescriptions);

        try {
            try (Connection connection = openConnection()) {
                connection.setAutoCommit(false);
                replacePatients(connection, safePatients);
                replacePrescriptions(connection, safePrescriptions);
                connection.commit();
                connection.setAutoCommit(true);
            }

            Map<String, Medication> medicationIndex = buildMedicationIndex(medications);
            for (Prescription prescription : safePrescriptions) {
                hydratePrescription(prescription, medicationIndex);
            }

            this.patients = safePatients;
            this.prescriptions = safePrescriptions;
            System.out.println("All data saved to SQLite successfully.");
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void replacePatients(Connection connection, List<Patient> patientList) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM patients");
        }
        insertPatients(connection, patientList);
    }

    private void hydratePrescription(Prescription prescription, Map<String, Medication> medicationsById) {
        List<PrescribedDrug> hydrated = new ArrayList<>(prescription.getPrescribedDrugs());
        for (PrescribedDrug drug : hydrated) {
            if (drug.getMedication() == null && drug.getMedicationId() != null) {
                drug.setMedication(medicationsById.get(drug.getMedicationId()));
            }
        }
        prescription.setPrescribedDrugs(hydrated);
    }

    private String generatePatientId() {
        return "PAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generatePrescriptionId() {
        return "RX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}