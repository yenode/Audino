# OOPs - Consolidated Documentation

This document consolidates all files from the `documentation` folder into a single reference.

## Included Sources

1. `OOP_PILLARS.md`
2. `CLASS_DIAGRAM.md`
3. `OBJECT_DIAGRAM.md`
4. `SEQUENCE_DIAGRAM.md`
5. `ACTIVITY_DIAGRAM.md`

---

## 1. Four Pillars of OOP

# Four Pillars of OOP in Audino Healthcare System

## 1. ENCAPSULATION
*Hiding internal details and providing controlled access*

### Real Examples from the Code:

#### A. Patient Class - Data Protection
```java
// Patient.java
public class Patient {
    private String patientId;        // Hidden from outside
    private String firstName;        // Private data
    private String lastName;         // Cannot be directly changed
    private List<String> allergies;  // Protected list
    
    // Controlled access through methods
    public String getPatientId() { 
        return patientId; 
    }
    
    public String getFullName() { 
        return firstName + " " + lastName;  // Combines private fields safely
    }
    
    public List<String> getAllergies() { 
        return new ArrayList<>(allergies);  // Returns copy, not original
    }
    
    public void addAllergy(String allergy) {
        if (allergy != null && !allergy.trim().isEmpty()) {  // Validates before adding
            allergies.add(allergy);
        }
    }
}
```
**Implementation:** Other classes cannot directly access patient data. They must use the provided methods, which include validation.

#### B. DataService Class - Hidden Database Operations
```java
// DataService.java
public class DataService {
    private List<Patient> patients;           // Hidden storage
    private String jdbcUrl;                  // Hidden SQLite configuration
    private List<Prescription> prescriptions; // Private data
    
    // Simple public interface
    public void savePatient(Patient patient) {
        // Complex operations hidden inside:
        // - Validation
        // - SQL mapping
        // - Transactional persistence
        // - Error handling
        patients.add(patient);
        savePatientToDatabase();  // Internal method
    }
    
    // Users call this simple method - complexity is hidden
    public List<Patient> searchPatients(String query) {
        // Complex filtering logic hidden here
        return patients.stream()
            .filter(p -> p.getFullName().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
    }
}
```
**Benefit:** Controllers do not need to know about SQLite tables, SQL statements, or transaction handling. The savePatient() method handles all complexity internally.

---

## 2. INHERITANCE
*One class inherits properties and behaviors from another*

### Real Examples from the Code:

#### A. Custom Exception Classes
```java
// Custom exception hierarchy
public class DataServiceException extends Exception {
    public DataServiceException(String message) {
        super(message);  // Inherits from Exception class
    }
}

public class PatientNotFoundException extends DataServiceException {
    public PatientNotFoundException(String patientId) {
        super("Patient not found with ID: " + patientId);  // Inherits constructor
    }
}
```
**Implementation:** PatientNotFoundException automatically gets all the features of Exception (like stack traces, error messages) plus adds its own specific behavior.

#### B. Model Base Class Pattern
```java
// Base entity class implementation
public abstract class BaseEntity {
    protected String id;                    // All entities have ID
    protected LocalDateTime createdDate;    // All entities have creation date
    
    public String getId() { return id; }
    public LocalDateTime getCreatedDate() { return createdDate; }
}

// Classes inherit common features
public class Patient extends BaseEntity {
    private String firstName;
    private String lastName;
    // Patient gets 'id' and 'createdDate' automatically from BaseEntity
}

public class Medication extends BaseEntity {
    private String displayName;
    private String genericName;
    // Medication also gets 'id' and 'createdDate' from BaseEntity
}
```
**Benefit:** No need to write ID and creation date code in every class. Write once, use everywhere.

#### C. Controller Inheritance from JavaFX
```java
// MainController inherits JavaFX capabilities
public class MainController implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inherits the contract from Initializable interface
        // Must implement this method
        setupPatientListView();
        setupMedicationComboBox();
    }
}
```
**Implementation:** JavaFX automatically calls the initialize() method when the window loads because the controller implements this interface.

---

## 3. POLYMORPHISM
*Same method name, different behaviors depending on the object*

### Real Examples from the Code:

