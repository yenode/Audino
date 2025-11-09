# Audino - CodeBase Index:

## Project Structure:

### Root Files:
- `pom.xml` - Maven project configuration and dependencies.
- `README.md` - Project overview and quick start guide.
- `start.ps1` - PowerShell script to run the application.
- `setup.ps1` - PowerShell script to set up the project environment.

### Source Code (`src/main/java/com/audino/`):

#### Main Application:
- `AudinoApplication.java` - Main entry point for the JavaFX application.

#### Controller Package (`controller/`):
- `MainController.java` - Handles UI interactions and connects view with business logic.

#### Model Package (`model/`):
- `AlertLevel.java` - Enumeration for alert severity levels.
- `AlertType.java` - Enumeration for types of drug interaction alerts.
- `InjectionMedication.java` - Represents injectable medications.
- `InteractionAlert.java` - Model for drug interaction warnings.
- `LiquidMedication.java` - Represents liquid medications.
- `Medication.java` - Base abstract class for all medications.
- `MedicationType.java` - Enumeration for medication form types.
- `Patient.java` - Represents patient information and medical history.
- `PrescribedDrug.java` - Links medication to prescription with dosage details.
- `Prescription.java` - Represents a patient's prescription with multiple drugs.
- `PrescriptionStatus.java` - Enumeration for prescription lifecycle status.
- `TabletMedication.java` - Represents tablet/capsule medications.

#### Service Package (`service/`):
- `AllergyCheckStrategy.java` - Strategy for checking patient allergies against medications.
- `ConditionCheckStrategy.java` - Strategy for checking drug-condition interactions.
- `DataService.java` - Handles data loading and persistence operations.
- `DrugDrugCheckStrategy.java` - Strategy for checking drug-drug interactions.
- `InteractionCheckStrategy.java` - Interface defining interaction checking strategy.
- `InteractionEngine.java` - Core engine orchestrating all interaction checks.

#### Utility Package (`util/`):
- `ConfigurationManager.java` - Manages application configuration and settings.

### Resources (`src/main/resources/`):

#### CSS Stylesheets (`css/`):
- `application.css` - Application-wide styling definitions.

#### Data Files (`data/`):
- `interaction-rules.json` - Database of drug interaction rules and conditions.
- `medications.json` - Comprehensive medication database with 150 medications.
- `patients.json` - Sample patient records (8 patients including Mridankan Mandal, Aditya Pachauri, Sayan Samajpati, and Sanskriti Wakale).

#### FXML Views (`fxml/`):
- `MainWindow.fxml` - Main application window layout definition.

### Test Code (`src/test/java/com/audino/`):

#### Model Tests (`model/`):
- `MedicationTest.java` - Unit tests for medication classes.
- `PatientTest.java` - Unit tests for patient model.

#### Service Tests (`service/`):
- `DataServiceTest.java` - Tests for data service operations.
- `InteractionEngineTest.java` - Tests for interaction detection logic.

#### Test Suite:
- `TestSuite.java` - Aggregated test suite runner.

## Key Design Patterns:

### Strategy Pattern:
- Used in interaction checking system with multiple strategies.
- Allows flexible addition of new interaction check types.

### Factory Pattern:
- Medication creation with type-specific subclasses.

### MVC Pattern:
- Clear separation between Model, View (FXML), and Controller.

## Technology Stack:

- **Java**: Core programming language.
- **JavaFX**: GUI framework for desktop application.
- **Maven**: Dependency management and build tool.
- **JUnit**: Unit testing framework.
- **JSON**: Data persistence format.

## Data Flow:

1. Application starts from `AudinoApplication.java`.
2. `DataService` loads data from JSON files.
3. `MainController` handles user interactions.
4. `InteractionEngine` processes prescriptions using strategy pattern.
5. Alerts are generated and displayed to user.

## Extension Points:

- Add new medication types by extending `Medication` class.
- Implement new interaction checks via `InteractionCheckStrategy` interface.
- Expand data sources in `DataService`.
- Customize UI through FXML and CSS files.
