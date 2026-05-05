# Audino: Intelligent Prescription Manager

## System Overview:
Audino is designed as a desktop healthcare application for safe prescription authoring. A JavaFX interface is provided for patient selection, medication search, prescription authoring, and interaction review. Runtime persistence is handled by PostgreSQL so that relational integrity can be preserved for patient, prescription, and medication records.

## Tech Stack Used:
- **Language**: Java 17
- **UI Framework**: JavaFX 19
- **Database Engine**: PostgreSQL (Embedded & Native support)
- **Database Connection Pooling**: HikariCP
- **Database Interaction**: Native JDBC
- **Security**: BCrypt for role-based clinician authentication
- **Build Tool**: Apache Maven

## Features and Functionality:
- **Patient Record Management**: Create, update, and securely store detailed patient demographics, chronic conditions, and allergy profiles.
- **Medication Intelligence System**: 
  - **Medication Search**: Search by generic name, brand name, and standard RxNorm identifier codes.
  - **Autocorrection Pipeline**: Typo tolerance via Levenshtein distance and character trigram cosine similarity scoring to detect and autocorrect misspelled medication searches.
  - **Search Recommendations**: Context-aware dynamic drop-down suggestions powered by Aho-Corasick multi-token candidate retrieval as the user types.
- **Prescription Authoring**: End-to-end authoring interface with structured dosage, frequency, duration, unit pricing, and prescribing physician tracking.
- **Real-Time Clinical Interaction Engine**: 
  - **Drug-Drug Interactions**: Actively prevents harmful cross-reactions between concurrently prescribed medications.
  - **Drug-Allergy Interactions**: Flags severe conflicts between prescribed drug classes and patient-specific allergies.
  - **Drug-Condition Contraindications**: Warns against prescriptions that aggravate documented chronic patient conditions.
- **Dynamic Alerting System**: Visual severity categorization (Critical, Warning, and Info alerts) that actively monitors the authoring flow. Unacknowledged critical alerts prevent prescription finalization.
- **Role-Based Access Control**: Secure login capabilities limiting administrative tasks exclusively to authorized clinicians using BCrypt-hashed credentials.
- **Automated Pricing Estimation**: Dynamic cost estimation calculated using unified prescription duration and dosage logic, yielding precise currency estimations in real-time.

## Architecture Summary:
The application is structured with strict Model-View-Controller boundaries. Safety rule evaluation is delegated to strategy implementations through the interaction engine. Persistence duties are centralized in DataService, and schema control is performed in PostgreSQL through JDBC.

## Steps to Run the Project:
For comprehensive instructions on compiling, configuring, and launching the application, please refer to the primary documentation:
- [Installation and Setup Guide](documentation/InstallationAndSetup.md)
- [Usage Guide](Usage.md)

## Documentation Map:
1. Data model and SQL schema details are documented in `DataBase.md`.
2. Integration and persistence flow details are documented in `documentation/DATABASE_INTEGRATION.md`.
3. Class level structure is documented in `documentation/CLASS_DIAGRAM.md`.
4. Detailed ER source is documented in `documentation/ER_DIAGRAM.puml`.
5. Detailed architecture source is documented in `documentation/ARCHITECTURE_DIAGRAM.puml`.

## Visual References:
### Main Window:
![Main Window](visuals/AudinoMainWindowWithPatientData.png)

### Add Patient Dialog:
![Add Patient](visuals/AudinoAddPatientDataWindow.png)

### Edit Patient Dialog:
![Edit Patient](visuals/AudinoEditPatientDataWindow.png)
