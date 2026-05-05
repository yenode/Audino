# Audino ER Diagram Reference:

## Purpose:
The relational entity relationship source for Audino is provided in ER_DIAGRAM.puml. The schema reflects the fully normalized, runtime PostgreSQL tables used by the main application to ensure ACID compliance.

## Coverage:
1. Core entities: `patients`, `medications`, `prescriptions`, `prescribed_drugs`, `interaction_rules`, `users`.
2. Normalized relationship tables: `patient_allergies`, `patient_conditions`, `medication_ingredients`, `medication_identifiers`, `prescription_alerts`, and `audit_logs`.
3. Primary keys, foreign keys, and referential constraints (e.g., `ON DELETE CASCADE`) are represented.
4. The many-to-many normalizations are fully documented.

## Render Command:
```bash
plantuml documentation/ER_DIAGRAM.puml
```

## Notes:
The shift from JSON columns to strict relational mapping allows high-performance constraint matching, rule validation, and secure audit tracking (HIPAA/GDPR compliance).