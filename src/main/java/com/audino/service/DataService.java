package com.audino.service;

import com.audino.model.*;
import org.mindrot.jbcrypt.BCrypt;
import com.audino.model.User;
import com.audino.util.ConfigurationManager;
import com.audino.util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.io.InputStream;

public class DataService {

    private static final TypeReference<Map<String, Object>> RULES_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<InteractionAlert>> ALERT_LIST_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final ConfigurationManager config;
    private List<Patient> patients = new ArrayList<>();
    private List<Medication> medications = new ArrayList<>();
    private List<Prescription> prescriptions = new ArrayList<>();
    private Map<String, Object> interactionRules = new HashMap<>();
    private MedicationSearchEngine medicationSearchEngine = new MedicationSearchEngine(List.of());

    public DataService() {
        this.config = ConfigurationManager.getInstance();
        this.objectMapper = config.getObjectMapper();
    }

    private Connection getConnection() throws SQLException {
        return config.getDataSource().getConnection();
    }

    public void loadAllData() {
        try (Connection conn = getConnection()) {
            ensureDatabaseSchema(conn);
            medications = loadMedicationsFromDb(conn);
            interactionRules = loadInteractionRulesFromDb(conn);
            patients = loadPatientsFromDb(conn);
            prescriptions = loadPrescriptionsFromDb(conn, buildMedicationIndex(medications));
            medicationSearchEngine = new MedicationSearchEngine(medications);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Audino runtime data from Postgres: " + e.getMessage(), e);
        }
    }

    private void ensureDatabaseSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS patients (patient_id VARCHAR(50) PRIMARY KEY, first_name VARCHAR(100), last_name VARCHAR(100), date_of_birth VARCHAR(50), gender VARCHAR(20), contact_number VARCHAR(50), version INTEGER DEFAULT 0)");
            statement.execute("CREATE TABLE IF NOT EXISTS patient_allergies (patient_id VARCHAR(50) REFERENCES patients(patient_id) ON DELETE CASCADE, allergy_name VARCHAR(255), PRIMARY KEY (patient_id, allergy_name))");
            statement.execute("CREATE TABLE IF NOT EXISTS patient_conditions (patient_id VARCHAR(50) REFERENCES patients(patient_id) ON DELETE CASCADE, condition_name VARCHAR(255), PRIMARY KEY (patient_id, condition_name))");
            
            statement.execute("CREATE TABLE IF NOT EXISTS medications (medication_id VARCHAR(50) PRIMARY KEY, generic_name VARCHAR(255), brand_name VARCHAR(255), rxnorm_code VARCHAR(50), medication_type VARCHAR(50), strength VARCHAR(50), concentration VARCHAR(50), route VARCHAR(50), price_per_unit NUMERIC(10,2), version INTEGER DEFAULT 0)");
            statement.execute("CREATE TABLE IF NOT EXISTS medication_ingredients (medication_id VARCHAR(50) REFERENCES medications(medication_id) ON DELETE CASCADE, ingredient_name VARCHAR(255), PRIMARY KEY (medication_id, ingredient_name))");
            statement.execute("CREATE TABLE IF NOT EXISTS medication_identifiers (medication_id VARCHAR(50) REFERENCES medications(medication_id) ON DELETE CASCADE, identifier_value VARCHAR(255), PRIMARY KEY (medication_id, identifier_value))");
            
            statement.execute("CREATE TABLE IF NOT EXISTS interaction_rules (id SERIAL PRIMARY KEY, rule_type VARCHAR(50), keyword1 VARCHAR(255), keyword2 VARCHAR(255), severity VARCHAR(50), description TEXT, recommendation TEXT)");
            statement.execute("CREATE TABLE IF NOT EXISTS users (username VARCHAR(50) PRIMARY KEY, password_hash VARCHAR(255) NOT NULL, role VARCHAR(20) NOT NULL)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_medications_name ON medications(generic_name)");
            
            // Seed default admin and user if not exists
            try (ResultSet rs = statement.executeQuery("SELECT count(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String defaultPassword = BCrypt.hashpw("1234567890", BCrypt.gensalt());
                    String insert = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
                    try (PreparedStatement ps = connection.prepareStatement(insert)) {
                        ps.setString(1, "admin"); ps.setString(2, defaultPassword); ps.setString(3, "ADMIN"); ps.addBatch();
                        ps.setString(1, "user"); ps.setString(2, defaultPassword); ps.setString(3, "USER"); ps.addBatch();
                        ps.executeBatch();
                    }
                }
            }

            
            boolean isPostgres = true;
            try {
                isPostgres = connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql");
            } catch (Exception e) {}
            
