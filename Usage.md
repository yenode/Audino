# Audino - Usage Guide:

## Getting Started:

### Launching the Application:

After installation, run the application using:
```powershell
.\start.ps1
```

The main window will open displaying the Audino interface.

## Main Features:

### 1. Patient Management:

#### Viewing Patient Information:
- Select a patient from the patient list.
- Patient details display including name, age, and medical history.
- View current allergies and medical conditions.

#### Sample Demonstration Patients:
The application includes 8 sample patients for demonstration:
- **Mridankan Mandal** (Born: May 14, 1998) - Has seasonal allergies and mild asthma, allergic to Latex and Ibuprofen.
- **Aditya Pachauri** (Born: August 23, 1995) - Manages hypertension and hyperlipidemia, no known drug allergies.
- **Sayan Samajpati** (Born: December 7, 1997) - Type 1 Diabetes and thyroid disorder, allergic to Sulfa and Shellfish.
- **Sanskriti Wakale** (Born: March 19, 1999) - Iron deficiency anemia and PCOS, allergic to Penicillin and Peanuts.
- Plus 4 additional sample patients (John Smith, Jane Doe, Robert Johnson, Emily Williams).

#### Adding a New Patient:
- Click the "Add Patient" button.
- Fill in required fields: name, date of birth, allergies, conditions.
- Save the patient record.

### 2. Medication Database:

#### Browsing Medications:
- Access the medication database from the main menu.
- Search medications by name or active ingredient.
- View detailed medication information including form type and standard dosages.

#### Medication Types:
- **Tablets**: Solid oral medications with dosage in milligrams.
- **Liquids**: Liquid oral medications with dosage in milliliters.
- **Injections**: Injectable medications with dosage in milliliters.

### 3. Prescription Creation:

#### Creating a New Prescription:
- Select the patient for whom the prescription is being created.
- Click "New Prescription" button.
- Add medications to the prescription.
- Specify dosage and frequency for each medication.

#### Adding Medications:
- Search for medication in the database.
- Select the medication from results.
- Enter prescribed dosage and administration instructions.
- Click "Add to Prescription".

### 4. Interaction Checking:

#### Automatic Checks:
The system automatically performs three types of checks:

1. **Drug-Drug Interactions**:
   - Detects potentially harmful combinations.
   - Shows severity level and description.

2. **Drug-Allergy Interactions**:
   - Checks medications against patient's known allergies.
   - Prevents allergic reactions.

3. **Drug-Condition Interactions**:
   - Verifies medication safety with patient's medical conditions.
   - Warns about contraindications.

#### Understanding Alerts:

**Alert Levels**:
- **CRITICAL**: Severe interaction, prescription should not be dispensed.
- **MAJOR**: Significant interaction requiring physician review.
- **MODERATE**: Monitor patient closely for adverse effects.
- **MINOR**: Low-risk interaction, patient awareness sufficient.

**Alert Types**:
- **DRUG_DRUG**: Interaction between two medications.
- **DRUG_ALLERGY**: Medication conflicts with patient allergy.
- **DRUG_CONDITION**: Medication contraindicated for patient condition.

#### Responding to Alerts:
- Review alert details carefully.
- Consult interaction description for clinical guidance.
- Remove problematic medication if necessary.
- Seek alternative medications when critical alerts appear.
- Document decision if proceeding despite warnings.

### 5. Prescription Management:

#### Prescription Status:
- **DRAFT**: Prescription being created, not finalized.
- **ACTIVE**: Approved and currently in use.
- **COMPLETED**: Prescription course finished.
- **CANCELLED**: Prescription cancelled before completion.

#### Editing Prescriptions:
- Select prescription from patient's history.
- Modify medications or dosages as needed.
- Re-check for interactions after changes.

#### Prescription History:
- View all past prescriptions for a patient.
- Access historical interaction alerts.
- Track medication changes over time.

## Best Practices:

### For Safe Prescribing:
- Always review patient allergies before prescribing.
- Check patient's current medical conditions.
- Never ignore CRITICAL level alerts.
- Document reasons for overriding alerts.
- Update patient information regularly.

### For Optimal System Use:
- Keep medication database current.
- Regularly update interaction rules.
- Back up patient data frequently.
- Train staff on alert interpretation.
- Review system reports periodically.

## Keyboard Shortcuts:

- `Ctrl+N`: New prescription.
- `Ctrl+P`: Select patient.
- `Ctrl+M`: Open medication database.
- `Ctrl+S`: Save current prescription.
- `Ctrl+Q`: Quit application.
- `F1`: Help documentation.

## Troubleshooting:

### Common Issues:

#### Application Won't Start:
- Verify Java is installed correctly.
- Check Maven dependencies are downloaded.
- Review console output for error messages.

#### Data Not Loading:
- Ensure JSON files exist in `src/main/resources/data/`.
- Validate JSON file syntax.
- Check file permissions.

#### Interaction Checks Not Working:
- Verify `interaction-rules.json` is populated.
- Check medication IDs match between files.
- Review console for service errors.

## Getting Help:

- Consult `README.md` for quick reference.
- Review `CodeBaseIndex.md` for system architecture.
- Check `InstallationAndSetup.md` for configuration issues.
- Contact system administrator for technical support.
