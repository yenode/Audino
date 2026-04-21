package com.audino.service;

import com.audino.model.Medication;
import com.audino.model.MedicationType;
import com.audino.model.Patient;
import com.audino.model.PrescribedDrug;
import com.audino.model.Prescription;
import com.audino.model.PrescriptionStatus;
import com.audino.model.InteractionAlert;
import com.audino.model.TabletMedication;
import com.audino.model.LiquidMedication;
import com.audino.model.InjectionMedication;
import com.audino.util.ConfigurationManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class DataService {

    private final ObjectMapper objectMapper;
    private final ConfigurationManager config;
    private List<Patient> patients = new ArrayList<>();
    private List<Medication> medications = new ArrayList<>();
    private List<Prescription> prescriptions = new ArrayList<>();
    private Map<String, Object> interactionRules;
    private MedicationSearchEngine medicationSearchEngine = new MedicationSearchEngine(List.of());
    private final boolean useExternalDataFallback;
    private final String externalDataRoot;
    private File sqliteDbFile;

    public DataService() {
        this.config = ConfigurationManager.getInstance();
        this.objectMapper = config.getObjectMapper();
        this.useExternalDataFallback = System.getProperty("surefire.test.class.path") == null;
        this.externalDataRoot = resolveExternalDataRoot();
    }

    public void loadAllData() {
        if (useExternalDataFallback) {
            loadAllFromSqlite();
        } else {
            patients = loadData(config.getPatientsDataFile(), new TypeReference<>() {});
            medications = loadData(config.getMedicationsDataFile(), new TypeReference<>() {});
            interactionRules = loadData(config.getInteractionRulesDataFile(), new TypeReference<>() {});
            prescriptions = loadData(config.getPrescriptionsDataFile(), new TypeReference<>() {});
        }
        medicationSearchEngine = new MedicationSearchEngine(medications);
        System.out.println("All data loaded.");
    }

    private void loadAllFromSqlite() {
        sqliteDbFile = resolveSqlitePath();
        if (sqliteDbFile == null) {
            throw new RuntimeException("Could not resolve SQLite database path for Audino runtime.");
        }

        ensureSqliteParentDirectory(sqliteDbFile);

        String jdbcUrl = "jdbc:sqlite:" + sqliteDbFile.getAbsolutePath();
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            connection.setAutoCommit(true);
            ensureSqliteSchema(connection);
            medications = loadMedicationsFromSqlite(connection);
            interactionRules = loadInteractionRulesFromSqlite(connection);
            patients = loadPatientsFromSqlite(connection);
            prescriptions = loadPrescriptionsFromSqlite(connection, medications);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Audino runtime data from SQLite: " + e.getMessage(), e);
        }
    }

    private boolean loadMedicationAndRulesFromSqlite() {
        File sqliteFile = resolveSqlitePath();
        if (sqliteFile == null || !sqliteFile.exists() || !sqliteFile.isFile()) {
            return false;
        }

        String jdbcUrl = "jdbc:sqlite:" + sqliteFile.getAbsolutePath();
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            List<Medication> sqliteMedications = loadMedicationsFromSqlite(connection);
            Map<String, Object> sqliteRules = loadInteractionRulesFromSqlite(connection);

            if (!sqliteMedications.isEmpty() && sqliteRules != null && !sqliteRules.isEmpty()) {
                medications = sqliteMedications;
                interactionRules = sqliteRules;
                return true;
            }
        } catch (Exception e) {
            System.err.println("Failed to load medication/rules from SQLite, falling back to JSON: " + e.getMessage());
        }

        return false;
    }

    private File resolveSqlitePath() {
        String configuredPath = System.getProperty("audino.sqlite.path");
        if (configuredPath != null && !configuredPath.isBlank()) {
            return Paths.get(configuredPath.trim()).toFile();
        }

        String explicitDataDir = System.getProperty("audino.data.dir");
        if (explicitDataDir != null && !explicitDataDir.isBlank()) {
            return Paths.get(explicitDataDir.trim(), "data", "audino.db").toFile();
        }

        List<Path> candidates = new ArrayList<>();

        Path cwd = Paths.get(System.getProperty("user.dir"));
        candidates.add(cwd.resolve("data").resolve("audino.db"));
        candidates.add(cwd.resolve("audino").resolve("data").resolve("audino.db"));
        candidates.add(cwd.resolve("..").resolve("audino").resolve("data").resolve("audino.db").normalize());
        candidates.add(Paths.get(externalDataRoot, "data", "audino.db"));

        for (Path candidate : candidates) {
            File file = candidate.toFile();
            if (file.exists() && file.isFile()) {
                return file;
            }
        }

        Path createTarget = Paths.get(externalDataRoot, "data", "audino.db");
        return createTarget.toFile();

    }

    private Connection openSqliteConnectionForWrite() throws SQLException {
        if (sqliteDbFile == null) {
            sqliteDbFile = resolveSqlitePath();
        }

        ensureSqliteParentDirectory(sqliteDbFile);

        String jdbcUrl = "jdbc:sqlite:" + sqliteDbFile.getAbsolutePath();
        Connection connection = DriverManager.getConnection(jdbcUrl);
        connection.setAutoCommit(false);
        ensureSqliteSchema(connection);
        return connection;
    }

    private void ensureSqliteParentDirectory(File sqliteFile) {
        if (sqliteFile == null) {
            return;
        }

        File parent = sqliteFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new RuntimeException("Failed to create SQLite data directory: " + parent.getAbsolutePath());
        }
    }

    private void ensureSqliteSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS patients (" +
                            "patient_id TEXT PRIMARY KEY," +
                            "first_name TEXT," +
                            "last_name TEXT," +
                            "date_of_birth TEXT," +
                            "gender TEXT," +
                            "contact_number TEXT," +
                            "allergies_json TEXT," +
                            "chronic_conditions_json TEXT" +
                            ")"
            );
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS prescriptions (" +
                            "prescription_id TEXT PRIMARY KEY," +
                            "patient_id TEXT NOT NULL UNIQUE," +
                            "created_at TEXT," +
                            "prescribed_by TEXT," +
                            "status TEXT," +
                            "alerts_json TEXT," +
                            "FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE" +
                            ")"
            );
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS prescribed_drugs (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "prescription_id TEXT NOT NULL," +
                            "medication_id TEXT NOT NULL," +
                            "dosage TEXT," +
                            "frequency TEXT," +
                            "duration TEXT," +
                            "special_instructions TEXT," +
                            "prescribed_by TEXT," +
                            "FOREIGN KEY (prescription_id) REFERENCES prescriptions(prescription_id) ON DELETE CASCADE," +
                            "FOREIGN KEY (medication_id) REFERENCES medications(medication_id)" +
                            ")"
            );
                        statement.execute(
                            "CREATE TABLE IF NOT EXISTS medications (" +
                                "medication_id TEXT PRIMARY KEY," +
                                "generic_name TEXT," +
                                "brand_name TEXT," +
                                "rxnorm_code TEXT," +
                                "medication_type TEXT," +
                                "strength TEXT," +
                                "concentration TEXT," +
                                "route TEXT," +
                                "active_ingredients_json TEXT," +
                                "interaction_identifiers_json TEXT" +
                                ")"
                        );
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS interaction_rules (" +
                            "id INTEGER PRIMARY KEY CHECK (id = 1)," +
                            "rules_json TEXT NOT NULL" +
                            ")"
            );
        }
    }

    private List<Medication> loadMedicationsFromSqlite(Connection connection) throws SQLException, IOException {
        List<Medication> result = new ArrayList<>();

        String sql = "SELECT medication_id, generic_name, brand_name, rxnorm_code, medication_type, strength, concentration, route, active_ingredients_json, interaction_identifiers_json FROM medications";
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                String medicationType = normalizeText(rs.getString("medication_type")).toUpperCase();
                Medication medication;
                switch (medicationType) {
                    case "LIQUID":
                        LiquidMedication liquidMedication = new LiquidMedication();
                        liquidMedication.setConcentration(normalizeText(rs.getString("concentration")));
                        medication = liquidMedication;
                        break;
                    case "INJECTION":
                        InjectionMedication injectionMedication = new InjectionMedication();
                        injectionMedication.setConcentration(normalizeText(rs.getString("concentration")));
                        injectionMedication.setRoute(normalizeText(rs.getString("route")));
                        medication = injectionMedication;
                        break;
                    case "TABLET":
                    default:
                        TabletMedication tabletMedication = new TabletMedication();
                        tabletMedication.setStrength(normalizeText(rs.getString("strength")));
                        medication = tabletMedication;
                        break;
                }

                medication.setMedicationId(normalizeText(rs.getString("medication_id")));
                medication.setGenericName(normalizeText(rs.getString("generic_name")));
                medication.setBrandName(normalizeText(rs.getString("brand_name")));
                medication.setRxNormCode(normalizeText(rs.getString("rxnorm_code")));

                try {
                    medication.setMedicationType(MedicationType.valueOf(medicationType));
                } catch (IllegalArgumentException ignored) {
                    medication.setMedicationType(MedicationType.TABLET);
                }

                String activeIngredientsJson = normalizeText(rs.getString("active_ingredients_json"));
                String interactionIdentifiersJson = normalizeText(rs.getString("interaction_identifiers_json"));

                List<String> activeIngredients = activeIngredientsJson.isBlank()
                        ? List.of()
                        : objectMapper.readValue(activeIngredientsJson, new TypeReference<List<String>>() {});
                List<String> interactionIdentifiers = interactionIdentifiersJson.isBlank()
                        ? List.of()
                        : objectMapper.readValue(interactionIdentifiersJson, new TypeReference<List<String>>() {});

                medication.setActiveIngredients(activeIngredients);
                medication.setInteractionIdentifiers(interactionIdentifiers);
                result.add(medication);
            }
        }

        return result;
    }

    private Map<String, Object> loadInteractionRulesFromSqlite(Connection connection) throws SQLException, IOException {
        String sql = "SELECT rules_json FROM interaction_rules WHERE id=1";
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                String rulesJson = normalizeText(rs.getString("rules_json"));
                if (!rulesJson.isBlank()) {
                    return objectMapper.readValue(rulesJson, new TypeReference<Map<String, Object>>() {});
                }
            }
        }
        return new LinkedHashMap<>();
    }

    private List<Patient> loadPatientsFromSqlite(Connection connection) throws SQLException, IOException {
        List<Patient> result = new ArrayList<>();
        String sql = "SELECT patient_id, first_name, last_name, date_of_birth, gender, contact_number, allergies_json, chronic_conditions_json FROM patients";
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Patient patient = new Patient();
                patient.setPatientId(normalizeText(rs.getString("patient_id")));
                patient.setFirstName(normalizeText(rs.getString("first_name")));
                patient.setLastName(normalizeText(rs.getString("last_name")));
                patient.setGender(normalizeText(rs.getString("gender")));
                patient.setContactNumber(normalizeText(rs.getString("contact_number")));

                String dobRaw = normalizeText(rs.getString("date_of_birth"));
                if (!dobRaw.isBlank()) {
                    try {
                        patient.setDateOfBirth(LocalDate.parse(dobRaw));
                    } catch (DateTimeParseException ignored) {
                        // Keep null when the legacy value format is not parseable.
                    }
                }

                String allergiesJson = normalizeText(rs.getString("allergies_json"));
                String conditionsJson = normalizeText(rs.getString("chronic_conditions_json"));

                List<String> allergies = allergiesJson.isBlank()
                        ? List.of()
                        : objectMapper.readValue(allergiesJson, new TypeReference<List<String>>() {});
                List<String> conditions = conditionsJson.isBlank()
                        ? List.of()
                        : objectMapper.readValue(conditionsJson, new TypeReference<List<String>>() {});

                patient.setAllergies(allergies);
                patient.setChronicConditions(conditions);
                result.add(patient);
            }
        }
        return result;
    }

    private List<Prescription> loadPrescriptionsFromSqlite(Connection connection, List<Medication> meds)
            throws SQLException, IOException {
        List<Prescription> result = new ArrayList<>();
        Map<String, Medication> medicationById = new HashMap<>();
        for (Medication medication : meds) {
            medicationById.put(medication.getMedicationId(), medication);
        }

        String sql = "SELECT prescription_id, patient_id, created_at, prescribed_by, status, alerts_json FROM prescriptions";
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Prescription prescription = new Prescription();
                prescription.setPrescriptionId(normalizeText(rs.getString("prescription_id")));
                prescription.setPatientId(normalizeText(rs.getString("patient_id")));
                prescription.setPrescribedBy(normalizeText(rs.getString("prescribed_by")));

                String createdAtRaw = normalizeText(rs.getString("created_at"));
                if (!createdAtRaw.isBlank()) {
                    try {
                        prescription.setCreatedAt(LocalDateTime.parse(createdAtRaw));
                    } catch (DateTimeParseException ignored) {
                        prescription.setCreatedAt(LocalDateTime.now());
                    }
                }

                String statusRaw = normalizeText(rs.getString("status"));
                if (!statusRaw.isBlank()) {
                    try {
                        prescription.setStatus(PrescriptionStatus.valueOf(statusRaw));
                    } catch (IllegalArgumentException ignored) {
                        prescription.setStatus(PrescriptionStatus.DRAFT);
                    }
                } else {
                    prescription.setStatus(PrescriptionStatus.DRAFT);
                }

                String alertsJson = normalizeText(rs.getString("alerts_json"));
                List<InteractionAlert> alerts = alertsJson.isBlank()
                        ? List.of()
                        : objectMapper.readValue(alertsJson, new TypeReference<List<InteractionAlert>>() {});
                prescription.setAlerts(alerts);

                prescription.setPrescribedDrugs(loadPrescribedDrugsForPrescription(connection, prescription.getPrescriptionId(), medicationById));
                result.add(prescription);
            }
        }

        return result;
    }

    private List<PrescribedDrug> loadPrescribedDrugsForPrescription(
            Connection connection,
            String prescriptionId,
            Map<String, Medication> medicationById
    ) throws SQLException {
        List<PrescribedDrug> result = new ArrayList<>();
        String sql = "SELECT medication_id, dosage, frequency, duration, special_instructions, prescribed_by FROM prescribed_drugs WHERE prescription_id = ? ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prescriptionId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    PrescribedDrug drug = new PrescribedDrug();
                    String medicationId = normalizeText(rs.getString("medication_id"));
                    drug.setMedicationId(medicationId);
                    drug.setMedication(medicationById.get(medicationId));
                    drug.setDosage(normalizeText(rs.getString("dosage")));
                    drug.setFrequency(normalizeText(rs.getString("frequency")));
                    drug.setDuration(normalizeText(rs.getString("duration")));
                    drug.setSpecialInstructions(normalizeText(rs.getString("special_instructions")));
                    drug.setPrescribedBy(normalizeText(rs.getString("prescribed_by")));
                    result.add(drug);
                }
            }
        }
        return result;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private <T> T loadData(String filePath, TypeReference<T> typeRef) {
        String relativeResourcePath = normalizeResourcePath(filePath);

        if (useExternalDataFallback) {
            File externalFile = new File(Paths.get(externalDataRoot, relativeResourcePath).toString());
            if (externalFile.exists() && externalFile.isFile()) {
                try {
                    return objectMapper.readValue(externalFile, typeRef);
                } catch (Exception e) {
                    System.err.println("Failed reading external data file, falling back to bundled resources: " + externalFile.getPath());
                }
            }
        }

        try (InputStream inputStream = DataService.class.getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Cannot find resource file: " + filePath);
            }
            return objectMapper.readValue(inputStream, typeRef);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load data from " + filePath, e);
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

        String lowerCaseTerm = searchTerm.toLowerCase().trim();
        List<Medication> directMatches = medications.stream()
                .filter(m -> m.getGenericName().toLowerCase().contains(lowerCaseTerm) ||
                     (m.getBrandName() != null && m.getBrandName().toLowerCase().contains(lowerCaseTerm)) ||
                     (m.getRxNormCode() != null && m.getRxNormCode().toLowerCase().contains(lowerCaseTerm)))
                .collect(Collectors.toList());

        if (!directMatches.isEmpty()) {
            return directMatches;
        }

        return medicationSearchEngine.suggest(searchTerm, 8);
    }

    public List<Medication> suggestMedications(String searchTerm, int limit) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllMedications();
        }
        return medicationSearchEngine.suggest(searchTerm, limit);
    }

    public String autoCorrectMedicationName(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return "";
        }

        Optional<String> corrected = medicationSearchEngine.autoCorrect(searchTerm);
        return corrected.orElse(searchTerm.trim());
    }

    public void savePatient(Patient patient) {
        patients.add(patient);
        saveAllData();
    }

    public void updatePatient(Patient patient) {
        saveAllData();
    }

    public void deletePatient(Patient patient) {
        patients.remove(patient);
        prescriptions.removeIf(p -> p.getPatientId().equals(patient.getPatientId()));
        saveAllData();
    }

    public void savePrescription(Prescription prescription) {
        prescriptions.removeIf(p -> p.getPatientId().equals(prescription.getPatientId()));
        prescriptions.add(prescription);
        saveAllData();
    }

    public boolean addMedicationToExistingPrescription(String patientId, Medication medication, String dosage, String frequency, String duration, String prescribingPhysician) {
        Prescription existingPrescription = prescriptions.stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .findFirst()
                .orElse(null);
        
        if (existingPrescription != null) {
            existingPrescription.addPrescribedDrug(new com.audino.model.PrescribedDrug(
                medication, dosage, frequency, duration, "", prescribingPhysician));
            saveAllData();
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
    
    public void saveAllData() {
        saveAllData(this.patients, this.prescriptions);
    }
    
    public void saveAllData(List<Patient> currentPatients, List<Prescription> currentPrescriptions) {
        this.patients = new ArrayList<>(currentPatients != null ? currentPatients : List.of());
        this.prescriptions = new ArrayList<>(currentPrescriptions != null ? currentPrescriptions : List.of());

        if (!useExternalDataFallback) {
            try {
                saveDataToFile(this.patients, config.getPatientsDataFile());
                saveDataToFile(this.prescriptions, config.getPrescriptionsDataFile());
                System.out.println("All data saved successfully.");
            } catch (Exception e) {
                System.err.println("Error saving data: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }

        try {
            persistPatientAndPrescriptionDataToSqlite(this.patients, this.prescriptions);
            System.out.println("All data saved successfully.");
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void persistPatientAndPrescriptionDataToSqlite(List<Patient> patientsToPersist, List<Prescription> prescriptionsToPersist)
            throws SQLException, IOException {
        try (Connection connection = openSqliteConnectionForWrite()) {
            connection.createStatement().execute("PRAGMA foreign_keys = ON");

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM prescribed_drugs");
                statement.executeUpdate("DELETE FROM prescriptions");
                statement.executeUpdate("DELETE FROM patients");
            }

            String patientSql = "INSERT INTO patients (patient_id, first_name, last_name, date_of_birth, gender, contact_number, allergies_json, chronic_conditions_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(patientSql)) {
                for (Patient patient : patientsToPersist) {
                    statement.setString(1, normalizeText(patient.getPatientId()));
                    statement.setString(2, normalizeText(patient.getFirstName()));
                    statement.setString(3, normalizeText(patient.getLastName()));
                    statement.setString(4, patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : "");
                    statement.setString(5, normalizeText(patient.getGender()));
                    statement.setString(6, normalizeText(patient.getContactNumber()));
                    statement.setString(7, objectMapper.writeValueAsString(patient.getAllergies()));
                    statement.setString(8, objectMapper.writeValueAsString(patient.getChronicConditions()));
                    statement.addBatch();
                }
                statement.executeBatch();
            }

            String prescriptionSql = "INSERT INTO prescriptions (prescription_id, patient_id, created_at, prescribed_by, status, alerts_json) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(prescriptionSql)) {
                for (Prescription prescription : prescriptionsToPersist) {
                    statement.setString(1, normalizeText(prescription.getPrescriptionId()));
                    statement.setString(2, normalizeText(prescription.getPatientId()));
                    statement.setString(3, prescription.getCreatedAt() != null ? prescription.getCreatedAt().toString() : "");
                    statement.setString(4, normalizeText(prescription.getPrescribedBy()));
                    statement.setString(5, prescription.getStatus() != null ? prescription.getStatus().name() : PrescriptionStatus.DRAFT.name());
                    statement.setString(6, objectMapper.writeValueAsString(prescription.getAlerts()));
                    statement.addBatch();
                }
                statement.executeBatch();
            }

            String prescribedDrugSql = "INSERT INTO prescribed_drugs (prescription_id, medication_id, dosage, frequency, duration, special_instructions, prescribed_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(prescribedDrugSql)) {
                for (Prescription prescription : prescriptionsToPersist) {
                    for (PrescribedDrug drug : prescription.getPrescribedDrugs()) {
                        statement.setString(1, normalizeText(prescription.getPrescriptionId()));
                        String medicationId = normalizeText(drug.getMedicationId());
                        if (medicationId.isBlank() && drug.getMedication() != null) {
                            medicationId = normalizeText(drug.getMedication().getMedicationId());
                        }
                        statement.setString(2, medicationId);
                        statement.setString(3, normalizeText(drug.getDosage()));
                        statement.setString(4, normalizeText(drug.getFrequency()));
                        statement.setString(5, normalizeText(drug.getDuration()));
                        statement.setString(6, normalizeText(drug.getSpecialInstructions()));
                        statement.setString(7, normalizeText(drug.getPrescribedBy()));
                        statement.addBatch();
                    }
                }
                statement.executeBatch();
            }

            connection.commit();
        }
    }
    
    private <T> void saveDataToFile(T data, String resourcePath) throws Exception {
        String projectRoot = System.getProperty("user.dir");
        String relativeResourcePath = normalizeResourcePath(resourcePath);

        if (useExternalDataFallback) {
            String externalFilePath = Paths.get(externalDataRoot, relativeResourcePath).toString();
            IOException externalError = writeJsonToFile(data, externalFilePath, "external");
            if (externalError == null) {
                return;
            }
        }

        // Save to source directory (src/main/resources)
        String sourceFilePath = Paths.get(projectRoot, "src", "main", "resources", relativeResourcePath).toString();

        // Also save to target directory (target/classes) for immediate effect
        String targetFilePath = Paths.get(projectRoot, "target", "classes", relativeResourcePath).toString();

        IOException sourceError = writeJsonToFile(data, sourceFilePath, "source");
        IOException targetError = writeJsonToFile(data, targetFilePath, "target");

        if (sourceError != null && targetError != null) {
            IOException combined = new IOException(
                    "Failed to write data to both source and target locations for " + relativeResourcePath);
            combined.addSuppressed(sourceError);
            combined.addSuppressed(targetError);
            throw combined;
        }
    }

    private String resolveExternalDataRoot() {
        String configuredPath = System.getProperty("audino.data.dir");
        if (configuredPath != null && !configuredPath.isBlank()) {
            return configuredPath.trim();
        }

        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            return Paths.get(localAppData, "Audino").toString();
        }

        return Paths.get(System.getProperty("user.home"), ".audino").toString();
    }

    private <T> IOException writeJsonToFile(T data, String filePath, String label) {
        try {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
            System.out.println("Saved data to " + label + ": " + filePath);
            return null;
        } catch (IOException e) {
            System.err.println("Skipping " + label + " save for " + filePath + ": " + e.getMessage());
            return e;
        }
    }

    private String normalizeResourcePath(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("resourcePath must not be blank");
        }

        String normalized = resourcePath.replace('\\', '/').trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("resourcePath must contain a relative file path");
        }

        return normalized;
    }
}