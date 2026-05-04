# Database Integration and Data Flow in Audino Healthcare System:

## Runtime Integration Model:
PostgreSQL is used as the runtime persistence layer in the main application path. The connection layer leverages HikariCP connection pooling for optimal performance and connection stability. DataService is responsible for connection control, schema assurance, table hydration, and transactional write operations.

1. Connection open is performed through JDBC URL resolution. If a native PostgreSQL server is unavailable on port 5432, an Embedded PostgreSQL instance is seamlessly spun up to maintain operational continuity.
2. Schema assurance is executed before read and write operations.
3. Domain hydration is performed from relational rows into model objects.
4. Save flows are wrapped in transactions to guarantee ACID compliance.

## ACID Compliance and Transaction Flow:
Audino ensures absolute data integrity by strictly adhering to ACID (Atomicity, Consistency, Isolation, Durability) properties through PostgreSQL transactional guarantees.

1. **Atomicity**: All multi-step write operations (e.g., saving a prescription and its associated prescribed drugs) are executed within a single transaction block with `conn.setAutoCommit(false)`. They either succeed completely or are entirely rolled back.
2. **Consistency**: Foreign key constraints and unique indexes (e.g., patient ID uniqueness, cascading deletes) are natively enforced by the PostgreSQL engine.
3. **Isolation**: Read-Committed isolation level ensures that concurrent reads do not see uncommitted data, preventing dirty reads and write skew during intensive operations.
4. **Durability**: Successful commits are instantly persisted to the PostgreSQL write-ahead log (WAL) protecting data against sudden crashes.

## Authentication and Authorization:
A robust, secure authentication system has been introduced.

1. User credentials and roles (`ADMIN`, `USER`) are managed in a new `users` schema.
2. Passwords are cryptographically secured using `BCrypt` hashing algorithm with randomized salt.
3. `DataService` provides explicit routines (`authenticate`, `changePassword`) to process logins, rejecting any plaintext password comparisons.

## Billing and Pricing Engine:
Medications now support integrated billing capabilities.

1. The `medications` table tracks `price_per_unit` as a `DOUBLE PRECISION` numeric column.
2. The UI logic (`PricingPromptController`) pulls this unit price to actively calculate and present dynamic estimates to clinicians during the prescription process.

## Startup Data Loading Flow:
Application startup is initiated in MainController and delegated to DataService. A deterministic sequence is used so that UI bindings are populated with complete in memory state.

1. DataService instance is created and the HikariCP pool initializes.
2. PostgreSQL path is resolved from runtime properties and fallback candidates (Embedded Postgres fallback).
3. Schema tables are generated if absent, including indexes for fast lookup.
4. Default `admin` and `user` accounts are seeded into the database if the `users` table is empty.
5. medications, interaction_rules, patients, and prescriptions are loaded.
6. PrescribedDrug rows are linked to Medication objects in memory.

## Runtime Save Flow and Commit Protections:
Patient, medication, and prescription operations are delegated from controller to DataService.

1. Write transaction is opened with `conn.setAutoCommit(false)`.
2. Execution flows are strictly controlled. During replacement/update flows, child rows are safely purged before parent rows are updated to avoid referential integrity violations.
3. Batch insert operations (`addBatch()`, `executeBatch()`) are executed for optimal network utilization and speed.
4. Commit is issued only after all batch operations are successful. If an exception triggers anywhere in the chain, `conn.rollback()` is actively fired.

## Foreign Key and Cascade Behavior:
Referential behavior is controlled through foreign key declarations.

1. prescriptions.patient_id references patients.patient_id with ON DELETE CASCADE.
2. prescribed_drugs.prescription_id references prescriptions.prescription_id with ON DELETE CASCADE.
3. prescribed_drugs.medication_id references medications.medication_id.

## Search Integration with Persistence:
Medication retrieval is performed from in memory collections that were hydrated from PostgreSQL. Ranked suggestions are provided by MedicationSearchEngine when direct matching is insufficient.

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

1. Runtime path uses PostgreSQL centric loading and persistence.
2. Standard unit tests use fixture mode and execute against ephemeral Embedded PostgreSQL containers ensuring zero state bleed between classes via idempotent `ON CONFLICT DO NOTHING` data seeding operations.

## Failure Handling Model:
Exceptions are surfaced with context and transaction scope is preserved.

1. Schema and connection exceptions are raised with detailed runtime messages.
2. Connection pooling automatically mitigates transient network failures.
3. Transactional writes are prevented from partial commit states via active rollback triggers.

## Operational Checks:
```bash
mvn test
mvn javafx:run
psql -h localhost -p 5432 -U postgres -d postgres -c "SELECT username, role FROM users;"
psql -h localhost -p 5432 -U postgres -d postgres -c "SELECT COUNT(*) FROM medications;"
psql -h localhost -p 5432 -U postgres -d postgres -c "SELECT medication_id, generic_name, price_per_unit FROM medications LIMIT 10;"
```

## Diagram References:
1. ER source is provided in ER_DIAGRAM.puml.
2. Architecture source is provided in ARCHITECTURE_DIAGRAM.puml.