#### A. Strategy Pattern for Interaction Checking
```java
// Same interface, different implementations
public interface InteractionCheckStrategy {
    List<InteractionAlert> checkInteraction(Patient patient, List<PrescribedDrug> drugs);
}

// Different classes, same method name, different behavior
public class AllergyCheckStrategy implements InteractionCheckStrategy {
    @Override
    public List<InteractionAlert> checkInteraction(Patient patient, List<PrescribedDrug> drugs) {
        // Checks for allergy conflicts
        List<InteractionAlert> alerts = new ArrayList<>();
        for (String allergy : patient.getAllergies()) {
            for (PrescribedDrug drug : drugs) {
                if (drug.getMedication().getMedicationClasses().contains(allergy)) {
                    alerts.add(new InteractionAlert(AlertLevel.CRITICAL, "Allergy Alert", 
                        "Patient allergic to " + allergy));
                }
            }
        }
        return alerts;
    }
}

public class DrugDrugCheckStrategy implements InteractionCheckStrategy {
    @Override
    public List<InteractionAlert> checkInteraction(Patient patient, List<PrescribedDrug> drugs) {
        // Checks for drug-drug interactions
        List<InteractionAlert> alerts = new ArrayList<>();
        for (int i = 0; i < drugs.size(); i++) {
            for (int j = i + 1; j < drugs.size(); j++) {
                // Complex drug interaction logic here
                if (drugsInteract(drugs.get(i), drugs.get(j))) {
                    alerts.add(new InteractionAlert(AlertLevel.WARNING, "Drug Interaction", 
                        "Possible interaction between medications"));
                }
            }
        }
        return alerts;
    }
}

// In InteractionEngine - same call, different behaviors
public class InteractionEngine {
    private List<InteractionCheckStrategy> strategies = new ArrayList<>();
    
    public void checkAllInteractions() {
        for (InteractionCheckStrategy strategy : strategies) {
            // Same method call 'checkInteraction()' but different behavior each time
            List<InteractionAlert> alerts = strategy.checkInteraction(patient, drugs);
            allAlerts.addAll(alerts);
        }
    }
}
```
**Implementation:** The same method checkInteraction() produces different results depending on which strategy object is used.

#### B. Collection Polymorphism in Controllers
```java
// MainController.java
public class MainController {
    // Same List interface, different implementations
    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private final ObservableList<Medication> medicationList = FXCollections.observableArrayList();
    private final ObservableList<PrescribedDrug> prescribedDrugList = FXCollections.observableArrayList();
    
    // Same method calls work on all lists
    public void clearAllLists() {
        patientList.clear();        // Same method name
        medicationList.clear();     // Same method name
        prescribedDrugList.clear(); // Same method name
        
        // Same behavior (clearing) but different internal implementations
    }
}
```

---

## 4. ABSTRACTION
*Hiding complex implementation details behind simple interfaces*

### Real Examples from the Code:

#### A. DataService Abstraction
```java
// Complex operations hidden behind simple methods
public class DataService {
    
    // Simple interface - complex implementation hidden
    public void savePatient(Patient patient) {
        // This method internally handles:
        // 1. Validates patient data
        // 2. Generates unique ID if needed
        // 3. Maps model fields to SQL payloads
        // 4. Handles database I/O operations
        // 5. Manages error conditions
        // 6. Updates in-memory lists
        
        // Callers do not need to know about this complexity
    }
    
    // Another abstraction example
    public List<Patient> searchPatients(String query) {
        // Simple to use - just pass a search string
        // But internally handles:
        // - Case-insensitive matching
        // - Multiple field searching
        // - Performance optimization
        // - Result sorting
        
        // Callers do not care about the implementation details
    }
}
```

#### B. MainController UI Abstraction
```java
// MainController.java
public class MainController {
    
    // Simple method call abstracts complex UI operations
    private void updateUIState() {
        // This one method call handles:
        // - Enabling/disabling multiple buttons
        // - Updating status labels
        // - Refreshing table views
        // - Managing prescription status
        // - Coordinating multiple UI components
        
        boolean patientSelected = selectedPatient != null;
        boolean prescriptionLoaded = currentPrescription != null;
        
        // Complex logic simplified into clear boolean conditions
        addMedicationBtn.setDisable(!prescriptionLoaded);
        saveBtn.setDisable(!hasUnsavedChanges || !isDraft);
        newPrescriptionBtn.setDisable(!patientSelected);
    }
    
    // Another abstraction - interaction checking
    private void checkInteractions() {
        // Simple method call that abstracts:
        // - Async processing
        // - Multiple strategy execution
        // - Thread management
        // - UI thread safety
        // - Error handling
        
        CompletableFuture<List<InteractionAlert>> future = interactionEngine.checkAllInteractionsAsync();
        // Callers do not need to know about CompletableFuture complexity
    }
}
```

