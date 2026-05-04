# Audino ER Diagram Reference:

## Purpose:
The relational entity relationship source for Audino is provided in ER_DIAGRAM.puml. The schema reflects runtime PostgreSQL tables used by the main application.

## Coverage:
1. patients, medications, prescriptions, prescribed_drugs, and interaction_rules entities are represented.
2. Primary keys, foreign keys, unique constraints, and cardinality are represented.
3. Rule payload relationship context is represented through dotted logical links.

## Render Command:
```bash
plantuml documentation/ER_DIAGRAM.puml
```

## Notes:
One active prescription header per patient is represented through unique patient_id in prescriptions.