            String serialType = isPostgres ? "SERIAL PRIMARY KEY" : "INTEGER PRIMARY KEY AUTOINCREMENT";
            String bigSerialType = isPostgres ? "BIGSERIAL PRIMARY KEY" : "INTEGER PRIMARY KEY AUTOINCREMENT";
            String timestampType = isPostgres ? "TIMESTAMP NOT NULL DEFAULT NOW()" : "TIMESTAMP DEFAULT CURRENT_TIMESTAMP";

            statement.execute("CREATE TABLE IF NOT EXISTS prescriptions (prescription_id VARCHAR(50) PRIMARY KEY, patient_id VARCHAR(50) NOT NULL REFERENCES patients(patient_id) ON DELETE CASCADE, created_at VARCHAR(50), prescribed_by VARCHAR(100), status VARCHAR(50), total_bill NUMERIC(10,2), version INTEGER DEFAULT 0)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_prescriptions_patient ON prescriptions(patient_id)");
            statement.execute("CREATE TABLE IF NOT EXISTS prescription_alerts (id " + serialType + ", prescription_id VARCHAR(50) REFERENCES prescriptions(prescription_id) ON DELETE CASCADE, alert_type VARCHAR(50), alert_level VARCHAR(50), message TEXT, acknowledged BOOLEAN)");
            statement.execute("CREATE TABLE IF NOT EXISTS prescribed_drugs (id " + serialType + ", prescription_id VARCHAR(50) NOT NULL REFERENCES prescriptions(prescription_id) ON DELETE CASCADE, medication_id VARCHAR(50) NOT NULL REFERENCES medications(medication_id), dosage VARCHAR(50), frequency VARCHAR(50), duration VARCHAR(50), special_instructions VARCHAR(255), prescribed_by VARCHAR(100), total_cost NUMERIC(10,2))");

