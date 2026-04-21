# Audino Database Documentation:

## Purpose:
This document defines the relational model used by the Audino main application. The persistence layer is implemented with SQLite and JDBC. Clinical data is stored in normalized relational tables, while rule payloads and selective lists are stored as JSON text columns where a document shape is required.

## RDBMS Engine Profile:
SQLite is used as the embedded RDBMS engine. A server process is not required. ACID semantics are provided by SQLite transactions, and referential integrity is enforced with foreign key checks.

1. Engine name is SQLite.
2. JDBC driver is org.xerial sqlite jdbc.
3. Runtime URL format is jdbc sqlite absolute path.
4. Foreign key checks are enabled with PRAGMA foreign_keys = ON.

## Database File and Runtime Resolution:
The main runtime file is resolved as data/audino.db in standard execution. A runtime override can be provided through system properties. Parent directory creation is performed before connection open so that first execution in a fresh path can proceed safely.

1. Property audino.sqlite.path is treated as explicit database file path.
2. Property audino.data.dir is treated as explicit root, and data/audino.db is derived from it.
3. Internal fallback candidates are checked from working directory variants.

## Entity Relationship Design:
The relational model is centered on patient, prescription, and medication domains. Cardinality is intentionally constrained for safe prescription flow.

1. One patient is linked to one active prescription header through a unique key in prescriptions.patient_id.
2. One prescription header is linked to many prescribed drug rows.
3. Many prescribed drug rows are linked to one medication master row.
4. Interaction rules are stored in a singleton row with id = 1.

Detailed ER source is provided in documentation/ER_DIAGRAM.puml.

## Normalization and Data Shape:
The schema is mostly in Third Normal Form for transactional entities.

1. Patient identifiers are isolated in patients.
2. Prescription headers are isolated in prescriptions.
3. Prescription items are isolated in prescribed_drugs.
4. Medication vocabulary is isolated in medications.
5. Rule payloads are stored as JSON text in interaction_rules for flexible structure.

Controlled denormalization is used for JSON payload columns.

1. allergies_json and chronic_conditions_json are used for compact patient list storage.
2. active_ingredients_json and interaction_identifiers_json are used for medication token sets.
3. alerts_json and rules_json are used for rule and alert payload storage.

## Full Schema Definitions:
### patients:
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

### medications:
```sql
CREATE TABLE IF NOT EXISTS medications (
    medication_id TEXT PRIMARY KEY,
    generic_name TEXT,
    brand_name TEXT,
    rxnorm_code TEXT,
    medication_type TEXT,
    strength TEXT,
    concentration TEXT,
    route TEXT,
    active_ingredients_json TEXT,
    interaction_identifiers_json TEXT
);
```

### prescriptions:
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

### prescribed_drugs:
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

### interaction_rules:
```sql
CREATE TABLE IF NOT EXISTS interaction_rules (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    rules_json TEXT NOT NULL
);
```

## Operational Index Plan:
The primary keys and unique keys already create essential indexes. Additional secondary indexes can be created for larger datasets.

1. Index on medications.rxnorm_code can be created for RxNorm query acceleration.
2. Index on medications.generic_name can be created for direct matching.
3. Index on prescribed_drugs.medication_id can be created for reverse medication drill down.
4. Index on prescriptions.created_at can be created for temporal reporting.

Example optional index statements are provided below.

```sql
CREATE INDEX IF NOT EXISTS idx_medications_rxnorm_code ON medications(rxnorm_code);
CREATE INDEX IF NOT EXISTS idx_medications_generic_name ON medications(generic_name);
CREATE INDEX IF NOT EXISTS idx_prescribed_drugs_medication_id ON prescribed_drugs(medication_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_created_at ON prescriptions(created_at);
```

## Transaction and Consistency Rules:
Write operations are wrapped in JDBC transactions for atomic behavior. When full replacement save is requested, parent and child rows are rewritten in controlled sequence.

1. prescribed_drugs is cleared first in replacement flow.
2. prescriptions is cleared second in replacement flow.
3. patients is cleared last in replacement pre stage.
4. Insert batches are executed for patients, prescriptions, and prescribed_drugs.
5. Commit is issued after all batches are successful.

If a failure is raised before commit, rollback semantics are provided by transaction boundaries.

## Application Layer Mapping:
The relational schema is mapped to domain classes through DataService hydration logic.

1. patients rows are mapped to Patient.
2. medications rows are mapped to Medication subclasses by medication_type.
3. prescriptions rows are mapped to Prescription.
4. prescribed_drugs rows are mapped to PrescribedDrug and linked to Medication.
5. interaction_rules row is mapped to a rules map.

## Query and Search Behavior:
Medication search is performed in two phases.

1. Direct SQL loaded in memory collections are checked through substring matching for generic, brand, and RxNorm code.
2. If direct matches are not found, MedicationSearchEngine is used for ranked suggestion generation.

The ranked suggestion flow uses Aho Corasick token retrieval and NLP based similarity ranking.

1. Levenshtein similarity is used for edit tolerance.
2. Character trigram cosine similarity is used for lexical closeness.
3. Token overlap scoring is used for final rank shaping.

## Integrity Constraints:
Relational integrity is guarded through key constraints and cascading actions.

1. patients.patient_id is the primary key.
2. prescriptions.prescription_id is the primary key.
3. prescriptions.patient_id is unique and not null.
4. prescribed_drugs.prescription_id references prescriptions.prescription_id with ON DELETE CASCADE.
5. prescribed_drugs.medication_id references medications.medication_id.

## Backup and Restore Guidance:
SQLite file copy strategy can be used when the process is not writing.

1. Application process should be stopped before file copy backup.
2. data/audino.db should be copied to a secure location.
3. Restore can be performed by placing the backup file at the runtime path.

## Validation Commands:
```bash
sqlite3 data/audino.db ".tables"
sqlite3 data/audino.db "PRAGMA foreign_keys;"
sqlite3 data/audino.db "SELECT COUNT(*) FROM medications;"
sqlite3 data/audino.db "SELECT medication_id, generic_name, rxnorm_code FROM medications ORDER BY medication_id LIMIT 10;"
```

## Diagram References:
### System Architecture Diagram:
![System Architecture Diagram](visuals/SystemArchitectureDiagram.png)

### Entity Relationship Diagram:
![Entity Relationship Diagram](visuals/ERDiagram.png)

