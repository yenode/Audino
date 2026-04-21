# Database Integration and Data Flow in Audino Healthcare System:

## Runtime Integration Model:
SQLite is used as the runtime persistence layer in the main application path. DataService is responsible for connection control, schema assurance, table hydration, and transactional write operations.

1. Connection open is performed through JDBC URL resolution.
2. Schema assurance is executed before read and write operations.
3. Domain hydration is performed from relational rows into model objects.
4. Save flows are wrapped in transactions for consistency.

## Startup Data Loading Flow:
Application startup is initiated in MainController and delegated to DataService. A deterministic sequence is used so that UI bindings are populated with complete in memory state.

1. DataService instance is created.
2. SQLite path is resolved from runtime properties and fallback candidates.
3. Parent directory for audino.db is created when required.
4. Schema tables are created if absent.
5. medications, interaction_rules, patients, and prescriptions are loaded.
6. PrescribedDrug rows are linked to Medication objects in memory.

## Runtime Save Flow:
Patient and prescription operations are delegated from controller to DataService. Full replacement persistence can be requested through saveAllData flow.

1. Existing in memory snapshots are synchronized from controller inputs.
2. Write transaction is opened with auto commit disabled.
3. Child rows are cleared before parent row rewrite sequence.
4. Batch insert operations are executed for all relevant entities.
5. Commit is issued after all batch operations are successful.

## Foreign Key and Cascade Behavior:
Referential behavior is controlled through foreign key declarations.

1. prescriptions.patient_id references patients.patient_id with ON DELETE CASCADE.
2. prescribed_drugs.prescription_id references prescriptions.prescription_id with ON DELETE CASCADE.
3. prescribed_drugs.medication_id references medications.medication_id.

Because cascade rules are active, child rows are removed automatically when a parent row is removed.

## Search Integration with Persistence:
Medication retrieval is performed from in memory collections that were hydrated from SQLite. Ranked suggestions are provided by MedicationSearchEngine when direct matching is insufficient.

1. Direct match checks generic name, brand name, and RxNorm code.
2. Aho Corasick token retrieval is used for candidate generation.
3. NLP based ranking is applied through Levenshtein distance and character trigram cosine similarity.
4. Token overlap weighting is applied for final rank order.

## Interaction Rule Integration:
Interaction rules are stored in interaction_rules.rules_json and consumed by strategy implementations.

1. AllergyCheckStrategy is used for allergy conflicts.
2. DrugDrugCheckStrategy is used for cross medication conflicts.
3. ConditionCheckStrategy is used for contraindication checks.
4. Aggregated alerts are returned to MainController for UI rendering.

## Runtime and Test Paths:
Runtime and test execution paths are intentionally separated for stability.

1. Runtime path uses SQLite centric loading and persistence.
2. Standard unit tests can use fixture mode through test classpath controls.
3. Dedicated runtime path tests validate external SQLite directory behavior.

## Failure Handling Model:
Exceptions are surfaced with context and transaction scope is preserved.

1. Schema and connection exceptions are raised with detailed runtime messages.
2. Directory creation failures are raised before connection attempts.
3. Transactional writes are prevented from partial commit states.

## Operational Checks:
```bash
mvn test
mvn javafx:run
sqlite3 data/audino.db "SELECT COUNT(*) FROM medications;"
sqlite3 data/audino.db "SELECT medication_id, generic_name, rxnorm_code FROM medications LIMIT 10;"
```

## Diagram References:
1. ER source is provided in ER_DIAGRAM.puml.
2. Architecture source is provided in ARCHITECTURE_DIAGRAM.puml.
