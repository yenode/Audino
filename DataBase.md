# Audino Database Documentation:

## Overview:
Audino uses SQLite as the runtime persistence engine.

- The persistence layer is implemented in DataService.
- SQLite is embedded and does not require a separate server process.
- Baseline seed data is created directly by DataService when the database is empty.

## Database Architecture:

### Runtime Persistence Model:
- Patient records are stored in relational tables.
- Medication records are stored in relational tables.
- Prescription records and prescribed drug rows are stored in relational tables.
- Interaction rules are stored in the database as JSON payloads.

### Bootstrap Data Strategy:
- The bootstrap process runs automatically during startup.
- Baseline records initialize medications, patients, and interaction rules.
- After bootstrap, runtime reads and writes are fully handled by SQLite.

### Data Access Layer Responsibilities:
- Open and configure SQLite connections.
- Enable foreign key constraints.
- Create schema objects when they do not exist.
- Execute transactional persistence operations.
- Hydrate domain entities for controller and interaction services.

## RDBMS Details:

- Database engine: SQLite.
- JDBC driver: org.xerial:sqlite-jdbc.
- JDBC URL format: jdbc:sqlite:<absolute-path>.
- Foreign key enforcement: Enabled via PRAGMA foreign_keys = ON.

## Database Location and Configuration:

- Default database path: data/audino.db.
- Property key: sqlite.database.path in application.properties.
- Runtime override: JVM system property audino.sqlite.path.

### Primary Configuration Keys:
- sqlite.database.path=data/audino.db.

### Example Configuration:

```properties
sqlite.database.path=data/audino.db
```

### Example Test Override:

```bash
mvn "-Daudino.sqlite.path=C:/temp/audino-test.db" test
```

## Initialization and Bootstrap Flow:

DataService.loadAllData executes the following sequence.

1. Opens a SQLite connection and enables foreign keys.
2. Creates tables if they do not already exist.
3. Checks whether all core tables are empty.
4. If empty, inserts baseline records directly through DataService.
5. Loads runtime entities from SQLite into in-memory collections.

## Schema:

### Patients Table:

```sql
CREATE TABLE IF NOT EXISTS patients (
    patient_id TEXT PRIMARY KEY,
    first_name TEXT,
    last_name TEXT,
    date_of_birth TEXT,
    gender TEXT,
    contact_number TEXT,
    allergies_json TEXT,
    chronic_conditions_json TEXT
);
```

### Medications Table:

```sql
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
);
```

### Interaction Rules Table:

```sql
CREATE TABLE IF NOT EXISTS interaction_rules (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    rules_json TEXT NOT NULL
);
```

### Prescriptions Table:

```sql
CREATE TABLE IF NOT EXISTS prescriptions (
    prescription_id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL UNIQUE,
    created_at TEXT,
    prescribed_by TEXT,
    status TEXT,
    alerts_json TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE
);
```

### Prescribed Drugs Table:

```sql
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
);
```

## Relational Data Model:

### Core Tables:
- patients.
- medications.
- prescriptions.
- prescribed_drugs.
- interaction_rules.

### Relationship Model:
- One-to-one patient-to-active-prescription behavior is enforced with a unique patient_id in prescriptions.
- One-to-many relationship links prescriptions to prescribed_drugs.
- Many-to-many medication interaction logic is represented through rule payloads and interaction identifiers.

### Business Rule Enforcement:
- Deleting a patient cascades to linked prescriptions and prescribed drugs.
- Saving a prescription replaces conflicting patient prescription rows according to one-active-prescription policy.
- Persistence operations are wrapped in transactions to avoid partial writes.

## Application Data Flow:

### Startup Sequence:
1. MainController initializes DataService.
2. DataService opens SQLite and ensures schema availability.
3. DataService checks whether core tables are empty.
4. If empty, DataService seeds baseline data directly in SQLite.
5. DataService loads entities from SQLite into memory.
6. MainController binds loaded entities to the JavaFX UI.

