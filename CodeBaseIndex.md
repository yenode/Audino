# Audino CodeBase Index:

## Purpose:
This index is provided as a structural map of the Audino main application. Runtime behavior, persistence behavior, interaction safety analysis, and medication retrieval intelligence are described at package and class level.

## Root Artifacts:
1. pom.xml is used for dependencies, plugins, and packaging.
2. README.md is used for execution guidance and high level architecture.
3. DataBase.md is used for relational schema and ER mapping.
4. setup.ps1 and start.ps1 are used for Windows setup and launch flow.
5. Audino.bat is used for command shell launch flow.

## Main Source Tree:
### Entry Layer:
1. src/main/java/com/audino/AudinoApplication.java is used as JavaFX entry point.

### Controller Layer:
1. src/main/java/com/audino/controller/MainController.java is used for UI orchestration.
2. src/main/java/com/audino/controller/PricingPromptController.java is used for billing and cost calculation logic.
3. Medication selection, patient selection, and save operations are coordinated in this layer.

### Model Layer:
1. Patient, Prescription, PrescribedDrug, and Medication are represented as core domain entities.
2. User is used as the security entity for authentication.
3. TabletMedication, LiquidMedication, and InjectionMedication are used as medication type specializations.
4. InteractionAlert and enum types are used for safety and workflow states.

### Service Layer:
1. DataService is used for runtime load, persistence orchestration, and secure BCrypt user authentication.
2. InteractionEngine is used for asynchronous interaction rule evaluation.
3. AllergyCheckStrategy, DrugDrugCheckStrategy, and ConditionCheckStrategy are used as strategy implementations.
4. MedicationSearchEngine is used for retrieval and ranked similarity scoring.

### Utility Layer:
1. ConfigurationManager is used for object mapper and path resolution.

## Runtime Persistence Model:
The main application runtime is backed by PostgreSQL. Schema creation is ensured by DataService on connection open. Core entities are loaded into in memory structures for UI binding and interaction analysis.

1. patients, medications, prescriptions, prescribed_drugs, and interaction_rules are used as primary tables.
2. Foreign key constraints are enforced natively by the PostgreSQL engine.
3. Transactional write flow is used for consistency during save operations.

## Medication Retrieval Engine:
MedicationSearchEngine performs candidate selection and ranking.

1. Aho Corasick algorithm is used for fast multi token candidate retrieval.
2. NLP based similarity ranking is applied with Levenshtein distance and character trigram cosine similarity.
3. Token overlap scoring is applied for final rank ordering.
4. Autocorrection suggestions are produced when confidence thresholds are met.

## Interaction Safety Engine:
InteractionEngine coordinates strategy execution and aggregates alerts.

1. Allergy strategy is used for patient allergy conflicts.
2. Drug drug strategy is used for cross medication conflicts.
3. Condition strategy is used for condition contraindications.
4. Alert severity is represented by CRITICAL, WARNING, and INFO levels.

## Resource Tree:
### UI Resources:
1. src/main/resources/fxml/MainWindow.fxml is used for main scene layout.
2. src/main/resources/css/application.css is used for desktop styling.

### Seed and Rule Resources:
1. src/main/resources/data/medications.json is used for medication seed data.
2. src/main/resources/data/patients.json is used for patient seed data.
3. src/main/resources/data/interaction-rules.json is used for safety rule payloads.

## Test Tree:
1. src/test/java/com/audino/model contains model level tests.
2. src/test/java/com/audino/service contains service and search tests.
3. DataServiceTest verifies load, query, and runtime PostgreSQL path behavior.
4. MedicationSearchEngineTest verifies ranking and autocorrection behavior.

## Documentation Tree:
1. documentation/ER_DIAGRAM.puml contains detailed ER source.
2. documentation/ARCHITECTURE_DIAGRAM.puml contains detailed architecture source.
3. documentation/DATABASE_INTEGRATION.md contains runtime persistence flow.
4. documentation/CLASS_DIAGRAM.md contains class relationship narrative.

## Visual Architecture References:
### System Architecture Diagram:
![System Architecture Diagram](visuals/SystemArchitectureDiagram.png)

### Entity Relationship Diagram:
![Entity Relationship Diagram](visuals/ERDiagram.png)

### Class Diagram:
![Class Diagram](visuals/ClassDiagram.png)

### Object Diagram:
![Object Diagram](visuals/ObjectDiagram.png)

### Sequence Diagram:
![Sequence Diagram](visuals/SequenceDiagram.png)

### Activity Diagram:
![Activity Diagram](visuals/ActivityDiagram.png)
