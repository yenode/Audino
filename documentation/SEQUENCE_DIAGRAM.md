# Audino Sequence Diagram:

## Purpose:
This diagram shows the implementation-aligned end-to-end sequence for startup, patient selection, medication suggestion/addition, parallel interaction checks, save, and shutdown.

## PlantUML Source:
The PlantUML source is now maintained in:

- `documentation/SEQUENCE_DIAGRAM.puml`.

## Flow Notes:
1. Startup includes `ConfigurationManager.initialize()` and `MainController.initialize()`.
2. Data loading branches by runtime mode: PostgreSQL in normal execution and JSON resources during Surefire tests.
3. Medication suggestion goes through `DataService.suggestMedications()` and `MedicationSearchEngine.suggest()`.
4. Interaction checks run in parallel using three strategies via `InteractionEngine.checkAllInteractionsAsync(...)`.
5. Save path reflects `savePrescription()` replacement-by-patient behavior and full PostgreSQL transaction rewrite.