### Save Sequence for Prescriptions:
1. MainController validates form state and interaction outcomes.
2. MainController delegates save operation to DataService.
3. DataService opens a transaction and upserts prescription metadata.
4. DataService rewrites child prescribed_drugs rows for consistency.
5. DataService commits transaction and refreshes in-memory state.

### Save Sequence for Patients:
1. MainController collects patient details from dialog UI.
2. DataService upserts patient row by patient_id.
3. DataService commits changes and updates memory snapshot.

## Data Modeling Notes:

- Patients, medications, prescriptions, and prescribed_drugs are relationally modeled.
- Interaction rules, alerts, allergies, chronic conditions, and identifier lists are stored as JSON text payloads.
- A unique constraint on prescriptions.patient_id enforces one active prescription record per patient.

## CRUD and Transaction Behavior:

- Patient create and update operations use UPSERT by patient_id.
- Patient delete cascades to prescription and prescribed drug rows through foreign keys.
- Prescription save removes conflicting patient prescription rows, upserts the current row, and replaces child prescribed_drugs rows in a transaction.
- saveAllData performs full replacement in a transaction for consistency.

## Data Integrity and Reliability:

### Current Safeguards:
- Constructor and model-level validation guard invalid payloads.
- Unique and foreign key constraints enforce relational consistency.
- Transaction rollback protects against partial updates.
- Controlled bootstrap logic prevents accidental reseeding over existing runtime data.

### Recommended Enhancements:
- Add versioned schema migration scripts for future releases.
- Add periodic backup and restore workflows.
- Add supplemental indexes for high-frequency query paths.
- Add audit trails for regulatory and traceability requirements.

## Performance Characteristics:

### Current Behavior:
- Reads use relational queries and targeted hydration.
- Writes use transactional SQL operations instead of full-file rewrites.
- Data loading is deterministic and isolated per operation.

### Optimization Opportunities:
- Add strategic caching for medication and interaction rule lookups.
- Add query profiling and index tuning for larger clinics.
- Add background maintenance jobs for archival scenarios.

## Security Considerations:

### Existing Posture:
- Desktop-only local runtime model with no direct network exposure.
- Local filesystem controls govern access to database files.
- Input sanitization and typed models reduce malformed data risks.

### Production-Grade Recommendations:
- Add encryption-at-rest for sensitive patient fields.
- Add role-based access control if multi-user mode is introduced.
- Add secure backup retention and recovery procedures.

## Testing Strategy:

### Current Coverage:
- Unit tests validate DataService CRUD behavior.
- Integration tests validate interaction processing with persisted data.
- Test suites use isolated temporary SQLite database paths.

### Recommended Additions:
- Add schema migration tests between released versions.
- Add concurrency tests for simultaneous persistence requests.
- Add large-dataset performance regression tests.

## SQLite Integration in Application Layers:

- ConfigurationManager resolves database path and runtime overrides.
- DataService owns schema creation, bootstrap logic, relational reads, and writes.
- MainController continues calling DataService methods without API disruption.
- Interaction strategies consume in-memory entities loaded from SQLite.

## Operational Commands:

### Run Application:

```bash
mvn javafx:run
```

### Run Tests:

```bash
mvn test
```

### Inspect Database Manually:

```bash
sqlite3 data/audino.db ".tables"
sqlite3 data/audino.db "SELECT COUNT(*) FROM patients;"
```

## Validation Summary:

- SQLite integration compiles and runs in application startup path.
- Full test suite passes in a clean execution environment.
- Workspace-specific file lock issues can affect local Maven output directories, but do not indicate database defects.

## Migration Summary:

### Previous State:
- Runtime persistence relied on direct JSON CRUD operations.

### Current State:
- Runtime persistence uses SQLite relational CRUD operations.
- Baseline records are seeded directly by DataService when required.

### Result:
The current architecture provides stronger consistency, better maintainability, and safer transaction handling for healthcare data operations.