#### C. Prescription Class Business Logic Abstraction
```java
// Prescription.java
public class Prescription {
    private List<PrescribedDrug> prescribedDrugs = new ArrayList<>();
    
    // Simple interface hides validation complexity
    public void addPrescribedDrug(PrescribedDrug drug) {
        // Simple call, but internally:
        // - Validates drug is not null
        // - Checks for duplicates
        // - Updates prescription status
        // - Maintains data integrity
        
        if (drug != null && !prescribedDrugs.contains(drug)) {
            prescribedDrugs.add(drug);
            setStatus(PrescriptionStatus.DRAFT);  // Auto-update status
        }
    }
    
    // Business logic abstracted into simple method
    public boolean isEmpty() {
        return prescribedDrugs.isEmpty();  // Simple but essential business rule
    }
    
    // Complex calculation hidden behind simple interface
    public int getTotalMedicationCount() {
        return prescribedDrugs.size();  // Could be more complex in real system
    }
}
```

---

## Summary

### 1. ENCAPSULATION = Keep private things private
- Patient data is protected - only accessible through safe methods
- Database operations are hidden in DataService
- UI components are private in controllers

### 2. INHERITANCE = Child gets parent features
- Exception classes inherit from Java Exception class
- Controllers inherit from JavaFX interfaces
- All entities can inherit common ID and date fields

### 3. POLYMORPHISM = Same name, different behaviors
- Different interaction checking strategies with same method name
- Different alert types responding to same interface
- Same list operations working on different data types

### 4. ABSTRACTION = Hide complexity behind simple interfaces
- savePatient() - simple call, complex operations hidden
- updateUIState() - one call updates entire interface
- checkInteractions() - simple call, complex async processing hidden

The Audino project demonstrates professional OOP design with all four pillars properly implemented for maintainable and extensible code.

---

## 2. Class Diagram

# Audino Healthcare System - Class Diagram

## UML Class Diagram

