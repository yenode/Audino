# Audino Activity Diagram

## Purpose
This diagram models the full activity path for initialization, patient selection, medication add workflow, parallel interaction checking, persistence branching, and shutdown.

## PlantUML Source
The PlantUML source is now maintained in:

- `documentation/ACTIVITY_DIAGRAM.puml`

## Behavioral Notes
1. Data load now explicitly includes runtime branch logic (`useExternalDataFallback`) for SQLite vs JSON sources.
2. Prescription creation follows the real get-or-create behavior in `handleAddMedication()`.
3. Dosage validation is represented as subtype-specific (`Medication.isValidDosage(...)`).
4. Interaction strategies execute in parallel and merge back into a sorted alert list.
5. Save path reflects `APPROVED` transition and full-table SQLite rewrite transaction.
