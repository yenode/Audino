# Audino: Intelligent Prescription Manager:

## Quick Start:

### Manual Launch:
```bash
mvn javafx:run       # Using Maven (most reliable)
# OR
java -jar target/audino-1.1.0.jar  # Direct JAR execution
```

## System Requirements:

- **OS**: Windows 10/11 and Linux (Ubuntu/Debian preferred).
- **Java**: Version 17+ (Java 24 tested and working).
- **Memory**: Minimum 512 MB RAM.
- **Dependencies**: Maven, OpenJFX runtime.

## Features:

### Core Functionality:
- **Patient Management**: Searchable patient records with comprehensive data.
- **Medication Database**: Extensive drug repository with interaction data.
- **Prescription Builder**: Real-time prescription creation with validation.
- **Drug Interaction Engine**: Intelligent detection of drug-allergy interactions, drug-drug interactions, and drug-condition contraindications.
- **Real-time Alerts**: Color-coded warnings with detailed recommendations.

### Technical Highlights:
- **MVC Architecture**: Clean separation of concerns.
- **Design Patterns**: Strategy, Observer, Singleton, and Composition.
- **Object-Oriented Design**: Full OOP principles implementation.
- **Asynchronous Processing**: Non-blocking UI for responsive experience.
- **Comprehensive Testing**: JUnit 5 test suite included.
- **Cross-Platform Runtime**: Works on both Windows and Linux.

## Technology Stack:

- **Language**: Java 17+.
- **GUI**: JavaFX 19+ with FXML layouts.
- **Styling**: Custom CSS themes.
- **Build Tool**: Maven with automated dependency management.
- **Data**: SQLite RDBMS with JDBC and baseline in-code seeding.
- **Testing**: JUnit 5 framework.
- **Platform**: Windows and Linux.

## Project Structure:

```

audino/
└── src/
├── main/
│   ├── java/com/audino/
│   │   ├── model/
│   │   ├── controller/
│   │   ├── service/
│   │   └── util/
│   └── resources/
│       ├── data/
│       ├── fxml/
│       └── css/
└── test/
└── java/com/audino/

````

## Quick Start:

### Prerequisites:
- Java JDK 17 or higher.
- Apache Maven 3.6 or higher.

### Building the Application:
From the project root directory:
```bash
mvn clean compile
````

### Running the Application:

**Windows Users - Easy Launch:**
Double-click `Audino.bat` file to run the application.

**Or use command line:**
```bash
mvn javafx:run
```

**Or use PowerShell script:**
```powershell
.\start.ps1
```

### Running Tests:

```bash
mvn test
```

### Building Executable Package:

To create a distributable JAR with all dependencies:
```bash
mvn clean package -DskipTests
```

This creates `target/audino-1.1.0.jar` - however, JavaFX applications require special handling. Use the `Audino.bat` launcher for the best experience.

## Application Screenshots:

### Main Window with Patient Data:
![Main Window](visuals/AudinoMainWindowWithPatientData.png)
**Description**: The main interface showing the patient list on the left, prescription creation form in the center, and interaction alerts panel displaying real-time drug safety warnings.

### Add Patient Dialog:
![Add Patient](visuals/AudinoAddPatientDataWindow.png)
**Description**: Dialog window for adding new patient information including name, date of birth, known allergies, and chronic medical conditions.

### Edit Patient Dialog:
![Edit Patient](visuals/AudinoEditPatientDataWindow.png)
**Description**: Dialog window for editing existing patient information, allowing healthcare providers to update allergies, conditions, and personal details.

## Architecture Overview:

### Design Patterns Implemented:

#### 1\. Model-View-Controller (MVC):

  - **Model**: `Patient`, `Medication`, `Prescription` classes in the `com.audino.model` package.
  - **View**: `MainWindow.fxml` and `application.css` in the resources folder.
  - **Controller**: `MainController` in the `com.audino.controller` package.

#### 2\. Strategy Pattern:

  - `InteractionCheckStrategy` interface defines the contract for checking different types of conflicts.
  - Concrete implementations (`AllergyCheckStrategy`, `DrugDrugCheckStrategy`, `ConditionCheckStrategy`) are managed by the `InteractionEngine`.

#### 3\. Singleton Pattern:

  - `ConfigurationManager` ensures a single, globally accessible instance for managing application settings like file paths.

#### 4\. Observer Pattern:

  - Implemented via JavaFX properties and listeners. The UI components in `MainController` listen for changes in the selection models of `ListView` and `TableView` to trigger updates and actions automatically.

### Object-Oriented Design:

#### Inheritance & Polymorphism:

The abstract `Medication` class is extended by concrete classes like `TabletMedication`, `LiquidMedication`, and `InjectionMedication`. Each subclass provides its own specific implementation for methods like `isValidDosage()`, demonstrating polymorphism.

#### Encapsulation:

All model classes have private fields, with access controlled through public getters and setters. Collections are returned as defensive copies to prevent external modification of the internal state.

## Data Management:

Runtime data for patients and prescriptions is stored in a SQLite database, configured via `sqlite.database.path` in `application.properties`. On first run, when the database is empty, the application seeds baseline medications, patients, and interaction rules directly through `DataService`. The `DataService` class handles schema creation, baseline seeding, and transactional CRUD operations.

The repository includes a SQLite database file at `data/audino.db`.

Detailed database documentation is available in `DataBase.md`.

### Sample Data:

The application includes baseline demonstration data featuring:
- **4 Medications**: Core examples including Amoxicillin, Ibuprofen, Warfarin, and Lisinopril.
- **3 Sample Patients**: Realistic profiles including allergy and chronic-condition scenarios.
- **Interaction Rules**: Pre-configured rules for drug-drug, drug-allergy, and drug-condition interactions.