```
┌─────────────────────────────────────┐
│            MainController           │
├─────────────────────────────────────┤
│ - dataService: DataService          │
│ - interactionEngine: InteractionEngine │
│ - selectedPatient: Patient          │
│ - currentPrescription: Prescription │
│ - patientList: ObservableList<Patient> │
│ - medicationList: ObservableList<Medication> │
│ - prescribedDrugList: ObservableList<PrescribedDrug> │
│ - alertList: ObservableList<InteractionAlert> │
├─────────────────────────────────────┤
│ + initialize(): void                │
│ + handlePatientSelection(Patient): void │
│ + handleAddMedication(): void       │
│ + handleSave(): void                │
│ + handleRefresh(): void             │
│ + checkInteractions(): void         │
│ + updateUIState(): void             │
└─────────────────────────────────────┘
                    │
                    │ uses
                    ▼
┌─────────────────────────────────────┐
│            DataService              │
├─────────────────────────────────────┤
│ - objectMapper: ObjectMapper        │
│ - patients: List<Patient>           │
│ - medications: List<Medication>     │
│ - prescriptions: List<Prescription> │
│ - interactionRules: List<InteractionRule> │
├─────────────────────────────────────┤
│ + loadAllData(): void               │
│ + savePatient(Patient): void        │
│ + savePrescription(Prescription): void │
│ + searchPatients(String): List<Patient> │
│ + searchMedications(String): List<Medication> │
│ + getActivePrescriberionForPatient(String): Prescription │
└─────────────────────────────────────┘
                    │
                    │ manages
                    ▼
┌─────────────────────────────────────┐
│              Patient                │
├─────────────────────────────────────┤
│ - patientId: String                 │
│ - firstName: String                 │
│ - lastName: String                  │
│ - dateOfBirth: LocalDate           │
│ - gender: String                    │
│ - allergies: List<String>           │
│ - chronicConditions: List<String>   │
├─────────────────────────────────────┤
│ + getFullName(): String             │
│ + getAge(): int                     │
│ + addAllergy(String): void          │
│ + addChronicCondition(String): void │
└─────────────────────────────────────┘
                    │
                    │ has
                    ▼
┌─────────────────────────────────────┐
│            Prescription             │
├─────────────────────────────────────┤
│ - prescriptionId: String            │
│ - patient: Patient                  │
│ - prescriber: String                │
│ - dateCreated: LocalDateTime        │
│ - status: PrescriptionStatus        │
│ - prescribedDrugs: List<PrescribedDrug> │
│ - alerts: List<InteractionAlert>    │
├─────────────────────────────────────┤
│ + addPrescribedDrug(PrescribedDrug): void │
│ + removePrescribedDrug(PrescribedDrug): void │
│ + isEmpty(): boolean                │
│ + setStatus(PrescriptionStatus): void │
└─────────────────────────────────────┘
                    │
                    │ contains
                    ▼
┌─────────────────────────────────────┐
│            PrescribedDrug           │
├─────────────────────────────────────┤
│ - medicationId: String              │
│ - medication: Medication            │
│ - dosage: String                    │
│ - frequency: String                 │
│ - duration: String                  │
│ - instructions: String              │
│ - prescriber: String                │
├─────────────────────────────────────┤
│ + getMedication(): Medication       │
│ + setMedication(Medication): void   │
│ + getDosage(): String               │
│ + getFrequency(): String            │
└─────────────────────────────────────┘
                    │
                    │ references
                    ▼
┌─────────────────────────────────────┐
│            Medication               │
├─────────────────────────────────────┤
│ - medicationId: String              │
│ - displayName: String               │
│ - genericName: String               │
│ - medicationType: MedicationType    │
│ - medicationClasses: List<String>   │
│ - strength: String                  │
│ - unit: String                      │
├─────────────────────────────────────┤
│ + getDisplayName(): String          │
│ + isValidDosage(String): boolean    │
│ + getMedicationClasses(): List<String> │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│          InteractionEngine          │
├─────────────────────────────────────┤
│ - strategies: List<InteractionCheckStrategy> │
├─────────────────────────────────────┤
│ + checkAllInteractionsAsync(): CompletableFuture<List<InteractionAlert>> │
│ + addStrategy(InteractionCheckStrategy): void │
│ + shutdown(): void                  │
└─────────────────────────────────────┘
                    │
                    │ uses
                    ▼
┌─────────────────────────────────────┐
│    InteractionCheckStrategy         │
│         <<interface>>               │
├─────────────────────────────────────┤
│ + checkInteraction(Patient, List<PrescribedDrug>, List<InteractionRule>, List<Medication>): List<InteractionAlert> │
└─────────────────────────────────────┘
                    △
                    │ implements
    ┌───────────────┼───────────────┐
    │               │               │
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│AllergyCheck │ │DrugDrugCheck│ │ConditionCheck│
│Strategy     │ │Strategy     │ │Strategy     │
└─────────────┘ └─────────────┘ └─────────────┘

┌─────────────────────────────────────┐
│         InteractionAlert            │
├─────────────────────────────────────┤
│ - alertLevel: AlertLevel            │
│ - alertType: String                 │
│ - message: String                   │
│ - acknowledged: boolean             │
├─────────────────────────────────────┤
│ + acknowledge(): void               │
│ + getFormattedMessage(): String     │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│           AlertLevel                │
│          <<enum>>                   │
├─────────────────────────────────────┤
│ CRITICAL                            │
│ WARNING                             │
│ INFO                                │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│       PrescriptionStatus            │
│          <<enum>>                   │
├─────────────────────────────────────┤
│ DRAFT                               │
│ APPROVED                            │
│ CANCELLED                           │
└─────────────────────────────────────┘
```

## Relationships:
- **MainController** → **DataService**: Uses (Dependency)
- **MainController** → **InteractionEngine**: Uses (Dependency)
- **Patient** → **Prescription**: One-to-Many (Composition)
- **Prescription** → **PrescribedDrug**: One-to-Many (Composition)
- **PrescribedDrug** → **Medication**: Many-to-One (Association)
- **InteractionEngine** → **InteractionCheckStrategy**: One-to-Many (Strategy Pattern)
- **DataService** manages all entities through SQLite persistence

---

## 3. Object Diagram

# Audino Healthcare System - Object Diagram

## Object Diagram: Adding Medication Workflow

