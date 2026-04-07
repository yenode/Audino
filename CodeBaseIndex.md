# Audino: CodeBase Index:

## Project Structure:

### Root Files:
- pom.xml: Maven project configuration and dependencies.
- README.md: Project overview and quick start guide.
- DataBase.md: Unified database architecture, schema, data flow, and SQLite integration guide.
- start.ps1: PowerShell script to run the application.
- setup.ps1: PowerShell script to set up the project environment.

### Source Code (src/main/java/com/audino):

#### Main Application:
- AudinoApplication.java: Main entry point for the JavaFX application.

#### Controller Package (controller):
- MainController.java: Handles UI interactions and connects view with business logic.

#### Model Package (model):
- AlertLevel.java: Enumeration for alert severity levels.
- AlertType.java: Enumeration for types of drug interaction alerts.
- InjectionMedication.java: Represents injectable medications.
- InteractionAlert.java: Model for drug interaction warnings.
- LiquidMedication.java: Represents liquid medications.
- Medication.java: Base abstract class for all medications.
- MedicationType.java: Enumeration for medication form types.
- Patient.java: Represents patient information and medical history.
- PrescribedDrug.java: Links medication to prescription with dosage details.
- Prescription.java: Represents a patient's prescription with multiple drugs.
- PrescriptionStatus.java: Enumeration for prescription lifecycle status.
- TabletMedication.java: Represents tablet and capsule medications.

#### Service Package (service):
- AllergyCheckStrategy.java: Strategy for checking patient allergies against medications.
- ConditionCheckStrategy.java: Strategy for checking drug-condition interactions.
- DataService.java: Handles SQLite schema initialization, bootstrap loading, entity hydration, and transactional persistence operations.
- DrugDrugCheckStrategy.java: Strategy for checking drug-drug interactions.
- InteractionCheckStrategy.java: Interface defining interaction checking strategy.
- InteractionEngine.java: Core engine orchestrating all interaction checks.

#### Utility Package (util):
- ConfigurationManager.java: Manages application configuration, properties, and runtime override settings.

### Resources (src/main/resources):

#### CSS Stylesheets (css):
- application.css: Application-wide styling definitions.

#### Data Files (data):
- audino.db: SQLite runtime database file (created at runtime based on configured path).

#### FXML Views (fxml):
- MainWindow.fxml: Main application window layout definition.
- PatientDialog.fxml: Dialog layout for creating and editing patient records.

### Test Code (src/test/java/com/audino):

#### Model Tests (model):
- MedicationTest.java: Unit tests for medication classes.
- PatientTest.java: Unit tests for patient model.

#### Service Tests (service):
- DataServiceTest.java: Tests for data service operations.
- InteractionEngineTest.java: Tests for interaction detection logic.

#### Test Suite:
- TestSuite.java: Aggregated test suite runner.

## Technology Stack:

### Core Language and Runtime:
- Java 17+ for application logic and domain modeling.
- OpenJDK runtime for cross-platform desktop deployment.

### Frontend Layer:
- JavaFX for desktop UI rendering.
- FXML for declarative view composition.
- CSS for UI styling and visual consistency.

### Data and Persistence Layer:
- SQLite as primary runtime relational database.
- SQLite JDBC for Java-to-database connectivity.
- Java Properties for runtime configuration.

### Testing and Quality Layer:
- JUnit 5 for unit and integration tests.
- Maven Surefire for automated test execution.

### Build and Dependency Management:
- Apache Maven for dependency resolution and build lifecycle orchestration.
- Key dependencies include javafx-controls, javafx-fxml, jackson-core, jackson-databind, jackson-annotations, sqlite-jdbc, and junit-jupiter.

## Application Architecture Stack:

### Layered Architecture:
- Presentation layer handles JavaFX views and user interactions.
- Business layer applies prescription validation and interaction analysis.
- Data layer manages SQLite schema, bootstrap, and transactional persistence.

### Primary Components:
- MainController coordinates UI events and application workflows.
- DataService handles data access and persistence responsibilities.
- InteractionEngine orchestrates rule-based interaction checks.
- ConfigurationManager resolves properties and runtime overrides.

## Key Design Patterns and Principles:

### Strategy Pattern:
- Used in interaction checking with multiple pluggable strategies.
- Supports extension of interaction logic without controller rewrites.

### MVC Pattern:
- Clear separation between Model, View (FXML), and Controller.

### OOP Principles:
- Encapsulation in model classes with controlled accessors.
- Inheritance and polymorphism in medication type hierarchy.
- Abstraction across controller, service, and model layers.

### Observer-Style UI Synchronization:
- JavaFX bindings and listeners keep UI synchronized with state changes.

## Data Flow:

1. Application starts from AudinoApplication.java.
2. DataService initializes SQLite schema and seeds baseline records directly if database is empty.
3. MainController binds loaded data to UI components and handles user actions.
4. DataService executes transactional CRUD for runtime patient and prescription data.
5. InteractionEngine evaluates interactions via strategy implementations.
6. Alerts are generated and displayed in the UI.

## Performance, Security, and Operations:

### Performance Characteristics:
- Relational storage reduces full-file rewrite overhead.
- Transaction boundaries improve multi-step write consistency.
- Bootstrap is one-time per empty database and then runtime is query-driven.

### Security Posture:
- Desktop local runtime with no direct network exposure.
- Database access constrained by local filesystem permissions.
- Input validation reduces malformed data persistence.

### Deployment and Configuration:
- Maven packaging supports repeatable builds.
- Platform launch scripts simplify local execution.
- sqlite.database.path controls default database location.
- audino.sqlite.path supports runtime override for testing and isolation.

## Extension Points:

- Add new medication types by extending Medication.
- Add new interaction checks via InteractionCheckStrategy.
- Expand data sources and persistence optimizations in DataService.
- Customize UI behavior through FXML and CSS.

## User Interface Screenshots:

### Main Application Window:
![Main Window](visuals/AudinoMainWindowWithPatientData.png)
Description: Complete view of the application showing patient list, prescription form, medication selector, and real-time interaction alerts panel.

### Patient Management Dialogs:

![Add Patient](visuals/AudinoAddPatientDataWindow.png)
Description: Dialog for adding new patients with fields for name, birth date, allergies, and medical conditions.

![Edit Patient](visuals/AudinoEditPatientDataWindow.png)
Description: Dialog for modifying existing patient records allowing updates to allergies, conditions, and personal information.
