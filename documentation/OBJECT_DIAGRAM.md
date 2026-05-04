# Audino Object Diagram:

## Purpose:
This diagram captures a concrete runtime snapshot centered on a selected patient, current draft prescription, prescribed drugs, generated alerts, and active service objects.

## PlantUML Source:
The PlantUML source is now maintained in:

- `documentation/OBJECT_DIAGRAM.puml`.

## Runtime Notes:
1. Snapshot now includes `MainController`, `DataService`, `InteractionEngine`, and `MedicationSearchEngine` runtime state.
2. Domain object fields are aligned with actual model fields (`patientId`, `prescriptionId`, `medicationId`, `alertLevel`, `acknowledged`).
3. Medication instances use concrete subclasses (`TabletMedication`, `LiquidMedication`) instead of generic placeholders.
4. Persistence boundary reflects PostgreSQL usage in runtime mode.