```
┌─────────────────────────────────────────────────────────────┐
│                    mainController : MainController          │
│ ─────────────────────────────────────────────────────────── │
│ selectedPatient = patient1                                  │
│ currentPrescription = prescription1                         │
│ dataService = dataService1                                  │
│ interactionEngine = interactionEngine1                     │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ selectedPatient
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    patient1 : Patient                      │
│ ─────────────────────────────────────────────────────────── │
│ patientId = "PAT-12345678"                                  │
│ firstName = "John"                                          │
│ lastName = "Doe"                                            │
│ dateOfBirth = 1990-05-15                                    │
│ gender = "Male"                                             │
│ allergies = ["Penicillin", "Sulfa"]                        │
│ chronicConditions = ["Hypertension", "Diabetes"]           │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ has
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                prescription1 : Prescription                 │
│ ─────────────────────────────────────────────────────────── │
│ prescriptionId = "RX-87654321"                              │
│ patient = patient1                                          │
│ prescriber = "Dr. User"                                     │
│ dateCreated = 2025-11-13T10:30:00                          │
│ status = DRAFT                                              │
│ prescribedDrugs = [prescribedDrug1, prescribedDrug2]       │
│ alerts = [alert1, alert2]                                  │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ contains
                                ▼
┌─────────────────────────────────────────────────────────────┐
│              prescribedDrug1 : PrescribedDrug               │
│ ─────────────────────────────────────────────────────────── │
│ medicationId = "MED-001"                                    │
│ medication = medication1                                    │
│ dosage = "10mg"                                             │
│ frequency = "Twice daily"                                   │
│ duration = "7 days"                                         │
│ instructions = "Take with food"                             │
│ prescriber = "Dr. User"                                     │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ references
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                 medication1 : Medication                    │
│ ─────────────────────────────────────────────────────────── │
│ medicationId = "MED-001"                                    │
│ displayName = "Lisinopril"                                  │
│ genericName = "Lisinopril"                                  │
│ medicationType = TABLET                                     │
│ medicationClasses = ["ACE Inhibitor"]                      │
│ strength = "10"                                             │
│ unit = "mg"                                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              prescribedDrug2 : PrescribedDrug               │
│ ─────────────────────────────────────────────────────────── │
│ medicationId = "MED-002"                                    │
│ medication = medication2                                    │
│ dosage = "500mg"                                            │
│ frequency = "Once daily"                                    │
│ duration = "30 days"                                        │
│ instructions = "Take in morning"                            │
│ prescriber = "Dr. User"                                     │
└─────────────────────────────────────────────────────────────┘
                                │
                                │ references
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                 medication2 : Medication                    │
│ ─────────────────────────────────────────────────────────── │
│ medicationId = "MED-002"                                    │
│ displayName = "Metformin"                                   │
│ genericName = "Metformin HCl"                               │
│ medicationType = TABLET                                     │
│ medicationClasses = ["Biguanide", "Antidiabetic"]          │
│ strength = "500"                                            │
│ unit = "mg"                                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                 dataService1 : DataService                 │
│ ─────────────────────────────────────────────────────────── │
│ patients = [patient1, patient2, patient3]                  │
│ medications = [medication1, medication2, medication3...]    │
│ prescriptions = [prescription1, prescription2]             │
│ interactionRules = [rule1, rule2, rule3...]                │
│ objectMapper = ObjectMapper instance                       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│            interactionEngine1 : InteractionEngine          │
│ ─────────────────────────────────────────────────────────── │
│ strategies = [allergyStrategy, drugDrugStrategy,            │
│              conditionStrategy]                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    alert1 : InteractionAlert               │
│ ─────────────────────────────────────────────────────────── │
│ alertLevel = WARNING                                        │
│ alertType = "Drug-Condition Interaction"                   │
│ message = "Lisinopril may affect blood pressure monitoring │
│           in hypertensive patients"                         │
│ acknowledged = false                                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    alert2 : InteractionAlert               │
│ ─────────────────────────────────────────────────────────── │
│ alertLevel = INFO                                           │
│ alertType = "Drug-Drug Synergy"                             │
│ message = "Lisinopril and Metformin work well together     │
│           for diabetic patients with hypertension"         │
│ acknowledged = false                                        │
└─────────────────────────────────────────────────────────────┘
```