            // Audit log table
            statement.execute("CREATE TABLE IF NOT EXISTS audit_logs ("
                + "id " + bigSerialType + ", "
                + "timestamp " + timestampType + ", "
                + "username VARCHAR(50), "
                + "operation VARCHAR(20) NOT NULL, "
                + "entity_type VARCHAR(50) NOT NULL, "
                + "entity_id VARCHAR(100), "
                + "details TEXT)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp DESC)");
        }
    }

    // ── Audit Logging ────────────────────────────────────────────────────

    private String currentUsername() {
        User u = SessionManager.getInstance().getCurrentUser();
        return u != null ? u.getUsername() : "SYSTEM";
    }

    private void logAudit(String operation, String entityType, String entityId, String details) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO audit_logs (timestamp, username, operation, entity_type, entity_id, details) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, currentUsername());
            ps.setString(3, operation);
            ps.setString(4, entityType);
            ps.setString(5, entityId);
            ps.setString(6, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AUDIT] Failed to write audit log: " + e.getMessage());
        }
    }

    public List<AuditLog> getAuditLogs(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT id, timestamp, username, operation, entity_type, entity_id, details FROM audit_logs ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setId(rs.getLong("id"));
                    log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    log.setUsername(rs.getString("username"));
                    log.setOperation(rs.getString("operation"));
                    log.setEntityType(rs.getString("entity_type"));
                    log.setEntityId(rs.getString("entity_id"));
                    log.setDetails(rs.getString("details"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AUDIT] Failed to read audit logs: " + e.getMessage());
        }
        return logs;
    }

    private List<Medication> loadMedicationsFromDb(Connection connection) throws SQLException {
        List<Medication> results = new ArrayList<>();
        String sql = "SELECT * FROM medications";
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Medication medication = createMedicationInstance(rs.getString("medication_type"));
                medication.setMedicationId(rs.getString("medication_id"));
                medication.setGenericName(rs.getString("generic_name"));
                medication.setBrandName(rs.getString("brand_name"));
                medication.setRxNormCode(rs.getString("rxnorm_code"));
                medication.setPricePerUnit(rs.getDouble("price_per_unit"));
                if (rs.wasNull()) medication.setPricePerUnit(null);
                medication.setVersion(rs.getInt("version"));

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
        
        for (Medication m : results) {
            m.setActiveIngredients(loadMedicationList(connection, "medication_ingredients", "ingredient_name", m.getMedicationId()));
            m.setInteractionIdentifiers(loadMedicationList(connection, "medication_identifiers", "identifier_value", m.getMedicationId()));
        }
        return results;
    }
    
    private List<String> loadMedicationList(Connection connection, String table, String column, String id) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT " + column + " FROM " + table + " WHERE medication_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) list.add(rs.getString(1));
            }
        }
        return list;
    }

    private List<Patient> loadPatientsFromDb(Connection connection) throws SQLException {
        List<Patient> results = new ArrayList<>();
        String sql = "SELECT * FROM patients";
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getString("patient_id"));
                p.setFirstName(rs.getString("first_name"));
                p.setLastName(rs.getString("last_name"));
                String dob = rs.getString("date_of_birth");
                if (dob != null && !dob.isEmpty()) p.setDateOfBirth(LocalDate.parse(dob));
                p.setGender(rs.getString("gender"));
                p.setContactNumber(rs.getString("contact_number"));
                p.setVersion(rs.getInt("version"));
                results.add(p);
            }
        }
        for (Patient p : results) {
            p.setAllergies(loadPatientList(connection, "patient_allergies", "allergy_name", p.getPatientId()));
            p.setChronicConditions(loadPatientList(connection, "patient_conditions", "condition_name", p.getPatientId()));
        }
        return results;
    }
    
    private List<String> loadPatientList(Connection connection, String table, String column, String id) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT " + column + " FROM " + table + " WHERE patient_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) list.add(rs.getString(1));
            }
        }
        return list;
    }

    private List<Prescription> loadPrescriptionsFromDb(Connection connection, Map<String, Medication> medicationsById) throws Exception {
        List<Prescription> results = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions";
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Prescription p = new Prescription();
                p.setPrescriptionId(rs.getString("prescription_id"));
                p.setPatientId(rs.getString("patient_id"));
                String createdAt = rs.getString("created_at");
                if (createdAt != null && !createdAt.isEmpty()) p.setCreatedAt(LocalDateTime.parse(createdAt));
                p.setPrescribedBy(rs.getString("prescribed_by"));
                try {
                    p.setStatus(PrescriptionStatus.valueOf(rs.getString("status")));
                } catch (Exception e) {
                    p.setStatus(PrescriptionStatus.DRAFT);
                }
                p.setVersion(rs.getInt("version"));
                
                p.setAlerts(loadPrescriptionAlerts(connection, p.getPrescriptionId()));
                p.setPrescribedDrugs(loadPrescribedDrugs(connection, p.getPrescriptionId(), medicationsById));
                results.add(p);
            }
        }
        return results;
    }

    private List<InteractionAlert> loadPrescriptionAlerts(Connection connection, String prescriptionId) throws SQLException {
        List<InteractionAlert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM prescription_alerts WHERE prescription_id = ? ORDER BY id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, prescriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InteractionAlert a = new InteractionAlert();
                    try { a.setAlertType(com.audino.model.AlertType.valueOf(rs.getString("alert_type"))); } catch(Exception e){}
                    try { a.setAlertLevel(com.audino.model.AlertLevel.valueOf(rs.getString("alert_level"))); } catch(Exception e){}
                    a.setMessage(rs.getString("message"));
                    a.setAcknowledged(rs.getBoolean("acknowledged"));
                    alerts.add(a);
                }
            }
        }
        return alerts;
    }

    private List<PrescribedDrug> loadPrescribedDrugs(Connection connection, String prescriptionId, Map<String, Medication> medicationsById) throws SQLException {
        List<PrescribedDrug> drugs = new ArrayList<>();
        String sql = "SELECT * FROM prescribed_drugs WHERE prescription_id = ? ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prescriptionId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    PrescribedDrug drug = new PrescribedDrug();
                    drug.setMedicationId(rs.getString("medication_id"));
                    drug.setDosage(rs.getString("dosage"));
                    drug.setFrequency(rs.getString("frequency"));
                    drug.setDuration(rs.getString("duration"));
                    drug.setSpecialInstructions(rs.getString("special_instructions"));
                    drug.setPrescribedBy(rs.getString("prescribed_by"));
                    drug.setTotalCost(rs.getDouble("total_cost"));
                    if (rs.wasNull()) drug.setTotalCost(null);
                    drug.setMedication(medicationsById.get(drug.getMedicationId()));
                    drugs.add(drug);
                }
            }
        }
        return drugs;
    }
    
    private Map<String, Object> loadInteractionRulesFromDb(Connection connection) throws Exception {
        Map<String, Object> rules = new HashMap<>();
        Map<String, Object> dd = new HashMap<>();
        Map<String, Object> da = new HashMap<>();
        Map<String, Object> dc = new HashMap<>();
        
        String sql = "SELECT * FROM interaction_rules";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String type = rs.getString("rule_type");
                String k1 = rs.getString("keyword1");
                String k2 = rs.getString("keyword2");
                String sev = rs.getString("severity");
                String desc = rs.getString("description");
                String rec = rs.getString("recommendation");
                String ruleId = "rule" + rs.getInt("id");
                
                Map<String, Object> node = new HashMap<>();
                node.put("severity", sev);
                node.put("description", desc);
                node.put("recommendation", rec);
                
                if ("DRUG_DRUG".equals(type)) {
                    node.put("drug1", Arrays.asList(k1.split(",")));
                    node.put("drug2", Arrays.asList(k2.split(",")));
                    dd.put(ruleId, node);
                } else if ("DRUG_ALLERGY".equals(type)) {
                    node.put("allergyKeywords", Arrays.asList(k1.split(",")));
                    node.put("medicationClasses", Arrays.asList(k2.split(",")));
                    da.put(ruleId, node);
                } else if ("DRUG_CONDITION".equals(type)) {
                    node.put("conditionKeywords", Arrays.asList(k1.split(",")));
                    node.put("medicationClasses", Arrays.asList(k2.split(",")));
                    dc.put(ruleId, node);
                }
            }
        }
        rules.put("drugDrugInteractions", dd);
        rules.put("drugAllergyInteractions", da);
        rules.put("drugConditionInteractions", dc);
        return rules;
    }

    private Map<String, Medication> buildMedicationIndex(List<Medication> medicationList) {
        return medicationList.stream().collect(Collectors.toMap(Medication::getMedicationId, Function.identity(), (a, b) -> a));
    }

    private Medication createMedicationInstance(String typeValue) {
        MedicationType type;
        try {
            type = MedicationType.valueOf(typeValue);
        } catch (Exception e) {
            type = MedicationType.TABLET;
        }
        return switch (type) {
            case TABLET -> new TabletMedication();
            case LIQUID -> new LiquidMedication();
            case INJECTION -> new InjectionMedication();
        };
    }

    // --- Public API ---

    public List<Patient> getAllPatients() { return new ArrayList<>(patients); }
    public List<Medication> getAllMedications() { return new ArrayList<>(medications); }
    public List<Prescription> getAllPrescriptions() { return new ArrayList<>(prescriptions); }
    public Map<String, Object> getInteractionRules() { return interactionRules; }

    public List<Patient> searchPatients(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) return getAllPatients();
        String lowerCaseTerm = searchTerm.toLowerCase();
        return patients.stream().filter(p -> p.getFullName().toLowerCase().contains(lowerCaseTerm)).collect(Collectors.toList());
    }

    public List<Medication> searchMedications(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) return getAllMedications();
        String lowerCaseTerm = searchTerm.toLowerCase().trim();
        List<Medication> directMatches = medications.stream()
                .filter(m -> m.getGenericName().toLowerCase().contains(lowerCaseTerm) ||
                     (m.getBrandName() != null && m.getBrandName().toLowerCase().contains(lowerCaseTerm)) ||
                     (m.getRxNormCode() != null && m.getRxNormCode().toLowerCase().contains(lowerCaseTerm)))
                .collect(Collectors.toList());
        if (!directMatches.isEmpty()) return directMatches;
        return medicationSearchEngine.suggest(searchTerm, 8);
    }

    public List<Medication> suggestMedications(String searchTerm, int limit) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) return getAllMedications();
        return medicationSearchEngine.suggest(searchTerm, limit);
    }

    public String autoCorrectMedicationName(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) return "";
        return medicationSearchEngine.autoCorrect(searchTerm).orElse(searchTerm.trim());
    }

    // --- Saving with Transactions and Optimistic Locking ---

    
    public User authenticate(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT password_hash, role FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (BCrypt.checkpw(password, hash)) {
                        User user = new User(username, hash, rs.getString("role"));
                        logAudit("LOGIN", "SESSION", username, "User logged in (" + user.getRole() + ")");
                        return user;
                    }
                }
            }
            logAudit("LOGIN_FAILED", "SESSION", username, "Authentication failed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createUser(String username, String password, String role) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            ps.setString(3, role);
            ps.executeUpdate();
            logAudit("CREATE", "USER", username, "Created user with role " + role);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username, password_hash, role FROM users")) {
            while (rs.next()) {
                users.add(new User(rs.getString("username"), rs.getString("password_hash"), rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean checkPassword(String username, String password) {
        return authenticate(username, password) != null;
    }

    public void changePassword(String username, String newPassword) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE username = ?")) {
            ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            ps.setString(2, username);
            ps.executeUpdate();
            logAudit("UPDATE", "PASSWORD", username, "Password changed for user '" + username + "'");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update password", e);
        }
    }

    public void saveMedication(Medication medication) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                String insert = "INSERT INTO medications (medication_id, generic_name, brand_name, rxnorm_code, medication_type, price_per_unit, version) VALUES (?, ?, ?, ?, ?, ?, 1)";
                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setString(1, medication.getMedicationId());
                    ps.setString(2, medication.getGenericName());
                    ps.setString(3, medication.getBrandName());
                    ps.setString(4, medication.getRxNormCode());
                    ps.setString(5, medication.getMedicationType().name());
                    if (medication.getPricePerUnit() != null) ps.setDouble(6, medication.getPricePerUnit());
                    else ps.setNull(6, Types.NUMERIC);
                    ps.executeUpdate();
                }
                conn.commit();
                medications.add(medication);
                medicationSearchEngine = new MedicationSearchEngine(medications);
                logAudit("CREATE", "MEDICATION", medication.getMedicationId(),
                    "Added medication '" + medication.getGenericName() + "' ($" + medication.getPricePerUnit() + ")");
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Failed to save new medication: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public void savePatient(Patient patient) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean exists = false;
                int currentVersion = 0;
                try (PreparedStatement check = conn.prepareStatement("SELECT version FROM patients WHERE patient_id = ?")) {
                    check.setString(1, patient.getPatientId());
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            exists = true;
                            currentVersion = rs.getInt("version");
                        }
                    }
                }
                
                if (exists) {
                    if (currentVersion != patient.getVersion()) {
                        throw new ConcurrentModificationException("Patient was modified by another user. Please refresh and try again.");
                    }
                    int newVersion = currentVersion + 1;
                    String update = "UPDATE patients SET first_name=?, last_name=?, date_of_birth=?, gender=?, contact_number=?, version=? WHERE patient_id=? AND version=?";
                    try (PreparedStatement ps = conn.prepareStatement(update)) {
                        ps.setString(1, patient.getFirstName());
                        ps.setString(2, patient.getLastName());
                        ps.setString(3, patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null);
                        ps.setString(4, patient.getGender());
                        ps.setString(5, patient.getContactNumber());
                        ps.setInt(6, newVersion);
                        ps.setString(7, patient.getPatientId());
                        ps.setInt(8, currentVersion);
                        int rows = ps.executeUpdate();
                        if (rows == 0) throw new ConcurrentModificationException("Patient update failed due to concurrent modification.");
                        patient.setVersion(newVersion);
                    }
                } else {
                    String insert = "INSERT INTO patients (patient_id, first_name, last_name, date_of_birth, gender, contact_number, version) VALUES (?, ?, ?, ?, ?, ?, 1)";
                    try (PreparedStatement ps = conn.prepareStatement(insert)) {
                        ps.setString(1, patient.getPatientId());
                        ps.setString(2, patient.getFirstName());
                        ps.setString(3, patient.getLastName());
                        ps.setString(4, patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null);
                        ps.setString(5, patient.getGender());
                        ps.setString(6, patient.getContactNumber());
                        ps.executeUpdate();
                        patient.setVersion(1);
                    }
                }
                
                // Update lists
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM patient_allergies WHERE patient_id=?")) {
                    ps.setString(1, patient.getPatientId()); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO patient_allergies (patient_id, allergy_name) VALUES (?, ?)")) {
                    for (String allergy : patient.getAllergies()) {
                        ps.setString(1, patient.getPatientId()); ps.setString(2, allergy); ps.addBatch();
                    }
                    ps.executeBatch();
                }
                
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM patient_conditions WHERE patient_id=?")) {
                    ps.setString(1, patient.getPatientId()); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO patient_conditions (patient_id, condition_name) VALUES (?, ?)")) {
                    for (String condition : patient.getChronicConditions()) {
                        ps.setString(1, patient.getPatientId()); ps.setString(2, condition); ps.addBatch();
                    }
                    ps.executeBatch();
                }
                
                conn.commit();
                
                patients.remove(patient);
                patients.add(patient);
                logAudit(exists ? "UPDATE" : "CREATE", "PATIENT", patient.getPatientId(),
                    (exists ? "Updated" : "Created") + " patient '" + patient.getFullName() + "'");
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Failed to save patient: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }


    public void updateMedication(Medication medication) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                String update = "UPDATE medications SET generic_name=?, brand_name=?, price_per_unit=?, version=version+1 WHERE medication_id=?";
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setString(1, medication.getGenericName());
                    ps.setString(2, medication.getBrandName());
                    if (medication.getPricePerUnit() != null) {
                        ps.setDouble(3, medication.getPricePerUnit());
                    } else {
                        ps.setNull(3, Types.NUMERIC);
                    }
                    ps.setString(4, medication.getMedicationId());
                    ps.executeUpdate();
                }
                conn.commit();
                logAudit("UPDATE", "MEDICATION", medication.getMedicationId(),
                    "Updated medication '" + medication.getGenericName() + "' (price=$" + medication.getPricePerUnit() + ")");
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Failed to update medication: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public void updatePatient(Patient patient) {
        savePatient(patient);
    }

    public void deletePatient(Patient patient) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM patients WHERE patient_id=?")) {
                ps.setString(1, patient.getPatientId());
                ps.executeUpdate();
                conn.commit();
                logAudit("DELETE", "PATIENT", patient.getPatientId(), "Deleted patient '" + patient.getFullName() + "'");
                patients.remove(patient);
                prescriptions.removeIf(p -> p.getPatientId().equals(patient.getPatientId()));
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete patient", e);
        }
    }

    public void savePrescription(Prescription prescription) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean exists = false;
                int currentVersion = 0;
                try (PreparedStatement check = conn.prepareStatement("SELECT version FROM prescriptions WHERE prescription_id = ?")) {
                    check.setString(1, prescription.getPrescriptionId());
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            exists = true;
                            currentVersion = rs.getInt("version");
                        }
                    }
                }
                
                Double totalBill = prescription.getTotalBill();
                
                if (exists) {
                    if (currentVersion != prescription.getVersion()) {
                        throw new ConcurrentModificationException("Prescription modified by another user.");
                    }
                    int newVersion = currentVersion + 1;
                    String update = "UPDATE prescriptions SET patient_id=?, created_at=?, prescribed_by=?, status=?, total_bill=?, version=? WHERE prescription_id=? AND version=?";
                    try (PreparedStatement ps = conn.prepareStatement(update)) {
                        ps.setString(1, prescription.getPatientId());
                        ps.setString(2, prescription.getCreatedAt() != null ? prescription.getCreatedAt().toString() : null);
                        ps.setString(3, prescription.getPrescribedBy());
                        ps.setString(4, prescription.getStatus() != null ? prescription.getStatus().name() : PrescriptionStatus.DRAFT.name());
                        if (totalBill != null) ps.setDouble(5, totalBill); else ps.setNull(5, Types.NUMERIC);
                        ps.setInt(6, newVersion);
                        ps.setString(7, prescription.getPrescriptionId());
                        ps.setInt(8, currentVersion);
                        int rows = ps.executeUpdate();
                        if (rows == 0) throw new ConcurrentModificationException("Prescription update failed.");
                        prescription.setVersion(newVersion);
                    }
                } else {
                    if (prescription.getPrescriptionId() == null || prescription.getPrescriptionId().isBlank()) {
                        prescription.setPrescriptionId("RX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    }
                    String insert = "INSERT INTO prescriptions (prescription_id, patient_id, created_at, prescribed_by, status, total_bill, version) VALUES (?, ?, ?, ?, ?, ?, 1)";
                    try (PreparedStatement ps = conn.prepareStatement(insert)) {
                        ps.setString(1, prescription.getPrescriptionId());
                        ps.setString(2, prescription.getPatientId());
                        ps.setString(3, prescription.getCreatedAt() != null ? prescription.getCreatedAt().toString() : null);
                        ps.setString(4, prescription.getPrescribedBy());
                        ps.setString(5, prescription.getStatus() != null ? prescription.getStatus().name() : PrescriptionStatus.DRAFT.name());
                        if (totalBill != null) ps.setDouble(6, totalBill); else ps.setNull(6, Types.NUMERIC);
                        ps.executeUpdate();
                        prescription.setVersion(1);
                    }
                }
                
                // Update prescribed drugs and alerts
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM prescribed_drugs WHERE prescription_id=?")) {
                    ps.setString(1, prescription.getPrescriptionId()); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM prescription_alerts WHERE prescription_id=?")) {
                    ps.setString(1, prescription.getPrescriptionId()); ps.executeUpdate();
                }
                
                String insertAlert = "INSERT INTO prescription_alerts (prescription_id, alert_type, alert_level, message, acknowledged) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertAlert)) {
                    for (InteractionAlert alert : prescription.getAlerts()) {
                        ps.setString(1, prescription.getPrescriptionId());
                        ps.setString(2, alert.getAlertType() != null ? alert.getAlertType().name() : null);
                        ps.setString(3, alert.getAlertLevel() != null ? alert.getAlertLevel().name() : null);
                        ps.setString(4, alert.getMessage());
                        ps.setBoolean(5, alert.isAcknowledged());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                
                String insertDrug = "INSERT INTO prescribed_drugs (prescription_id, medication_id, dosage, frequency, duration, special_instructions, prescribed_by, total_cost) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertDrug)) {
                    for (PrescribedDrug drug : prescription.getPrescribedDrugs()) {
                        String medId = drug.getMedicationId();
                        if (medId == null || medId.isBlank()) {
                            if (drug.getMedication() != null) medId = drug.getMedication().getMedicationId();
                            else continue;
                        }
                        ps.setString(1, prescription.getPrescriptionId());
                        ps.setString(2, medId);
                        ps.setString(3, drug.getDosage());
                        ps.setString(4, drug.getFrequency());
                        ps.setString(5, drug.getDuration());
                        ps.setString(6, drug.getSpecialInstructions());
                        ps.setString(7, drug.getPrescribedBy());
                        if (drug.getTotalCost() != null) ps.setDouble(8, drug.getTotalCost()); else ps.setNull(8, Types.NUMERIC);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                
                conn.commit();
                
                prescriptions.removeIf(p -> p.getPrescriptionId().equals(prescription.getPrescriptionId()));
                prescriptions.add(prescription);
                logAudit(exists ? "UPDATE" : "CREATE", "PRESCRIPTION", prescription.getPrescriptionId(),
                    (exists ? "Updated" : "Created") + " prescription for patient " + prescription.getPatientId()
                    + " ($" + String.format("%.2f", prescription.getTotalBill()) + ", " + prescription.getPrescribedDrugs().size() + " drugs)");
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Failed to save prescription: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public boolean addMedicationToExistingPrescription(String patientId, Medication medication, String dosage, String frequency, String duration, String prescribingPhysician) {
        Prescription existingPrescription = getActivePrescriberionForPatient(patientId);
        if (existingPrescription != null) {
            PrescribedDrug drug = new PrescribedDrug(medication, dosage, frequency, duration, "", prescribingPhysician);
            drug.calculateCost();
            existingPrescription.addPrescribedDrug(drug);
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
    
    public void saveAllData() {
        // Only memory operation now, as UI interactions immediately trigger saves
    }
    
    public void saveAllData(List<Patient> currentPatients, List<Prescription> currentPrescriptions) {
        // Real-time operations save individually to Postgres, batch saves are obsolete
        this.patients = new ArrayList<>(currentPatients != null ? currentPatients : List.of());
        this.prescriptions = new ArrayList<>(currentPrescriptions != null ? currentPrescriptions : List.of());
    }
}