## Object Relationships in Runtime:
1. **MainController** holds references to active Patient and current Prescription
2. **Patient** object contains personal data and medical history
3. **Prescription** aggregates multiple PrescribedDrug objects
4. **PrescribedDrug** references Medication from the master medication list
5. **DataService** manages persistence of all entities to SQLite tables
6. **InteractionEngine** processes current prescription and generates alerts

---

## 4. Sequence Diagram

# Audino Healthcare System - Sequence Diagram

## Sequence Diagram: Add Medication Use Case

```
User          MainController    DataService    InteractionEngine    SQLiteDB
 │                 │                │                 │               │
 │ selectPatient() │                │                 │               │
 ├─────────────────►               │                 │               │
 │                 │                │                 │               │
 │                 │ getActivePrescriberionForPatient(patientId)      │
 │                 ├────────────────►               │               │
 │                 │                │                 │               │
 │                 │                │ readFromDatabase()            │
 │                 │                ├─────────────────────────────────►
 │                 │                │                 │               │
 │                 │                │ SELECT from prescriptions      │
 │                 │                ◄─────────────────────────────────┤
 │                 │                │                 │               │
 │                 │ return Prescription             │               │
 │                 ◄────────────────┤                 │               │
 │                 │                │                 │               │
 │ updateUI()      │                │                 │               │
 ◄─────────────────┤                │                 │               │
 │                 │                │                 │               │
 │ searchMedication│                │                 │               │
 ├─────────────────►               │                 │               │
 │                 │                │                 │               │
 │                 │ searchMedications(query)        │               │
 │                 ├────────────────►               │               │
 │                 │                │                 │               │
 │                 │ return filteredList             │               │
 │                 ◄────────────────┤                 │               │
 │                 │                │                 │               │
 │ selectMedication│                │                 │               │
 │ & enterDosage   │                │                 │               │
 ├─────────────────►               │                 │               │
 │                 │                │                 │               │
 │ clickAddMedication()             │                 │               │
 ├─────────────────►               │                 │               │
 │                 │                │                 │               │
 │                 │ validateInput()│                 │               │
 │                 ├──────────┐     │                 │               │
 │                 │          │     │                 │               │
 │                 │◄─────────┘     │                 │               │
 │                 │                │                 │               │
 │                 │ addPrescribedDrug()             │               │
 │                 ├──────────┐     │                 │               │
 │                 │          │     │                 │               │
 │                 │◄─────────┘     │                 │               │
 │                 │                │                 │               │
 │                 │ updatePrescriptionTable()       │               │
 │                 ├──────────┐     │                 │               │
 │                 │          │     │                 │               │
 │                 │◄─────────┘     │                 │               │
 │                 │                │                 │               │
 │                 │ checkAllInteractionsAsync()     │               │
 │                 ├──────────────────────────────────►               │
 │                 │                │                 │               │
 │                 │                │                 │ processRules() │
 │                 │                │                 ├───────┐       │
 │                 │                │                 │       │       │
 │                 │                │                 │◄──────┘       │
 │                 │                │                 │               │
 │                 │ return CompletableFuture<List<InteractionAlert>> │
 │                 ◄──────────────────────────────────┤               │
 │                 │                │                 │               │
 │                 │ updateAlertsView()              │               │
 │                 ├──────────┐     │                 │               │
 │                 │          │     │                 │               │
 │                 │◄─────────┘     │                 │               │
 │                 │                │                 │               │
 │ showDraftStatus │                │                 │               │
 ◄─────────────────┤                │                 │               │
 │                 │                │                 │               │
 │ clickSave()     │                │                 │               │
 ├─────────────────►               │                 │               │
 │                 │                │                 │               │
 │                 │ savePrescription()              │               │
 │                 ├────────────────►               │               │
 │                 │                │                 │               │
 │                 │                │ writeToDatabase()             │
 │                 │                ├─────────────────────────────────►
 │                 │                │                 │               │
 │                 │                │ UPSERT prescriptions           │
 │                 │                ◄─────────────────────────────────┤
 │                 │                │                 │               │
 │                 │ return success │                 │               │
 │                 ◄────────────────┤                 │               │
 │                 │                │                 │               │
 │ showSuccessMsg  │                │                 │               │
 ◄─────────────────┤                │                 │               │
```

## Key Interactions:

### 1. Patient Selection Phase
- User selects patient → Controller loads existing prescription from DataService
- DataService reads from SQLite to get prescription data
- UI updates with patient information and prescription history

### 2. Medication Search Phase 
- User types in search field → Controller calls DataService.searchMedications()
- Real-time filtering of medication list from in-memory data
- ComboBox updates with filtered results

### 3. Add Medication Phase
- User fills form and clicks Add → Controller validates input locally
- Medication added to current prescription object (in-memory only)
- Prescription table updated immediately
- Interaction engine called asynchronously for safety checks

### 4. Interaction Checking (Async)
- InteractionEngine runs strategies in parallel
- Checks drug-drug, drug-allergy, and condition interactions
- Returns CompletableFuture with alerts list
- UI updated with interaction alerts when processing completes

### 5. Save Phase
- User clicks Save → Controller calls DataService.savePrescription()
- DataService writes updated prescription to SQLite
- Prescription status changed from DRAFT to APPROVED
- Success message shown to user

## Asynchronous Operations:
- **Interaction Checking**: Non-blocking background process
- **Database I/O**: SQLite read/write operations
- **UI Updates**: Platform.runLater() for thread-safe UI updates

---

## 5. Activity Diagram

# Audino Healthcare System - Activity Diagram

## Activity Diagram: Add Medication Use Case

```
                    ┌─────────────────────┐
                    │       START         │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │  Select Patient     │
                    │  from List          │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Load Patient Data   │
                    │ & Existing          │
                    │ Prescription        │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Check if Current    │
           ┌────────┤ Prescription Exists?├────────┐
           │        └─────────────────────┘        │
           │ NO                                    │ YES
           ▼                                       ▼
┌─────────────────────┐              ┌─────────────────────┐
│ Create New          │              │ Load Existing       │
│ Prescription        │              │ Prescription        │
│ (DRAFT Status)      │              │ Data               │
└──────────┬──────────┘              └──────────┬──────────┘
           │                                    │
           └─────────────────┬──────────────────┘
                             │
                             ▼
                    ┌─────────────────────┐
                    │ Search & Select     │
                    │ Medication from     │
                    │ ComboBox           │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Enter Dosage,       │
                    │ Frequency &         │
                    │ Duration            │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Validate Input      │
           ┌────────┤ Data               ├────────┐
           │        └─────────────────────┘        │
           │ Invalid                              │ Valid
           ▼                                       ▼
┌─────────────────────┐              ┌─────────────────────┐
│ Show Warning        │              │ Add Medication to   │
│ Alert with          │              │ Prescription        │
│ Error Message       │              │ (In Memory)         │
└──────────┬──────────┘              └──────────┬──────────┘
           │                                    │
           └─────────────────┬──────────────────┘
                             │
                             ▼
                    ┌─────────────────────┐
                    │ Update Prescription │
                    │ Table View          │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Run Interaction     │
                    │ Engine Check        │
                    │ (Async)             │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Display Interaction │
                    │ Alerts & Summary    │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Set Status to       │
                    │ DRAFT (Unsaved)     │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Enable Save Button  │
                    │ & Show Status       │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ User Clicks         │
           ┌────────┤ Save Button?        ├────────┐
           │        └─────────────────────┘        │
           │ NO                                    │ YES
           ▼                                       ▼
┌─────────────────────┐              ┌─────────────────────┐
│ Medication Added    │              │ Save Prescription   │
│ to Prescription     │              │ to SQLite Database  │
│ (Memory Only)       │              └──────────┬──────────┘
└─────────────────────┘                         │
                                                 ▼
                                      ┌─────────────────────┐
                                      │ Set Status to       │
                                      │ APPROVED            │
                                      └──────────┬──────────┘
                                                 │
                                                 ▼
                                      ┌─────────────────────┐
                                      │ Show Success        │
                                      │ Message             │
                                      └──────────┬──────────┘
                                                 │
                                                 ▼
                                      ┌─────────────────────┐
                                      │       END           │
                                      └─────────────────────┘
```

## Decision Points:
- **Patient Selection**: Must select patient before adding medications
- **Prescription Existence**: System checks for existing prescription or creates new one
- **Input Validation**: Dosage format must match medication type
- **Save Decision**: User controls when to persist data to database

## Parallel Processes:
- **Interaction Checking**: Runs asynchronously in background
- **UI Updates**: Real-time updates of prescription table and alerts
- **Search Filtering**: Live medication search as user types
